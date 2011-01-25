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
 * パラメータには、ARToolKitの樽型歪みパラメータを使います。
 * <p>アルゴリズム - 
 * このクラスでは、歪み矯正前の座標を観察座標系、歪み矯正後の座標を理想座標系と呼びます。
 * パラメータと理論については、以下の資料、11pageを参照してください。
 * http://www.hitl.washington.edu/artoolkit/Papers/ART02-Tutorial.pdf
 * <pre>
 * x=x(xi-x0),y=s(yi-y0)
 * d^2=x^2+y^2
 * p=(1-fd^2)
 * xd=px+x0,yd=py+y0
 * </pre>
 * </p>
 * このクラスは{@link NyARParam}に所有されることを前提にしており、単独の仕様は考慮されていません。
 */
public class NyARCameraDistortionFactor
{
	
	private static final int PD_LOOP = 3;
	private double _f0;//x0
	private double _f1;//y0
	private double _f2;//100000000.0*f
	private double _f3;//s
	
	
	/**
	 * この関数は、参照元から歪みパラメータ値をコピーします。
	 * @param i_ref
	 * コピー元のオブジェクト。
	 */
	public void copyFrom(NyARCameraDistortionFactor i_ref)
	{
		this._f0=i_ref._f0;
		this._f1=i_ref._f1;
		this._f2=i_ref._f2;
		this._f3=i_ref._f3;
		return;
	}

	/**
	 * この関数は、配列の値を歪みパラメータ値として、このインスタンスにセットします。
	 * @param i_factor
	 * 歪みパラメータ値を格納した配列。4要素である必要があります。
	 */
	public void setValue(double[] i_factor)
	{
		this._f0=i_factor[0];
		this._f1=i_factor[1];
		this._f2=i_factor[2];
		this._f3=i_factor[3];
		return;
	}
	
	/**
	 * この関数は、パラメータ値を配列へ返します。
	 * @param o_factor
	 * 歪みパラメータ値の出力先配列。4要素である必要があります。
	 */
	public void getValue(double[] o_factor)
	{
		o_factor[0]=this._f0;
		o_factor[1]=this._f1;
		o_factor[2]=this._f2;
		o_factor[3]=this._f3;
		return;
	}
	
	/**
	 * この関数は、歪みパラメータをスケール倍します。
	 * パラメータ値は、スケール値の大きさだけ、拡大、又は縮小します。
	 * @param i_scale
	 * パラメータの倍率。
	 */
	public void changeScale(double i_scale)
	{
		this._f0=this._f0*i_scale;//X
		this._f1=this._f1*i_scale;//Y
		this._f2=this._f2/ (i_scale * i_scale);// newparam->dist_factor[2]=source->dist_factor[2]/ (scale*scale);
		//this.f3=this.f3;// newparam->dist_factor[3] =source->dist_factor[3];
		return;
	}
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public final void ideal2Observ(NyARDoublePoint2d i_in, NyARDoublePoint2d o_out)
	{
		final double x = (i_in.x - this._f0) * this._f3;
		final double y = (i_in.y - this._f1) * this._f3;
		if (x == 0.0 && y == 0.0) {
			o_out.x = this._f0;
			o_out.y = this._f1;
		} else {
			final double d = 1.0 - this._f2 / 100000000.0 * (x * x + y * y);
			o_out.x = x * d + this._f0;
			o_out.y = y * d + this._f1;
		}
		return;
	}
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public final void ideal2Observ(NyARDoublePoint2d i_in, NyARIntPoint2d o_out)
	{
		this.ideal2Observ(i_in.x,i_in.y,o_out);
		return;
	}
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_x
	 * 変換元の座標
	 * @param i_y
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public final void ideal2Observ(double i_x,double i_y, NyARIntPoint2d o_out)
	{
		final double x = (i_x - this._f0) * this._f3;
		final double y = (i_y - this._f1) * this._f3;
		if (x == 0.0 && y == 0.0) {
			o_out.x = (int)(this._f0);
			o_out.y = (int)(this._f1);
		} else {
			final double d = 1.0 - this._f2 / 100000000.0 * (x * x + y * y);
			o_out.x = (int)(x * d + this._f0);
			o_out.y = (int)(y * d + this._f1);
		}
		return;
	}
	
	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 */
	public final void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size)
	{
		double x, y;
		final double d0 = this._f0;
		final double d1 = this._f1;
		final double d3 = this._f3;
		final double d2_w = this._f2 / 100000000.0;
		for (int i = 0; i < i_size; i++) {
			x = (i_in[i].x - d0) * d3;
			y = (i_in[i].y - d1) * d3;
			if (x == 0.0 && y == 0.0) {
				o_out[i].x = d0;
				o_out[i].y = d1;
			} else {
				final double d = 1.0 - d2_w * (x * x + y * y);
				o_out[i].x = x * d + d0;
				o_out[i].y = y * d + d1;
			}
		}
		return;
	}

	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 */
	public final void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARIntPoint2d[] o_out, int i_size)
	{
		double x, y;
		final double d0 = this._f0;
		final double d1 = this._f1;
		final double d3 = this._f3;
		final double d2_w = this._f2 / 100000000.0;
		for (int i = 0; i < i_size; i++) {
			x = (i_in[i].x - d0) * d3;
			y = (i_in[i].y - d1) * d3;
			if (x == 0.0 && y == 0.0) {
				o_out[i].x = (int)d0;
				o_out[i].y = (int)d1;
			} else {
				final double d = 1.0 - d2_w * (x * x + y * y);
				o_out[i].x = (int)(x * d + d0);
				o_out[i].y = (int)(y * d + d1);
			}
		}
		return;
	}
	
	/**
	 * この関数は、座標を観察座標系から理想座標系へ変換します。
	 * @param ix
	 * 変換元の座標
	 * @param iy
	 * 変換元の座標
	 * @param o_point
	 * 変換後の座標を受け取るオブジェクト
	 */
	public final void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point)
	{
		double z02, z0, p, q, z, px, py, opttmp_1;
		final double d0 = this._f0;
		final double d1 = this._f1;

		px = ix - d0;
		py = iy - d1;
		p = this._f2 / 100000000.0;
		z02 = px * px + py * py;
		q = z0 = Math.sqrt(z02);// Optimize//q = z0 = Math.sqrt(px*px+ py*py);

		for (int i = 1;; i++) {
			if (z0 != 0.0) {
				// Optimize opttmp_1
				opttmp_1 = p * z02;
				z = z0 - ((1.0 - opttmp_1) * z0 - q) / (1.0 - 3.0 * opttmp_1);
				px = px * z / z0;
				py = py * z / z0;
			} else {
				px = 0.0;
				py = 0.0;
				break;
			}
			if (i == PD_LOOP) {
				break;
			}
			z02 = px * px + py * py;
			z0 = Math.sqrt(z02);// Optimize//z0 = Math.sqrt(px*px+ py*py);
		}
		o_point.x = px / this._f3 + d0;
		o_point.y = py / this._f3 + d1;
		return;
	}

	/**
	 * この関数は、座標を観察座標系から理想座標系へ変換します。
	 * @param ix
	 * 変換元の座標
	 * @param iy
	 * 変換元の座標
	 * @param o_veclinear
	 * 変換後の座標を受け取るオブジェクト。{@link NyARVecLinear2d#x}と{@link NyARVecLinear2d#y}のみに値をセットします。
	 */
	public void observ2Ideal(double ix, double iy, NyARVecLinear2d o_veclinear)
	{
		double z02, z0, p, q, z, px, py, opttmp_1;
		final double d0 = this._f0;
		final double d1 = this._f1;

		px = ix - d0;
		py = iy - d1;
		p = this._f2 / 100000000.0;
		z02 = px * px + py * py;
		q = z0 = Math.sqrt(z02);// Optimize//q = z0 = Math.sqrt(px*px+ py*py);

		for (int i = 1;; i++) {
			if (z0 != 0.0) {
				// Optimize opttmp_1
				opttmp_1 = p * z02;
				z = z0 - ((1.0 - opttmp_1) * z0 - q) / (1.0 - 3.0 * opttmp_1);
				px = px * z / z0;
				py = py * z / z0;
			} else {
				px = 0.0;
				py = 0.0;
				break;
			}
			if (i == PD_LOOP) {
				break;
			}
			z02 = px * px + py * py;
			z0 = Math.sqrt(z02);// Optimize//z0 = Math.sqrt(px*px+ py*py);
		}
		o_veclinear.x = px / this._f3 + d0;
		o_veclinear.y = py / this._f3 + d1;
		return;
	}
}
