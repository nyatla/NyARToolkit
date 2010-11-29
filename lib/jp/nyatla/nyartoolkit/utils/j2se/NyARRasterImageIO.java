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
package jp.nyatla.nyartoolkit.utils.j2se;


import java.awt.image.*;
import java.awt.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * BufferdImageとRasterイメージ間で、ビットマップをコピーします。
 */
public class NyARRasterImageIO
{
	/**
	 * i_inの内容を、このイメージにコピーします。
	 * @param i_in
	 * @throws NyARException
	 */
	public static void copy(INyARRgbRaster i_in,BufferedImage o_out) throws NyARException
	{
		assert i_in.getSize().isEqualSize(o_out.getWidth(), o_out.getHeight());
		
		//thisへ転写
		INyARRgbPixelReader reader=i_in.getRgbPixelReader();
		int[] rgb=new int[3];

		for(int y=o_out.getHeight()-1;y>=0;y--){
			for(int x=o_out.getWidth()-1;x>=0;x--){
				reader.getPixel(x,y,rgb);
				o_out.setRGB(x,y,(rgb[0]<<16)|(rgb[1]<<8)|rgb[2]);
			}
		}
		return;
	}
	/**
	 * GrayScale用
	 * @param i_in
	 * @throws NyARException
	 */
	public static void copy(NyARGrayscaleRaster i_in,BufferedImage o_out) throws NyARException
	{
		assert i_in.getSize().isEqualSize(o_out.getWidth(), o_out.getHeight());
		if(i_in.isEqualBufferType(NyARBufferType.INT1D_GRAY_8))
		{
			final int[] buf=(int[])i_in.getBuffer();
			final int w=o_out.getWidth();
			final int h=o_out.getHeight();
			for(int y=h-1;y>=0;y--){
				for(int x=w-1;x>=0;x--){
					int v=buf[x+y*w];
					o_out.setRGB(x, y,v*(1+0x100+0x10000));
				}
			}
		}
		return;
	}	
	/**
	 * BIN_8用
	 * @param i_in
	 * @throws NyARException
	 */
	public static void copy(INyARRaster i_in,BufferedImage o_out) throws NyARException
	{
		assert i_in.getSize().isEqualSize(o_out.getWidth(), o_out.getHeight());
		if(i_in.isEqualBufferType(NyARBufferType.INT1D_BIN_8))
		{
			final int[] buf=(int[])i_in.getBuffer();
			final int w=o_out.getWidth();
			final int h=o_out.getHeight();
			for(int y=h-1;y>=0;y--){
				for(int x=w-1;x>=0;x--){
					o_out.setRGB(x, y,buf[x+y*w]==0?0:0xffffff);
				}
			}
		}
		return;
	}	
	/**
	 * ヒストグラムを書き出します。
	 * @param i_in
	 * @param o_out
	 * @throws NyARException
	 */
	public static void copy(NyARHistogram i_in,int i_l,int i_t,int i_h,Graphics i_g) throws NyARException
	{
		//正規化のための定数を出す。
		int max=0;
		for(int i=0;i<i_in.length;i++){
			max=max<i_in.data[i]?i_in.data[i]:max;
		}
		if(max==0){
			return;
		}
		//ヒストグラムを書く
		for(int i=0;i<i_in.length;i++){
			i_g.drawLine(i_l+i,i_t,i_l+i,i_h-i_h*i_in.data[i]/max);
		}
		return;
	}	
	/**
	 * i_outへこのイメージを出力します。
	 * 
	 * @param i_out
	 * @throws NyARException
	 */
	public static void copy(BufferedImage i_in,INyARRgbRaster o_out) throws NyARException
	{
		assert o_out.getSize().isEqualSize(i_in.getWidth(), i_in.getHeight());
		
		//thisへ転写
		INyARRgbPixelReader reader=o_out.getRgbPixelReader();
		int[] rgb=new int[3];
		for(int y=i_in.getHeight()-1;y>=0;y--){
			for(int x=i_in.getWidth()-1;x>=0;x--){
				int pix=i_in.getRGB(x, y);
				rgb[0]=(pix>>16)&0xff;
				rgb[1]=(pix>>8)&0xff;
				rgb[2]=(pix)&0xff;
				reader.setPixel(x,y,rgb);
			}
		}
		return;
	}
	/**
	 * BIN_8用
	 * @param i_in
	 * @throws NyARException
	 */
	public static void copy(BufferedImage i_in,INyARRaster o_out) throws NyARException
	{
		assert o_out.getSize().isEqualSize(i_in.getWidth(), i_in.getHeight());
		if(o_out.isEqualBufferType(NyARBufferType.INT1D_BIN_8))
		{
			final int[] buf=(int[])o_out.getBuffer();
			final int w=i_in.getWidth();
			final int h=i_in.getHeight();
			for(int y=h-1;y>=0;y--){
				for(int x=w-1;x>=0;x--){
					buf[x+y*w]=(i_in.getRGB(x, y)&0xffffff)>0?1:0;
				}
			}
		}
		return;
	}
	
}
