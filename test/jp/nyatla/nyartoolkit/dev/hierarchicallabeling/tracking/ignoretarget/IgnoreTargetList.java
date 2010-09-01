package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourTargetSrcPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.*;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList.*;

public class IgnoreTargetList extends NyARObjectStack<IgnoreTargetList.IgnoreTargetItem>
{
	public static class IgnoreTargetItem extends TrackTarget
	{
		public AreaTargetSrcPool.AreaTargetSrcItem ref_area;
	}
	private AreaTargetSrcPool _area_pool;
	
	/**
	 * NewTargetItemのアイテムをアップグレードします。アップグレードすると、参照オブジェクトをこのターゲットに取り付け、NewTargetItemから
	 * 取り外します。
	 * @param i_item
	 * @return
	 */
	public IgnoreTargetItem upgradeTarget(NewTargetItem i_item)
	{
		IgnoreTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		//TrackTarget部分
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		
		//i_itemの所有値を移転
		i_item.giveData(item);
		return item;
	}
	public IgnoreTargetItem upgradeTarget(ContoureTargetList.ContoureTargetItem i_item)
	{
		IgnoreTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;

		//Areaのみ委譲
		item.ref_area=i_item.area;
		i_item.area=null;
		return item;
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
	/**
	 * ターゲットを削除します。同時に、ターゲットの参照している外部リソースも開放します。
	 * @param i_index
	 */
	public void deleteTarget(int i_index)
	{
		if(this._items[i_index].ref_area!=null){
			this._area_pool.deleteObject(this._items[i_index].ref_area);
		}
		super.removeIgnoreOrder(i_index);
	}	
	
	
	protected IgnoreTargetItem createElement()
	{
		return new IgnoreTargetItem();
	}
	public IgnoreTargetList(int i_size,AreaTargetSrcPool i_area_pool) throws NyARException
	{
		super.initInstance(i_size,IgnoreTargetItem.class);
		this._area_pool=i_area_pool;
	}
	/**
	 * AppearTargetSrc.AppearSrcItemと合致する可能性のあるアイテムのインデクスを返す。
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AreaTargetSrcPool.AreaTargetSrcItem i_item)
	{
		AreaTargetSrcPool.AreaTargetSrcItem iitem;
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