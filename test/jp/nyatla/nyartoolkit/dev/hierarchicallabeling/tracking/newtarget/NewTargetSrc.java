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
		public AreaTargetSrcHolder.AreaSrcItem area_src;
		public int match_index;
	}
	private AreaTargetSrcHolder _ref_area_pool;
	public NewTargetSrc.NewSrcItem pushSrcTarget(AreaTargetSrcHolder.AreaSrcItem i_item)
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
	public NewTargetSrc(int i_size,AreaTargetSrcHolder i_area_pool) throws NyARException
	{
		this._ref_area_pool=i_area_pool;
		super.initInstance(i_size, NewTargetSrc.NewSrcItem.class);
	}
	public void clear()
	{
		//所有するオブジェクトを開放してからクリア処理
		for(int i=this._length-1;i>=0;i--){
			if(this._items[i].area_src!=null){
				this._ref_area_pool.deleteObject(this._items[i].area_src);
			}
		}
		super.clear();
	}
}