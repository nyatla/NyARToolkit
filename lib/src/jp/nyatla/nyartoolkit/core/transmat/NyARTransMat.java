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
package jp.nyatla.nyartoolkit.core.transmat;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.solver.*;
import jp.nyatla.nyartoolkit.core.transmat.optimize.*;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

/**
 * このクラスは、NyARToolkitの計算式で、二次元矩形から３次元位置姿勢を推定します。
 */
public class NyARTransMat implements INyARTransMat
{	
	private NyARPerspectiveProjectionMatrix _ref_projection_mat;
	protected NyARRotMatrix _rotmatrix;
	protected NyARTransportVectorSolver _transsolver;
	protected NyARPartialDifferentiationOptimize _mat_optimize;


	private NyARCameraDistortionFactor _ref_dist_factor;

	/**
	 * コンストラクタです。
	 * 派生クラスで自分でメンバオブジェクトを指定したい場合はこちらを使います。
	 */
	protected NyARTransMat()
	{
		//_calculator,_rotmatrix,_mat_optimizeをコンストラクタの終了後に
		//作成して割り当ててください。
		return;
	}
	
	/**
	 * この関数は、コンストラクタから呼び出してください。
	 * @param i_distfactor
	 * 歪みの逆矯正に使うオブジェクト。
	 * @param i_projmat
	 * @throws NyARException
	 */
	private final void initInstance(NyARCameraDistortionFactor i_distfactor,NyARPerspectiveProjectionMatrix i_projmat) throws NyARException
	{
		this._transsolver=new NyARTransportVectorSolver(i_projmat,4);
		//互換性が重要な時は、NyARRotMatrix_ARToolKitを使うこと。
		//理屈はNyARRotMatrix_NyARToolKitもNyARRotMatrix_ARToolKitも同じだけど、少しだけ値がずれる。
		this._rotmatrix = new NyARRotMatrix(i_projmat);
		this._mat_optimize=new NyARPartialDifferentiationOptimize(i_projmat);
		this._ref_dist_factor=i_distfactor;
		this._ref_projection_mat=i_projmat;
		return;
	}
	/**
	 * コンストラクタです。
	 * 座標計算に必要なオブジェクトの参照値を元に、インスタンスを生成します。
	 * @param i_ref_distfactor
	 * 樽型歪み矯正オブジェクトの参照値です。歪み矯正が不要な時は、nullを指定します。
	 * @param i_ref_projmat
	 * 射影変換オブジェクトの参照値です。
	 * @throws NyARException
	 */
	public NyARTransMat(NyARCameraDistortionFactor i_ref_distfactor,NyARPerspectiveProjectionMatrix i_ref_projmat) throws NyARException
	{
		initInstance(i_ref_distfactor,i_ref_projmat);
		return;
	}
	/**
	 * コンストラクタです。
	 * 座標計算に必要なカメラパラメータの参照値を元に、インスタンスを生成します。
	 * @param i_param
	 * ARToolKit形式のカメラパラメータです。
	 * インスタンスは、この中から樽型歪み矯正オブジェクト、射影変換オブジェクトを参照します。
	 * @throws NyARException
	 */
	public NyARTransMat(NyARParam i_param) throws NyARException
	{
		initInstance(i_param.getDistortionFactor(),i_param.getPerspectiveProjectionMatrix());
		return;
	}

	private final NyARDoublePoint2d[] __transMat_vertex_2d = NyARDoublePoint2d.createArray(4);
	private final NyARDoublePoint3d[] __transMat_vertex_3d = NyARDoublePoint3d.createArray(4);
	private final NyARDoublePoint3d __transMat_trans=new NyARDoublePoint3d();
	
	/**
	 * 頂点情報を元に、エラー閾値を計算します。
	 * @param i_vertex
	 */
	private double makeErrThreshold(NyARDoublePoint2d[] i_vertex)
	{
		double a,b,l1,l2;
		a=i_vertex[0].x-i_vertex[2].x;
		b=i_vertex[0].y-i_vertex[2].y;
		l1=a*a+b*b;
		a=i_vertex[1].x-i_vertex[3].x;
		b=i_vertex[1].y-i_vertex[3].y;
		l2=a*a+b*b;
		return (Math.sqrt(l1>l2?l1:l2))/200;
	}
	
	/**
	 * この関数は、理想座標系の四角系を元に、位置姿勢変換行列を求めます。
	 * ARToolKitのarGetTransMatに該当します。
	 * @see INyARTransMat#transMatContinue
	 */
	public void transMat(NyARSquare i_square,NyARRectOffset i_offset, NyARTransMatResult o_result_conv) throws NyARException
	{
		final NyARDoublePoint3d trans=this.__transMat_trans;
		double err_threshold=makeErrThreshold(i_square.sqvertex);

		NyARDoublePoint2d[] vertex_2d;
		if(this._ref_dist_factor!=null){
			//歪み復元必要
			vertex_2d=this.__transMat_vertex_2d;
			this._ref_dist_factor.ideal2ObservBatch(i_square.sqvertex, vertex_2d,4);
		}else{
			//歪み復元は不要
			vertex_2d=i_square.sqvertex;
		}
		//平行移動量計算機に、2D座標系をセット
		this._transsolver.set2dVertex(vertex_2d,4);

		//回転行列を計算
		this._rotmatrix.initRotBySquare(i_square.line,i_square.sqvertex);
		
		//回転後の3D座標系から、平行移動量を計算
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		this._rotmatrix.getPoint3dBatch(i_offset.vertex,vertex_3d,4);
		this._transsolver.solveTransportVector(vertex_3d,trans);
		
		//計算結果の最適化(平行移動量と回転行列の最適化)
		this.optimize(this._rotmatrix, trans, this._transsolver,i_offset.vertex, vertex_2d,err_threshold,o_result_conv);
		return;
	}

	/**
	 * この関数は、理想座標系の四角系を元に、位置姿勢変換行列を求めます。
	 * 計算に過去の履歴を使う点が、{@link #transMat}と異なります。
	 * @see INyARTransMat#transMatContinue
	 */
	public final void transMatContinue(NyARSquare i_square,NyARRectOffset i_offset,NyARTransMatResult i_prev_result,NyARTransMatResult o_result) throws NyARException
	{
		final NyARDoublePoint3d trans=this.__transMat_trans;
		// io_result_convが初期値なら、transMatで計算する。
		if (!i_prev_result.has_value) {
			this.transMat(i_square,i_offset, o_result);
			return;
		}
		//過去のエラーレートを記録(ここれやるのは、i_prev_resultとo_resultに同じインスタンスを指定できるようにするため)
		double last_error=i_prev_result.last_error;
		
		//最適化計算の閾値を決定
		double err_threshold=makeErrThreshold(i_square.sqvertex);

		
		//平行移動量計算機に、2D座標系をセット
		NyARDoublePoint2d[] vertex_2d;
		if(this._ref_dist_factor!=null){
			vertex_2d=this.__transMat_vertex_2d;
			this._ref_dist_factor.ideal2ObservBatch(i_square.sqvertex, vertex_2d,4);		
		}else{
			vertex_2d=i_square.sqvertex;
		}
		this._transsolver.set2dVertex(vertex_2d,4);

		//回転行列を計算
		NyARRotMatrix rot=this._rotmatrix;
		rot.initRotByPrevResult(i_prev_result);
		
		//回転後の3D座標系から、平行移動量を計算
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		rot.getPoint3dBatch(i_offset.vertex,vertex_3d,4);
		this._transsolver.solveTransportVector(vertex_3d,trans);

		//現在のエラーレートを計算
		double min_err=errRate(this._rotmatrix,trans,i_offset.vertex, vertex_2d,4,vertex_3d);
		//結果をストア
		o_result.setValue(rot,trans,min_err);
		//エラーレートの判定
		if(min_err<last_error+err_threshold){
//			System.out.println("TR:ok");
			//最適化してみる。
			for (int i = 0;i<5; i++) {
				//変換行列の最適化
				this._mat_optimize.modifyMatrix(rot, trans, i_offset.vertex, vertex_2d, 4);
				double err=errRate(rot,trans,i_offset.vertex, vertex_2d,4,vertex_3d);
				//System.out.println("E:"+err);
				if(min_err-err<err_threshold/2){
					//System.out.println("BREAK");
					break;
				}
				this._transsolver.solveTransportVector(vertex_3d, trans);				
				o_result.setValue(rot,trans,err);
				min_err=err;
			}
		}else{
//			System.out.println("TR:again");
			//回転行列を計算
			rot.initRotBySquare(i_square.line,i_square.sqvertex);
			
			//回転後の3D座標系から、平行移動量を計算
			rot.getPoint3dBatch(i_offset.vertex,vertex_3d,4);
			this._transsolver.solveTransportVector(vertex_3d,trans);
			
			//計算結果の最適化(平行移動量と回転行列の最適化)
			this.optimize(rot,trans, this._transsolver,i_offset.vertex, vertex_2d,err_threshold,o_result);
		}
		return;
	}

/*	public double transMatContinue(NyARSquare i_square,NyARRectOffset i_offset,double i_prev_error,NyARDoublePoint3d io_angle,NyARDoublePoint3d io_trans) throws NyARException
	{
		final NyARDoublePoint3d trans=this.__transMat_trans;
		
		//最適化計算の閾値を決定
		double err_threshold=makeErrThreshold(i_square.sqvertex);

		
		//平行移動量計算機に、2D座標系をセット
		NyARDoublePoint2d[] vertex_2d=this.__transMat_vertex_2d;
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		this._ref_dist_factor.ideal2ObservBatch(i_square.sqvertex, vertex_2d,4);		
		this._transsolver.set2dVertex(vertex_2d,4);
		
		//回転行列を計算
		this._rotmatrix.initRotByAngle(io_angle);
		
		//回転後の3D座標系から、平行移動量を計算
		this._rotmatrix.getPoint3dBatch(i_offset.vertex,vertex_3d,4);
		this._transsolver.solveTransportVector(vertex_3d,trans);

		//現在のエラーレートを計算しておく
		double min_err=errRate(this._rotmatrix,trans,i_offset.vertex, vertex_2d,4,vertex_3d);
		NyARDoubleMatrix33 rot=this.__rot;
		//エラーレートが前回のエラー値より閾値分大きかったらアゲイン
		if(min_err<i_prev_error+err_threshold){
			rot.setValue(this._rotmatrix);
			//最適化してみる。
			for (int i = 0;i<5; i++) {
				//変換行列の最適化
				this._mat_optimize.modifyMatrix(rot, trans, i_offset.vertex, vertex_2d, 4);
				double err=errRate(rot,trans,i_offset.vertex, vertex_2d,4,vertex_3d);
				//System.out.println("E:"+err);
				if(min_err-err<err_threshold/2){
					//System.out.println("BREAK");
					break;
				}
				this._transsolver.solveTransportVector(vertex_3d, trans);
				this._rotmatrix.setValue(rot);
				min_err=err;
			}
			this.updateMatrixValue(this._rotmatrix,  trans,o_result_conv);
		}else{
			//回転行列を計算
			this._rotmatrix.initRotBySquare(i_square.line,i_square.sqvertex);
			
			//回転後の3D座標系から、平行移動量を計算
			this._rotmatrix.getPoint3dBatch(i_offset.vertex,vertex_3d,4);
			this._transsolver.solveTransportVector(vertex_3d,trans);
			
			//計算結果の最適化(平行移動量と回転行列の最適化)
			min_err=this.optimize(this._rotmatrix, trans, this._transsolver,i_offset.vertex, vertex_2d,err_threshold);
			this.updateMatrixValue(this._rotmatrix, trans,o_result_conv);
		}
		return min_err;
	}	
	
*/
	/**
	 * 
	 * @param iw_rotmat
	 * @param iw_transvec
	 * @param i_solver
	 * @param i_offset_3d
	 * @param i_2d_vertex
	 * @param i_err_threshold
	 * @param o_result
	 * @return
	 * @throws NyARException
	 */
	private void optimize(NyARRotMatrix iw_rotmat,NyARDoublePoint3d iw_transvec,INyARTransportVectorSolver i_solver,NyARDoublePoint3d[] i_offset_3d,NyARDoublePoint2d[] i_2d_vertex,double i_err_threshold,NyARTransMatResult o_result) throws NyARException
	{
		//System.out.println("START");
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		//初期のエラー値を計算
		double min_err=errRate(iw_rotmat, iw_transvec, i_offset_3d, i_2d_vertex,4,vertex_3d);
		o_result.setValue(iw_rotmat,iw_transvec,min_err);

		for (int i = 0;i<5; i++) {
			//変換行列の最適化
			this._mat_optimize.modifyMatrix(iw_rotmat,iw_transvec, i_offset_3d, i_2d_vertex,4);
			double err=errRate(iw_rotmat,iw_transvec, i_offset_3d, i_2d_vertex,4,vertex_3d);
			//System.out.println("E:"+err);
			if(min_err-err<i_err_threshold){
				//System.out.println("BREAK");
				break;
			}
			i_solver.solveTransportVector(vertex_3d,iw_transvec);
			o_result.setValue(iw_rotmat,iw_transvec,err);
			min_err=err;
		}
		//System.out.println("END");
		return;
	}

	/**
	 * この関数は、姿勢行列のエラーレートを計算します。
	 * エラーレートは、回転行列、平行移動量、オフセット、観察座標から計算します。
	 * 通常、ユーザが使うことはありません。
	 * @param i_rot
	 * 回転行列
	 * @param i_trans
	 * 平行移動量
	 * @param i_vertex3d
	 * オフセット位置
	 * @param i_vertex2d
	 * 理想座標
	 * @param i_number_of_vertex
	 * 評価する頂点数
	 * @param o_rot_vertex
	 * 計算過程で得られた、各頂点の三次元座標
	 * @return
	 * エラーレート(Σ(理想座標と計算座標の距離[n]^2))
	 * @throws NyARException
	 */
	public final double errRate(NyARDoubleMatrix33 i_rot,NyARDoublePoint3d i_trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d,int i_number_of_vertex,NyARDoublePoint3d[] o_rot_vertex) throws NyARException
	{
		NyARPerspectiveProjectionMatrix cp = this._ref_projection_mat;
		final double cp00=cp.m00;
		final double cp01=cp.m01;
		final double cp02=cp.m02;
		final double cp11=cp.m11;
		final double cp12=cp.m12;

		double err=0;
		for(int i=0;i<i_number_of_vertex;i++){
			double x3d,y3d,z3d;
			o_rot_vertex[i].x=x3d=i_rot.m00*i_vertex3d[i].x+i_rot.m01*i_vertex3d[i].y+i_rot.m02*i_vertex3d[i].z;
			o_rot_vertex[i].y=y3d=i_rot.m10*i_vertex3d[i].x+i_rot.m11*i_vertex3d[i].y+i_rot.m12*i_vertex3d[i].z;
			o_rot_vertex[i].z=z3d=i_rot.m20*i_vertex3d[i].x+i_rot.m21*i_vertex3d[i].y+i_rot.m22*i_vertex3d[i].z;
			x3d+=i_trans.x;
			y3d+=i_trans.y;
			z3d+=i_trans.z;
			
			//射影変換
			double x2d=x3d*cp00+y3d*cp01+z3d*cp02;
			double y2d=y3d*cp11+z3d*cp12;
			double h2d=z3d;
			
			//エラーレート計算
			double t1=i_vertex2d[i].x-x2d/h2d;
			double t2=i_vertex2d[i].y-y2d/h2d;
			err+=t1*t1+t2*t2;
			
		}
		return err/i_number_of_vertex;
	}
}
