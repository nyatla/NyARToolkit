package jp.nyatla.nyartoolkit.core.kpm.vision.math;

import jp.nyatla.nyartoolkit.core.kpm.Point2d;

public class geometry {
    /**
	 * Find which side of a line a point is on (+,-).
	 *
	 * @param[in] A First point on line
	 * @param[in] B Second point on line
	 * @param[in] C Arbitrary third point
	 */
//	float LinePointSide(float A[2], float B[], const T C[2]) {
	public static float LinePointSide(Point2d A, Point2d B,Point2d C) {
//		return ((B[0]-A[0])*(C[1]-A[1])-(B[1]-A[1])*(C[0]-A[0]));
		return ((B.x-A.x)*(C.y-A.y)-(B.y-A.y)*(C.x-A.x));
	}
    /**
     * Check the geometric consistency between three correspondences.
     */
    public static boolean Homography3PointsGeometricallyConsistent(Point2d x1, Point2d x2, Point2d x3,Point2d x1p, Point2d x2p, Point2d x3p)
    {
        if(((LinePointSide(x1, x2, x3) > 0) ^ (LinePointSide(x1p, x2p, x3p) > 0)) == true) {
            return false;
        }
        return true;
    }    	
    /**
     * Check the geometric consistency between four correspondences.
     */
//    boolean Homography4PointsGeometricallyConsistent(const T x1[2], const T x2[2], const T x3[2], const T x4[2],const T x1p[2], const T x2p[2], const T x3p[2], const T x4p[2]) {
	public static boolean Homography4PointsGeometricallyConsistent(Point2d x1, Point2d x2, Point2d x3, Point2d x4,Point2d x1p,Point2d x2p,Point2d x3p,Point2d x4p) {
        if(((LinePointSide(x1, x2, x3) > 0) ^ (LinePointSide(x1p, x2p, x3p) > 0)) == true)
            return false;
        if(((LinePointSide(x2, x3, x4) > 0) ^ (LinePointSide(x2p, x3p, x4p) > 0)) == true)
            return false;
        if(((LinePointSide(x3, x4, x1) > 0) ^ (LinePointSide(x3p, x4p, x1p) > 0)) == true)
            return false;
        if(((LinePointSide(x4, x1, x2) > 0) ^ (LinePointSide(x4p, x1p, x2p) > 0)) == true)
            return false;
        return true;
    }



}
