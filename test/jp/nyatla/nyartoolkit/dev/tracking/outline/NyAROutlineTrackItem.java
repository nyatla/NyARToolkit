package jp.nyatla.nyartoolkit.dev.tracking.outline;

import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

public class NyAROutlineTrackItem
{
	//現在の位置
	public NyARIntPoint2d   center=new NyARIntPoint2d();
	public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
	public int sq_dist_max;
	public int serial;
	public int life;
	public Object tag;
}