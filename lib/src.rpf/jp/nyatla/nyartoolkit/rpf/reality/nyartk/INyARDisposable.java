package jp.nyatla.nyartoolkit.rpf.reality.nyartk;

/**
 * オブジェクトの破棄タイミングを受け取るインタフェイスです。
 *
 */
public interface INyARDisposable
{
	/**
	 * オブジェクトの終期化のタイミングを与えます。オブジェクトの終期化に必要な処理を実装します。
	 */
	public void dispose();
}
