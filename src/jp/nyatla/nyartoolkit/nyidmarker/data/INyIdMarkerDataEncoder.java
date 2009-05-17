package jp.nyatla.nyartoolkit.nyidmarker.data;

import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPattern;


public interface INyIdMarkerDataEncoder
{
	public boolean encode(NyIdMarkerPattern i_data,INyIdMarkerData o_dest);
	public INyIdMarkerData createDataInstance();
}
