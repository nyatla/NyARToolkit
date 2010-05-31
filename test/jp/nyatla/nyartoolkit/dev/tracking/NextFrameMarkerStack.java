package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;

public class NextFrameMarkerStack extends NyARObjectStack<NextFrameMarkerStack.Item>
{
	public class Item
	{
		public MarkerPositionTable.Item ref_item;
		public NyARDoublePoint2d   center=new NyARDoublePoint2d();
		public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
		/**
		 * 探索距離の２乗値
		 */
		public double sq_norm;
		public int min_dist;
		
		
	}
	protected NextFrameMarkerStack.Item createElement()
	{
		return new NextFrameMarkerStack.Item();
	}

	
	
	public NextFrameMarkerStack(int i_length) throws NyARException
	{
		super(i_length,NextFrameMarkerStack.Item.class);
		return;
	}
	/**
	 * スクリーン上で指定点の一番近くにあるアイテムを探します。
	 * @param i_table
	 * @param i_pos
	 * 探索点
	 * @param i_limit_max
	 * 探索範囲の最大値
	 * @return
	 */
	public NextFrameMarkerStack.Item getNearItem(NyARDoublePoint2d i_pos)
	{
		NextFrameMarkerStack.Item[] items=this._items;
		
		double d=Double.MAX_VALUE;
		//エリア
		int index=-1;
		for(int i=this._length-1;i>=0;i--)
		{
			NyARDoublePoint2d center=items[i].center;
			double nd=NyARMath.sqNorm(i_pos, center);
			//有効範囲内？
			if(nd>items[i].sq_norm){
				continue;
			}
			//現在の最小エラーレートよりも小さい？
			if(nd>items[i].min_dist)
			if(d>nd){
				d=nd;
				index=i;
			}
		}
		return index==-1?null:items[index];
	}

	
}