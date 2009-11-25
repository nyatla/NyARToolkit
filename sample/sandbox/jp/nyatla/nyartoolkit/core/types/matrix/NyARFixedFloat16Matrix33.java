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
package jp.nyatla.nyartoolkit.core.types.matrix;

import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARFixedFloat16Matrix33;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARI64Matrix33;


public class NyARFixedFloat16Matrix33 extends NyARI64Matrix33
{
	public void copyFrom(NyARDoubleMatrix33 i_matrix)
	{
		this.m00=(long)i_matrix.m00*0x10000;
		this.m01=(long)i_matrix.m01*0x10000;
		this.m02=(long)i_matrix.m02*0x10000;
		this.m10=(long)i_matrix.m10*0x10000;
		this.m11=(long)i_matrix.m11*0x10000;
		this.m12=(long)i_matrix.m12*0x10000;
		this.m20=(long)i_matrix.m20*0x10000;
		this.m21=(long)i_matrix.m21*0x10000;
		this.m22=(long)i_matrix.m22*0x10000;
		return;
	}
	public static NyARFixedFloat16Matrix33[] createArray(int i_number)
	{
		NyARFixedFloat16Matrix33[] ret=new NyARFixedFloat16Matrix33[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARFixedFloat16Matrix33();
		}
		return ret;
	}
}
