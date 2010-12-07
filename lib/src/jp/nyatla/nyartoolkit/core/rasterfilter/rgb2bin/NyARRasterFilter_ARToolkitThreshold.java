package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
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
		NyARIntSize s=i_input.getSize();
		this._do_threshold_impl.doThFilter(i_input,0,0,s.w,s.h,this._threshold,i_output);
		return;
	}
	public void doFilter(INyARRgbRaster i_input,NyARIntRect i_area, NyARBinRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._do_threshold_impl.doThFilter(i_input,i_area.x,i_area.y,i_area.w,i_area.h,this._threshold,i_output);
		return;
		
	}
	


	protected interface IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h,int i_th,INyARRaster o_raster);
	}
	
	class doThFilterImpl_BUFFERFORMAT_BYTE1D_RGB_24 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h,int i_th,INyARRaster o_raster)
		{
			assert (
					i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||
					i_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
			final byte[] input=(byte[])i_raster.getBuffer();
			final int[] output=(int[])o_raster.getBuffer();
			int th=i_th*3;
			NyARIntSize s=i_raster.getSize();
			int skip_dst=(s.w-i_w);
			int skip_src=skip_dst*3;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt_dst=(i_t*s.w+i_l);
			int pt_src=pt_dst*3;
			for (int y = i_h-1; y >=0 ; y-=1){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
				}
				for (;x>=0;x-=8){
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=3;
				}
				//スキップ
				pt_src+=skip_src;
				pt_dst+=skip_dst;
			}
			return;	
		}
	}
	class doThFilterImpl_BUFFERFORMAT_INT1D_X8R8G8B8_32 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h,int i_th,INyARRaster o_raster)
		{
			assert (i_raster.isEqualBufferType( NyARBufferType.INT1D_X8R8G8B8_32));
			final int[] input=(int[])i_raster.getBuffer();
			final int[] output=(int[])o_raster.getBuffer();
			int th=i_th*3;

			NyARIntSize s=i_raster.getSize();
			int skip_src=(s.w-i_w);
			int skip_dst=skip_src;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt_dst=(i_t*s.w+i_l);
			int pt_src=pt_dst;
			for (int y = i_h-1; y >=0 ; y-=1){
				int x,v;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
				}
				for (;x>=0;x-=8){
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v& 0xff))<=th?0:1;
				}
				//スキップ
				pt_src+=skip_src;
				pt_dst+=skip_dst;				
			}
			return;			
		}	
	}

	


	class doThFilterImpl_BUFFERFORMAT_BYTE1D_B8G8R8X8_32 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h,int i_th,INyARRaster o_raster)
		{
	        assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
			final byte[] input=(byte[])i_raster.getBuffer();
			final int[] output=(int[])o_raster.getBuffer();
			NyARIntSize s=i_raster.getSize();
			int th=i_th*3;
			int skip_dst=(s.w-i_w);
			int skip_src=skip_dst*4;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt_dst=(i_t*s.w+i_l);
			int pt_src=pt_dst*4;
			for (int y = i_h-1; y >=0 ; y-=1){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					output[pt_dst++]=((input[pt_src+ 0]& 0xff)+(input[pt_src+ 1]& 0xff)+(input[pt_src+ 2]& 0xff))<=th?0:1;
					pt_src+=4;
				}
				for (;x>=0;x-=8){
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))<=th?0:1;
					pt_src+=4;
				}
				//スキップ
				pt_src+=skip_src;
				pt_dst+=skip_dst;				
			}
			return;	
	    }
	}

	class doThFilterImpl_BUFFERFORMAT_BYTE1D_X8R8G8B8_32 implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h,int i_th,INyARRaster o_raster)
		{
	        assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_X8R8G8B8_32));
			final byte[] input=(byte[])i_raster.getBuffer();
			final int[] output=(int[])o_raster.getBuffer();
			int th=i_th*3;
			NyARIntSize s=i_raster.getSize();
			int skip_dst=(s.w-i_w);
			int skip_src=skip_dst*4;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt_dst=(i_t*s.w+i_l);
			int pt_src=pt_dst*4;
			for (int y = i_h-1; y >=0 ; y-=1){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
				}
				for (;x>=0;x-=8){
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
					output[pt_dst++]=((input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff)+(input[pt_src+3]& 0xff))<=th?0:1;
					pt_src+=4;
				}
				//スキップ
				pt_src+=skip_src;
				pt_dst+=skip_dst;				
			}
			return;	
	    }
	}

	class doThFilterImpl_BUFFERFORMAT_WORD1D_R5G6B5_16LE implements IdoThFilterImpl
	{
		public void doThFilter(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h,int i_th,INyARRaster o_raster)
		{
	        assert(i_raster.isEqualBufferType(NyARBufferType.WORD1D_R5G6B5_16LE));
			final short[] input=(short[])i_raster.getBuffer();
			final int[] output=(int[])o_raster.getBuffer();
			int th=i_th*3;
			NyARIntSize s=i_raster.getSize();
			int skip_dst=(s.w-i_w);
			int skip_src=skip_dst;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt_dst=(i_t*s.w+i_l);
			int pt_src=pt_dst;
			for (int y = i_h-1; y >=0 ; y-=1){
				int x,v;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
				}
				for (;x>=0;x-=8){
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
					v =(int)input[pt_src++]; output[pt_dst++]=(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))<=th?0:1;
				}
				//スキップ
				pt_src+=skip_src;
				pt_dst+=skip_dst;
			}
			return;	
	    }
	}
	
}
