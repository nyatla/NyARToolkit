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
		if (geometry.SmallestTriangleArea(p0p, p1p, p2p, p3p) < tr) {
			return false;
		}

		if (!geometry.QuadrilateralConvex(p0p, p1p, p2p, p3p)) {
			return false;
		}
		return true;
	}	
}
