package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourTargetSrcHolder;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder.AreaSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;

public class ContoureTargetSrc extends NyARObjectStack<ContoureTargetSrc.ContoureTargetSrcItem>
{
	public static class ContoureTargetSrcItem
	{
		public AreaTargetSrcHolder.AreaSrcItem area_src;
		public ContourTargetSrcHolder.ContourTargetSrcItem contour_src;
	}
	private AreaTargetSrcHolder _ref_area_pool;
	private ContourTargetSrcHolder _ref_contoure_pool;
	
	public ContoureTargetSrc.ContoureTargetSrcItem pushSrcTarget(ContourTargetSrcHolder.ContourTargetSrcItem i_item)
	{
		ContoureTargetSrc.ContoureTargetSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.area_src=i_item._ref_area_src;
		item.contour_src=i_item;
		return item;
	}
	protected ContoureTargetSrcItem createElement()
	{
		return new ContoureTargetSrcItem();
	}
	public ContoureTargetSrc(int i_size,AreaTargetSrcHolder i_area_pool,ContourTargetSrcHolder i_contoure_pool) throws NyARException
	{
		this._ref_area_pool=i_area_pool;
		this._ref_contoure_pool=i_contoure_pool;
		super.initInstance(i_size,ContoureTargetSrcItem.class);
	}
	public void clear()
	{
		//所有するオブジェクトを開放してからクリア処理
		for(int i=this._length-1;i>=0;i--){
			if(this._items[i].area_src!=null){
				this._ref_area_pool.deleteObject(this._items[i].area_src);
				this._ref_contoure_pool.deleteObject(this._items[i].contour_src);
			}
		}
		super.clear();
	}

}