package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc.NyARIgnoreSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.*;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContourTargetList.*;

public class IgnoreTargetList extends NyARObjectStack<IgnoreTargetList.IgnoreTarget>
{
	public static class IgnoreTarget extends TrackTarget
	{
		public NyARIntRect area=new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();

	}
	public IgnoreTarget pushTarget(NewTargetItem i_item)
	{
		IgnoreTarget item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		return item;
	}
	public IgnoreTarget pushTarget(ContourTargetItem i_item)
	{
		IgnoreTarget item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		return item;
	}

	
	public void updateTarget(int i_index,long i_tick,NyARIgnoreSrcItem i_item)
	{
		IgnoreTarget item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		return;
	}
	protected IgnoreTarget createElement()
	{
		return new IgnoreTarget();
	}
	public IgnoreTargetList(int i_size) throws NyARException
	{
		super.initInstance(i_size,IgnoreTarget.class);
	}
	/**
	 * 一致する矩形を検索する。一致する矩形の判定は、Areaの重なり具合
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AppearTargetSrc.AppearSrcItem i_item)
	{
		IgnoreTarget iitem;
		//許容距離誤差の2乗を計算(10%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/10)^2
		int dist_rate2=(i_item.area_sq_diagonal)/100;

		//距離は領域の10%以内の誤差、大きさは10%以内の誤差であること。
		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i];
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