/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.nyidmarker.data;

import jp.nyatla.nyartoolkit.nyidmarker.*;


public class NyIdMarkerDataEncoder_RawBit implements INyIdMarkerDataEncoder
{	
	private final static int _DOMAIN_ID=0;
	private final static int[] _mod_data={7,31,127,511,2047,4095};
	public boolean encode(NyIdMarkerPattern i_data,INyIdMarkerData o_dest)
	{
		final NyIdMarkerData_RawBit dest=(NyIdMarkerData_RawBit)o_dest;
		if(i_data.ctrl_domain!=_DOMAIN_ID){
			return false;
		}
		//パケット数計算
		final int resolution_len=(i_data.model+i_data.model-1);      //データドットの数
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
