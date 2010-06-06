package jp.nyatla.nyartoolkit.dev.tracking.square;

import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public class NyARSquareTrackItem
{
	//現在の変換行列
	public NyARTransMatResult transmat=new NyARTransMatResult();
	//現在の角度とか
	public NyARDoublePoint3d angle=new NyARDoublePoint3d();
	public NyARDoublePoint3d trans=new NyARDoublePoint3d();
	//現在の角速度
	public NyARDoublePoint3d angle_v=new NyARDoublePoint3d();
	//現在の速度
	public NyARDoublePoint3d trans_v=new NyARDoublePoint3d();
	
	public int life;
	public int sirial;
	public Object tag;
	
	public final NyARSquareEstimateItem estimate=new NyARSquareEstimateItem();
	//現在のangle,trans

}
