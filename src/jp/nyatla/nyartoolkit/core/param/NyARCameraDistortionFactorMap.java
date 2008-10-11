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
package jp.nyatla.nyartoolkit.core.param;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.*;
/**
 * 歪み成分マップを使用するINyARCameraDistortionFactor
 */
final public class NyARCameraDistortionFactorMap implements INyARCameraDistortionFactor
{
	private double[] _factor=new double[4];
	private int _stride;
	private double[] _mapx;
	private double[] _mapy;
	/**
	 * int arParamIdeal2Observ( const double dist_factor[4], const double ix,const double iy,double *ox, double *oy ) 関数の代替関数
	 * 
	 * @param i_in
	 * @param o_out
	 */
	public void ideal2Observ(final NyARDoublePoint2d i_in, NyARDoublePoint2d o_out)
	{
		final double f0=this._factor[0];
		final double f1=this._factor[1];
		final double x = (i_in.x - f0) * this._factor[3];
		final double y = (i_in.y - f1) * this._factor[3];
		if (x == 0.0 && y == 0.0) {
			o_out.x = f0;
			o_out.y = f1;
		} else {
			final double d = 1.0 - this._factor[2] / 100000000.0 * (x * x + y * y);
			o_out.x = x * d + f0;
			o_out.y = y * d + f1;
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
		final double d0 = this._factor[0];
		final double d1 = this._factor[1];
		final double d3 = this._factor[3];
		final double d2_w = this._factor[2] / 100000000.0;
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
	
	
	
	public NyARCameraDistortionFactorMap(NyARCameraDistortionFactor i_distfactor,NyARIntSize i_screen_size)
	{
		NyARDoublePoint2d opoint=new NyARDoublePoint2d();
		this._mapx=new double[i_screen_size.w*i_screen_size.h];
		this._mapy=new double[i_screen_size.w*i_screen_size.h];
		this._stride=i_screen_size.w;
		int ptr=i_screen_size.h*i_screen_size.w-1;
		//歪みマップを構築
		for(int i=i_screen_size.h-1;i>=0;i--)
		{
			for(int i2=i_screen_size.w-1;i2>=0;i2--)
			{
				i_distfactor.observ2Ideal(i2,i, opoint);
				this._mapx[ptr]=opoint.x;
				this._mapy[ptr]=opoint.y;
				ptr--;
			}
		}
		i_distfactor.getValue(this._factor);
		return;
	}
	public void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point)
	{
		int idx=(int)ix+(int)iy*this._stride;
		o_point.x=this._mapx[idx];
		o_point.y=this._mapy[idx];
		return;
	}
	public void observ2IdealBatch(int[] i_x_coord, int[] i_y_coord,int i_start, int i_num, double[] o_x_coord,double[] o_y_coord)
	{
		int idx;
		int ptr=0;
		for (int j = 0; j < i_num; j++) {
			idx=i_x_coord[i_start + j]+i_y_coord[i_start + j]*this._stride;
			o_x_coord[ptr]=this._mapx[idx];
			o_y_coord[ptr]=this._mapy[idx];
			ptr++;
		}
		return;
	}	
}
