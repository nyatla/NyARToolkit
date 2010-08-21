package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder;

public class EnterTargetSrc extends NyARObjectStack<EnterTargetSrc.EnterSrcItem>
{
	public static class EnterSrcItem
	{
		public AreaTargetSrcHolder.AreaSrcItem ref_area_src;
	}
	private AreaTargetSrcHolder _ref_area_pool;
	public EnterTargetSrc.EnterSrcItem pushSrcTarget(AreaTargetSrcHolder.AreaSrcItem i_item)
	{
		EnterTargetSrc.EnterSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.ref_area_src=i_item;
		return item;
	}
	protected EnterSrcItem createElement()
	{
		return new EnterSrcItem();
	}
	public EnterTargetSrc(int i_size,AreaTargetSrcHolder i_ref_area_pool) throws NyARException
	{
		this._ref_area_pool=i_ref_area_pool;
		super.initInstance(i_size, EnterTargetSrc.EnterSrcItem.class);
	}
	public void clear()
	{
		//所有するオブジェクトを開放してからクリア処理
		for(int i=this._length-1;i>=0;i--){
			if(this._items[i].ref_area_src!=null){
				this._ref_area_pool.deleteObject(this._items[i].ref_area_src);
			}
		}
		super.clear();
	}
}