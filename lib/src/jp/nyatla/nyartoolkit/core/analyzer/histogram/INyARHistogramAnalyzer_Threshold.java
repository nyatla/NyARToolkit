package jp.nyatla.nyartoolkit.core.analyzer.histogram;

import jp.nyatla.nyartoolkit.core.types.NyARHistogram;

public interface INyARHistogramAnalyzer_Threshold
{
	/**
	 * ヒストグラムから閾値探索をします。
	 * @param i_histogram
	 * ヒストグラム
	 * @return
	 */
	public int getThreshold(NyARHistogram i_histogram);
}
