package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool.AreaDataItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;

public class ContoureTargetSrc extends NyARObjectStack<ContoureTargetSrc.ContoureTargetSrcItem>
{
	public static class ContoureTargetSrcItem
	{
		public AreaDataPool.AreaDataItem area_src;
		public ContourDataPool.ContourTargetSrcItem contour_src;
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
	
	public ContoureTargetSrc.ContoureTargetSrcItem pushSrcTarget(
			AreaDataPool.AreaDataItem i_area_ietm,
			ContourDataPool.ContourTargetSrcItem i_item)
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
	public ContoureTargetSrc(int i_size) throws NyARException
	{
		super.initInstance(i_size,ContoureTargetSrcItem.class);
	}
	public void clear()
	{
		//所有するオブジェクトを開放してからクリア処理
		for(int i=this._length-1;i>=0;i--){
			if(this._items[i].area_src!=null){
				this._items[i].area_src.deleteMe();
				this._items[i].contour_src.deleteMe();
			}
		}
		super.clear();
	}

}