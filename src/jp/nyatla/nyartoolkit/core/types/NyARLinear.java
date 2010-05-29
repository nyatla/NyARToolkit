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
 * 0=dx*x+dy*y+cのパラメータを格納します。
 * x,yの増加方向は、x=L→R,y=B→Tです。 
 *
 */
public class NyARLinear
{
	public double dx;//dx軸の増加量
	public double dy;//dy軸の増加量
	public double c;//切片
	public static NyARLinear[] createArray(int i_number)
	{
		NyARLinear[] ret=new NyARLinear[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARLinear();
		}
		return ret;
	}	
	public final void copyFrom(NyARLinear i_source)
	{
		this.dx=i_source.dx;
		this.dy=i_source.dy;
		this.c=i_source.c;
		return;
	}
	/**
	 * 2直線の交点を計算します。
	 * @param l_line_i
	 * @param l_line_2
	 * @param o_point
	 * @return
	 */
	public final static boolean crossPos(NyARLinear l_line_i,NyARLinear l_line_2,NyARDoublePoint2d o_point)
	{
		final double w1 = l_line_2.dy * l_line_i.dx - l_line_i.dy * l_line_2.dx;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = (l_line_2.dx * l_line_i.c - l_line_i.dx * l_line_2.c) / w1;
		o_point.y = (l_line_i.dy * l_line_2.c - l_line_2.dy * l_line_i.c) / w1;
		return true;
	}	
}
