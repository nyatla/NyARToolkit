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
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARFixedFloat24Matrix33;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point2d;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point3d;



/**
 * NyARRotMatrix_NyARToolKitのFixedFloat版です。
 * 
 */
public class NyARFixedFloatRotMatrix extends NyARFixedFloat24Matrix33
{
	private static int DIV0CANCEL=1;
	/**
	 * インスタンスを準備します。
	 * 
	 * @param i_param
	 */
	public NyARFixedFloatRotMatrix(NyARPerspectiveProjectionMatrix i_matrix) throws NyARException
	{
		this.__initRot_vec1 = new NyARFixedFloatRotVector(i_matrix);
		this.__initRot_vec2 = new NyARFixedFloatRotVector(i_matrix);
		return;
	}

	final private NyARFixedFloatRotVector __initRot_vec1;
	final private NyARFixedFloatRotVector __initRot_vec2;
	final private NyARFixedFloat16Point3d _angle=new NyARFixedFloat16Point3d();

	public final void initRotByPrevResult(NyARTransMatResult i_prev_result)
	{
		this.m00 =(long)(i_prev_result.m00*0x1000000);
		this.m01 =(long)(i_prev_result.m01*0x1000000);
		this.m02 =(long)(i_prev_result.m02*0x1000000);

		this.m10 =(long)(i_prev_result.m10*0x1000000);
		this.m11 =(long)(i_prev_result.m11*0x1000000);
		this.m12 =(long)(i_prev_result.m12*0x1000000);

		this.m20 =(long)(i_prev_result.m20*0x1000000);
		this.m21 =(long)(i_prev_result.m21*0x1000000);
		this.m22 =(long)(i_prev_result.m22*0x1000000);
		return;
	}

	public final void initRotBySquare(final NyARLinear[] i_linear, final NyARFixedFloat16Point2d[] i_sqvertex) throws NyARException
	{
		final NyARFixedFloatRotVector vec1 = this.__initRot_vec1;
		final NyARFixedFloatRotVector vec2 = this.__initRot_vec2;

		// 向かい合った辺から、２本のベクトルを計算

		// 軸１
		vec1.exteriorProductFromLinear(i_linear[0], i_linear[2]);
		vec1.checkVectorByVertex(i_sqvertex[0], i_sqvertex[1]);

		// 軸２
		vec2.exteriorProductFromLinear(i_linear[1], i_linear[3]);
		vec2.checkVectorByVertex(i_sqvertex[3], i_sqvertex[0]);

		// 回転の最適化？
		NyARFixedFloatRotVector.checkRotation(vec1, vec2);
	

		this.m00 = vec1.v1<<8;
		this.m10 = vec1.v2<<8;
		this.m20 = vec1.v3<<8;
		this.m01 = vec2.v1<<8;
		this.m11 = vec2.v2<<8;
		this.m21 = vec2.v3<<8;
		


		// 最後の軸を計算
		final long w02 = (vec1.v2 * vec2.v3 - vec1.v3 * vec2.v2)>>16;//S16
		final long w12 = (vec1.v3 * vec2.v1 - vec1.v1 * vec2.v3)>>16;//S16
		final long w22 = (vec1.v1 * vec2.v2 - vec1.v2 * vec2.v1)>>16;//S16
		final long w = NyMath.sqrtFixdFloat((w02 * w02 + w12 * w12 + w22 * w22)>>16,16);//S16
		this.m02 = (w02<<24) / w;
		this.m12 = (w12<<24) / w;
		this.m22 = (w22<<24) / w;
        //Matrixからangleをロード
        this.updateAngleFromMatrix();
		
		return;
	}
    public NyARFixedFloat16Point3d refAngle()
    {
        return this._angle;
    }
	/**
	 * int arGetAngle( double rot[3][3], double *wa, double *wb, double *wc ) Optimize:2008.04.20:STEP[481→433] 3x3変換行列から、回転角を復元して返します。
	 * 
	 * @param o_angle
	 * @return
	 */
	private final void updateAngleFromMatrix()
	{
		int a, b, c;
		long sina, cosa, sinb, cosb, sinc, cosc;

		if (this.m22 > NyMath.FIXEDFLOAT24_1) {// <Optimize/>if( rot[2][2] > 1.0 ) {
			this.m22 = NyMath.FIXEDFLOAT24_1;// <Optimize/>rot[2][2] = 1.0;
		} else if (this.m22 < -NyMath.FIXEDFLOAT24_1) {// <Optimize/>}else if( rot[2][2] < -1.0 ) {
			this.m22 = -NyMath.FIXEDFLOAT24_1;// <Optimize/>rot[2][2] = -1.0;
		}
		cosb = this.m22;// <Optimize/>cosb = rot[2][2];
		b=NyMath.acosFixedFloat16((int)cosb);
		sinb = (long)NyMath.sinFixedFloat24(b);
		final long rot02 = this.m02;
		final long rot12 = this.m12;
		if (sinb!=0) {
			cosa = (rot02<<24) / sinb;// <Optimize/>cosa = rot[0][2] / sinb;
			sina = (rot12<<24) / sinb;// <Optimize/>sina = rot[1][2] / sinb;
			if (cosa > NyMath.FIXEDFLOAT24_1) {
				/* printf("cos(alph) = %f\n", cosa); */
				cosa = NyMath.FIXEDFLOAT24_1;
				sina = 0;
			}
			if (cosa < -NyMath.FIXEDFLOAT24_1) {
				/* printf("cos(alph) = %f\n", cosa); */
				cosa = -NyMath.FIXEDFLOAT24_1;
				sina = 0;
			}
			if (sina > NyMath.FIXEDFLOAT24_1) {
				/* printf("sin(alph) = %f\n", sina); */
				sina = NyMath.FIXEDFLOAT24_1;
				cosa = 0;
			}
			if (sina < -NyMath.FIXEDFLOAT24_1) {
				/* printf("sin(alph) = %f\n", sina); */
				sina = -NyMath.FIXEDFLOAT24_1;
				cosa = 0;
			}
			a = NyMath.acosFixedFloat16((int)cosa);
			if (sina < 0) {
				a = -a;
			}
			// <Optimize>
			// sinc = (rot[2][1]*rot[0][2]-rot[2][0]*rot[1][2])/(rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
			// cosc = -(rot[0][2]*rot[2][0]+rot[1][2]*rot[2][1])/(rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
			final long tmp = DIV0CANCEL+((rot02 * rot02 + rot12 * rot12)>>24);
			sinc = (this.m21 * rot02 - this.m20 * rot12) / tmp;
			cosc = -(rot02 * this.m20 + rot12 * this.m21) / tmp;
			// </Optimize>

			if (cosc > NyMath.FIXEDFLOAT24_1){
				/* printf("cos(r) = %f\n", cosc); */
				cosc = NyMath.FIXEDFLOAT24_1;
				sinc = 0;
			}
			if (cosc < -NyMath.FIXEDFLOAT24_1) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = -NyMath.FIXEDFLOAT24_1;
				sinc = 0;
			}
			if (sinc > NyMath.FIXEDFLOAT24_1) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = NyMath.FIXEDFLOAT24_1;
				cosc = 0;
			}
			if (sinc < -NyMath.FIXEDFLOAT24_1) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = -NyMath.FIXEDFLOAT24_1;
				cosc = 0;
			}
			c = (int)NyMath.acosFixedFloat16((int)cosc);
			if (sinc < 0) {
				c = -c;
			}
		} else {
			a = b = 0;
			cosa = cosb = NyMath.FIXEDFLOAT24_1;
			sina = sinb = 0;
			cosc = this.m00;// cosc = rot[0];// <Optimize/>cosc = rot[0][0];
			sinc = this.m01;// sinc = rot[1];// <Optimize/>sinc = rot[1][0];
			if (cosc > NyMath.FIXEDFLOAT24_1) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = NyMath.FIXEDFLOAT24_1;
				sinc = 0;
			}
			if (cosc < -NyMath.FIXEDFLOAT24_1) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = -NyMath.FIXEDFLOAT24_1;
				sinc = 0;
			}
			if (sinc > NyMath.FIXEDFLOAT24_1) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = NyMath.FIXEDFLOAT24_1;
				cosc = 0;
			}
			if (sinc < -NyMath.FIXEDFLOAT24_1) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = -NyMath.FIXEDFLOAT24_1;
				cosc = 0;
			}
			c = NyMath.acosFixedFloat16((int)cosc);
			if (sinc < 0) {
				c = -c;
			}
		}
		this._angle.x = (long)a;// wa.value=a;//*wa = a;
		this._angle.y = (long)b;// wb.value=b;//*wb = b;
		this._angle.z = (long)c;// wc.value=c;//*wc = c;
		return;
	}
	public final void setAngle(int i_x, int i_y, int i_z)
	{
		/*
		 * |cos(a) -sin(a) 0| |cos(b) 0 sin(b)| |cos(a-c) sin(a-c) 0| rot = |sin(a) cos(a) 0| |0 1 0 | |-sin(a-c) cos(a-c) 0| |0 0 1| |-sin(b) 0 cos(b)| |0 0 1|
		 */

		long Sa, Sb, Ca, Cb, Sac, Cac, CaCb, SaCb;
		Sa = NyMath.sinFixedFloat24(i_x);
		Ca = NyMath.cosFixedFloat24(i_x);
		Sb = NyMath.sinFixedFloat24(i_y);
		Cb = NyMath.cosFixedFloat24(i_y);
		Sac = NyMath.sinFixedFloat24(i_x - i_z);
		Cac = NyMath.cosFixedFloat24(i_x - i_z);
		CaCb =(Ca * Cb)>>24;
		SaCb =(Sa * Cb)>>24;

		this.m00 =(CaCb * Cac + Sa * Sac)>>24;
		this.m01 =(CaCb * Sac - Sa * Cac)>>24;
		this.m02 =(Ca * Sb)>>24;
		this.m10 =(SaCb * Cac - Ca * Sac)>>24;
		this.m11 =(SaCb * Sac + Ca * Cac)>>24;
		this.m12 =(Sa * Sb)>>24;
		this.m20 =(-Sb * Cac)>>24;
		this.m21 =(-Sb * Sac)>>24;
		this.m22 =Cb;
		//angleを逆計算せずに直接代入
		this._angle.x=i_x;
		this._angle.y=i_y;
		this._angle.z=i_z;		
		return;
	}
	/**
	 * i_in_pointを変換行列で座標変換する。
	 * 
	 * @param i_in_point
	 * @param i_out_point
	 */
	public final void getPoint3d(final NyARFixedFloat16Point3d i_in_point, final NyARFixedFloat16Point3d i_out_point)
	{
		i_out_point.x = (this.m00 * i_in_point.x + this.m01 * i_in_point.y + this.m02 * i_in_point.z)>>24;
		i_out_point.y = (this.m10 * i_in_point.x + this.m11 * i_in_point.y + this.m12 * i_in_point.z)>>24;
		i_out_point.z = (this.m20 * i_in_point.x + this.m21 * i_in_point.y + this.m22 * i_in_point.z)>>24;
		return;
	}

	/**
	 * 複数の頂点を一括して変換する
	 * 
	 * @param i_in_point
	 * @param i_out_point
	 * @param i_number_of_vertex
	 */
	public final void getPoint3dBatch(final NyARFixedFloat16Point3d[] i_in_point, NyARFixedFloat16Point3d[] i_out_point, int i_number_of_vertex)
	{
		for (int i = i_number_of_vertex - 1; i >= 0; i--) {
			final NyARFixedFloat16Point3d out_ptr = i_out_point[i];
			final NyARFixedFloat16Point3d in_ptr = i_in_point[i];
			out_ptr.x =(this.m00 * in_ptr.x + this.m01 * in_ptr.y + this.m02 * in_ptr.z)>>24;
			out_ptr.y =(this.m10 * in_ptr.x + this.m11 * in_ptr.y + this.m12 * in_ptr.z)>>24;
			out_ptr.z =(this.m20 * in_ptr.x + this.m21 * in_ptr.y + this.m22 * in_ptr.z)>>24;
		}
		return;
	}
}
