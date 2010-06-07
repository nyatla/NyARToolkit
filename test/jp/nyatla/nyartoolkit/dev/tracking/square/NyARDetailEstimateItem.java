package jp.nyatla.nyartoolkit.dev.tracking.square;

import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.types.*;

public class NyARDetailEstimateItem
{


	//この矩形のoffset
	public NyARRectOffset offset=new NyARRectOffset();	
	public NyARIntPoint2d   center=new NyARIntPoint2d();
	public NyARDoublePoint2d[] vertex=NyARDoublePoint2d.createArray(4);
	public int sq_dist_max;

}
