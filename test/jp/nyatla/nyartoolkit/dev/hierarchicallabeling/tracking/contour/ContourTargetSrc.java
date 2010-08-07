package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc.AppearSrcItem;


public class ContourTargetSrc extends NyARObjectStack<ContourTargetSrc.ContourTargetSrcItem>
{
	public static class ContourTargetSrcItem
	{
		public NyARIntRect area=new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		public int area_sq_diagonal;
		/*部分矩形の左上インデクス*/
		public NyARIntPoint2d image_lt=new NyARIntPoint2d();

		public int skip;
		//輪郭線の特徴
		public NyARIntPoint2d[] coord;
		public int coord_len;
		public NyARIntPoint2d coord_center=new NyARIntPoint2d();
		public ContourTargetSrcItem(int i_max_coord)
		{
			this.coord=NyARIntPoint2d.createArray(i_max_coord);
		}
		/**
		 * 値を初期化する。
		 * @param i_tick
		 * @param i_item
		 */
	}
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	
	protected ContourTargetSrcItem createElement(Object i_param)
	{
		int size=(Integer)i_param;
		return new ContourTargetSrcItem(size);
	}
	public ContourTargetSrcItem pushTarget(AppearTargetSrc.AppearSrcItem i_item,HierarchyRect i_hrect,NyARGrayscaleRaster i_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException
	{
		ContourTargetSrcItem item=this.prePush();
		if(item==null){
			return null;
		}		
		item.skip=i_hrect.dot_skip;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		item.area_sq_diagonal=i_item.area_sq_diagonal;
		item.image_lt.x=i_hrect.x;
		item.image_lt.y=i_hrect.y;

		//輪郭抽出
		int n=this._cpickup.getContour(i_raster,i_th,info.entry_x,info.clip_t,item.coord);
		if(n==item.coord.length){
			//輪郭線MAXならなにもできないね。
			this.pop();
			return null;
		}
		item.coord_len=n;
		//輪郭線の中央値を出す
		int x=0;
		int y=0;
		for(int i=n-1;i>=0;i--){
			x+=item.coord[i].x;
			y+=item.coord[i].y;
		}
		item.coord_center.x=x/n;
		item.coord_center.y=y/n;
		return item;
	}
	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @param i_cood_max
	 * 輪郭バッファのサイズ
	 * @throws NyARException
	 */
	public ContourTargetSrc(int i_size,int i_cood_max) throws NyARException
	{
		super.initInstance(i_size,ContourTargetSrcItem.class,new Integer(i_cood_max));
	}

}