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
 * Copyright (C)2008-2009 R.Iizuka
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
package jp.nyatla.nyartoolkit.core.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix22;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class NyARDoubleMatrixProcessor
{
	/**
	 * i_srcの逆行列を計算して、thisへ格納します。
	 * @param i_src
	 * @return
	 */
	public static boolean inverse(NyARDoubleMatrix22 i_src,NyARDoubleMatrix22 o_dest)
	{
		final double a11,a12,a21,a22;
		a11=i_src.m00;
		a12=i_src.m01;
		a21=i_src.m10;
		a22=i_src.m11;
		double det=a11*a22-a12*a21;
		if(det==0){
			return false;
		}
		det=1/det;
		o_dest.m00=a22*det;
		o_dest.m01=-a12*det;
		o_dest.m10=a21*det;
		o_dest.m11=-a11*det;
		return true;
	}	
	public static boolean inverse(NyARDoubleMatrix33 i_src,NyARDoubleMatrix33 o_dest) throws NyARException
	{
		/*i_srcの逆行列をthisへ格納するコードを書くこと。*/
		NyARException.notImplement();
		return false;
	}
	/**
	 * i_srcの逆行列を計算して、結果をo_destへセットします。
	 * @param i_src
	 * @return
	 */
	public static boolean inverse(NyARDoubleMatrix44 i_src,NyARDoubleMatrix44 o_dest)
	{
		final double a11,a12,a13,a14,a21,a22,a23,a24,a31,a32,a33,a34,a41,a42,a43,a44;
		final double b11,b12,b13,b14,b21,b22,b23,b24,b31,b32,b33,b34,b41,b42,b43,b44;	
		double t1,t2,t3,t4,t5,t6;
		a11=i_src.m00;a12=i_src.m01;a13=i_src.m02;a14=i_src.m03;
		a21=i_src.m10;a22=i_src.m11;a23=i_src.m12;a24=i_src.m13;
		a31=i_src.m20;a32=i_src.m21;a33=i_src.m22;a34=i_src.m23;
		a41=i_src.m30;a42=i_src.m31;a43=i_src.m32;a44=i_src.m33;
		
		t1=a33*a44-a34*a43;
		t2=a34*a42-a32*a44;
		t3=a32*a43-a33*a42;
		t4=a34*a41-a31*a44;
		t5=a31*a43-a33*a41;
		t6=a31*a42-a32*a41;
		
		b11=a22*t1+a23*t2+a24*t3;
		b21=-(a23*t4+a24*t5+a21*t1);
		b31=a24*t6-a21*t2+a22*t4;
		b41=-(a21*t3-a22*t5+a23*t6);
		
		t1=a43*a14-a44*a13;
		t2=a44*a12-a42*a14;
		t3=a42*a13-a43*a12;
		t4=a44*a11-a41*a14;
		t5=a41*a13-a43*a11;
		t6=a41*a12-a42*a11;

		b12=-(a32*t1+a33*t2+a34*t3);
		b22=a33*t4+a34*t5+a31*t1;
		b32=-(a34*t6-a31*t2+a32*t4);
		b42=a31*t3-a32*t5+a33*t6;
		
		t1=a13*a24-a14*a23;
		t2=a14*a22-a12*a24;
		t3=a12*a23-a13*a22;
		t4=a14*a21-a11*a24;
		t5=a11*a23-a13*a21;
		t6=a11*a22-a12*a21;

		b13=a42*t1+a43*t2+a44*t3;
		b23=-(a43*t4+a44*t5+a41*t1);
		b33=a44*t6-a41*t2+a42*t4;
		b43=-(a41*t3-a42*t5+a43*t6);

		t1=a23*a34-a24*a33;
		t2=a24*a32-a22*a34;
		t3=a22*a33-a23*a32;
		t4=a24*a31-a21*a34;		
		t5=a21*a33-a23*a31;
		t6=a21*a32-a22*a31;

		b14=-(a12*t1+a13*t2+a14*t3);
		b24=a13*t4+a14*t5+a11*t1;
		b34=-(a14*t6-a11*t2+a12*t4);
		b44=a11*t3-a12*t5+a13*t6;
		
		double det_1=(a11*b11+a21*b12+a31*b13+a41*b14);
		if(det_1==0){
			return false;
		}
		det_1=1/det_1;

		o_dest.m00=b11*det_1;
		o_dest.m01=b12*det_1;
		o_dest.m02=b13*det_1;
		o_dest.m03=b14*det_1;
		
		o_dest.m10=b21*det_1;
		o_dest.m11=b22*det_1;
		o_dest.m12=b23*det_1;
		o_dest.m13=b24*det_1;
		
		o_dest.m20=b31*det_1;
		o_dest.m21=b32*det_1;
		o_dest.m22=b33*det_1;
		o_dest.m23=b34*det_1;
		
		o_dest.m30=b41*det_1;
		o_dest.m31=b42*det_1;
		o_dest.m32=b43*det_1;
		o_dest.m33=b44*det_1;
		
		return true;
	}
}
