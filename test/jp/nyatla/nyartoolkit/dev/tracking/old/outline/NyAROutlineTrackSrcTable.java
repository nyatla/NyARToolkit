package jp.nyatla.nyartoolkit.dev.tracking.old.outline;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.*;
import jp.nyatla.nyartoolkit.core.param.*;

public class NyAROutlineTrackSrcTable extends NyARObjectStack<NyAROutlineTrackSrcTable.Item>
{
	public class Item
	{
		//マーカ中心
		public NyARIntPoint2d ideal_center=new NyARIntPoint2d();
		//4頂点(スクリーン座標系)
		public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
	}	
	
	protected NyAROutlineTrackSrcTable.Item createElement()
	{
		return new NyAROutlineTrackSrcTable.Item();
	}
	
	public NyAROutlineTrackSrcTable(int i_length) throws NyARException
	{
		super(i_length,NyAROutlineTrackSrcTable.Item.class);
		return;
	}
	public int getNearestItem(NyARIntPoint2d i_pos,int i_sq_dist)
	{
		double d=Double.MAX_VALUE;
		//エリア
		int index=-1;
		for(int i=this._length-1;i>=0;i--)
		{
			double nd=NyARMath.sqNorm(i_pos, this._items[i].ideal_center);
			//有効範囲内？
			if(nd>i_sq_dist){
				continue;
			}
			if(d>nd){
				d=nd;
				index=i;
			}
		}
		return index;
	}
	NyARObserv2IdealMap _dist;
	public NyAROutlineTrackSrcTable.Item push(NyARIntPoint2d i_v1,NyARIntPoint2d i_v2,NyARIntPoint2d i_v3,NyARIntPoint2d i_v4)
	{
		NyAROutlineTrackSrcTable.Item item=this.prePush();
		if(item==null){
			return null;
		}
		item.ideal_center.x=(i_v1.x+i_v2.x+i_v3.x+i_v4.x)/4;
		item.ideal_center.y=(i_v1.y+i_v2.y+i_v3.y+i_v4.y)/4;
		item.vertex[0].x=i_v1.x;
		item.vertex[0].y=i_v1.y;
		item.vertex[1].x=i_v2.x;
		item.vertex[1].y=i_v2.y;
		item.vertex[2].x=i_v3.x;
		item.vertex[2].y=i_v3.y;
		item.vertex[3].x=i_v4.x;
		item.vertex[3].y=i_v4.y;
		return item;	
	}

}