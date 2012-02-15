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
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.NyARPerspectiveParamGenerator;
import jp.nyatla.nyartoolkit.core.utils.NyARPerspectiveParamGenerator_O1;

public class NyARPerspectiveCopyFactory
{
	/**
	 * 指定したIN/OUTに最適な{@link INyARPerspectiveReaader}を生成します。
	 * <p>入力ラスタについて
	 * 基本的には全ての{@link INyARRgbRaster}を実装したクラスを処理できますが、次の３種類のバッファを持つものを推奨します。
	 * <ul>
	 * <li>{@link NyARBufferType#BYTE1D_B8G8R8X8_32}
	 * <li>{@link NyARBufferType#BYTE1D_B8G8R8_24}
	 * <li>{@link NyARBufferType#BYTE1D_R8G8B8_24}
	 * </ul>
	 * </p>
	 * <p>出力ラスタについて
	 * 基本的には全ての{@link NyARBufferType#INT1D_X8R8G8B8_32}形式のバッファを持つラスタを使用してください。
	 * 他の形式でも動作しますが、低速な場合があります。
	 * </p>
	 * <p>高速化について - 
	 * 入力ラスタ形式が、{@link NyARBufferType#BYTE1D_B8G8R8X8_32},{@link NyARBufferType#BYTE1D_B8G8R8_24}
	 * ,{@link NyARBufferType#BYTE1D_R8G8B8_24}のものについては、他の形式よりも高速に動作します。
	 * また、出力ラスタ形式が、{@link NyARBufferType#INT1D_X8R8G8B8_32}の物については、単体サンプリングモードの時のみ、さらに高速に動作します。
	 * 他の形式のラスタでは、以上のものよりも低速転送で対応します。
	 * @param i_in_raster_type
	 * 入力ラスタの形式です。
	 * @param i_out_raster_type
	 * 出力ラスタの形式です。
	 * @return
	 */
	public static INyARPerspectiveCopy createDriver(INyARRgbRaster i_raster) throws NyARException
	{
		//新しいモードに対応したら書いてね。
		switch(i_raster.getBufferType()){
		case NyARBufferType.BYTE1D_B8G8R8X8_32:
			return new PerspectiveCopy_BYTE1D_B8G8R8X8_32(i_raster);
		case NyARBufferType.BYTE1D_B8G8R8_24:
			return new PerspectiveCopy_BYTE1D_B8G8R8_24(i_raster);
		case NyARBufferType.BYTE1D_R8G8B8_24:
			return new PerspectiveCopy_BYTE1D_R8G8B8_24(i_raster);
		default:
			return new PerspectiveCopy_ANYRgb(i_raster);
		}
	}
}

//
//ラスタドライバ
//



abstract class PerspectiveCopy_Base implements INyARPerspectiveCopy
{
	private static final int LOCAL_LT=1;
	protected NyARPerspectiveParamGenerator _perspective_gen;
	protected final double[] __pickFromRaster_cpara=new double[8];	
	protected PerspectiveCopy_Base()
	{
		this._perspective_gen=new NyARPerspectiveParamGenerator_O1(LOCAL_LT,LOCAL_LT);		
	}
	public boolean copyPatt(double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException
	{
		NyARIntSize out_size=i_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4,this.__pickFromRaster_cpara)) {
				return false;
			}
			this.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_out);
		}
		return true;
	}

	public boolean copyPatt(NyARDoublePoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException
	{
		return this.copyPatt(i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, i_edge_x, i_edge_y, i_resolution, i_out);
	}
	public boolean copyPatt(NyARIntPoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException
	{
		return this.copyPatt(i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, i_edge_x, i_edge_y, i_resolution, i_out);
	}
	protected abstract boolean onePixel(int pk_l,int pk_t,double[] cpara,INyARRaster o_out)throws NyARException;
	protected abstract boolean multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRaster o_out)throws NyARException;

}

/**
 * RGBインタフェイスを持つラスタをソースにしたフィルタ
 */
class PerspectiveCopy_ANYRgb extends PerspectiveCopy_Base
{
	protected INyARRgbRaster _ref_raster;
	private final int[] __pickFromRaster_rgb_tmp = new int[3];
	public PerspectiveCopy_ANYRgb(INyARRaster i_ref_raster)
	{
		this._ref_raster=(INyARRgbRaster)i_ref_raster;
	}
	protected boolean onePixel(int pk_l,int pk_t,double[] cpara,INyARRaster o_out)throws NyARException
	{
		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		
		INyARRgbPixelDriver i_in_reader=this._ref_raster.getRgbPixelDriver();
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			int[] pat_data=(int[])o_out.getBuffer();			
			int p=0;
			for(int iy=out_h-1;iy>=0;iy--){
				//解像度分の点を取る。
				double cp7_cy_1_cp6_cx  =cp7_cy_1;
				double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
				double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
				
				for(int ix=out_w-1;ix>=0;ix--){
					//1ピクセルを作成
					final double d=1/(cp7_cy_1_cp6_cx);
					int x=(int)((cp1_cy_cp2_cp0_cx)*d);
					int y=(int)((cp4_cy_cp5_cp3_cx)*d);
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
					i_in_reader.getPixel(x, y, rgb_tmp);
					cp7_cy_1_cp6_cx+=cp6;
					cp1_cy_cp2_cp0_cx+=cp0;
					cp4_cy_cp5_cp3_cx+=cp3;

					pat_data[p]=(rgb_tmp[0]<<16)|(rgb_tmp[1]<<8)|((rgb_tmp[2]&0xff));
					p++;
				}
				cp7_cy_1+=cp7;
				cp1_cy_cp2+=cp1;
				cp4_cy_cp5+=cp4;
			}
			return true;
		default:
			//ANY to RGBx
			if(o_out instanceof INyARRgbRaster){
				INyARRgbPixelDriver out_reader=((INyARRgbRaster)o_out).getRgbPixelDriver();	
				for(int iy=0;iy<out_h;iy++){
					//解像度分の点を取る。
					double cp7_cy_1_cp6_cx  =cp7_cy_1;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
					for(int ix=0;ix<out_w;ix++){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
								
						i_in_reader.getPixel(x, y, rgb_tmp);
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
		
						out_reader.setPixel(ix,iy,rgb_tmp);
					}
					cp7_cy_1+=cp7;
					cp1_cy_cp2+=cp1;
					cp4_cy_cp5+=cp4;
				}
				return true;
			}
			break;
		}
		return false;
	}
	protected boolean multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		int[] pat_data=(int[])o_out.getBuffer();
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();
		INyARRgbPixelDriver i_in_reader=this._ref_raster.getRgbPixelDriver();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			int p=(out_w*out_h-1);
			for(int iy=out_h-1;iy>=0;iy--){
				//解像度分の点を取る。
				for(int ix=out_w-1;ix>=0;ix--){
					int r,g,b;
					r=g=b=0;
					int cy=pk_t+iy*i_resolution;
					int cx=pk_l+ix*i_resolution;
					double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
					double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
					double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
					for(int i2y=i_resolution-1;i2y>=0;i2y--){
						double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
						double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
						double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
						for(int i2x=i_resolution-1;i2x>=0;i2x--){
							//1ピクセルを作成
							final double d=1/(cp7_cy_1_cp6_cx);
							int x=(int)((cp1_cy_cp2_cp0_cx)*d);
							int y=(int)((cp4_cy_cp5_cp3_cx)*d);
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
							i_in_reader.getPixel(x, y, rgb_tmp);
							r+=rgb_tmp[0];
							g+=rgb_tmp[1];
							b+=rgb_tmp[2];
							cp7_cy_1_cp6_cx+=cp6;
							cp1_cy_cp2_cp0_cx+=cp0;
							cp4_cy_cp5_cp3_cx+=cp3;
						}
						cp7_cy_1_cp6_cx_b+=cp7;
						cp1_cy_cp2_cp0_cx_b+=cp1;
						cp4_cy_cp5_cp3_cx_b+=cp4;
					}
					r/=res_pix;
					g/=res_pix;
					b/=res_pix;
					pat_data[p]=((r&0xff)<<16)|((g&0xff)<<8)|((b&0xff));
					p--;
				}
			}
			return true;
		default:
			//ANY to RGBx
			if(o_out instanceof INyARRgbRaster){
				INyARRgbPixelDriver out_reader=((INyARRgbRaster)o_out).getRgbPixelDriver();
				for(int iy=out_h-1;iy>=0;iy--){
					//解像度分の点を取る。
					for(int ix=out_w-1;ix>=0;ix--){
						int r,g,b;
						r=g=b=0;
						int cy=pk_t+iy*i_resolution;
						int cx=pk_l+ix*i_resolution;
						double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
						double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
						double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
						for(int i2y=i_resolution-1;i2y>=0;i2y--){
							double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
							double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
							double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
							for(int i2x=i_resolution-1;i2x>=0;i2x--){
								//1ピクセルを作成
								final double d=1/(cp7_cy_1_cp6_cx);
								int x=(int)((cp1_cy_cp2_cp0_cx)*d);
								int y=(int)((cp4_cy_cp5_cp3_cx)*d);
								if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
								if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
								
								i_in_reader.getPixel(x, y, rgb_tmp);
								r+=rgb_tmp[0];
								g+=rgb_tmp[1];
								b+=rgb_tmp[2];
								cp7_cy_1_cp6_cx+=cp6;
								cp1_cy_cp2_cp0_cx+=cp0;
								cp4_cy_cp5_cp3_cx+=cp3;
							}
							cp7_cy_1_cp6_cx_b+=cp7;
							cp1_cy_cp2_cp0_cx_b+=cp1;
							cp4_cy_cp5_cp3_cx_b+=cp4;
						}
						out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
					}
				}
				return true;
			}
			break;
		}
		return false;
	}

}
class PerspectiveCopy_BYTE1D_B8G8R8X8_32 extends PerspectiveCopy_Base
{
	protected INyARRgbRaster _ref_raster;
	public PerspectiveCopy_BYTE1D_B8G8R8X8_32(INyARRaster i_ref_raster)
	{
		this._ref_raster=(INyARRgbRaster)i_ref_raster;
	}
	protected boolean onePixel(int pk_l,int pk_t,double[] cpara,INyARRaster o_out)throws NyARException
	{
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();
		byte[] i_in_buf=(byte[])this._ref_raster.getBuffer();

		int[] pat_data=(int[])o_out.getBuffer();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b,p;
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			p=0;
			for(int iy=0;iy<out_h;iy++){
				//解像度分の点を取る。
				double cp7_cy_1_cp6_cx  =cp7_cy_1;
				double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
				double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
				
				for(int ix=0;ix<out_w;ix++){
					//1ピクセルを作成
					final double d=1/(cp7_cy_1_cp6_cx);
					int x=(int)((cp1_cy_cp2_cp0_cx)*d);
					int y=(int)((cp4_cy_cp5_cp3_cx)*d);
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
					final int bp = (x + y * in_w) * 4;
					r=(i_in_buf[bp + 2] & 0xff);
					g=(i_in_buf[bp + 1] & 0xff);
					b=(i_in_buf[bp + 0] & 0xff);
					cp7_cy_1_cp6_cx+=cp6;
					cp1_cy_cp2_cp0_cx+=cp0;
					cp4_cy_cp5_cp3_cx+=cp3;
					pat_data[p]=(r<<16)|(g<<8)|((b&0xff));
					p++;
				}
				cp7_cy_1+=cp7;
				cp1_cy_cp2+=cp1;
				cp4_cy_cp5+=cp4;
			}
			return true;
		default:
			//ANY to RGBx
			if(o_out instanceof INyARRgbRaster){
				INyARRgbPixelDriver out_reader=((INyARRgbRaster)o_out).getRgbPixelDriver();
				for(int iy=0;iy<out_h;iy++){
					//解像度分の点を取る。
					double cp7_cy_1_cp6_cx  =cp7_cy_1;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
					
					for(int ix=0;ix<out_w;ix++){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
								
						final int bp = (x + y * in_w) * 4;
						r=(i_in_buf[bp + 2] & 0xff);
						g=(i_in_buf[bp + 1] & 0xff);
						b=(i_in_buf[bp + 0] & 0xff);
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
						out_reader.setPixel(ix,iy,r,g,b);
					}
					cp7_cy_1+=cp7;
					cp1_cy_cp2+=cp1;
					cp4_cy_cp5+=cp4;
				}
				return true;
			}
			break;
		}
		return false;
	}
	protected boolean multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRaster o_out)throws NyARException
	{
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();
		byte[] i_in_buf=(byte[])this._ref_raster.getBuffer();
		final int res_pix=i_resolution*i_resolution;

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		if(o_out instanceof INyARRgbRaster){
			INyARRgbPixelDriver out_reader=((INyARRgbRaster)o_out).getRgbPixelDriver();
			for(int iy=out_h-1;iy>=0;iy--){
				//解像度分の点を取る。
				for(int ix=out_w-1;ix>=0;ix--){
					int r,g,b;
					r=g=b=0;
					int cy=pk_t+iy*i_resolution;
					int cx=pk_l+ix*i_resolution;
					double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
					double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
					double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
					for(int i2y=i_resolution-1;i2y>=0;i2y--){
						double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
						double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
						double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
						for(int i2x=i_resolution-1;i2x>=0;i2x--){
							//1ピクセルを作成
							final double d=1/(cp7_cy_1_cp6_cx);
							int x=(int)((cp1_cy_cp2_cp0_cx)*d);
							int y=(int)((cp4_cy_cp5_cp3_cx)*d);
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
							final int bp = (x + y * in_w) * 4;
							r+=(i_in_buf[bp + 2] & 0xff);
							g+=(i_in_buf[bp + 1] & 0xff);
							b+=(i_in_buf[bp + 0] & 0xff);
							cp7_cy_1_cp6_cx+=cp6;
							cp1_cy_cp2_cp0_cx+=cp0;
							cp4_cy_cp5_cp3_cx+=cp3;
						}
						cp7_cy_1_cp6_cx_b+=cp7;
						cp1_cy_cp2_cp0_cx_b+=cp1;
						cp4_cy_cp5_cp3_cx_b+=cp4;
					}
					out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
				}
			}
			return true;
		}
		return false;
	}	
}

class PerspectiveCopy_BYTE1D_B8G8R8_24 extends PerspectiveCopy_Base
{
	protected INyARRgbRaster _ref_raster;
	public PerspectiveCopy_BYTE1D_B8G8R8_24(INyARRaster i_ref_raster)
	{
		this._ref_raster=(INyARRgbRaster)i_ref_raster;
	}
	protected boolean onePixel(int pk_l,int pk_t,double[] cpara,INyARRaster o_out)throws NyARException
	{
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();
		byte[] i_in_buf=(byte[])this._ref_raster.getBuffer();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		switch(this._ref_raster.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			int[] pat_data=(int[])o_out.getBuffer();
			int p=0;
			for(int iy=0;iy<out_h;iy++){
				//解像度分の点を取る。
				double cp7_cy_1_cp6_cx  =cp7_cy_1;
				double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
				double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
				
				for(int ix=0;ix<out_w;ix++){
					//1ピクセルを作成
					final double d=1/(cp7_cy_1_cp6_cx);
					int x=(int)((cp1_cy_cp2_cp0_cx)*d);
					int y=(int)((cp4_cy_cp5_cp3_cx)*d);
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
					final int bp = (x + y * in_w) * 3;
					r=(i_in_buf[bp + 2] & 0xff);
					g=(i_in_buf[bp + 1] & 0xff);
					b=(i_in_buf[bp + 0] & 0xff);
					cp7_cy_1_cp6_cx+=cp6;
					cp1_cy_cp2_cp0_cx+=cp0;
					cp4_cy_cp5_cp3_cx+=cp3;
					pat_data[p]=(r<<16)|(g<<8)|((b&0xff));
					p++;
				}
				cp7_cy_1+=cp7;
				cp1_cy_cp2+=cp1;
				cp4_cy_cp5+=cp4;
			}
			return true;
		default:
			if(o_out instanceof INyARRgbRaster)
			{
				INyARRgbPixelDriver out_reader=((INyARRgbRaster)o_out).getRgbPixelDriver();	
				for(int iy=0;iy<out_h;iy++){
					//解像度分の点を取る。
					double cp7_cy_1_cp6_cx  =cp7_cy_1;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
					
					for(int ix=0;ix<out_w;ix++){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
								
						final int bp = (x + y * in_w) * 3;
						r=(i_in_buf[bp + 2] & 0xff);
						g=(i_in_buf[bp + 1] & 0xff);
						b=(i_in_buf[bp + 0] & 0xff);
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
	
						out_reader.setPixel(ix,iy,r,g,b);
					}
					cp7_cy_1+=cp7;
					cp1_cy_cp2+=cp1;
					cp4_cy_cp5+=cp4;
				}
				return true;
			}
			break;
		}
		return false;
	}
	protected boolean multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();
		byte[] i_in_buf=(byte[])this._ref_raster.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		if(o_out instanceof INyARRgbRaster)
		{
			INyARRgbPixelDriver out_reader=((INyARRgbRaster) o_out).getRgbPixelDriver();
			for(int iy=out_h-1;iy>=0;iy--){
				//解像度分の点を取る。
				for(int ix=out_w-1;ix>=0;ix--){
					int r,g,b;
					r=g=b=0;
					int cy=pk_t+iy*i_resolution;
					int cx=pk_l+ix*i_resolution;
					double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
					double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
					double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
					for(int i2y=i_resolution-1;i2y>=0;i2y--){
						double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
						double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
						double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
						for(int i2x=i_resolution-1;i2x>=0;i2x--){
							//1ピクセルを作成
							final double d=1/(cp7_cy_1_cp6_cx);
							int x=(int)((cp1_cy_cp2_cp0_cx)*d);
							int y=(int)((cp4_cy_cp5_cp3_cx)*d);
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
							final int bp = (x + y * in_w) * 3;
							r+=(i_in_buf[bp + 2] & 0xff);
							g+=(i_in_buf[bp + 1] & 0xff);
							b+=(i_in_buf[bp + 0] & 0xff);
							cp7_cy_1_cp6_cx+=cp6;
							cp1_cy_cp2_cp0_cx+=cp0;
							cp4_cy_cp5_cp3_cx+=cp3;
						}
						cp7_cy_1_cp6_cx_b+=cp7;
						cp1_cy_cp2_cp0_cx_b+=cp1;
						cp4_cy_cp5_cp3_cx_b+=cp4;
					}
					out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
				}
			}
			return true;
		}
		return false;
	}
}

class PerspectiveCopy_BYTE1D_R8G8B8_24 extends PerspectiveCopy_Base
{
	protected INyARRgbRaster _ref_raster;
	public PerspectiveCopy_BYTE1D_R8G8B8_24(INyARRaster i_ref_raster)
	{
		this._ref_raster=(INyARRgbRaster)i_ref_raster;
	}
	protected boolean onePixel(int pk_l,int pk_t,double[] cpara,INyARRaster o_out)throws NyARException
	{
		byte[] i_in_buf=(byte[])this._ref_raster.getBuffer();
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		switch(this._ref_raster.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			int p=0;
			int[] pat_data=(int[])o_out.getBuffer();
			for(int iy=0;iy<out_h;iy++){
				//解像度分の点を取る。
				double cp7_cy_1_cp6_cx  =cp7_cy_1;
				double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
				double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
				
				for(int ix=0;ix<out_w;ix++){
					//1ピクセルを作成
					final double d=1/(cp7_cy_1_cp6_cx);
					int x=(int)((cp1_cy_cp2_cp0_cx)*d);
					int y=(int)((cp4_cy_cp5_cp3_cx)*d);
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
					final int bp = (x + y * in_w) * 3;
					r=(i_in_buf[bp + 0] & 0xff);
					g=(i_in_buf[bp + 1] & 0xff);
					b=(i_in_buf[bp + 2] & 0xff);
					cp7_cy_1_cp6_cx+=cp6;
					cp1_cy_cp2_cp0_cx+=cp0;
					cp4_cy_cp5_cp3_cx+=cp3;
					pat_data[p]=(r<<16)|(g<<8)|((b&0xff));
					p++;
				}
				cp7_cy_1+=cp7;
				cp1_cy_cp2+=cp1;
				cp4_cy_cp5+=cp4;
			}
			return true;
		default:
			if(o_out instanceof INyARRgbRaster){
				INyARRgbPixelDriver out_reader=((INyARRgbRaster) o_out).getRgbPixelDriver();
				for(int iy=0;iy<out_h;iy++){
					//解像度分の点を取る。
					double cp7_cy_1_cp6_cx  =cp7_cy_1;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
					
					for(int ix=0;ix<out_w;ix++){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
								
						final int bp = (x + y * in_w) * 3;
						r=(i_in_buf[bp + 0] & 0xff);
						g=(i_in_buf[bp + 1] & 0xff);
						b=(i_in_buf[bp + 2] & 0xff);
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
	
						out_reader.setPixel(ix,iy,r,g,b);
					}
					cp7_cy_1+=cp7;
					cp1_cy_cp2+=cp1;
					cp4_cy_cp5+=cp4;
				}
				return true;
			}
			break;
		}
		return false;
	}
	protected boolean multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;

		byte[] i_in_buf=(byte[])this._ref_raster.getBuffer();
		int in_w=this._ref_raster.getWidth();
		int in_h=this._ref_raster.getHeight();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		if(o_out instanceof INyARRgbRaster)
		{
			INyARRgbPixelDriver out_reader=((INyARRgbRaster) o_out).getRgbPixelDriver();
			for(int iy=out_h-1;iy>=0;iy--){
				//解像度分の点を取る。
				for(int ix=out_w-1;ix>=0;ix--){
					int r,g,b;
					r=g=b=0;
					int cy=pk_t+iy*i_resolution;
					int cx=pk_l+ix*i_resolution;
					double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
					double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
					double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
					for(int i2y=i_resolution-1;i2y>=0;i2y--){
						double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
						double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
						double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
						for(int i2x=i_resolution-1;i2x>=0;i2x--){
							//1ピクセルを作成
							final double d=1/(cp7_cy_1_cp6_cx);
							int x=(int)((cp1_cy_cp2_cp0_cx)*d);
							int y=(int)((cp4_cy_cp5_cp3_cx)*d);
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
							
							final int bp = (x + y * in_w) * 3;
							r+=(i_in_buf[bp + 0] & 0xff);
							g+=(i_in_buf[bp + 1] & 0xff);
							b+=(i_in_buf[bp + 2] & 0xff);
							cp7_cy_1_cp6_cx+=cp6;
							cp1_cy_cp2_cp0_cx+=cp0;
							cp4_cy_cp5_cp3_cx+=cp3;
						}
						cp7_cy_1_cp6_cx_b+=cp7;
						cp1_cy_cp2_cp0_cx_b+=cp1;
						cp4_cy_cp5_cp3_cx_b+=cp4;
					}
					out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
				}
			}
			return true;
		}
		return false;
	}
}


