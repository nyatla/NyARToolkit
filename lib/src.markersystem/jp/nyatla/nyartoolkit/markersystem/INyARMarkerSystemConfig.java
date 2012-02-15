package jp.nyatla.nyartoolkit.markersystem;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.INyARHistogramAnalyzer_Threshold;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;

/**
 * このインタフェイスは、NyARMarkerSystemのコンフィギュレーションインタフェイスを定義します。
 *
 */
public interface INyARMarkerSystemConfig
{
	/**
	 * 姿勢変換アルゴリズムクラスのオブジェクトを生成して返します。
	 * @return
	 * @throws NyARException
	 */
	public INyARTransMat createTransmatAlgorism() throws NyARException;
	/**
	 * 敷居値決定クラスを生成して返します。
	 * @return
	 * @throws NyARException
	 */
	public INyARHistogramAnalyzer_Threshold createAutoThresholdArgorism() throws NyARException;
	/**
	 * ARToolKitパラメータオブジェクトを返します。
	 * @return
	 * [readonly]
	 */
	public NyARParam getNyARParam();
}