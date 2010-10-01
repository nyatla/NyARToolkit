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
 * 0=a*x+b*y+cのパラメータを格納します。
 * x,yの増加方向は、x=L→R,y=B→Tです。 y軸が反転しているので注意してください。
 *
 */
public class NyARLinear
{
	public double b;//係数b
	public double a;//係数a
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
		this.b=i_source.b;
		this.a=i_source.a;
		this.c=i_source.c;
		return;
	}
	/**
	 * 2直線の交点を計算します。
	 * @param l_line_2
	 * @param l_line_1
	 * @param o_point
	 * @return
	 */
	public final static boolean crossPos(NyARLinear l_line_1,NyARLinear l_line_2,NyARDoublePoint2d o_point)
	{
		final double w1 = l_line_1.a * l_line_2.b - l_line_2.a * l_line_1.b;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = (l_line_1.b * l_line_2.c - l_line_2.b * l_line_1.c) / w1;
		o_point.y = (l_line_2.a * l_line_1.c - l_line_1.a * l_line_2.c) / w1;
		return true;
	}
	/**
	 * ２点を結ぶ直線の式を得る。この式は正規化されている。
	 * @param i_point1
	 * @param i_point2
	 * @return
	 */
	public final boolean calculateLine(NyARDoublePoint2d i_point1,NyARDoublePoint2d i_point2)
	{
		double x1=i_point1.x;
		double y1=i_point1.y;
		double x2=i_point2.x;
		double y2=i_point2.y;
		double dx=y2-y1;
		double dy=x1-x2;
		double sq=Math.sqrt(dx*dx+dy*dy);
		if(sq==0){
			return false;
		}
		sq=1/sq;
		this.a=dx*sq;
		this.b=dy*sq;
		this.c=(x1*(y1-y2)+y1*(x2-x1))*sq;
		return true;
	}
	/**
	 * 傾きと通過点を入力して、その直線式をセットする。
	 * @param i_dx
	 * @param i_dy
	 * @param i_x
	 * @param i_y
	 */
	public final void setVector(double i_dx,double i_dy,double i_x,double i_y)
	{
		this.a= i_dy;
		this.b=-i_dx;
		this.c=(i_dx*i_y-i_dy*i_x)/i_dx;
		return;
	}
	/**
	 * 直行する直線を求めます。
	 */
	public final void orthogonalLine()
	{
		double dx=this.a;
		this.a=this.b;
		this.b=dx;
	}

}
