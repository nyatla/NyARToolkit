/* 
 * PROJECT: NyARToolkit Quicktime utilities.
 * --------------------------------------------------------------------------------
 * Copyright (C)2008 arc@dmz
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
 *	
 *	<arc(at)digitalmuseum.jp>
 * 
 */
package jp.nyatla.nyartoolkit.qt.utils;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * RGB形式のbyte配列をラップするNyARRasterです。
 * 保持したデータからBufferedImageを出力する機能も持ちます。
 */
public class QtNyARRaster_RGB implements INyARRgbRaster
{
	private NyARIntSize _size;
	private byte[] _buffer;
	private NyARRgbPixelReader_BYTE1D_R8G8B8_24 _reader;
	private int _buffer_type;

	/**
	 * QuickTimeオブジェクトからイメージを取得するラスタオブジェクトを作ります。
	 * この
	 * @param i_width
	 * @param i_height
	 */
	public QtNyARRaster_RGB(int i_width, int i_height)
	{
		this._size=new NyARIntSize(i_width,i_height);
		this._buffer= null;
		this._buffer_type=NyARBufferType.BYTE1D_R8G8B8_24;
		this._reader = new NyARRgbPixelReader_BYTE1D_R8G8B8_24(null,this._size);
	}
	
	final public int getWidth()
	{
		return this._size.w;
	}

	final public int getHeight()
	{
		return this._size.h;
	}

	final public NyARIntSize getSize()
	{
		return this._size;
	}
	final public int getBufferType()
	{
		return this._buffer_type;
	}
	final public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._reader;
	}
	final public boolean hasBuffer()
	{
		return this._buffer!=null;
	}
	final public Object getBuffer()
	{
		assert(this._buffer!=null);
		return this._buffer;
	}
	final public boolean isEqualBufferType(int i_type_value)
	{
		return this._buffer_type==i_type_value;
	}
	final public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		this._buffer=(byte[])i_ref_buf;
		this._reader.switchBuffer(i_ref_buf);
	}
}
