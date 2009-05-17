package jp.nyatla.nyartoolkit.nyidmarker.data;

import jp.nyatla.nyartoolkit.nyidmarker.NyARIdMarkerPattern;


public interface INyIdMarkerDataEncoder
{
	public boolean encode(NyARIdMarkerPattern i_data,INyIdMarkerData o_dest);
	public INyIdMarkerData createDataInstance();
}
