/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.core.transmat;


import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

/**
 * NyARTransMat戻り値専用のNyARMat
 * 
 */
public class NyARTransMatResult extends NyARDoubleMatrix34
{
	/**
	 * エラーレート。この値はINyARTransMatの派生クラスが使います。
	 */
	public double error;	
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
	public final void transformVertex(double i_x,double i_y,double i_z,NyARDoublePoint3d o_out)
	{
		o_out.x=this.m00*i_x+this.m01*i_y+this.m02*i_z+this.m03;
		o_out.y=this.m10*i_x+this.m11*i_y+this.m12*i_z+this.m13;
		o_out.z=this.m20*i_x+this.m21*i_y+this.m22*i_z+this.m23;
		return;
	}
	public final void transformVertex(NyARDoublePoint3d i_in,NyARDoublePoint3d o_out)
	{
		transformVertex(i_in.x,i_in.y,i_in.z,o_out);
	}
}
