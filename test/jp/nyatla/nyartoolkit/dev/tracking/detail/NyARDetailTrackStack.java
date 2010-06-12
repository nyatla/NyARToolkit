package jp.nyatla.nyartoolkit.dev.tracking.detail;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;

public class NyARDetailTrackStack extends NyARObjectStack<NyARDetailTrackItem>
{
	protected NyARDetailTrackItem createElement()
	{
		return new NyARDetailTrackItem();
	}
	public NyARDetailTrackStack(int i_max_tracking) throws NyARException
	{
		super(i_max_tracking,NyARDetailTrackItem.class);
		return;
	}
	public int getNearestItem(NyARIntPoint2d i_pos)
	{
		double d=Double.MAX_VALUE;
		//エリア
		int index=-1;
		for(int i=this._length-1;i>=0;i--)
		{
			NyARIntPoint2d center=this._items[i].estimate.center;
			double nd=NyARMath.sqNorm(i_pos, center);
			//有効範囲内？
			if(nd>this._items[i].estimate.ideal_sq_dist_max){
				continue;
			}
			if(d>nd){
				d=nd;
				index=i;
			}
		}
		return index;
	}			
}