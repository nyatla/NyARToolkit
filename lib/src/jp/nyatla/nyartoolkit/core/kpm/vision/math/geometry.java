package jp.nyatla.nyartoolkit.core.kpm.vision.math;


import jp.nyatla.nyartoolkit.core.kpm.vision.match.indexing;
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
    /**
     * Check the geometric consistency between three correspondences.
     */
    public static boolean Homography3PointsGeometricallyConsistent(NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3,NyARDoublePoint2d x1p, NyARDoublePoint2d x2p, NyARDoublePoint2d x3p)
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
	public static boolean Homography4PointsGeometricallyConsistent(NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4,NyARDoublePoint2d x1p,NyARDoublePoint2d x2p,NyARDoublePoint2d x3p,NyARDoublePoint2d x4p) {
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
    /**
     * Subtract two vectors.
     */
    static void SubVector2(NyARDoublePoint2d c, NyARDoublePoint2d a, NyARDoublePoint2d b) {
		c.x = a.x - b.x;
		c.y = a.y - b.y;
	}
    /**
     * Compute the area of a triangle.
     */
    static double AreaOfTriangle(NyARDoublePoint2d u,NyARDoublePoint2d v) {
//		T a = u[0]*v[1] - u[1]*v[0];
    	double a = u.x*v.y - u.y*v.x;
		return (double) (Math.abs(a)*0.5);
	}
	/**
	 * Find the smallest area for each triangle formed by 4 points.
	 */
	public static double SmallestTriangleArea(NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3,NyARDoublePoint2d x4)
	{
		NyARDoublePoint2d v12=new NyARDoublePoint2d();
		NyARDoublePoint2d v13=new NyARDoublePoint2d();
		NyARDoublePoint2d v14=new NyARDoublePoint2d();
		NyARDoublePoint2d v32=new NyARDoublePoint2d();
		NyARDoublePoint2d v34=new NyARDoublePoint2d();
	    
		SubVector2(v12, x2, x1);
		SubVector2(v13, x3, x1);
		SubVector2(v14, x4, x1);
		SubVector2(v32, x2, x3);
		SubVector2(v34, x4, x3);
		
		double a1 = AreaOfTriangle(v12, v13);
		double a2 = AreaOfTriangle(v13, v14);
		double a3 = AreaOfTriangle(v12, v14);
	    double a4 = AreaOfTriangle(v32, v34);
		
		return indexing.min4(a1, a2, a3, a4);
	}
    /**
     * Check if four points form a convex quadrilaternal.
     */
    public static boolean QuadrilateralConvex(NyARDoublePoint2d x1,NyARDoublePoint2d x2,NyARDoublePoint2d x3,NyARDoublePoint2d x4) {
        int s;
        
        s  = LinePointSide(x1, x2, x3) > 0 ? 1 : -1;
        s += LinePointSide(x2, x3, x4) > 0 ? 1 : -1;
        s += LinePointSide(x3, x4, x1) > 0 ? 1 : -1;
        s += LinePointSide(x4, x1, x2) > 0 ? 1 : -1;
        
        return (Math.abs(s) == 4);
    }
}
