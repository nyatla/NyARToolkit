package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc.NyARIgnoreSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.*;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContourTargetList.*;

public class IgnoreTargetList extends NyARObjectStack<IgnoreTargetList.IgnoreTargetItem>
{
	public static class IgnoreTargetItem extends TrackTarget
	{
		public AreaTargetSrcHolder.AppearSrcItem _ref_area_src;
	}
	public IgnoreTargetItem pushTarget(NewTargetItem i_item)
	{
		IgnoreTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item._ref_area_src=i_item._ref_area;
		return item;
	}
	public IgnoreTargetItem pushTarget(ContourTargetItem i_item)
	{
		IgnoreTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item._ref_area_src=i_item._ref_area;
		return item;
	}

	
	public void updateTarget(int i_index,long i_tick,NyARIgnoreSrcItem i_item)
	{
		IgnoreTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		item._ref_area_src=i_item._ref_area_src;
		return;
	}
	protected IgnoreTargetItem createElement()
	{
		return new IgnoreTargetItem();
	}
	public IgnoreTargetList(int i_size) throws NyARException
	{
		super.initInstance(i_size,IgnoreTargetItem.class);
	}
	/**
	 * AppearTargetSrc.AppearSrcItemと合致する可能性のあるアイテムのインデクスを返す。
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AreaTargetSrcHolder.AppearSrcItem i_item)
	{
		AreaTargetSrcHolder.AppearSrcItem iitem;
		//許容距離誤差の2乗を計算(10%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/10)^2
		int dist_rate2=(i_item.area_sq_diagonal)/100;

		//距離は領域の10%以内の誤差、大きさは10%以内の誤差であること。
		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i]._ref_area_src;
			//大きさチェック
			double ratio;
			ratio=((double)iitem.area.w)/i_item.area.w;
			if(ratio<0.81 || 1.21<ratio){
				continue;
			}
			//距離チェック
			int d2=NyARMath.sqNorm(i_item.area_center,iitem.area_center);
			if(d2>dist_rate2)
			{
				continue;
			}
			//多分同じ対象物
			return i;
		}
		return -1;
	}		
}