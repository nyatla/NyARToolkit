/* 
 * PROJECT: NyARToolkit Professional
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2012 Ryo Iizuka
 * wm@nyatla.jp
 * http://nyatla.jp
 * 
 * This work is based on the ARToolKit4.
 * Copyright 2010-2011 ARToolworks, Inc. All rights reserved.
 *
 */
package jp.nyatla.nyartoolkit.core.icp.base;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class NyARIcpUtils_Base
{
	public static boolean icpGetU_from_X_by_MatX2U(NyARDoublePoint2d u,NyARDoubleMatrix44 matX2U,NyARDoublePoint3d coord3d)
	{
	    double hx = matX2U.m00 * coord3d.x + matX2U.m01 * coord3d.y+ matX2U.m02 * coord3d.z + matX2U.m03;
	    double hy = matX2U.m10 * coord3d.x + matX2U.m11 * coord3d.y+ matX2U.m12 * coord3d.z + matX2U.m13;
	    double h  = matX2U.m20 * coord3d.x + matX2U.m21 * coord3d.y+ matX2U.m22 * coord3d.z + matX2U.m23;

	    if( h == 0.0 ){
	    	return false;
	    }
	    u.x = hx / h;
	    u.y = hy / h;
	    return true;
	}
	/**
	 * @param S
	 * double[6]
	 * @param J_U_S
	 * double[][6]
	 * @return
	 * @throws NyARException 
	 */
	public static boolean icpGetDeltaS(NyARMat matS, double[] dU, double[][] J_U_S, int n ) throws NyARException
	{
//	    ARMat   matS, matU, matJ;
//	    ARMat  *matJt, *matJtJ, *matJtU;
	    //matS.row = 6;
	    //matS.clm = 1;
	    //matS.m   = S;

	    double[][] t2=new double[n][1];
	    for(int i=0;i<n;i++){
	    	t2[i][0]=dU[i];
	    }
	    NyARMat matU=new NyARMat(n,1,t2,false);
	    //matU.row = n;
	    //matU.clm = 1;
	    //matU.m   = dU;

	    //J_US[][][]->JUS[][]へ変換
//	    double[][] t3={J_U_S[0]};
	    NyARMat matJ=new NyARMat(n,6,J_U_S,false);
	    //matJ.row = n;
	    //matJ.clm = 6;
	    //matJ.m   = &J_U_S[0][0];

	    NyARMat matJt = new NyARMat(6,n);
	    matJt.transpose(matJ);
//	    arMatrixAllocTrans( &matJ );
//	    if( matJt == NULL ) return -1;

	    NyARMat matJtJ = new NyARMat(6,6);
	    matJtJ.mul(matJt, matJ);


	    NyARMat matJtU =new NyARMat(6,1);
	    matJtU.mul(matJt,matU);
//	    arMatrixAllocMul( matJt, &matU );

	    if(!matJtJ.inverse()) {
	    	return false;
	    }
	    matS.mul(matJtJ, matJtU);
	    return true;
	}
	/**
	 * 
	 * @param J_U_S
	 * double[2][6]
	 * @return
	 */
	public static boolean icpGetJ_U_S(double[][] J_U_S,int i_idx,NyARDoubleMatrix44 matXc2U, NyARDoubleMatrix44 matXw2Xc,NyARDoublePoint3d worldCoord)
	{
	    double[][] J_Xc_S=new double[3][6];
	    double[][] J_U_Xc=new double[2][3];
	    NyARDoublePoint3d   Xc=new NyARDoublePoint3d();

	    if(!icpGetJ_Xc_S( J_Xc_S, Xc, matXw2Xc, worldCoord )) {
	        return false;
	    }
	    if(!icpGetJ_U_Xc( J_U_Xc, matXc2U,Xc )) {
	        return false;
	    }
	    //6*(n*2)行列へ変換
	    for(int j = 0; j < 2; j++ ){
	        for(int  i = 0; i < 6; i++ ){
	            J_U_S[i_idx+j][i] = 0.0;
	            for(int k = 0; k < 3; k++ ){
	                J_U_S[i_idx+j][i] += J_U_Xc[j][k] * J_Xc_S[k][i];
	            }
	        }
	    }
	    return true;
	}
	/**
	 * @param J_U_Xc
	 * double [2][3]
	 * @return
	 */
	private static boolean icpGetJ_U_Xc(double[][] J_U_Xc, NyARDoubleMatrix44 matXc2U,NyARDoublePoint3d cameraCoord)
	{
	    double w1, w2, w3, w3_w3;

	    w1 = matXc2U.m00 * cameraCoord.x + matXc2U.m01 * cameraCoord.y + matXc2U.m02 * cameraCoord.z + matXc2U.m03;
	    w2 = matXc2U.m10 * cameraCoord.x + matXc2U.m11 * cameraCoord.y + matXc2U.m12 * cameraCoord.z + matXc2U.m13;
	    w3 = matXc2U.m20 * cameraCoord.x + matXc2U.m21 * cameraCoord.y + matXc2U.m22 * cameraCoord.z + matXc2U.m23;

	    if( w3 == 0.0 ){
	    	return false;
	    }

	    w3_w3 = w3 * w3;
	    J_U_Xc[0][0] = (matXc2U.m00 * w3 - matXc2U.m20 * w1) / w3_w3;
	    J_U_Xc[0][1] = (matXc2U.m01 * w3 - matXc2U.m21 * w1) / w3_w3;
	    J_U_Xc[0][2] = (matXc2U.m02 * w3 - matXc2U.m22 * w1) / w3_w3;
	    J_U_Xc[1][0] = (matXc2U.m10 * w3 - matXc2U.m20 * w2) / w3_w3;
	    J_U_Xc[1][1] = (matXc2U.m11 * w3 - matXc2U.m21 * w2) / w3_w3;
	    J_U_Xc[1][2] = (matXc2U.m12 * w3 - matXc2U.m22 * w2) / w3_w3;

	    return true;
	}

	private static boolean icpGetJ_Xc_S(double[][] J_Xc_S, NyARDoublePoint3d cameraCoord, NyARDoubleMatrix44 T0, NyARDoublePoint3d worldCoord)
	{
	    double[][] J_Xc_T=new double[3][12];
	    int      i, j, k;

	    cameraCoord.x = T0.m00*worldCoord.x + T0.m01*worldCoord.y + T0.m02*worldCoord.z + T0.m03;
	    cameraCoord.y = T0.m10*worldCoord.x + T0.m11*worldCoord.y + T0.m12*worldCoord.z + T0.m13;
	    cameraCoord.z = T0.m20*worldCoord.x + T0.m21*worldCoord.y + T0.m22*worldCoord.z + T0.m23;

	    J_Xc_T[0][0] = T0.m00 * worldCoord.x;
	    J_Xc_T[0][1] = T0.m00 * worldCoord.y;
	    J_Xc_T[0][2] = T0.m00 * worldCoord.z;
	    J_Xc_T[0][3] = T0.m01 * worldCoord.x;
	    J_Xc_T[0][4] = T0.m01 * worldCoord.y;
	    J_Xc_T[0][5] = T0.m01 * worldCoord.z;
	    J_Xc_T[0][6] = T0.m02 * worldCoord.x;
	    J_Xc_T[0][7] = T0.m02 * worldCoord.y;
	    J_Xc_T[0][8] = T0.m02 * worldCoord.z;
	    J_Xc_T[0][9] = T0.m00;
	    J_Xc_T[0][10] = T0.m01;
	    J_Xc_T[0][11] = T0.m02;

	    J_Xc_T[1][0] = T0.m10 * worldCoord.x;
	    J_Xc_T[1][1] = T0.m10 * worldCoord.y;
	    J_Xc_T[1][2] = T0.m10 * worldCoord.z;
	    J_Xc_T[1][3] = T0.m11 * worldCoord.x;
	    J_Xc_T[1][4] = T0.m11 * worldCoord.y;
	    J_Xc_T[1][5] = T0.m11 * worldCoord.z;
	    J_Xc_T[1][6] = T0.m12 * worldCoord.x;
	    J_Xc_T[1][7] = T0.m12 * worldCoord.y;
	    J_Xc_T[1][8] = T0.m12 * worldCoord.z;
	    J_Xc_T[1][9] = T0.m10;
	    J_Xc_T[1][10] = T0.m11;
	    J_Xc_T[1][11] = T0.m12;

	    J_Xc_T[2][0] = T0.m20 * worldCoord.x;
	    J_Xc_T[2][1] = T0.m20 * worldCoord.y;
	    J_Xc_T[2][2] = T0.m20 * worldCoord.z;
	    J_Xc_T[2][3] = T0.m21 * worldCoord.x;
	    J_Xc_T[2][4] = T0.m21 * worldCoord.y;
	    J_Xc_T[2][5] = T0.m21 * worldCoord.z;
	    J_Xc_T[2][6] = T0.m22 * worldCoord.x;
	    J_Xc_T[2][7] = T0.m22 * worldCoord.y;
	    J_Xc_T[2][8] = T0.m22 * worldCoord.z;
	    J_Xc_T[2][9] = T0.m20;
	    J_Xc_T[2][10] = T0.m21;
	    J_Xc_T[2][11] = T0.m22;

	    for( j = 0; j < 3; j++ ) {
	        for( i = 0; i < 6; i++ ) {
	            J_Xc_S[j][i] = 0.0;
	            for( k = 0; k < 12; k++ ) {
	                J_Xc_S[j][i] += J_Xc_T[j][k] * J_T_S[k][i];
	            }
	        }
	    }
	    return true;
	}		
	private static double[][] J_T_S={
			{ 0, 0, 0, 0, 0, 0},//0
			{ 0, 0,-1, 0, 0, 0},//1
			{ 0, 1, 0, 0, 0, 0},//2
			{ 0, 0, 1, 0, 0, 0},//3
			{ 0, 0, 0, 0, 0, 0},//4
			{-1, 0, 0, 0, 0, 0},//5
			{ 0,-1, 0, 0, 0, 0},//6
			{ 1, 0, 0, 0, 0, 0},//7
			{ 0, 0, 0, 0, 0, 0},//8
			{ 0, 0, 0, 1, 0, 0},//9
			{ 0, 0, 0, 0, 1, 0},//10
			{ 0, 0, 0, 0, 0, 1} //11
	};
	private static int icpGetXc_from_Xw_by_MatXw2Xc(NyARDoublePoint3d Xc, NyARDoubleMatrix44 matXw2Xc, NyARDoublePoint3d Xw)
	{
	    Xc.x = matXw2Xc.m00 * Xw.x + matXw2Xc.m01 * Xw.y + matXw2Xc.m02 * Xw.z + matXw2Xc.m03;
	    Xc.y = matXw2Xc.m10 * Xw.x + matXw2Xc.m11 * Xw.y + matXw2Xc.m12 * Xw.z + matXw2Xc.m13;
	    Xc.z = matXw2Xc.m20 * Xw.x + matXw2Xc.m21 * Xw.y + matXw2Xc.m22 * Xw.z + matXw2Xc.m23;

	    return 0;
	}
	/**
	 * 
	 * @param matXw2Xc
	 * @param dS
	 * double[6]
	 * @return
	 */
	public static void icpUpdateMat(NyARDoubleMatrix44 matXw2Xc,NyARMat dS)
	{
	    double[] q=new double[7];
	    NyARDoubleMatrix44 mat=new NyARDoubleMatrix44();
	    NyARDoubleMatrix44 mat2=new NyARDoubleMatrix44();
	    icpGetQ_from_S(q, dS);
	    icpGetMat_from_Q(mat,q);
	    
	    //ji
        mat2.m00 = matXw2Xc.m00 * mat.m00+ matXw2Xc.m01 * mat.m10+ matXw2Xc.m02 * mat.m20;
        mat2.m01 = matXw2Xc.m00 * mat.m01+ matXw2Xc.m01 * mat.m11+ matXw2Xc.m02 * mat.m21;
        mat2.m02 = matXw2Xc.m00 * mat.m02+ matXw2Xc.m01 * mat.m12+ matXw2Xc.m02 * mat.m22;
        mat2.m03 = matXw2Xc.m00 * mat.m03+ matXw2Xc.m01 * mat.m13+ matXw2Xc.m02 * mat.m23+matXw2Xc.m03;

        mat2.m10 = matXw2Xc.m10 * mat.m00+ matXw2Xc.m11 * mat.m10+ matXw2Xc.m12 * mat.m20;
        mat2.m11 = matXw2Xc.m10 * mat.m01+ matXw2Xc.m11 * mat.m11+ matXw2Xc.m12 * mat.m21;
        mat2.m12 = matXw2Xc.m10 * mat.m02+ matXw2Xc.m11 * mat.m12+ matXw2Xc.m12 * mat.m22;
        mat2.m13 = matXw2Xc.m10 * mat.m03+ matXw2Xc.m11 * mat.m13+ matXw2Xc.m12 * mat.m23+matXw2Xc.m13;

        mat2.m20 = matXw2Xc.m20 * mat.m00+ matXw2Xc.m21 * mat.m10+ matXw2Xc.m22 * mat.m20;
        mat2.m21 = matXw2Xc.m20 * mat.m01+ matXw2Xc.m21 * mat.m11+ matXw2Xc.m22 * mat.m21;
        mat2.m22 = matXw2Xc.m20 * mat.m02+ matXw2Xc.m21 * mat.m12+ matXw2Xc.m22 * mat.m22;
        mat2.m23 = matXw2Xc.m20 * mat.m03+ matXw2Xc.m21 * mat.m13+ matXw2Xc.m22 * mat.m23+matXw2Xc.m23;

        mat2.m30 = 0;
        mat2.m31 = 0;
        mat2.m32 = 0;
        mat2.m33 = 1;        
        matXw2Xc.setValue(mat2);
	    return;
	}
	/**
	 * @param q
	 * double[7]
	 * @param s
	 * double[6]
	 * @return
	 */
	static void icpGetQ_from_S(double[] q, NyARMat s)
	{
		double[][] mat=s.getArray();
	    double ra = mat[0][0]*mat[0][0] + mat[1][0]*mat[1][0] + mat[2][0]*mat[2][0];
	    if( ra == 0.0 ) {
	        q[0] = 1.0;
	        q[1] = 0.0;
	        q[2] = 0.0;
	        q[3] = 0.0;
	    }else{
	    	ra=Math.sqrt(ra);
	        q[0] = mat[0][0] / ra;
	        q[1] = mat[1][0] / ra;
	        q[2] = mat[2][0] / ra;
	        q[3] = ra;
	    }
	    q[4] = mat[3][0];
	    q[5] = mat[4][0];
	    q[6] = mat[5][0];
	    return;
	}
	final static class Q
	{
		public double q0;
		public double q1;
		public double q2;
		public double q3;
		public double q4;
		public double q5;
		public double q6;
	}
	
	static void icpGetMat_from_Q(NyARDoubleMatrix44 mat,double[] q)
	{
	    double cra, one_cra, sra;

	    cra = Math.cos(q[3]);
	    one_cra = 1.0 - cra;
	    sra = Math.sin(q[3]);

	    mat.m00 = q[0]*q[0]*one_cra + cra;
	    mat.m01 = q[0]*q[1]*one_cra - q[2]*sra;
	    mat.m02 = q[0]*q[2]*one_cra + q[1]*sra;
	    mat.m03 = q[4];
	    mat.m10 = q[1]*q[0]*one_cra + q[2]*sra;
	    mat.m11 = q[1]*q[1]*one_cra + cra;
	    mat.m12 = q[1]*q[2]*one_cra - q[0]*sra;
	    mat.m13 = q[5];
	    mat.m20 = q[2]*q[0]*one_cra - q[1]*sra;
	    mat.m21 = q[2]*q[1]*one_cra + q[0]*sra;
	    mat.m22 = q[2]*q[2]*one_cra + cra;
	    mat.m23 = q[6];

	    return;
	}		
}
