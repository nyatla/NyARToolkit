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
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.solver.*;
import jp.nyatla.nyartoolkit.core.transmat.optimize.*;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*;
import jp.nyatla.nyartoolkit.core.types.*;


/**
 * This class calculates ARMatrix from square information and holds it. --
 * 変換行列を計算して、結果を保持するクラス。
 * 
 */
public class NyARTransMat implements INyARTransMat
{
	private final static int AR_GET_TRANS_MAT_MAX_LOOP_COUNT = 5;// #define AR_GET_TRANS_MAT_MAX_LOOP_COUNT 5
	private final static double AR_GET_TRANS_MAT_MAX_FIT_ERROR = 1.0;// #define AR_GET_TRANS_MAT_MAX_FIT_ERROR 1.0
	private final static double AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR = 1.0;

	private final NyARDoublePoint2d _center=new NyARDoublePoint2d(0,0);
	private final NyARTransOffset _offset=new NyARTransOffset();
	protected NyARRotMatrix _rotmatrix;
	protected INyARTransportVectorSolver _transsolver;
	protected INyARRotMatrixOptimize _mat_optimize;
	private NyARCameraDistortionFactor _ref_dist_factor;

	/**
	 * 派生クラスで自分でメンバオブジェクトを指定したい場合はこちらを使う。
	 *
	 */
	protected NyARTransMat()
	{
		//_calculator,_rotmatrix,_mat_optimizeをコンストラクタの終了後に
		//作成して割り当ててください。
		return;
	}
	public NyARTransMat(NyARParam i_param) throws NyARException
	{
		final NyARCameraDistortionFactor dist=i_param.getDistortionFactor();
		final NyARPerspectiveProjectionMatrix pmat=i_param.getPerspectiveProjectionMatrix();
		this._transsolver=new NyARTransportVectorSolver_ARToolKit(pmat);
		//互換性が重要な時は、NyARRotMatrix_ARToolKitを使うこと。
		//理屈はNyARRotMatrix_NyARToolKitもNyARRotMatrix_ARToolKitも同じだけど、少しだけ値がずれる。
		this._rotmatrix = new NyARRotMatrix_NyARToolKit(pmat);
		this._mat_optimize=new NyARRotMatrixOptimize_O2(pmat);
		this._ref_dist_factor=dist;
	}

	public void setCenter(double i_x, double i_y)
	{
		this._center.x= i_x;
		this._center.y= i_y;
	}




	/**
	 * 頂点順序をi_directionに対応して並べ替えます。
	 * @param i_square
	 * @param i_direction
	 * @param o_sqvertex_ref
	 * @param o_liner_ref
	 */
	private final void initVertexOrder(NyARSquare i_square, int i_direction, NyARDoublePoint2d[] o_sqvertex_ref, NyARLinear[] o_liner_ref)
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
		return;
	}


	private final NyARDoublePoint2d[] __transMat_sqvertex_ref = new NyARDoublePoint2d[4];
	private final NyARDoublePoint2d[] __transMat_vertex_2d = NyARDoublePoint2d.createArray(4);
	private final NyARDoublePoint3d[] __transMat_vertex_3d = NyARDoublePoint3d.createArray(4);
	private final NyARLinear[] __transMat_linear_ref=new NyARLinear[4];
	private final NyARDoublePoint3d __transMat_trans=new NyARDoublePoint3d();
	/**
	 * double arGetTransMat( ARMarkerInfo *marker_info,double center[2], double width, double conv[3][4] )
	 * 
	 * @param i_square
	 * 計算対象のNyARSquareオブジェクト
	 * @param i_direction
	 * @param i_width
	 * @return
	 * @throws NyARException
	 */
	public void transMat(final NyARSquare i_square, int i_direction, double i_width, NyARTransMatResult o_result_conv) throws NyARException
	{
		final NyARDoublePoint2d[] sqvertex_ref = __transMat_sqvertex_ref;
		final NyARLinear[] linear_ref=__transMat_linear_ref;
		final NyARDoublePoint3d trans=this.__transMat_trans;
		
		//計算用に頂点情報を初期化（順番調整）
		initVertexOrder(i_square, i_direction, sqvertex_ref,linear_ref);
		
		//平行移動量計算機に、2D座標系をセット
		NyARDoublePoint2d[] vertex_2d=this.__transMat_vertex_2d;
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		this._ref_dist_factor.ideal2ObservBatch(sqvertex_ref, vertex_2d,4);		
		this._transsolver.set2dVertex(vertex_2d,4);
		
		//基準矩形の3D座標系を作成
		this._offset.setSquare(i_width,this._center);

		//回転行列を計算
		this._rotmatrix.initRotBySquare(linear_ref,sqvertex_ref);
		
		//回転後の3D座標系から、平行移動量を計算
		this._rotmatrix.getPoint3dBatch(this._offset.vertex,vertex_3d,4);
		this._transsolver.solveTransportVector(vertex_3d,trans);
		
		//計算結果の最適化(平行移動量と回転行列の最適化)
		this.optimize(this._rotmatrix, trans, this._transsolver,this._offset.vertex, vertex_2d);
		
		// マトリクスの保存
		this.updateMatrixValue(this._rotmatrix, this._offset.point, trans,o_result_conv);
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see jp.nyatla.nyartoolkit.core.transmat.INyARTransMat#transMatContinue(jp.nyatla.nyartoolkit.core.NyARSquare, int, double, jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult)
	 */
	public void transMatContinue(NyARSquare i_square, int i_direction, double i_width, NyARTransMatResult o_result_conv) throws NyARException
	{
		final NyARDoublePoint2d[] sqvertex_ref = __transMat_sqvertex_ref;
		final NyARLinear[] linear_ref=__transMat_linear_ref;
		final NyARDoublePoint3d trans=this.__transMat_trans;

		// io_result_convが初期値なら、transMatで計算する。
		if (!o_result_conv.has_value) {
			this.transMat(i_square, i_direction, i_width, o_result_conv);
			return;
		}

		//計算用に頂点情報を初期化（順番調整）
		initVertexOrder(i_square, i_direction, sqvertex_ref,linear_ref);

		
		//平行移動量計算機に、2D座標系をセット
		NyARDoublePoint2d[] vertex_2d=this.__transMat_vertex_2d;
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		this._ref_dist_factor.ideal2ObservBatch(sqvertex_ref, vertex_2d,4);		
		this._transsolver.set2dVertex(vertex_2d,4);
		
		//基準矩形の3D座標系を作成
		this._offset.setSquare(i_width,this._center);

		//回転行列を計算
		this._rotmatrix.initRotByPrevResult(o_result_conv);
		
		//回転後の3D座標系から、平行移動量を計算
		this._rotmatrix.getPoint3dBatch(this._offset.vertex,vertex_3d,4);
		this._transsolver.solveTransportVector(vertex_3d,trans);
		
		//計算結果の最適化(平行移動量と回転行列の最適化)
		double err=this.optimize(this._rotmatrix, trans, this._transsolver, this._offset.vertex, vertex_2d);
		
		// マトリクスの保存
		this.updateMatrixValue(this._rotmatrix, this._offset.point, trans,o_result_conv);
		
		// エラー値が許容範囲でなければTransMatをやり直し
		if (err > AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR) {
			// rotationを矩形情報で初期化
			this._rotmatrix.initRotBySquare(linear_ref,sqvertex_ref);
			//回転行列の平行移動量の計算
			this._rotmatrix.getPoint3dBatch(this._offset.vertex,vertex_3d,4);
			this._transsolver.solveTransportVector(vertex_3d,trans);
			//計算結果の最適化(this._rotmatrix,trans)
			final double err2=this.optimize(this._rotmatrix, trans, this._transsolver, this._offset.vertex, vertex_2d);
			//エラー値が低かったら値を差換え
			if (err2 < err) {
				// 良い値が取れたら、差換え
				this.updateMatrixValue(this._rotmatrix, this._offset.point, trans,o_result_conv);
			}
		}
		return;
	}
	private double optimize(NyARRotMatrix io_rotmat,NyARDoublePoint3d io_transvec,INyARTransportVectorSolver i_solver,NyARDoublePoint3d[] i_offset_3d,NyARDoublePoint2d[] i_2d_vertex) throws NyARException
	{
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		double err = -1;
		/* ループを抜けるタイミングをARToolKitと合わせるために変なことしてます。 */
		for (int i = 0;; i++) {
			// <arGetTransMat3>
			err = this._mat_optimize.modifyMatrix(io_rotmat, io_transvec, i_offset_3d, i_2d_vertex);
			io_rotmat.getPoint3dBatch(i_offset_3d,vertex_3d,4);
			i_solver.solveTransportVector(vertex_3d, io_transvec);
			
			err = this._mat_optimize.modifyMatrix(io_rotmat, io_transvec, i_offset_3d, i_2d_vertex);
			// //</arGetTransMat3>
			if (err < AR_GET_TRANS_MAT_MAX_FIT_ERROR || i == AR_GET_TRANS_MAT_MAX_LOOP_COUNT - 1) {
				break;
			}
			io_rotmat.getPoint3dBatch(i_offset_3d,vertex_3d,4);
			i_solver.solveTransportVector(vertex_3d, io_transvec);
		}
		return err;
	}	
	/**
	 * パラメータで変換行列を更新します。
	 * 
	 * @param i_rot
	 * @param i_off
	 * @param i_trans
	 */
	public void updateMatrixValue(NyARRotMatrix i_rot, NyARDoublePoint3d i_off, NyARDoublePoint3d i_trans,NyARTransMatResult o_result)
	{
		o_result.m00=i_rot.m00;
		o_result.m01=i_rot.m01;
		o_result.m02=i_rot.m02;
		o_result.m03=i_rot.m00 * i_off.x + i_rot.m01 * i_off.y + i_rot.m02 * i_off.z + i_trans.x;

		o_result.m10 = i_rot.m10;
		o_result.m11 = i_rot.m11;
		o_result.m12 = i_rot.m12;
		o_result.m13 = i_rot.m10 * i_off.x + i_rot.m11 * i_off.y + i_rot.m12 * i_off.z + i_trans.y;

		o_result.m20 = i_rot.m20;
		o_result.m21 = i_rot.m21;
		o_result.m22 = i_rot.m22;
		o_result.m23 = i_rot.m20 * i_off.x + i_rot.m21 * i_off.y + i_rot.m22 * i_off.z + i_trans.z;

		o_result.angle.setValue(i_rot.refAngle());
		o_result.has_value = true;
		return;
	}	
}
