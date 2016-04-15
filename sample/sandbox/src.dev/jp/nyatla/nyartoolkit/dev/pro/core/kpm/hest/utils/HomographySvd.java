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
package jp.nyatla.nyartoolkit.dev.pro.core.kpm.hest.utils;

import jp.nyatla.nyartoolkit.core.math.NyARMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

/**
 * 4頂点のSVD計算に特化したSVDMatクラス
 */
public class HomographySvd extends SvdMat
{
	public HomographySvd()
	{
		super(9,4*3);
		this.resetConstantValues();
	}
	/**
	 * 固定�?�部�?をリセ�?トします�??
	 */
	public void resetConstantValues()
	{
		double[][] aaptr=this._m;
		for(int i = 0 ; i < 4 ; i++ )
		{
			aaptr[0][(0+i*3)]= 0;//a[0];
			aaptr[1][(0+i*3)]= 0;//a[1];
			aaptr[2][(0+i*3)]= 0;//a[2];
			aaptr[2][(1+i*3)]= 1;//a[11];
			aaptr[3][(1+i*3)]= 0;//a[12];
			aaptr[4][(1+i*3)]= 0;//a[13];
			aaptr[5][(0+i*3)]= -1;//a[5];
			aaptr[5][(1+i*3)]= 0;//a[14];
			aaptr[6][(2+i*3)]= 0;//a[24];
			aaptr[7][(2+i*3)]= 0;//a[25];
			aaptr[8][(2+i*3)]= 0;//a[26];
		}			
	}
	/**
	 * 座標系をセ�?トします�??
	 * @param pt1
	 * @param pt2
	 */
	public void setPoints(NyARDoublePoint2d[] pt1, NyARDoublePoint2d[] pt2)
	{
		double[][] aaptr=this._m;
		for(int i = 0 ; i < 4 ; i++ )
		{
			double x1 = pt1[i].x;
			double y1 = pt1[i].y;

			double x2 = pt2[i].x;
			double y2 = pt2[i].y;


			aaptr[0][(1+i*3)]= x1;//a[9];
			aaptr[0][(2+i*3)]= -y2*x1;//a[18];
			aaptr[1][(1+i*3)]= y1;//a[10];
			aaptr[1][(2+i*3)]= -y2*y1;//a[19];
			aaptr[2][(2+i*3)]= -y2;//a[20];
			aaptr[3][(0+i*3)]= -x1;//a[3];
			aaptr[3][(2+i*3)]= x2*x1;//a[21];
			aaptr[4][(0+i*3)]= -y1;//a[4];
			aaptr[4][(2+i*3)]= x2*y1;//a[22];
			aaptr[5][(0+i*3)]= -1;//a[5];
			aaptr[5][(2+i*3)]= x2;//a[23];
			aaptr[6][(0+i*3)]=  y2*x1;//a[6];
			aaptr[6][(1+i*3)]= -x2*x1;//a[15];
			aaptr[7][(0+i*3)]= y2*y1;//a[7];
			aaptr[7][(1+i*3)]= -x2*y1;//a[16];
			aaptr[8][(0+i*3)]= y2;//a[8];
			aaptr[8][(1+i*3)]= -x2;//a[17];
		}			
	}
	private NyARMat __U=new NyARMat(4*3, 4*3);
	private NyARMat __vn=new NyARMat(9,9); 
	private NyARMat __dn=new NyARMat(1,9); 	
	/**
	 * i_,matにU成�?をセ�?トして返します�?�細かいことは判らん�?
	 */
	public void svd_u(NyARDoubleMatrix33 i_mat)
	{
		int r = this.row;
		int c = this.clm;
		assert(r<=c);
		//m>nである�?

		//UとVnひっくりかえした�?
		double[][] warr=this.__dn.getArray();/* n*1 */
		double[][] vt=__U.getArray();/* m*n */
		double[][] u=this.__vn.getArray();/* n*n*/
	    
		//vtのm*m行�?�を利用して計算する�??(コピ�?�するのは�?終行以�?)
		for(int i=r-1;i>=0;i--){
			double[] ptr1=vt[i];
			double[] ptr2=this._m[i];
			int i2=c-1;
			for(;i2>3;i2-=4){
				ptr1[i2]=ptr2[i2];
				ptr1[i2-1]=ptr2[i2-1];
				ptr1[i2-2]=ptr2[i2-2];
				ptr1[i2-3]=ptr2[i2-3];
			}
			for(;i2>=0;i2--){
				ptr1[i2]=ptr2[i2];
			}
		}
		//A,W,Vの計�?
		jacobiSvd_1(vt,warr[0],u,c, r);
		i_mat.m00=u[0][9-1];
		i_mat.m01=u[1][9-1];
		i_mat.m02=u[2][9-1];
		i_mat.m10=u[3][9-1];
		i_mat.m11=u[4][9-1];
		i_mat.m12=u[5][9-1];
		i_mat.m20=u[6][9-1];
		i_mat.m21=u[7][9-1];
		i_mat.m22=u[8][9-1];
		return;
	}	
	
	
}