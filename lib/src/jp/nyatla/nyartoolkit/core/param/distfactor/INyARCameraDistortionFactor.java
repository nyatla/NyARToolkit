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
package jp.nyatla.nyartoolkit.core.param.distfactor;

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、樽型歪み設定/解除クラスです。
 */
public interface INyARCameraDistortionFactor
{	
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
	 * この関数は、観察座標を理想座標へ変換します。
	 * 入力できる値範囲は、コンストラクタに設定したスクリーンサイズの範囲内です。
	 * @param ix
	 * 観察座標の値
	 * @param iy
	 * 観察座標の値
	 * @param o_point
	 * 理想座標を受け取るオブジェクト。
	 */	
	public void observ2Ideal(int ix, int iy, NyARDoublePoint2d o_point);

	/**
	 * 座標配列全てに対して、{@link #observ2Ideal(double, double, NyARDoublePoint2d)}を適応します。
	 * @param i_in
	 * @param o_out
	 * @param i_size
	 */
	public void observ2IdealBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size);
	
	public void observ2IdealBatch(NyARIntPoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size);



	
	
	
	
	
}
