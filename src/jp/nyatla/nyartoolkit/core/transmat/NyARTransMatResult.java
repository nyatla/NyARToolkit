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
package jp.nyatla.nyartoolkit.core.transmat;


import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

/**
 * NyARTransMat戻り値専用のNyARMat
 * 
 */
public class NyARTransMatResult extends NyARDoubleMatrix34
{
	public boolean has_value = false;
	/**
	 * この関数は、0-PIの間で値を返します。
	 * @param o_out
	 */
	public final void getZXYAngle(NyARDoublePoint3d o_out)
	{
		double sina = this.m21;
		if (sina >= 1.0) {
			o_out.x = Math.PI / 2;
			o_out.y = 0;
			o_out.z = Math.atan2(-this.m10, this.m00);
		} else if (sina <= -1.0) {
			o_out.x = -Math.PI / 2;
			o_out.y = 0;
			o_out.z = Math.atan2(-this.m10, this.m00);
		} else {
			o_out.x = Math.asin(sina);
			o_out.z = Math.atan2(-this.m01, this.m11);
			o_out.y = Math.atan2(-this.m20, this.m22);
		}
	}	
}
