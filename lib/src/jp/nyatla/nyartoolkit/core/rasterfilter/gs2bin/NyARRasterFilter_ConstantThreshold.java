package jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARRasterFilter_ConstantThreshold implements INyARRasterFilter_Gs2Bin
{
	public int _threshold;
	public NyARRasterFilter_ConstantThreshold(int i_initial_threshold,int i_in_raster_type,int i_out_raster_type) throws NyARException
	{
		assert(i_in_raster_type==NyARBufferType.INT1D_GRAY_8);
		assert(i_out_raster_type==NyARBufferType.INT1D_BIN_8);
		//初期化
		this._threshold=i_initial_threshold;
		
	}
	/**
	 * ２値化の閾値を設定する。
	 * 暗点<=th<明点となります。
	 * @throws NyARException
	 */
	public NyARRasterFilter_ConstantThreshold() throws NyARException
	{
		this._threshold=0;
	}

	
	public void setThreshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}
	public void doFilter(NyARGrayscaleRaster i_input, NyARBinRaster i_output) throws NyARException
	{
		assert(i_input.getBufferType()==NyARBufferType.INT1D_GRAY_8);
		assert(i_output.getBufferType()==NyARBufferType.INT1D_BIN_8);
		int[] out_buf = (int[]) i_output.getBuffer();
		int[] in_buf = (int[]) i_input.getBuffer();
		NyARIntSize s=i_input.getSize();
		
		final int th=this._threshold;
		int bp =s.w*s.h-1;
		final int pix_count   =s.h*s.w;
		final int pix_mod_part=pix_count-(pix_count%8);
		for(bp=pix_count-1;bp>=pix_mod_part;bp--){
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
		}
		//タイリング
		for (;bp>=0;) {
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
		}
		return;			
	}
}
