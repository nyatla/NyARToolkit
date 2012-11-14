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

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、樽型歪み設定/解除クラスです。
 */
public interface INyARCameraDistortionFactor
{	
	/**
	 * この関数は、参照元から歪みパラメータ値をコピーします。
	 * @param i_ref
	 * コピー元のオブジェクト。
	 */
	public void copyFrom(INyARCameraDistortionFactor i_ref);

	/**
	 * この関数は、配列の値を歪みパラメータ値として、このインスタンスにセットします。
	 * @param i_factor
	 * 歪みパラメータ値を格納した配列。
	 */
	public void setValue(double[] i_factor);
	
	/**
	 * この関数は、パラメータ値を配列へ返します。
	 * @param o_factor
	 * 歪みパラメータ値の出力先配列。
	 */
	public void getValue(double[] o_factor);
	
	/**
	 * この関数は、歪みパラメータをスケール倍します。
	 * パラメータ値は、スケール値の大きさだけ、拡大、又は縮小します。
	 * @param i_x_scale
	 * x方向のパラメータ倍率
	 * @param i_y_scale
	 * y方向のパラメータ倍率
	 */
	public void changeScale(double i_x_scale,double i_y_scale);

	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(NyARDoublePoint2d i_in, NyARDoublePoint2d o_out);
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(NyARDoublePoint2d i_in, NyARIntPoint2d o_out);
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(double i_x,double i_y, NyARDoublePoint2d o_out);
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_x
	 * 変換元の座標
	 * @param i_y
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(double i_x,double i_y, NyARIntPoint2d o_out);
	
	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * i_inとo_outには、同じインスタンスを指定できます。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 */
	public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size);

	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * i_inとo_outには、同じインスタンスを指定できます。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 */
	public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARIntPoint2d[] o_out, int i_size);
	
	/**
	 * この関数は、座標を観察座標系から理想座標系へ変換します。
	 * @param ix
	 * 変換元の座標
	 * @param iy
	 * 変換元の座標
	 * @param o_point
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point);

	/**
	 * {@link #observ2Ideal(double, double, NyARDoublePoint2d)}のラッパーです。
	 * i_inとo_pointには、同じオブジェクトを指定できます。
	 * @param i_in
	 * @param o_point
	 */	
	public void observ2Ideal(NyARDoublePoint2d i_in, NyARDoublePoint2d o_point);

	/**
	 * 座標配列全てに対して、{@link #observ2Ideal(double, double, NyARDoublePoint2d)}を適応します。
	 * @param i_in
	 * @param o_out
	 * @param i_size
	 */
	public void observ2IdealBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size);
}
