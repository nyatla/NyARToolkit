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
 * データ型です。
 * 2次元の浮動小数点方の点を格納します。
 */
public class NyARDoublePoint2d
{
	public double x;
	public double y;
	/**
	 * 配列ファクトリ
	 * @param i_number
	 * @return
	 */
	public static NyARDoublePoint2d[] createArray(int i_number)
	{
		NyARDoublePoint2d[] ret=new NyARDoublePoint2d[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARDoublePoint2d();
		}
		return ret;
	}
	public static NyARDoublePoint2d[][] create2dArray(int i_length_x,int i_length_y)
	{
		NyARDoublePoint2d[][] ret=new NyARDoublePoint2d[i_length_y][i_length_x];
		for(int i=0;i<i_length_y;i++)
		{
			for(int i2=0;i2<i_length_x;i2++)
			{
				ret[i][i2]=new NyARDoublePoint2d();
			}
		}
		return ret;
	}
	/**
	 * p1->p2と、p2->p3の直線の外積を計算します。
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	public final static double crossProduct3Point(NyARDoublePoint2d p1,NyARDoublePoint2d p2,NyARDoublePoint2d p3)
	{
		return (p2.x-p1.x)*(p3.y-p2.y)-(p2.y-p1.y)*(p3.x-p2.x);
	}
	/**
	 * p1->p2と、p2->p3の直線の外積を計算します。
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	public final static double crossProduct3Point(NyARDoublePoint2d p1,NyARDoublePoint2d p2,double p3_x,double p3_y)
	{
		return (p2.x-p1.x)*(p3_y-p2.y)-(p2.y-p1.y)*(p3_x-p2.x);
	}

	
	
	/**
	 * 頂点配列の中央値を求めます。
	 * @param i_points
	 * @param i_number_of_data
	 * 配列中の有効な頂点数です。
	 * @param o_out
	 */
	public final static void makeCenter(NyARDoublePoint2d[] i_points,int i_number_of_data,NyARDoublePoint2d o_out)
	{
		double x,y;
		x=y=0;
		for(int i=i_number_of_data-1;i>=0;i--)
		{
			x+=i_points[i].x;
			y+=i_points[i].y;
		}
		o_out.x=x/i_number_of_data;
		o_out.x=y/i_number_of_data;
	}
	/**
	 * {@link #makeCenter}の出力型違いの関数です。
	 * @param i_points
	 * @param i_number_of_data
	 * @param o_out
	 */
	public final static void makeCenter(NyARDoublePoint2d[] i_points,int i_number_of_data,NyARIntPoint2d o_out)
	{
		double lx,ly;
		lx=ly=0;
		for(int i=i_number_of_data-1;i>=0;i--)
		{
			lx+=i_points[i].x;
			ly+=i_points[i].y;
		}
		o_out.x=(int)(lx/i_number_of_data);
		o_out.y=(int)(ly/i_number_of_data);
	}
	
	/**
	 * コンストラクタです。
	 */
	public NyARDoublePoint2d()
	{
		this.x=0;
		this.y=0;
		return;
	}
	/**
	 * i_srcの値をthisへセットします。
	 * @param i_src
	 */
	public NyARDoublePoint2d(double i_x,double i_y)
	{
		this.x=i_x;
		this.y=i_y;
		return;
	}
	/**
	 * i_srcの値をthisへセットします。
	 * @param i_src
	 */
	public NyARDoublePoint2d(NyARDoublePoint2d i_src)
	{
		this.x=i_src.x;
		this.y=i_src.y;
		return;
	}
	/**
	 * i_srcの値をthisへセットします。
	 * @param i_src
	 */
	public NyARDoublePoint2d(NyARIntPoint2d i_src)
	{
		this.x=(double)i_src.x;
		this.y=(double)i_src.y;
		return;
	}
	/**
	 * p2-p1間の距離の二乗値を計算します。
	 * @param i_p1
	 * @param i_p2
	 * @return
	 */	
	public final double sqDist(NyARDoublePoint2d i_p1)
	{
		double x,y;
		x=this.x-i_p1.x;
		y=this.y-i_p1.y;
		return x*x+y*y;
	}
	public final double sqDist(NyARIntPoint2d i_p1)
	{
		double x,y;
		x=this.x-i_p1.x;
		y=this.y-i_p1.y;
		return x*x+y*y;
	}	
	/**
	 * i_srcの値をthisへセットします。
	 * @param i_src
	 */
	public final void setValue(NyARDoublePoint2d i_src)
	{
		this.x=i_src.x;
		this.y=i_src.y;
		return;
	}
	/**
	 * i_srcの値をthisへセットします。
	 * @param i_src
	 */
	public final void setValue(NyARIntPoint2d i_src)
	{
		this.x=(double)i_src.x;
		this.y=(double)i_src.y;
		return;
	}
	public final void setValue(double x,double y)
	{
		this.x=x;
		this.y=y;
		return;
	}
	
}
