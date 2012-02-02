package jp.nyatla.nyartoolkit.core.rasterdriver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;

/**
 * ヒストグラムを生成するインタフェイスです。
 * @author nyatla
 *
 */
public interface INyARHistogramFromRaster
{
	//GSRaster
	public void createHistogram(int i_l,int i_t,int i_w,int i_h,int i_skip, NyARHistogram o_histogram) throws NyARException;
	public void createHistogram(int i_skip,NyARHistogram o_histogram) throws NyARException;
}
