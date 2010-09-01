package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourTargetSrcPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcPool.AreaTargetSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;

public class ContoureTargetSrc extends NyARObjectStack<ContoureTargetSrc.ContoureTargetSrcItem>
{
	public static class ContoureTargetSrcItem
	{
		public AreaTargetSrcPool.AreaTargetSrcItem area_src;
		public ContourTargetSrcPool.ContourTargetSrcItem contour_src;
		public void attachToTarget(ContoureTargetList.ContoureTargetItem o_output)
		{
			o_output.area.deleteMe();
			o_output.contoure.deleteMe();
			o_output.area=this.area_src;
			o_output.contoure=this.contour_src;
			this.area_src=null;
			this.contour_src=null;
		}
	}
	private AreaTargetSrcPool _ref_area_pool;
	private ContourTargetSrcPool _ref_contoure_pool;
	
	public ContoureTargetSrc.ContoureTargetSrcItem pushSrcTarget(
			AreaTargetSrcPool.AreaTargetSrcItem i_area_ietm,
			ContourTargetSrcPool.ContourTargetSrcItem i_item)
	{
		ContoureTargetSrc.ContoureTargetSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.area_src=i_area_ietm;
		item.contour_src=i_item;
		return item;
	}
	protected ContoureTargetSrcItem createElement()
	{
		return new ContoureTargetSrcItem();
	}
	public ContoureTargetSrc(int i_size,AreaTargetSrcPool i_area_pool,ContourTargetSrcPool i_contoure_pool) throws NyARException
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