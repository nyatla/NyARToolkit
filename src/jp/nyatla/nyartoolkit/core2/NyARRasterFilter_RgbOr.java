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
package jp.nyatla.nyartoolkit.core2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.INyARRasterFilter_RgbToGs;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARRasterFilter_RgbOr implements INyARRasterFilter_RgbToGs
{
	public void doFilter(INyARRgbRaster i_input, NyARGlayscaleRaster i_output) throws NyARException
	{
		INyARBufferReader in_buffer_reader=i_input.getBufferReader();	
		INyARBufferReader out_buffer_reader=i_output.getBufferReader();			
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);

		final int[][] out_buf = (int[][]) out_buffer_reader.getBuffer();
		final byte[] in_buf = (byte[]) in_buffer_reader.getBuffer();

		NyARIntSize size = i_output.getSize();
		switch (in_buffer_reader.getBufferType()) {
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24:
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24:
			convert24BitRgb(in_buf, out_buf, size);
			break;
		default:
			throw new NyARException();
		}
		return;
	}

	private void convert24BitRgb(byte[] i_in, int[][] i_out, NyARIntSize i_size)
	{
		int bp = 0;
		for (int y = 0; y < i_size.h; y++) {
			for (int x = 0; x < i_size.w; x++) {
				i_out[y][x] = ((i_in[bp] & 0xff) | (i_in[bp + 1] & 0xff) | (i_in[bp + 2] & 0xff));
				bp += 3;
			}
		}
		return;
	}
}