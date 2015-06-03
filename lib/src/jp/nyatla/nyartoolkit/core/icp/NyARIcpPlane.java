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
package jp.nyatla.nyartoolkit.core.icp;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public class NyARIcpPlane
{
	protected NyARDoubleMatrix44 _cparam;

	public NyARIcpPlane(NyARParam i_param)
	{
		this._cparam = i_param.getPerspectiveProjectionMatrix();
	}
	private NyARRotVector __vec0 = new NyARRotVector();
	private NyARRotVector __vec1 = new NyARRotVector();
	
	private NyARMat __matAtA = new NyARMat(8, 8);
	private NyARMat __matAtB = new NyARMat(8, 1);
	private NyARMat __matC = new NyARMat(8, 1);
	private static NyARMat makeMatAtA(NyARDoublePoint2d[] screenCoord, NyARDoublePoint3d[] worldCoord,int i_num,NyARMat o_matAtA)
	{
		o_matAtA.loadZero();
		double[][] t=o_matAtA.getArray();
		for(int i=0;i<i_num;i++){
			//0
			double wx=worldCoord[i].x;
			double wy=worldCoord[i].y;
			double sx=screenCoord[i].x;
			double sy=screenCoord[i].y;
			double wxwx=wx*wx;
			double wywy=wy*wy;
			double wxwy=wx*wy;
			t[0][0]+=wxwx;
			t[0][1]+=wxwy;
			t[0][2]+=wx;
			t[1][2]+=wy;
			t[0][6]+=(-wxwx) * (sx);
			t[0][7]+=(-wxwy) * (sx);
			t[1][1]+=wywy;
			t[1][7]+=(-wywy) * (sx);
			t[2][6]+=(-wx) * (sx);
			t[2][7]+=(-wy) * (sx);
			t[3][6]+=(-wxwx) * (sy);
			t[3][7]+=(-wxwy) * (sy);
			t[4][7]+=(-wywy) * (sy);
			t[5][6]+=(-wx) * (sy);
			t[5][7]+=(-wy) * (sy);
			t[6][6]+=(wxwx) * (sx)* (sx) + (wxwx) * (sy)* (sy);
			t[6][7]+=(wxwy) * (sx)* (sx) + (wxwy) * (sy)* (sy);
			t[7][7]+=(wywy) * (sx)* (sx) + (wywy) * (sy) * (sy);			
		}
		t[1][0]=t[3][4]=t[4][3]=t[0][1];
		t[2][0]=t[3][5]=t[5][3]=t[0][2];
		t[2][1]=t[5][4]=t[4][5]=t[1][2];
		t[7][0]=t[6][1]=t[1][6]=t[0][7];
		t[4][6]=t[6][4]=t[7][3]=t[3][7];
		t[2][2]=t[5][5]=i_num;
		t[3][3]=t[0][0];
		t[4][4]=t[1][1];
		t[6][0]=t[0][6];
		t[6][2]=t[2][6];		
		t[6][3]=t[3][6];		
		t[6][5]=t[5][6];
		t[7][1]=t[1][7];
		t[7][2]=t[2][7];
		t[7][4]=t[4][7];
		t[7][5]=t[5][7];
		t[7][6]=t[6][7];
		//先頭でゼロクリアしない場合。
		//t[0][3]=t[0][4]=t[0][5]=t[1][3]=t[1][4]=t[1][5]=t[2][3]=t[2][4]=t[2][5]=t[3][0]=t[3][1]=t[3][2]=t[4][0]=t[4][1]=t[4][2]=t[5][0]=t[5][1]=t[5][2]=0;
		return o_matAtA;
	}
	private static NyARMat makeMatAtB(NyARDoublePoint2d[] screenCoord, NyARDoublePoint3d[] worldCoord,int i_num,NyARMat o_matAtB)
	{
		double v0,v1,v2,v3,v4,v5,v6,v7;
		v0=v1=v2=v3=v4=v5=v6=v7=0;
		for (int i = 0; i < i_num; i++) {
			double wx=worldCoord[i].x;
			double wy=worldCoord[i].y;
			double sx=screenCoord[i].x;
			double sy=screenCoord[i].y;			
			v0+=wx*sx;
			v1+=wy*sx;
			v2+=sx;
			v3+=wx*sy;
			v4+=wy*sy;
			v5+=sy;
			v6+=-wx*sx*sx-wx*sy*sy;
			v7+=-wy*sx*sx-wy*sy*sy;			
		}
		double[][] t=o_matAtB.getArray();
		t[0][0]=v0;
		t[1][0]=v1;
		t[2][0]=v2;
		t[3][0]=v3;
		t[4][0]=v4;
		t[5][0]=v5;
		t[6][0]=v6;
		t[7][0]=v7;
		return o_matAtB;
		
	}
	public boolean icpGetInitXw2Xc_from_PlanarData(
		NyARDoublePoint2d[] screenCoord, NyARDoublePoint3d[] worldCoord,
		int i_num, NyARDoubleMatrix44 initMatXw2Xc) throws NyARException
	{

		if (i_num < 4) {
			throw new NyARException();
		}
		// nを元に配列の準備
		
		NyARMat matAtA = this.__matAtA;
		NyARMat matAtB = this.__matAtB;
		NyARMat matC = this.__matC;
		makeMatAtA(screenCoord, worldCoord, i_num, matAtA);
		makeMatAtB(screenCoord, worldCoord, i_num, matAtB);

		if (!matAtA.inverse()) {
			return false;
		}

		matC.mul(matAtA, matAtB);
		double[][] bufc = matC.getArray();
		double t0, t1, t2;

		NyARRotVector vec0 =this.__vec0;
		NyARRotVector vec1 =this.__vec1;
		NyARDoubleMatrix44 matxc = this._cparam;

		vec0.v3 = (bufc[6][0]);
		vec0.v2 = (bufc[3][0] - matxc.m12 * vec0.v3) / matxc.m11;
		vec0.v1 = (bufc[0][0] - matxc.m02 * vec0.v3 - matxc.m01 * vec0.v2)/ matxc.m00;
		vec1.v3 = (bufc[7][0]);
		vec1.v2 = (bufc[4][0] - matxc.m12 * vec1.v3) / matxc.m11;
		vec1.v1 = (bufc[1][0] - matxc.m02 * vec1.v3 - matxc.m01 * vec1.v2)/ matxc.m00;
		t2 = 1.0;
		t1 = (bufc[5][0] - matxc.m12 * t2) / matxc.m11;
		t0 = (bufc[2][0] - matxc.m02 * t2 - matxc.m01 * t1) / matxc.m00;

		double l1 = Math.sqrt(vec0.v1 * vec0.v1 + vec0.v2 * vec0.v2 + vec0.v3* vec0.v3);
		double l2 = Math.sqrt(vec1.v1 * vec1.v1 + vec1.v2 * vec1.v2 + vec1.v3* vec1.v3);
		vec0.v1 /= l1;
		vec0.v2 /= l1;
		vec0.v3 /= l1;
		vec1.v1 /= l2;
		vec1.v2 /= l2;
		vec1.v3 /= l2;
		t0 /= (l1 + l2) / 2.0;
		t1 /= (l1 + l2) / 2.0;
		t2 /= (l1 + l2) / 2.0;
		if (t2 < 0.0) {
			vec0.v1 = -vec0.v1;
			vec0.v2 = -vec0.v2;
			vec0.v3 = -vec0.v3;
			vec1.v1 = -vec1.v1;
			vec1.v2 = -vec1.v2;
			vec1.v3 = -vec1.v3;
			t0 = -t0;
			t1 = -t1;
			t1 = -t2;
		}
		// ここまで

		if(!NyARRotVector.checkRotation(vec0, vec1)){
			return false;
		}
		double v20 = vec0.v2 * vec1.v3 - vec0.v3 * vec1.v2;
		double v21 = vec0.v3 * vec1.v1 - vec0.v1 * vec1.v3;
		double v22 = vec0.v1 * vec1.v2 - vec0.v2 * vec1.v1;
		l1 = Math.sqrt(v20 * v20 + v21 * v21 + v22 * v22);
		v20 /= l1;
		v21 /= l1;
		v22 /= l1;

		initMatXw2Xc.m00 = vec0.v1;
		initMatXw2Xc.m10 = vec0.v2;
		initMatXw2Xc.m20 = vec0.v3;
		initMatXw2Xc.m01 = vec1.v1;
		initMatXw2Xc.m11 = vec1.v2;
		initMatXw2Xc.m21 = vec1.v3;
		initMatXw2Xc.m02 = v20;
		initMatXw2Xc.m12 = v21;
		initMatXw2Xc.m22 = v22;
		initMatXw2Xc.m03 = t0;
		initMatXw2Xc.m13 = t1;
		initMatXw2Xc.m23 = t2;
		initMatXw2Xc.m30=initMatXw2Xc.m31=initMatXw2Xc.m32=0;
		initMatXw2Xc.m33=1;

		icpGetInitXw2XcSub(initMatXw2Xc, screenCoord, worldCoord, i_num,initMatXw2Xc);
		return true;
	}
	//There is work variables.
	private NyARDoublePoint3d __off=new NyARDoublePoint3d();
	private NyARDoubleMatrix33 __matd=new NyARDoubleMatrix33();
	private double[] __mate=new double[3];
	
	public void icpGetInitXw2XcSub(NyARDoubleMatrix44 rot,
			NyARDoublePoint2d[] pos2d, NyARDoublePoint3d[] ppos3d, int num,
			NyARDoubleMatrix44 conv) throws NyARException
	{
		NyARDoublePoint3d off=makeOffset(ppos3d,num,this.__off);
		NyARDoubleMatrix33 matd=makeMatD(this._cparam,pos2d, num,this.__matd);
		double[] mate=makeMatE(this._cparam,rot,pos2d,ppos3d,off,num,this.__mate);
		
		conv.setValue(rot);
		//matf
		conv.m03 = matd.m00*mate[0]+matd.m01*mate[1]+matd.m02*mate[2];
		conv.m13 = matd.m10*mate[0]+matd.m11*mate[1]+matd.m12*mate[2];
		conv.m23 = matd.m20*mate[0]+matd.m21*mate[1]+matd.m22*mate[2];

		conv.m03 = conv.m00 * off.x + conv.m01 * off.y + conv.m02 * off.z+ conv.m03;
		conv.m13 = conv.m10 * off.x + conv.m11 * off.y + conv.m12 * off.z+ conv.m13;
		conv.m23 = conv.m20 * off.x + conv.m21 * off.y + conv.m22 * off.z+ conv.m23;

		return;
	}
	/**
	 * {@link #icpGetInitXw2XcSub}関数のオフセットを計算します。
	 */
	private static NyARDoublePoint3d makeOffset(NyARDoublePoint3d[] ppos3d,int num,NyARDoublePoint3d o_off)
	{
		double minx,miny,minz;
		double maxx,maxy,maxz;
		minx=miny=minz=Double.POSITIVE_INFINITY;
		maxx=maxy=maxz=Double.NEGATIVE_INFINITY;
		for (int i = 0; i < num; i++) {
			if (ppos3d[i].x > maxx) {
				maxx = ppos3d[i].x;
			}
			if (ppos3d[i].x < minx) {
				minx = ppos3d[i].x;
			}
			if (ppos3d[i].y > maxy) {
				maxy = ppos3d[i].y;
			}
			if (ppos3d[i].y < miny) {
				miny = ppos3d[i].y;
			}
			if (ppos3d[i].z > maxz) {
				maxz = ppos3d[i].z;
			}
			if (ppos3d[i].z < minz) {
				minz = ppos3d[i].z;
			}
		}
		o_off.x= -(maxx + minx) / 2.0;
		o_off.y= -(maxy + miny) / 2.0;
		o_off.z= -(maxz + minz) / 2.0;
		return o_off;
	}	
	/**
	 * {@link #icpGetInitXw2XcSub}関数のmat_eを計算します。
	 */	
	private static double[] makeMatE(NyARDoubleMatrix44 i_cp,NyARDoubleMatrix44 rot,NyARDoublePoint2d[] pos2d,NyARDoublePoint3d[] pos3d,NyARDoublePoint3d offset,int i_num,double[] o_val)
	{
		double v0=0;
		double v1=0;
		double v2=0;
		for (int j = 0; j < i_num; j++){
			double p3x=pos3d[j].x+offset.x;
			double p3y=pos3d[j].y+offset.y;
			double p3z=pos3d[j].z+offset.z;
			double wx = rot.m00 * p3x + rot.m01 * p3y + rot.m02* p3z;
			double wy = rot.m10 * p3x + rot.m11 * p3y + rot.m12* p3z;
			double wz = rot.m20 * p3x + rot.m21 * p3y + rot.m22* p3z;
			double c1 = wz * pos2d[j].x - i_cp.m00 * wx - i_cp.m01 * wy- i_cp.m02 * wz;
			double c2 = wz * pos2d[j].y - i_cp.m11 * wy - i_cp.m12* wz;
			v0+=i_cp.m00*c1;
			v1+=i_cp.m01*c1+i_cp.m11*c2;
			v2+=(i_cp.m02 - pos2d[j].x)*c1+(i_cp.m12 - pos2d[j].y)*c2;
		}
		o_val[0]=v0;
		o_val[1]=v1;
		o_val[2]=v2;
		return o_val;
	}

	
	/**
	 * {@link #icpGetInitXw2XcSub}関数のmat_dを計算します。
	 */
	private static NyARDoubleMatrix33 makeMatD(NyARDoubleMatrix44 i_cp,NyARDoublePoint2d[] pos2d,int i_num,NyARDoubleMatrix33 o_mat)
	{
		double m02=0;
		double m12=0;
		double m20=0;
		double m21=0;
		double m22=0;
		for(int i=0;i<i_num;i++){
			double cx=i_cp.m02 - pos2d[i].x;
			double cy=i_cp.m12 - pos2d[i].y;
			m02+=(cx)*i_cp.m00;
			m12+=(cx)*i_cp.m01+(cy)*i_cp.m11;
			m20+=i_cp.m00*(cx);
			m21+=i_cp.m01*(cx)+i_cp.m11*(cy);
			m22+=(cx)*(cx)+(cy)*(cy);
		}
		o_mat.m00=(i_cp.m00*i_cp.m00)*i_num;
		o_mat.m01=(i_cp.m00*i_cp.m01)*i_num;
		o_mat.m02=m02;
		o_mat.m10=(i_cp.m01*i_cp.m00)*i_num;
		o_mat.m11=(i_cp.m01*i_cp.m01+i_cp.m11*i_cp.m11)*i_num;
		o_mat.m12=m12;
		o_mat.m20=m20;
		o_mat.m21=m21;
		o_mat.m22=m22;
		o_mat.inverse(o_mat);
		return o_mat;
	}

	
}
