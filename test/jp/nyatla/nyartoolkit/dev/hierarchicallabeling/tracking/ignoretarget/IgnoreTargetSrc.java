package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder.AppearSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;

public class IgnoreTargetSrc extends NyARObjectStack<IgnoreTargetSrc.NyARIgnoreSrcItem>
{
	public static class NyARIgnoreSrcItem
	{
		public AreaTargetSrcHolder.AppearSrcItem _ref_area_src;
		public int match_index;

	}
	public IgnoreTargetSrc.NyARIgnoreSrcItem pushSrcTarget(AreaTargetSrcHolder.AppearSrcItem i_item)
	{
		IgnoreTargetSrc.NyARIgnoreSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item._ref_area_src=i_item;
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