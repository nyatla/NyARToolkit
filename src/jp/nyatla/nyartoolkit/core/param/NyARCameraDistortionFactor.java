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

/**
 * カメラの歪み成分を格納するクラスと、補正関数群
 * http://www.hitl.washington.edu/artoolkit/Papers/ART02-Tutorial.pdf
 * 11ページを読むといいよ。
 * 
 * x=x(xi-x0),y=s(yi-y0)
 * d^2=x^2+y^2
 * p=(1-fd^2)
 * xd=px+x0,yd=py+y0
 */
public class NyARCameraDistortionFactor implements INyARCameraDistortionFactor
{
	private static final int PD_LOOP = 3;
	private double _f0;//x0
	private double _f1;//y0
	private double _f2;//100000000.0*ｆ
	private double _f3;//s
	public void copyFrom(NyARCameraDistortionFactor i_ref)
	{
		this._f0=i_ref._f0;
		this._f1=i_ref._f1;
		this._f2=i_ref._f2;
		this._f3=i_ref._f3;
		return;
	}
	/**
	 * 配列の値をファクタ値としてセットする。
	 * @param i_factor
	 * 4要素以上の配列
	 */
	public void setValue(double[] i_factor)
	{
		this._f0=i_factor[0];
		this._f1=i_factor[1];
		this._f2=i_factor[2];
		this._f3=i_factor[3];
		return;
	}
	public void getValue(double[] o_factor)
	{
		o_factor[0]=this._f0;
		o_factor[1]=this._f1;
		o_factor[2]=this._f2;
		o_factor[3]=this._f3;
		return;
	}	
	public void changeScale(double i_scale)
	{
		this._f0=this._f0*i_scale;// newparam->dist_factor[0] =source->dist_factor[0] *scale;
		this._f1=this._f1*i_scale;// newparam->dist_factor[1] =source->dist_factor[1] *scale;
		this._f2=this._f2/ (i_scale * i_scale);// newparam->dist_factor[2]=source->dist_factor[2]/ (scale*scale);
		//this.f3=this.f3;// newparam->dist_factor[3] =source->dist_factor[3];
		return;
	}
	/**
	 * int arParamIdeal2Observ( const double dist_factor[4], const double ix,const double iy,double *ox, double *oy ) 関数の代替関数
	 * 
	 * @param i_in
	 * @param o_out
	 */
	public void ideal2Observ(final NyARDoublePoint2d i_in, NyARDoublePoint2d o_out)
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
	 * ideal2Observをまとめて実行します。
	 * @param i_in
	 * @param o_out
	 */
	public void ideal2ObservBatch(final NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size)
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
	 * int arParamObserv2Ideal( const double dist_factor[4], const double ox,const double oy,double *ix, double *iy );
	 * 
	 * @param ix
	 * @param iy
	 * @param ix
	 * @param iy
	 * @return
	 */
	public void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point)
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
	public void observ2IdealBatch(int[] i_x_coord, int[] i_y_coord,int i_start, int i_num, double[] o_x_coord,double[] o_y_coord)
	{
		double z02, z0, q, z, px, py, opttmp_1;
		final double d0 = this._f0;
		final double d1 = this._f1;
		final double d3 = this._f3;
		final double p = this._f2 / 100000000.0;
		for (int j = 0; j < i_num; j++) {

			px = i_x_coord[i_start + j] - d0;
			py = i_y_coord[i_start + j] - d1;

			z02 = px * px + py * py;
			q = z0 = Math.sqrt(z02);// Optimize//q = z0 = Math.sqrt(px*px+py*py);

			for (int i = 1;; i++) {
				if (z0 != 0.0) {
					// Optimize opttmp_1
					opttmp_1 = p * z02;
					z = z0 - ((1.0 - opttmp_1) * z0 - q)/ (1.0 - 3.0 * opttmp_1);
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
			o_x_coord[j] = px / d3 + d0;
			o_y_coord[j] = py / d3 + d1;
		}
		return;
	}	
}
