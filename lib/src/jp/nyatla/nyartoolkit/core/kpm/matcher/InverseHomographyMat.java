package jp.nyatla.nyartoolkit.core.kpm.matcher;

import jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation.geometry;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class InverseHomographyMat extends NyARDoubleMatrix33 {
	public void multiplyPointHomographyInhomogenous(double i_x, double i_y, NyARDoublePoint2d i_dest)
	{
		double w = this.m20 * i_x + this.m21 * i_y + this.m22;
		i_dest.x = (this.m00 * i_x + this.m01 * i_y + this.m02) / w;// XP
		i_dest.y = (this.m10 * i_x + this.m11 * i_y + this.m12) / w;// YP
	}
	/**
	 * CheckHomographyHeuristics
	 * Check if a homography is valid based on some heuristics.
	 * @param i_h_inv
	 * inversed homography matrix;
	 * @param refWidth
	 * @param refHeight
	 * @return
	 */
	public boolean checkHomographyHeuristics(int refWidth, int refHeight)
	{
		NyARDoublePoint2d p0p = new NyARDoublePoint2d();
		NyARDoublePoint2d p1p = new NyARDoublePoint2d();
		NyARDoublePoint2d p2p = new NyARDoublePoint2d();
		NyARDoublePoint2d p3p = new NyARDoublePoint2d();


		this.multiplyPointHomographyInhomogenous(0,0,p0p);
		this.multiplyPointHomographyInhomogenous(refWidth,0,p1p);
		this.multiplyPointHomographyInhomogenous(refWidth,refHeight,p2p);
		this.multiplyPointHomographyInhomogenous(0,refHeight,p3p);

		double tr = refWidth * refHeight * 0.0001f;
		if (SmallestTriangleArea(p0p, p1p, p2p, p3p) < tr) {
			return false;
		}

		if (!QuadrilateralConvex(p0p, p1p, p2p, p3p)) {
			return false;
		}
		return true;
	}
    /**
     * Check if four points form a convex quadrilaternal.
     */
    public static boolean QuadrilateralConvex(NyARDoublePoint2d x1,NyARDoublePoint2d x2,NyARDoublePoint2d x3,NyARDoublePoint2d x4) {
        int s;
        
        s  = geometry.LinePointSide(x1, x2, x3) > 0 ? 1 : -1;
        s += geometry.LinePointSide(x2, x3, x4) > 0 ? 1 : -1;
        s += geometry.LinePointSide(x3, x4, x1) > 0 ? 1 : -1;
        s += geometry.LinePointSide(x4, x1, x2) > 0 ? 1 : -1;
        
        return (Math.abs(s) == 4);
    }
	/**
	 * Find the smallest area for each triangle formed by 4 points.
	 */
	private static double SmallestTriangleArea(NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3,NyARDoublePoint2d x4)
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
		
		return min4(a1, a2, a3, a4);
	}
	private static double min4(double a1, double a2, double a3, double a4)
	{
		double r=a1;
		if(r>a2){
			r=a2;
		}
		if(r>a3){
			r=a3;
		}
		if(r>a4){
			r=a4;
		}
		return r;
	}	
    /**
     * Subtract two vectors.
     */
    private static void SubVector2(NyARDoublePoint2d c, NyARDoublePoint2d a, NyARDoublePoint2d b) {
		c.x = a.x - b.x;
		c.y = a.y - b.y;
	}
    /**
     * Compute the area of a triangle.
     */
    private static double AreaOfTriangle(NyARDoublePoint2d u,NyARDoublePoint2d v) {
//		T a = u[0]*v[1] - u[1]*v[0];
    	double a = u.x*v.y - u.y*v.x;
		return (double) (Math.abs(a)*0.5);
	}   
}
