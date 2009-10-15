/* 
 * PROJECT: NyARToolkit
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
package jp.nyatla.nyartoolkit.core.param;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

public interface INyARCameraDistortionFactor
{
	/**
	 * int arParamIdeal2Observ( const double dist_factor[4], const double ix,const double iy,double *ox, double *oy ) 関数の代替関数
	 * 
	 * @param i_in
	 * @param o_out
	 */
	public void ideal2Observ(final NyARDoublePoint2d i_in, NyARDoublePoint2d o_out);
	/**
	 * ideal2Observをまとめて実行します。
	 * @param i_in
	 * @param o_out
	 */
	public void ideal2ObservBatch(final NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size);
	/**
	 * int arParamObserv2Ideal( const double dist_factor[4], const double ox,const double oy,double *ix, double *iy );
	 * 
	 * @param ix
	 * @param iy
	 * @param ix
	 * @param iy
	 * @return
	 */
	public void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point);
	/**
	 * 指定範囲のobserv2Idealをまとめて実行して、結果をo_idealに格納します。
	 * @param i_x_coord
	 * @param i_y_coord
	 * @param i_start
	 *            coord開始点
	 * @param i_num
	 *            計算数
	 * @param o_ideal
	 *            出力バッファ[i_num][2]であること。
	 */
	public void observ2IdealBatch(int[] i_x_coord, int[] i_y_coord,int i_start, int i_num, double[] o_x_coord,double[] o_y_coord);

}
