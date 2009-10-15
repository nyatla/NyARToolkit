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
 * 歪み成分マップを使用するINyARCameraDistortionFactor
 */
final public class NyARObserv2IdealMap
{
	private int _stride;
	private double[] _mapx;
	private double[] _mapy;
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
