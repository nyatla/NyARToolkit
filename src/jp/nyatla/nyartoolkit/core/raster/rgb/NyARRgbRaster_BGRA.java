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
package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;

public class NyARRgbRaster_BGRA extends NyARRgbRaster_BasicClass implements INyARRgbRaster
{
	private class PixelReader implements INyARRgbPixelReader
	{
		private NyARRgbRaster_BGRA _parent;

		public PixelReader(NyARRgbRaster_BGRA i_parent)
		{
			this._parent = i_parent;
		}

		public void getPixel(int i_x, int i_y, int[] o_rgb)
		{
			byte[] ref = this._parent._ref_buf;
			int bp = (i_x + i_y * this._parent._size.w) * 4;
			o_rgb[0] = (ref[bp + 2] & 0xff);// R
			o_rgb[1] = (ref[bp + 1] & 0xff);// G
			o_rgb[2] = (ref[bp + 0] & 0xff);// B
			return;
		}

		public void getPixelSet(int[] i_x, int i_y[], int i_num, int[] o_rgb)
		{
			int width = _parent._size.w;
			byte[] ref = _parent._ref_buf;
			int bp;
			for (int i = i_num - 1; i >= 0; i--) {
				bp = (i_x[i] + i_y[i] * width) * 4;
				o_rgb[i * 3 + 0] = (ref[bp + 2] & 0xff);// R
				o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
				o_rgb[i * 3 + 2] = (ref[bp + 0] & 0xff);// B
			}
		}
	}

	private INyARRgbPixelReader _rgb_reader;
	private INyARBufferReader _buffer_reader;
	private byte[] _ref_buf;

	public static NyARRgbRaster_BGRA wrap(byte[] i_buffer, int i_width, int i_height)
	{
		return new NyARRgbRaster_BGRA(i_buffer, i_width, i_height);
	}

	private NyARRgbRaster_BGRA(byte[] i_buffer, int i_width, int i_height)
	{
		super(new NyARIntSize(i_width,i_height));
		this._ref_buf = i_buffer;
		this._rgb_reader = new PixelReader(this);
		this._buffer_reader=new NyARBufferReader(i_buffer,INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8X8_32);
		return;
	}
	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._rgb_reader;
	}
	public INyARBufferReader getBufferReader()
	{
		return this._buffer_reader;
	}
}
