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
package jp.nyatla.nyartoolkit.core.transmat;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.core.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;




public interface NyARTransRot
{
//	public double[] getArray();
	/**
	 * 
	 * @param trans
	 * @param vertex
	 * @param pos2d
	 * [n*2]配列
	 * @return
	 * @throws NyARException
	 */
	public double modifyMatrix(double trans[], NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d) throws NyARException;

	public void initRotBySquare(final NyARLinear[] i_linear,final NyARDoublePoint2d[] i_sqvertex) throws NyARException;

}

/**
 * NyARTransRot派生クラスで共通に使いそうな関数類をまとめたもの。
 * 
 */
abstract class NyARTransRot_OptimizeCommon extends NyARRotMatrix implements NyARTransRot
{	
	protected final int _number_of_vertex;
	protected final NyARParam _cparam;

	/**
	 * インスタンスを準備します。
	 * 
	 * @param i_param
	 * nullを指定した場合、一部の関数が使用不能になります。
	 */
	public NyARTransRot_OptimizeCommon(NyARParam i_param, int i_number_of_vertex) throws NyARException
	{
		super(i_param);
		this._number_of_vertex = i_number_of_vertex;
		this._cparam = i_param;
		return;
	}
}

/**
 * NyARModifyMatrixの最適化バージョン1 配列の１次元化、計算ステップの圧縮等の最適化をしてみた。
 * 
 */
class NyARTransRot_O1 extends NyARTransRot_OptimizeCommon
{
	public NyARTransRot_O1(NyARParam i_param, int i_number_of_vertex) throws NyARException
	{
		super(i_param, i_number_of_vertex);
	}


	private final double[] wk_arModifyMatrix_combo = new double[12];// [3][4];

	private final NyARDoublePoint3d __modifyMatrix_angle = new NyARDoublePoint3d();

	private final double[] wk_arModifyMatrix_rot = new double[9];

	/**
	 * Optimize:2008.04.20:STEP[456→-]
	 * 
	 * @param rot
	 * [3x3]配列
	 * @param trans
	 * @param vertex
	 * @param pos2d
	 * @param num
	 * @return
	 */
	public final double modifyMatrix(double trans[], double vertex[][], double pos2d[][]) throws NyARException
	{
		int num = this._number_of_vertex;
		double factor;
		double a1, b1, c1;
		double a2, b2, c2;
		double ma = 0.0, mb = 0.0, mc = 0.0;
		double hx, hy, h, x, y;
		double err, minerr = 0;
		int t1, t2, t3;
		int s1 = 0, s2 = 0, s3 = 0;
		int i, j;
		double[] combo = this.wk_arModifyMatrix_combo;// arGetNewMatrixで初期化されるので初期化不要//new
		// double[3][4];
		final NyARDoublePoint3d angle = this.__modifyMatrix_angle;
		double[] rot = wk_arModifyMatrix_rot;

		this.getAngle(angle);// arGetAngle( rot, &a, &b, &c );
		a2 = angle.x;
		b2 = angle.y;
		c2 = angle.z;
		factor = 10.0 * Math.PI / 180.0;
		for (j = 0; j < 10; j++) {
			minerr = 1000000000.0;
			for (t1 = -1; t1 <= 1; t1++) {
				for (t2 = -1; t2 <= 1; t2++) {
					for (t3 = -1; t3 <= 1; t3++) {
						a1 = a2 + factor * t1;
						b1 = b2 + factor * t2;
						c1 = c2 + factor * t3;
						arGetRot(a1, b1, c1, rot);
						arGetNewMatrix(rot, trans, null, combo);
						err = 0.0;
						for (i = 0; i < num; i++) {
							hx = combo[0] * vertex[i][0] + combo[1] * vertex[i][1] + combo[2] * vertex[i][2] + combo[3];
							hy = combo[4] * vertex[i][0] + combo[5] * vertex[i][1] + combo[6] * vertex[i][2] + combo[7];
							h = combo[8] * vertex[i][0] + combo[9] * vertex[i][1] + combo[10] * vertex[i][2] + combo[11];
							x = hx / h;
							y = hy / h;
							err += (pos2d[i][0] - x) * (pos2d[i][0] - x) + (pos2d[i][1] - y) * (pos2d[i][1] - y);
						}
						if (err < minerr) {
							minerr = err;
							ma = a1;
							mb = b1;
							mc = c1;
							s1 = t1;
							s2 = t2;
							s3 = t3;
						}
					}
				}
			}
			if (s1 == 0 && s2 == 0 && s3 == 0) {
				factor *= 0.5;
			}
			a2 = ma;
			b2 = mb;
			c2 = mc;
		}
		this.setRot(ma, mb, mc);
		/* printf("factor = %10.5f\n", factor*180.0/MD_PI); */
		return minerr / num;
	}

	private final double[] wk_cpara2_arGetNewMatrix = new double[12];// [3][4];

	/**
	 * Optimize:2008.04.20:STEP[569->432]
	 * 
	 * @param i_rot
	 * [9]
	 * @param trans
	 * @param trans2
	 * @param ret
	 * double[3x4]配列
	 * @return
	 */
	private final int arGetNewMatrix(double[] i_rot, double trans[], double trans2[][], double ret[]) throws NyARException
	{
		final double cpara[] = _cparam.get34Array();
		final double[] cpara2; // この関数で初期化される。
		int j, j_idx;
		// double[] cpara_pt;
		// cparaの2次元配列→1次元に変換して計算
		if (trans2 != null) {
			cpara2 = wk_cpara2_arGetNewMatrix; // この関数で初期化される。

			for (j = 0; j < 3; j++) {
				// Optimize(使わないから最適化してない)
				NyARException.trap("未チェックのパス");
				cpara2[j * 4 + 0] = cpara[j * 4 + 0] * trans2[0][0] + cpara[j * 4 + 1] * trans2[1][0] + cpara[j * 4 + 2] * trans2[2][0];
				cpara2[j * 4 + 1] = cpara[j * 4 + 0] * trans2[0][1] + cpara[j * 4 + 1] * trans2[1][1] + cpara[j * 4 + 2] * trans2[2][1];
				cpara2[j * 4 + 2] = cpara[j * 4 + 0] * trans2[0][2] + cpara[j * 4 + 1] * trans2[1][2] + cpara[j * 4 + 2] * trans2[2][2];
				cpara2[j * 4 + 3] = cpara[j * 4 + 0] * trans2[0][3] + cpara[j * 4 + 1] * trans2[1][3] + cpara[j * 4 + 2] * trans2[2][3];
			}
		} else {
			cpara2 = cpara;// cparaの値をそのまま使う
		}
		for (j = 0; j < 3; j++) {
			// cpara2_pt=cpara2[j];
			j_idx = j * 4;
			// <Optimize>
			// ret[j][0] = cpara2_pt[0] * rot[0][0]+ cpara2_pt[1] * rot[1][0]+
			// cpara2_pt[2] * rot[2][0];
			// ret[j][1] = cpara2_pt[0] * rot[0][1]+ cpara2_pt[1] * rot[1][1]+
			// cpara2_pt[2] * rot[2][1];
			// ret[j][2] = cpara2_pt[0] * rot[0][2]+ cpara2_pt[1] * rot[1][2]+
			// cpara2_pt[2] * rot[2][2];
			// ret[j][3] = cpara2_pt[0] * trans[0]+ cpara2_pt[1] * trans[1]+
			// cpara2_pt[2] * trans[2]+ cpara2_pt[3];
			ret[j_idx + 0] = cpara2[j_idx + 0] * i_rot[0] + cpara2[j_idx + 1] * i_rot[3] + cpara2[j_idx + 2] * i_rot[6];
			ret[j_idx + 1] = cpara2[j_idx + 0] * i_rot[1] + cpara2[j_idx + 1] * i_rot[4] + cpara2[j_idx + 2] * i_rot[7];
			ret[j_idx + 2] = cpara2[j_idx + 0] * i_rot[2] + cpara2[j_idx + 1] * i_rot[5] + cpara2[j_idx + 2] * i_rot[8];
			ret[j_idx + 3] = cpara2[j_idx + 0] * trans[0] + cpara2[j_idx + 1] * trans[1] + cpara2[j_idx + 2] * trans[2] + cpara2[j_idx + 3];
			// </Optimize>
		}
		return (0);
	}
}



/**
 * NyARModifyMatrixの最適化バージョン3 O3版の演算テーブル版 計算速度のみを追求する
 * 
 */
class NyARTransRot_O3 extends NyARTransRot_OptimizeCommon
{
	public NyARTransRot_O3(NyARParam i_param, int i_number_of_vertex) throws NyARException
	{
		super(i_param, i_number_of_vertex);
		if (i_number_of_vertex != 4) {
			// 4以外の頂点数は処理しない
			throw new NyARException();
		}
	}


	private final double[][] __modifyMatrix_double1D = new double[8][3];
	private final NyARDoublePoint3d __modifyMatrix_angle = new NyARDoublePoint3d();


	/**
	 * arGetRot計算を階層化したModifyMatrix 896
	 * 
	 * @param nyrot
	 * @param trans
	 * @param i_vertex3d
	 * [m][3]
	 * @param i_vertex2d
	 * [n][2]
	 * @return
	 * @throws NyARException
	 */
	public double modifyMatrix(double trans[], NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d i_vertex2d[]) throws NyARException
	{
		double factor;
		double a2, b2, c2;
		double ma = 0.0, mb = 0.0, mc = 0.0;
		double h, x, y;
		double err, minerr = 0;
		int t1, t2, t3;
		int s1 = 0, s2 = 0, s3 = 0;

		factor = 10.0 * Math.PI / 180.0;
		double rot0, rot1, rot3, rot4, rot6, rot7;
		double combo00, combo01, combo02, combo03, combo10, combo11, combo12, combo13, combo20, combo21, combo22, combo23;
		double combo02_2, combo02_5, combo02_8, combo02_11;
		double combo22_2, combo22_5, combo22_8, combo22_11;
		double combo12_2, combo12_5, combo12_8, combo12_11;
		// vertex展開
		final double VX00, VX01, VX02, VX10, VX11, VX12, VX20, VX21, VX22, VX30, VX31, VX32;
		NyARDoublePoint3d d_pt;
		d_pt = i_vertex3d[0];
		VX00 = d_pt.x;
		VX01 = d_pt.y;
		VX02 = d_pt.z;
		d_pt = i_vertex3d[1];
		VX10 = d_pt.x;
		VX11 = d_pt.y;
		VX12 = d_pt.z;
		d_pt = i_vertex3d[2];
		VX20 = d_pt.x;
		VX21 = d_pt.y;
		VX22 = d_pt.z;
		d_pt = i_vertex3d[3];
		VX30 = d_pt.x;
		VX31 = d_pt.y;
		VX32 = d_pt.z;
		final double P2D00, P2D01, P2D10, P2D11, P2D20, P2D21, P2D30, P2D31;
		NyARDoublePoint2d d_pt2;
		d_pt2 = i_vertex2d[0];
		P2D00 = d_pt2.x;
		P2D01 = d_pt2.y;
		d_pt2 = i_vertex2d[1];
		P2D10 = d_pt2.x;
		P2D11 = d_pt2.y;
		d_pt2 = i_vertex2d[2];
		P2D20 = d_pt2.x;
		P2D21 = d_pt2.y;
		d_pt2 = i_vertex2d[3];
		P2D30 = d_pt2.x;
		P2D31 = d_pt2.y;
		final double cpara[] = _cparam.get34Array();
		final double CP0, CP1, CP2, CP3, CP4, CP5, CP6, CP7, CP8, CP9, CP10;
		CP0 = cpara[0];
		CP1 = cpara[1];
		CP2 = cpara[2];
		CP3 = cpara[3];
		CP4 = cpara[4];
		CP5 = cpara[5];
		CP6 = cpara[6];
		CP7 = cpara[7];
		CP8 = cpara[8];
		CP9 = cpara[9];
		CP10 = cpara[10];
		combo03 = CP0 * trans[0] + CP1 * trans[1] + CP2 * trans[2] + CP3;
		combo13 = CP4 * trans[0] + CP5 * trans[1] + CP6 * trans[2] + CP7;
		combo23 = CP8 * trans[0] + CP9 * trans[1] + CP10 * trans[2] + cpara[11];
		double CACA, SASA, SACA, CA, SA;
		double CACACB, SACACB, SASACB, CASB, SASB;
		double SACASC, SACACBSC, SACACBCC, SACACC;
		final double[][] double1D = this.__modifyMatrix_double1D;

		final NyARDoublePoint3d angle = this.__modifyMatrix_angle;

		final double[] a_factor = double1D[1];
		final double[] sinb = double1D[2];
		final double[] cosb = double1D[3];
		final double[] b_factor = double1D[4];
		final double[] sinc = double1D[5];
		final double[] cosc = double1D[6];
		final double[] c_factor = double1D[7];
		double w, w2;
		double wsin, wcos;

		this.getAngle(angle);// arGetAngle( rot, &a, &b, &c );
		a2 = angle.x;
		b2 = angle.y;
		c2 = angle.z;

		// comboの3行目を先に計算
		for (int i = 0; i < 10; i++) {
			minerr = 1000000000.0;
			// sin-cosテーブルを計算(これが外に出せるとは…。)
			for (int j = 0; j < 3; j++) {
				w2 = factor * (j - 1);
				w = a2 + w2;
				a_factor[j] = w;
				w = b2 + w2;
				b_factor[j] = w;
				sinb[j] = Math.sin(w);
				cosb[j] = Math.cos(w);
				w = c2 + w2;
				c_factor[j] = w;
				sinc[j] = Math.sin(w);
				cosc[j] = Math.cos(w);
			}
			//
			for (t1 = 0; t1 < 3; t1++) {
				SA = Math.sin(a_factor[t1]);
				CA = Math.cos(a_factor[t1]);
				// Optimize
				CACA = CA * CA;
				SASA = SA * SA;
				SACA = SA * CA;
				for (t2 = 0; t2 < 3; t2++) {
					wsin = sinb[t2];
					wcos = cosb[t2];
					CACACB = CACA * wcos;
					SACACB = SACA * wcos;
					SASACB = SASA * wcos;
					CASB = CA * wsin;
					SASB = SA * wsin;
					// comboの計算1
					combo02 = CP0 * CASB + CP1 * SASB + CP2 * wcos;
					combo12 = CP4 * CASB + CP5 * SASB + CP6 * wcos;
					combo22 = CP8 * CASB + CP9 * SASB + CP10 * wcos;

					combo02_2 = combo02 * VX02 + combo03;
					combo02_5 = combo02 * VX12 + combo03;
					combo02_8 = combo02 * VX22 + combo03;
					combo02_11 = combo02 * VX32 + combo03;
					combo12_2 = combo12 * VX02 + combo13;
					combo12_5 = combo12 * VX12 + combo13;
					combo12_8 = combo12 * VX22 + combo13;
					combo12_11 = combo12 * VX32 + combo13;
					combo22_2 = combo22 * VX02 + combo23;
					combo22_5 = combo22 * VX12 + combo23;
					combo22_8 = combo22 * VX22 + combo23;
					combo22_11 = combo22 * VX32 + combo23;
					for (t3 = 0; t3 < 3; t3++) {
						wsin = sinc[t3];
						wcos = cosc[t3];
						SACASC = SACA * wsin;
						SACACC = SACA * wcos;
						SACACBSC = SACACB * wsin;
						SACACBCC = SACACB * wcos;

						rot0 = CACACB * wcos + SASA * wcos + SACACBSC - SACASC;
						rot3 = SACACBCC - SACACC + SASACB * wsin + CACA * wsin;
						rot6 = -CASB * wcos - SASB * wsin;

						combo00 = CP0 * rot0 + CP1 * rot3 + CP2 * rot6;
						combo10 = CP4 * rot0 + CP5 * rot3 + CP6 * rot6;
						combo20 = CP8 * rot0 + CP9 * rot3 + CP10 * rot6;

						rot1 = -CACACB * wsin - SASA * wsin + SACACBCC - SACACC;
						rot4 = -SACACBSC + SACASC + SASACB * wcos + CACA * wcos;
						rot7 = CASB * wsin - SASB * wcos;
						combo01 = CP0 * rot1 + CP1 * rot4 + CP2 * rot7;
						combo11 = CP4 * rot1 + CP5 * rot4 + CP6 * rot7;
						combo21 = CP8 * rot1 + CP9 * rot4 + CP10 * rot7;
						//
						err = 0.0;
						h = combo20 * VX00 + combo21 * VX01 + combo22_2;
						x = P2D00 - (combo00 * VX00 + combo01 * VX01 + combo02_2) / h;
						y = P2D01 - (combo10 * VX00 + combo11 * VX01 + combo12_2) / h;
						err += x * x + y * y;
						h = combo20 * VX10 + combo21 * VX11 + combo22_5;
						x = P2D10 - (combo00 * VX10 + combo01 * VX11 + combo02_5) / h;
						y = P2D11 - (combo10 * VX10 + combo11 * VX11 + combo12_5) / h;
						err += x * x + y * y;
						h = combo20 * VX20 + combo21 * VX21 + combo22_8;
						x = P2D20 - (combo00 * VX20 + combo01 * VX21 + combo02_8) / h;
						y = P2D21 - (combo10 * VX20 + combo11 * VX21 + combo12_8) / h;
						err += x * x + y * y;
						h = combo20 * VX30 + combo21 * VX31 + combo22_11;
						x = P2D30 - (combo00 * VX30 + combo01 * VX31 + combo02_11) / h;
						y = P2D31 - (combo10 * VX30 + combo11 * VX31 + combo12_11) / h;
						err += x * x + y * y;
						if (err < minerr) {
							minerr = err;
							ma = a_factor[t1];
							mb = b_factor[t2];
							mc = c_factor[t3];
							s1 = t1 - 1;
							s2 = t2 - 1;
							s3 = t3 - 1;
						}
					}
				}
			}
			if (s1 == 0 && s2 == 0 && s3 == 0) {
				factor *= 0.5;
			}
			a2 = ma;
			b2 = mb;
			c2 = mc;
		}
		this.setRot(ma, mb, mc);
		/* printf("factor = %10.5f\n", factor*180.0/MD_PI); */
		return minerr / 4;
	}
}
