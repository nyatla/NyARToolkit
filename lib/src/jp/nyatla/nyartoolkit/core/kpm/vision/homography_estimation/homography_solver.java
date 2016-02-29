package jp.nyatla.nyartoolkit.core.kpm.vision.homography_estimation;


import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.HomographyMat;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.Homography4PointsInhomogenousSolver;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.liner_algebr;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.liner_solver;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class homography_solver {

	/**
	 * Condition four 2D points such that the mean is zero and the standard
	 * deviation is sqrt(2).
	 */
	static boolean Condition4Points2d(NyARDoublePoint2d xp1, NyARDoublePoint2d xp2, NyARDoublePoint2d xp3,
			NyARDoublePoint2d xp4, double[] mus, // ms[2],sの3要素
			NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4) {
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
/*
		mus[0] = (x1[0] + x2[0] + x3[0] + x4[0]) / 4;
		mus[1] = (x1[1] + x2[1] + x3[1] + x4[1]) / 4;

		d1[0] = x1[0] - mus[0];
		d1[1] = x1[1] - mus[1];
		d2[0] = x2[0] - mus[0];
		d2[1] = x2[1] - mus[1];
		d3[0] = x3[0] - mus[0];
		d3[1] = x3[1] - mus[1];
		d4[0] = x4[0] - mus[0];
		d4[1] = x4[1] - mus[1];

 */
		double ds1 = (double) Math.sqrt(d1[0] * d1[0] + d1[1] * d1[1]);
		double ds2 = (double) Math.sqrt(d2[0] * d2[0] + d2[1] * d2[1]);
		double ds3 = (double) Math.sqrt(d3[0] * d3[0] + d3[1] * d3[1]);
		double ds4 = (double) Math.sqrt(d4[0] * d4[0] + d4[1] * d4[1]);
		double d = (ds1 + ds2 + ds3 + ds4) / 4;

		if (d == 0) {
			return false;
		}

		double s = (double) ((1 / d) * math_utils.SQRT2);
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
	 * Add a point to the homography constraint matrix.
	 */
	static void AddHomographyPointContraint(double A[][], int A_ptr, NyARDoublePoint2d x, NyARDoublePoint2d xp) {

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

	/**
	 * Construct the homography constraint matrix from 4 point correspondences.
	 */
	static void Homography4PointsInhomogeneousConstraint(
			double[][] A,// [8][9],
			NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4, NyARDoublePoint2d xp1,
			NyARDoublePoint2d xp2, NyARDoublePoint2d xp3, NyARDoublePoint2d xp4) {
		AddHomographyPointContraint(A, 0, x1, xp1);
		AddHomographyPointContraint(A, 2, x2, xp2);
		AddHomographyPointContraint(A, 4, x3, xp3);
		AddHomographyPointContraint(A, 6, x4, xp4);
	}

	/**
	 * Solve for the homography given four 2D point correspondences.
	 */
	static boolean SolveHomography4PointsInhomogenous(NyARDoubleMatrix33 H, NyARDoublePoint2d x1,
			NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4, NyARDoublePoint2d xp1, NyARDoublePoint2d xp2,
			NyARDoublePoint2d xp3, NyARDoublePoint2d xp4) {
		double[][] A = new double[8][9];
		Homography4PointsInhomogeneousConstraint(A, x1, x2, x3, x4, xp1, xp2,xp3, xp4);
		if (!liner_solver.SolveNullVector8x9Destructive(H, A)) {
			return false;
		}
		if (Math.abs(H.determinant()) < 1e-5) {
			return false;
		}
		return true;
	}

	/**
	 * Solve for the homography given 4 point correspondences.
	 */
	static // boolean SolveHomography4Points(float H[9],
	// float x1[2],
	// float x2[2],
	// float x3[2],
	// float x4[2],
	// float xp1[2],
	// float xp2[2],
	// float xp3[2],
	// float xp4[2]) {
	boolean SolveHomography4Points(HomographyMat H, NyARDoublePoint2d x1, NyARDoublePoint2d x2,
			NyARDoublePoint2d x3, NyARDoublePoint2d x4, NyARDoublePoint2d xp1, NyARDoublePoint2d xp2, NyARDoublePoint2d xp3,
			NyARDoublePoint2d xp4) {

		// T s, sp;
		// T t[2], tp[2];

		NyARDoublePoint2d x1p = new NyARDoublePoint2d(), x2p = new NyARDoublePoint2d(), x3p = new NyARDoublePoint2d(), x4p = new NyARDoublePoint2d();
		NyARDoublePoint2d xp1p = new NyARDoublePoint2d(), xp2p = new NyARDoublePoint2d(), xp3p = new NyARDoublePoint2d(), xp4p = new NyARDoublePoint2d();
		double[] ts = new double[3];
		double[] tps = new double[3];
		//
		// Condition the points
		//

		if (!Condition4Points2d(x1p, x2p, x3p, x4p, ts, x1, x2, x3, x4)) {
			return false;
		}
		if (!Condition4Points2d(xp1p, xp2p, xp3p, xp4p, tps, xp1, xp2, xp3, xp4)) {
			return false;
		}

		//
		// Solve for the homography
		//

		if (!SolveHomography4PointsInhomogenous(H, x1p, x2p, x3p, x4p, xp1p,
				xp2p, xp3p, xp4p)) {
			return false;
		}

		//
		// Denomalize the computed homography
		//
		H.denormalizeHomography(ts, tps);
		return true;
	}



}
