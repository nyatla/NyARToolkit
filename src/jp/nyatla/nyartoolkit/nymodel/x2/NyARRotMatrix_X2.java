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
package jp.nyatla.nyartoolkit.nymodel.x2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*;
import jp.nyatla.nyartoolkit.core.param.*;



/**
 * 回転行列計算用の、3x3行列
 * 
 */
public class NyARRotMatrix_X2 extends NyARRotMatrix
{
	private NyARSinTable _sin_table_ref;

	/**
	 * インスタンスを準備します。
	 * 
	 * @param i_param
	 */
	public NyARRotMatrix_X2(NyARPerspectiveProjectionMatrix i_matrix,NyARSinTable i_sin_table_ref) throws NyARException
	{
		this.__initRot_vec1 = new NyARRotVector(i_matrix);
		this.__initRot_vec2 = new NyARRotVector(i_matrix);
		this._sin_table_ref=i_sin_table_ref;
		return;
	}

	final private NyARRotVector __initRot_vec1;

	final private NyARRotVector __initRot_vec2;

	public final void initRotByPrevResult(NyARTransMatResult i_prev_result)
	{

		this.m00 = i_prev_result.m00;
		this.m01 = i_prev_result.m01;
		this.m02 = i_prev_result.m02;

		this.m10 = i_prev_result.m10;
		this.m11 = i_prev_result.m11;
		this.m12 = i_prev_result.m12;

		this.m20 = i_prev_result.m20;
		this.m21 = i_prev_result.m21;
		this.m22 = i_prev_result.m22;
		return;
	}

	public final void initRotBySquare(final NyARLinear[] i_linear, final NyARDoublePoint2d[] i_sqvertex) throws NyARException
	{
		final NyARRotVector vec1 = this.__initRot_vec1;
		final NyARRotVector vec2 = this.__initRot_vec2;

		// 向かい合った辺から、２本のベクトルを計算

		// 軸１
		vec1.exteriorProductFromLinear(i_linear[0], i_linear[2]);
		vec1.checkVectorByVertex(i_sqvertex[0], i_sqvertex[1]);

		// 軸２
		vec2.exteriorProductFromLinear(i_linear[1], i_linear[3]);
		vec2.checkVectorByVertex(i_sqvertex[3], i_sqvertex[0]);

		// 回転の最適化？
		NyARRotVector.checkRotation(vec1, vec2);

		this.m00 = vec1.v1;
		this.m10 = vec1.v2;
		this.m20 = vec1.v3;
		this.m01 = vec2.v1;
		this.m11 = vec2.v2;
		this.m21 = vec2.v3;

		// 最後の軸を計算
		final double w02 = vec1.v2 * vec2.v3 - vec1.v3 * vec2.v2;
		final double w12 = vec1.v3 * vec2.v1 - vec1.v1 * vec2.v3;
		final double w22 = vec1.v1 * vec2.v2 - vec1.v2 * vec2.v1;
		final double w = Math.sqrt(w02 * w02 + w12 * w12 + w22 * w22);
		this.m02 = w02 / w;
		this.m12 = w12 / w;
		this.m22 = w22 / w;
		return;
	}

	/**
	 * int arGetAngle( double rot[3][3], double *wa, double *wb, double *wc ) Optimize:2008.04.20:STEP[481→433] 3x3変換行列から、回転角を復元して返します。
	 * 
	 * @param o_angle
	 * @return
	 */
	public final void getAngle(final NyARDoublePoint3d o_angle)
	{
		double a, b, c;
		double sina, cosa, sinb, cosb, sinc, cosc;

		if (this.m22 > 1.0) {// <Optimize/>if( rot[2][2] > 1.0 ) {
			this.m22 = 1.0;// <Optimize/>rot[2][2] = 1.0;
		} else if (this.m22 < -1.0) {// <Optimize/>}else if( rot[2][2] < -1.0 ) {
			this.m22 = -1.0;// <Optimize/>rot[2][2] = -1.0;
		}
		cosb = this.m22;// <Optimize/>cosb = rot[2][2];
		b = Math.acos(cosb);
		sinb = this._sin_table_ref.sin(b);
		final double rot02 = this.m02;
		final double rot12 = this.m12;
		if (b >= 0.000001 || b <= -0.000001) {
			cosa = rot02 / sinb;// <Optimize/>cosa = rot[0][2] / sinb;
			sina = rot12 / sinb;// <Optimize/>sina = rot[1][2] / sinb;
			if (cosa > 1.0) {
				/* printf("cos(alph) = %f\n", cosa); */
				cosa = 1.0;
				sina = 0.0;
			}
			if (cosa < -1.0) {
				/* printf("cos(alph) = %f\n", cosa); */
				cosa = -1.0;
				sina = 0.0;
			}
			if (sina > 1.0) {
				/* printf("sin(alph) = %f\n", sina); */
				sina = 1.0;
				cosa = 0.0;
			}
			if (sina < -1.0) {
				/* printf("sin(alph) = %f\n", sina); */
				sina = -1.0;
				cosa = 0.0;
			}
			a = Math.acos(cosa);
			if (sina < 0) {
				a = -a;
			}
			// <Optimize>
			// sinc = (rot[2][1]*rot[0][2]-rot[2][0]*rot[1][2])/(rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
			// cosc = -(rot[0][2]*rot[2][0]+rot[1][2]*rot[2][1])/(rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
			final double tmp = (rot02 * rot02 + rot12 * rot12);
			sinc = (this.m21 * rot02 - this.m20 * rot12) / tmp;
			cosc = -(rot02 * this.m20 + rot12 * this.m21) / tmp;
			// </Optimize>

			if (cosc > 1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = 1.0;
				sinc = 0.0;
			}
			if (cosc < -1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = -1.0;
				sinc = 0.0;
			}
			if (sinc > 1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = 1.0;
				cosc = 0.0;
			}
			if (sinc < -1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = -1.0;
				cosc = 0.0;
			}
			c = Math.acos(cosc);
			if (sinc < 0) {
				c = -c;
			}
		} else {
			a = b = 0.0;
			cosa = cosb = 1.0;
			sina = sinb = 0.0;
			cosc = this.m00;// cosc = rot[0];// <Optimize/>cosc = rot[0][0];
			sinc = this.m01;// sinc = rot[1];// <Optimize/>sinc = rot[1][0];
			if (cosc > 1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = 1.0;
				sinc = 0.0;
			}
			if (cosc < -1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = -1.0;
				sinc = 0.0;
			}
			if (sinc > 1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = 1.0;
				cosc = 0.0;
			}
			if (sinc < -1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = -1.0;
				cosc = 0.0;
			}
			c = Math.acos(cosc);
			if (sinc < 0) {
				c = -c;
			}
		}
		o_angle.x = a;// wa.value=a;//*wa = a;
		o_angle.y = b;// wb.value=b;//*wb = b;
		o_angle.z = c;// wc.value=c;//*wc = c;
		return;
	}

	/**
	 * 回転角から回転行列を計算してセットします。
	 * 
	 * @param i_x
	 * @param i_y
	 * @param i_z
	 */
	public final void setAngle(final double i_x, final double i_y, final double i_z)
	{
		/*
		 * |cos(a) -sin(a) 0| |cos(b) 0 sin(b)| |cos(a-c) sin(a-c) 0| rot = |sin(a) cos(a) 0| |0 1 0 | |-sin(a-c) cos(a-c) 0| |0 0 1| |-sin(b) 0 cos(b)| |0 0 1|
		 */

		double Sa, Sb, Ca, Cb, Sac, Cac, CaCb, SaCb;
		Sa = this._sin_table_ref.sin(i_x);
		Ca = this._sin_table_ref.cos(i_x);
		Sb = this._sin_table_ref.sin(i_y);
		Cb = this._sin_table_ref.cos(i_y);
		Sac = this._sin_table_ref.sin(i_x - i_z);
		Cac = this._sin_table_ref.cos(i_x - i_z);
		CaCb = Ca * Cb;
		SaCb = Sa * Cb;

		this.m00 = CaCb * Cac + Sa * Sac;
		this.m01 = CaCb * Sac - Sa * Cac;
		this.m02 = Ca * Sb;
		this.m10 = SaCb * Cac - Ca * Sac;
		this.m11 = SaCb * Sac + Ca * Cac;
		this.m12 = Sa * Sb;
		this.m20 = -Sb * Cac;
		this.m21 = -Sb * Sac;
		this.m22 = Cb;

		return;
	}

	/**
	 * i_in_pointを変換行列で座標変換する。
	 * 
	 * @param i_in_point
	 * @param i_out_point
	 */
	public final void getPoint3d(final NyARDoublePoint3d i_in_point, final NyARDoublePoint3d i_out_point)
	{
		final double x = i_in_point.x;
		final double y = i_in_point.y;
		final double z = i_in_point.z;
		i_out_point.x = this.m00 * x + this.m01 * y + this.m02 * z;
		i_out_point.y = this.m10 * x + this.m11 * y + this.m12 * z;
		i_out_point.z = this.m20 * x + this.m21 * y + this.m22 * z;
		return;
	}

	/**
	 * 複数の頂点を一括して変換する
	 * 
	 * @param i_in_point
	 * @param i_out_point
	 * @param i_number_of_vertex
	 */
	public final void getPoint3dBatch(final NyARDoublePoint3d[] i_in_point, NyARDoublePoint3d[] i_out_point, int i_number_of_vertex)
	{
		for (int i = i_number_of_vertex - 1; i >= 0; i--) {
			final NyARDoublePoint3d out_ptr = i_out_point[i];
			final NyARDoublePoint3d in_ptr = i_in_point[i];
			final double x = in_ptr.x;
			final double y = in_ptr.y;
			final double z = in_ptr.z;
			out_ptr.x = this.m00 * x + this.m01 * y + this.m02 * z;
			out_ptr.y = this.m10 * x + this.m11 * y + this.m12 * z;
			out_ptr.z = this.m20 * x + this.m21 * y + this.m22 * z;
		}
		return;
	}
}
