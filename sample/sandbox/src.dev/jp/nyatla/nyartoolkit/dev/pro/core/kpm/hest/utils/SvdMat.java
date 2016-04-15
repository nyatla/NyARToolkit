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
import jp.nyatla.nyartoolkit.core.math.NyARMath;



public class SvdMat extends NyARMat
{
	/**
	 * i_row=<i_clmであること�?
	 * @param i_row
	 * @param i_clm
	 */
	public SvdMat(int i_row, int i_clm)
	{
		super(i_row,i_clm);
	}
	public void svd(NyARMat i_warr,NyARMat i_u, NyARMat i_vt)
	{
		int r = this.row;
		int c = this.clm;
		assert(r<=c);
		//m>nである�?
		assert(i_warr.getRow()==1 && i_warr.getClm()==r);
		double[][] warr=i_warr.getArray();/* n*1 */
		double[][] vt=i_vt.getArray();/* m*n */
		double[][] u=i_u.getArray();/* n*n*/
	    
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
		//Aの計算�?�つづ�?
		jacobiSvd_2(vt,warr,c, r,Double.MIN_VALUE);
	}
	
	protected static void jacobiSvd_1(double[][] At,double[] _W, double[][] Vt,int m, int n)
	{
		double eps = NyARMath.DBL_EPSILON*10;
		int j, k, iter;
		int max_iter = m>30?m:30;    
		for(int i = 0; i < n; i++ )
		{
			double sd=0;
			for( k = 0;k < m; k++ )
			{
				double t = At[i][k];
				sd += (double)t*t;
			}
			_W[i] = sd;
	        
			for( k = 0; k < n; k++ ){
				Vt[k][i] = 0;
			}
			Vt[i][i] = 1;
		}
	    
		for( iter = 0; iter < max_iter; iter++ )
		{
			boolean changed = false;
	        
			for(int i = 0; i < n-1; i++ ){
				double Wi = _W[i];
				double[] At_i=At[i];
				for( j = i+1; j < n; j++ )
				{
					double[] At_j=At[j];
					double Wj = _W[j];
					double p = 0;
	                //<tiling>
					for( k = m-1; k >=3; k-=4){
						p += At_i[k]*At_j[k]
						  +  At_i[k-1]*At_j[k-1]
						  +  At_i[k-2]*At_j[k-2]
						  +  At_i[k-3]*At_j[k-3];
					}
					for(; k >=0; k--){
						p += At_i[k]*At_j[k];
					}
					//</tiling>
	                
					if((p>0?p:-p) <= eps*Math.sqrt((double)Wi*Wj) ){
						continue;           
					}
	                
					p *= 2;
					double beta = Wi - Wj;
					double gamma = Math.hypot((double)p, beta);
					double delta;
					double c, s;
					if( beta < 0 )
					{
						delta = (gamma - beta)*0.5;
						s = Math.sqrt(delta/gamma);
						c = (p/(gamma*s*2));
					}
					else
					{
						c = Math.sqrt((gamma + beta)/(gamma*2));
						s = (p/(gamma*c*2));
						delta = p*p*0.5/(gamma + beta);
					}
	                
					Wi += delta;
					Wj -= delta;
	                
					if( iter % 2 != 0 && Wi > 0 && Wj > 0 )
					{	                    
						for(k=0 ; k < m; k++ )
						{
							double t0 = c*At_i[k] + s*At_j[k];
							double t1 = -s*At_i[k] + c*At_j[k];
							At_i[k] = t0; At_j[k] = t1;
						}
					}
					else
					{
						Wi = Wj = 0;
						for( k = m-1; k >=0; k-- )
						{
							double t0 = c*At_i[k] + s*At_j[k];
							double t1 = -s*At_i[k] + c*At_j[k];
							At_i[k] = t0; At_j[k] = t1;
							Wi +=t0*t0; Wj +=t1*t1;
						}
						/*_W[i] = a;*/ 
					}
	                
					changed = true;
	                
	                    
					for(k=n-1 ; k >=0; k-- )
					{
						double t0 = c*Vt[k][i] + s*Vt[k][j];
						double t1 = -s*Vt[k][i] + c*Vt[k][j];
						Vt[k][i] = t0; Vt[k][j] = t1;						
					}
					_W[j] = Wj;
				}
				_W[i]=Wi;
			}
			if( !changed ){
				break;
			}
		}
	    
		for(int i = 0; i < n; i++ )
		{
			double sd=0;
			for( k = 0; k < m; k++ )
			{
				double t = At[i][k];
				sd += t*t;
			}
			_W[i] = Math.sqrt(sd);
		}
	    
		for(int i = 0; i < n-1; i++ )
		{
			j = i;
			for( k = i+1; k < n; k++ )
			{
				if( _W[j] < _W[k] ){
					j = k;
				}
			}
			if( i != j )
			{
				double swp;
				swp=_W[i];
				_W[i]=_W[j];
				_W[j]=swp;
				for( k = 0; k < m; k++ ){
					swp=At[i][k];
					At[i][k]=At[j][k];
					At[j][k]=swp;
				}
				for( k = 0; k < n; k++ ){
					swp=Vt[k][i];//
					Vt[k][i]=Vt[k][j];
					Vt[k][j]=swp;
				}
			}
		}		
	}
	protected static void jacobiSvd_2(double[][] At,double[][] W,int m, int n, double minval)
	{
		int i, j, k, iter;
		//ここからVtの計�?
		long rnd=0x12345678;

		for( i = 0; i < m; i++ )
		{
			double sd = i < n ? W[0][i] : 0;
	        
			while( sd <= minval )
			{
				// if we got a zero singular value, then in order to get the corresponding left singular vector
				// we generate a random vector, project it to the previously computed left singular vectors,
				// subtract the projection and normalize the difference.
				double val0 = (double)(1.0/m);
				for( k = 0; k < m; k++ )
				{
					rnd = rnd*4164903690L + (rnd >> 32);
					double val = (rnd & 0xff) != 0 ? val0 : -val0;
					At[i][k] = val;					//At[i*m + k] = val;
				}
				for( iter = 0; iter < 2; iter++ )
				{
					for( j = 0; j < i; j++ )
					{
						sd = 0;
						for( k = 0; k < m; k++ ){
							sd += At[i][k]*At[j][k];//sd += At[i*m + k]*At[j*m + k];
						}
						double asum = 0;
						for( k = 0; k < m; k++ )
						{
							double t = (double)(At[i][k] - sd*At[j][k]);
							At[i][k] = t;
							asum += Math.abs(t);
						}
						asum = asum!=0 ? 1/asum : 0;
						for( k = 0; k < m; k++ ){
							At[i][k] *= asum;
						}
					}
				}
				sd = 0;
				for( k = 0; k < m; k++ )
				{
					double t = At[i][k];
					sd += (double)t*t;
				}
				sd = Math.sqrt(sd);
			}
	        
			double s = (double)(1/sd);
			for( k = 0; k < m; k++ ){
				At[i][k] *= s;
			}
		}		
	}
}

