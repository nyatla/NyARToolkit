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
	public double cauchyProjectiveReprojectionCostSum(FeaturePairStack.Item[] i_array,int i_s,int i_e,double i_one_over_scale2)
	{
		double ret=0;
		for(int i=i_s;i<i_e;i++){
			NyARDoublePoint2d ref=i_array[i].ref;
			NyARDoublePoint2d query=i_array[i].query;
	    	double w = this.m20*ref.x + this.m21*ref.y + this.m22;
	        double vx = ((this.m00*ref.x + this.m01*ref.y + this.m02)/w)-query.x;//XP
	        double vy = ((this.m10*ref.x + this.m11*ref.y + this.m12)/w)-query.y;//YP
			ret+= Math.log(1 + (vx * vx + vy * vy) * i_one_over_scale2);
		}
		return ret;
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
	public void multiplyPointHomographyInhomogenous(double i_x, double i_y, NyARDoublePoint2d i_dest)
	{
		double w = this.m20 * i_x + this.m21 * i_y + this.m22;
		i_dest.x = (this.m00 * i_x + this.m01 * i_y + this.m02) / w;// XP
		i_dest.y = (this.m10 * i_x + this.m11 * i_y + this.m12) / w;// YP
	}	
}