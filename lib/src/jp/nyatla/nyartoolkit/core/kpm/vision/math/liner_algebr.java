package jp.nyatla.nyartoolkit.core.kpm.vision.math;

public class liner_algebr {
	static double Cofactor2x2(double a, double b, double c) {
		return (a * c) - (b * b);
	}
	static double Cofactor2x2(double a, double b, double c, double d)
	{
		return (a * d) - (b * c);
	}	
	public static double Determinant3x3(double[] A) {
		double C1 = Cofactor2x2(A[4], A[5], A[7], A[8]);
        double C2 = Cofactor2x2(A[3], A[5], A[6], A[8]);
        double C3 = Cofactor2x2(A[3], A[4], A[6], A[7]);
		return (A[0] * C1) - (A[1] * C2) + (A[2] * C3);
	}    	
	public static boolean MatrixInverse3x3(double[] B,double[] A, double threshold) {
		double det = Determinant3x3(A);
		
		if(Math.abs(det) <= threshold) {
			return false;
		}
		
		double one_over_det = (double) (1./det);
		
		B[0] = Cofactor2x2(A[4], A[5], A[7], A[8]) * one_over_det;
		B[1] = Cofactor2x2(A[2], A[1], A[8], A[7]) * one_over_det;
		B[2] = Cofactor2x2(A[1], A[2], A[4], A[5]) * one_over_det;
		B[3] = Cofactor2x2(A[5], A[3], A[8], A[6]) * one_over_det;
		B[4] = Cofactor2x2(A[0], A[2], A[6], A[8]) * one_over_det;
		B[5] = Cofactor2x2(A[2], A[0], A[5], A[3]) * one_over_det;
		B[6] = Cofactor2x2(A[3], A[4], A[6], A[7]) * one_over_det;
		B[7] = Cofactor2x2(A[1], A[0], A[7], A[6]) * one_over_det;
		B[8] = Cofactor2x2(A[0], A[1], A[3], A[4]) * one_over_det;
		
		return true;
	}
	
}
