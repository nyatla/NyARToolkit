/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.transmat;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.rasterdriver.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.solver.*;
import jp.nyatla.nyartoolkit.core.transmat.optimize.artoolkit.INyARRotMatrixOptimize;
import jp.nyatla.nyartoolkit.core.transmat.optimize.artoolkit.NyARRotMatrixOptimize_O2;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;


/**
 * このクラスは、ARToolKitと同じ方法で、二次元矩形から３次元位置姿勢を推定します。
 */
public class NyARTransMat_ARToolKit implements INyARTransMat
{
	private final static int AR_GET_TRANS_MAT_MAX_LOOP_COUNT = 5;// #define AR_GET_TRANS_MAT_MAX_LOOP_COUNT 5
	private final static double AR_GET_TRANS_MAT_MAX_FIT_ERROR = 1.0;// #define AR_GET_TRANS_MAT_MAX_FIT_ERROR 1.0
	private final static double AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR = 1.0;

	/** 回転行列の計算オブジェクト*/
	protected NyARRotMatrix_ARToolKit _rotmatrix;
	/** 平行移動量の計算オブジェクト*/
	protected INyARTransportVectorSolver _transsolver;
	/** 姿勢行列最適化オブジェクト*/
	protected INyARRotMatrixOptimize _mat_optimize;
	private INyARCameraDistortionFactor _ref_dist_factor;

	/**
	 * コンストラクタです。
	 * 派生クラスで自分でメンバオブジェクトを指定したい場合はこちらを使います。
	 */
	protected NyARTransMat_ARToolKit()
	{
		//_calculator,_rotmatrix,_mat_optimizeをコンストラクタの終了後に
		//作成して割り当ててください。
		return;
	}
	/**
	 * コンストラクタです。
	 * 座標計算に必要なオブジェクトの参照値を元に、インスタンスを生成します。
	 * @param i_ref_distfactor
	 * 樽型歪み矯正オブジェクトの参照値です。歪み矯正が不要な時は、nullを指定します。
	 * @param i_ref_projmat
	 * 射影変換オブジェクトの参照値です。
	 * @throws NyARRuntimeException
	 */
	public NyARTransMat_ARToolKit(INyARCameraDistortionFactor i_ref_distfactor,NyARPerspectiveProjectionMatrix i_ref_projmat)
	{
		final INyARCameraDistortionFactor dist=i_ref_distfactor;
		final NyARPerspectiveProjectionMatrix pmat=i_ref_projmat;
		this._transsolver=new NyARTransportVectorSolver_ARToolKit(pmat);
		//互換性が重要な時は、NyARRotMatrix_ARToolKitを使うこと。
		//理屈はNyARRotMatrix_NyARToolKitもNyARRotMatrix_ARToolKitも同じだけど、少しだけ値がずれる。
		this._rotmatrix = new NyARRotMatrix_ARToolKit_O2(pmat);
		this._mat_optimize=new NyARRotMatrixOptimize_O2(pmat);
		this._ref_dist_factor=dist;		return;
	}
	/**
	 * コンストラクタです。
	 * 座標計算に必要なカメラパラメータの参照値を元に、インスタンスを生成します。
	 * @param i_param
	 * ARToolKit形式のカメラパラメータです。
	 * インスタンスは、この中から樽型歪み矯正オブジェクト、射影変換オブジェクトを参照します。
	 * @throws NyARRuntimeException
	 */	
	public NyARTransMat_ARToolKit(NyARParam i_param)
	{
		this(i_param.getDistortionFactor(),i_param.getPerspectiveProjectionMatrix());
	}

	private final NyARDoublePoint2d[] __transMat_vertex_2d = NyARDoublePoint2d.createArray(4);
	private final NyARDoublePoint3d[] __transMat_vertex_3d = NyARDoublePoint3d.createArray(4);
	private final NyARDoublePoint3d __transMat_trans=new NyARDoublePoint3d();
	/**
	 * この関数は、理想座標系の四角系を元に、位置姿勢変換行列を求めます。
	 * ARToolKitのarGetTransMatに該当します。
	 * @see INyARTransMat#transMatContinue
	 */
	public boolean transMat(NyARSquare i_square,NyARRectOffset i_offset, NyARDoubleMatrix44 o_result,NyARTransMatResultParam o_param)
	{
		final NyARDoublePoint3d trans=this.__transMat_trans;
		
		//平行移動量計算機に、2D座標系をセット
		NyARDoublePoint2d[] vertex_2d;
		if(this._ref_dist_factor!=null){
			//歪み復元必要
			vertex_2d=this.__transMat_vertex_2d;
			this._ref_dist_factor.ideal2ObservBatch(i_square.sqvertex, vertex_2d,4);
		}else{
			//歪み復元は不要
			vertex_2d=i_square.sqvertex;
		}
		this._transsolver.set2dVertex(vertex_2d,4);
		//回転行列を計算
		if(!this._rotmatrix.initRotBySquare(i_square.line,i_square.sqvertex)){
			return false;
		}
		
		//回転後の3D座標系から、平行移動量を計算
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		this._rotmatrix.getPoint3dBatch(i_offset.vertex,vertex_3d,4);
		this._transsolver.solveTransportVector(vertex_3d,trans);
		
		//計算結果の最適化(平行移動量と回転行列の最適化)
		double err=this.optimize(this._rotmatrix, trans, this._transsolver,i_offset.vertex, vertex_2d);
		
		// マトリクスの保存
		o_result.setValue(this._rotmatrix,trans);
		if(o_param!=null){
			o_param.last_error=err;
		}
		return true;
	}
	/**
	 * ARToolkitと同じ結果を返します。i_prev_err引数は無視されますので、0を指定してください。
	 * @see INyARTransMat#transMatContinue
	 */
	public boolean transMatContinue(NyARSquare i_square,NyARRectOffset i_offset, NyARDoubleMatrix44 i_prev_result,double i_prev_err,NyARDoubleMatrix44 o_result,NyARTransMatResultParam o_param)
	{
		final NyARDoublePoint3d trans=this.__transMat_trans;
		
		//平行移動量計算機に、2D座標系をセット
		NyARDoublePoint2d[] vertex_2d;
		if(this._ref_dist_factor!=null){
			//歪み復元必要
			vertex_2d=this.__transMat_vertex_2d;
			this._ref_dist_factor.ideal2ObservBatch(i_square.sqvertex, vertex_2d,4);
		}else{
			//歪み復元は不要
			vertex_2d=i_square.sqvertex;
		}
		this._transsolver.set2dVertex(vertex_2d,4);

		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		//回転行列を計算
		this._rotmatrix.initRotByPrevResult(i_prev_result);
		
		//回転後の3D座標系から、平行移動量を計算
		this._rotmatrix.getPoint3dBatch(i_offset.vertex,vertex_3d,4);
		this._transsolver.solveTransportVector(vertex_3d,trans);
		
		//計算結果の最適化(平行移動量と回転行列の最適化)
		double err=this.optimize(this._rotmatrix, trans, this._transsolver,i_offset.vertex, vertex_2d);
		
		if (err > AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR) {
			return false;
		}
		// マトリクスの保存
		o_result.setValue(this._rotmatrix,  trans);
		//エラー値保存
		if(o_param!=null){
			o_param.last_error=err;
		}
		return true;
	}
	private double optimize(NyARRotMatrix_ARToolKit io_rotmat,NyARDoublePoint3d io_transvec,INyARTransportVectorSolver i_solver,NyARDoublePoint3d[] i_offset_3d,NyARDoublePoint2d[] i_2d_vertex)
	{
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		double err = -1;
		//System.out.println("START");
		// ループを抜けるタイミングをARToolKitと合わせるために変なことしてます。 
		for (int i = 0;; i++) {
			// <arGetTransMat3>
			err = this._mat_optimize.modifyMatrix(io_rotmat, io_transvec, i_offset_3d, i_2d_vertex);
			io_rotmat.getPoint3dBatch(i_offset_3d,vertex_3d,4);
			i_solver.solveTransportVector(vertex_3d, io_transvec);
			
			err = this._mat_optimize.modifyMatrix(io_rotmat, io_transvec, i_offset_3d, i_2d_vertex);
			//System.out.println("E:"+err*4);
			// //</arGetTransMat3>
			if (err < AR_GET_TRANS_MAT_MAX_FIT_ERROR || i == AR_GET_TRANS_MAT_MAX_LOOP_COUNT - 1) {
				break;
			}
			io_rotmat.getPoint3dBatch(i_offset_3d,vertex_3d,4);
			i_solver.solveTransportVector(vertex_3d, io_transvec);
		}
		//System.out.println("END");
		return err;
	}	
}
