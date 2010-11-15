package jp.nyatla.nyartoolkit.dev.rpf.utils;

import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;

public class NyARRasterFilter_AbsDiff
{
	private int _buffer_type;
	public NyARRasterFilter_AbsDiff(int i_buffer_type)
	{
		this._buffer_type=i_buffer_type;
	}
	public void doFilter(NyARGrayscaleRaster i_in1,NyARGrayscaleRaster i_in2,NyARGrayscaleRaster o_raster)
	{
		assert(i_in1.isEqualBufferType(this._buffer_type));
		assert(i_in2.isEqualBufferType(this._buffer_type));
		assert(o_raster.isEqualBufferType(this._buffer_type));
		int[] buf1=(int[])i_in1.getBuffer();
		int[] buf2=(int[])i_in2.getBuffer();
		int[] bufo=(int[])o_raster.getBuffer();
	}
}
