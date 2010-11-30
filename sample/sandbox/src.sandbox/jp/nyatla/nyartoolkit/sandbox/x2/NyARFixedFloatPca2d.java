/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.sandbox.x2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.pca2d.*;
/**
 * 64bit(小数部16bit)の固定小数点を利用したPCA関数
 *
 */
public class NyARFixedFloatPca2d implements INyARPca2d
{
//	private static final double PCA_EPS = 1e-6; // #define EPS 1e-6

	private static final int PCA_MAX_ITER = 100; // #define MAX_ITER 100
	/**
	 * static int QRM( ARMat *a, ARVec *dv )の代替関数
	 * 
	 * @param a
	 * @param dv
	 * @throws NyARException
	 */
	private static void PCA_QRM(NyARI64Matrix22 o_matrix, NyARI64Point2d dv) throws NyARException
	{
		long abs_x;
		long w, t, s, x, y, c;
		long ev1;
		long dv_x,dv_y;
		long mat00,mat01,mat10,mat11;
		// <this.vecTridiagonalize2d(i_mat, dv, ev);>
		dv_x =o_matrix.m00;// this.m[dim - 2][dim - 2];// d.v[dim-2]=a.m[dim-2][dim-2];//d->v[dim-2]=a->m[(dim-2)*dim+(dim-2)];
		ev1  =o_matrix.m01;// this.m[dim - 2][dim - 1];// e.v[dim-2+i_e_start]=a.m[dim-2][dim-1];//e->v[dim-2] = a->m[(dim-2)*dim+(dim-1)];
		dv_y =o_matrix.m11;// this.m[dim - 1][dim - 1];// d.v[dim-1]=a_array[dim-1][dim-1];//d->v[dim-1] =a->m[(dim-1)*dim+(dim-1)];
		// 単位行列にする。
		mat00 = mat11 = (1<<16);
		mat01 = mat10 = 0;
		// </this.vecTridiagonalize2d(i_mat, dv, ev);>

		// int j = 1;
		// // while(j>0 && fabs(ev->v[j])>EPS*(fabs(dv->v[j-1])+fabs(dv->v[j])))
		// while (j > 0 && Math.abs(ev1) > PCA_EPS * (Math.abs(dv.x) + Math.abs(dv.y))) {
		// j--;
		// }
		// if (j == 0) {
		int iter = 0;
		do {
			iter++;
			if (iter > PCA_MAX_ITER) {
				break;
			}
			w = (dv_x - dv_y) / 2L;//S16
			t = (ev1 * ev1)>>16;// t = ev->v[h] * ev->v[h];
			s = NyMath.sqrtFixdFloat16(((w * w)>>16) + t);
			if (w < 0) {
				s = -s;
			}
			x = dv_x - dv_y + (t<<16) / (w + s);// x = dv->v[j] -dv->v[h] +t/(w+s);
			y = ev1;// y = ev->v[j+1];

			abs_x=(x>0?x:-x);
			if (abs_x >= (y>0?y:-y)) { //if (Math.abs(x) >= Math.abs(y)) {
				if ((abs_x>>16) > 0) {
					t = -(y<<16) / x;
					c = (1L<<32) / NyMath.sqrtFixdFloat16(((t * t)>>16) + (1L<<16));
					s = (t * c)>>16;
				} else {
					c = (1L<<16);
					s = 0;
				}
			} else {
				t = -(x<<16) / y;
				s = (1L<<32) / NyMath.sqrtFixdFloat16(((t * t)>>16) + (1L<<16));
				c = (t * s)>>16;
			}
			w = dv_x - dv_y;// w = dv->v[k] -dv->v[k+1];
			t = (((w * s + 2L * c * ev1)>>16) * s)>>16;// t = (w * s +2 * c *ev->v[k+1]) *s;
			dv_x -= t;// dv->v[k] -= t;
			dv_y += t;// dv->v[k+1] += t;
			ev1 += (s * ((c * w - 2L * s * ev1)>>16)>>16);// ev->v[k+1]+= s * (c* w- 2* s *ev->v[k+1]);

			x = mat00;// x = a->m[k*dim+i];
			y = mat10;// y = a->m[(k+1)*dim+i];
			mat00 = (c * x - s * y)>>16;// a->m[k*dim+i] = c * x - s* y;
			mat10 = (s * x + c * y)>>16;// a->m[(k+1)*dim+i] = s* x + c * y;
			
			x = mat01;// x = a->m[k*dim+i];
			y = mat11;// y = a->m[(k+1)*dim+i];
			mat01 = (c * x - s * y)>>16;// a->m[k*dim+i] = c * x - s* y;
			mat11 = (s * x + c * y)>>16;// a->m[(k+1)*dim+i] = s* x + c * y;
		} while (((ev1>0?ev1:-ev1)>>16) > ((((dv_x>0?dv_x:-dv_x) + (dv_y>0?dv_y:-dv_y))>>16)/1000000L));
		// }

		t = dv_x;// t = dv->v[h];
		if (dv_y > t) {// if( dv->v[i] > t ) {
			t = dv_y;// t = dv->v[h];
			dv_y = dv_x;// dv->v[h] = dv->v[k];
			dv_x = t;// dv->v[k] = t;
			// 行の入れ替え
			o_matrix.m00 = mat10;
			o_matrix.m01 = mat11;
			o_matrix.m10 = mat00;		
			o_matrix.m11 = mat01;
			
		} else {
			// 行の入れ替えはなし
			o_matrix.m00 = mat00;
			o_matrix.m01 = mat01;
			o_matrix.m10 = mat10;		
			o_matrix.m11 = mat11;
		}
		dv.x=dv_x;
		dv.y=dv_y;
		return;	
	}

	/**
	 * static int PCA( ARMat *input, ARMat *output, ARVec *ev )
	 * 
	 * @param output
	 * @param o_ev
	 * @throws NyARException
	 */
	/**
	 * static int PCA( ARMat *input, ARMat *output, ARVec *ev )
	 * 
	 * @param output
	 * @param o_ev
	 * @throws NyARException
	 */
	private void PCA_PCA(int[] i_x,int[] i_y,int i_number_of_data,NyARI64Matrix22 o_matrix, NyARI64Point2d o_ev,NyARI64Point2d o_mean) throws NyARException
	{
		// double[] mean_array=mean.getArray();
		// mean.zeroClear();

		//PCA_EXの処理
		long sx = 0;
		long sy = 0;
		for (int i = 0; i < i_number_of_data; i++) {
			sx += i_x[i];//S16
			sy += i_y[i];//S16
		}
		sx = sx / i_number_of_data;//S16
		sy = sy / i_number_of_data;//S16
		
		//PCA_CENTERとPCA_xt_by_xを一緒に処理
		final long srow = NyMath.sqrtFixdFloat16((long)i_number_of_data<<16);//S16
		long w00, w11, w10;
		w00 = w11 = w10 = 0L;// *out = 0.0;
		for (int i = 0; i < i_number_of_data; i++) {
			final long x = ((i_x[i] - sx)<<16) / srow;//S6
			final long y = ((i_y[i] - sy)<<16) / srow;//S6
			w00 += (x * x)>>16;//S16
			w10 += (x * y)>>16;//S16
			w11 += (y * y)>>16;//S16
		}
		o_matrix.m00=w00;
		o_matrix.m01=o_matrix.m10=w10;
		o_matrix.m11=w11;
		
		//PCA_PCAの処理
		PCA_QRM(o_matrix, o_ev);
		// m2 = o_output.m;// m2 = output->m;
		if (o_ev.x < 0) {// if( ev->v[i] < VZERO ){
			o_ev.x = 0;// ev->v[i] = 0.0;
			o_matrix.m00 = 0;// *(m2++) = 0.0;
			o_matrix.m01 = 0;// *(m2++) = 0.0;
		}

		if (o_ev.y < 0) {// if( ev->v[i] < VZERO ){
			o_ev.y = 0;// ev->v[i] = 0.0;
			o_matrix.m10 = 0;// *(m2++) = 0.0;
			o_matrix.m11 = 0;// *(m2++) = 0.0;
		}
		o_mean.x=sx;
		o_mean.y=sy;
		// }
		return;
	}
	private int[] __pca_tmpx=null;
	private int[] __pca_tmpy=null;
	private NyARI64Matrix22 __pca_evec=null;
	private NyARI64Point2d __pca_ev=null;
	private NyARI64Point2d __pca_mean=null;
	public void pca(double[] i_x,double[] i_y,int i_number_of_point,NyARDoubleMatrix22 o_evec, NyARDoublePoint2d o_ev,NyARDoublePoint2d o_mean) throws NyARException
	{
		//変換用のワーク変数作成
		if(__pca_tmpx==null)
		{
			this.__pca_tmpx=new int[i_number_of_point];
			this.__pca_tmpy=new int[i_number_of_point];
			this.__pca_evec=new NyARI64Matrix22 ();
			this.__pca_ev=new NyARI64Point2d ();
			this.__pca_mean=new NyARI64Point2d ();
		}else if(i_number_of_point>this.__pca_tmpx.length)
		{
			this.__pca_tmpx=new int[i_number_of_point];
			this.__pca_tmpy=new int[i_number_of_point];
		}
		//値のセット
		final int[] x_ptr=this.__pca_tmpx;
		final int[] y_ptr=this.__pca_tmpy;
		for(int i=0;i<i_number_of_point;i++)
		{
			x_ptr[i]=(int)(i_x[i]*65536L);
			y_ptr[i]=(int)(i_y[i]*65536L);
		}
		//計算
		pcaF16(x_ptr,y_ptr,i_number_of_point,this.__pca_evec,this.__pca_ev,this.__pca_mean);
		//結果値を変換
		o_evec.m00=(double)this.__pca_evec.m00/65536.0;
		o_evec.m01=(double)this.__pca_evec.m01/65536.0;
		o_evec.m10=(double)this.__pca_evec.m10/65536.0;
		o_evec.m11=(double)this.__pca_evec.m11/65536.0;
		o_ev.x=this.__pca_ev.x/65536.0;
		o_ev.y=this.__pca_ev.y/65536.0;
		o_mean.x=this.__pca_mean.x/65536.0;
		o_mean.y=this.__pca_mean.y/65536.0;
		return;	
	}
	/**
	 * 値は全て小数点部16bitの固定小数点です。
	 * @param i_x
	 * @param i_y
	 * @param i_number_of_point
	 * @param o_evec
	 * @param o_ev
	 * @param o_mean
	 * @throws NyARException
	 */
	public void pcaF16(int[] i_x,int[] i_y,int i_number_of_point,NyARI64Matrix22 o_evec, NyARI64Point2d o_ev,NyARI64Point2d o_mean) throws NyARException
	{
		//計算
		PCA_PCA(i_x,i_y,i_number_of_point,o_evec,o_ev,o_mean);
		final long sum = o_ev.x + o_ev.y;
		// For順変更禁止
		o_ev.x = (o_ev.x<<16)/sum;// ev->v[i] /= sum;
		o_ev.y = (o_ev.y<<16)/sum;// ev->v[i] /= sum;
		return;	
	}	
}
