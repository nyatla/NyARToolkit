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

import java.util.Date;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.core.NyARSquare;
import jp.nyatla.utils.DoubleValue;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * This class calculates ARMatrix from square information and holds it. --
 * 変換行列を計算して、結果を保持するクラス。
 * 
 */
public class NyARTransMat_O2 implements INyARTransMat
{
	private final static int AR_FITTING_TO_IDEAL = 0;// #define
														// AR_FITTING_TO_IDEAL 0

	private final static int AR_FITTING_TO_INPUT = 1;// #define
														// AR_FITTING_TO_INPUT 1

	private final static int arFittingMode = AR_FITTING_TO_INPUT;

	private final static int AR_GET_TRANS_MAT_MAX_LOOP_COUNT = 5;// #define
																	// AR_GET_TRANS_MAT_MAX_LOOP_COUNT
																	// 5

	private final static double AR_GET_TRANS_MAT_MAX_FIT_ERROR = 1.0;// #define
																		// AR_GET_TRANS_MAT_MAX_FIT_ERROR
																		// 1.0

	private final static double AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR = 1.0;

	private final static int NUMBER_OF_VERTEX = 4;// 処理対象の頂点数

	private final NyARRotMatrix _rotmatrix;

	private final double[] center = { 0.0, 0.0 };

	private final NyARParam param;

	public NyARTransMat_O2(NyARParam i_param) throws NyARException
	{
		param = i_param;
		this._rotmatrix = new NyARTransRot_O3(i_param, NUMBER_OF_VERTEX);
		this.__transMat_marker_vertex3d=NyARDoublePoint3d.createArray(NUMBER_OF_VERTEX);
		this.__transMat_marker_vertex2d=NyARDoublePoint2d.createArray(NUMBER_OF_VERTEX);
	}

	public void setCenter(double i_x, double i_y)
	{
		center[0] = i_x;
		center[1] = i_x;
	}




	/**
	 * i_squareの内容で、頂点情報を初期化します。
	 * 
	 * @param i_square
	 * @param i_direction
	 * @param i_width
	 * @param o_sqvertex_ref
	 * @param o_markbase_vertex
	 */
	private final void initializeVertexArray(NyARSquare i_square, int i_direction, NyARDoublePoint2d[] o_sqvertex_ref, NyARLinear[] o_liner_ref)
	{
		//頂点順序を考慮した矩形の頂点情報
		o_sqvertex_ref[0]= i_square.sqvertex[(4 - i_direction) % 4];
		o_sqvertex_ref[1]= i_square.sqvertex[(5 - i_direction) % 4];
		o_sqvertex_ref[2]= i_square.sqvertex[(6 - i_direction) % 4];
		o_sqvertex_ref[3]= i_square.sqvertex[(7 - i_direction) % 4];
		
		o_liner_ref[0]=i_square.line[(4 - i_direction) % 4];
		o_liner_ref[1]=i_square.line[(5 - i_direction) % 4];
		o_liner_ref[2]=i_square.line[(6 - i_direction) % 4];
		o_liner_ref[3]=i_square.line[(7 - i_direction) % 4];

//		//3d空間上の頂点位置
//		final double c0 = center[0];
//		final double c1 = center[1];
//		final double w_2 = i_width / 2.0;
//		o_markbase_vertex[0][0] = c0 - w_2;// center[0] - w/2.0;
//		o_markbase_vertex[0][1] = c1 + w_2;// center[1] + w/2.0;
//		o_markbase_vertex[1][0] = c0 + w_2;// center[0] + w/2.0;
//		o_markbase_vertex[1][1] = c1 + w_2;// center[1] + w/2.0;
//		o_markbase_vertex[2][0] = c0 + w_2;// center[0] + w/2.0;
//		o_markbase_vertex[2][1] = c1 - w_2;// center[1] - w/2.0;
//		o_markbase_vertex[3][0] = c0 - w_2;// center[0] - w/2.0;
//		o_markbase_vertex[3][1] = c1 - w_2;// center[1] - w/2.0;
		return;
	}

	private NyARDoublePoint3d[] __transMat_marker_vertex3d;
	private NyARDoublePoint2d[] __transMat_marker_vertex2d;

	private final NyARDoublePoint2d[] __transMat_sqvertex_ref = new NyARDoublePoint2d[4];
	private final NyARLinear[] __transMat_linear_ref=new NyARLinear[4];


	private final double[] wk_transMat_off = new double[3];


	private final NyARMat wk_transMat_mat_b = new NyARMat(3, NUMBER_OF_VERTEX * 2);

	private final NyARMat wk_transMat_mat_d = new NyARMat(3, 3);

	private final double[] wk_transMat_mat_trans = new double[3];

	/**
	 * double arGetTransMat( ARMarkerInfo *marker_info,double center[2], double
	 * width, double conv[3][4] ) 演算シーケンス最適化のため、arGetTransMat3等の関数フラグメントを含みます。
	 * 保持している変換行列を更新する。
	 * 
	 * @param i_square
	 * 計算対象のNyARSquareオブジェクト
	 * @param i_direction
	 * @param i_width
	 * @return
	 * @throws NyARException
	 */
	public double transMat(final NyARSquare i_square, int i_direction, double i_width, NyARTransMatResult o_result_conv) throws NyARException
	{
		final NyARDoublePoint2d[] sqvertex_ref = __transMat_sqvertex_ref;
		final NyARLinear[] linear_ref=__transMat_linear_ref;
//		double[][] mark_vertex = __transMat_mark_vertex;
		double[] off = wk_transMat_off;


		//画面上の頂点情報と、マーカーベースの頂点を矩形情報から初期化
		initializeVertexArray(i_square, i_direction, sqvertex_ref,linear_ref);

		// rotationの初期化
		_rotmatrix.initRotBySquare(linear_ref,sqvertex_ref);
		
		// arGetTransMat3の前段処理(pos3dとoffを初期化)
		final NyARMat mat_b = this.wk_transMat_mat_b;
		final NyARMat mat_d = this.wk_transMat_mat_d;

		final NyARDoublePoint2d[] marker_vertex2d = this.__transMat_marker_vertex2d;
		final NyARDoublePoint3d[] marker_vertex3d = this.__transMat_marker_vertex3d;
		initTransMat(i_width, sqvertex_ref, marker_vertex2d, marker_vertex3d, off, mat_b, mat_d);

		double err = -1;
		double[] trans = this.wk_transMat_mat_trans;
		for (int i = 0; i < AR_GET_TRANS_MAT_MAX_LOOP_COUNT; i++) {
			// <arGetTransMat3>
			err = arGetTransMatSub(marker_vertex2d, marker_vertex3d, mat_b, mat_d, trans);
			// //</arGetTransMat3>
			if (err < AR_GET_TRANS_MAT_MAX_FIT_ERROR) {
				break;
			}
		}
		// マトリクスの保存
		o_result_conv.updateMatrixValue(this._rotmatrix, off, trans);
		return err;
	}

	private final NyARTransMatResult wk_transMatContinue_result = new NyARTransMatResult();

	/**
	 * double arGetTransMatCont( ARMarkerInfo *marker_info, double
	 * prev_conv[3][4],double center[2], double width, double conv[3][4] )
	 * 
	 * @param i_square
	 * @param i_direction
	 * マーカーの方位を指定する。
	 * @param i_width
	 * @param io_result_conv
	 * 計算履歴を持つNyARTransMatResultオブジェクトを指定する。 履歴を持たない場合は、transMatと同じ処理を行う。
	 * @return
	 * @throws NyARException
	 */
	public double transMatContinue(NyARSquare i_square, int i_direction, double i_width, NyARTransMatResult io_result_conv) throws NyARException
	{
		// io_result_convが初期値なら、transMatで計算する。
		if (!io_result_conv.hasValue()) {
			return this.transMat(i_square, i_direction, i_width, io_result_conv);
		}

		final NyARLinear[] linear_ref=__transMat_linear_ref;
		
		NyARDoublePoint2d[] sqvertex_ref = this.__transMat_sqvertex_ref;
//		double[][] mark_vertex = this.__transMat_mark_vertex;
		double[] off = wk_transMat_off;
		final NyARDoublePoint3d[] marker_vertex3d = this.__transMat_marker_vertex3d;
		final NyARDoublePoint2d[] marker_vertex2d = this.__transMat_marker_vertex2d;

		// arGetTransMatContSub計算部分
		_rotmatrix.initRotByPrevResult(io_result_conv);

		// ppos2dとppos3dの初期化
		initializeVertexArray(i_square,i_direction,sqvertex_ref,linear_ref);

		// arGetTransMat3の前段処理(pos3dとoffを初期化)
		final NyARMat mat_b = this.wk_transMat_mat_b;
		final NyARMat mat_d = this.wk_transMat_mat_d;

		// transMatに必要な初期値を計算
		initTransMat(i_width, sqvertex_ref, marker_vertex2d, marker_vertex3d, off, mat_b, mat_d);

		double err1, err2;
		int i;

		err1 = err2 = -1;
		double[] trans = this.wk_transMat_mat_trans;
		for (i = 0; i < AR_GET_TRANS_MAT_MAX_LOOP_COUNT; i++) {
			err1 = arGetTransMatSub(marker_vertex2d, marker_vertex3d, mat_b, mat_d, trans);
			if (err1 < AR_GET_TRANS_MAT_MAX_FIT_ERROR) {
				// 十分な精度を達成できたらブレーク
				break;
			}
		}
		// 値を保存
		io_result_conv.updateMatrixValue(this._rotmatrix, off, trans);

		// エラー値が許容範囲でなければTransMatをやり直し
		if (err1 > AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR) {
			NyARTransMatResult result2 = this.wk_transMatContinue_result;
			// transMatを実行(初期化値は共用)
			_rotmatrix.initRotBySquare(linear_ref,sqvertex_ref);
			err2 = transMat(i_square, i_direction, i_width, result2);
			// transmMatここまで
			if (err2 < err1) {
				// 良い値が取れたら、差換え
				io_result_conv.copyFrom(result2);
				err1 = err2;
			}
		}
		return err1;
	}

	private final NyARMat wk_arGetTransMat3_mat_a = new NyARMat(NUMBER_OF_VERTEX * 2, 3);
	/**
	 * arGetTransMat3関数の前処理部分。i_ppos3dから、o_pos3dとoffを計算する。
	 * 計算結果から再帰的に変更される可能性が無いので、切り離し。
	 * 
	 * @param i_mark_vertex
	 * 入力配列[num][2]
	 * @param i_square_vertex
	 * 出力配列[P_MAX][3]
	 * @param o_marker_vertex3d
	 * 
	 * @param o_off
	 * [3]
	 * @throws NyARException
	 */
	private final void initTransMat(final double i_width, NyARDoublePoint2d[] i_square_vertex, NyARDoublePoint2d[] o_marker_vertex_2d, NyARDoublePoint3d[] o_marker_vertex3d, double[] o_off, NyARMat o_mat_b, NyARMat o_mat_d) throws NyARException
	{
		// オフセット位置の計算
		// オフセットは、マーカーの初期値での、4頂点の中心位置(z方向は0)
		// ARToolKitの独自値は以下のように計算していたが、正方形マーカーの場合は計算せずとも求まる。
		// 
		// final double c0 = center[0];
		// final double c1 = center[1];
		// final double w_2 = i_width / 2.0;
		// i_mark_vertex[0][0] = c0 - w_2;// center[0] - w/2.0;
		// i_mark_vertex[0][1] = c1 + w_2;// center[1] + w/2.0;
		// i_mark_vertex[1][0] = c0 + w_2;// center[0] + w/2.0;
		// i_mark_vertex[1][1] = c1 + w_2;// center[1] + w/2.0;
		// i_mark_vertex[2][0] = c0 + w_2;// center[0] + w/2.0;
		// i_mark_vertex[2][1] = c1 - w_2;// center[1] - w/2.0;
		// i_mark_vertex[3][0] = c0 - w_2;// center[0] - w/2.0;
		// i_mark_vertex[3][1] = c1 - w_2;// center[1] - w/2.0;
		// //マーカー座標系の最大値と最小値を計算
		// double pmax0, pmax1, pmax2, pmin0, pmin1, pmin2;
		// pmax0 = pmax1 = pmax2 = -10000000000.0;
		// pmin0 = pmin1 = pmin2 = 10000000000.0;
		// for (i = 0; i < NUMBER_OF_VERTEX; i++) {
		// 	if (i_mark_vertex[i][0] > pmax0) {
		// 		pmax0 = i_mark_vertex[i][0];
		// 	}
		// 	if (i_mark_vertex[i][0] < pmin0) {
		// 		pmin0 = i_mark_vertex[i][0];
		// 	}
		// 	if (i_mark_vertex[i][1] > pmax1) {
		// 		pmax1 = i_mark_vertex[i][1];
		// 	}
		// 	if (i_mark_vertex[i][1] < pmin1) {
		//  	pmin1 = i_mark_vertex[i][1];
		// 	}
		// }
		// o_off[0] = -(pmax0 + pmin0) / 2.0;
		// o_off[1] = -(pmax1 + pmin1) / 2.0;
		// o_off[2] = -(pmax2 + pmin2) / 2.0;
		//

		o_off[0] = -center[0];
		o_off[1] = -center[1];
		o_off[2] = -0;
		 
		//ジッタ除去するときあわせる正方形頂点(理論値)の計算
		final double c0 = center[0];
		final double c1 = center[1];
		final double w_2 = i_width / 2.0;
		
		NyARDoublePoint3d o_pos3d_ptr;
		o_pos3d_ptr= o_marker_vertex3d[0];
		o_pos3d_ptr.x = c0 - w_2 + o_off[0];
		o_pos3d_ptr.y = c1 + w_2 + o_off[1];
		o_pos3d_ptr.z = 0.0;
		o_pos3d_ptr= o_marker_vertex3d[1];
		o_pos3d_ptr.x = c0 + w_2 + o_off[0];
		o_pos3d_ptr.y = c1 + w_2 + o_off[1];
		o_pos3d_ptr.z = 0.0;
		o_pos3d_ptr= o_marker_vertex3d[2];
		o_pos3d_ptr.x = c0 + w_2 + o_off[0];
		o_pos3d_ptr.y = c1 - w_2 + o_off[1];
		o_pos3d_ptr.z = 0.0;
		o_pos3d_ptr= o_marker_vertex3d[3];
		o_pos3d_ptr.x = c0 - w_2 + o_off[0];
		o_pos3d_ptr.y = c1 - w_2 + o_off[1];
		o_pos3d_ptr.z = 0.0;


		// arGetTransMatSubにあった処理。毎回おなじっぽい。pos2dに変換座標を格納する。
		int i;
		if (arFittingMode == AR_FITTING_TO_INPUT) {
			// arParamIdeal2Observをバッチ処理
			param.ideal2ObservBatch(i_square_vertex, o_marker_vertex_2d, NUMBER_OF_VERTEX);
		} else {
			for (i = 0; i < NUMBER_OF_VERTEX; i++) {
				o_marker_vertex_2d[i].x = i_square_vertex[i].x;
				o_marker_vertex_2d[i].y = i_square_vertex[i].y;
			}
		}

		// 変換マトリクスdとbの準備(arGetTransMatSubの一部)
		final double cpara[] = param.get34Array();
		final NyARMat mat_a = this.wk_arGetTransMat3_mat_a;
		final double[][] a_array = mat_a.getArray();

		// mat_bの設定
		final double[][] b_array = o_mat_b.getArray();

		int x2;
		for (i = 0; i < NUMBER_OF_VERTEX; i++) {
			x2 = i * 2;
			// </Optimize>
			a_array[x2][0] = b_array[0][x2] = cpara[0 * 4 + 0];// mat_a->m[j*6+0]=mat_b->m[num*0+j*2] =cpara[0][0];
			a_array[x2][1] = b_array[1][x2] = cpara[0 * 4 + 1];// mat_a->m[j*6+1]=mat_b->m[num*2+j*2]=cpara[0][1];
			a_array[x2][2] = b_array[2][x2] = cpara[0 * 4 + 2] - o_marker_vertex_2d[i].x;// mat_a->m[j*6+2]=mat_b->m[num*4+j*2]=cpara[0][2]-pos2d[j][0];
			a_array[x2 + 1][0] = b_array[0][x2 + 1] = 0.0;// mat_a->m[j*6+3] =mat_b->m[num*0+j*2+1]= 0.0;
			a_array[x2 + 1][1] = b_array[1][x2 + 1] = cpara[1 * 4 + 1];// mat_a->m[j*6+4] =mat_b->m[num*2+j*2+1]= cpara[1][1];
			a_array[x2 + 1][2] = b_array[2][x2 + 1] = cpara[1 * 4 + 2] - o_marker_vertex_2d[i].y;// mat_a->m[j*6+5]=mat_b->m[num*4+j*2+1]=cpara[1][2]-pos2d[j][1];
		}

		// mat_d
		o_mat_d.matrixMul(o_mat_b, mat_a);
		o_mat_d.matrixSelfInv();
	}

	private final NyARMat wk_arGetTransMatSub_mat_c = new NyARMat(NUMBER_OF_VERTEX * 2, 1);

	private final NyARMat wk_arGetTransMatSub_mat_e = new NyARMat(3, 1);

	private final NyARMat wk_arGetTransMatSub_mat_f = new NyARMat(3, 1);
	private final NyARDoublePoint3d __arGetTransMatSub_point3d=new NyARDoublePoint3d();
	/**
	 * static double arGetTransMatSub( double rot[3][3], double
	 * ppos2d[][2],double pos3d[][3], int num, double conv[3][4],double
	 * *dist_factor, double cpara[3][4] ) Optimize:2008.04.20:STEP[1033→1004]
	 * 
	 * @param i_ppos2d
	 * @param i_vertex3d
	 * @param i_mat_b
	 * 演算用行列b
	 * @param i_mat_d
	 * 演算用行列d
	 * @return
	 * @throws NyARException
	 */
	private final double arGetTransMatSub(final NyARDoublePoint2d[] i_vertex2d,final NyARDoublePoint3d[] i_vertex3d, NyARMat i_mat_b, NyARMat i_mat_d, double[] o_trans) throws NyARException
	{
		double cpara[] = param.get34Array();
		NyARMat mat_c, mat_e, mat_f;// ARMat *mat_a, *mat_b, *mat_c, *mat_d,*mat_e, *mat_f;

		double ret;
		int i;
		final NyARDoublePoint3d point3d=this.__arGetTransMatSub_point3d;

		mat_c = this.wk_arGetTransMatSub_mat_c;// 次処理で値をもらうので、初期化の必要は無い。
		double[][] c_array = mat_c.getArray();
		NyARRotMatrix rot=this._rotmatrix;//ちょっと無理のあるキャストなんとかしよう。
//		double[] rot = transrot.getArray();
		for (i = 0; i < NUMBER_OF_VERTEX; i++) {
			final int x2 = i+i;
			rot.getPoint3d(i_vertex3d[i],point3d);
			c_array[x2][0] = point3d.z * i_vertex2d[i].x - cpara[0 * 4 + 0] * point3d.x - cpara[0 * 4 + 1] * point3d.y - cpara[0 * 4 + 2] * point3d.z;// mat_c->m[j*2+0] = wz*pos2d[j][0]-cpara[0][0]*wx-cpara[0][1]*wy-cpara[0][2]*wz;
			c_array[x2 + 1][0] = point3d.z * i_vertex2d[i].y - cpara[1 * 4 + 1] * point3d.y - cpara[1 * 4 + 2] * point3d.z;// mat_c->m[j*2+1]= wz*pos2d[j][1]-cpara[1][1]*wy-cpara[1][2]*wz;
		}
		mat_e = this.wk_arGetTransMatSub_mat_e;// 次処理で値をもらうので、初期化の必要は無い。
		mat_f = this.wk_arGetTransMatSub_mat_f;// 次処理で値をもらうので、初期化の必要は無い。
		double[][] f_array = mat_f.getArray();

		mat_e.matrixMul(i_mat_b, mat_c);
		mat_f.matrixMul(i_mat_d, mat_e);

		// double[] trans=wk_arGetTransMatSub_trans;//double trans[3];
		o_trans[0] = f_array[0][0];// trans[0] = mat_f->m[0];
		o_trans[1] = f_array[1][0];
		o_trans[2] = f_array[2][0];// trans[2] = mat_f->m[2];
		ret = _rotmatrix.modifyMatrix(o_trans, i_vertex3d, i_vertex2d);
		
		
		
		
		
		for (i = 0; i < NUMBER_OF_VERTEX; i++) {
			final int x2 = i+i;
			rot.getPoint3d(i_vertex3d[i],point3d);
			c_array[x2][0] = point3d.z * i_vertex2d[i].x - cpara[0 * 4 + 0] * point3d.x - cpara[0 * 4 + 1] * point3d.y - cpara[0 * 4 + 2] * point3d.z;// mat_c->m[j*2+0]= wz*pos2d[j][0]-cpara[0][0]*wx-cpara[0][1]*wy-cpara[0][2]*wz;
			c_array[x2 + 1][0] = point3d.z * i_vertex2d[i].y - cpara[1 * 4 + 1] * point3d.y - cpara[1 * 4 + 2] * point3d.z;// mat_c->m[j*2+1]= wz*pos2d[j][1]-cpara[1][1]*wy-cpara[1][2]*wz;
		}

		mat_e.matrixMul(i_mat_b, mat_c);
		mat_f.matrixMul(i_mat_d, mat_e);
		o_trans[0] = f_array[0][0];// trans[0] = mat_f->m[0];
		o_trans[1] = f_array[1][0];
		o_trans[2] = f_array[2][0];// trans[2] = mat_f->m[2];
		ret = _rotmatrix.modifyMatrix(o_trans, i_vertex3d, i_vertex2d);
		return ret;
	}
}
