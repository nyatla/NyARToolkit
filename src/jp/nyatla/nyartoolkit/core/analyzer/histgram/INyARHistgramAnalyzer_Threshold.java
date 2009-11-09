package jp.nyatla.nyartoolkit.core.analyzer.histgram;

import jp.nyatla.nyartoolkit.core.types.NyARHistgram;

public interface INyARHistgramAnalyzer_Threshold
{
	/**
	 * ヒストグラムから閾値探索をします。
	 * @param i_histgram
	 * ヒストグラム
	 * @return
	 */
	public int getThreshold(NyARHistgram i_histgram);
}
