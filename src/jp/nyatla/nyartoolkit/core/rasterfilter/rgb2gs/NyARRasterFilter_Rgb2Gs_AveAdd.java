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
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * RGBラスタをGrayScaleに変換するフィルタを作成します。
 * このフィルタは、RGB値の平均値を、(R+G+B)/3で算出します。
 *
 */
public class NyARRasterFilter_Rgb2Gs_AveAdd implements INyARRasterFilter_Rgb2Gs
{
	IdoThFilterImpl _do_filter_impl;
	public NyARRasterFilter_Rgb2Gs_AveAdd(int i_in_raster_type,int i_out_raster_type) throws NyARException
	{
		if(!initInstance(i_in_raster_type,i_out_raster_type))
		{
			throw new NyARException();
		}
	}
	public NyARRasterFilter_Rgb2Gs_AveAdd(int i_in_raster_type) throws NyARException
	{
		if(!initInstance(i_in_raster_type,NyARBufferType.INT1D_GRAY_8))
		{
			throw new NyARException();
		}
	}
	protected boolean initInstance(int i_in_raster_type,int i_out_raster_type)
	{
		switch(i_out_raster_type){
		case NyARBufferType.INT1D_GRAY_8:
			switch (i_in_raster_type){
			case NyARBufferType.BYTE1D_B8G8R8_24:
			case NyARBufferType.BYTE1D_R8G8B8_24:
				this._do_filter_impl=new doThFilterImpl_BYTE1D_B8G8R8_24();
				break;
			case NyARBufferType.BYTE1D_B8G8R8X8_32:
				this._do_filter_impl=new doThFilterImpl_BYTE1D_B8G8R8X8_32();
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
		NyARIntSize s=i_input.getSize();
		this._do_filter_impl.doFilter(i_input,(int[])i_output.getBuffer(),0,0,s.w,s.h);
		return;
	}
	public void doFilter(INyARRgbRaster i_input, NyARGrayscaleRaster i_output,NyARIntRect i_area) throws NyARException
	{
		
	}
	/*
	 * ここから各種ラスタ向けのフィルタ実装
	 *
	 */
	interface IdoThFilterImpl
	{
		public void doFilter(INyARRaster i_input,int[] o_output, int l,int t,int w,int h);
	}
	class doThFilterImpl_BYTE1D_B8G8R8_24 implements IdoThFilterImpl
	{
		public void doFilter(INyARRaster i_input, int[] o_output,int l,int t,int w,int h)
		{
			assert(i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||i_input.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
			
			NyARIntSize size=i_input.getSize();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			int bp = l*3;
			final int b=t+h;
			final int row_padding=(size.w-w)*3;
			for (int y = t; y < b; y++) {
				for (int x = 0; x < w; x++) {
					o_output[y*size.w+x] = ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff)) / 3;
					bp += 3;
				}
				bp+=row_padding;
			}
			return;
		}		
	}
	class doThFilterImpl_BYTE1D_B8G8R8X8_32 implements IdoThFilterImpl
	{
		public void doFilter(INyARRaster i_input, int[] o_output,int l,int t,int w,int h)
		{
			assert(i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
			NyARIntSize size=i_input.getSize();
			byte[] in_buf = (byte[]) i_input.getBuffer();

			int bp = l*4;
			final int b=t+h;
			final int row_padding=(size.w-w)*4;
			for (int y = t; y < b; y++) {
				for (int x = 0; x < w; x++) {
					o_output[y*size.w+x] = ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff)) / 3;
					bp += 4;
				}
				bp+=row_padding;
			}
		}
	}
	
	
	
}






