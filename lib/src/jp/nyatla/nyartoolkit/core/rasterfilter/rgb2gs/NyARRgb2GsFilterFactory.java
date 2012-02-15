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
package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARRgb2GsFilterFactory
{
	/**
	 * この関数は、(R*G*B)/3 でグレースケール化するフィルタを生成します。
	 * 最適化されている形式は以下の通りです。
	 * <ul>
	 * <li>{@link NyARBufferType#BYTE1D_B8G8R8X8_32}</li>
	 * </ul>
	 * @param i_raster
	 * @return
	 * @throws NyARException
	 */
	public static INyARRgb2GsFilterRgbAve createRgbAveDriver(INyARRgbRaster i_raster) throws NyARException
	{
		switch(i_raster.getBufferType()){
		case NyARBufferType.BYTE1D_B8G8R8X8_32:
			return new NyARRgb2GsFilterRgbAve_BYTE1D_B8G8R8X8_32(i_raster);
		case NyARBufferType.BYTE1D_B8G8R8_24:
			return new NyARRgb2GsFilterRgbAve_BYTE1D_C8C8C8_24(i_raster);
		case NyARBufferType.BYTE1D_X8R8G8B8_32:
			return new NyARRgb2GsFilterRgbAve_INT1D_X8R8G8B8_32(i_raster);
		default:
			return new NyARRgb2GsFilterRgbAve_Any(i_raster);
		}
	}
	/**
	 * この関数は、(R*G*B>>16) でグレースケール化するフィルタを生成します。
	 * 最適化されていません。
	 * @param i_raster
	 * @return
	 * @throws NyARException
	 */
	public static INyARRgb2GsFilterRgbAve createRgbCubeDriver(INyARRgbRaster i_raster) throws NyARException
	{
		switch(i_raster.getBufferType()){
		default:
			return new NyARRgb2GsFilterRgbCube_Any(i_raster);
		}
	}
	/**
	 * この関数は(Yrcb)でグレースケール化するフィルタを生成します。
	 * 最適化されていません。
	 * @param i_raster
	 * @return
	 * @throws NyARException
	 */
	public static INyARRgb2GsFilterYCbCr createYCbCrDriver(INyARRgbRaster i_raster) throws NyARException
	{
		switch(i_raster.getBufferType()){
		default:
			return new NyARRgb2GsFilterYCbCr_Any(i_raster);
		}
	}
}


////////////////////////////////////////////////////////////////////////////////
//
// RgbAveのラスタドライバ
//
////////////////////////////////////////////////////////////////////////////////



class NyARRgb2GsFilterRgbAve_BYTE1D_B8G8R8X8_32 implements INyARRgb2GsFilterRgbAve
{
	private INyARRaster _ref_raster;
	public NyARRgb2GsFilterRgbAve_BYTE1D_B8G8R8X8_32(INyARRaster i_ref_raster)
	{
		assert i_ref_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32);
		this._ref_raster=i_ref_raster;
	}
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException
	{
		NyARIntSize s=this._ref_raster.getSize();
		this.convertRect(0,0,s.w,s.h,i_raster);
	}
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster o_raster) throws NyARException
	{
		NyARIntSize size=this._ref_raster.getSize();
		int bp = (l+t*size.w)*4;
		final int b=t+h;
		final int row_padding_dst=(size.w-w);
		final int row_padding_src=row_padding_dst*4;
		final int pix_count=w;
		final int pix_mod_part=pix_count-(pix_count%8);
		int dst_ptr=t*size.w+l;
		byte[] in_buf = (byte[]) this._ref_raster.getBuffer();
		switch(o_raster.getBufferType()){
		case NyARBufferType.INT1D_GRAY_8:
			int[] out_buf=(int[])o_raster.getBuffer();
			for (int y = t; y < b; y++) {
				
				int x=0;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
				}
				for (;x>=0;x-=8){
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3;					
					bp+=4;
				}
				bp+=row_padding_src;
				dst_ptr+=row_padding_dst;
			}
			return;
		default:
			INyARGsPixelDriver out_drv=o_raster.getGsPixelDriver();
			for (int y = t; y < b; y++) {
				for (int x = 0; x<pix_count; x++){
					out_drv.setPixel(x,y,((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3);
					bp+=4;
				}
				bp+=row_padding_src;
			}
			return;
		}
	}
}


class NyARRgb2GsFilterRgbAve_BYTE1D_C8C8C8_24 implements INyARRgb2GsFilterRgbAve
{
	private INyARRaster _ref_raster;
	public NyARRgb2GsFilterRgbAve_BYTE1D_C8C8C8_24(INyARRaster i_ref_raster)
	{
		assert i_ref_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24) || i_ref_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24);
		this._ref_raster=i_ref_raster;
	}
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException
	{
		NyARIntSize s=this._ref_raster.getSize();
		this.convertRect(0,0,s.w,s.h,i_raster);
	}
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster o_raster) throws NyARException
	{
		NyARIntSize size=this._ref_raster.getSize();
		int bp = (l+t*size.w)*3;
		final int b=t+h;
		final int row_padding_dst=(size.w-w);
		final int row_padding_src=row_padding_dst*3;
		final int pix_count=w;
		final int pix_mod_part=pix_count-(pix_count%8);
		int dst_ptr=t*size.w+l;
		byte[] in_buf = (byte[]) this._ref_raster.getBuffer();
		switch(o_raster.getBufferType()){
		case NyARBufferType.INT1D_GRAY_8:
			int[] out_buf=(int[])o_raster.getBuffer();
			for (int y = t; y < b; y++) {
				
				int x=0;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
				}
				for (;x>=0;x-=8){
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
					out_buf[dst_ptr++] = ((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff))/3;
					bp+=3;
				}
				bp+=row_padding_src;
				dst_ptr+=row_padding_dst;
			}
			return;
		default:
			INyARGsPixelDriver out_drv=o_raster.getGsPixelDriver();
			for (int y = t; y < b; y++) {
				for (int x = 0; x<pix_count; x++){
					out_drv.setPixel(x,y,((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3);
					bp+=3;
				}
				bp+=row_padding_src;
			}
			return;
		}
	}
}

class NyARRgb2GsFilterRgbAve_INT1D_X8R8G8B8_32 implements INyARRgb2GsFilterRgbAve
{
	private INyARRaster _ref_raster;
	public NyARRgb2GsFilterRgbAve_INT1D_X8R8G8B8_32(INyARRaster i_ref_raster)
	{
		assert i_ref_raster.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32);
		this._ref_raster=i_ref_raster;
	}
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException
	{
		NyARIntSize s=this._ref_raster.getSize();
		this.convertRect(0,0,s.w,s.h,i_raster);
	}
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster o_raster) throws NyARException
	{
		NyARIntSize size=this._ref_raster.getSize();
		int bp = (l+t*size.w);
		final int b=t+h;
		final int row_padding_dst=(size.w-w);
		final int row_padding_src=row_padding_dst;
		final int pix_count=w;
		final int pix_mod_part=pix_count-(pix_count%8);
		int dst_ptr=t*size.w+l;
		int[] in_buf = (int[]) this._ref_raster.getBuffer();
		switch(o_raster.getBufferType()){
		case NyARBufferType.INT1D_GRAY_8:
			int v;
			int[] out_buf=(int[])o_raster.getBuffer();
			for (int y = t; y < b; y++) {
				int x=0;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
				}
				for (;x>=0;x-=8){
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
					v=in_buf[dst_ptr++];out_buf[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3;
				}
				bp+=row_padding_src;
				dst_ptr+=row_padding_dst;
			}
			return;
		default:
			INyARGsPixelDriver out_drv=o_raster.getGsPixelDriver();
			for (int y = t; y < b; y++) {
				for (int x = 0; x<pix_count; x++){
					v=in_buf[dst_ptr++];
					out_drv.setPixel(x,y,(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))/3);
				}
			}
			return;
		}
	}
}





class NyARRgb2GsFilterRgbAve_Any implements INyARRgb2GsFilterRgbAve
{
	private INyARRgbRaster _ref_raster;
	public NyARRgb2GsFilterRgbAve_Any(INyARRgbRaster i_ref_raster)
	{
		this._ref_raster=i_ref_raster;
	}
	private int[] _wk=new int[3];
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException
	{
		NyARIntSize s=this._ref_raster.getSize();
		this.convertRect(0,0,s.w,s.h,i_raster);
	}	
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster o_raster) throws NyARException
	{
		int[] wk=this._wk;
		final int b=t+h;
		final int pix_count=w;
		switch(o_raster.getBufferType()){
		default:
			INyARGsPixelDriver out_drv=o_raster.getGsPixelDriver();
			INyARRgbPixelDriver in_drv=this._ref_raster.getRgbPixelDriver();
			for (int y = t; y < b; y++) {
				for (int x = pix_count-1; x >=0; x--){
					in_drv.getPixel(x,y,wk);
					out_drv.setPixel(x,y,(wk[0]+wk[1]+wk[2])/3);
				}
			}
			return;
		}
	}
}

////////////////////////////////////////////////////////////////////////////////
//
// RgbCubeのラスタドライバ
//
////////////////////////////////////////////////////////////////////////////////


class NyARRgb2GsFilterRgbCube_Any implements INyARRgb2GsFilterRgbAve
{
	private INyARRgbRaster _ref_raster;
	public NyARRgb2GsFilterRgbCube_Any(INyARRgbRaster i_ref_raster)
	{
		this._ref_raster=i_ref_raster;
	}
	private int[] _wk=new int[3];
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException
	{
		NyARIntSize s=this._ref_raster.getSize();
		this.convertRect(0,0,s.w,s.h,i_raster);
	}
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster o_raster) throws NyARException
	{
		int[] wk=this._wk;
		final int b=t+h;
		final int pix_count=w;
		switch(o_raster.getBufferType()){
		default:
			INyARGsPixelDriver out_drv=o_raster.getGsPixelDriver();
			INyARRgbPixelDriver in_drv=this._ref_raster.getRgbPixelDriver();
			for (int y = t; y < b; y++) {
				for (int x = pix_count-1; x >=0; x--){
					in_drv.getPixel(x,y,wk);
					out_drv.setPixel(x,y,(wk[0]*wk[1]*wk[2])>>16);
				}
			}
			return;
		}
	}
}



//
//RgbCubeのラスタドライバ
//
class NyARRgb2GsFilterYCbCr_Any implements INyARRgb2GsFilterYCbCr
{
	private INyARRgbRaster _ref_raster;
	public NyARRgb2GsFilterYCbCr_Any(INyARRgbRaster i_ref_raster)
	{
		this._ref_raster=i_ref_raster;
	}
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException
	{
		NyARIntSize s=this._ref_raster.getSize();
		this.convertRect(0,0,s.w,s.h,i_raster);
	}	
	private int[] _wk=new int[3];
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster o_raster) throws NyARException
	{
		int[] wk=this._wk;
		final int b=t+h;
		final int pix_count=w;
		switch(o_raster.getBufferType()){
		default:
			INyARGsPixelDriver out_drv=o_raster.getGsPixelDriver();
			INyARRgbPixelDriver in_drv=this._ref_raster.getRgbPixelDriver();
			for (int y = t; y < b; y++) {
				for (int x = pix_count-1; x >=0; x--){
					in_drv.getPixel(x,y,wk);
					out_drv.setPixel(x,y,(306*(wk[2] & 0xff)+601*(wk[1] & 0xff)+117 * (wk[0] & 0xff))>>10);
				}
			}
			return;
		}
	}
}

/*	old cut filters
 		public void doCutFilter(INyARRaster i_input, int l,int t,int i_st,INyARGrayscaleRaster o_output) throws NyARException
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
		
		public void doCutFilter(INyARRaster i_input, int l,int t,int i_st,INyARGrayscaleRaster o_output) throws NyARException
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
 */