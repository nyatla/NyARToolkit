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
import jp.nyatla.nyartoolkit.core.NyARSquare;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.fitveccalc.NyARFitVecCalculator;
import jp.nyatla.nyartoolkit.core.transmat.optimize.*;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.NyARRotMatrix;
import jp.nyatla.nyartoolkit.core.types.*;


/**
 * This class calculates ARMatrix from square information and holds it. --
 * 変換行列を計算して、結果を保持するクラス。
 * 
 */
public class NyARTransMat implements INyARTransMat
{
	private final static double AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR = 1.0;

	private final NyARDoublePoint2d _center=new NyARDoublePoint2d(0,0);
	private final NyARTransOffset _offset=new NyARTransOffset();
	private final NyARRotMatrix _rotmatrix;
	private final NyARFitVecCalculator _calculator;
	private final INyARRotTransOptimize _mat_optimize;

	protected NyARTransMat(NyARParam i_param,NyARRotMatrix i_rotmatrix,INyARRotTransOptimize i_optimize)
	{
		final NyARCameraDistortionFactor dist=i_param.getDistortionFactor();
		final NyARPerspectiveProjectionMatrix pmat=i_param.getPerspectiveProjectionMatrix();
		this._calculator=new NyARFitVecCalculator(pmat,dist);
		this._rotmatrix =i_rotmatrix;
		this._mat_optimize=i_optimize;		
	}
	public NyARTransMat(NyARParam i_param) throws NyARException
	{
		final NyARCameraDistortionFactor dist=i_param.getDistortionFactor();
		final NyARPerspectiveProjectionMatrix pmat=i_param.getPerspectiveProjectionMatrix();
		this._calculator=new NyARFitVecCalculator(pmat,dist);
		this._rotmatrix = new NyARRotMatrix(pmat);
		this._mat_optimize=new NyARRotTransOptimize(pmat);
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
		
		//基準矩形を設定
		this._offset.setSquare(i_width,this._center);

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
		o_result_conv.updateMatrixValue(this._rotmatrix, this._offset.point, trans);
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
		return;
	}
}
