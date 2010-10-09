package jp.nyatla.nyartoolkit.core.utils;

import jp.nyatla.nyartoolkit.core.types.*;
/**
 * typeの
 * 
 * 
 * @author nyatla
 *
 */
public class NyARMath
{
	public static final double sqNorm(double i_p1x,double i_p1y,double i_p2x,double i_p2y)
	{
		double x,y;
		x=i_p2x-i_p1x;
		y=i_p2y-i_p1y;
		return x*x+y*y;
	}
	/**
	 * p2-p1ベクトルのsquare normを計算する。
	 * @param i_p1
	 * @param i_p2
	 * @return
	 */	
	public static final double sqNorm(NyARDoublePoint2d i_p1,NyARDoublePoint2d i_p2)
	{
		double x,y;
		x=i_p2.x-i_p1.x;
		y=i_p2.y-i_p1.y;
		return x*x+y*y;
	}
	public static final int sqNorm(NyARIntPoint2d i_p1,NyARIntPoint2d i_p2)
	{
		int x,y;
		x=i_p2.x-i_p1.x;
		y=i_p2.y-i_p1.y;
		return x*x+y*y;
	}
	/**
	 * p2-p1ベクトルのsquare normを計算する。
	 * @param i_p1
	 * @param i_p2
	 * @return
	 */	
	public static final double sqNorm(NyARDoublePoint3d i_p1,NyARDoublePoint3d i_p2)
	{
		double x,y,z;
		x=i_p2.x-i_p1.x;
		y=i_p2.y-i_p1.y;
		z=i_p2.z-i_p1.z;
		return x*x+y*y+z*z;
	}
	
	
	public static final double dist(double i_x1,double i_y1,double i_x2,double i_y2)
	{
		return Math.sqrt(i_x1*i_y1+i_x2+i_y2);
	}
	public static final double dist(NyARDoublePoint2d i_vec)
	{
		return Math.sqrt(i_vec.x*i_vec.x+i_vec.y+i_vec.y);
	}
	/**
	 * 格納値をベクトルとして、距離を返します。
	 * @return
	 */
	public static final double dist(NyARDoublePoint3d i_vec)
	{
		return Math.sqrt(i_vec.x*i_vec.x+i_vec.y*i_vec.y+i_vec.z*i_vec.z);
	}	
	
	
	
	/**
	 * 3乗根を求められないシステムで、３乗根を求めます。
	 * http://aoki2.si.gunma-u.ac.jp/JavaScript/src/3jisiki.html
	 * @param i_in
	 * @return
	 */
	public static double cubeRoot(double i_in)
	{
		double res = Math.pow(Math.abs(i_in), 1.0 / 3.0);
		return (i_in >= 0) ? res : -res;
	}
	/**
	 * ユークリッドの互除法により、2変数の最大公約数を求める。
	 * http://ja.wikipedia.org/wiki/%E3%83%A6%E3%83%BC%E3%82%AF%E3%83%AA%E3%83%83%E3%83%89%E3%81%AE%E4%BA%92%E9%99%A4%E6%B3%95
	 * @param i_x
	 * @param i_y
	 * @return
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
