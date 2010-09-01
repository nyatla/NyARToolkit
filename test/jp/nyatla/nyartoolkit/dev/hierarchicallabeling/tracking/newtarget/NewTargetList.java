package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.EnterTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList;

public class NewTargetList extends NyARObjectStack<NewTargetList.NewTargetItem>
{
	public static class NewTargetItem extends TrackTarget
	{
		public AreaTargetSrcPool.AreaTargetSrcItem ref_area;
		public void giveData(IgnoreTargetList.IgnoreTargetItem o_output)
		{
			assert(o_output!=null);
			o_output.ref_area=this.ref_area;
			this.ref_area=null;
		}
	}
	private AreaTargetSrcPool _area_pool;
	/**
	 * i_itemの内容で初期化する。
	 * @param i_item
	 */
	public NewTargetItem pushTarget(long i_tick,EnterTargetSrc.EnterSrcItem i_item)
	{
		NewTargetItem item=this.prePush();
		//シリアル番号の割り当て
		item.serial=TrackTarget.getSerial();
		item.tag=null;
		item.age=0;
		item.last_update=i_tick;
		//情報の譲渡
		i_item.attachToTarget(item);
		return item;
	}
	/**
	 * i_itemの内容で更新します。
	 * @param i_item
	 */
	public void updateTarget(int i_index,long i_tick,NewTargetSrc.NewSrcItem i_item)
	{
		NewTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		//情報の譲渡
		i_item.attachToTarget(item);
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
	
	
	
	protected NewTargetItem createElement()
	{
		return new NewTargetItem();
	}
	public NewTargetList(int i_size,AreaTargetSrcPool i_area_pool) throws NyARException
	{
		super.initInstance(i_size,NewTargetItem.class);
		this._area_pool=i_area_pool;
	}
	/**
	 * AppearTargetSrcと合致する可能性のあるアイテムのインデクスを返す。
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AreaTargetSrcPool.AreaTargetSrcItem i_item)
	{
		AreaTargetSrcPool.AreaTargetSrcItem iitem;
		//許容距離誤差の2乗を計算(許容誤差10%)
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