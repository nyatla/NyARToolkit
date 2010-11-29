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
package jp.nyatla.nyartoolkit.core.types.matrix;

public class NyARDoubleMatrix34 implements INyARDoubleMatrix
{
	public double m00;
	public double m01;
	public double m02;
	public double m03;
	public double m10;
	public double m11;
	public double m12;
	public double m13;
	public double m20;
	public double m21;
	public double m22;
	public double m23;
	public void setValue(double[] i_value)
	{
		this.m00=i_value[0];
		this.m01=i_value[1];
		this.m02=i_value[2];
		this.m03=i_value[3];
		this.m10=i_value[4];
		this.m11=i_value[5];
		this.m12=i_value[6];
		this.m13=i_value[7];
		this.m20=i_value[8];
		this.m21=i_value[9];
		this.m22=i_value[10];
		this.m23=i_value[11];
		return;
	}
	public void setValue(NyARDoubleMatrix34 i_value)
	{
		this.m00=i_value.m00;
		this.m01=i_value.m01;
		this.m02=i_value.m02;
		this.m03=i_value.m03;
		this.m10=i_value.m10;
		this.m11=i_value.m11;
		this.m12=i_value.m12;
		this.m13=i_value.m13;
		this.m20=i_value.m20;
		this.m21=i_value.m21;
		this.m22=i_value.m22;
		this.m23=i_value.m23;
		return;
	}
	public void getValue(double[] o_value)
	{
		o_value[0]=this.m00;
		o_value[1]=this.m01;
		o_value[2]=this.m02;
		o_value[3]=this.m03;
		o_value[4]=this.m10;
		o_value[5]=this.m11;
		o_value[6]=this.m12;
		o_value[7]=this.m13;
		o_value[8]=this.m20;
		o_value[9]=this.m21;
		o_value[10]=this.m22;
		o_value[11]=this.m23;
		return;
	}
}
