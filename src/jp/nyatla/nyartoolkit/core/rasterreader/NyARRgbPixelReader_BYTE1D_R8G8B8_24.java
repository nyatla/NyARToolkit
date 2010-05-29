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
package jp.nyatla.nyartoolkit.core.rasterreader;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
/**
 * byte[]配列に、パディング無しの8bit画素値が、RGBRGBの順で並んでいる
 * バッファに使用できるピクセルリーダー
 *
 */
public class NyARRgbPixelReader_BYTE1D_R8G8B8_24 implements INyARRgbPixelReader
{
	protected byte[] _ref_buf;

	private NyARIntSize _size;

	public NyARRgbPixelReader_BYTE1D_R8G8B8_24(byte[] i_buf, NyARIntSize i_size)
	{
		this._ref_buf = i_buf;
		this._size = i_size;
	}

	public void getPixel(int i_x, int i_y, int[] o_rgb)
	{
		final byte[] ref_buf = this._ref_buf;
		final int bp = (i_x + i_y * this._size.w) * 3;
		o_rgb[0] = (ref_buf[bp + 0] & 0xff);// R
		o_rgb[1] = (ref_buf[bp + 1] & 0xff);// G
		o_rgb[2] = (ref_buf[bp + 2] & 0xff);// B
		return;
	}

	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
		int bp;
		final int width = this._size.w;
		final byte[] ref_buf = this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 3;
			o_rgb[i * 3 + 0] = (ref_buf[bp + 0] & 0xff);// R
			o_rgb[i * 3 + 1] = (ref_buf[bp + 1] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref_buf[bp + 2] & 0xff);// B
		}
		return;
	}
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		final byte[] ref_buf = this._ref_buf;
		final int idx=(i_y*this._size.w+i_x)*3;
		ref_buf[idx + 0] = (byte)i_rgb[0];// R
		ref_buf[idx + 1] = (byte)i_rgb[1];// G
		ref_buf[idx + 2] = (byte)i_rgb[2];// B
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void switchBuffer(Object i_ref_buffer) throws NyARException
	{
		assert(((byte[])i_ref_buffer).length>=this._size.w*this._size.h*3);
		this._ref_buf=(byte[])i_ref_buffer;
	}	
}