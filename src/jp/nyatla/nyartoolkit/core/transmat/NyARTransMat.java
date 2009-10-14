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
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 * This class calculates ARMatrix from square information and holds it. --
 * 変換行列を計算して、結果を保持するクラス。
 * 
 */
public class NyARTransMat implements INyARTransMat
{
	private final static double FIT_DIFF_THRESHOLD = 0.01;
	private final static double FIT_DIFF_THRESHOLD_CONT = 1.0;

	private final NyARDoublePoint2d _center=new NyARDoublePoint2d(0,0);
	private final NyARTransOffset _offset=new NyARTransOffset();
	private NyARPerspectiveProjectionMatrix _projection_mat_ref;
	protected NyARRotMatrix _rotmatrix;
	protected INyARTransportVectorSolver _transsolver;
	protected NyARPartialDifferentiationOptimize _mat_optimize;


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
		this._transsolver=new NyARTransportVectorSolver(pmat,4);
		//互換性が重要な時は、NyARRotMatrix_ARToolKitを使うこと。
		//理屈はNyARRotMatrix_NyARToolKitもNyARRotMatrix_ARToolKitも同じだけど、少しだけ値がずれる。
		this._rotmatrix = new NyARRotMatrix(pmat);
		this._mat_optimize=new NyARPartialDifferentiationOptimize(pmat);
		this._ref_dist_factor=dist;
		this._projection_mat_ref=pmat;
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

		//現在のエラーレートを計算しておく
		double min_err=errRate(this._rotmatrix,trans, this._offset.vertex, vertex_2d,4,vertex_3d);
		NyARDoubleMatrix33 rot=this.__rot;
		//エラーレートが閾値超えてたらアゲイン
		if(min_err<FIT_DIFF_THRESHOLD_CONT){
			rot.setValue(this._rotmatrix);
			//最適化してみる。
			for (int i = 0;i<5; i++) {
				//変換行列の最適化
				this._mat_optimize.modifyMatrix(rot, trans, this._offset.vertex, vertex_2d, 4);
				double err=errRate(rot,trans,this._offset.vertex, vertex_2d,4,vertex_3d);
				//System.out.println("E:"+err);
				if(min_err-err<FIT_DIFF_THRESHOLD){
					//System.out.println("BREAK");
					break;
				}
				this._transsolver.solveTransportVector(vertex_3d, trans);
				this._rotmatrix.setValue(rot);
				min_err=err;
			}
			this.updateMatrixValue(this._rotmatrix, this._offset.point, trans,o_result_conv);
		}else{
			//回転行列を計算
			this._rotmatrix.initRotBySquare(linear_ref,sqvertex_ref);
			
			//回転後の3D座標系から、平行移動量を計算
			this._rotmatrix.getPoint3dBatch(this._offset.vertex,vertex_3d,4);
			this._transsolver.solveTransportVector(vertex_3d,trans);
			
			//計算結果の最適化(平行移動量と回転行列の最適化)
			this.optimize(this._rotmatrix, trans, this._transsolver,this._offset.vertex, vertex_2d);
			this.updateMatrixValue(this._rotmatrix, this._offset.point, trans,o_result_conv);
		}
		return;
	}
	private NyARDoubleMatrix33 __rot=new NyARDoubleMatrix33();
	private double optimize(NyARRotMatrix io_rotmat,NyARDoublePoint3d io_transvec,INyARTransportVectorSolver i_solver,NyARDoublePoint3d[] i_offset_3d,NyARDoublePoint2d[] i_2d_vertex) throws NyARException
	{
		//System.out.println("START");
		NyARDoublePoint3d[] vertex_3d=this.__transMat_vertex_3d;
		//初期のエラー値を計算
		double min_err=errRate(io_rotmat, io_transvec, i_offset_3d, i_2d_vertex,4,vertex_3d);
		NyARDoubleMatrix33 rot=this.__rot;
		rot.setValue(io_rotmat);
		for (int i = 0;i<5; i++) {
			//変換行列の最適化
			this._mat_optimize.modifyMatrix(rot, io_transvec, i_offset_3d, i_2d_vertex, 4);
			double err=errRate(rot,io_transvec, i_offset_3d, i_2d_vertex,4,vertex_3d);
			//System.out.println("E:"+err);
			if(min_err-err<FIT_DIFF_THRESHOLD){
				//System.out.println("BREAK");
				break;
			}
			i_solver.solveTransportVector(vertex_3d, io_transvec);
			io_rotmat.setValue(rot);
			min_err=err;
		}
		//System.out.println("END");
		return min_err;
	}
	
	//エラーレート計算機
	public double errRate(NyARDoubleMatrix33 io_rot,NyARDoublePoint3d i_trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d,int i_number_of_vertex,NyARDoublePoint3d[] o_rot_vertex) throws NyARException
	{
		NyARPerspectiveProjectionMatrix cp = this._projection_mat_ref;
		final double cp00=cp.m00;
		final double cp01=cp.m01;
		final double cp02=cp.m02;
		final double cp11=cp.m11;
		final double cp12=cp.m12;

		double err=0;
		for(int i=0;i<i_number_of_vertex;i++){
			double x3d,y3d,z3d;
			o_rot_vertex[i].x=x3d=io_rot.m00*i_vertex3d[i].x+io_rot.m01*i_vertex3d[i].y+io_rot.m02*i_vertex3d[i].z;
			o_rot_vertex[i].y=y3d=io_rot.m10*i_vertex3d[i].x+io_rot.m11*i_vertex3d[i].y+io_rot.m12*i_vertex3d[i].z;
			o_rot_vertex[i].z=z3d=io_rot.m20*i_vertex3d[i].x+io_rot.m21*i_vertex3d[i].y+io_rot.m22*i_vertex3d[i].z;
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

		o_result.has_value = true;
		return;
	}	
}
