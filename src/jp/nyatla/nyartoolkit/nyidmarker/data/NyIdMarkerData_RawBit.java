/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.nyidmarker.data;

/**
 * [[Strage class]]
 *
 */
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
