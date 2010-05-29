package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;



/**
 * 定数閾値による2値化をする。
 * 
 */
public class NyARRasterFilter_ARToolkitThreshold implements INyARRasterFilter_Rgb2Bin
{
	protected int _threshold;
	private IdoThFilterImpl _do_threshold_impl;

	public NyARRasterFilter_ARToolkitThreshold(int i_threshold,int i_in_raster_type) throws NyARException
	{
		if(!initInstance(i_threshold,i_in_raster_type,NyARBufferType.INT1D_BIN_8)){
			throw new NyARException();
		}
	}
	public NyARRasterFilter_ARToolkitThreshold(int i_threshold,int i_in_raster_type,int i_out_raster_type) throws NyARException
	{
		if(!initInstance(i_threshold,i_in_raster_type,i_out_raster_type)){
			throw new NyARException();
		}
	}
	protected boolean initInstance(int i_threshold,int i_in_raster_type,int i_out_raster_type)
	{
		switch(i_out_raster_type){
		case NyARBufferType.INT1D_BIN_8:
			switch (i_in_raster_type){
			case NyARBufferType.BYTE1D_B8G8R8_24:
			case NyARBufferType.BYTE1D_R8G8B8_24:
				this._do_threshold_impl=new doThFilterImpl_BUFFERFORMAT_BYTE1D_RGB_24();
				break;
			case NyARBufferType.BYTE1D_B8G8R8X8_32:
				this._do_threshold_impl=new doThFilterImpl_BUFFERFORMAT_BYTE1D_B8G8R8X8_32();
				break;
			case NyARBufferType.BYTE1D_X8R8G8B8_32:
				this._do_threshold_impl=new doThFilterImpl_BUFFERFORMAT_BYTE1D_X8R8G8B8_32();
				break;
			case NyARBufferType.INT1D_X8R8G8B8_32:
				this._do_threshold_impl=new doThFilterImpl_BUFFERFORMAT_INT1D_X8R8G8B8_32();
				break;
			case NyARBufferType.WORD1D_R5G6B5_16LE:
				this._do_threshold_impl=new doThFilterImpl_BUFFERFORMAT_WORD1D_R5G6B5_16LE();
				break;
			default:
				return false;//サポートしない組み合わせ
			}
			break;
		default:
			return false;//サポートしない組み合わせ
		}
		this._threshold = i_threshold;
		return true;
	}	
	
	/**
	 * 画像を２値化するための閾値。暗点<=th<明点となります。
	 * @param i_threshold
	 */
	public void setThreshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}

	public void doFilter(INyARRgbRaster i_input, NyARBinRaster i_output) throws NyARException
	{

		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._do_threshold_impl.doThFilter(i_input,i_output,i_output.getSize(), this._threshold);
		return;
	}
	/*
	 * ここから各ラスタ用のフィルタ実装
	 */
	interface IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size,int i_threshold);
	}
	class doThFilterImpl_BUFFERFORMAT_BYTE1D_RGB_24 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size,int i_threshold)
		{
			assert (i_output.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			
			final int th=i_threshold*3;
			int bp =(i_size.w*i_size.h-1)*3;
			int w;
			int xy;
			final int pix_count   =i_size.h*i_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);
			for(xy=pix_count-1;xy>=pix_mod_part;xy--){
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
			}
			//タイリング
			for (;xy>=0;) {
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 3;
				xy--;
			}
			return;			
		}
		
	}
	class doThFilterImpl_BUFFERFORMAT_BYTE1D_B8G8R8X8_32 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size,int i_threshold)
		{
			assert (i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
			assert (i_output.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			
			final int th=i_threshold*3;
			int bp =(i_size.w*i_size.h-1)*4;
			int w;
			int xy;
			final int pix_count   =i_size.h*i_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);
			for(xy=pix_count-1;xy>=pix_mod_part;xy--){
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
			}
			//タイリング
			for (;xy>=0;) {
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
			}			
		}		
	}
	
	class doThFilterImpl_BUFFERFORMAT_BYTE1D_X8R8G8B8_32 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size,int i_threshold)
		{
			assert (i_output.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			
			final int th=i_threshold*3;
			int bp =(i_size.w*i_size.h-1)*4;
			int w;
			int xy;
			final int pix_count   =i_size.h*i_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);
			for(xy=pix_count-1;xy>=pix_mod_part;xy--){
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
			}
			//タイリング
			for (;xy>=0;) {
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
				w= ((in_buf[bp+1] & 0xff) + (in_buf[bp + 2] & 0xff) + (in_buf[bp + 3] & 0xff));
				out_buf[xy]=w<=th?0:1;
				bp -= 4;
				xy--;
			}
			return;			
		}
		
	}	
	
	class doThFilterImpl_BUFFERFORMAT_INT1D_X8R8G8B8_32 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size,int i_threshold)
		{
			assert (i_output.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			int[] in_buf = (int[]) i_input.getBuffer();
			
			final int th=i_threshold*3;
			int w;
			int xy;
			final int pix_count   =i_size.h*i_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);

			for(xy=pix_count-1;xy>=pix_mod_part;xy--){
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
			}
			//タイリング
			for (;xy>=0;) {
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
				w=in_buf[xy];
				out_buf[xy]=(((w>>16)&0xff)+((w>>8)&0xff)+(w&0xff))<=th?0:1;
				xy--;
			}			
		}		
	}
	
	class doThFilterImpl_BUFFERFORMAT_WORD1D_R5G6B5_16LE implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size,int i_threshold)
		{
			assert (i_output.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			short[] in_buf = (short[]) i_input.getBuffer();
			
			final int th=i_threshold*3;
			int w;
			int xy;
			final int pix_count   =i_size.h*i_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);

			for(xy=pix_count-1;xy>=pix_mod_part;xy--){				
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
			}
			//タイリング
			for (;xy>=0;) {
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
                w =(int)in_buf[xy];
                w = ((w & 0xf800) >> 8) + ((w & 0x07e0) >> 3) + ((w & 0x001f) << 3);
                out_buf[xy] = w <= th ? 0 : 1;
				xy--;
			}
		}		
	}	
}
