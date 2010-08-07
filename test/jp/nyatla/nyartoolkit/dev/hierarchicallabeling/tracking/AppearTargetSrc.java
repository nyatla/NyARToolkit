package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;

/**
 * 新しくトラックするべきアイテムを集約するクラス
 *
 */
public class AppearTargetSrc extends NyARObjectStack<AppearTargetSrc.AppearSrcItem>
{
	public static class AppearSrcItem
	{
		public NyARIntRect    area  =new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		/**
		 * エリア対角距離の2乗
		 */
		public int area_sq_diagonal;
	}
	
	public AppearTargetSrc.AppearSrcItem pushSrcTarget(HierarchyRect i_imgmap,NyARRleLabelFragmentInfo info)
	{
		AppearTargetSrc.AppearSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		int skip=i_imgmap.dot_skip;
		item.area.x=info.clip_l*skip+i_imgmap.x;
		item.area.y=info.clip_t*skip+i_imgmap.y;
		item.area.w=(info.clip_r-info.clip_l)*skip;
		item.area.h=(info.clip_b-info.clip_t)*skip;
		item.area_center.x=item.area.x+item.area.w/2;
		item.area_center.y=item.area.y+item.area.h/2;
		item.area_sq_diagonal=item.area.w*item.area.w+item.area.h*item.area.h;
		return item;		
	}
	protected AppearTargetSrc.AppearSrcItem createElement()
	{
		return new AppearTargetSrc.AppearSrcItem();
	}
	public AppearTargetSrc(int i_size) throws NyARException
	{
		super.initInstance(i_size,AppearTargetSrc.AppearSrcItem.class);
	}
}