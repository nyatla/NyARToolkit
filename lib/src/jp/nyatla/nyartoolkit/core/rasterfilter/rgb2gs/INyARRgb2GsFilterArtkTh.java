/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public interface INyARRgb2GsFilterArtkTh
{
	/**
	 * RGBラスタを(R+G+B)<th*3?0:i_high_valueで2値にします。
	 * @param i_th
	 * @param i_high_val
	 * @param i_gsraster
	 * INT1D_BIN_8形式である必要があります。
	 */
	public void doFilter(int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException;
	public void doFilter(int i_l,int i_t,int i_w,int i_h,int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException;
}

abstract class NyARRgb2GsFilterArtkTh_Base implements INyARRgb2GsFilterArtkTh
{
	protected INyARRgbRaster _raster;
	public void doFilter(int i_h,INyARGrayscaleRaster i_gsraster) throws NyARException
	{
		NyARIntSize s=this._raster.getSize();
		this.doFilter(0,0,s.w,s.h,i_h,i_gsraster);
	}	
}

class NyARRgb2GsFilterArtkTh_BYTE1D_C8C8C8_24 extends NyARRgb2GsFilterArtkTh_Base
{
	public NyARRgb2GsFilterArtkTh_BYTE1D_C8C8C8_24(INyARRgbRaster i_raster)
	{
		assert (
				i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||
				i_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
		this._raster=i_raster;
	}
	public void doFilter(int i_l,int i_t,int i_w,int i_h,int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException
	{
		assert i_gsraster.isEqualBufferType(NyARBufferType.INT1D_BIN_8);
		final byte[] input=(byte[])this._raster.getBuffer();
		final int[] output=(int[])i_gsraster.getBuffer();
		int th=i_th*3;
		NyARIntSize s=this._raster.getSize();
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

class NyARRgb2GsFilterArtkTh_BYTE1D_B8G8R8X8_32 extends NyARRgb2GsFilterArtkTh_Base
{
	public NyARRgb2GsFilterArtkTh_BYTE1D_B8G8R8X8_32(INyARRgbRaster i_raster)
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
		this._raster=i_raster;
	}
	public void doFilter(int i_l,int i_t,int i_w,int i_h,int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException
	{
		assert i_gsraster.isEqualBufferType(NyARBufferType.INT1D_BIN_8);
		final byte[] input=(byte[])this._raster.getBuffer();
		final int[] output=(int[])i_gsraster.getBuffer();
		NyARIntSize s=this._raster.getSize();
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



class NyARRgb2GsFilterArtkTh_BYTE1D_X8R8G8B8_32 extends NyARRgb2GsFilterArtkTh_Base
{
	public NyARRgb2GsFilterArtkTh_BYTE1D_X8R8G8B8_32(INyARRgbRaster i_raster)
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_X8R8G8B8_32));
		this._raster=i_raster;
	}
	public void doFilter(int i_l,int i_t,int i_w,int i_h,int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException
	{
		assert i_gsraster.isEqualBufferType(NyARBufferType.INT1D_BIN_8);
		final byte[] input=(byte[])this._raster.getBuffer();
		final int[] output=(int[])i_gsraster.getBuffer();
		int th=i_th*3;
		NyARIntSize s=this._raster.getSize();
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


class NyARRgb2GsFilterArtkTh_INT1D_X8R8G8B8_32 extends NyARRgb2GsFilterArtkTh_Base
{
	public NyARRgb2GsFilterArtkTh_INT1D_X8R8G8B8_32(INyARRgbRaster i_raster)
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		this._raster=i_raster;
	}
	public void doFilter(int i_l,int i_t,int i_w,int i_h,int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException
	{
		assert i_gsraster.isEqualBufferType(NyARBufferType.INT1D_BIN_8);
		final int[] input=(int[])this._raster.getBuffer();
		final int[] output=(int[])i_gsraster.getBuffer();
		int th=i_th*3;

		NyARIntSize s=this._raster.getSize();
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

class NyARRgb2GsFilterArtkTh_WORD1D_R5G6B5_16LE extends NyARRgb2GsFilterArtkTh_Base
{
	public NyARRgb2GsFilterArtkTh_WORD1D_R5G6B5_16LE(INyARRgbRaster i_raster)
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.WORD1D_R5G6B5_16LE));
		this._raster=i_raster;
	}
	public void doFilter(int i_l,int i_t,int i_w,int i_h,int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException
	{
		assert i_gsraster.isEqualBufferType(NyARBufferType.INT1D_BIN_8);
		final short[] input=(short[])this._raster.getBuffer();
		final int[] output=(int[])i_gsraster.getBuffer();
		int th=i_th*3;
		NyARIntSize s=i_gsraster.getSize();
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


class NyARRgb2GsFilterArtkTh_Any extends NyARRgb2GsFilterArtkTh_Base
{
	public NyARRgb2GsFilterArtkTh_Any(INyARRgbRaster i_raster)
	{
		this._raster=i_raster;
	}
	private int[] __rgb=new int[3];
	public void doFilter(int i_l,int i_t,int i_w,int i_h,int i_th,INyARGrayscaleRaster i_gsraster) throws NyARException
	{
		assert i_gsraster.isEqualBufferType(NyARBufferType.INT1D_BIN_8);
		INyARRgbPixelDriver input=this._raster.getRgbPixelDriver();
		final int[] output=(int[])i_gsraster.getBuffer();
		int th=i_th*3;
		NyARIntSize s=i_gsraster.getSize();
		int skip_dst=(s.w-i_w);
		int skip_src=skip_dst;
		//左上から1行づつ走査していく
		int pt_dst=(i_t*s.w+i_l);
		int[] rgb=this.__rgb;
		for (int y = i_h-1; y >=0 ; y-=1){
			int x;
			for (x = i_w-1; x >=0; x--){
				input.getPixel(x+i_l,y+i_t,rgb);
				output[pt_dst++]=(rgb[0]+rgb[1]+rgb[2])<=th?0:1;
			}
			//スキップ
			pt_dst+=skip_dst;
		}
		return;	
    }
}		

