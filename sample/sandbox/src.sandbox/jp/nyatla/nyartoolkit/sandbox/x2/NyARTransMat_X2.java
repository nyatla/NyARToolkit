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
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point2d;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point3d;


/**
 * This class calculates ARMatrix from square information and holds it. --
 * 変換行列を計算して、結果を保持するクラス。
 * 
 */
public class NyARTransMat_X2 implements INyARTransMat
{
	private final NyARFixedFloat16Point2d _center=new NyARFixedFloat16Point2d();
	private final NyARFixedFloatTransOffset _offset=new NyARFixedFloatTransOffset();
	private NyARFixedFloatRotMatrix _rotmatrix;
	private NyARFixedFloatFitVecCalculator _calculator;
	private NyARFixedFloatRotTransOptimize_O2 _mat_optimize;

	public NyARTransMat_X2(NyARParam i_param) throws NyARException
	{
		final NyARPerspectiveProjectionMatrix pmat=i_param.getPerspectiveProjectionMatrix();
		this._calculator=new NyARFixedFloatFitVecCalculator(pmat,i_param.getDistortionFactor());
		this._rotmatrix = new NyARFixedFloatRotMatrix(pmat);
		this._mat_optimize=new NyARFixedFloatRotTransOptimize_O2(pmat);
	}

	public void setCenter(double i_x, double i_y)
	{
		this._center.x= (long)i_x*NyMath.FIXEDFLOAT16_1;
		this._center.y= (long)i_y*NyMath.FIXEDFLOAT16_1;
		return;
	}
	/**
	 * 頂点順序をi_directionに対応して並べ替えます。
	 * @param i_square
	 * @param i_direction
	 * @param o_sqvertex_ref
	 * @param o_liner_ref
	 */
	private final void initVertexOrder(NyARSquare i_square, int i_direction, NyARFixedFloat16Point2d[] o_sqvertex_ref, NyARLinear[] o_liner_ref)
	{
		//頂点順序を考慮した矩形の頂点情報
		o_sqvertex_ref[0].x= (long)(i_square.sqvertex[(4 - i_direction) % 4].x*NyMath.FIXEDFLOAT16_1);
		o_sqvertex_ref[0].y= (long)(i_square.sqvertex[(4 - i_direction) % 4].y*NyMath.FIXEDFLOAT16_1);
		o_sqvertex_ref[1].x= (long)(i_square.sqvertex[(5 - i_direction) % 4].x*NyMath.FIXEDFLOAT16_1);
		o_sqvertex_ref[1].y= (long)(i_square.sqvertex[(5 - i_direction) % 4].y*NyMath.FIXEDFLOAT16_1);
		o_sqvertex_ref[2].x= (long)(i_square.sqvertex[(6 - i_direction) % 4].x*NyMath.FIXEDFLOAT16_1);
		o_sqvertex_ref[2].y= (long)(i_square.sqvertex[(6 - i_direction) % 4].y*NyMath.FIXEDFLOAT16_1);
		o_sqvertex_ref[3].x= (long)(i_square.sqvertex[(7 - i_direction) % 4].x*NyMath.FIXEDFLOAT16_1);	
		o_sqvertex_ref[3].y= (long)(i_square.sqvertex[(7 - i_direction) % 4].y*NyMath.FIXEDFLOAT16_1);	
		o_liner_ref[0]=i_square.line[(4 - i_direction) % 4];
		o_liner_ref[1]=i_square.line[(5 - i_direction) % 4];
		o_liner_ref[2]=i_square.line[(6 - i_direction) % 4];
		o_liner_ref[3]=i_square.line[(7 - i_direction) % 4];
		return;
	}


	private final NyARFixedFloat16Point2d[] __transMat_sqvertex_ref = NyARFixedFloat16Point2d.createArray(4);
	private final NyARLinear[] __transMat_linear_ref=new NyARLinear[4];
	private final NyARFixedFloat16Point3d __transMat_trans=new NyARFixedFloat16Point3d();
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
		final NyARFixedFloat16Point2d[] sqvertex_ref = __transMat_sqvertex_ref;
		final NyARLinear[] linear_ref=__transMat_linear_ref;
		final NyARFixedFloat16Point3d trans=this.__transMat_trans;
	
		//計算用に頂点情報を初期化（順番調整）
		initVertexOrder(i_square, i_direction, sqvertex_ref,linear_ref);
		
		//基準矩形を設定
		this._offset.setSquare((long)(i_width*NyMath.FIXEDFLOAT16_1),this._center);

		// rotationを矩形情報から計算
		this._rotmatrix.initRotBySquare(linear_ref,sqvertex_ref);

		//平行移動量計算機にオフセット頂点をセット
		this._calculator.setOffsetSquare(this._offset);
		
		//平行移動量計算機に適応先矩形の情報をセット
		this._calculator.setFittedSquare(sqvertex_ref);	

		//回転行列の平行移動量の計算
		this._calculator.calculateTransfer(this._rotmatrix,trans);
		
		//計算結果の最適化(this._rotmatrix,trans)
		this._mat_optimize.optimize(this._rotmatrix,trans,this._calculator);
		
		// マトリクスの保存
		this.updateMatrixValue(this._rotmatrix, this._offset.point, trans,o_result_conv);
		return;
	}
	/**
	 * double arGetTransMatCont( ARMarkerInfo *marker_info, double prev_conv[3][4],double center[2], double width, double conv[3][4] )
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
	public void transMatContinue(NyARSquare i_square, int i_direction, double i_width, NyARTransMatResult io_result_conv) throws NyARException
	{
		/*	今度実装
		final NyARDoublePoint2d[] sqvertex_ref = __transMat_sqvertex_ref;
		final NyARLinear[] linear_ref=__transMat_linear_ref;
		final NyARDoublePoint3d trans=this.__transMat_trans;

		// io_result_convが初期値なら、transMatで計算する。
		if (!io_result_conv.hasValue()) {
			this.transMat(i_square, i_direction, i_width, io_result_conv);
			return;
		}
		
		//基準矩形を設定
		this._offset.setSquare(i_width,this._center);

		// rotationを矩形情報を一つ前の変換行列で初期化
		this._rotmatrix.initRotByPrevResult(io_result_conv);

		//平行移動量計算機に、オフセット頂点をセット
		this._calculator.setOffsetSquare(this._offset);
		
		//平行移動量計算機に、適応先矩形の情報をセット
		this._calculator.setFittedSquare(sqvertex_ref);	
				
		//回転行列の平行移動量の計算
		this._calculator.calculateTransfer(this._rotmatrix,trans);
		
		//計算結果の最適化(this._rotmatrix,trans)
		final double err=this._mat_optimize.optimize(this._rotmatrix,trans,this._calculator);
		
		//計算結果を保存
		io_result_conv.updateMatrixValue(this._rotmatrix, this._offset.point, trans);

		// エラー値が許容範囲でなければTransMatをやり直し
		if (err > AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR) {
			// rotationを矩形情報で初期化
			this._rotmatrix.initRotBySquare(linear_ref,sqvertex_ref);
			//回転行列の平行移動量の計算
			this._calculator.calculateTransfer(this._rotmatrix,trans);
			//計算結果の最適化(this._rotmatrix,trans)
			final double err2=this._mat_optimize.optimize(this._rotmatrix,trans,this._calculator);
			//エラー値が低かったら値を差換え
			if (err2 < err) {
				// 良い値が取れたら、差換え
				io_result_conv.updateMatrixValue(this._rotmatrix, this._offset.point, trans);
			}
		}		
		return;*/
		NyARException.notImplement();
	}
	public void updateMatrixValue(NyARFixedFloatRotMatrix i_rot, NyARFixedFloat16Point3d i_off, NyARFixedFloat16Point3d i_trans,NyARTransMatResult o_result)
	{
		o_result.m00=(double)i_rot.m00/NyMath.FIXEDFLOAT24_1;
		o_result.m01=(double)i_rot.m01/NyMath.FIXEDFLOAT24_1;
		o_result.m02=(double)i_rot.m02/NyMath.FIXEDFLOAT24_1;
		o_result.m03=(double)(((i_rot.m00 * i_off.x + i_rot.m01 * i_off.y + i_rot.m02 * i_off.z)>>24) + i_trans.x)/NyMath.FIXEDFLOAT16_1;

		o_result.m10 =(double)i_rot.m10/NyMath.FIXEDFLOAT24_1;
		o_result.m11 =(double)i_rot.m11/NyMath.FIXEDFLOAT24_1;
		o_result.m12 =(double)i_rot.m12/NyMath.FIXEDFLOAT24_1;
		o_result.m13 =(double)(((i_rot.m10 * i_off.x + i_rot.m11 * i_off.y + i_rot.m12 * i_off.z)>>24) + i_trans.y)/NyMath.FIXEDFLOAT16_1;

		o_result.m20 =(double)i_rot.m20/NyMath.FIXEDFLOAT24_1;
		o_result.m21 =(double)i_rot.m21/NyMath.FIXEDFLOAT24_1;
		o_result.m22 =(double)i_rot.m22/NyMath.FIXEDFLOAT24_1;
		o_result.m23 =(double)(((i_rot.m20 * i_off.x + i_rot.m21 * i_off.y + i_rot.m22 * i_off.z)>>24) + i_trans.z)/NyMath.FIXEDFLOAT16_1;
		
		final NyARFixedFloat16Point3d angle=i_rot.refAngle();

		o_result.has_value = true;
		return;
	}	
}
