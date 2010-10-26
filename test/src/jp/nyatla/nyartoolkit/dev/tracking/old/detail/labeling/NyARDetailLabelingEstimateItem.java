package jp.nyatla.nyartoolkit.dev.tracking.old.detail.labeling;

import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.types.*;


public class NyARDetailLabelingEstimateItem
{
	/**
	 * 探索範囲
	 */
	public NyARIntRect     search_area=new NyARIntRect();
	/**
	 * 頂点の予測位置位置
	 */
	public NyARDoublePoint2d[] prev_ideal_vertex=NyARDoublePoint2d.createArray(4);
	public int sq_dist_max;	
	//予想すべき矩形の位置
	public NyARIntRect rect=new NyARIntRect();

	public NyARRectOffset offset=new NyARRectOffset();
	public double width;
	
	/**
	 * 画面位置での中心座標
	 */
	public NyARIntPoint2d   center=new NyARIntPoint2d();
	/**
	 * 理想位置
	 */
	public NyARDoublePoint2d[] ideal_vertex=NyARDoublePoint2d.createArray(4);
	public int ideal_sq_dist_max;

}
