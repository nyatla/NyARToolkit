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
package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.math.NyARMath;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

/**
 * Construct the homography constraint matrix from 4 point correspondences.
 * copy from Homography4PointsInhomogeneousConstraint.
 */
public class HomographySolver
{
	final static protected double SQRT2=NyARMath.SQRT2;
	
	private boolean solveHomography4PointsInhomogenous(NyARDoubleMatrix33 i_homography_mat,
			NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4,
			NyARDoublePoint2d xp1,NyARDoublePoint2d xp2, NyARDoublePoint2d xp3, NyARDoublePoint2d xp4) {
		double[][] _mat_A=new double[8][9];

//		x1.setValue(0, 0);x2.setValue(10, 0);x3.setValue(10, 10);x4.setValue(0, 10);
//		xp1.setValue(10, 10);xp2.setValue(10, 0);xp3.setValue(0, 0);xp4.setValue(0, 10);		
		
		//Homography4PointsInhomogeneousConstraint
		AddHomographyPointContraint(_mat_A, 0, x1, xp1);
		AddHomographyPointContraint(_mat_A, 2, x2, xp2);
		AddHomographyPointContraint(_mat_A, 4, x3, xp3);
		AddHomographyPointContraint(_mat_A, 6, x4, xp4);
		//SolveHomography4PointsInhomogenous
		if (!this.solveNullVector8x9Destructive(i_homography_mat,_mat_A)) {
			return false;
		}
		if (Math.abs(i_homography_mat.determinant()) < 1e-5) {
			return false;
		}
		return true;
	}
	
	/**
	 * Condition four 2D points such that the mean is zero and the standard
	 * deviation is sqrt(2).
	 */
	private static boolean condition4Points2d(NyARDoublePoint2d xp1, NyARDoublePoint2d xp2, NyARDoublePoint2d xp3,
			NyARDoublePoint2d xp4, double[] mus, // ms[2],sの3要素
			FreakFeaturePoint x1, FreakFeaturePoint x2, FreakFeaturePoint x3, FreakFeaturePoint x4) {
		double[] d1 = new double[2], d2 = new double[2], d3 = new double[2], d4 = new double[2];

		mus[0] = (x1.x + x2.x + x3.x + x4.x) / 4;
		mus[1] = (x1.y + x2.y + x3.y + x4.y) / 4;

		d1[0] = x1.x - mus[0];
		d1[1] = x1.y - mus[1];
		d2[0] = x2.x - mus[0];
		d2[1] = x2.y - mus[1];
		d3[0] = x3.x - mus[0];
		d3[1] = x3.y - mus[1];
		d4[0] = x4.x - mus[0];
		d4[1] = x4.y - mus[1];

		double ds1 = (double) Math.sqrt(d1[0] * d1[0] + d1[1] * d1[1]);
		double ds2 = (double) Math.sqrt(d2[0] * d2[0] + d2[1] * d2[1]);
		double ds3 = (double) Math.sqrt(d3[0] * d3[0] + d3[1] * d3[1]);
		double ds4 = (double) Math.sqrt(d4[0] * d4[0] + d4[1] * d4[1]);
		double d = (ds1 + ds2 + ds3 + ds4) / 4;

		if (d == 0) {
			return false;
		}

		double s = (double) ((1 / d) * SQRT2);
		mus[2] = s;
		xp1.x = d1[0] * s;
		xp1.y = d1[1] * s;
		xp2.x = d2[0] * s;
		xp2.y = d2[1] * s;
		xp3.x = d3[0] * s;
		xp3.y = d3[1] * s;
		xp4.x = d4[0] * s;
		xp4.y = d4[1] * s;

		return true;
	}






	/**
	 * SolveHomography4Points
	 * Solve for the homography given 4 point correspondences.
	 */
	public boolean solveHomography4Points(FeaturePairStack.Item p1, FeaturePairStack.Item p2, FeaturePairStack.Item p3, FeaturePairStack.Item p4,HomographyMat H) {

		// T s, sp;
		// T t[2], tp[2];

		NyARDoublePoint2d x1p = new NyARDoublePoint2d(), x2p = new NyARDoublePoint2d(), x3p = new NyARDoublePoint2d(), x4p = new NyARDoublePoint2d();
		NyARDoublePoint2d xp1p = new NyARDoublePoint2d(), xp2p = new NyARDoublePoint2d(), xp3p = new NyARDoublePoint2d(), xp4p = new NyARDoublePoint2d();
		double[] ts = new double[3];
		double[] tps = new double[3];
		//
		// Condition the points
		//

		if (!condition4Points2d(x1p, x2p, x3p, x4p, ts, p1.ref, p2.ref, p3.ref, p4.ref)) {
			return false;
		}
		if (!condition4Points2d(xp1p, xp2p, xp3p, xp4p, tps, p1.query, p2.query, p3.query, p4.query)) {
			return false;
		}

		//
		// Solve for the homography
		//

		if (!this.solveHomography4PointsInhomogenous(H, x1p, x2p, x3p, x4p, xp1p,xp2p, xp3p, xp4p))
		{
			return false;
		}

		//
		// Denomalize the computed homography
		//
		H.denormalizeHomography(ts, tps);
		return true;
	}	
	
	
	
	
	
	/**
	 * Add a point to the homography constraint matrix.
	 */
	private static void AddHomographyPointContraint(double A[][], int A_ptr, NyARDoublePoint2d x, NyARDoublePoint2d xp) {

		A[A_ptr][0] = -x.x;//[0];
		A[A_ptr][1] = -x.y;//[1];
		A[A_ptr][2] = -1;
		// ZeroVector3(A+3);
		A[A_ptr][3] = 0;
		A[A_ptr][4] = 0;
		A[A_ptr][5] = 0;

		A[A_ptr][6] = xp.x * x.x;//xp[0] * x[0];
		A[A_ptr][7] = xp.x * x.y;//xp[0] * x[1];
		A[A_ptr][8] = xp.x;//xp[0];

		// ZeroVector3(A+9);
		A[A_ptr+1][0] = 0;
		A[A_ptr+1][1] = 0;
		A[A_ptr+1][2] = 0;

		A[A_ptr+1][3] = -x.x;//-x[0];
		A[A_ptr+1][4] = -x.y;//-x[1];
		A[A_ptr+1][5] = -1;
		A[A_ptr+1][6] = xp.y * x.x;//xp[1] * x[0];
		A[A_ptr+1][7] = xp.y * x.y;//xp[1] * x[1];
		A[A_ptr+1][8] = xp.y;//xp[1];
	}
	
	//liner_solver.h
	
	
	private static double DotProduct9(double[] a, double[] b) {
		return a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3] + a[4] * b[4] + a[5] * b[5] + a[6] * b[6] + a[7]
				* b[7] + a[8] * b[8];
	}

	/**
	 * Sum sqaured.
	 */
	private static double SumSquares9(double[] x) {
		return DotProduct9(x, x);
	}




	/**
	 * Accumulate a scaled vector.
	 * 
	 * dst += src*s
	 */
	private static void AccumulateScaledVector9(double[] dst, double[] src, double s) {
		dst[0] += src[0] * s;
		dst[1] += src[1] * s;
		dst[2] += src[2] * s;
		dst[3] += src[3] * s;
		dst[4] += src[4] * s;
		dst[5] += src[5] * s;
		dst[6] += src[6] * s;
		dst[7] += src[7] * s;
		dst[8] += src[8] * s;
		return;
	}

	/**
	 * Project a vector "a" onto a normalized basis vector "e".
	 * 
	 * x = x - dot(a,e)*e
	 */
	private static void AccumulateProjection9(double[] x, double[] e, double[] a) {
		double d = DotProduct9(a, e);
		x[0] -= d * e[0];
		x[1] -= d * e[1];
		x[2] -= d * e[2];
		x[3] -= d * e[3];
		x[4] -= d * e[4];
		x[5] -= d * e[5];
		x[6] -= d * e[6];
		x[7] -= d * e[7];
		x[8] -= d * e[8];
	}

	/**
	 * Swap the contents of two vectors.
	 */
	private static void Swap9(double[] a, double[] b) {
		double tmp;
		for (int i = 0; i < 9; i++) {
			tmp = a[i];
			a[i] = b[i];
			b[i] = tmp;
		}
	}

	private static void ScaleVector9(double[] dst, double[] src, double s) {
		dst[0] = src[0] * s;
		dst[1] = src[1] * s;
		dst[2] = src[2] * s;
		dst[3] = src[3] * s;
		dst[4] = src[4] * s;
		dst[5] = src[5] * s;
		dst[6] = src[6] * s;
		dst[7] = src[7] * s;
		dst[8] = src[8] * s;
	}

	/**
	 * \defgroup Project the rows of A onto the current basis set to identity a new orthogonal vector.
	 * 
	 * @{
	 */
	private static boolean OrthogonalizePivot8x9Basis0(double[][] Q, double[][] A) {
		int index = 0;
		double ss = 0;
		for (int i = 7; i >= 0; i--) {
			double t = SumSquares9(A[i]);
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss == 0) {
			return false;
		}
		Swap9(A[0], A[index]);
		ScaleVector9(Q[0], A[0], (double) (1.f / Math.sqrt(ss)));
		CopyVector(Q[1], 0, A[1], 0, 9);
		CopyVector(Q[2], 0, A[2], 0, 9);
		CopyVector(Q[3], 0, A[3], 0, 9);
		CopyVector(Q[4], 0, A[4], 0, 9);
		CopyVector(Q[5], 0, A[5], 0, 9);
		CopyVector(Q[6], 0, A[6], 0, 9);
		CopyVector(Q[7], 0, A[7], 0, 9);

		return true;
	}

	// boolean OrthogonalizePivot8x9Basis1(T Q[8*9], T A[8*9]) {
	private static boolean OrthogonalizePivot8x9Basis1(double[][] Q, double[][] A) {
		int index = 0;
		double ss = 0;
		for (int i = 6; i >= 0; i--) {
			AccumulateProjection9(Q[i + 1], Q[0], A[i + 1]);
			double t = SumSquares9(Q[i + 1]);
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss == 0) {
			return false;
		}
		Swap9(Q[1], Q[1 + index]);
		Swap9(A[1], A[1 + index]);
		ScaleVector9(Q[1], Q[1], (double) (1.f / Math.sqrt(ss)));

		return true;
	}

	// boolean OrthogonalizePivot8x9Basis2(T Q[8*9], T A[8*9]) {
	private static boolean OrthogonalizePivot8x9Basis2(double[][] Q, double[][] A) {
		int index = 0;
		double ss = 0;
		for (int i = 5; i >= 0; i--) {
			AccumulateProjection9(Q[i + 2], Q[1], A[i + 2]);
			double t = SumSquares9(Q[i + 2]);
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss == 0) {
			return false;
		}

		Swap9(Q[2], Q[2 + index]);
		Swap9(A[2], A[2 + index]);
		ScaleVector9(Q[2], Q[2], (double) (1.f / Math.sqrt(ss)));

		return true;
	}

	private static boolean OrthogonalizePivot8x9Basis3(double[][] Q, double[][] A) {
		int index = 0;
		double ss = 0;
		for (int i = 4; i >= 0; i--) {
			AccumulateProjection9(Q[i + 3], Q[2], A[i + 3]);
			double t = SumSquares9(Q[i + 3]);
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss == 0) {
			return false;
		}

		Swap9(Q[3], Q[3 + index]);
		Swap9(A[3], A[3 + index]);
		ScaleVector9(Q[3], Q[3], (double) (1.f / Math.sqrt(ss)));

		return true;
	}

	private static boolean OrthogonalizePivot8x9Basis4(double[][] Q, double[][] A) {
		int index = 0;
		double ss = 0;
		for (int i = 3; i >= 0; i--) {
			AccumulateProjection9(Q[i + 4], Q[3], A[i + 4]);
			double t = SumSquares9(Q[i + 4]);
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss == 0) {
			return false;
		}

		Swap9(Q[4], Q[4 + index]);
		Swap9(A[4], A[4 + index]);
		ScaleVector9(Q[4], Q[4], (double) (1.f / Math.sqrt(ss)));

		return true;
	}

	private static boolean OrthogonalizePivot8x9Basis5(double[][] Q, double[][] A) {
		int index = 0;
		double ss = 0;
		for (int i = 2; i >= 0; i--) {
			AccumulateProjection9(Q[i + 5], Q[4], A[i + 5]);
			double t = SumSquares9(Q[i + 5]);
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss == 0) {
			return false;
		}

		Swap9(Q[5], Q[5 + index]);
		Swap9(A[5], A[5 + index]);
		ScaleVector9(Q[5], Q[5], (double) (1.f / Math.sqrt(ss)));

		return true;
	}

	private static boolean OrthogonalizePivot8x9Basis6(double[][] Q, double[][] A) {

		int index = 0;
		AccumulateProjection9(Q[6], Q[5], A[6]);
		double ss = SumSquares9(Q[6]);
		AccumulateProjection9(Q[7], Q[5], A[7]);
		double s2 = SumSquares9(Q[7]);
		if (ss < s2) {
			ss = s2;
			index = 1;
		}

		Swap9(Q[6], Q[6 + index]);
		Swap9(A[6], A[6 + index]);
		ScaleVector9(Q[6], Q[6], (double) (1.f / Math.sqrt(ss)));

		return true;
	}

	private static boolean OrthogonalizePivot8x9Basis7(double[][] Q, double[][] A) {
		AccumulateProjection9(Q[7], Q[6], A[7]);
		double ss = SumSquares9(Q[7]);
		if (ss == 0) {
			return false;
		}
		ScaleVector9(Q[7], Q[7], (double) (1.f / Math.sqrt(ss)));
		return true;
	}

	// float OrthogonalizeIdentity8x9(float x[9], const T Q[72], int i) {
	private static double OrthogonalizeIdentity8x9(double[] x, double[][] Q, int i) {
		ScaleVector9(x, Q[0], -Q[0][i]);
		x[i] = 1 + x[i];

		AccumulateScaledVector9(x, Q[1], -Q[1][i]);
		AccumulateScaledVector9(x, Q[2], -Q[2][i]);
		AccumulateScaledVector9(x, Q[3], -Q[3][i]);
		AccumulateScaledVector9(x, Q[4], -Q[4][i]);
		AccumulateScaledVector9(x, Q[5], -Q[5][i]);
		AccumulateScaledVector9(x, Q[6], -Q[6][i]);
		AccumulateScaledVector9(x, Q[7], -Q[7][i]);

		double ss = SumSquares9(x);
		if (ss == 0) {
			return 0;
		}

		double w = (double) Math.sqrt(ss);
		ScaleVector9(x, x, 1.f / w);

		return w;
	}


	// boolean OrthogonalizeIdentity8x9(T x[9], const T Q[72]) {
	private boolean OrthogonalizeIdentity8x9(NyARDoubleMatrix33 x, double[][] Q)
	{
		double[] XX=new double[9];
		double max_w=0;
		for(int i=8;i>=0;i--){
			double w=OrthogonalizeIdentity8x9(XX, Q, i);
			if(w>max_w){
				max_w=w;
				x.m00 = XX[0];
				x.m01 = XX[1];
				x.m02 = XX[2];
				x.m10 = XX[3];
				x.m11 = XX[4];
				x.m12 = XX[5];
				x.m20 = XX[6];
				x.m21 = XX[7];
				x.m22 = XX[8];				
			}
		}
		return true;
	}


	/**
	 * Solve for the null vector x of an 8x9 matrix A such A*x=0. The matrix A is destroyed in the process. This system
	 * is solved using QR decomposition with Gram-Schmidt.
	 */
	private boolean solveNullVector8x9Destructive(NyARDoubleMatrix33 x, double[][] A) {
		double[][] Q = new double[8][9];

		if (!OrthogonalizePivot8x9Basis0(Q, A))
			return false;
		if (!OrthogonalizePivot8x9Basis1(Q, A))
			return false;
		if (!OrthogonalizePivot8x9Basis2(Q, A))
			return false;
		if (!OrthogonalizePivot8x9Basis3(Q, A))
			return false;
		if (!OrthogonalizePivot8x9Basis4(Q, A))
			return false;
		if (!OrthogonalizePivot8x9Basis5(Q, A))
			return false;
		if (!OrthogonalizePivot8x9Basis6(Q, A))
			return false;
		if (!OrthogonalizePivot8x9Basis7(Q, A))
			return false;

		return OrthogonalizeIdentity8x9(x, Q);
	}
	private static void CopyVector(double[] dst,int i_dst_idx, double[] src,int i_src_idx,int i_len) {
    	for(int i=0;i<i_len;i++){
    		dst[i_dst_idx+i]=src[i_src_idx+i];
    	}
    }  	
}