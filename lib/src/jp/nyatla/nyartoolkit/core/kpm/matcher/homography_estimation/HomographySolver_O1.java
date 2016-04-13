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

import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

/**
 * Construct the homography constraint matrix from 4 point correspondences.
 * copy from Homography4PointsInhomogeneousConstraint.
 */
final public class HomographySolver_O1 extends HomographySolver
{
	private double[][] _mat_A=new double[8][9];
	private boolean solveHomography4PointsInhomogenous(NyARDoubleMatrix33 i_homography_mat)
	{
		//SolveHomography4PointsInhomogenous
		if (!this.solveNullVector8x9Destructive(i_homography_mat,this._mat_A)) {
			return false;
		}
		if (Math.abs(i_homography_mat.determinant()) < 1e-5) {
			return false;
		}
		return true;
	}


	private final NyARDoublePoint2d[] _a1=new NyARDoublePoint2d[4];
	private final NyARDoublePoint2d[] _a2=new NyARDoublePoint2d[4];



	/**
	 * SolveHomography4Points
	 * Solve for the homography given 4 point correspondences.
	 */
	@Override
	public boolean solveHomography4Points(FeaturePairStack.Item p1, FeaturePairStack.Item p2, FeaturePairStack.Item p3, FeaturePairStack.Item p4,HomographyMat H)
	{

		NyARDoublePoint2d[] a1=this._a1;
		NyARDoublePoint2d[] a2=this._a2;
		a1[0]=p1.ref;a1[1]=p2.ref;a1[2]=p3.ref;a1[3]=p4.ref;
		a2[0]=p1.query;a2[1]=p2.query;a2[2]=p3.query;a2[3]=p4.query;
		double mus1_0=0;
		double mus1_1=0;
		double mus2_0=0;
		double mus2_1=0;
		double s1,s2;
		{
			/**
			 * Condition four 2D points such that the mean is zero and the standard
			 * deviation is sqrt(2).
			 * condition4Points2d+AddHomographyPointContraint function
			 */			
			//condition4Points2d(a1,a2,this._mat_A);
			for(int i=0;i<4;i++){
				mus1_0+=a1[i].x;
				mus1_1+=a1[i].y;
				mus2_0+=a2[i].x;
				mus2_1+=a2[i].y;
			}
			mus1_0/=4;
			mus1_1/=4;
			mus2_0/=4;
			mus2_1/=4;
			double dw1=0;
			double dw2=0;
			for(int i=0;i<4;i++){
				double[] l;
				double X1= a1[i].x - mus1_0;
				double Y1= a1[i].y - mus1_1;		
				double X2= a2[i].x - mus2_0;
				double Y2= a2[i].y - mus2_1;
				dw2+=Math.sqrt(X2 * X2 + Y2 * Y2);
				dw1+=Math.sqrt(X1 * X1 + Y1 * Y1);
				l=this._mat_A[i*2];
				l[0]=-X1;
				l[1]=-Y1;
				l[6]=X2*X1;
				l[7]=X2*Y1;
				l[8]=X2;
				l=this._mat_A[i*2+1];
				l[3]=-X1;
				l[4]=-Y1;
				l[6]=Y2*X1;
				l[7]=Y2*Y1;
				l[8]=Y2;			
			}
			if(dw1*dw2==0){
				return false;
			}
			s1=(4/dw1)* SQRT2;
			s2=(4/dw2)* SQRT2;
//			double ss=s1*s2;
			for(int i=0;i<4;i++){
				double[] l;
				l=this._mat_A[i*2];
				l[6]=(-l[0]*s1)*(l[8]*s2);//emulation calculation order.
				l[7]=(-l[1]*s1)*(l[8]*s2);//emulation calculation order.			
//				l[6]*=ss;
//				l[7]*=ss;
				l[0]*=s1;
				l[1]*=s1;
				l[2]=-1;
				l[3]=l[4]=l[5]=0;
				l[8]*=s2;
				l=this._mat_A[i*2+1];
				l[6]=(-l[3]*s1)*(l[8]*s2);//emulation calculation order.
				l[7]=(-l[4]*s1)*(l[8]*s2);//emulation calculation order.
//				l[6]*=ss;
//				l[7]*=ss;
				l[0]=l[1]=l[2]=0;
				l[3]*=s1;
				l[4]*=s1;
				l[5]=-1;
				l[8]*=s2;
			}
		}	
		
		if (!this.solveHomography4PointsInhomogenous(H))
		{
			return false;
		}


		//
		// Denomalize the computed homography
		//
		{	//H.denormalizeHomography(ts, tps);
	
	
			double stx = s1 * mus1_0;
			double sty = s1 * mus1_1;
	
			double apc = (H.m20 * mus2_0) + (H.m00 / s2);
			double bpd = (H.m21 * mus2_0) + (H.m01 / s2);
			H.m00 = s1 * apc;
			H.m01 = s1 * bpd;
			H.m02 = H.m22 * mus2_0 + H.m02 / s2 - stx * apc - sty * bpd;
	
			double epg = (H.m20 * mus2_1) + (H.m10 / s2);
			double fph = (H.m21 * mus2_1) + (H.m11 / s2);
			H.m10 = s1 * epg;
			H.m11 = s1 * fph;
			H.m12 = H.m22 * mus2_1 + H.m12 / s2 - stx * epg - sty * fph;
	
			H.m20 = H.m20 * s1;
			H.m21 = H.m21 * s1;
			H.m22 = H.m22 - H.m20 * mus1_0 - H.m21 * mus1_1;
		}
		
		
		
		return true;
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