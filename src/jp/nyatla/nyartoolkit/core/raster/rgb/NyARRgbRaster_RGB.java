/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
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

import jp.nyatla.nyartoolkit.core.raster.TNyRasterType;
import jp.nyatla.nyartoolkit.core.rasterreader.*;

public class NyARRgbRaster_RGB extends NyARRgbRaster_BasicClass
{
	protected byte[] _ref_buf;

	private NyARRgbPixelReader_RGB24 _reader;

	public static NyARRgbRaster_RGB wrap(byte[] i_buffer, int i_width, int i_height)
	{
		return new NyARRgbRaster_RGB(i_buffer, i_width, i_height);
	}

	private NyARRgbRaster_RGB(byte[] i_buffer, int i_width, int i_height)
	{
		this._ref_buf = i_buffer;
		this._size.w = i_width;
		this._size.h = i_height;
		this._reader = new NyARRgbPixelReader_RGB24(i_buffer, this._size);
		return;
	}

	public byte[] getBufferObject()
	{
		return this._ref_buf;
	}

	public int getBufferType()
	{
		return TNyRasterType.BUFFERFORMAT_BYTE1D_R8G8B8_24;
	}

	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._reader;
	}
}
