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
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * RGBラスタをGrayScaleに変換するフィルタを作成します。
 * このフィルタは、RGB値の平均値を、(R+G+B)/3で算出します。
 *
 */
public class NyARRasterFilter_RgbAveAdd implements INyARRasterFilter_RgbToGs
{
	IdoThFilterImpl _do_filter_impl;
	public NyARRasterFilter_RgbAveAdd(int i_raster_type) throws NyARException
	{
		switch (i_raster_type) {
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24:
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24:
			this._do_filter_impl=new doThFilterImpl_BYTE1D_B8G8R8_24();
			break;
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8X8_32:
			this._do_filter_impl=new doThFilterImpl_BYTE1D_B8G8R8X8_32();
			break;
		default:
			throw new NyARException();
		}
	}
	public void doFilter(INyARRgbRaster i_input, NyARGrayscaleRaster i_output) throws NyARException
	{
		INyARBufferReader in_buffer_reader=i_input.getBufferReader();	
		INyARBufferReader out_buffer_reader=i_output.getBufferReader();	
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._do_filter_impl.doFilter(in_buffer_reader,out_buffer_reader,i_input.getSize());
		return;
	}
	
	/*
	 * ここから各種ラスタ向けのフィルタ実装
	 *
	 */
	interface IdoThFilterImpl
	{
		public void doFilter(INyARBufferReader i_input, INyARBufferReader i_output,NyARIntSize i_size);
	}
	class doThFilterImpl_BYTE1D_B8G8R8_24 implements IdoThFilterImpl
	{
		public void doFilter(INyARBufferReader i_input, INyARBufferReader i_output,NyARIntSize i_size)
		{
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			
			int bp = 0;
			for (int y = 0; y < i_size.h; y++) {
				for (int x = 0; x < i_size.w; x++) {
					out_buf[y*i_size.w+x] = ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff)) / 3;
					bp += 3;
				}
			}
			return;
		}		
	}
	class doThFilterImpl_BYTE1D_B8G8R8X8_32 implements IdoThFilterImpl
	{
		public void doFilter(INyARBufferReader i_input, INyARBufferReader i_output,NyARIntSize i_size)
		{
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();

			int bp = 0;
			for (int y = 0; y < i_size.h; y++) {
				for (int x = 0; x < i_size.w; x++) {
					out_buf[y*i_size.w+x] = ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff)) / 3;
					bp += 4;
				}
			}
		}
	}
	
	
	
}






