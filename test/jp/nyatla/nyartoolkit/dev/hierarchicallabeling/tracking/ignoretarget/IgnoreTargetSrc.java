package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList;

public class IgnoreTargetSrc extends NyARObjectStack<IgnoreTargetSrc.NyARIgnoreSrcItem>
{
	public static class NyARIgnoreSrcItem
	{
		public AreaDataPool.AreaDataItem area_src;
		public void attachToTarget(IgnoreTargetList.IgnoreTargetItem o_output)
		{
			o_output.ref_area.deleteMe();
			o_output.ref_area=this.area_src;
		}
	}
	public IgnoreTargetSrc.NyARIgnoreSrcItem pushSrcTarget(AreaDataPool.AreaDataItem i_item)
	{
		IgnoreTargetSrc.NyARIgnoreSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.area_src=i_item;
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
	public void clear()
	{
		//所有するオブジェクトを開放してからクリア処理
		for(int i=this._length-1;i>=0;i--){
			if(this._items[i].area_src!=null){
				this._items[i].area_src.deleteMe();
			}
		}
		super.clear();
	}
}