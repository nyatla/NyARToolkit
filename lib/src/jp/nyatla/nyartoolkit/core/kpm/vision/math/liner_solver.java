package jp.nyatla.nyartoolkit.core.kpm.vision.math;

import jp.nyatla.nyartoolkit.core.kpm.Utils;
import jp.nyatla.nyartoolkit.core.kpm.vision.match.indexing;

public class liner_solver {

    static float DotProduct9(float[] a,float[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2] + a[3]*b[3] + a[4]*b[4] + a[5]*b[5] + a[6]*b[6] + a[7]*b[7] + a[8]*b[8];
    }
    static float DotProduct9(float[] a,int a_ptr,float[] b,int b_ptr) {
        return a[a_ptr+0]*b[0+b_ptr] + a[a_ptr+1]*b[1+b_ptr] + a[a_ptr+2]*b[2+b_ptr] + a[a_ptr+3]*b[3+b_ptr] + a[a_ptr+4]*b[4+b_ptr] + a[a_ptr+5]*b[5+b_ptr] + a[a_ptr+6]*b[6+b_ptr] + a[a_ptr+7]*b[7+b_ptr] + a[a_ptr+8]*b[8+b_ptr];
    }
    
    /**
     * Sum sqaured.
     */
    static float SumSquares9(float[] x) {
        return DotProduct9(x, x);
    }
    static float SumSquares9(float[] x,int x_i) {
        return DotProduct9(x,x_i, x,x_i);
    }
    
    static int MaxIndex(float[] x,int len) {
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
    static void AccumulateScaledVector9(float[] dst,int i_dst,float[] src,int i_src, float s)
    {
        dst[0+i_dst] += src[0+i_src]*s;
        dst[1+i_dst] += src[1+i_src]*s;
        dst[2+i_dst] += src[2+i_src]*s;
        dst[3+i_dst] += src[3+i_src]*s;
        dst[4+i_dst] += src[4+i_src]*s;
        dst[5+i_dst] += src[5+i_src]*s;
        dst[6+i_dst] += src[6+i_src]*s;
        dst[7+i_dst] += src[7+i_src]*s;
        dst[8+i_dst] += src[8+i_src]*s;
        return;
    }
    /**
     * Project a vector "a" onto a normalized basis vector "e".
     *
     * x = x - dot(a,e)*e
     */
static //    void AccumulateProjection9(T x[9], const T e[9], const T a[9]) {
    void AccumulateProjection9(float[] x,int xi, float[] e,int ei,float[] a,int ai) {
        float d = DotProduct9(Utils.arraysubset(a,ai,9),Utils.arraysubset(e,ei,9));
        x[xi+0] -= d*e[ei+0];
        x[xi+1] -= d*e[ei+1];
        x[xi+2] -= d*e[ei+2];
        x[xi+3] -= d*e[ei+3];
        x[xi+4] -= d*e[ei+4];
        x[xi+5] -= d*e[ei+5];
        x[xi+6] -= d*e[ei+6];
        x[xi+7] -= d*e[ei+7];
        x[xi+8] -= d*e[ei+8];
    }   
    
    /**
     * Swap the contents of two vectors.
     */
    static void Swap9(float[] a,int a_ptr, float[] b,int b_ptr) {
        float tmp;
        for(int i=0;i<9;i++){
        	tmp = a[i+a_ptr]; a[i+a_ptr] = b[i+b_ptr]; b[i+b_ptr]= tmp;
        }
    }
    static void ScaleVector9(float[] dst,int i_dst_ptr,float[] src,int src_ptr, float s) {
        dst[i_dst_ptr+0] = src[src_ptr+0]*s;
        dst[i_dst_ptr+1] = src[src_ptr+1]*s;
        dst[i_dst_ptr+2] = src[src_ptr+2]*s;
        dst[i_dst_ptr+3] = src[src_ptr+3]*s;
        dst[i_dst_ptr+4] = src[src_ptr+4]*s;
        dst[i_dst_ptr+5] = src[src_ptr+5]*s;
        dst[i_dst_ptr+6] = src[src_ptr+6]*s;
        dst[i_dst_ptr+7] = src[src_ptr+7]*s;
        dst[i_dst_ptr+8] = src[src_ptr+8]*s;
    }    
    /**
     * \defgroup Project the rows of A onto the current basis set to identity a new orthogonal vector.
     * @{
     */
static //    boolean OrthogonalizePivot8x9Basis0(T Q[8*9], T A[8*9]) {  
    boolean OrthogonalizePivot8x9Basis0(float[] Q, float[] A)
    {
        float[] ss=new float[8];
        ss[0] = SumSquares9(Utils.arraysubset(A,0,9));
        ss[1] = SumSquares9(Utils.arraysubset(A,9,9));
        ss[2] = SumSquares9(Utils.arraysubset(A,18,9));
        ss[3] = SumSquares9(Utils.arraysubset(A,27,9));
        ss[4] = SumSquares9(Utils.arraysubset(A,36,9));
        ss[5] = SumSquares9(Utils.arraysubset(A,45,9));
        ss[6] = SumSquares9(Utils.arraysubset(A,54,9));
        ss[7] = SumSquares9(Utils.arraysubset(A,63,9));
        
//        int index = MaxIndex8(ss);
        int index = MaxIndex(ss,8);
        if(ss[index] == 0) {
            return false;
        }
        
        Swap9(A,0,A,index*9);
        ScaleVector9(Q, 0,A,0, (float)(1.f/Math.sqrt(ss[index])));
        indexing.CopyVector(Q,9, A,9, 63);
        
        return true;
    }
//    boolean OrthogonalizePivot8x9Basis1(T Q[8*9], T A[8*9]) {
    static boolean OrthogonalizePivot8x9Basis1(float[] Q, float[] A) {
        AccumulateProjection9(Q,9,  Q,0, A,9);
        AccumulateProjection9(Q,18, Q,0, A,18);
        AccumulateProjection9(Q,27, Q,0, A,27);
        AccumulateProjection9(Q,36, Q,0, A,36);
        AccumulateProjection9(Q,45, Q,0, A,45);
        AccumulateProjection9(Q,54, Q,0, A,54);
        AccumulateProjection9(Q,63, Q,0, A,63);

        float[] ss=new float[7];
        ss[0] = SumSquares9(Utils.arraysubset(Q,9,9));
        ss[1] = SumSquares9(Utils.arraysubset(Q,18,9));
        ss[2] = SumSquares9(Utils.arraysubset(Q,27,9));
        ss[3] = SumSquares9(Utils.arraysubset(Q,36,9));
        ss[4] = SumSquares9(Utils.arraysubset(Q,45,9));
        ss[5] = SumSquares9(Utils.arraysubset(Q,54,9));
        ss[6] = SumSquares9(Utils.arraysubset(Q,63,9));
        
//        int index = MaxIndex7(ss);
        int index = MaxIndex(ss,7);
        if(ss[index] == 0) {
            return false;
        }
        
        Swap9(Q,9, Q,9+index*9);
        Swap9(A,9, A,9+index*9);
        ScaleVector9(Q,9, Q,9, (float) (1.f/Math.sqrt(ss[index])));
        
        return true;
    }
//    boolean OrthogonalizePivot8x9Basis2(T Q[8*9], T A[8*9]) {  
    static boolean OrthogonalizePivot8x9Basis2(float[] Q, float[] A) {
        AccumulateProjection9(Q,18, Q,9, A,18);
        AccumulateProjection9(Q,27, Q,9, A,27);
        AccumulateProjection9(Q,36, Q,9, A,36);
        AccumulateProjection9(Q,45, Q,9, A,45);
        AccumulateProjection9(Q,54, Q,9, A,54);
        AccumulateProjection9(Q,63, Q,9, A,63);
        
        float[] ss=new float[6];
        ss[0] = SumSquares9(Utils.arraysubset(Q,18,9));
        ss[1] = SumSquares9(Utils.arraysubset(Q,27,9));
        ss[2] = SumSquares9(Utils.arraysubset(Q,36,9));
        ss[3] = SumSquares9(Utils.arraysubset(Q,45,9));
        ss[4] = SumSquares9(Utils.arraysubset(Q,54,9));
        ss[5] = SumSquares9(Utils.arraysubset(Q,63,9));
        
        int index = MaxIndex(ss,6);
        if(ss[index] == 0) {
            return false;
        }
        Swap9(Q,18, Q,18+index*9);
        Swap9(A,18, A,18+index*9);
        ScaleVector9(Q,18, Q,18, (float)(1.f/Math.sqrt(ss[index])));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis3(float[] Q,float[] A) {
        AccumulateProjection9(Q,27, Q,18, A,27);
        AccumulateProjection9(Q,36, Q,18, A,36);
        AccumulateProjection9(Q,45, Q,18, A,45);
        AccumulateProjection9(Q,54, Q,18, A,54);
        AccumulateProjection9(Q,63, Q,18, A,63);
        
        float[] ss=new float[5];
        ss[0] = SumSquares9(Utils.arraysubset(Q,27,9));
        ss[1] = SumSquares9(Utils.arraysubset(Q,36,9));
        ss[2] = SumSquares9(Utils.arraysubset(Q,45,9));
        ss[3] = SumSquares9(Utils.arraysubset(Q,54,9));
        ss[4] = SumSquares9(Utils.arraysubset(Q,63,9));
        
        int index = MaxIndex(ss,5);
        if(ss[index] == 0) {
            return false;
        }
        
        Swap9(Q,27, Q,27+index*9);
        Swap9(A,27, A,27+index*9);
        ScaleVector9(Q,27, Q,27,(float)(1.f/Math.sqrt(ss[index])));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis4(float[] Q,float[] A) {
        AccumulateProjection9(Q,36, Q,27, A,36);
        AccumulateProjection9(Q,45, Q,27, A,45);
        AccumulateProjection9(Q,54, Q,27, A,54);
        AccumulateProjection9(Q,63, Q,27, A,63);
        
        float[] ss=new float[4];
        ss[0] = SumSquares9(Utils.arraysubset(Q,36,9));
        ss[1] = SumSquares9(Utils.arraysubset(Q,45,9));
        ss[2] = SumSquares9(Utils.arraysubset(Q,54,9));
        ss[3] = SumSquares9(Utils.arraysubset(Q,63,9));
        
        int index = MaxIndex(ss,4);
        if(ss[index] == 0){
            return false;
        }
        
        Swap9(Q,36, Q,36+index*9);
        Swap9(A,36, A,36+index*9);
        ScaleVector9(Q,36, Q,36,(float)(1.f/Math.sqrt(ss[index])));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis5(float[] Q,float[] A) {
        AccumulateProjection9(Q,45, Q,36, A,45);
        AccumulateProjection9(Q,54, Q,36, A,54);
        AccumulateProjection9(Q,63, Q,36, A,63);
        
        float[] ss=new float[3];
        ss[0] = SumSquares9(Utils.arraysubset(Q,45,9));
        ss[1] = SumSquares9(Utils.arraysubset(Q,54,9));
        ss[2] = SumSquares9(Utils.arraysubset(Q,63,9));
        
        int index = MaxIndex(ss,3);
        if(ss[index] == 0) {
            return false;
        }
        
        Swap9(Q,45, Q,45+index*9);
        Swap9(A,45, A,45+index*9);
        ScaleVector9(Q,45, Q,45,(float)(1.f/Math.sqrt(ss[index])));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis6(float[] Q,float[] A) {
        AccumulateProjection9(Q,54, Q,45, A,54);
        AccumulateProjection9(Q,63, Q,45, A,63);
        
        float[] ss=new float[2];
        ss[0] = SumSquares9(Utils.arraysubset(Q,54,9));
        ss[1] = SumSquares9(Utils.arraysubset(Q,63,9));
        
        int index = MaxIndex(ss,2);
        if(ss[index] == 0) {
            return false;
        }
        
        Swap9(Q,54, Q,54+index*9);
        Swap9(A,54, A,54+index*9);
        ScaleVector9(Q,54, Q,54,(float)(1.f/Math.sqrt(ss[index])));
        
        return true;
    }
    
    static boolean OrthogonalizePivot8x9Basis7(float[] Q,float[] A) {
        AccumulateProjection9(Q,63, Q,54, A,63);
        
        float ss = SumSquares9(Utils.arraysubset(Q,63,9));
        if(ss == 0) {
            return false;
        }
        
        Swap9(Q,63, Q,63);
        Swap9(A,63, A,63);
        ScaleVector9(Q,63, Q,63, (float)(1.f/Math.sqrt(ss)));
        
        return true;
    }
//    float OrthogonalizeIdentity8x9(float x[9], const T Q[72], int i) {
    static float OrthogonalizeIdentity8x9(float[] x,int x_ptr,float[] Q, int i) {
        ScaleVector9(x,x_ptr, Q,0, -Q[i]);
        x[i+x_ptr] = 1+x[i+x_ptr];
        
        AccumulateScaledVector9(x,x_ptr, Q,9,  -Q[9 +i]);
        AccumulateScaledVector9(x,x_ptr, Q,18, -Q[18+i]);
        AccumulateScaledVector9(x,x_ptr, Q,27, -Q[27+i]);
        AccumulateScaledVector9(x,x_ptr, Q,36, -Q[36+i]);
        AccumulateScaledVector9(x,x_ptr, Q,45, -Q[45+i]);
        AccumulateScaledVector9(x,x_ptr, Q,54, -Q[54+i]);
        AccumulateScaledVector9(x,x_ptr, Q,63, -Q[63+i]);
        
        float ss = SumSquares9(x,x_ptr);
        if(ss == 0) {
            return 0;
        }
        
        float w = (float) Math.sqrt(ss);
        ScaleVector9(x,x_ptr, x,x_ptr, 1.f/w);
        
        return w;
    }    
    
    
//    boolean OrthogonalizeIdentity8x9(T x[9], const T Q[72]) {
    static boolean OrthogonalizeIdentity8x9(float[] x,float[] Q)
    {
        float[] w=new float[9];
        float[] X=new float[9*9];
        
        w[0] = OrthogonalizeIdentity8x9(X,0,    Q, 0);
        w[1] = OrthogonalizeIdentity8x9(X,9,  Q, 1);
        w[2] = OrthogonalizeIdentity8x9(X,18, Q, 2);
        w[3] = OrthogonalizeIdentity8x9(X,27, Q, 3);
        w[4] = OrthogonalizeIdentity8x9(X,36, Q, 4);
        w[5] = OrthogonalizeIdentity8x9(X,45, Q, 5);
        w[6] = OrthogonalizeIdentity8x9(X,54, Q, 6);
        w[7] = OrthogonalizeIdentity8x9(X,63, Q, 7);
        w[8] = OrthogonalizeIdentity8x9(X,72, Q, 8);
        
        int index = MaxIndex(w,9);
        if(w[index] == 0) {
            return false;
        }
        
        indexing.CopyVector(x,0, X,index*9,9);
        
        return true;
    }    
    /**
     * Solve for the null vector x of an 8x9 matrix A such A*x=0. The matrix
     * A is destroyed in the process. This system is solved using QR 
     * decomposition with Gram-Schmidt.
     */
    public static boolean SolveNullVector8x9Destructive(float[] x,float[] A) {
        float[] Q=new float[72];
        
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
