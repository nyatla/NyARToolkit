package jp.nyatla.nyartoolkit.dev.tracking.old.outline;

import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.dev.tracking.old.NyARTrackItem;

public abstract class NyAROutlineTrackItem extends NyARTrackItem
{
	//現在の位置
	public NyARIntPoint2d   center=new NyARIntPoint2d();
	public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
	public int sq_dist_max;
	public int life;
	/**
	 * このアイテムに、付加情報を設定します。
	 * @param i_width
	 * @param i_direction
	 */
	public abstract void setUpgradeInfo(double i_width,int i_direction);
}

