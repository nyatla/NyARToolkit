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


/**
 * RGBラスタをGrayScaleに変換するフィルタを作成します。
 * このフィルタは、RGB値の平均値を、(R*G*B)/(255*255)で算出します。
 * 
 * この値は、RGB成分の作る立方体の体積を0-255スケールにした値です。
 *
 */
public class NyARRasterFilter_Rgb2Gs_RgbCube implements INyARRasterFilter_Rgb2Gs
{
	private IdoFilterImpl _dofilterimpl;
	public NyARRasterFilter_Rgb2Gs_RgbCube(int i_in_raster_type) throws NyARException
	{
		if(!initInstance(i_in_raster_type,NyARBufferType.INT1D_GRAY_8))
		{
			throw new NyARException();
		}
	}
	public NyARRasterFilter_Rgb2Gs_RgbCube(int i_in_raster_type,int i_out_raster) throws NyARException
	{
		if(!initInstance(i_in_raster_type,i_out_raster))
		{
			throw new NyARException();
		}
	}	
	protected boolean initInstance(int i_in_raster_type,int i_out_raster_type)
	{
		switch(i_out_raster_type){
		case NyARBufferType.INT1D_GRAY_8:
			switch (i_in_raster_type) {
			case NyARBufferType.BYTE1D_B8G8R8_24:
			case NyARBufferType.BYTE1D_R8G8B8_24:
				this._dofilterimpl=new IdoFilterImpl_BYTE1D_B8G8R8_24();
				break;
			default:
				return false;
			}
			break;
		default:
			return false;
		}
		return true;
	}	
	
	
	public void doFilter(INyARRgbRaster i_input, NyARGrayscaleRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._dofilterimpl.doFilter(i_input,i_output,i_input.getSize());
	}
	
	interface IdoFilterImpl
	{
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException;
	}
	class IdoFilterImpl_BYTE1D_B8G8R8_24 implements IdoFilterImpl
	{
		/**
		 * This function is not optimized.
		 */
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
		{
			assert(		i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)
					||	i_input.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
			assert(i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();

			int bp = 0;
			for (int y = 0; y < i_size.h; y++) {
				for (int x = 0; x < i_size.w; x++) {
					out_buf[y*i_size.w+x] = ((in_buf[bp] & 0xff) * (in_buf[bp + 1] & 0xff) * (in_buf[bp + 2] & 0xff)) >> 16;
					bp += 3;
				}
			}
			return;
		}
	}
	
}

