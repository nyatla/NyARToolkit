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

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class NyARIcpUtils
{
	final static class U extends NyARDoublePoint2d
	{
		/**
		 * icpGetU_from_X_by_MatX2U関数
		 * @param matX2U
		 * @param coord3d
		 * @return
		 */
		public boolean setXbyMatX2U(NyARDoubleMatrix44 matX2U,NyARDoublePoint3d coord3d)
		{
		    double hx = matX2U.m00 * coord3d.x + matX2U.m01 * coord3d.y+ matX2U.m02 * coord3d.z + matX2U.m03;
		    double hy = matX2U.m10 * coord3d.x + matX2U.m11 * coord3d.y+ matX2U.m12 * coord3d.z + matX2U.m13;
		    double h  = matX2U.m20 * coord3d.x + matX2U.m21 * coord3d.y+ matX2U.m22 * coord3d.z + matX2U.m23;

		    if( h == 0.0 ){
		    	return false;
		    }
		    this.x = hx / h;
		    this.y = hy / h;
		    return true;
		}
		
	}


	
	
	final static class JusStack extends NyARObjectStack<JusStack.Item>
	{
		public JusStack(int i_length)
		{
			super(i_length,JusStack.Item.class);
			return;
		}
		/**
		 * この関数は、配列要素を作成します。
		 */
		protected JusStack.Item createElement()
		{
			return new Item();
		}		
		static class Item
		{
			public double m00;
			public double m01;
			public double m02;
			public double m03;
			public double m04;
			public double m05;
			public double m10;
			public double m11;
			public double m12;
			public double m13;
			public double m14;
			public double m15;
			public double ux;
			public double uy;
			static Item[] createArray(int i_num)
			{
				Item[] ret=new Item[i_num];
				for(int i=0;i<i_num;i++){
					ret[i]=new Item();
				}
				return ret;
			}
		}
		public NyARMat makeJtJ(NyARMat o_dst)
		{
			o_dst.loadZero();
			Item[] buf=this._items;
			double[][] b=o_dst.getArray();
			for(int k=0;k<this._length;k++)
			{
				Item ptr=buf[k];
				b[0][0]+=(ptr.m00*ptr.m00)+(ptr.m10*ptr.m10);
				b[0][1]+=(ptr.m00*ptr.m01)+(ptr.m10*ptr.m11);
				b[0][2]+=(ptr.m00*ptr.m02)+(ptr.m10*ptr.m12);
				b[0][3]+=(ptr.m00*ptr.m03)+(ptr.m10*ptr.m13);
				b[0][4]+=(ptr.m00*ptr.m04)+(ptr.m10*ptr.m14);
				b[0][5]+=(ptr.m00*ptr.m05)+(ptr.m10*ptr.m15);
				
				b[1][0]+=(ptr.m01*ptr.m00)+(ptr.m11*ptr.m10);
				b[1][1]+=(ptr.m01*ptr.m01)+(ptr.m11*ptr.m11);
				b[1][2]+=(ptr.m01*ptr.m02)+(ptr.m11*ptr.m12);
				b[1][3]+=(ptr.m01*ptr.m03)+(ptr.m11*ptr.m13);
				b[1][4]+=(ptr.m01*ptr.m04)+(ptr.m11*ptr.m14);
				b[1][5]+=(ptr.m01*ptr.m05)+(ptr.m11*ptr.m15);
				
				b[2][0]+=(ptr.m02*ptr.m00)+(ptr.m12*ptr.m10);
				b[2][1]+=(ptr.m02*ptr.m01)+(ptr.m12*ptr.m11);
				b[2][2]+=(ptr.m02*ptr.m02)+(ptr.m12*ptr.m12);
				b[2][3]+=(ptr.m02*ptr.m03)+(ptr.m12*ptr.m13);
				b[2][4]+=(ptr.m02*ptr.m04)+(ptr.m12*ptr.m14);
				b[2][5]+=(ptr.m02*ptr.m05)+(ptr.m12*ptr.m15);

				b[3][0]+=(ptr.m03*ptr.m00)+(ptr.m13*ptr.m10);
				b[3][1]+=(ptr.m03*ptr.m01)+(ptr.m13*ptr.m11);
				b[3][2]+=(ptr.m03*ptr.m02)+(ptr.m13*ptr.m12);
				b[3][3]+=(ptr.m03*ptr.m03)+(ptr.m13*ptr.m13);
				b[3][4]+=(ptr.m03*ptr.m04)+(ptr.m13*ptr.m14);
				b[3][5]+=(ptr.m03*ptr.m05)+(ptr.m13*ptr.m15);
				
				b[4][0]+=(ptr.m04*ptr.m00)+(ptr.m14*ptr.m10);
				b[4][1]+=(ptr.m04*ptr.m01)+(ptr.m14*ptr.m11);
				b[4][2]+=(ptr.m04*ptr.m02)+(ptr.m14*ptr.m12);
				b[4][3]+=(ptr.m04*ptr.m03)+(ptr.m14*ptr.m13);
				b[4][4]+=(ptr.m04*ptr.m04)+(ptr.m14*ptr.m14);
				b[4][5]+=(ptr.m04*ptr.m05)+(ptr.m14*ptr.m15);
				
				b[5][0]+=(ptr.m05*ptr.m00)+(ptr.m15*ptr.m10);
				b[5][1]+=(ptr.m05*ptr.m01)+(ptr.m15*ptr.m11);
				b[5][2]+=(ptr.m05*ptr.m02)+(ptr.m15*ptr.m12);
				b[5][3]+=(ptr.m05*ptr.m03)+(ptr.m15*ptr.m13);
				b[5][4]+=(ptr.m05*ptr.m04)+(ptr.m15*ptr.m14);
				b[5][5]+=(ptr.m05*ptr.m05)+(ptr.m15*ptr.m15);
			}
			return o_dst;
		}
		public double[] makeJtU(double[] o_dst)
		{
			double d0,d1,d2,d3,d4,d5;
			d0=d1=d2=d3=d4=d5=0;
			for(int i=0;i<this._length;i++){
				d0+=this._items[i].m00*this._items[i].ux+this._items[i].m10*this._items[i].uy;
				d1+=this._items[i].m01*this._items[i].ux+this._items[i].m11*this._items[i].uy;
				d2+=this._items[i].m02*this._items[i].ux+this._items[i].m12*this._items[i].uy;
				d3+=this._items[i].m03*this._items[i].ux+this._items[i].m13*this._items[i].uy;
				d4+=this._items[i].m04*this._items[i].ux+this._items[i].m14*this._items[i].uy;
				d5+=this._items[i].m05*this._items[i].ux+this._items[i].m15*this._items[i].uy;
			}
			o_dst[0]=d0;
			o_dst[1]=d1;
			o_dst[2]=d2;
			o_dst[3]=d3;
			o_dst[4]=d4;
			o_dst[5]=d5;
			return o_dst;
		}
		
	    private double[][] __J_Xc_S=new double[3][6];
	    private J_U_Xc __juxc=new J_U_Xc();
		/**
		 * 
		 * @param J_U_S
		 * double[2][6]
		 * @return
		 */
		public boolean push(NyARDoubleMatrix44 matXc2U,NyARDoubleMatrix44 matXw2Xc,NyARDoublePoint3d worldCoord,NyARDoublePoint2d du,double W)
		{
		    double[][] J_Xc_S=this.__J_Xc_S;
		    J_U_Xc juxc=this.__juxc;
		    double wx=worldCoord.x;
		    double wy=worldCoord.y;
		    double wz=worldCoord.z;

		    //icpGetJ_Xc_S1
			if(!juxc.setXc2UXc(
		    	matXc2U,
		    	matXw2Xc.m00*wx + matXw2Xc.m01*wy + matXw2Xc.m02*wz + matXw2Xc.m03,
		    	matXw2Xc.m10*wx + matXw2Xc.m11*wy + matXw2Xc.m12*wz + matXw2Xc.m13,
		    	matXw2Xc.m20*wx + matXw2Xc.m21*wy + matXw2Xc.m22*wz + matXw2Xc.m23))
		    {
		        return false;
		    }
		    
		    //icpGetJ_Xc_S2
	        J_Xc_S[0][0] = (-matXw2Xc.m01 * wz) + ( matXw2Xc.m02 * wy);
	        J_Xc_S[0][1] = ( matXw2Xc.m00 * wz ) + (-matXw2Xc.m02 * wx);
	        J_Xc_S[0][2] = (-matXw2Xc.m00 * wy ) + ( matXw2Xc.m01 * wx);
	        J_Xc_S[0][3] = ( matXw2Xc.m00 );
	        J_Xc_S[0][4] = ( matXw2Xc.m01 );
	        J_Xc_S[0][5] = ( matXw2Xc.m02 );	        
	        J_Xc_S[1][0] = (-matXw2Xc.m11 * wz ) + ( matXw2Xc.m12 * wy);
	        J_Xc_S[1][1] = ( matXw2Xc.m10 * wz ) + (-matXw2Xc.m12 * wx);
	        J_Xc_S[1][2] = (-matXw2Xc.m10 * wy ) + ( matXw2Xc.m11 * wx);
	        J_Xc_S[1][3] = ( matXw2Xc.m10 );
	        J_Xc_S[1][4] = ( matXw2Xc.m11);
	        J_Xc_S[1][5] = ( matXw2Xc.m12);        
	        J_Xc_S[2][0] = (-matXw2Xc.m21 * wz ) + ( matXw2Xc.m22 * wy);
	        J_Xc_S[2][1] = ( matXw2Xc.m20 * wz ) + (-matXw2Xc.m22 * wx);
	        J_Xc_S[2][2] = (-matXw2Xc.m20 * wy ) + ( matXw2Xc.m21 * wx);
	        J_Xc_S[2][3] = ( matXw2Xc.m20 );
	        J_Xc_S[2][4] = ( matXw2Xc.m21);
	        J_Xc_S[2][5] = ( matXw2Xc.m22);
		    

		    Item item=this.prePush();
		    if(item==null){
		    	return false;
		    }

        	item.m00=((juxc.m00 * J_Xc_S[0][0])+(juxc.m01 * J_Xc_S[1][0])+(juxc.m02 * J_Xc_S[2][0]))*W;
        	item.m01=((juxc.m00 * J_Xc_S[0][1])+(juxc.m01 * J_Xc_S[1][1])+(juxc.m02 * J_Xc_S[2][1]))*W;
        	item.m02=((juxc.m00 * J_Xc_S[0][2])+(juxc.m01 * J_Xc_S[1][2])+(juxc.m02 * J_Xc_S[2][2]))*W;
        	item.m03=((juxc.m00 * J_Xc_S[0][3])+(juxc.m01 * J_Xc_S[1][3])+(juxc.m02 * J_Xc_S[2][3]))*W;
        	item.m04=((juxc.m00 * J_Xc_S[0][4])+(juxc.m01 * J_Xc_S[1][4])+(juxc.m02 * J_Xc_S[2][4]))*W;
        	item.m05=((juxc.m00 * J_Xc_S[0][5])+(juxc.m01 * J_Xc_S[1][5])+(juxc.m02 * J_Xc_S[2][5]))*W;
        	item.m10=((juxc.m10 * J_Xc_S[0][0])+(juxc.m11 * J_Xc_S[1][0])+(juxc.m12 * J_Xc_S[2][0]))*W;
        	item.m11=((juxc.m10 * J_Xc_S[0][1])+(juxc.m11 * J_Xc_S[1][1])+(juxc.m12 * J_Xc_S[2][1]))*W;
        	item.m12=((juxc.m10 * J_Xc_S[0][2])+(juxc.m11 * J_Xc_S[1][2])+(juxc.m12 * J_Xc_S[2][2]))*W;
        	item.m13=((juxc.m10 * J_Xc_S[0][3])+(juxc.m11 * J_Xc_S[1][3])+(juxc.m12 * J_Xc_S[2][3]))*W;
        	item.m14=((juxc.m10 * J_Xc_S[0][4])+(juxc.m11 * J_Xc_S[1][4])+(juxc.m12 * J_Xc_S[2][4]))*W;
        	item.m15=((juxc.m10 * J_Xc_S[0][5])+(juxc.m11 * J_Xc_S[1][5])+(juxc.m12 * J_Xc_S[2][5]))*W;
        	
        	item.ux=du.x*W;
        	item.uy=du.y*W;
		    return true;
		}

	}	
	final static class J_U_Xc
	{
		public double m00;
		public double m01;
		public double m02;
		public double m10;
		public double m11;
		public double m12;
		/**
		 * icpGetJ_U_Xc
		 * @param J_U_Xc
		 * @param matXc2U
		 * @param cameraCoord
		 * @return
		 */
		public boolean setXc2UXc(NyARDoubleMatrix44 matXc2U,double Xcx,double Xcy,double Xcz)
		{
		    double w1, w2, w3, w3_w3;

		    w1 = matXc2U.m00 * Xcx + matXc2U.m01 * Xcy + matXc2U.m02 * Xcz + matXc2U.m03;
		    w2 = matXc2U.m10 * Xcx + matXc2U.m11 * Xcy + matXc2U.m12 * Xcz + matXc2U.m13;
		    w3 = matXc2U.m20 * Xcx + matXc2U.m21 * Xcy + matXc2U.m22 * Xcz + matXc2U.m23;

		    if( w3 == 0.0 ){
		    	return false;
		    }

		    w3_w3 = w3 * w3;
		    this.m00 = (matXc2U.m00 * w3 - matXc2U.m20 * w1) / w3_w3;
		    this.m01 = (matXc2U.m01 * w3 - matXc2U.m21 * w1) / w3_w3;
		    this.m02 = (matXc2U.m02 * w3 - matXc2U.m22 * w1) / w3_w3;
		    this.m10 = (matXc2U.m10 * w3 - matXc2U.m20 * w2) / w3_w3;
		    this.m11 = (matXc2U.m11 * w3 - matXc2U.m21 * w2) / w3_w3;
		    this.m12 = (matXc2U.m12 * w3 - matXc2U.m22 * w2) / w3_w3;
		    return true;
		}		
	}


	final static class DeltaS
	{
		public double s0;
		public double s1;
		public double s2;
		public double s3;
		public double s4;
		public double s5;
		private NyARMat __matJtJ = new NyARMat(6,6);
		private double[] __JtU=new double[6];
		/**
		 * icpGetDeltaS関数です。
		 * @param dU
		 * @param J_U_S
		 * @param n
		 * @return
		 * @throws NyARException
		 */
		public boolean setJusArray(NyARIcpUtils.JusStack i_jus) throws NyARException
		{
		    i_jus.makeJtJ(this.__matJtJ);
		    double[] JtU=i_jus.makeJtU(this.__JtU);

		    if(!this.__matJtJ.inverse()) {
		    	return false;
		    }		    
		    double[][] matJtJb=this.__matJtJ.getArray();
		    this.s0=	matJtJb[0][0]*JtU[0]+matJtJb[0][1]*JtU[1]+matJtJb[0][2]*JtU[2]+matJtJb[0][3]*JtU[3]+matJtJb[0][4]*JtU[4]+matJtJb[0][5]*JtU[5];
		    this.s1=	matJtJb[1][0]*JtU[0]+matJtJb[1][1]*JtU[1]+matJtJb[1][2]*JtU[2]+matJtJb[1][3]*JtU[3]+matJtJb[1][4]*JtU[4]+matJtJb[1][5]*JtU[5];
		    this.s2=	matJtJb[2][0]*JtU[0]+matJtJb[2][1]*JtU[1]+matJtJb[2][2]*JtU[2]+matJtJb[2][3]*JtU[3]+matJtJb[2][4]*JtU[4]+matJtJb[2][5]*JtU[5];
		    this.s3=	matJtJb[3][0]*JtU[0]+matJtJb[3][1]*JtU[1]+matJtJb[3][2]*JtU[2]+matJtJb[3][3]*JtU[3]+matJtJb[3][4]*JtU[4]+matJtJb[3][5]*JtU[5];
		    this.s4=	matJtJb[4][0]*JtU[0]+matJtJb[4][1]*JtU[1]+matJtJb[4][2]*JtU[2]+matJtJb[4][3]*JtU[3]+matJtJb[4][4]*JtU[4]+matJtJb[4][5]*JtU[5];
		    this.s5=	matJtJb[5][0]*JtU[0]+matJtJb[5][1]*JtU[1]+matJtJb[5][2]*JtU[2]+matJtJb[5][3]*JtU[3]+matJtJb[5][4]*JtU[4]+matJtJb[5][5]*JtU[5];
		    return true;
		}
		private IcpMat44 __mat=new IcpMat44();
		
		/**
		 * icpUpdateMat関数です。matXw2Xcへ値を出力します。
		 * @param matXw2Xc
		 * @param dS
		 * double[6]
		 * @return
		 */
		public void makeMat(NyARDoubleMatrix44 matXw2Xc)
		{
			IcpMat44 mat=this.__mat;
			mat.setDeltaS(this);
		    
		    double w0,w1,w2,w3;
		    w0=matXw2Xc.m00;
		    w1=matXw2Xc.m01;
		    w2=matXw2Xc.m02;
		    w3=matXw2Xc.m03;
		    
		    //ji
		    matXw2Xc.m00 = w0 * mat.m00+ w1 * mat.m10+ w2 * mat.m20;
		    matXw2Xc.m01 = w0 * mat.m01+ w1 * mat.m11+ w2 * mat.m21;
		    matXw2Xc.m02 = w0 * mat.m02+ w1 * mat.m12+ w2 * mat.m22;
		    matXw2Xc.m03 = w0 * mat.m03+ w1 * mat.m13+ w2 * mat.m23+w3;
		    
		    
		    w0=matXw2Xc.m10;
		    w1=matXw2Xc.m11;
		    w2=matXw2Xc.m12;
		    w3=matXw2Xc.m13;
		    matXw2Xc.m10 = w0 * mat.m00+ w1 * mat.m10+ w2 * mat.m20;
		    matXw2Xc.m11 = w0 * mat.m01+ w1 * mat.m11+ w2 * mat.m21;
		    matXw2Xc.m12 = w0 * mat.m02+ w1 * mat.m12+ w2 * mat.m22;
		    matXw2Xc.m13 = w0 * mat.m03+ w1 * mat.m13+ w2 * mat.m23+w3;

		    w0=matXw2Xc.m20;
		    w1=matXw2Xc.m21;
		    w2=matXw2Xc.m22;
		    w3=matXw2Xc.m23;	        
		    matXw2Xc.m20 = w0 * mat.m00+ w1 * mat.m10+ w2 * mat.m20;
		    matXw2Xc.m21 = w0 * mat.m01+ w1 * mat.m11+ w2 * mat.m21;
	        matXw2Xc.m22 = w0 * mat.m02+ w1 * mat.m12+ w2 * mat.m22;
	        matXw2Xc.m23 = w0 * mat.m03+ w1 * mat.m13+ w2 * mat.m23+w3;

	        matXw2Xc.m30 = 0;
	        matXw2Xc.m31 = 0;
	        matXw2Xc.m32 = 0;
	        matXw2Xc.m33 = 1;        
		    return;
		}		
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
		/**
		 * icpGetQ_from_S関数です。
		 * @param i_val
		 */
		public void setS(DeltaS i_val)
		{
		    double ra=i_val.s0*i_val.s0+i_val.s1*i_val.s1+i_val.s2*i_val.s2;
		    if( ra == 0.0 ) {
		        this.q0 = 1.0;
		        this.q1 = 0.0;
		        this.q2 = 0.0;
		        this.q3 = 0.0;
		    }else{
		    	ra=Math.sqrt(ra);
		    	this.q0 = i_val.s0 / ra;
		    	this.q1 = i_val.s1 / ra;
		    	this.q2 = i_val.s2 / ra;
		    	this.q3 = ra;
		    }
		    this.q4 = i_val.s3;
		    this.q5 = i_val.s4;
		    this.q6 = i_val.s5;
		    return;			
		}
	}
	final static class IcpMat44 extends NyARDoubleMatrix44
	{
		private Q __q2=new Q();
		/**
		 * icpGetMat_from_S+ icpGetMat_from_Q
		 * @param i_val
		 */
		public void setDeltaS(DeltaS i_val)
		{
		    Q q2=this.__q2;
		    q2.setS(i_val);

		    double cra, one_cra, sra;
		    cra = Math.cos(q2.q3);
		    one_cra = 1.0 - cra;
		    sra = Math.sin(q2.q3);

		    this.m00 = q2.q0*q2.q0*one_cra + cra;
		    this.m01 = q2.q0*q2.q1*one_cra - q2.q2*sra;
		    this.m02 = q2.q0*q2.q2*one_cra + q2.q1*sra;
		    this.m03 = q2.q4;
		    this.m10 = q2.q1*q2.q0*one_cra + q2.q2*sra;
		    this.m11 = q2.q1*q2.q1*one_cra + cra;
		    this.m12 = q2.q1*q2.q2*one_cra - q2.q0*sra;
		    this.m13 = q2.q5;
		    this.m20 = q2.q2*q2.q0*one_cra - q2.q1*sra;
		    this.m21 = q2.q2*q2.q1*one_cra + q2.q0*sra;
		    this.m22 = q2.q2*q2.q2*one_cra + cra;
		    this.m23 = q2.q6;			
		    return;
		}
	}
	
}
