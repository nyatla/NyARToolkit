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
package jp.nyatla.nyartoolkit.sandbox.quadx2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;

/**
 * 1/4のサイズの画像に変換しながら閾値判定する関数
 * 
 */
public class NyARRasterFilter_ARTTh_Quad implements INyARRasterFilter_Rgb2Bin
{
	private int _threshold;

	public NyARRasterFilter_ARTTh_Quad(int i_threshold)
	{
		this._threshold = i_threshold;
	}
	public void setThreshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}

	public void doFilter(INyARRgbRaster i_input, NyARBinRaster i_output) throws NyARException
	{
		int in_buf_type=i_input.getBufferType();

		NyARIntSize size = i_output.getSize();
		assert (i_output.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
		assert (checkInputType(in_buf_type)==true);	
		assert (i_input.getSize().isEqualSize(size.w*2,size.h*2) == true);

		int[] out_buf = (int[]) i_output.getBuffer();
		byte[] in_buf = (byte[]) i_input.getBuffer();

		switch (i_input.getBufferType()) {
		case NyARBufferType.BYTE1D_B8G8R8_24:
		case NyARBufferType.BYTE1D_R8G8B8_24:
			convert24BitRgb(in_buf, out_buf, size);
			break;
//		case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8X8_32:
//			convert32BitRgbx(in_buf, out_buf, size);
//			break;
		default:
			throw new NyARException();
		}
		return;
	}

	private void convert24BitRgb(byte[] i_in, int[] i_out, NyARIntSize i_size)
	{
		final int size_w=i_size.w*2;
		final int x_mod_end= size_w-(size_w%8);
		final int th=this._threshold*3;
		int bp =(size_w*i_size.h*2-1)*3;	
		int w;
		int x;		
		for (int y =i_size.h-1; y>=0 ; y--){
			//端数分
			final int row_ptr=y*i_size.w;
			for (x = i_size.w-1;x>=x_mod_end;x--) {
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x]=w<=th?0:1;
				bp -= 6;
			}
			//タイリング		
			for (;x>=0;x-=8) {
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x]=w<=th?0:1;
				bp -= 6;
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-1]=w<=th?0:1;
				bp -= 6;
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-2]=w<=th?0:1;
				bp -= 6;
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-3]=w<=th?0:1;
				bp -= 6;
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-4]=w<=th?0:1;
				bp -= 6;
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-5]=w<=th?0:1;
				bp -= 6;
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-6]=w<=th?0:1;
				bp -= 6;
				w=((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-7]=w<=th?0:1;
				bp -= 6;
			}
			//1行飛ばし
			bp-=size_w*3;
		}
		return;
	}
	private void convert32BitRgbx(byte[] i_in, int[] i_out, NyARIntSize i_size)
	{
		final int size_w=i_size.w;
		final int x_mod_end= size_w-(size_w%8);
		final int th=this._threshold*3;
		int bp =(size_w*i_size.h-1)*4;
		int w;
		int x;
		for (int y =i_size.h-1; y>=0 ; y--){
			final int row_ptr=y*i_size.w;

			//端数分
			for (x = size_w-1;x>=x_mod_end;x--) {
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x]=w<=th?0:1;
				bp -= 4;
			}
			//タイリング
			for (;x>=0;x-=8) {
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x]=w<=th?0:1;
				bp -= 4;
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-1]=w<=th?0:1;
				bp -= 4;
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-2]=w<=th?0:1;
				bp -= 4;
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-3]=w<=th?0:1;
				bp -= 4;
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-4]=w<=th?0:1;
				bp -= 4;
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-5]=w<=th?0:1;
				bp -= 4;
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-6]=w<=th?0:1;
				bp -= 4;
				w= ((i_in[bp] & 0xff) + (i_in[bp + 1] & 0xff) + (i_in[bp + 2] & 0xff));
				i_out[row_ptr+x-7]=w<=th?0:1;
				bp -= 4;
			}	
		}
		return;
	}
	
	private boolean checkInputType(int i_input_type) throws NyARException
	{
		switch(i_input_type){
		case NyARBufferType.BYTE1D_B8G8R8_24:
		case NyARBufferType.BYTE1D_R8G8B8_24:
//		case NyARBufferType.BYTE1D_B8G8R8X8_32:
//		case NyARBufferType.BYTE1D_R5G6B5_16LE:
			return true;
		default:
			return false;
		}
	}
}
