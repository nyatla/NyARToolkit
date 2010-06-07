package jp.nyatla.nyartoolkit.dev.tracking.square;

import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;


public class NyARDetailTrackItem
{
	//現在の角度とか
	public NyARDoublePoint3d angle=new NyARDoublePoint3d();
	public NyARDoublePoint3d trans=new NyARDoublePoint3d();
	
	public int life;
	public int sirial;
	public Object tag;
	
	public final NyARDetailEstimateItem estimate=new NyARDetailEstimateItem();

}
