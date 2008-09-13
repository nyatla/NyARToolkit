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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BasicClass;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
/**
 * RGB形式のbyte配列をラップするNyARRasterです。
 * 保持したデータからBufferedImageを出力する機能も持ちます。
 */
public class QtNyARRaster_RGB extends NyARRgbRaster_BasicClass
{
	private class PixcelReader extends NyARRgbPixelReader_RGB24 implements INyARBufferReader
	{
		public PixcelReader(NyARIntSize i_size)
		{
			super(null, i_size);
			return;
		}

		public void syncBuffer(byte[] i_ref_buffer)
		{
			this._ref_buf = i_ref_buffer;
			return;
		}

		//
		// INyARBufferReader
		//
		public Object getBuffer()
		{
			return this._ref_buf;
		}

		public int getBufferType()
		{
			return INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24;
		}

		public boolean isEqualBufferType(int i_type_value)
		{
			return i_type_value == INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24;
		}
	}

	protected byte[] _ref_buf;

	protected PixcelReader _reader;

	private WritableRaster _raster;

	private BufferedImage _image;

	/**
	 * RGB形式のJMFバッファをラップするオブジェクトをつくります。 生成直後のオブジェクトはデータを持ちません。 メンバ関数はsetBufferを実行後に使用可能になります。
	 */
	public QtNyARRaster_RGB(int i_width, int i_height)
	{
		this._size.w = i_width;
		this._size.h = i_height;
		this._ref_buf = null;
		this._reader = new PixcelReader(this._size);
		_raster = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_BYTE, i_width, i_height, i_width * 3, 3, new int[] { 0, 1, 2 }, null);
		_image = new BufferedImage(i_width, i_height, BufferedImage.TYPE_3BYTE_BGR);
	}

	/**
	 * javax.media.Bufferを分析して、その分析結果をNyARRasterに適合する形で保持します。 関数実行後に外部でi_bufferの内容変更した場合には、再度setBuffer関数を呼び出してください。
	 * 
	 * @param i_buffer
	 * RGB形式のデータを格納したjavax.media.Bufferオブジェクトを指定してください。
	 * @return i_bufferをラップしたオブジェクトを返します。
	 * @throws NyARException
	 */
	public void setBuffer(byte[] i_buffer)
	{
		this._ref_buf = i_buffer;
		this._reader.syncBuffer(i_buffer);
	}

	public INyARBufferReader getBufferReader()
	{
		return this._reader;
	}

	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._reader;
	}

	/**
	 * データを持っているかを返します。
	 * 
	 * @return
	 */
	public boolean hasData()
	{
		return this._ref_buf != null;
	}

	/**
	 * 保持しているデータからBufferedImageを作って返します。
	 * 
	 * @return
	 */
	public BufferedImage createImage()
	{
		_raster.setDataElements(0, 0, this._size.w, this._size.h, this._ref_buf);
		_image.setData(_raster);
		return _image;
	}

}
