package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder;

public class NewTargetSrc extends NyARObjectStack<NewTargetSrc.NewSrcItem>
{
	public static class NewSrcItem
	{
		public AreaTargetSrcHolder.AppearSrcItem _ref_area_src;
		public int match_index;
	}
	public NewTargetSrc.NewSrcItem pushSrcTarget(AreaTargetSrcHolder.AppearSrcItem i_item)
	{
		NewTargetSrc.NewSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item._ref_area_src=i_item;
		item.match_index=-1;
		return item;
	}
	protected NewSrcItem createElement()
	{
		return new NewSrcItem();
	}
	public NewTargetSrc(int i_size) throws NyARException
	{
		super.initInstance(i_size, NewTargetSrc.NewSrcItem.class);
	}

}