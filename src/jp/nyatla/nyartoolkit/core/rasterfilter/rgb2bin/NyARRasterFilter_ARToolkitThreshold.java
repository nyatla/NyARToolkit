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
package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.INyARRasterFilter;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 定数閾値による2値化をする。
 * 
 */
public class NyARRasterFilter_ARToolkitThreshold implements INyARRasterFilter
{
	private int _threshold;

	public NyARRasterFilter_ARToolkitThreshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}
	public void setThreshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}

	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		assert (checkInputType(i_input)==true);

		int[][] out_buf = (int[][]) i_output.getBufferObject();
		byte[] in_buf = (byte[]) i_input.getBufferObject();

		NyARIntSize size = i_output.getSize();
		switch (i_input.getBufferType()) {
		case TNyRasterType.BUFFERFORMAT_BYTE1D_B8G8R8_24:
		case TNyRasterType.BUFFERFORMAT_BYTE1D_R8G8B8_24:
			convert24BitRgb(in_buf, out_buf, size);
			break;
		case TNyRasterType.BUFFERFORMAT_BYTE1D_B8G8R8X8_32:
			convert32BitRgbx(in_buf, out_buf, size);
			break;
		default:
			throw new NyARException();
		}
		return;
	}

	private void convert24BitRgb(byte[] i_in, int[][] i_out, NyARIntSize i_size)
	{
		int bp = 0;
		int th=this._threshold*3;
		int w;
		for (int y = 0; y < i_size.h; y++) {
			for (int x = 0; x < i_size.w; x++) {
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[y][x]=w<=th?0:1;
				bp += 3;
			}
		}
		return;
	}
	private void convert32BitRgbx(byte[] i_in, int[][] i_out, NyARIntSize i_size)
	{
		int bp =0;
		final int th=this._threshold*3;
		int w;
		for (int y =0; y<i_size.h ; y++){
			int x;
			for (x = 0;x<i_size.w;x++) {
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[y][x]=w<=th?0:1;
				bp += 4;
			}	
		}
		return;
	}
	
	private boolean checkInputType(INyARRaster i_input) throws NyARException
	{
		switch(i_input.getBufferType()){
		case TNyRasterType.BUFFERFORMAT_BYTE1D_B8G8R8_24:
		case TNyRasterType.BUFFERFORMAT_BYTE1D_R8G8B8_24:
		case TNyRasterType.BUFFERFORMAT_BYTE1D_B8G8R8X8_32:
			return true;
		default:
			return false;
		}
	}
}
