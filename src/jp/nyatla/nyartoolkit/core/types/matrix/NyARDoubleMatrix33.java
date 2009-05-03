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
package jp.nyatla.nyartoolkit.core.types.matrix;

import jp.nyatla.nyartoolkit.*;

public class NyARDoubleMatrix33 implements INyARDoubleMatrix
{
	public double m00;
	public double m01;
	public double m02;
	public double m10;
	public double m11;
	public double m12;
	public double m20;
	public double m21;
	public double m22;
	public static NyARDoubleMatrix33[] createArray(int i_number)
	{
		NyARDoubleMatrix33[] ret=new NyARDoubleMatrix33[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARDoubleMatrix33();
		}
		return ret;
	}
	/**
	 * 遅いからあんまり使わないでね。
	 */
	public void setValue(double[] i_value)
	{
		this.m00=i_value[0];
		this.m01=i_value[1];
		this.m02=i_value[2];
		this.m10=i_value[3];
		this.m11=i_value[4];
		this.m12=i_value[5];
		this.m20=i_value[6];
		this.m21=i_value[7];
		this.m22=i_value[8];
		return;
	}
	/**
	 * 遅いからあんまり使わないでね。
	 */
	public void getValue(double[] o_value)
	{
		o_value[0]=this.m00;
		o_value[1]=this.m01;
		o_value[2]=this.m02;
		o_value[3]=this.m10;
		o_value[4]=this.m11;
		o_value[5]=this.m12;
		o_value[6]=this.m20;
		o_value[7]=this.m21;
		o_value[8]=this.m22;
		return;
	}
}
