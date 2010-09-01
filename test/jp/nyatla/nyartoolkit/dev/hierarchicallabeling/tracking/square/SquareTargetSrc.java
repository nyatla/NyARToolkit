package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.square;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.*;

public class SquareTargetSrc extends NyARObjectStack<SquareTargetSrc.SquareTargetSrcItem>
{
	public static class SquareTargetSrcItem
	{
		public AreaTargetSrcPool.AreaTargetSrcItem area_src;
		public ContourTargetSrcPool.ContourTargetSrcItem contour_src;
		public Square2dTargetSrcPool.Square2dSrcItem square2d_src;
		/**
		 * このオブジェクトの持つデータを、o_targetに割り付けます。
		 * @param o_target
		 */
		public void attachToTarget(SquareTargetList.SquareTargetItem o_target)
		{
			o_target.ref_area.deleteMe();
			o_target.ref_square2d.deleteMe();
			o_target.ref_area=this.area_src;
			o_target.ref_square2d=this.square2d_src;
			this.contour_src.deleteMe();
			this.area_src=null;
			this.contour_src=null;
			this.square2d_src=null;
			return;
		}
	}
	
	public SquareTargetSrc.SquareTargetSrcItem pushSrcTarget(
			AreaTargetSrcPool.AreaTargetSrcItem i_area_item,
			ContourTargetSrcPool.ContourTargetSrcItem i_contoure_item,
			Square2dTargetSrcPool.Square2dSrcItem i_sq_item)
	{
		SquareTargetSrc.SquareTargetSrcItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.area_src=i_area_item;
		item.contour_src=i_contoure_item;
		item.square2d_src=i_sq_item;
		return item;
	}
	protected SquareTargetSrcItem createElement()
	{
		return new SquareTargetSrcItem();
	}
	public SquareTargetSrc(int i_size) throws NyARException
	{
		super.initInstance(i_size,SquareTargetSrcItem.class);
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