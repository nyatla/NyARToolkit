package jp.nyatla.nyartoolkit.core.analyzer.raster;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
/**
 * 画像のヒストグラムを計算します。
 * RGBの場合、(R+G+B)/3のヒストグラムを計算します。
 * 
 * 
 */
public class NyARRasterAnalyzer_Histogram
{
	protected ICreateHistogramImpl _histImpl;
	/**
	 * ヒストグラム解析の縦方向スキップ数。継承クラスはこのライン数づつ
	 * スキップしながらヒストグラム計算を行うこと。
	 */
	protected int _vertical_skip;
	
	
	public NyARRasterAnalyzer_Histogram(int i_raster_format,int i_vertical_interval) throws NyARException
	{
		if(!initInstance(i_raster_format,i_vertical_interval)){
			throw new NyARException();
		}
	}
	protected boolean initInstance(int i_raster_format,int i_vertical_interval)
	{
		switch (i_raster_format) {
		case  NyARBufferType.BYTE1D_B8G8R8_24:
		case  NyARBufferType.BYTE1D_R8G8B8_24:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_BYTE1D_RGB_24();
			break;
		case  NyARBufferType.INT1D_GRAY_8:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_INT1D_GRAY_8();
			break;
		case  NyARBufferType.BYTE1D_B8G8R8X8_32:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_BYTE1D_B8G8R8X8_32();
			break;
		case  NyARBufferType.BYTE1D_X8R8G8B8_32:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_BYTE1D_X8R8G8B8_32();
			break;
		case  NyARBufferType.WORD1D_R5G6B5_16LE:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_WORD1D_R5G6B5_16LE();
			break;
		case  NyARBufferType.INT1D_X8R8G8B8_32:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_INT1D_X8R8G8B8_32();
			break;
		default:
			return false;
		}
		//初期化
		this._vertical_skip=i_vertical_interval;
		return true;
	}	
	
	
	public void setVerticalInterval(int i_step)
	{
		this._vertical_skip=i_step;
		return;
	}

	/**
	 * o_histogramにヒストグラムを出力します。
	 * @param i_input
	 * @param o_histogram
	 * @return
	 * @throws NyARException
	 */
	public int analyzeRaster(INyARRaster i_input,NyARHistogram o_histogram) throws NyARException
	{
		
		final NyARIntSize size=i_input.getSize();
		//最大画像サイズの制限
		assert(size.w*size.h<0x40000000);
		assert(o_histogram.length==256);//現在は固定

		int[] h=o_histogram.data;
		//ヒストグラム初期化
		for (int i = o_histogram.length-1; i >=0; i--){
			h[i] = 0;
		}
		o_histogram.total_of_data=size.w*size.h/this._vertical_skip;
		return this._histImpl.createHistogram(i_input, size,h,this._vertical_skip);		
	}
	
	interface ICreateHistogramImpl
	{
		public int createHistogram(INyARRaster i_reader,NyARIntSize i_size, int[] o_histogram,int i_skip);
	}

	class NyARRasterThresholdAnalyzer_Histogram_INT1D_GRAY_8 implements ICreateHistogramImpl
	{
		public int createHistogram(INyARRaster i_reader,NyARIntSize i_size, int[] o_histogram,int i_skip)
		{
			assert (i_reader.isEqualBufferType( NyARBufferType.INT1D_GRAY_8));
			final int[] input=(int[]) i_reader.getBuffer();
			for (int y = i_size.h-1; y >=0 ; y-=i_skip){
				int pt=y*i_size.w;
				for (int x = i_size.w-1; x >=0; x--) {
					o_histogram[input[pt]]++;
					pt++;
				}
			}
			return i_size.w*i_size.h;
		}	
	}
	class NyARRasterThresholdAnalyzer_Histogram_INT1D_X8R8G8B8_32 implements ICreateHistogramImpl
	{
		public int createHistogram(INyARRaster i_reader,NyARIntSize i_size, int[] o_histogram,int i_skip)
		{
			assert (i_reader.isEqualBufferType( NyARBufferType.INT1D_X8R8G8B8_32));
			final int[] input=(int[]) i_reader.getBuffer();
			for (int y = i_size.h-1; y >=0 ; y-=i_skip){
				int pt=y*i_size.w;
				for (int x = i_size.w-1; x >=0; x--) {
					int p=input[pt];
					o_histogram[((p& 0xff)+(p& 0xff)+(p& 0xff))/3]++;
					pt++;
				}
			}
			return i_size.w*i_size.h;
		}	
	}

	
	class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_RGB_24 implements ICreateHistogramImpl
	{
		public int createHistogram(INyARRaster i_reader,NyARIntSize i_size, int[] o_histogram,int i_skip)
		{
			assert (
					i_reader.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||
					i_reader.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
			final byte[] input=(byte[]) i_reader.getBuffer();
			final int pix_count=i_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);
			for (int y = i_size.h-1; y >=0 ; y-=i_skip) {
				int pt=y*i_size.w*3;
				int x,v;
				for (x = pix_count-1; x >=pix_mod_part; x--) {
					v=((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3;
					o_histogram[v]++;
					pt+=3;
				}
				//タイリング
				for (;x>=0;x-=8){
					v=((input[pt+ 0]& 0xff)+(input[pt+ 1]& 0xff)+(input[pt+ 2]& 0xff))/3;
					o_histogram[v]++;
					v=((input[pt+ 3]& 0xff)+(input[pt+ 4]& 0xff)+(input[pt+ 5]& 0xff))/3;
					o_histogram[v]++;
					v=((input[pt+ 6]& 0xff)+(input[pt+ 7]& 0xff)+(input[pt+ 8]& 0xff))/3;
					o_histogram[v]++;
					v=((input[pt+ 9]& 0xff)+(input[pt+10]& 0xff)+(input[pt+11]& 0xff))/3;
					o_histogram[v]++;
					v=((input[pt+12]& 0xff)+(input[pt+13]& 0xff)+(input[pt+14]& 0xff))/3;
					o_histogram[v]++;
					v=((input[pt+15]& 0xff)+(input[pt+16]& 0xff)+(input[pt+17]& 0xff))/3;
					o_histogram[v]++;
					v=((input[pt+18]& 0xff)+(input[pt+19]& 0xff)+(input[pt+20]& 0xff))/3;
					o_histogram[v]++;
					v=((input[pt+21]& 0xff)+(input[pt+22]& 0xff)+(input[pt+23]& 0xff))/3;
					o_histogram[v]++;
					pt+=3*8;
				}
			}
			return i_size.w*i_size.h;
		}
	}

	class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_B8G8R8X8_32 implements ICreateHistogramImpl
	{
		public int createHistogram(INyARRaster i_reader,NyARIntSize i_size, int[] o_histogram,int i_skip)
		{
	        assert(i_reader.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
	        byte[] input = (byte[])i_reader.getBuffer();
	        int pix_count = i_size.w;
	        int pix_mod_part = pix_count - (pix_count % 8);
	        for (int y = i_size.h - 1; y >= 0; y -= i_skip)
	        {
	            int pt = y * i_size.w * 4;
	            int x, v;
	            for (x = pix_count - 1; x >= pix_mod_part; x--)
	            {
	                v = ((input[pt + 0] & 0xff) + (input[pt + 1] & 0xff) + (input[pt + 2] & 0xff)) / 3;
	                o_histogram[v]++;
	                pt += 4;
	            }
	            //タイリング
	            for (; x >= 0; x -= 8)
	            {
	                v = ((input[pt + 0] & 0xff) + (input[pt + 1] & 0xff) + (input[pt + 2] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 4] & 0xff) + (input[pt + 5] & 0xff) + (input[pt + 6] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 8] & 0xff) + (input[pt + 9] & 0xff) + (input[pt + 10] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 12] & 0xff) + (input[pt + 13] & 0xff) + (input[pt + 14] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 16] & 0xff) + (input[pt + 17] & 0xff) + (input[pt + 18] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 20] & 0xff) + (input[pt + 21] & 0xff) + (input[pt + 22] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 24] & 0xff) + (input[pt + 25] & 0xff) + (input[pt + 26] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 28] & 0xff) + (input[pt + 29] & 0xff) + (input[pt + 30] & 0xff)) / 3;
	                o_histogram[v]++;
	                pt += 4 * 8;
	            }
	        }
	        return i_size.w*i_size.h;
	    }
	}

	class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_X8R8G8B8_32 implements ICreateHistogramImpl
	{
		public int createHistogram(INyARRaster i_reader,NyARIntSize i_size, int[] o_histogram,int i_skip)
		{
	        assert(i_reader.isEqualBufferType(NyARBufferType.BYTE1D_X8R8G8B8_32));
	        byte[] input = (byte[])i_reader.getBuffer();
	        int pix_count = i_size.w;
	        int pix_mod_part = pix_count - (pix_count % 8);
	       	for (int y = i_size.h - 1; y >= 0; y -=i_skip)
	        {
	            int pt = y * i_size.w * 4;
	            int x, v;
	            for (x = pix_count - 1; x >= pix_mod_part; x--)
	            {
	                v = ((input[pt + 1] & 0xff) + (input[pt + 2] & 0xff) + (input[pt + 3] & 0xff)) / 3;
	                o_histogram[v]++;
	                pt += 4;
	            }
	            //タイリング
	            for (; x >= 0; x -= 8)
	            {
	                v = ((input[pt + 1] & 0xff) + (input[pt + 2] & 0xff) + (input[pt + 3] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 5] & 0xff) + (input[pt + 6] & 0xff) + (input[pt + 7] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 9] & 0xff) + (input[pt + 10] & 0xff) + (input[pt + 11] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 13] & 0xff) + (input[pt + 14] & 0xff) + (input[pt + 15] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 17] & 0xff) + (input[pt + 18] & 0xff) + (input[pt + 19] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 21] & 0xff) + (input[pt + 22] & 0xff) + (input[pt + 23] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 25] & 0xff) + (input[pt + 26] & 0xff) + (input[pt + 27] & 0xff)) / 3;
	                o_histogram[v]++;
	                v = ((input[pt + 29] & 0xff) + (input[pt + 30] & 0xff) + (input[pt + 31] & 0xff)) / 3;
	                o_histogram[v]++;
	                pt += 4 * 8;
	            }
	        }
	       	return i_size.w*i_size.h;
	    }
	}

	class NyARRasterThresholdAnalyzer_Histogram_WORD1D_R5G6B5_16LE implements ICreateHistogramImpl
	{
		public int createHistogram(INyARRaster i_reader,NyARIntSize i_size, int[] o_histogram,int i_skip)
		{
	        assert(i_reader.isEqualBufferType(NyARBufferType.WORD1D_R5G6B5_16LE));
	        short[] input = (short[])i_reader.getBuffer();
	        int pix_count = i_size.w;
	        int pix_mod_part = pix_count - (pix_count % 8);
	        for (int y = i_size.h - 1; y >= 0; y -= i_skip)
	        {
	            int pt = y * i_size.w;
	            int x, v;
	            for (x = pix_count - 1; x >= pix_mod_part; x--)
	            {
	                v =(int)input[pt];
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                pt++;
	            }
	            //タイリング
	            for (; x >= 0; x -= 8)
	            {
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	                v =(int)input[pt];pt++;
	                v = (((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3;
	                o_histogram[v]++;
	            }
	        }
	        return i_size.w*i_size.h;
	    }
	}


}