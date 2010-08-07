package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;

public class NewTargetList extends NyARObjectStack<NewTargetList.NewTargetItem>
{
	public static class NewTargetItem extends TrackTarget
	{
		private static Object _serial_lock=new Object();
		private static long _serial=0;
		
		public NyARIntRect area=new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		public int area_sq_diagonal;

	}
	/**
	 * i_itemの内容で初期化する。
	 * @param i_item
	 */
	public NewTargetItem pushTarget(long i_tick,AppearTargetSrc.AppearSrcItem i_item)
	{
		NewTargetItem item=this.prePush();
		//シリアル番号の割り当て
		synchronized(NewTargetItem._serial_lock)
		{
			item.serial=NewTargetItem._serial++;
		}
		item.tag=null;
		item.age=0;//寿命リセット
		item.last_update=i_tick;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		//対角距離
		return item;
	}
	/**
	 * i_itemの内容で更新する。
	 * @param i_item
	 */
	public void updateTarget(int i_index,long i_tick,NewTargetSrc.NewSrcItem i_item)
	{
		NewTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
	}	
	
	protected NewTargetItem createElement()
	{
		return new NewTargetItem();
	}
	public NewTargetList(int i_size) throws NyARException
	{
		super.initInstance(i_size,NewTargetItem.class);
	}
	/**
	 * 一致する矩形を検索する。一致する矩形の判定は、Areaの重なり具合
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AppearTargetSrc.AppearSrcItem i_item)
	{
		NewTargetItem iitem;
		//許容距離誤差の2乗を計算(許容誤差10%)
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