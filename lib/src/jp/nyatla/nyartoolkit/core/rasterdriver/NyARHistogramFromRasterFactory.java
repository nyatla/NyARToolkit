/* 
 * PROJECT: NyARToolkit(Extension)
 * -------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.rasterdriver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * INyARHistogramFromRasterを生成します。
 * 対応しているラスタは、以下の通りです。
 * <ul>
 * <li>{@link INyARGrayscaleRaster}の継承ラスタ</li>
 * <li>{@link INyARRgbRaster}の継承ラスタ</li>
 * <li>{@link NyARBufferType#INT1D_GRAY_8}形式のバッファを持つもの<li>
 * <li>{@link NyARBufferType#INT1D_BIN_8}形式のバッファを持つもの<li>
 */
public class NyARHistogramFromRasterFactory
{
	public static INyARHistogramFromRaster createInstance(INyARGrayscaleRaster i_raster) throws NyARException
	{
		switch(i_raster.getBufferType()){
		case NyARBufferType.INT1D_GRAY_8:
		case NyARBufferType.INT1D_BIN_8:
			return new NyARHistogramFromRaster_INTGS8(i_raster);
		default:
			if(i_raster instanceof INyARGrayscaleRaster){
				return new NyARHistogramFromRaster_AnyGs((INyARGrayscaleRaster)i_raster);
			}
			if(i_raster instanceof INyARRgbRaster){
				return new NyARHistogramFromRaster_AnyRgb((INyARRgbRaster)i_raster);
			}
		}
		throw new NyARException();
	}
	public static INyARHistogramFromRaster createInstance(INyARRgbRaster i_raster) throws NyARException
	{
		if(i_raster instanceof INyARRgbRaster){
			return new NyARHistogramFromRaster_AnyRgb((INyARRgbRaster)i_raster);
		}
		throw new NyARException();
	}
	
}

//ラスタドライバ

class NyARHistogramFromRaster_AnyGs implements INyARHistogramFromRaster
{
	private INyARGrayscaleRaster _gsr;
	public NyARHistogramFromRaster_AnyGs(INyARGrayscaleRaster i_raster)
	{
		this._gsr=i_raster;
	}
	public void createHistogram(int i_skip,NyARHistogram o_histogram) throws NyARException
	{
		NyARIntSize s=this._gsr.getSize();
		this.createHistogram(0,0,s.w,s.h,i_skip,o_histogram);
	}
	public void createHistogram(int i_l,int i_t,int i_w,int i_h,int i_skip,NyARHistogram o_histogram) throws NyARException
	{
		o_histogram.reset();
		int[] data_ptr=o_histogram.data;
		INyARGsPixelDriver drv=this._gsr.getGsPixelDriver();
		final int pix_count=i_w;
		final int pix_mod_part=pix_count-(pix_count%8);
		//左上から1行づつ走査していく
		for (int y = i_h-1; y >=0 ; y-=i_skip){
			for (int x = pix_count-1; x >=pix_mod_part; x--){
				data_ptr[drv.getPixel(x,y)]++;
			}
		}
		o_histogram.total_of_data=i_w*i_h/i_skip;
		return;
	}	
}

class NyARHistogramFromRaster_AnyRgb implements INyARHistogramFromRaster
{
	private INyARRgbRaster _gsr;
	public NyARHistogramFromRaster_AnyRgb(INyARRgbRaster i_raster)
	{
		this._gsr=i_raster;
	}
	public void createHistogram(int i_skip,NyARHistogram o_histogram) throws NyARException
	{
		NyARIntSize s=this._gsr.getSize();
		this.createHistogram(0,0,s.w,s.h,i_skip,o_histogram);
	}
	private int[] tmp=new int[3];
	public void createHistogram(int i_l,int i_t,int i_w,int i_h,int i_skip,NyARHistogram o_histogram) throws NyARException
	{
		o_histogram.reset();
		int[] data_ptr=o_histogram.data;
		INyARRgbPixelDriver drv=this._gsr.getRgbPixelDriver();
		final int pix_count=i_w;
		final int pix_mod_part=pix_count-(pix_count%8);			
		//左上から1行づつ走査していく
		for (int y = i_h-1; y >=0 ; y-=i_skip){
			for (int x = pix_count-1; x >=pix_mod_part; x--){
				drv.getPixel(x,y,tmp);
				data_ptr[(tmp[0]+tmp[1]+tmp[2])/3]++;
			}
		}
		o_histogram.total_of_data=i_w*i_h/i_skip;
		return;
	}	
}


class NyARHistogramFromRaster_INTGS8 implements INyARHistogramFromRaster
{
	private INyARRaster _gsr;
	public NyARHistogramFromRaster_INTGS8(INyARRaster i_raster)
	{
		this._gsr=i_raster;
	}
	public void createHistogram(int i_skip,NyARHistogram o_histogram) throws NyARException
	{
		NyARIntSize s=this._gsr.getSize();
		this.createHistogram(0,0,s.w,s.h,i_skip,o_histogram);
	}
	public void createHistogram(int i_l,int i_t,int i_w,int i_h,int i_skip,NyARHistogram o_histogram)
	{
		o_histogram.reset();
		final int[] input=(int[])this._gsr.getBuffer();
		NyARIntSize s=this._gsr.getSize();
		int skip=(i_skip*s.w-i_w);
		final int pix_count=i_w;
		final int pix_mod_part=pix_count-(pix_count%8);			
		//左上から1行づつ走査していく
		int pt=(i_t*s.w+i_l);
		int[] data=o_histogram.data;
		for (int y = i_h-1; y >=0 ; y-=i_skip){
			int x;
			for (x = pix_count-1; x >=pix_mod_part; x--){
				data[input[pt++]]++;
			}
			for (;x>=0;x-=8){
				data[input[pt++]]++;
				data[input[pt++]]++;
				data[input[pt++]]++;
				data[input[pt++]]++;
				data[input[pt++]]++;
				data[input[pt++]]++;
				data[input[pt++]]++;
				data[input[pt++]]++;
			}
			//スキップ
			pt+=skip;
		}
		o_histogram.total_of_data=i_w*i_h/i_skip;
		return;
	}
}





//class NyARRasterThresholdAnalyzer_Histogram_INT1D_X8R8G8B8_32 implements NyARRasterAnalyzer_Histogram.IFilter
//{
//	public boolean isSupport(INyARRaster i_raster)
//	{
//		return i_raster.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32);
//	}
//	public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
//	{
//		assert (i_raster.isEqualBufferType( NyARBufferType.INT1D_X8R8G8B8_32));
//		final int[] input=(int[])i_raster.getBuffer();
//		NyARIntSize s=i_raster.getSize();
//		int skip=(i_skip*s.w-i_w);
//		final int pix_count=i_w;
//		final int pix_mod_part=pix_count-(pix_count%8);			
//		//左上から1行づつ走査していく
//		int pt=(i_t*s.w+i_l);
//		for (int y = i_h-1; y >=0 ; y-=i_skip){
//			int x,v;
//			for (x = pix_count-1; x >=pix_mod_part; x--){
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//			}
//			for (;x>=0;x-=8){
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//				v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
//			}
//			//スキップ
//			pt+=skip;
//		}
//		return;			
//	}	
//}
//
//
//class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_RGB_24 implements NyARRasterAnalyzer_Histogram.IFilter
//{
//	public boolean isSupport(INyARRaster i_raster)
//	{
//		return i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24) || i_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24);
//	}
//	public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
//	{
//		assert (
//				i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||
//				i_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
//		final byte[] input=(byte[])i_raster.getBuffer();
//		NyARIntSize s=i_raster.getSize();
//		int skip=(i_skip*s.w-i_w)*3;
//		final int pix_count=i_w;
//		final int pix_mod_part=pix_count-(pix_count%8);			
//		//左上から1行づつ走査していく
//		int pt=(i_t*s.w+i_l)*3;
//		for (int y = i_h-1; y >=0 ; y-=i_skip){
//			int x;
//			for (x = pix_count-1; x >=pix_mod_part; x--){
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=3;
//			}
//			for (;x>=0;x-=8){
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=3*8;
//			}
//			//スキップ
//			pt+=skip;
//		}
//		return;	
//	}
//}
//
//class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_B8G8R8X8_32 implements NyARRasterAnalyzer_Histogram.IFilter
//{
//	public boolean isSupport(INyARRaster i_raster)
//	{
//		return i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32);
//	}
//	public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
//	{
//        assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
//		final byte[] input=(byte[])i_raster.getBuffer();
//		NyARIntSize s=i_raster.getSize();
//		int skip=(i_skip*s.w-i_w)*4;
//		final int pix_count=i_w;
//		final int pix_mod_part=pix_count-(pix_count%8);			
//		//左上から1行づつ走査していく
//		int pt=(i_t*s.w+i_l)*4;
//		for (int y = i_h-1; y >=0 ; y-=i_skip){
//			int x;
//			for (x = pix_count-1; x >=pix_mod_part; x--){
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//			}
//			for (;x>=0;x-=8){
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
//				pt+=4;
//			}
//			//スキップ
//			pt+=skip;
//		}
//		return;	
//    }
//}
//
//class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_X8R8G8B8_32 implements NyARRasterAnalyzer_Histogram.IFilter
//{
//	public boolean isSupport(INyARRaster i_raster)
//	{
//		return i_raster.isEqualBufferType(NyARBufferType.BYTE1D_X8R8G8B8_32);
//	}	
//	public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
//	{
//        assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_X8R8G8B8_32));
//		final byte[] input=(byte[])i_raster.getBuffer();
//		NyARIntSize s=i_raster.getSize();
//		int skip=(i_skip*s.w-i_w)*4;
//		final int pix_count=i_w;
//		final int pix_mod_part=pix_count-(pix_count%8);			
//		//左上から1行づつ走査していく
//		int pt=(i_t*s.w+i_l)*4;
//		for (int y = i_h-1; y >=0 ; y-=i_skip){
//			int x;
//			for (x = pix_count-1; x >=pix_mod_part; x--){
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+ 3]& 0xff))/3]++;
//				pt+=4;
//			}
//			for (;x>=0;x-=8){
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//				o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
//				pt+=4;
//			}
//			//スキップ
//			pt+=skip;
//		}
//		return;	
//    }
//}
//
//class NyARRasterThresholdAnalyzer_Histogram_WORD1D_R5G6B5_16LE implements NyARRasterAnalyzer_Histogram.IFilter
//{
//	public boolean isSupport(INyARRaster i_raster)
//	{
//		return i_raster.isEqualBufferType(NyARBufferType.WORD1D_R5G6B5_16LE);
//	}		
//	public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
//	{
//        assert(i_raster.isEqualBufferType(NyARBufferType.WORD1D_R5G6B5_16LE));
//		final short[] input=(short[])i_raster.getBuffer();
//		NyARIntSize s=i_raster.getSize();
//		int skip=(i_skip*s.w-i_w);
//		final int pix_count=i_w;
//		final int pix_mod_part=pix_count-(pix_count%8);			
//		//左上から1行づつ走査していく
//		int pt=(i_t*s.w+i_l);
//		for (int y = i_h-1; y >=0 ; y-=i_skip){
//			int x,v;
//			for (x = pix_count-1; x >=pix_mod_part; x--){
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//			}
//			for (;x>=0;x-=8){
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//				v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
//			}
//			//スキップ
//			pt+=skip;
//		}
//		return;	
//    }
//}