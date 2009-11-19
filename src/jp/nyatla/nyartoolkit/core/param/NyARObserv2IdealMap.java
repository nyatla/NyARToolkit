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
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 歪み矯正した座標系を格納したクラスです。
 * ２次元ラスタを１次元配列で表現します。
 *
 */
public class NyARObserv2IdealMap
{
	protected int _stride;
	protected double[] _mapx;
	protected double[] _mapy;
	public NyARObserv2IdealMap(NyARCameraDistortionFactor i_distfactor,NyARIntSize i_screen_size)
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
		return;
	}
	public void observ2Ideal(int ix, int iy, NyARDoublePoint2d o_point)
	{
		int idx=ix+iy*this._stride;
		o_point.x=this._mapx[idx];
		o_point.y=this._mapy[idx];
		return;
	}
	public void observ2IdealBatch(int[] i_x_coord, int[] i_y_coord,int i_start, int i_num, double[] o_x_coord,double[] o_y_coord,int i_out_start_index)
	{
		int idx;
		int ptr=i_out_start_index;
		final double[] mapx=this._mapx;
		final double[] mapy=this._mapy;
		final int stride=this._stride;
		for (int j = 0; j < i_num; j++){
			idx=i_x_coord[i_start + j]+i_y_coord[i_start + j]*stride;
			o_x_coord[ptr]=mapx[idx];
			o_y_coord[ptr]=mapy[idx];
			ptr++;
		}
		return;
	}	
}
