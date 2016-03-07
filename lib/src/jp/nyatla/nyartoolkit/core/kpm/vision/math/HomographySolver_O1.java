package jp.nyatla.nyartoolkit.core.kpm.vision.math;

import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.HomographyMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

/**
 * Construct the homography constraint matrix from 4 point correspondences.
 * copy from Homography4PointsInhomogeneousConstraint.
 */
public class HomographySolver_O1
{
	private double[][] _mat_A=new double[8][9];
	private boolean solveHomography4PointsInhomogenous(NyARDoubleMatrix33 i_homography_mat,
			NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4,
			NyARDoublePoint2d xp1,NyARDoublePoint2d xp2, NyARDoublePoint2d xp3, NyARDoublePoint2d xp4) {

//		x1.setValue(0, 0);x2.setValue(10, 0);x3.setValue(10, 10);x4.setValue(0, 10);
//		xp1.setValue(10, 10);xp2.setValue(10, 0);xp3.setValue(0, 0);xp4.setValue(0, 10);		
		
		
		//Homography4PointsInhomogeneousConstraint
		AddHomographyPointContraint(this._mat_A, 0, x1, xp1);
		AddHomographyPointContraint(this._mat_A, 2, x2, xp2);
		AddHomographyPointContraint(this._mat_A, 4, x3, xp3);
		AddHomographyPointContraint(this._mat_A, 6, x4, xp4);
		//SolveHomography4PointsInhomogenous
		if (!this.solveNullVector8x9Destructive(i_homography_mat,this._mat_A)) {
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
			NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4) {


		double mus_0 = (x1.x + x2.x + x3.x + x4.x) / 4;
		double mus_1 = (x1.y + x2.y + x3.y + x4.y) / 4;

		double d1_0 = x1.x - mus_0;
		double d1_1 = x1.y - mus_1;
		double d2_0 = x2.x - mus_0;
		double d2_1 = x2.y - mus_1;
		double d3_0 = x3.x - mus_0;
		double d3_1 = x3.y - mus_1;
		double d4_0 = x4.x - mus_0;
		double d4_1 = x4.y - mus_1;

		double d = (Math.sqrt(d1_0 * d1_0 + d1_1 * d1_1) + Math.sqrt(d2_0 * d2_0 + d2_1 * d2_1) + Math.sqrt(d3_0 * d3_0 + d3_1 * d3_1) + Math.sqrt(d4_0 * d4_0 + d4_1 * d4_1)) / 4;

		if (d == 0) {
			return false;
		}

		double s = (double) ((1 / d) * math_utils.SQRT2);
		xp1.x = d1_0 * s;
		xp1.y = d1_1 * s;
		xp2.x = d2_0 * s;
		xp2.y = d2_1 * s;
		xp3.x = d3_0 * s;
		xp3.y = d3_1 * s;
		xp4.x = d4_0 * s;
		xp4.y = d4_1 * s;
		mus[0] = mus_0;
		mus[1] = mus_1;
		mus[2] = s;

		return true;
	}






	/**
	 * SolveHomography4Points
	 * Solve for the homography given 4 point correspondences.
	 */
	public boolean solveHomography4Points(HomographyMat H, NyARDoublePoint2d x1, NyARDoublePoint2d x2,
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

		if (!condition4Points2d(x1p, x2p, x3p, x4p, ts, x1, x2, x3, x4)) {
			return false;
		}
		if (!condition4Points2d(xp1p, xp2p, xp3p, xp4p, tps, xp1, xp2, xp3, xp4)) {
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
	private static void AddHomographyPointContraint(double A[][], int A_ptr, NyARDoublePoint2d x, NyARDoublePoint2d xp)
	{

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
	 * \defgroup Project the rows of A onto the current basis set to identity a new orthogonal vector.
	 * 
	 * @{
	 */
	private static boolean OrthogonalizePivot8x9Basis0(double[][] Q, double[][] A)
	{
		int index = 0;
		double ss = 0;
		for (int i = 7; i >= 0; i--) {
			double[] x=A[i];
			double t=x[0] * x[0] + x[1] * x[1] + x[2] * x[2] + x[3] * x[3] + x[4] * x[4] + x[5] * x[5] + x[6] * x[6] + x[7]* x[7] + x[8] * x[8];
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss == 0) {
			return false;
		}
		{	//Swap9(A[0],A[index]);
			double[] swap=A[0];
			A[0]=A[index];
			A[index]=swap;
		}
		{	//ScaleVector9(Q[0], A[0], (double) (1.f / Math.sqrt(ss)));
			double[] d=Q[0];
			double[] src=A[0];
			double s=(double) (1.f / Math.sqrt(ss));
			d[0] = src[0] * s;
			d[1] = src[1] * s;
			d[2] = src[2] * s;
			d[3] = src[3] * s;
			d[4] = src[4] * s;
			d[5] = src[5] * s;
			d[6] = src[6] * s;
			d[7] = src[7] * s;
			d[8] = src[8] * s;
		}		
		for(int i=1;i<8;i++){
    		double d[]=Q[i];
    		double s[]=A[i];
    		d[0]=s[0];
    		d[1]=s[1];
    		d[2]=s[2];
    		d[3]=s[3];
    		d[4]=s[4];
    		d[5]=s[5];
    		d[6]=s[6];
    		d[7]=s[7];
    		d[8]=s[8];
		}
		return true;
	}

	
	
	
	public static boolean OrthogonalizePivot8x9Basis(double dest[][],int line, double[][] a)
	{
		int p=7-line;
		int index = 0;
		double ss = 0;
		for (int i = line; i >= 0; i--) {
			//AccumulateProjection9
			double ee[]=dest[6-line];
			double aa[]= a[i + p];
			double xx[]=dest[i + p];
			double d=ee[0] * aa[0] + ee[1] * aa[1] + ee[2] * aa[2] + ee[3] * aa[3] + ee[4] * aa[4] + ee[5] * aa[5] + ee[6] * aa[6] + ee[7]* aa[7] + ee[8] * aa[8];
			xx[0] -= d * ee[0];
			xx[1] -= d * ee[1];
			xx[2] -= d * ee[2];
			xx[3] -= d * ee[3];
			xx[4] -= d * ee[4];
			xx[5] -= d * ee[5];
			xx[6] -= d * ee[6];
			xx[7] -= d * ee[7];
			xx[8] -= d * ee[8];			
			
//			AccumulateProjection9(dest[i + p],dest[6-line], a[i + p]);
			//double t = SumSquares9(dest[i + p]);
			double t=xx[0] * xx[0] + xx[1] * xx[1] + xx[2] * xx[2] + xx[3] * xx[3] + xx[4] * xx[4] + xx[5] * xx[5] + xx[6] * xx[6] + xx[7]* xx[7] + xx[8] * xx[8];
			if (t > ss) {
				ss = t;
				index = i;
			}
		}
		if (ss<0) {
			return false;
		}
		//swap
		{	//Swap9(dest[p], dest[p + index]);
			double[] t;
			t=dest[p];
			dest[p]=dest[p + index];
			dest[p + index]=t;
			t=a[p];
			a[p]=a[p + index];
			a[p + index]=t;
		}
		{	//ScaleVector9(dest[p], dest[p], (double) (1.f / Math.sqrt(ss)));
			double[] dst=dest[p];
			double s=(double) (1.f / Math.sqrt(ss));
			dst[0]*=s;
			dst[1]*=s;
			dst[2]*=s;
			dst[3]*=s;
			dst[4]*=s;
			dst[5]*=s;
			dst[6]*=s;
			dst[7]*=s;
			dst[8]*=s;
		}
		return true;
	}



	// float OrthogonalizeIdentity8x9(float x[9], const T Q[72], int i) {
	private static double OrthogonalizeIdentity8x9(double[] x, double[][] Q, int i)
	{
		
		{	//ScaleVector9(x, Q[0], -Q[0][i]);
			double[] src=Q[0];
			double s=-src[i];
			x[0] = src[0] * s;
			x[1] = src[1] * s;
			x[2] = src[2] * s;
			x[3] = src[3] * s;
			x[4] = src[4] * s;
			x[5] = src[5] * s;
			x[6] = src[6] * s;
			x[7] = src[7] * s;
			x[8] = src[8] * s;			
		}
		x[i] = 1 + x[i];
		
		for(int j=1;j<8;j++){
			double[] src=Q[j];
			double s=src[i];
			x[0] -= src[0] * s;
			x[1] -= src[1] * s;
			x[2] -= src[2] * s;
			x[3] -= src[3] * s;
			x[4] -= src[4] * s;
			x[5] -= src[5] * s;
			x[6] -= src[6] * s;
			x[7] -= src[7] * s;
			x[8] -= src[8] * s;			
		}
		double ss;
		{	//double ss = SumSquares9(x);
			ss=x[0] * x[0] + x[1] * x[1] + x[2] * x[2] + x[3] * x[3] + x[4] * x[4] + x[5] * x[5] + x[6] * x[6] + x[7]* x[7] + x[8] * x[8];
		}
		if (ss == 0) {
			return 0;
		}

		double w =(double) Math.sqrt(ss);		
		{	//ScaleVector9(x, x, 1.f / w);
			double iw=1.0/w;
			x[0]*=iw;
			x[1]*=iw;
			x[2]*=iw;
			x[3]*=iw;
			x[4]*=iw;
			x[5]*=iw;
			x[6]*=iw;
			x[7]*=iw;
			x[8]*=iw;			
		}
		return w;
	}
	private final double[] _OrthogonalizeIdentity8x9_X=new double[9];

	// boolean OrthogonalizeIdentity8x9(T x[9], const T Q[72]) {
	private boolean OrthogonalizeIdentity8x9(NyARDoubleMatrix33 x, double[][] Q)
	{
		double[] XX=this._OrthogonalizeIdentity8x9_X;
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

	private final double[][] _solveNullVector8x9Destructive_Q=new double[8][9];
	/**
	 * Solve for the null vector x of an 8x9 matrix A such A*x=0. The matrix A is destroyed in the process. This system
	 * is solved using QR decomposition with Gram-Schmidt.
	 */
	private boolean solveNullVector8x9Destructive(NyARDoubleMatrix33 x, double[][] A) {
		double[][] Q = this._solveNullVector8x9Destructive_Q;

		if (!OrthogonalizePivot8x9Basis0(Q, A)){
			return false;
		}
		if (!OrthogonalizePivot8x9Basis(Q,6,A)){
			return false;
		}
		if (!OrthogonalizePivot8x9Basis(Q,5,A)){
			return false;
		}
		if (!OrthogonalizePivot8x9Basis(Q,4,A)){
			return false;
		}
		if (!OrthogonalizePivot8x9Basis(Q,3,A)){
			return false;
		}
		if (!OrthogonalizePivot8x9Basis(Q,2,A)){
			return false;
		}
		if (!OrthogonalizePivot8x9Basis(Q,1,A)){
			return false;
		}
		if (!OrthogonalizePivot8x9Basis(Q,0,A)){
			return false;
		}

		return OrthogonalizeIdentity8x9(x, Q);
	}	
}