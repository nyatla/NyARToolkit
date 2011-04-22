package jp.nyatla.nyartoolkit.nyidmarker.data;



/**
 * このクラスは、{@link NyIdMarkerDataEncoder_RawBitId}の出力するデータを
 * 格納します。
 */
public class NyIdMarkerData_RawBitId implements INyIdMarkerData
{
	/** RawbitドメインのNyIdから作成したID値です。*/
	public long marker_id;
	/**
	 * この関数は、i_targetのマーカデータとインスタンスのデータを比較します。
	 * 引数には、{@link NyIdMarkerData_RawBitId}型のオブジェクトを指定してください。
	 */
	public boolean isEqual(INyIdMarkerData i_target)
	{
		NyIdMarkerData_RawBitId s=(NyIdMarkerData_RawBitId)i_target;
		return s.marker_id==this.marker_id;
	}
	/**
	 * この関数は、i_sourceからインスタンスにマーカデータをコピーします。
	 * 引数には、{@link NyIdMarkerData_RawBit}型のオブジェクトを指定してください。
	 */	
	public void copyFrom(INyIdMarkerData i_source)
	{
		final NyIdMarkerData_RawBitId s=(NyIdMarkerData_RawBitId)i_source;
		this.marker_id=s.marker_id;
		return;
	}
}