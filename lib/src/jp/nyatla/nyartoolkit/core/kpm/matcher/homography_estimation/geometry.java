package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;


import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;


public class geometry {




    /**
	 * Find which side of a line a point is on (+,-).
	 *
	 * @param[in] A First point on line
	 * @param[in] B Second point on line
	 * @param[in] C Arbitrary third point
	 */
//	float LinePointSide(float A[2], float B[], const T C[2]) {
	public static double LinePointSide(NyARDoublePoint2d A, NyARDoublePoint2d B,NyARDoublePoint2d C) {
//		return ((B[0]-A[0])*(C[1]-A[1])-(B[1]-A[1])*(C[0]-A[0]));
		return ((B.x-A.x)*(C.y-A.y)-(B.y-A.y)*(C.x-A.x));
	}
   	

}
