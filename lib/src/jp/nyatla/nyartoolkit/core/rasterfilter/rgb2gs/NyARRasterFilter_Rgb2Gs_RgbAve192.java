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
package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * RGBラスタをGrayScaleに変換するフィルタを作成します。
 * このフィルタは、RGB値の平均値を、(R+G+B)>>4で算出します。(スケールは、192>=n>=0になります。)
 *
 */
public class NyARRasterFilter_Rgb2Gs_RgbAve192 implements INyARRasterFilter_Rgb2Gs
{
	IdoThFilterImpl _do_filter_impl;
	public NyARRasterFilter_Rgb2Gs_RgbAve192(int i_in_raster_type,int i_out_raster_type) throws NyARException
	{
		if(!initInstance(i_in_raster_type,i_out_raster_type))
		{
			throw new NyARException();
		}
	}
	public NyARRasterFilter_Rgb2Gs_RgbAve192(int i_in_raster_type) throws NyARException
	{
		if(!initInstance(i_in_raster_type,NyARBufferType.INT1D_GRAY_8))
		{
			throw new NyARException();
		}
	}
	protected boolean initInstance(int i_in_raster_type,int i_out_raster_type)
	{
		switch(i_out_raster_type){
		case NyARBufferType.INT1D_GRAY_8:
			switch (i_in_raster_type){
			case NyARBufferType.BYTE1D_B8G8R8_24:
			case NyARBufferType.BYTE1D_R8G8B8_24:
				this._do_filter_impl=new doThFilterImpl_BYTE1D_B8G8R8_24();
				break;
			case NyARBufferType.BYTE1D_B8G8R8X8_32:
				this._do_filter_impl=new doThFilterImpl_BYTE1D_B8G8R8X8_32();
				break;
			case NyARBufferType.INT1D_X8R8G8B8_32:
				this._do_filter_impl=new doThFilterImpl_BUFFERFORMAT_INT1D_X8R8G8B8_32();
				break;
			default:
				return false;
			}
			break;
		default:
			return false;
		}
		return true;
	}
	public void doFilter(INyARRgbRaster i_input, NyARGrayscaleRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		NyARIntSize s=i_input.getSize();
		this._do_filter_impl.doFilter(i_input,(int[])i_output.getBuffer(),0,0,s.w,s.h);
		return;
	}
	/**
	 * 同一サイズのラスタi_inputとi_outputの間で、一部の領域だけにラスタ処理を実行します。
	 * @param i_input
	 * @param i_rect
	 * @param i_output
	 * @throws NyARException
	 */
	public void doFilter(INyARRgbRaster i_input,NyARIntRect i_rect, NyARGrayscaleRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._do_filter_impl.doFilter(i_input,(int[])i_output.getBuffer(),i_rect.x,i_rect.y,i_rect.w,i_rect.h);
		
	}
	/**
	 * 異サイズのラスタi_inputとi_outputの間で、一部の領域をi_outputへ転送します。
	 * 関数は、i_outputのサイズをi_skip倍した領域を、i_inputのi_left,i_topの位置から切り出し、フィルタ処理をしてi_outputへ格納します。
	 * @param i_input
	 * @param i_left
	 * @param i_top
	 * @param i_skip
	 * @param i_output
	 */
	public void doCutFilter(INyARRgbRaster i_input,int i_left,int i_top,int i_skip,NyARGrayscaleRaster i_output) throws NyARException
	{
		this._do_filter_impl.doCutFilter(i_input,i_left,i_top,i_skip,i_output);		
	}
	/*
	 * ここから各種ラスタ向けのフィルタ実装
	 *
	 */
	interface IdoThFilterImpl
	{
		/**
		 * 同一サイズのラスタ間での転送
		 * @param i_input
		 * @param o_output
		 * @param l
		 * @param t
		 * @param w
		 * @param h
		 */
		public void doFilter(INyARRaster i_input,int[] o_output, int l,int t,int w,int h) throws NyARException;
		/**
		 * 異サイズラスタ間での転送
		 * @param i_input
		 * @param l
		 * @param t
		 * @param i_st
		 * @param o_output
		 */
		public void doCutFilter(INyARRaster i_input, int l,int t,int i_st,NyARGrayscaleRaster o_output) throws NyARException;
	}
	
	class doThFilterImpl_BUFFERFORMAT_INT1D_X8R8G8B8_32 implements IdoThFilterImpl
	{
		public void doCutFilter(INyARRaster i_input, int l,int t,int i_st,NyARGrayscaleRaster o_output) throws NyARException
		{
			assert(i_input.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
			assert(i_input.getSize().isInnerSize(l+o_output.getWidth()*i_st,t+o_output.getHeight()*i_st));
			final int[] input=(int[])i_input.getBuffer();
			final int[] output=(int[])o_output.getBuffer();
			int v;
			int pt_src,pt_dst;
			NyARIntSize dest_size=o_output.getSize();			
			NyARIntSize src_size=i_input.getSize();
			int skip_src_y=(src_size.w-dest_size.w*i_st)+src_size.w*(i_st-1);
			int skip_src_x=i_st;
			final int pix_count=dest_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			pt_dst=0;
			pt_src=(t*src_size.w+l);
			for (int y = dest_size.h-1; y >=0; y-=1){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
				}
				for (;x>=0;x-=8){
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
				}
				//スキップ
				pt_src+=skip_src_y;
			}
			return;		
		}
		public void doFilter(INyARRaster i_input, int[] o_output,int l,int t,int w,int h)
		{
			assert(i_input.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
			NyARIntSize size=i_input.getSize();
			int[] in_buf = (int[]) i_input.getBuffer();
			int bp = (l+t*size.w);
			int v;
			final int b=t+h;
			final int row_padding_dst=(size.w-w);
			final int row_padding_src=row_padding_dst;
			final int pix_count=w;
			final int pix_mod_part=pix_count-(pix_count%8);
			int src_ptr=t*size.w+l;
			for (int y = t; y < b; y++) {
				int x=0;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
				}
				for (;x>=0;x-=8){
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
				}
				bp+=row_padding_dst;
				src_ptr+=row_padding_src;
			}
			return;			
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	class doThFilterImpl_BYTE1D_B8G8R8_24 implements IdoThFilterImpl
	{
		public void doCutFilter(INyARRaster i_input, int l,int t,int i_st,NyARGrayscaleRaster o_output) throws NyARException
		{
			assert(i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||i_input.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
			assert(i_input.getSize().isInnerSize(l+o_output.getWidth()*i_st,t+o_output.getHeight()*i_st));
			
			final byte[] input=(byte[])i_input.getBuffer();
			final int[] output=(int[])o_output.getBuffer();
			int pt_src,pt_dst;
			NyARIntSize dest_size=o_output.getSize();			
			NyARIntSize src_size=i_input.getSize();
			int skip_src_y=(src_size.w-dest_size.w*i_st)*3+src_size.w*(i_st-1)*3;
			int skip_src_x=3*i_st;
			final int pix_count=dest_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			pt_dst=0;
			pt_src=(t*src_size.w+l)*3;
			for (int y = dest_size.h-1; y >=0; y-=1){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
				}
				for (;x>=0;x-=8){
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
					output[pt_dst++]=((input[pt_src+0]& 0xff)+(input[pt_src+1]& 0xff)+(input[pt_src+2]& 0xff))>>2;
					pt_src+=skip_src_x;
				}
				//スキップ
				pt_src+=skip_src_y;
			}
			return;
		}
		public void doFilter(INyARRaster i_input, int[] o_output,int l,int t,int w,int h)
		{
			assert(i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||i_input.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
			
			NyARIntSize size=i_input.getSize();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			int bp = (l+t*size.w)*3;
			final int b=t+h;
			final int row_padding_dst=(size.w-w);
			final int row_padding_src=row_padding_dst*3;
			final int pix_count=w;
			final int pix_mod_part=pix_count-(pix_count%8);
			int src_ptr=t*size.w+l;
			for (int y = t; y < b; y++) {
				
				int x=0;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
				}
				for (;x>=0;x-=8){
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
					o_output[src_ptr++] = ((in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff) + (in_buf[bp++] & 0xff)) >>2;
				}
				
				bp+=row_padding_dst;
				src_ptr+=row_padding_src;
			}
			return;
		}		
	}
	class doThFilterImpl_BYTE1D_B8G8R8X8_32 implements IdoThFilterImpl
	{
		public void doCutFilter(INyARRaster i_input, int l,int t,int i_st,NyARGrayscaleRaster o_output) throws NyARException
		{
			NyARException.notImplement();
		}
		public void doFilter(INyARRaster i_input, int[] o_output,int l,int t,int w,int h) throws NyARException
		{
			assert(i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
			NyARIntSize size=i_input.getSize();
			byte[] in_buf = (byte[]) i_input.getBuffer();

			int bp = (l+t*size.w)*4;
			final int b=t+h;
			final int row_padding=(size.w-w)*4;
			for (int y = t; y < b; y++) {
				for (int x = 0; x < w; x++) {
					o_output[y*size.w+x+l] = ((in_buf[bp] & 0xff) + (in_buf[bp + 1] & 0xff) + (in_buf[bp + 2] & 0xff)) >>2;
					bp += 4;
				}
				bp+=row_padding;
			}
		}
	}

}






