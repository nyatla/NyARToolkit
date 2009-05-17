package jp.nyatla.nyartoolkit.nyidmarker.data;

import jp.nyatla.nyartoolkit.nyidmarker.NyARIdMarkerPattern;


public class NyIdMarkerDataEncoder_RawBit implements INyIdMarkerDataEncoder
{	
	private final static int _DOMAIN_ID=0;
	private final static int _mod_data[]={7,31,127,511,2047,4095};
	public boolean encode(NyARIdMarkerPattern i_data,INyIdMarkerData o_dest)
	{
		final NyIdMarkerData_RawBit dest=(NyIdMarkerData_RawBit)o_dest;
		if(i_data.ctrl_domain!=_DOMAIN_ID){
			return false;
		}
		//パケット数計算
		final int resolution_len=(i_data.model+1);
		final int packet_length=(resolution_len*resolution_len)/8+1;
		int sum=0;
		for(int i=0;i<packet_length;i++){
			dest.packet[i]=i_data.data[i];
			sum+=i_data.data[i];
		}
		//チェックドット値計算
		sum=sum%_mod_data[i_data.model-2];
		//チェックドット比較
		if(i_data.check!=sum){
			return false;
		}
		dest.length=packet_length;
		return true;
	}
	public INyIdMarkerData createDataInstance()
	{
		return new NyIdMarkerData_RawBit();
	}
}
