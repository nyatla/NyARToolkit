package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.*;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList.*;

public class IgnoreTargetList extends NyARObjectStack<IgnoreTargetList.IgnoreTargetItem>
{
	public static class IgnoreTargetItem extends TrackTarget
	{
		public AreaDataPool.AreaDataItem ref_area;
		public void terminate()
		{
			this.ref_area.deleteMe();
			this.ref_area=null;
			return;
		}		
	}

	/**
	 * IgnoreTargetSrcのアイテムで指定した要素を更新します。
	 * @param i_index
	 * @param i_tick
	 * @param i_item
	 */
	public void updateTarget(int i_index,long i_tick,IgnoreTargetSrc.NyARIgnoreSrcItem i_item)
	{
		IgnoreTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		//Srcオブジェクトをアイテムにアタッチ
		i_item.attachToTarget(item);
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
	public int getMatchTargetIndex(AreaDataPool.AreaDataItem i_item)
	{
		AreaDataPool.AreaDataItem iitem;
		//許容距離誤差の2乗を計算(10%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/10)^2
		int dist_rate2=(i_item.area_sq_diagonal)/100;

		//距離は領域の10%以内の誤差、大きさは10%以内の誤差であること。
		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i].ref_area;
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