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
package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRasterFilter_RgbToGs;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * YCbCr変換して、Y成分のグレースケールの値を計算します。
 * 変換式は、http://www.tyre.gotdns.org/を参考にしました。
 */
public class NyARRasterFilter_Rgb2Gs_YCbCr implements INyARRasterFilter_RgbToGs
{
	private IdoFilterImpl _dofilterimpl;
	public NyARRasterFilter_Rgb2Gs_YCbCr(int i_raster_type) throws NyARException
	{
		switch (i_raster_type) {
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24:
			this._dofilterimpl=new IdoFilterImpl_BYTE1D_B8G8R8_24();
			break;
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24:
		default:
			throw new NyARException();
		}
	}
	public void doFilter(INyARRgbRaster i_input, NyARGrayscaleRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._dofilterimpl.doFilter(i_input.getBufferReader(),i_output.getBufferReader(),i_input.getSize());
	}
	
	interface IdoFilterImpl
	{
		public void doFilter(INyARBufferReader i_input, INyARBufferReader i_output,NyARIntSize i_size) throws NyARException;
	}
	class IdoFilterImpl_BYTE1D_B8G8R8_24 implements IdoFilterImpl
	{
		/**
		 * This function is not optimized.
		 */
		public void doFilter(INyARBufferReader i_input, INyARBufferReader i_output,NyARIntSize i_size) throws NyARException
		{
			assert(		i_input.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();

			int bp = 0;
			for (int y = 0; y < i_size.h; y++){
				for (int x = 0; x < i_size.w; x++){
					out_buf[y*i_size.w+x]=(306*(in_buf[bp+2] & 0xff)+601*(in_buf[bp + 1] & 0xff)+117 * (in_buf[bp + 0] & 0xff))>>10;
					bp += 3;
				}
			}
			return;
		}
	}	
}