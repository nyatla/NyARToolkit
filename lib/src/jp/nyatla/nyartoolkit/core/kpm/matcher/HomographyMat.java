package jp.nyatla.nyartoolkit.core.kpm.matcher;

import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class HomographyMat extends NyARDoubleMatrix33
{

	/**
	 * Normalize a homography such that H(3,3) = 1.
	 */
	public void normalizeHomography()
	{
		double one_over = (1. / this.m22);
		this.m00 *= one_over;
		this.m01 *= one_over;
		this.m02 *= one_over;
		this.m10 *= one_over;
		this.m11 *= one_over;
		this.m12 *= one_over;
		this.m20 *= one_over;
		this.m21 *= one_over;
		this.m22 = 1.0;
	}
	/**
	 * Denomalize the homograhy matrix.
	 * 
	 * Hp = inv(Tp)*H*T
	 * 
	 * where T and Tp are the noramalizing transformations for the points that
	 * were used to compute H.
	 */
	public void denormalizeHomography(double[] ts, double[] tps)
	{
		double sp = tps[2];
		double a = this.m20 * tps[0];
		double b = this.m21 * tps[0];
		double c = this.m00 / sp;
		double d = this.m01 / sp;
		double apc = a + c;
		double bpd = b + d;

		double e = this.m20 * tps[1];
		double f = this.m21 * tps[1];
		double g = this.m10 / sp;
		double h = this.m11 / sp;
		double epg = e + g;
		double fph = f + h;

		double s = ts[2];
		double stx = s * ts[0];
		double sty = s * ts[1];

		this.m00 = s * apc;
		this.m01 = s * bpd;
		this.m02 = this.m22 * tps[0] + this.m02 / sp - stx * apc - sty * bpd;

		this.m10 = s * epg;
		this.m11 = s * fph;
		this.m12 = this.m22 * tps[1] + this.m12 / sp - stx * epg - sty * fph;

		this.m20 = this.m20 * s;
		this.m21 = this.m21 * s;
		this.m22 = this.m22 - this.m20 * ts[0] - this.m21 * ts[1];
		return;
	}
	public double cauchyProjectiveReprojectionCost(FeaturePairStack.Item i_ptr, double i_one_over_scale2)
	{
		NyARDoublePoint2d ref=i_ptr.ref;
    	double w = this.m20*ref.x + this.m21*ref.y + this.m22;
        double vx = ((this.m00*ref.x + this.m01*ref.y + this.m02)/w)-i_ptr.query.x;//XP
        double vy = ((this.m10*ref.x + this.m11*ref.y + this.m12)/w)-i_ptr.query.y;//YP
		double T= Math.log(1 + (vx * vx + vy * vy) * i_one_over_scale2);
		return T;
	}
	
}