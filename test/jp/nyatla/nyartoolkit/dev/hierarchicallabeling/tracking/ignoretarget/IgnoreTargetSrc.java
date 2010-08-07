package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc.AppearSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;

public class IgnoreTargetSrc extends NyARObjectStack<IgnoreTargetSrc.NyARIgnoreSrcItem>
{
	public static class NyARIgnoreSrcItem
	{
		public NyARIntRect area=new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		public int match_index;

	}
	public IgnoreTargetSrc.NyARIgnoreSrcItem pushSrcTarget(AppearTargetSrc.AppearSrcItem i_item)
	{
		IgnoreTargetSrc.NyARIgnoreSrcItem item=this.prePush();
		if(item==null){
			return null;
		}		
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		item.match_index=-1;
		return item;
	}
	protected NyARIgnoreSrcItem createElement()
	{
		return new NyARIgnoreSrcItem();
	}
	public IgnoreTargetSrc(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARIgnoreSrcItem.class);
	}

}