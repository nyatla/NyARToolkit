package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * Roberts法で勾配を計算します。
 * 出力画像のピクセルは、X,Y軸方向に-1され、下端、右端の画素は無効な値が入ります。
 * X=|-1, 0|  Y=|0,-1|
 *   | 0, 1|    |1, 0|
 * V=sqrt(X^2+Y+2)/2
 */
public class NyARRasterFilter_Roberts implements INyARRasterFilter
{
	private IdoFilterImpl _do_filter_impl; 
	public NyARRasterFilter_Roberts(int i_raster_type) throws NyARException
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
			for(int y=0;y<height-1;y++){
				int idx=y*width;
				int p00=in_ptr[idx];
				int p10=in_ptr[width+idx];
				int p01,p11;
				for(int x=0;x<width-1;x++){
					p01=in_ptr[idx+1];
					p11=in_ptr[idx+width+1];
					int fx=p11-p00;
					int fy=p10-p01;
					out_ptr[idx]=(int)Math.sqrt(fx*fx+fy*fy)>>1;
					p00=p01;
					p10=p11;
					idx++;
				}
			}
			return;
		}
	}
}

