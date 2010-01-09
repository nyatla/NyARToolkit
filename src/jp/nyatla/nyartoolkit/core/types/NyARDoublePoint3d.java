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

public class NyARDoublePoint3d
{
	public double x;
	public double y;
	public double z;
	/**
	 * 配列ファクトリ
	 * @param i_number
	 * @return
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
	public void setValue(final NyARDoublePoint3d i_in)
	{
		this.x=i_in.x;
		this.y=i_in.y;
		this.z=i_in.z;
		return;
	}
	/**
	 * i_pointとのベクトルから距離を計算します。
	 * @return
	 */
	public double dist(NyARDoublePoint3d i_point)
	{
		double x,y,z;
		x=this.x-i_point.x;
		y=this.y-i_point.y;
		z=this.z-i_point.z;
		return Math.sqrt(x*x+y*y+z*z);
	}
}
