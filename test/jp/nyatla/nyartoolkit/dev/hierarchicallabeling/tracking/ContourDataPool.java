package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARObjectPool;



/*
 * 輪郭情報を保管します。
 * このクラスの要素は、他の要素から参照する可能性があります。
 */
public class ContourDataPool extends NyARObjectPool<ContourDataPool.ContourTargetSrcItem>
{	
	/**
	 * 輪郭ソース1個を格納するクラスです。
	 *
	 */
	public static class ContourTargetSrcItem
	{
		/**
		 * 輪郭要素1個を格納するデータ型です。
		 */
		public static class CoordData extends NyARVecLinear2d
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
			/**
			 * 輪郭配列から、から、キーのベクトル(絶対値の大きいベクトル)を順序を壊さずに抽出します。
			 * @param i_vecpos
			 * @param i_len
			 * @param o_index
			 * int[4]以上の配列を渡します。
			 */
			public static void getKeyCoordInfoIndex(CoordData[] i_vecpos,int i_len,int[] o_index)
			{
				assert(o_index.length<i_vecpos.length);
				int i;
				int out_len=o_index.length;
				int out_len_1=out_len-1;
				for(i=out_len-1;i>=0;i--){
					o_index[i]=0;			
				}
				//sqdistでソートする
				for(i=0;i<out_len_1;i++){
					if(i_vecpos[o_index[i]].sq_dist<i_vecpos[o_index[i+1]].sq_dist){
						int t=o_index[i];
						o_index[i]=o_index[i+1];
						o_index[i+1]=t;
						i=0;
					}
				}
				//先に4個をsq_distでソートしながら格納
				for(i=out_len;i<i_len;i++){
					//配列の値と比較
					for(int i2=out_len_1;i2>=0;i2--){
						if(i_vecpos[i].sq_dist>i_vecpos[o_index[i2]].sq_dist){				
							//値挿入の為のシフト
							for(int i3=out_len-2;i3>i2;i3--){
								o_index[i3+1]=o_index[i3];
							}
							//設定
							o_index[i2]=i;
						}
					}
				}
				//idxでソート
				for(i=0;i<out_len_1;i++){
					if(o_index[i]<o_index[i+1]){
						int t=o_index[i];
						o_index[i]=o_index[i+1];
						o_index[i+1]=t;
						i=0;
					}
				}
				return;
			}		
		}		
		//関連する領域ソースへのポインタ
		public NyARIntPoint2d coord_center=new NyARIntPoint2d();
		public CoordData[] vecpos=CoordData.createArray(100);
		public int vecpos_length;
		/*distの合計値*/
		public double sq_dist_sum;
		/*部分矩形の左上インデクス*/
		public NyARIntPoint2d image_lt=new NyARIntPoint2d();
		//
		//制御部
		private ContourDataPool _pool;
		public ContourTargetSrcItem(ContourDataPool i_pool)
		{
			this._pool=i_pool;
		}
		/**
		 * このインスタンスを開放します。
		 */
		public void deleteMe()
		{
			this._pool.deleteObject(this);
		}

	}
	private final NyARContourPickup _cpickup=new NyARContourPickup();

	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @param i_cood_max
	 * 輪郭ベクトルの最大数
	 * @throws NyARException
	 */
	public ContourDataPool(int i_size,int i_cood_max) throws NyARException
	{
		this._coord=NyARIntPoint2d.createArray(i_cood_max);
		super.initInstance(i_size,ContourTargetSrcItem.class);
	}
	protected ContourTargetSrcItem createElement()
	{
		return new ContourTargetSrcItem(this);
	}
	private NyARIntPoint2d[] _coord;
	
	/**
	 * 
	 * @param i_item
	 * @param i_hrect
	 * @param i_src_raster_reader
	 * 元画像を格納したラスタを指定します。
	 * @param i_th
	 * 
	 * @param info
	 * @return
	 * @throws NyARException
	 */
	public ContourTargetSrcItem newSrcTarget(AreaDataPool.AreaDataItem i_item,HierarchyRect i_hrect,NyARGrayscaleRaster i_parcical_raster,NyARGrayscaleRaster i_base_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException
	{
		NyARIntPoint2d[] coord=this._coord;
		ContourTargetSrcItem item=this.newObject();
		if(item==null){
			return null;
		}
//		item._ref_area_src=i_item;
		item.image_lt.x=i_hrect.x;
		item.image_lt.y=i_hrect.y;

		//輪郭抽出
		int coord_len=this._cpickup.getContour(i_parcical_raster,i_th,info.entry_x,info.clip_t,coord);
		if(coord_len==coord.length){
			//輪郭線MAXならなにもできないね。
			this.deleteObject(item);
			return null;
		}
		NyARIntSize base_size=i_base_raster.getSize();
		NyARVectorReader_INT1D_GRAY_8 vr=i_base_raster.getVectorReader();
		NyARIntRect tmprect=new NyARIntRect();
		//輪郭→ベクトルの変換
		

		//ベクトル化
		int skip=i_hrect.dot_skip;
		tmprect.w=tmprect.h=skip*2;


		int number_of_data=1;
		//0個目のベクトル
		tmprect.x=i_hrect.x+(coord[0].x-1)*skip;
		tmprect.y=i_hrect.y+(coord[0].y-1)*skip;
		tmprect.clip(1,1,base_size.w-1,base_size.h-1);
		vr.getAreaVector8(tmprect,item.vecpos[0]);

		int ccx=0;
		int ccy=0;
		ContourTargetSrcItem.CoordData prev_vec_ptr    = item.vecpos[0];
		ContourTargetSrcItem.CoordData current_vec_ptr = null;

		//ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
		for(int i=1;i<coord_len;i++){
			current_vec_ptr = item.vecpos[number_of_data];
			//ベクトル定義矩形を作る。
			tmprect.x=i_hrect.x+(coord[i].x-1)*skip;
			tmprect.y=i_hrect.y+(coord[i].y-1)*skip;
			tmprect.clip(1,1,base_size.w-1,base_size.h-1);
			//ベクトル取得
			vr.getAreaVector8(tmprect,current_vec_ptr);
			
			//類似度判定
			if(getVecCos(prev_vec_ptr,current_vec_ptr)<0.99){
				//相関なし
				number_of_data++;
				prev_vec_ptr=current_vec_ptr;

			}else{
				//相関あり
				prev_vec_ptr.x=(prev_vec_ptr.x+current_vec_ptr.x)/2;
				prev_vec_ptr.y=(prev_vec_ptr.y+current_vec_ptr.y)/2;
				prev_vec_ptr.dx=(prev_vec_ptr.dx+current_vec_ptr.dx);
				prev_vec_ptr.dy=(prev_vec_ptr.dy+current_vec_ptr.dy);
			}
			//輪郭中心を出すための計算
			ccx+=this._coord[i].x;
			ccy+=this._coord[i].y;
//輪郭ベクトルバッファの最大を超えたら失敗
		}
		//ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
		prev_vec_ptr=item.vecpos[0];
		if(getVecCos(current_vec_ptr,prev_vec_ptr)<0.99){
			//相関なし
		}else{
			//相関あり
			prev_vec_ptr.x=(prev_vec_ptr.x+current_vec_ptr.x)/2;
			prev_vec_ptr.y=(prev_vec_ptr.y+current_vec_ptr.y)/2;
			prev_vec_ptr.dx=(prev_vec_ptr.dx+current_vec_ptr.dx);
			prev_vec_ptr.dy=(prev_vec_ptr.dy+current_vec_ptr.dy);
			number_of_data--;
		}
		//輪郭中心位置の保存
		item.coord_center.x=ccx/coord_len;
		item.coord_center.y=ccy/coord_len;
		item.vecpos_length=number_of_data;
		//vectorのsq_distを必要なだけ計算
		double d=0;
		for(int i=number_of_data-1;i>=0;i--)
		{
			current_vec_ptr=item.vecpos[i];
			//ベクトルの法線を取る。
			current_vec_ptr.normalVec(current_vec_ptr);
			//sqdistを計算
			current_vec_ptr.sq_dist=current_vec_ptr.dx*current_vec_ptr.dx+current_vec_ptr.dy*current_vec_ptr.dy;
			d+=current_vec_ptr.sq_dist;
		}
		//sq_distの合計を計算
		item.sq_dist_sum=d;
		return item;
	}	
	private double getVecCos(NyARVecLinear2d i_v1,NyARVecLinear2d i_v2)
	{
		double x1=i_v1.dx;
		double y1=i_v1.dy;
		double x2=i_v2.dx;
		double y2=i_v2.dy;
		double d=(x1*x2+y1*y2)/Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2));
		return d;
		
	}
}