package jp.nyatla.nyartoolkit.core.kpm.matcher;


import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;


public class Geometry
{
    /**
	 * Find which side of a line a point is on (+,-).
	 *
	 * @param[in] A First point on line
	 * @param[in] B Second point on line
	 * @param[in] C Arbitrary third point
	 */
	public static double LinePointSide(NyARDoublePoint2d A, NyARDoublePoint2d B,NyARDoublePoint2d C) {
		return ((B.x-A.x)*(C.y-A.y)-(B.y-A.y)*(C.x-A.x));
	}
   	

}
