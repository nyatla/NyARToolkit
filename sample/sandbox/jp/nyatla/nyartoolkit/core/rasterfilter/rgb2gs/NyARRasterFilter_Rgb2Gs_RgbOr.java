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
package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRasterFilter_Rgb2Gs;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARRasterFilter_Rgb2Gs_RgbOr implements INyARRasterFilter_Rgb2Gs
{
	public void doFilter(INyARRgbRaster i_input, NyARGrayscaleRaster i_output) throws NyARException
	{
		
		assert(	i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);

		final int[] out_buf = (int[]) i_output.getBuffer();
		final byte[] in_buf = (byte[]) i_input.getBuffer();

		NyARIntSize size = i_output.getSize();
		switch (i_input.getBufferType()) {
		case NyARBufferType.BYTE1D_B8G8R8_24:
		case NyARBufferType.BYTE1D_R8G8B8_24:
			convert24BitRgb(in_buf, out_buf, size);
			break;
		default:
			throw new NyARException();
		}
		return;
	}

	private void convert24BitRgb(byte[] i_in, int[] i_out, NyARIntSize i_size)
	{
		int bp = 0;
		for (int y = 0; y < i_size.h; y++) {
			for (int x = 0; x < i_size.w; x++) {
				i_out[y*i_size.w+x] = ((i_in[bp] & 0xff) | (i_in[bp + 1] & 0xff) | (i_in[bp + 2] & 0xff));
				bp += 3;
			}
		}
		return;
	}
}