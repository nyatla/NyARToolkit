package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LrlsGsRaster;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.NyARVectorReader_INT1D_GRAY_8;

/**
 * 輪郭ソース1個を格納するクラスです。
 *
 */
public class NyARContourTargetStatus extends NyARTargetStatus
{
	private NyARContourTargetStatusPool.WorkObject _shared;
	
	public static class VectorCoords
	{
		public int length;
		public CoordData item[];
		public VectorCoords(int i_length)
		{
			this.length=0;
			this.item=CoordData.createArray(i_length);
		}
		/**
		 * 輪郭配列から、から、キーのベクトル(絶対値の大きいベクトル)を順序を壊さずに抽出します。
		 * @param i_vecpos
		 * 抽出元の
		 * @param i_len
		 * @param o_index
		 * インデクス番号を受け取る配列。受け取るインデックスの個数は、この配列の数と同じになります。
		 */
		public void getKeyCoordIndexes(int[] o_index)
		{
			CoordData[] vp=this.item;
			assert(o_index.length<=this.length);
			int i;
			int out_len=o_index.length;
			int out_len_1=out_len-1;
			for(i=out_len-1;i>=0;i--){
				o_index[i]=i;			
			}
			//sqdistでソートする(B->S)
			for(i=0;i<out_len_1;){
				if(vp[o_index[i]].sq_dist<vp[o_index[i+1]].sq_dist){
					int t=o_index[i];
					o_index[i]=o_index[i+1];
					o_index[i+1]=t;
					i=0;
					continue;
				}
				i++;
			}
			//先に4個をsq_distでソートしながら格納
			for(i=out_len;i<this.length;i++){
				//配列の値と比較
				for(int i2=0;i2<out_len;i2++){
					if(vp[i].sq_dist>vp[o_index[i2]].sq_dist){				
						//値挿入の為のシフト
						for(int i3=out_len-1;i3>i2;i3--){
							o_index[i3]=o_index[i3-1];
						}
						//設定
						o_index[i2]=i;
						break;
					}
				}
			}
			//idxでソート
			for(i=0;i<out_len_1;){
				if(o_index[i]>o_index[i+1]){
					int t=o_index[i];
					o_index[i]=o_index[i+1];
					o_index[i+1]=t;
					i=0;
					continue;
				}
				i++;
			}
			return;
		}
		/**
		 * 最も大きいベクトル成分のインデクスを返します。
		 * @return
		 */
		public int getMaxCoordIndex()
		{
			CoordData[] vp=this.item;
			int index=0;
			double max_dist=vp[0].sq_dist;
			for(int i=this.length-1;i>0;i--){
				if(max_dist<vp[i].sq_dist){
					max_dist=vp[index].sq_dist;
					index=i;
				}
			}
			return index;
		}
		
	}
	
	/**
	 * データ型です。
	 * 輪郭ベクトルを格納します。
	 */
	public static class CoordData extends NyARPointVector2d
	{
		/**
		 * ベクトルの2乗値です。輪郭の強度値にもなります。
		 */
		public double sq_dist;
		public static CoordData[] createArray(int i_length)
		{
			CoordData[] r=new CoordData[i_length];
			for(int i=0;i<i_length;i++){
				r[i]=new CoordData();
			}
			return r;
		}
		
	}
	/**
	 * ベクトル要素を格納する配列です。
	 */
	public VectorCoords vecpos=new VectorCoords(100);
	/**
	 * ベクトル配列の有効長です。
	 */
//	public int vecpos_length;
	
	
	//
	//制御部

	/**
	 * @param i_ref_pool_operator
	 * @param i_shared
	 * 共有ワークオブジェクトを指定します。
	 * 
	 */
	public NyARContourTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator,NyARContourTargetStatusPool.WorkObject i_shared)
	{
		super(i_ref_pool_operator);
		this._shared=i_shared;
	}
	/**
	 * ベクトル結合時の、敷居値(cos(x)の値)
	 * 0.99は約?度
	 */
	public boolean setValue(LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		LrlsGsRaster r=(LrlsGsRaster)i_sample.ref_raster;
		return r.baseraster.getVectorReader().traceConture(r, i_sample.lebeling_th, i_sample.entry_pos, vecpos);
	}	
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_raster
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */
	public boolean setValue_xxx(LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		NyARContourTargetStatusPool.WorkObject sh=this._shared;
		NyARIntPoint2d[] coord=sh.coord_buf;
		//輪郭抽出
		int coord_len=sh.cpickup.getContour(i_sample.ref_raster,i_sample.lebeling_th,i_sample.entry_pos.x,i_sample.entry_pos.y,sh.coord_buf);
		if(coord_len==sh.coord_buf.length){
			//輪郭線MAXならなにもできないね。
			return false;
		}
		LrlsGsRaster r=(LrlsGsRaster)i_sample.ref_raster;
		NyARIntSize base_size=r.baseraster.getSize();
		NyARVectorReader_INT1D_GRAY_8 vr=r.baseraster.getVectorReader();
		NyARIntRect tmprect=new NyARIntRect();
		//輪郭→ベクトルの変換
		

		//ベクトル化
		int MAX_COORD=this.vecpos.item.length;
		int skip=i_sample.resolution;
		tmprect.w=tmprect.h=skip*2;


		int number_of_data=1;
		//0個目のベクトル
		tmprect.x=(coord[0].x-1)*skip;
		tmprect.y=(coord[0].y-1)*skip;
		tmprect.clip(1,1,base_size.w-2,base_size.h-2);
		vr.getAreaVector8(tmprect,this.vecpos.item[0]);

//		int ccx=0;
//		int ccy=0;
		NyARContourTargetStatus.CoordData prev_vec_ptr    = this.vecpos.item[0];
		NyARContourTargetStatus.CoordData current_vec_ptr = null;

		//ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
		for(int i=1;i<coord_len;i++){
			current_vec_ptr = this.vecpos.item[number_of_data];
			//ベクトル定義矩形を作る。
			tmprect.x=(coord[i].x-1)*skip;
			tmprect.y=(coord[i].y-1)*skip;
			tmprect.w=tmprect.h=skip*2;
			tmprect.clip(1,1,base_size.w-2,base_size.h-2);
			//ベクトル取得
			vr.getAreaVector8(tmprect,current_vec_ptr);
			
			//類似度判定
			if(NyARPointVector2d.getVecCos(prev_vec_ptr,current_vec_ptr)<0.99){
				//相関なし
				number_of_data++;
				prev_vec_ptr=current_vec_ptr;

			}else{
				//相関あり(ベクトルの統合)
				prev_vec_ptr.x=(prev_vec_ptr.x+current_vec_ptr.x)/2;
				prev_vec_ptr.y=(prev_vec_ptr.y+current_vec_ptr.y)/2;
				prev_vec_ptr.dx=(prev_vec_ptr.dx+current_vec_ptr.dx);
				prev_vec_ptr.dy=(prev_vec_ptr.dy+current_vec_ptr.dy);
			}
//			//輪郭中心を出すための計算
//			ccx+=this._coord[i].x;
//			ccy+=this._coord[i].y;
			if(number_of_data==MAX_COORD){
				//輪郭ベクトルバッファの最大を超えたら失敗
				return false;
			}
		}
		//ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
		prev_vec_ptr=this.vecpos.item[0];
		if(NyARPointVector2d.getVecCos(current_vec_ptr,prev_vec_ptr)<0.99){
			//相関なし
		}else{
			//相関あり(ベクトルの統合)
			prev_vec_ptr.x=(prev_vec_ptr.x+current_vec_ptr.x)/2;
			prev_vec_ptr.y=(prev_vec_ptr.y+current_vec_ptr.y)/2;
			prev_vec_ptr.dx=(prev_vec_ptr.dx+current_vec_ptr.dx);
			prev_vec_ptr.dy=(prev_vec_ptr.dy+current_vec_ptr.dy);
			number_of_data--;
		}
		//輪郭中心位置の保存
//		item.coord_center.x=ccx/coord_len;
//		item.coord_center.y=ccy/coord_len;
		this.vecpos.length=number_of_data;
		//vectorのsq_distを必要なだけ計算
		double d=0;
		for(int i=number_of_data-1;i>=0;i--)
		{
			current_vec_ptr=this.vecpos.item[i];
			//ベクトルの法線を取る。
			current_vec_ptr.OrthogonalVec(current_vec_ptr);
			//sqdistを計算
			current_vec_ptr.sq_dist=current_vec_ptr.dx*current_vec_ptr.dx+current_vec_ptr.dy*current_vec_ptr.dy;
			d+=current_vec_ptr.sq_dist;
		}
		//sq_distの合計を計算
//		item.sq_dist_sum=d;
		return true;
	}	 
 
	
	
	/**
	 * 輪郭配列から、から、キーのベクトル(絶対値の大きいベクトル)を順序を壊さずに抽出します。
	 * @param i_vecpos
	 * 抽出元の
	 * @param i_len
	 * @param o_index
	 * インデクス番号を受け取る配列。受け取るインデックスの個数は、この配列の数と同じになります。
	 *//*
	public void getKeyCoordIndexes(int i_len,int[] o_index)
	{
		CoordData[] vp=this.vecpos;
		assert(o_index.length<vp.length);
		int i;
		int out_len=o_index.length;
		int out_len_1=out_len-1;
		for(i=out_len-1;i>=0;i--){
			o_index[i]=i;			
		}
		//sqdistでソートする(B->S)
		for(i=0;i<out_len_1;){
			if(vp[o_index[i]].sq_dist<vp[o_index[i+1]].sq_dist){
				int t=o_index[i];
				o_index[i]=o_index[i+1];
				o_index[i+1]=t;
				i=0;
				continue;
			}
			i++;
		}
		//先に4個をsq_distでソートしながら格納
		for(i=out_len;i<i_len;i++){
			//配列の値と比較
			for(int i2=0;i2<out_len;i2++){
				if(vp[i].sq_dist>vp[o_index[i2]].sq_dist){				
					//値挿入の為のシフト
					for(int i3=out_len-1;i3>i2;i3--){
						o_index[i3]=o_index[i3-1];
					}
					//設定
					o_index[i2]=i;
					break;
				}
			}
		}
		//idxでソート
		for(i=0;i<out_len_1;){
			if(o_index[i]>o_index[i+1]){
				int t=o_index[i];
				o_index[i]=o_index[i+1];
				o_index[i+1]=t;
				i=0;
				continue;
			}
			i++;
		}
		return;
	}*/
}
