/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.rasterreader;

import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public 	class NyARVectorReader_INT1D_GRAY_8
{
	private int[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARVectorReader_INT1D_GRAY_8(INyARRaster i_ref_raster)
	{
		assert(i_ref_raster.getBufferType()==NyARBufferType.INT1D_GRAY_8);
		this._ref_buf=(int[])(i_ref_raster.getBuffer());
		this._ref_size=i_ref_raster.getSize();
	}
	/**
	 * 4近傍の画素ベクトルを取得します。
	 * 0,1,0
	 * 1,x,1
	 * 0,1,0
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
	public void getPixelVector4(int x,int y,NyARIntPoint2d o_v)
	{
		int[] buf=this._ref_buf;
		int w=this._ref_size.w;
		int idx=w*y+x;
		o_v.x=buf[idx+1]-buf[idx-1];
		o_v.y=buf[idx+w]-buf[idx-w];
	}
	/**
	 * 8近傍画素ベクトル
	 * 1,2,1
	 * 2,x,2
	 * 1,2,1
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
	public void getPixelVector8(int x,int y,NyARIntPoint2d o_v)
	{
		int[] buf=this._ref_buf;
		NyARIntSize s=this._ref_size;
		int idx_0 =s.w*y+x;
		int idx_p1=idx_0+s.w;
		int idx_m1=idx_0-s.w;
		int b=buf[idx_m1-1];
		int d=buf[idx_m1+1];
		int h=buf[idx_p1-1];
		int f=buf[idx_p1+1];
		o_v.x=buf[idx_0+1]-buf[idx_0-1]+(d-b+f-h)/2;
		o_v.y=buf[idx_p1]-buf[idx_m1]+(f-d+h-b)/2;
	}	
}