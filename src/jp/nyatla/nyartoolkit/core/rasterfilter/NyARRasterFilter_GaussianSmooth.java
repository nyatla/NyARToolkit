package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * 平滑化フィルタ
 * Gaussianフィルタで画像を平滑化します。
 * カーネルサイズは3x3です。
 */
public class NyARRasterFilter_GaussianSmooth implements INyARRasterFilter
{
	private IdoFilterImpl _do_filter_impl; 
	public NyARRasterFilter_GaussianSmooth(int i_raster_type) throws NyARException
	{
		switch (i_raster_type) {
		case INyARBufferReader.BUFFERFORMAT_INT1D_GRAY_8:
			this._do_filter_impl=new IdoFilterImpl_GRAY_8();
			break;
		default:
			throw new NyARException();
		}
	}
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input!=i_output);
		this._do_filter_impl.doFilter(i_input.getBufferReader(),i_output.getBufferReader(),i_input.getSize());
	}
	
	interface IdoFilterImpl
	{
		public void doFilter(INyARBufferReader i_input, INyARBufferReader i_output,NyARIntSize i_size) throws NyARException;
	}
	class IdoFilterImpl_GRAY_8 implements IdoFilterImpl
	{
		public void doFilter(INyARBufferReader i_input, INyARBufferReader i_output,NyARIntSize i_size) throws NyARException
		{
			assert (i_input.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT1D_GRAY_8));
			assert (i_output.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT1D_GRAY_8));
			int[] in_ptr =(int[])i_input.getBuffer();
			int[] out_ptr=(int[])i_output.getBuffer();
			int width=i_size.w;
			int height=i_size.h;
			int col0,col1,col2;
			int bptr=0;
			//1行目
			col1=in_ptr[bptr  ]*2+in_ptr[bptr+width  ];
			col2=in_ptr[bptr+1]*2+in_ptr[bptr+width+1];
			out_ptr[bptr]=(col1*2+col2)/9;
			bptr++;
			for(int x=0;x<width-2;x++){
				col0=col1;
				col1=col2;
				col2=in_ptr[bptr+1]*2+in_ptr[bptr+width+1];
				out_ptr[bptr]=(col0+col1*2+col2)/12;
				bptr++;
			}			
			out_ptr[bptr]=(col1+col2)/9;
			bptr++;
			//2行目-末行-1

			for(int y=0;y<height-2;y++){
				//左端
				col1=in_ptr[bptr  ]*2+in_ptr[bptr-width  ]+in_ptr[bptr+width  ];
				col2=in_ptr[bptr+1]*2+in_ptr[bptr-width+1]+in_ptr[bptr+width+1];
				out_ptr[bptr]=(col1+col2)/12;
				bptr++;
				for(int x=0;x<width-2;x++){
					col0=col1;
					col1=col2;
					col2=in_ptr[bptr+1]*2+in_ptr[bptr-width+1]+in_ptr[bptr+width+1];
					out_ptr[bptr]=(col0+col1*2+col2)/16;
					bptr++;
				}
				//右端
				out_ptr[bptr]=(col1*2+col2)/12;
				bptr++;
			}
			//末行目
			col1=in_ptr[bptr  ]*2+in_ptr[bptr-width  ];
			col2=in_ptr[bptr+1]*2+in_ptr[bptr-width+1];
			out_ptr[bptr]=(col1+col2)/9;
			bptr++;
			for(int x=0;x<width-2;x++){
				col0=col1;
				col1=col2;
				col2=in_ptr[bptr+1]*2+in_ptr[bptr-width+1];
				out_ptr[bptr]=(col0+col1*2+col2)/12;
				bptr++;
			}			
			out_ptr[bptr]=(col1*2+col2)/9;
			bptr++;
			return;
		}
	}
}