package jp.nyatla.nyartoolkit.core.utils;


/**
 * 数学関数を定義します。
 *
 */
public class NyARMath
{
	public final static int SQ_40=40*40;
	public final static int SQ_20=20*20;
	public final static int SQ_10=10*10;
	public final static int SQ_8=8*8;
	public final static int SQ_5=5*5;
	public final static int SQ_2=2*2;

	
	public final static double COS_DEG_30=0.8660;
	public final static double COS_DEG_25=0.9063;
	public final static double COS_DEG_20=0.9396;
	public final static double COS_DEG_15=0.9395;
	public final static double COS_DEG_10=0.9848;
	public final static double COS_DEG_8 =0.9902;
	public final static double COS_DEG_5 =0.9961;
	public static final double sqNorm(double i_p1x,double i_p1y,double i_p2x,double i_p2y)
	{
		double x,y;
		x=i_p2x-i_p1x;
		y=i_p2y-i_p1y;
		return x*x+y*y;
	}

	
	
	public static final double dist(double i_x1,double i_y1,double i_x2,double i_y2)
	{
		return Math.sqrt(i_x1*i_y1+i_x2+i_y2);
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
