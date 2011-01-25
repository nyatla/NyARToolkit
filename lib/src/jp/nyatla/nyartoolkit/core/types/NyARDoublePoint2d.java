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
 * ２次元の浮動小数点座標を格納します。
 */
public class NyARDoublePoint2d
{
	/** X座標の値です。*/
	public double x;
	/** Y座標の値です。*/
	public double y;
	/**
	 * この関数は、オブジェクトの一次元配列を作ります。
	 * @param i_number
	 * 作成する配列の長さ
	 * @return
	 * 新しい配列。
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
	/**
	 * この関数は、オブジェクトの二次元配列を作ります。
	 * @param i_length_x
	 * 作成する配列の列数
	 * @param i_length_y
	 * 作成する配列の行数
	 * @return
	 * 新しい配列。
	 */	
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
	 * この関数は、３点で定義される直線から、外積を計算します。
	 * 外積は、p1->p2と、p2->p3で定義する直線の外積です。
	 * @param p1
	 * 点１
	 * @param p2
	 * 点２
	 * @param p3
	 * 点３
	 * @return
	 * 外積の値
	 */
	public final static double crossProduct3Point(NyARDoublePoint2d p1,NyARDoublePoint2d p2,NyARDoublePoint2d p3)
	{
		return (p2.x-p1.x)*(p3.y-p2.y)-(p2.y-p1.y)*(p3.x-p2.x);
	}
	/**
	 * この関数は、３点で定義される直線から、外積を計算します。
	 * 外積は、p1->p2と、p2->p3で定義する直線の外積です。
	 * @param p1
	 * 点1
	 * @param p2
	 * 点2
	 * @param p3_x
	 * 点3の座標(X)
	 * @param p3_y
	 * 点3の座標(Y)
	 * @return
	 * 外積の値
	 */
	public final static double crossProduct3Point(NyARDoublePoint2d p1,NyARDoublePoint2d p2,double p3_x,double p3_y)
	{
		return (p2.x-p1.x)*(p3_y-p2.y)-(p2.y-p1.y)*(p3_x-p2.x);
	}
	/**
	 * この関数は、頂点集合から、中央値(Σp[n]/n)を求めます。
	 * @param i_points
	 * 頂点集合を格納した配列です。
	 * @param i_number_of_data
	 * 配列中の有効な頂点数です。
	 * @param o_out
	 * 中央値を受け取るオブジェクトです。
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
	 * この関数は、頂点集合から、中央値(Σp[n]/n)を求めます。
	 * @param i_points
	 * 頂点集合を格納した配列です。
	 * @param i_number_of_data
	 * 配列中の有効な頂点数です。
	 * @param o_out
	 * 中央値を受け取るオブジェクトです。
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
	 * 初期値を格納したインスタンスを生成します。
	 */
	public NyARDoublePoint2d()
	{
		this.x=0;
		this.y=0;
		return;
	}
	/**
	 * コンストラクタです。
	 * 初期値を指定してインスタンスを生成します。
	 * @param i_x
	 * {@link #x}の初期値
	 * @param i_y
	 * {@link #y}の初期値
	 */
	public NyARDoublePoint2d(double i_x,double i_y)
	{
		this.x=i_x;
		this.y=i_y;
		return;
	}
	/**
	 * コンストラクタです。
	 * i_srcの値で初期化したインスタンスを生成します。
	 * @param i_src
	 * 初期値とするオブジェクト
	 */
	public NyARDoublePoint2d(NyARDoublePoint2d i_src)
	{
		this.x=i_src.x;
		this.y=i_src.y;
		return;
	}
	/**
	 * コンストラクタです。
	 * i_srcの値で初期化したインスタンスを生成します。
	 * @param i_src
	 * 初期値とするオブジェクト
	 */
	public NyARDoublePoint2d(NyARIntPoint2d i_src)
	{
		this.x=(double)i_src.x;
		this.y=(double)i_src.y;
		return;
	}
	/**
	 * この関数は、インスタンスの座標と、指定点との距離の２乗値を返します。
	 * @param i_p1
	 * 点の座標
	 * @return
	 * i_p1との距離の二乗値
	 */	
	public final double sqDist(NyARDoublePoint2d i_p1)
	{
		double x,y;
		x=this.x-i_p1.x;
		y=this.y-i_p1.y;
		return x*x+y*y;
	}
	/**
	 * この関数は、インスタンスの座標と、指定点との距離の２乗値を返します。
	 * @param i_p1
	 * 点の座標
	 * @return
	 * i_p1との距離の二乗値
	 */	
	public final double sqDist(NyARIntPoint2d i_p1)
	{
		double x,y;
		x=this.x-i_p1.x;
		y=this.y-i_p1.y;
		return x*x+y*y;
	}
	/**
	 * この関数は、オブジェクトからインスタンスに値をセットします。
	 * @param i_src
	 * コピー元のオブジェクト。
	 */
	public final void setValue(NyARDoublePoint2d i_src)
	{
		this.x=i_src.x;
		this.y=i_src.y;
		return;
	}
	/**
	 * この関数は、オブジェクトからインスタンスに値をセットします。
	 * @param i_src
	 * コピー元のオブジェクト。
	 */
	public final void setValue(NyARIntPoint2d i_src)
	{
		this.x=(double)i_src.x;
		this.y=(double)i_src.y;
		return;
	}
	/**
	 * この関数は、インスタンスに値をセットします。
	 * @param i_x
	 * {@link #x}にセットする値
	 * @param i_y
	 * {@link #y}にセットする値
	 */
	public final void setValue(double i_x,double i_y)
	{
		this.x=i_x;
		this.y=i_y;
		return;
	}
	
}
