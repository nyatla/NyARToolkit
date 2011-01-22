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
package jp.nyatla.nyartoolkit.core.types;
/**
 * このクラスはデータ型です。
 * ３次元の浮動小数点座標を格納します。
 */
public class NyARDoublePoint3d
{
	/** X座標の値です。*/
	public double x;
	/** Y座標の値です。*/
	public double y;
	/** Z座標の値です。*/	
	public double z;
	/**
	 * この関数は、オブジェクトの一次配列を作ります。
	 * @param i_number
	 * 作成する配列の長さ
	 * @return
	 * 新しい配列。
	 */
	public static NyARDoublePoint3d[] createArray(int i_number)
	{
		NyARDoublePoint3d[] ret=new NyARDoublePoint3d[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARDoublePoint3d();
		}
		return ret;
	}
	/**
	 * この関数は、オブジェクトからインスタンスに値をセットします。
	 * @param i_in
	 * コピー元のオブジェクト。
	 */	
	public final void setValue(NyARDoublePoint3d i_in)
	{
		this.x=i_in.x;
		this.y=i_in.y;
		this.z=i_in.z;
		return;
	}
	/**
	 * この関数は、インスタンスの座標と、指定点との距離の２乗値を返します。
	 * @param i_p1
	 * 点の座標
	 * @return
	 * i_p1との距離の二乗値
	 */		
	public final double sqDist(NyARDoublePoint3d i_p1)
	{
		double x,y,z;
		x=this.x-i_p1.x;
		y=this.y-i_p1.y;
		z=this.z-i_p1.z;
		return x*x+y*y+z*z;
	}	
}
