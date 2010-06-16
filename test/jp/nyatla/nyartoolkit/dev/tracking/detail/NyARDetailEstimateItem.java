package jp.nyatla.nyartoolkit.dev.tracking.detail;

import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.types.*;

public class NyARDetailEstimateItem
{


	public NyARRectOffset offset=new NyARRectOffset();	
	/**
	 * 画面位置での中心座標
	 */
	public NyARIntPoint2d  center=new NyARIntPoint2d();
	/**
	 * 探索範囲
	 */
	public NyARIntRect     search_area=new NyARIntRect();
	/**
	 * 頂点の予測位置位置
	 */
	public NyARDoublePoint2d[] ideal_vertex=NyARDoublePoint2d.createArray(4);
	public int ideal_sq_dist_max;

}
