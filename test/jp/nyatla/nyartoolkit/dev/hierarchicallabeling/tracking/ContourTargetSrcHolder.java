package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARObjectPool;



/*
 * 輪郭情報を保管します。
 * このクラスの要素は、他の要素から参照する可能性があります。
 */
public class ContourTargetSrcHolder extends NyARObjectPool<ContourTargetSrcHolder.ContourTargetSrcItem>
{	
	public static class ContourTargetSrcItem
	{
		//関連する領域ソースへのポインタ
		public AreaTargetSrcHolder.AreaSrcItem _ref_area_src;
		public NyARIntPoint2d coord_center=new NyARIntPoint2d();
		public NyARPointVector2d[] vecpos=NyARPointVector2d.createArray(100);
		public int vecpos_length;
		/*部分矩形の左上インデクス*/
		public NyARIntPoint2d image_lt=new NyARIntPoint2d();
		/**
		 * ベクトル配列を点集合に変換して返します。
		 * @param o_vertex
		 */
		public void getVertex(NyARIntPoint2d o_vertex)
		{
			//ベクトルに直行する2つのベクトルを90度傾けての交点を計算
			for(int i=0;i<vecpos_length-1;i++){
				
			}
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
	public ContourTargetSrcHolder(int i_size,int i_cood_max) throws NyARException
	{
		this._coord=NyARIntPoint2d.createArray(i_cood_max);
		super.initInstance(i_size,ContourTargetSrcItem.class);
	}
	protected ContourTargetSrcItem createElement()
	{
		return new ContourTargetSrcItem();
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
	public ContourTargetSrcItem newSrcTarget(AreaTargetSrcHolder.AreaSrcItem i_item,HierarchyRect i_hrect,NyARGrayscaleRaster i_parcical_raster,NyARGrayscaleRaster i_base_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException
	{
		NyARIntPoint2d[] coord=this._coord;
		ContourTargetSrcItem item=this.newObject();
		if(item==null){
			return null;
		}
		item._ref_area_src=i_item;
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
		NyARPointVector2d prev_vec_ptr    = item.vecpos[0];
		NyARPointVector2d current_vec_ptr = null;

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
		return item;
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