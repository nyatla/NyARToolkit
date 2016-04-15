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
 * ARToolKit Version2の樽型歪みを使った歪み補正クラスです。
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
 */
public class NyARCameraDistortionFactorV2 extends NyARCameraDistortionFactorImpl
{
	final public static int NUM_OF_FACTOR=4;
	final private static int PD_LOOP = 3;
	final private double _f0;//x0
	final private double _f1;//y0
	final private double _f2;//100000000.0*f
	final private double _f3;//s
	
	public NyARCameraDistortionFactorV2(NyARCameraDistortionFactorV2 i_copyfrom,double i_x_scale,double i_y_scale)
	{
		this(new double[]{i_copyfrom._f0,i_copyfrom._f1,i_copyfrom._f2,i_copyfrom._f3},i_x_scale,i_y_scale);
	}
	public NyARCameraDistortionFactorV2(double[] i_factor,double i_x_scale,double i_y_scale)
	{
		this._f0=i_factor[0]*i_x_scale;
		this._f1=i_factor[1]*i_y_scale;
		this._f2=i_factor[2]/ (i_x_scale * i_y_scale);
		this._f3=i_factor[3];
	}

	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 */
	public final void ideal2Observ(double i_x,double i_y, NyARDoublePoint2d o_out)
	{
		final double x = (i_x - this._f0) * this._f3;
		final double y = (i_y - this._f1) * this._f3;
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
	

	@Override
	public final void observ2Ideal(int ix, int iy, NyARDoublePoint2d o_point)
	{
		this.observ2Ideal((double)ix,(double)iy, o_point);
		return;
	}	
	/**
	 * この関数は、座標を観察座標系から理想座標系へ変換します。
	 */
	@Override
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
				px = py= 0.0;
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
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * i_inとo_outには、同じインスタンスを指定できます。
	 */
	final public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size)
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
	 * i_inとo_outには、同じインスタンスを指定できます。
	 */
	final public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARIntPoint2d[] o_out, int i_size)
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
	
}
