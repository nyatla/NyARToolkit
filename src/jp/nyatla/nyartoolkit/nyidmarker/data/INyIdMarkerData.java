package jp.nyatla.nyartoolkit.nyidmarker.data;


public interface INyIdMarkerData
{
	/**
	 * i_targetのマーカデータと自身のデータが等しいかを返します。
	 * @param i_target
	 * 比較するマーカオブジェクト
	 * @return
	 * 等しいかの真偽値
	 */
	public boolean isEqual(INyIdMarkerData i_target);
	/**
	 * i_sourceからマーカデータをコピーします。
	 * @param i_source
	 */
	public void copyFrom(INyIdMarkerData i_source);
}
