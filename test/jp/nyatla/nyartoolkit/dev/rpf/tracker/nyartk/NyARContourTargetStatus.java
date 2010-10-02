package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;

/**
 * 輪郭ソース1個を格納するクラスです。
 *
 */
public class NyARContourTargetStatus extends NyARTargetStatus
{
	private NyARContourTargetStatusPool.WorkObject _shared;
	/**
	 * 輪郭ベクトル配列を格納するクラスです。
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
	public CoordData[] vecpos=CoordData.createArray(100);
	/**
	 * ベクトル配列の有効長です。
	 */
	public int vecpos_length;
	
	
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
	 * 0.98は約10度
	 */
	private final double _ANG_TH=0.98;
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_raster
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */
	public boolean setValue(NyARGrayscaleRaster i_raster,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		NyARContourTargetStatusPool.WorkObject sh=this._shared;
		NyARIntPoint2d[] coord=sh.coord_buf;
		//輪郭抽出
		int coord_len=sh.cpickup.getContour(i_sample.ref_raster,i_sample.lebeling_th,i_sample.entry_pos.x,i_sample.entry_pos.y,sh.coord_buf);
		if(coord_len==sh.coord_buf.length){
			//輪郭線MAXならなにもできないね。
			return false;
		}
		NyARIntSize base_size=i_raster.getSize();
		NyARVectorReader_INT1D_GRAY_8 vr=i_raster.getVectorReader();
		NyARIntRect tmprect=new NyARIntRect();
		//輪郭→ベクトルの変換
		

		//ベクトル化
		int MAX_COORD=this.vecpos.length;
		int skip=i_sample.resolution;
		tmprect.w=tmprect.h=skip*2;


		int number_of_data=1;
		//0個目のベクトル
		tmprect.x=(coord[0].x-1)*skip;
		tmprect.y=(coord[0].y-1)*skip;
		tmprect.clip(1,1,base_size.w-2,base_size.h-2);
		vr.getAreaVector8(tmprect,this.vecpos[0]);

//		int ccx=0;
//		int ccy=0;
		NyARContourTargetStatus.CoordData prev_vec_ptr    = this.vecpos[0];
		NyARContourTargetStatus.CoordData current_vec_ptr = null;

		//ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
		for(int i=1;i<coord_len;i++){
			current_vec_ptr = this.vecpos[number_of_data];
			//ベクトル定義矩形を作る。
			tmprect.x=(coord[i].x-1)*skip;
			tmprect.y=(coord[i].y-1)*skip;
			tmprect.w=tmprect.h=skip*2;
			tmprect.clip(1,1,base_size.w-2,base_size.h-2);
			//ベクトル取得
			vr.getAreaVector8(tmprect,current_vec_ptr);
			
			//類似度判定
			if(getVecCos(prev_vec_ptr,current_vec_ptr)<_ANG_TH){
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
		prev_vec_ptr=this.vecpos[0];
		if(getVecCos(current_vec_ptr,prev_vec_ptr)<_ANG_TH){
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
		this.vecpos_length=number_of_data;
		//vectorのsq_distを必要なだけ計算
		double d=0;
		for(int i=number_of_data-1;i>=0;i--)
		{
			current_vec_ptr=this.vecpos[i];
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
	 */
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
	}
	private double getVecCos(NyARPointVector2d i_v1,NyARPointVector2d i_v2)
	{
		double x1=i_v1.dx;
		double y1=i_v1.dy;
		double x2=i_v2.dx;
		double y2=i_v2.dy;
		double d=(x1*x2+y1*y2)/Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2));
		return d;
		
	}
}
