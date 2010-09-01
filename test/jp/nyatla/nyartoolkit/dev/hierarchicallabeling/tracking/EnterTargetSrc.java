package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList;

public class EnterTargetSrc extends NyARObjectStack<EnterTargetSrc.EnterSrcItem>
{
	public static class EnterSrcItem
	{
		public AreaTargetSrcPool.AreaTargetSrcItem area_src;
		public void attachToTarget(NewTargetList.NewTargetItem o_target)
		{
			assert(this.area_src!=null);
			o_target.ref_area=this.area_src;
			this.area_src=null;
		}
	}
	public EnterTargetSrc.EnterSrcItem pushSrcTarget(AreaTargetSrcPool.AreaTargetSrcItem i_item)
	{
		EnterTargetSrc.EnterSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.area_src=i_item;
		return item;
	}
	protected EnterSrcItem createElement()
	{
		return new EnterSrcItem();
	}
	public EnterTargetSrc(int i_size) throws NyARException
	{
		super.initInstance(i_size, EnterTargetSrc.EnterSrcItem.class);
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