/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Point2d;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.match.indexing;


public class geometry {
    /**
	 * Find which side of a line a point is on (+,-).
	 *
	 * @param[in] A First point on line
	 * @param[in] B Second point on line
	 * @param[in] C Arbitrary third point
	 */
//	float LinePointSide(float A[2], float B[], const T C[2]) {
	public static double LinePointSide(Point2d A, Point2d B,Point2d C) {
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
    /**
     * Subtract two vectors.
     */
    static void SubVector2(Point2d c, Point2d a, Point2d b) {
		c.x = a.x - b.x;
		c.y = a.y - b.y;
	}
    /**
     * Compute the area of a triangle.
     */
    static double AreaOfTriangle(Point2d u,Point2d v) {
//		T a = u[0]*v[1] - u[1]*v[0];
    	double a = u.x*v.y - u.y*v.x;
		return (double) (Math.abs(a)*0.5);
	}
	/**
	 * Find the smallest area for each triangle formed by 4 points.
	 */
	public static double SmallestTriangleArea(Point2d x1, Point2d x2, Point2d x3,Point2d x4)
	{
		Point2d v12=new Point2d();
		Point2d v13=new Point2d();
		Point2d v14=new Point2d();
		Point2d v32=new Point2d();
		Point2d v34=new Point2d();
	    
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
    public static boolean QuadrilateralConvex(Point2d x1,Point2d x2,Point2d x3,Point2d x4) {
        int s;
        
        s  = LinePointSide(x1, x2, x3) > 0 ? 1 : -1;
        s += LinePointSide(x2, x3, x4) > 0 ? 1 : -1;
        s += LinePointSide(x3, x4, x1) > 0 ? 1 : -1;
        s += LinePointSide(x4, x1, x2) > 0 ? 1 : -1;
        
        return (Math.abs(s) == 4);
    }
}
