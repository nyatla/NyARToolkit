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
package jp.nyatla.nyartoolkit.core.transmat.rotmatrix;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
/**
 * 回転行列計算用の、3x3行列
 *
 */
public abstract class NyARRotMatrix extends NyARDoubleMatrix33
{	
	/**
	 * NyARTransMatResultの内容からNyARRotMatrixを復元します。
	 * @param i_prev_result
	 */
	public abstract void initRotByPrevResult(NyARTransMatResult i_prev_result);
	public abstract void initRotBySquare(final NyARLinear[] i_linear,final NyARDoublePoint2d[] i_sqvertex) throws NyARException;
	/**
	 * int arGetAngle( double rot[3][3], double *wa, double *wb, double *wc )
	 * Optimize:2008.04.20:STEP[481→433]
	 * 3x3変換行列から、回転角を復元して返します。
	 * @param o_angle
	 * @return
	 */
	public abstract void getAngle(final NyARDoublePoint3d o_angle);
	/**
	 * 回転角から回転行列を計算してセットします。
	 * @param i_x
	 * @param i_y
	 * @param i_z
	 */
	public abstract void setAngle(final double i_x, final double i_y, final double i_z);
	/**
	 * i_in_pointを変換行列で座標変換する。
	 * @param i_in_point
	 * @param i_out_point
	 */	
	public abstract void getPoint3d(final NyARDoublePoint3d i_in_point,final NyARDoublePoint3d i_out_point);
	/**
	 * 複数の頂点を一括して変換する
	 * @param i_in_point
	 * @param i_out_point
	 * @param i_number_of_vertex
	 */
	public abstract void getPoint3dBatch(final NyARDoublePoint3d[] i_in_point,NyARDoublePoint3d[] i_out_point,int i_number_of_vertex);
}
