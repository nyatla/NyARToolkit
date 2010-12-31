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
package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * 平滑化フィルタ
 * 画像を平滑化します。
 * カーネルサイズは3x3です。
 */
public class NyARRasterFilter_SimpleSmooth implements INyARRasterFilter
{
	private IdoFilterImpl _do_filter_impl; 
	public NyARRasterFilter_SimpleSmooth(int i_raster_type) throws NyARException
	{
		switch (i_raster_type) {
		case NyARBufferType.INT1D_GRAY_8:
			this._do_filter_impl=new IdoFilterImpl_GRAY_8();
			break;
		default:
			throw new NyARException();
		}
	}
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input!=i_output);
		this._do_filter_impl.doFilter(i_input,i_output,i_input.getSize());
	}
	
	interface IdoFilterImpl
	{
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException;
	}
	class IdoFilterImpl_GRAY_8 implements IdoFilterImpl
	{
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
		{
			assert (i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			assert (i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			int[] in_ptr =(int[])i_input.getBuffer();
			int[] out_ptr=(int[])i_output.getBuffer();
			/* 画像端は捨てる。
			 */
			int width=i_size.w;
			int height=i_size.h;
			int col0,col1,col2;
			int bptr=0;
			//1行目
			col1=in_ptr[bptr  ]+in_ptr[bptr+width  ];
			col2=in_ptr[bptr+1]+in_ptr[bptr+width+1];
			out_ptr[bptr]=(col1+col2)/4;
			bptr++;
			for(int x=0;x<width-2;x++){
				col0=col1;
				col1=col2;
				col2=in_ptr[bptr+1]+in_ptr[bptr+width+1];
				out_ptr[bptr]=(col0+col1+col2)/6;
				bptr++;
			}			
			out_ptr[bptr]=(col1+col2)/4;
			bptr++;
			//2行目-末行-1

			for(int y=0;y<height-2;y++){
				//左端
				col1=in_ptr[bptr  ]+in_ptr[bptr-width  ]+in_ptr[bptr+width  ];
				col2=in_ptr[bptr+1]+in_ptr[bptr-width+1]+in_ptr[bptr+width+1];
				out_ptr[bptr]=(col1+col2)/6;
				bptr++;
				for(int x=0;x<width-2;x++){
					col0=col1;
					col1=col2;
					col2=in_ptr[bptr+1]+in_ptr[bptr-width+1]+in_ptr[bptr+width+1];
					out_ptr[bptr]=(col0+col1+col2)/9;
					bptr++;
				}
				//右端
				out_ptr[bptr]=(col1+col2)/6;
				bptr++;
			}
			//末行目
			col1=in_ptr[bptr  ]+in_ptr[bptr-width  ];
			col2=in_ptr[bptr+1]+in_ptr[bptr-width+1];
			out_ptr[bptr]=(col1+col2)/4;
			bptr++;
			for(int x=0;x<width-2;x++){
				col0=col1;
				col1=col2;
				col2=in_ptr[bptr+1]+in_ptr[bptr-width+1];
				out_ptr[bptr]=(col0+col1+col2)/6;
				bptr++;
			}			
			out_ptr[bptr]=(col1+col2)/4;
			bptr++;
			return;
		}
	}
}