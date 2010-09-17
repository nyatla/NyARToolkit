package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.square;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.Square2dDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;

public class SquareTargetList extends NyARObjectStack<SquareTargetList.SquareTargetItem>
{
	public static class SquareTargetItem extends TrackTarget
	{
		public boolean enable;
		public AreaDataPool.AreaDataItem ref_area;
		public Square2dDataPool.Square2dSrcItem ref_square2d;
	}
	private AreaDataPool _area_pool;
	private Square2dDataPool _sq2d_pool;
	/**
	 * ContourTargetから、Square2dTargetへの昇格に使います。昇格直後は一部のパラメータが不定です。
	 * @param i_item
	 * @return
	 */
	public SquareTargetItem upgradeTarget(NewTargetItem i_item)
	{
		SquareTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;

		//areaの委譲
		item.ref_area   =i_item.ref_area;
		i_item.ref_area=null;
		//countoureは委譲元が存在しないので、nullを指定する。
		item.ref_square2d=null;
		item.enable=false;
		return item;
	}

	/**
	 * srcの内容でターゲットを更新します。
	 * @param i_index
	 * @param i_tick
	 * @param i_src
	 */
	public void updateTarget(int i_index,long i_tick,SquareTargetSrc.SquareTargetSrcItem i_src)
	{
		SquareTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		
		//areaの差し替え
		this._area_pool.deleteObject(item.ref_area);
		item.ref_area=i_src.area_src;
		i_src.area_src=null;

		//sq_2d差し替え
		if(item.ref_square2d!=null){
			this._sq2d_pool.deleteObject(item.ref_square2d);
		}
		item.ref_square2d=i_src.square2d_src;
		i_src.square2d_src=null;

		item.enable=true;
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
	
	
	
	protected SquareTargetItem createElement()
	{
		return new SquareTargetItem();
	}
	public SquareTargetList(int i_size,AreaDataPool i_area_pool,Square2dDataPool i_sq2d_pool) throws NyARException
	{
		super.initInstance(i_size,SquareTargetItem.class);
		this._area_pool=i_area_pool;
		this._sq2d_pool=i_sq2d_pool;
	}
	/**
	 * AppearTargetSrcと合致する可能性のあるアイテムのインデクスを返す。
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AreaDataPool.AreaDataItem i_item)
	{
		AreaDataPool.AreaDataItem iitem;
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