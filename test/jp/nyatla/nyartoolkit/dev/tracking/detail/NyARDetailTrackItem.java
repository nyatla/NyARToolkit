package jp.nyatla.nyartoolkit.dev.tracking.detail;

import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.dev.tracking.*;


public class NyARDetailTrackItem extends NyARTrackItem
{
	//現在の角度とか
	public NyARDoublePoint3d angle=new NyARDoublePoint3d();
	public NyARDoublePoint3d trans=new NyARDoublePoint3d();
	public NyARDoublePoint3d trans_v=new NyARDoublePoint3d();
	
	public int life;
	public final NyARDetailEstimateItem estimate=new NyARDetailEstimateItem();

}
