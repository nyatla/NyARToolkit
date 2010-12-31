package jp.nyatla.nyartoolkit.core.analyzer.histogram;

import jp.nyatla.nyartoolkit.core.types.NyARHistogram;

/**
 * 敷居値判別ヒストグラム分析器の、標準的なインタフェイスを定義します。
 *
 */
public interface INyARHistogramAnalyzer_Threshold
{
	/**
	 * ヒストグラムから閾値探索をします。
	 * @param i_histogram
	 * 分析するヒストグラムオブジェクト
	 * @return
	 * 敷居値を返します。
	 */
	public int getThreshold(NyARHistogram i_histogram);
}
