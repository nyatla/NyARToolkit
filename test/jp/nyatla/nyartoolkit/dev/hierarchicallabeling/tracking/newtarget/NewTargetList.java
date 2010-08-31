package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.EnterTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;

public class NewTargetList extends NyARObjectStack<NewTargetList.NewTargetItem>
{
	public static class NewTargetItem extends TrackTarget
	{
		public AreaTargetSrcHolder.AreaSrcItem ref_area;
	}
	private AreaTargetSrcHolder _area_pool;
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
		//所有権の移転
		item.ref_area=i_item.area_src;
		i_item.area_src=null;
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
		//オブジェクトの差し替え
		this._area_pool.deleteObject(item.ref_area);
		item.ref_area=i_item.ref_area_src;
		i_item.ref_area_src=null;
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
	public NewTargetList(int i_size,AreaTargetSrcHolder i_area_pool) throws NyARException
	{
		super.initInstance(i_size,NewTargetItem.class);
		this._area_pool=i_area_pool;
	}
	/**
	 * AppearTargetSrcと合致する可能性のあるアイテムのインデクスを返す。
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AreaTargetSrcHolder.AreaSrcItem i_item)
	{
		AreaTargetSrcHolder.AreaSrcItem iitem;
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