package jp.nyatla.nyartoolkit.nyidmarker.data;

public class NyIdMarkerData_RawBit implements INyIdMarkerData
{
	public int[] packet=new int[22];
	public int length;
	public boolean isEqual(INyIdMarkerData i_target)
	{
		NyIdMarkerData_RawBit s=(NyIdMarkerData_RawBit)i_target;
		if(s.length!=this.length){
			return false;
		}
		for(int i=s.length-1;i>=0;i--){
			if(s.packet[i]!=s.packet[i]){
				return false;
			}
		}
		return true;
	}
	public void copyFrom(INyIdMarkerData i_source)
	{
		final NyIdMarkerData_RawBit s=(NyIdMarkerData_RawBit)i_source;
		System.arraycopy(s.packet,0,this.packet,0,s.length);
		this.length=s.length;
		return;
	}
}
