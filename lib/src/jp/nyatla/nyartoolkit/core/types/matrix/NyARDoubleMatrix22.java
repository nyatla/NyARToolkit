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
/**
 * このクラスは、2x2行列を格納します。
 */
public class NyARDoubleMatrix22 implements INyARDoubleMatrix
{
	/** 行列の要素値です。*/
	public double m00;
	/** 行列の要素値です。*/
	public double m01;
	/** 行列の要素値です。*/
	public double m10;
	/** 行列の要素値です。*/
	public double m11;
	/**
	 * この関数は、要素数4の配列を、行列にセットします。
	 */
	public void setValue(double[] i_value)
	{
		this.m00=i_value[0];
		this.m01=i_value[1];
		this.m10=i_value[3];
		this.m11=i_value[4];
		return;
	}
	/**
	 * この関数は、要素数4の配列に、行列の内容をコピーします。
	 */
	public void getValue(double[] o_value)
	{
		o_value[0]=this.m00;
		o_value[1]=this.m01;
		o_value[3]=this.m10;
		o_value[4]=this.m11;
		return;
	}
	/**
	 * この関数は、逆行列を計算して、インスタンスにセットします。
	 * @param i_src
	 * 逆行列を計算するオブジェクト。thisを指定できます。
	 * @return
	 * 逆行列を得られると、trueを返します。
	 */
	public boolean inverse(NyARDoubleMatrix22 i_src)
	{
		final double a11,a12,a21,a22;
		a11=i_src.m00;
		a12=i_src.m01;
		a21=i_src.m10;
		a22=i_src.m11;
		double det=a11*a22-a12*a21;
		if(det==0){
			return false;
		}
		det=1/det;
		this.m00=a22*det;
		this.m01=-a12*det;
		this.m10=-a21*det;
		this.m11=a11*det;
		return true;
	}	
}
