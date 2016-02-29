package jp.nyatla.nyartoolkit.core.kpm.vision.math;


import jp.nyatla.nyartoolkit.core.kpm.vision.match.indexing;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class liner_solver {

    static double DotProduct9(double[] a,double[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2] + a[3]*b[3] + a[4]*b[4] + a[5]*b[5] + a[6]*b[6] + a[7]*b[7] + a[8]*b[8];
    }
    static double DotProduct9(double[] a,int a_ptr,double[] b,int b_ptr) {
        return a[a_ptr+0]*b[0+b_ptr] + a[a_ptr+1]*b[1+b_ptr] + a[a_ptr+2]*b[2+b_ptr] + a[a_ptr+3]*b[3+b_ptr] + a[a_ptr+4]*b[4+b_ptr] + a[a_ptr+5]*b[5+b_ptr] + a[a_ptr+6]*b[6+b_ptr] + a[a_ptr+7]*b[7+b_ptr] + a[a_ptr+8]*b[8+b_ptr];
    }
    
    /**
     * Sum sqaured.
     */
    static double SumSquares9(double[] x) {
        return DotProduct9(x, x);
    }
    static double SumSquares9(double[] x,int x_i) {
        return DotProduct9(x,x_i, x,x_i);
    }
    
    static int MaxIndex(double[] x,int len) {
        int index = 0;
        for(int i=1;i<len;i++){
        	if(x[i]>x[index]){
        		index=i;
        	}
        }
        return index;
    }
    /**
     * Accumulate a scaled vector.
     *
     * dst += src*s
     */
    static void AccumulateScaledVector9(double[] dst,double[] src,double s)
    {
        dst[0] += src[0]*s;
        dst[1] += src[1]*s;
        dst[2] += src[2]*s;
        dst[3] += src[3]*s;
        dst[4] += src[4]*s;
        dst[5] += src[5]*s;
        dst[6] += src[6]*s;
        dst[7] += src[7]*s;
        dst[8] += src[8]*s;
        return;
    }
    /**
     * Project a vector "a" onto a normalized basis vector "e".
     *
     * x = x - dot(a,e)*e
     */
static //    void AccumulateProjection9(T x[9], const T e[9], const T a[9]) {
    void AccumulateProjection9(double[] x,double[] e,double[] a) {
	double d = DotProduct9(a,e);
        x[0] -= d*e[0];
        x[1] -= d*e[1];
        x[2] -= d*e[2];
        x[3] -= d*e[3];
        x[4] -= d*e[4];
        x[5] -= d*e[5];
        x[6] -= d*e[6];
        x[7] -= d*e[7];
        x[8] -= d*e[8];
    }   
    
    /**
     * Swap the contents of two vectors.
     */
    static void Swap9(double[] a,double[] b) {
    	double tmp;
        for(int i=0;i<9;i++){
        	tmp = a[i]; a[i] = b[i]; b[i]= tmp;
        }
    }   
    private static void ScaleVector9(double[] dst,double[] src, double s) {
        dst[0] = src[0]*s;
        dst[1] = src[1]*s;
        dst[2] = src[2]*s;
        dst[3] = src[3]*s;
        dst[4] = src[4]*s;
        dst[5] = src[5]*s;
        dst[6] = src[6]*s;
        dst[7] = src[7]*s;
        dst[8] = src[8]*s;
    }    

    /**
     * \defgroup Project the rows of A onto the current basis set to identity a new orthogonal vector.
     * @{
     */
static boolean OrthogonalizePivot8x9Basis0(double[][] Q, double[][] A)
    {
		int index=0;
		double ss=0;
		for(int i=7;i>=0;i--){
			double t=SumSquares9(A[i]);
			if(t>ss){
				ss=t;
				index=i;
			}
		}
		if(ss==0){
			return false;
		}
        Swap9(A[0],A[index]);
        ScaleVector9(Q[0],A[0], (double)(1.f/Math.sqrt(ss)));
        indexing.CopyVector(Q[1],0, A[1],0,9);
        indexing.CopyVector(Q[2],0, A[2],0,9);
        indexing.CopyVector(Q[3],0, A[3],0,9);
        indexing.CopyVector(Q[4],0, A[4],0,9);
        indexing.CopyVector(Q[5],0, A[5],0,9);
        indexing.CopyVector(Q[6],0, A[6],0,9);
        indexing.CopyVector(Q[7],0, A[7],0,9);
        
        return true;
    }
//    boolean OrthogonalizePivot8x9Basis1(T Q[8*9], T A[8*9]) {
    static boolean OrthogonalizePivot8x9Basis1(double[][] Q, double[][] A)
    {
		int index=0;
		double ss=0;
		for(int i=6;i>=0;i--){
	        AccumulateProjection9(Q[i+1], Q[0], A[i+1]);
			double t=SumSquares9(Q[i+1]);
			if(t>ss){
				ss=t;
				index=i;
			}
		}
		if(ss==0){
			return false;
		}
        Swap9(Q[1], Q[1+index]);
        Swap9(A[1], A[1+index]);
        ScaleVector9(Q[1], Q[1], (double) (1.f/Math.sqrt(ss)));
        
        return true;
    }
//    boolean OrthogonalizePivot8x9Basis2(T Q[8*9], T A[8*9]) {  
    static boolean OrthogonalizePivot8x9Basis2(double[][] Q, double[][] A)
    {
		int index=0;
		double ss=0;
		for(int i=5;i>=0;i--){
	        AccumulateProjection9(Q[i+2], Q[1], A[i+2]);
			double t=SumSquares9(Q[i+2]);
			if(t>ss){
				ss=t;
				index=i;
			}
		}
		if(ss==0){
			return false;
		}        
        

        Swap9(Q[2], Q[2+index]);
        Swap9(A[2], A[2+index]);
        ScaleVector9(Q[2], Q[2], (double)(1.f/Math.sqrt(ss)));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis3(double[][] Q,double[][] A)
    {
		int index=0;
		double ss=0;
		for(int i=4;i>=0;i--){
	        AccumulateProjection9(Q[i+3], Q[2], A[i+3]);
			double t=SumSquares9(Q[i+3]);
			if(t>ss){
				ss=t;
				index=i;
			}
		}
		if(ss==0){
			return false;
		}
		
        
        Swap9(Q[3], Q[3+index]);
        Swap9(A[3], A[3+index]);
        ScaleVector9(Q[3], Q[3],(double)(1.f/Math.sqrt(ss)));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis4(double[][] Q,double[][] A)
    {
		int index=0;
		double ss=0;
		for(int i=3;i>=0;i--){
	        AccumulateProjection9(Q[i+4], Q[3], A[i+4]);
			double t=SumSquares9(Q[i+4]);
			if(t>ss){
				ss=t;
				index=i;
			}
		}
		if(ss==0){
			return false;
		}
        
        Swap9(Q[4],Q[4+index]);
        Swap9(A[4],A[4+index]);
        ScaleVector9(Q[4], Q[4],(double)(1.f/Math.sqrt(ss)));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis5(double[][] Q,double[][] A) {        
		int index=0;
		double ss=0;
		for(int i=2;i>=0;i--){
	        AccumulateProjection9(Q[i+5], Q[4], A[i+5]);
			double t=SumSquares9(Q[i+5]);
			if(t>ss){
				ss=t;
				index=i;
			}
		}
		if(ss==0){
			return false;
		}        

        
        Swap9(Q[5], Q[5+index]);
        Swap9(A[5], A[5+index]);
        ScaleVector9(Q[5], Q[5],(double)(1.f/Math.sqrt(ss)));
        
        return true;
    }
    
    private static boolean OrthogonalizePivot8x9Basis6(double[][] Q,double[][] A) {
        
        int index=0;
        AccumulateProjection9(Q[6], Q[5], A[6]);
        double ss=SumSquares9(Q[6]);
        AccumulateProjection9(Q[7], Q[5], A[7]);
        double s2=SumSquares9(Q[7]);
        if(ss<s2){
        	ss=s2;
        	index=1;
        }
        
        Swap9(Q[6],Q[6+index]);
        Swap9(A[6],A[6+index]);
        ScaleVector9(Q[6], Q[6],(double)(1.f/Math.sqrt(ss)));
        
        return true;
    }
    
    private static boolean OrthogonalizePivot8x9Basis7(double[][] Q,double[][] A) {
        AccumulateProjection9(Q[7], Q[6], A[7]);        
        double ss = SumSquares9(Q[7]);
        if(ss == 0) {
            return false;
        }
        ScaleVector9(Q[7],Q[7], (double)(1.f/Math.sqrt(ss)));
        return true;
    }
//    float OrthogonalizeIdentity8x9(float x[9], const T Q[72], int i) {
    static double OrthogonalizeIdentity8x9(double[] x,double[][] Q, int i) {
        ScaleVector9(x,Q[0], -Q[0][i]);
        x[i] = 1+x[i];
        
        AccumulateScaledVector9(x,Q[1], -Q[1][i]);
        AccumulateScaledVector9(x,Q[2], -Q[2][i]);
        AccumulateScaledVector9(x,Q[3], -Q[3][i]);
        AccumulateScaledVector9(x,Q[4], -Q[4][i]);
        AccumulateScaledVector9(x,Q[5], -Q[5][i]);
        AccumulateScaledVector9(x,Q[6], -Q[6][i]);
        AccumulateScaledVector9(x,Q[7], -Q[7][i]);
        
        double ss = SumSquares9(x);
        if(ss == 0) {
            return 0;
        }
        
        double w = (double) Math.sqrt(ss);
        ScaleVector9(x, x, 1.f/w);
        
        return w;
    }    
    
    
//    boolean OrthogonalizeIdentity8x9(T x[9], const T Q[72]) {
    static boolean OrthogonalizeIdentity8x9(NyARDoubleMatrix33 x,double[][] Q)
    {
    	double[] w=new double[9];
    	double[][] X=new double[9][9];
        
        w[0] = OrthogonalizeIdentity8x9(X[0],Q, 0);
        w[1] = OrthogonalizeIdentity8x9(X[1],Q, 1);
        w[2] = OrthogonalizeIdentity8x9(X[2],Q, 2);
        w[3] = OrthogonalizeIdentity8x9(X[3],Q, 3);
        w[4] = OrthogonalizeIdentity8x9(X[4],Q, 4);
        w[5] = OrthogonalizeIdentity8x9(X[5],Q, 5);
        w[6] = OrthogonalizeIdentity8x9(X[6],Q, 6);
        w[7] = OrthogonalizeIdentity8x9(X[7],Q, 7);
        w[8] = OrthogonalizeIdentity8x9(X[8],Q, 8);
        
        int index = MaxIndex(w,9);
        if(w[index] == 0) {
            return false;
        }
        x.m00=X[index][0];
        x.m01=X[index][1];
        x.m02=X[index][2];
        x.m10=X[index][3];
        x.m11=X[index][4];
        x.m12=X[index][5];
        x.m20=X[index][6];
        x.m21=X[index][7];
        x.m22=X[index][8];

        return true;
    }    
    /**
     * Solve for the null vector x of an 8x9 matrix A such A*x=0. The matrix
     * A is destroyed in the process. This system is solved using QR 
     * decomposition with Gram-Schmidt.
     */
    public static boolean SolveNullVector8x9Destructive(NyARDoubleMatrix33 x,double[][] A) {
    	double[][] Q=new double[8][9];
        
        if(!OrthogonalizePivot8x9Basis0(Q, A)) return false;
        if(!OrthogonalizePivot8x9Basis1(Q, A)) return false;
        if(!OrthogonalizePivot8x9Basis2(Q, A)) return false;
        if(!OrthogonalizePivot8x9Basis3(Q, A)) return false;
        if(!OrthogonalizePivot8x9Basis4(Q, A)) return false;
        if(!OrthogonalizePivot8x9Basis5(Q, A)) return false;
        if(!OrthogonalizePivot8x9Basis6(Q, A)) return false;
        if(!OrthogonalizePivot8x9Basis7(Q, A)) return false;
        
        return OrthogonalizeIdentity8x9(x, Q);
    }
}
