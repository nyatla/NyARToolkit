package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcPool;

public class NewTargetSrc extends NyARObjectStack<NewTargetSrc.NewSrcItem>
{
	public static class NewSrcItem
	{
		public AreaTargetSrcPool.AreaTargetSrcItem area_src;
		public int match_index;
		/**
		 * このオブジェクトの持つデータを、o_targetに割り付けます。
		 * @param o_target
		 */
		public void attachToTarget(NewTargetList.NewTargetItem o_target)
		{
			o_target.ref_area.deleteMe();
			o_target.ref_area=this.area_src;
			this.area_src=null;
			return;
		}
	}
	public NewTargetSrc.NewSrcItem pushSrcTarget(AreaTargetSrcPool.AreaTargetSrcItem i_item)
	{
		NewTargetSrc.NewSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.area_src=i_item;
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