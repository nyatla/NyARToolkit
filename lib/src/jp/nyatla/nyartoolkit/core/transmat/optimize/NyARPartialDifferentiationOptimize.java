/* 
 * PROJECT: NyARToolkit (Extension)
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.transmat.optimize;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.*;

import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.utils.*;

class TSinCosValue{
	public double cos_val;
	public double sin_val;
	public static TSinCosValue[] createArray(int i_size)
	{
		TSinCosValue[] result=new TSinCosValue[i_size];
		for(int i=0;i<i_size;i++){
			result[i]=new TSinCosValue();
		}
		return result;
	}
}

/**
 * 基本姿勢と実画像を一致するように、角度を微調整→平行移動量を再計算 を繰り返して、変換行列を最適化する。
 * 
 */
public class NyARPartialDifferentiationOptimize
{
	private final NyARPerspectiveProjectionMatrix _projection_mat_ref;

	public NyARPartialDifferentiationOptimize(NyARPerspectiveProjectionMatrix i_projection_mat_ref)
	{
		this._projection_mat_ref = i_projection_mat_ref;
		return;
	}

	public final void sincos2Rotation_ZXY(TSinCosValue[] i_sincos, NyARDoubleMatrix33 i_rot_matrix)
	{
		final double sina = i_sincos[0].sin_val;
		final double cosa = i_sincos[0].cos_val;
		final double sinb = i_sincos[1].sin_val;
		final double cosb = i_sincos[1].cos_val;
		final double sinc = i_sincos[2].sin_val;
		final double cosc = i_sincos[2].cos_val;
		i_rot_matrix.m00 = cosc * cosb - sinc * sina * sinb;
		i_rot_matrix.m01 = -sinc * cosa;
		i_rot_matrix.m02 = cosc * sinb + sinc * sina * cosb;
		i_rot_matrix.m10 = sinc * cosb + cosc * sina * sinb;
		i_rot_matrix.m11 = cosc * cosa;
		i_rot_matrix.m12 = sinc * sinb - cosc * sina * cosb;
		i_rot_matrix.m20 = -cosa * sinb;
		i_rot_matrix.m21 = sina;
		i_rot_matrix.m22 = cosb * cosa;
	}

	private final void rotation2Sincos_ZXY(NyARDoubleMatrix33 i_rot_matrix, TSinCosValue[] o_out,NyARDoublePoint3d o_ang)
	{
		double x, y, z;
		double sina = i_rot_matrix.m21;
		if (sina >= 1.0) {
			x = Math.PI / 2;
			y = 0;
			z = Math.atan2(-i_rot_matrix.m10, i_rot_matrix.m00);
		} else if (sina <= -1.0) {
			x = -Math.PI / 2;
			y = 0;
			z = Math.atan2(-i_rot_matrix.m10, i_rot_matrix.m00);
		} else {
			x = Math.asin(sina);
			y = Math.atan2(-i_rot_matrix.m20, i_rot_matrix.m22);
			z = Math.atan2(-i_rot_matrix.m01, i_rot_matrix.m11);
		}
		o_ang.x=x;
		o_ang.y=y;
		o_ang.z=z;
		o_out[0].sin_val = Math.sin(x);
		o_out[0].cos_val = Math.cos(x);
		o_out[1].sin_val = Math.sin(y);
		o_out[1].cos_val = Math.cos(y);
		o_out[2].sin_val = Math.sin(z);
		o_out[2].cos_val = Math.cos(z);
		return;
	}

	/*
	 * 射影変換式 基本式 ox=(cosc * cosb - sinc * sina * sinb)*ix+(-sinc * cosa)*iy+(cosc * sinb + sinc * sina * cosb)*iz+i_trans.x; oy=(sinc * cosb + cosc * sina *
	 * sinb)*ix+(cosc * cosa)*iy+(sinc * sinb - cosc * sina * cosb)*iz+i_trans.y; oz=(-cosa * sinb)*ix+(sina)*iy+(cosb * cosa)*iz+i_trans.z;
	 * 
	 * double ox=(cosc * cosb)*ix+(-sinc * sina * sinb)*ix+(-sinc * cosa)*iy+(cosc * sinb)*iz + (sinc * sina * cosb)*iz+i_trans.x; double oy=(sinc * cosb)*ix
	 * +(cosc * sina * sinb)*ix+(cosc * cosa)*iy+(sinc * sinb)*iz+(- cosc * sina * cosb)*iz+i_trans.y; double oz=(-cosa * sinb)*ix+(sina)*iy+(cosb *
	 * cosa)*iz+i_trans.z;
	 * 
	 * sina,cosaについて解く cx=(cp00*(-sinc*sinb*ix+sinc*cosb*iz)+cp01*(cosc*sinb*ix-cosc*cosb*iz)+cp02*(iy))*sina
	 * +(cp00*(-sinc*iy)+cp01*((cosc*iy))+cp02*(-sinb*ix+cosb*iz))*cosa
	 * +(cp00*(i_trans.x+cosc*cosb*ix+cosc*sinb*iz)+cp01*((i_trans.y+sinc*cosb*ix+sinc*sinb*iz))+cp02*(i_trans.z));
	 * cy=(cp11*(cosc*sinb*ix-cosc*cosb*iz)+cp12*(iy))*sina +(cp11*((cosc*iy))+cp12*(-sinb*ix+cosb*iz))*cosa
	 * +(cp11*((i_trans.y+sinc*cosb*ix+sinc*sinb*iz))+cp12*(i_trans.z)); ch=(iy)*sina +(-sinb*ix+cosb*iz)*cosa +i_trans.z; sinb,cosb hx=(cp00*(-sinc *
	 * sina*ix+cosc*iz)+cp01*(cosc * sina*ix+sinc*iz)+cp02*(-cosa*ix))*sinb +(cp01*(sinc*ix-cosc * sina*iz)+cp00*(cosc*ix+sinc * sina*iz)+cp02*(cosa*iz))*cosb
	 * +(cp00*(i_trans.x+(-sinc*cosa)*iy)+cp01*(i_trans.y+(cosc * cosa)*iy)+cp02*(i_trans.z+(sina)*iy)); double hy=(cp11*(cosc *
	 * sina*ix+sinc*iz)+cp12*(-cosa*ix))*sinb +(cp11*(sinc*ix-cosc * sina*iz)+cp12*(cosa*iz))*cosb +(cp11*(i_trans.y+(cosc *
	 * cosa)*iy)+cp12*(i_trans.z+(sina)*iy)); double h =((-cosa*ix)*sinb +(cosa*iz)*cosb +i_trans.z+(sina)*iy); パラメータ返還式 L=2*Σ(d[n]*e[n]+a[n]*b[n])
	 * J=2*Σ(d[n]*f[n]+a[n]*c[n])/L K=2*Σ(-e[n]*f[n]+b[n]*c[n])/L M=Σ(-e[n]^2+d[n]^2-b[n]^2+a[n]^2)/L 偏微分式 +J*cos(x) +K*sin(x) -sin(x)^2 +cos(x)^2
	 * +2*M*cos(x)*sin(x)
	 */
	private double optimizeParamX(TSinCosValue i_angle_y, TSinCosValue i_angle_z, NyARDoublePoint3d i_trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d, int i_number_of_vertex, double i_hint_angle) throws NyARException
	{
		NyARPerspectiveProjectionMatrix cp = this._projection_mat_ref;
		final double sinb = i_angle_y.sin_val;
		final double cosb = i_angle_y.cos_val;
		final double sinc = i_angle_z.sin_val;
		final double cosc = i_angle_z.cos_val;
		double L, J, K, M, N, O;
		L = J = K = M = N = O = 0;
		for (int i = 0; i < i_number_of_vertex; i++) {
			double ix, iy, iz;
			ix = i_vertex3d[i].x;
			iy = i_vertex3d[i].y;
			iz = i_vertex3d[i].z;

			final double cp00 = cp.m00;
			final double cp01 = cp.m01;
			final double cp02 = cp.m02;
			final double cp11 = cp.m11;
			final double cp12 = cp.m12;

			double X0 = (cp00 * (-sinc * sinb * ix + sinc * cosb * iz) + cp01 * (cosc * sinb * ix - cosc * cosb * iz) + cp02 * (iy));
			double X1 = (cp00 * (-sinc * iy) + cp01 * ((cosc * iy)) + cp02 * (-sinb * ix + cosb * iz));
			double X2 = (cp00 * (i_trans.x + cosc * cosb * ix + cosc * sinb * iz) + cp01 * ((i_trans.y + sinc * cosb * ix + sinc * sinb * iz)) + cp02 * (i_trans.z));
			double Y0 = (cp11 * (cosc * sinb * ix - cosc * cosb * iz) + cp12 * (iy));
			double Y1 = (cp11 * ((cosc * iy)) + cp12 * (-sinb * ix + cosb * iz));
			double Y2 = (cp11 * ((i_trans.y + sinc * cosb * ix + sinc * sinb * iz)) + cp12 * (i_trans.z));
			double H0 = (iy);
			double H1 = (-sinb * ix + cosb * iz);
			double H2 = i_trans.z;

			double VX = i_vertex2d[i].x;
			double VY = i_vertex2d[i].y;

			double a, b, c, d, e, f;
			a = (VX * H0 - X0);
			b = (VX * H1 - X1);
			c = (VX * H2 - X2);
			d = (VY * H0 - Y0);
			e = (VY * H1 - Y1);
			f = (VY * H2 - Y2);

			L += d * e + a * b;
			N += d * d + a * a;
			J += d * f + a * c;
			M += e * e + b * b;
			K += e * f + b * c;
			O += f * f + c * c;

		}
		L *=2;
		J *=2;
		K *=2;

		return getMinimumErrorAngleFromParam(L,J, K, M, N, O, i_hint_angle);


	}

	private double optimizeParamY(TSinCosValue i_angle_x, TSinCosValue i_angle_z, NyARDoublePoint3d i_trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d, int i_number_of_vertex, double i_hint_angle) throws NyARException
	{
		NyARPerspectiveProjectionMatrix cp = this._projection_mat_ref;
		final double sina = i_angle_x.sin_val;
		final double cosa = i_angle_x.cos_val;
		final double sinc = i_angle_z.sin_val;
		final double cosc = i_angle_z.cos_val;
		double L, J, K, M, N, O;
		L = J = K = M = N = O = 0;
		for (int i = 0; i < i_number_of_vertex; i++) {
			double ix, iy, iz;
			ix = i_vertex3d[i].x;
			iy = i_vertex3d[i].y;
			iz = i_vertex3d[i].z;

			final double cp00 = cp.m00;
			final double cp01 = cp.m01;
			final double cp02 = cp.m02;
			final double cp11 = cp.m11;
			final double cp12 = cp.m12;

			double X0 = (cp00 * (-sinc * sina * ix + cosc * iz) + cp01 * (cosc * sina * ix + sinc * iz) + cp02 * (-cosa * ix));
			double X1 = (cp01 * (sinc * ix - cosc * sina * iz) + cp00 * (cosc * ix + sinc * sina * iz) + cp02 * (cosa * iz));
			double X2 = (cp00 * (i_trans.x + (-sinc * cosa) * iy) + cp01 * (i_trans.y + (cosc * cosa) * iy) + cp02 * (i_trans.z + (sina) * iy));
			double Y0 = (cp11 * (cosc * sina * ix + sinc * iz) + cp12 * (-cosa * ix));
			double Y1 = (cp11 * (sinc * ix - cosc * sina * iz) + cp12 * (cosa * iz));
			double Y2 = (cp11 * (i_trans.y + (cosc * cosa) * iy) + cp12 * (i_trans.z + (sina) * iy));
			double H0 = (-cosa * ix);
			double H1 = (cosa * iz);
			double H2 = i_trans.z + (sina) * iy;

			double VX = i_vertex2d[i].x;
			double VY = i_vertex2d[i].y;

			double a, b, c, d, e, f;
			a = (VX * H0 - X0);
			b = (VX * H1 - X1);
			c = (VX * H2 - X2);
			d = (VY * H0 - Y0);
			e = (VY * H1 - Y1);
			f = (VY * H2 - Y2);

			L += d * e + a * b;
			N += d * d + a * a;
			J += d * f + a * c;
			M += e * e + b * b;
			K += e * f + b * c;
			O += f * f + c * c;

		}
		L *= 2;
		J *= 2;
		K *= 2;
		return getMinimumErrorAngleFromParam(L,J, K, M, N, O, i_hint_angle);

	}

	private double optimizeParamZ(TSinCosValue i_angle_x, TSinCosValue i_angle_y, NyARDoublePoint3d i_trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d, int i_number_of_vertex, double i_hint_angle) throws NyARException
	{
		NyARPerspectiveProjectionMatrix cp = this._projection_mat_ref;
		final double sina = i_angle_x.sin_val;
		final double cosa = i_angle_x.cos_val;
		final double sinb = i_angle_y.sin_val;
		final double cosb = i_angle_y.cos_val;
		double L, J, K, M, N, O;
		L = J = K = M = N = O = 0;
		for (int i = 0; i < i_number_of_vertex; i++) {
			double ix, iy, iz;
			ix = i_vertex3d[i].x;
			iy = i_vertex3d[i].y;
			iz = i_vertex3d[i].z;

			final double cp00 = cp.m00;
			final double cp01 = cp.m01;
			final double cp02 = cp.m02;
			final double cp11 = cp.m11;
			final double cp12 = cp.m12;

			double X0 = (cp00 * (-sina * sinb * ix - cosa * iy + sina * cosb * iz) + cp01 * (ix * cosb + sinb * iz));
			double X1 = (cp01 * (sina * ix * sinb + cosa * iy - sina * iz * cosb) + cp00 * (cosb * ix + sinb * iz));
			double X2 = cp00 * i_trans.x + cp01 * (i_trans.y) + cp02 * (-cosa * sinb) * ix + cp02 * (sina) * iy + cp02 * ((cosb * cosa) * iz + i_trans.z);
			double Y0 = cp11 * (ix * cosb + sinb * iz);
			double Y1 = cp11 * (sina * ix * sinb + cosa * iy - sina * iz * cosb);
			double Y2 = (cp11 * i_trans.y + cp12 * (-cosa * sinb) * ix + cp12 * ((sina) * iy + (cosb * cosa) * iz + i_trans.z));
			double H0 = 0;
			double H1 = 0;
			double H2 = ((-cosa * sinb) * ix + (sina) * iy + (cosb * cosa) * iz + i_trans.z);

			double VX = i_vertex2d[i].x;
			double VY = i_vertex2d[i].y;

			double a, b, c, d, e, f;
			a = (VX * H0 - X0);
			b = (VX * H1 - X1);
			c = (VX * H2 - X2);
			d = (VY * H0 - Y0);
			e = (VY * H1 - Y1);
			f = (VY * H2 - Y2);

			L += d * e + a * b;
			N += d * d + a * a;
			J += d * f + a * c;
			M += e * e + b * b;
			K += e * f + b * c;
			O += f * f + c * c;

		}
		L *=2;
		J *=2;
		K *=2;
		
		return getMinimumErrorAngleFromParam(L,J, K, M, N, O, i_hint_angle);
	}
	private TSinCosValue[] __angles_in=TSinCosValue.createArray(3);
	private NyARDoublePoint3d __ang=new NyARDoublePoint3d();
	public void modifyMatrix(NyARDoubleMatrix33 io_rot, NyARDoublePoint3d i_trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d, int i_number_of_vertex) throws NyARException
	{
		TSinCosValue[] angles_in = this.__angles_in;// x,y,z
		NyARDoublePoint3d ang = this.__ang;

		// ZXY系のsin/cos値を抽出
		rotation2Sincos_ZXY(io_rot, angles_in,ang);
		ang.x += optimizeParamX(angles_in[1], angles_in[2], i_trans, i_vertex3d, i_vertex2d, i_number_of_vertex, ang.x);
		ang.y += optimizeParamY(angles_in[0], angles_in[2], i_trans, i_vertex3d, i_vertex2d, i_number_of_vertex, ang.y);
		ang.z += optimizeParamZ(angles_in[0], angles_in[1], i_trans, i_vertex3d, i_vertex2d, i_number_of_vertex, ang.z);
		io_rot.setZXYAngle(ang.x, ang.y, ang.z);
		return;
	}
	private double[] __sin_table= new double[4];
	/**
	 * エラーレートが最小になる点を得る。
	 */
	private double getMinimumErrorAngleFromParam(double iL,double iJ, double iK, double iM, double iN, double iO, double i_hint_angle) throws NyARException
	{
		double[] sin_table = this.__sin_table;

		double M = (iN - iM)/iL;
		double J = iJ/iL;
		double K = -iK/iL;

		// パラメータからsinテーブルを作成
		// (- 4*M^2-4)*x^4 + (4*K- 4*J*M)*x^3 + (4*M^2 -(K^2- 4)- J^2)*x^2 +(4*J*M- 2*K)*x + J^2-1 = 0
		int number_of_sin = NyAREquationSolver.solve4Equation(-4 * M * M - 4, 4 * K - 4 * J * M, 4 * M * M - (K * K - 4) - J * J, 4 * J * M - 2 * K, J * J - 1, sin_table);


		// 最小値２個を得ておく。
		double min_ang_0 = Double.MAX_VALUE;
		double min_ang_1 = Double.MAX_VALUE;
		double min_err_0 = Double.MAX_VALUE;
		double min_err_1 = Double.MAX_VALUE;
		for (int i = 0; i < number_of_sin; i++) {
			// +-cos_v[i]が頂点候補
			double sin_rt = sin_table[i];
			double cos_rt = Math.sqrt(1 - (sin_rt * sin_rt));
			// cosを修復。微分式で0に近い方が正解
			// 0 = 2*cos(x)*sin(x)*M - sin(x)^2 + cos(x)^2 + sin(x)*K + cos(x)*J
			double a1 = 2 * cos_rt * sin_rt * M + sin_rt * (K - sin_rt) + cos_rt * (cos_rt + J);
			double a2 = 2 * (-cos_rt) * sin_rt * M + sin_rt * (K - sin_rt) + (-cos_rt) * ((-cos_rt) + J);
			// 絶対値になおして、真のcos値を得ておく。
			a1 = a1 < 0 ? -a1 : a1;
			a2 = a2 < 0 ? -a2 : a2;
			cos_rt = (a1 < a2) ? cos_rt : -cos_rt;
			double ang = Math.atan2(sin_rt, cos_rt);
			// エラー値を計算
			double err = iN * sin_rt * sin_rt + (iL*cos_rt + iJ) * sin_rt + iM * cos_rt * cos_rt + iK * cos_rt + iO;
			// 最小の２個を獲得する。
			if (min_err_0 > err) {
				min_err_1 = min_err_0;
				min_ang_1 = min_ang_0;
				min_err_0 = err;
				min_ang_0 = ang;
			} else if (min_err_1 > err) {
				min_err_1 = err;
				min_ang_1 = ang;
			}
		}
		// [0]をテスト
		double gap_0;
		gap_0 = min_ang_0 - i_hint_angle;
		if (gap_0 > Math.PI) {
			gap_0 = (min_ang_0 - Math.PI * 2) - i_hint_angle;
		} else if (gap_0 < -Math.PI) {
			gap_0 = (min_ang_0 + Math.PI * 2) - i_hint_angle;
		}
		// [1]をテスト
		double gap_1;
		gap_1 = min_ang_1 - i_hint_angle;
		if (gap_1 > Math.PI) {
			gap_1 = (min_ang_1 - Math.PI * 2) - i_hint_angle;
		} else if (gap_1 < -Math.PI) {
			gap_1 = (min_ang_1 + Math.PI * 2) - i_hint_angle;
		}
		return Math.abs(gap_1) < Math.abs(gap_0) ? gap_1 : gap_0;
	}
}
