/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.math;


/**
 * このクラスには、数学関数、数学定数を定義します。
 */
public class NyARMath
{
	public final static double SQRT2=1.41421356237309504880;
	final static public double PI=3.1415926535897932384626433832795;
	
	public final static double DBL_EPSILON=2.2204460492503131e-016;
	/** 定数値。40^2*/
	public final static int SQ_40=40*40;
	/** 定数値。20^2*/
	public final static int SQ_20=20*20;
	/** 定数値。10^2*/
	public final static int SQ_10=10*10;
	/** 定数値。8^2*/
	public final static int SQ_8=8*8;
	/** 定数値。5^2*/
	public final static int SQ_5=5*5;
	/** 定数値。2^2*/
	public final static int SQ_2=2*2;

	/** 定数値。cos(30)の近似値*/	
	public final static double COS_DEG_30=0.8660;
	/** 定数値。cos(25)の近似値*/	
	public final static double COS_DEG_25=0.9063;
	/** 定数値。cos(20)の近似値*/
	public final static double COS_DEG_20=0.9396;
	/** 定数値。cos(15)の近似値*/
	public final static double COS_DEG_15=0.9395;
	/** 定数値。cos(10)の近似値*/
	public final static double COS_DEG_10=0.9848;
	/** 定数値。cos(8)の近似値*/
	public final static double COS_DEG_8 =0.9902;
	/** 定数値。cos(5)の近似値*/
	public final static double COS_DEG_5 =0.9961;
	/**
	 * この関数は、点p1と点p2の距離の二乗値を返します。
	 * @param i_p1x
	 * 点1のX座標
	 * @param i_p1y
	 * 点1のY座標
	 * @param i_p2x
	 * 点2のX座標
	 * @param i_p2y
	 * 点2のY座標
	 * @return
	 * 二乗距離値
	 */
	public static double sqNorm(double i_p1x,double i_p1y,double i_p2x,double i_p2y)
	{
		double x,y;
		x=i_p2x-i_p1x;
		y=i_p2y-i_p1y;
		return x*x+y*y;
	}
	/**
	 * この関数は、点1と点2の距離値を返します。
	 * @param i_x1
	 * 点1のX座標
	 * @param i_y1
	 * 点1のY座標
	 * @param i_x2
	 * 点2のX座標
	 * @param i_y2
	 * 点2のY座標
	 * @return
	 * 距離値
	 */
	public static final double dist(double i_x1,double i_y1,double i_x2,double i_y2)
	{
		return Math.sqrt(i_x1*i_y1+i_x2+i_y2);
	}
	/**
	 * この関数は、3乗根を求めます。
	 * 出典 http://aoki2.si.gunma-u.ac.jp/JavaScript/src/3jisiki.html
	 * @param i_in
	 * 3乗根を求める数値
	 * @return
	 * 三乗根の値
	 */
	public static double cubeRoot(double i_in)
	{
		double res = Math.pow(Math.abs(i_in), 1.0 / 3.0);
		return (i_in >= 0) ? res : -res;
	}
	/**
	 * この関数は、ユークリッドの互除法により、最大公約数を求めます。
	 * 出典 http://ja.wikipedia.org/wiki/%E3%83%A6%E3%83%BC%E3%82%AF%E3%83%AA%E3%83%83%E3%83%89%E3%81%AE%E4%BA%92%E9%99%A4%E6%B3%95
	 * @param i_x
	 * 最大公約数を求める数1
	 * @param i_y
	 * 最大公約数を求める数2
	 * @return
	 * 最大公約数
	 */
	public static int gcd(int i_x,int i_y)
	{
		int x=i_x;
		int y=i_y;
	    int r;
	    while (y != 0) {
	        r = x % y;
	        x = y;
	        y = r;
	    }
	    return x;
	}
//	/**
//	 * 格納値をベクトルとして、距離を返します。
//	 * @return
//	 */
//	public static final double dist(NyARDoublePoint2d i_vec)
//	{
//		return Math.sqrt(i_vec.x*i_vec.x+i_vec.y+i_vec.y);
//	}
//	/**
//	 * 格納値をベクトルとして、距離を返します。
//	 * @return
//	 */
//	public static final double dist(NyARDoublePoint3d i_vec)
//	{
//		return Math.sqrt(i_vec.x*i_vec.x+i_vec.y*i_vec.y+i_vec.z*i_vec.z);
//	}
}
