/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.rasterreader;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.*;


/**
 * 遠近法を使ったパースペクティブ補正をかけて、ラスタ上の四角形から
 * 任意解像度の矩形パターンを作成します。
 * 
 * 出力ラスタ形式が、INT1D_X8R8G8B8_32の物については、単体サンプリングモードの時は高速化済み。マルチサンプリングモードのときは高速化なし。
 * 入力ラスタ形式がINT1D_X8R8G8B8_32,BYTE1D_B8G8R8_24,BYTE1D_R8G8B8_24については、高速化済み。
 * 他の形式のラスタでは、PixelReaderを介した低速転送で対応します。
 * <p>メモ-
 * この関数は、1倍の時はNyARColorPatt_Perspective,
 * n倍の時はNyARColorPatt_Perspective_O2の関数を元に作ってます。
 * </p>
 *
 */
public class NyARPerspectiveRasterReader
{
	protected NyARPerspectiveParamGenerator _perspective_gen;
	private static final int LOCAL_LT=1;
	protected final double[] __pickFromRaster_cpara=new double[8];
	private IPickupRasterImpl _picker;	
	
	private void initializeInstance(int i_buffer_type)
	{
		//新しいモードに対応したら書いてね。
		switch(i_buffer_type){
//		case NyARBufferType.BYTE1D_B8G8R8X8_32:
//			this._picker=new PPickup_Impl_BYTE1D_B8G8R8X8_32();
//			break;
//		case NyARBufferType.BYTE1D_B8G8R8_24:
//			this._picker=new PPickup_Impl_BYTE1D_B8G8R8_24();
//			break;
//		case NyARBufferType.BYTE1D_R8G8B8_24:
//			this._picker=new PPickup_Impl_BYTE1D_R8G8B8_24();
//			break;
		default:
			this._picker=new PPickup_Impl_AnyRaster();
			//低速インタフェイス警告。必要に応じて、高速取得系を実装してね
//			System.out.println("NyARToolKit Warning:"+this.getClass().getName()+":Low speed interface.");
			break;
		}
		this._perspective_gen=new NyARPerspectiveParamGenerator_O1(LOCAL_LT,LOCAL_LT);
		return;		
	}
	/**
	 * コンストラクタです。このコンストラクタで作成したインスタンスは、入力ラスタタイプに依存しませんが低速です。
	 * 入力画像のラスタの形式が既知の場合は、もう一方のコンストラクタを使用してください。
	 */
	public NyARPerspectiveRasterReader()
	{
		initializeInstance(NyARBufferType.NULL_ALLZERO);
		return;
	}
	/**
	 * コンストラクタです。入力ラスタの形式を制限してインスタンスを作成します。
	 * @param i_edge_percentage
	 * エッジ幅の割合(ARToolKit標準と同じなら、25)
	 * @param i_input_raster_type
	 */
	public NyARPerspectiveRasterReader(int i_input_raster_type)
	{
		//入力制限
		this.initializeInstance(i_input_raster_type);
		return;
	}

	/**
	 * i_in_rasterから4頂点i_vertexsでかこまれた領域の画像を射影変換して、o_outへ格納します。
	 * @param i_in_raster
	 * このラスタの形式は、コンストラクタで制限したものと一致している必要があります。
	 * @param i_vertex
	 * 4頂点を格納した配列です。
	 * @param i_edge_x
	 * X方向のエッジ割合です。0-99の数値を指定します。
	 * @param i_edge_y
	 * Y方向のエッジ割合です。0-99の数値を指定します。
	 * @param i_resolution
	 * 出力の1ピクセルあたりのサンプリング数を指定します。例えば2を指定すると、出力1ピクセルあたり4ピクセルをサンプリングします。
	 * @param o_out
	 * 出力先のラスタです。
	 * @return
	 * @throws NyARException
	 */
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARDoublePoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		NyARIntSize out_size=o_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this._picker.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_in_raster,o_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this._picker.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_in_raster,o_out);
		}
		return true;
	}
	/**
	 * read4Pointの入力型違いです。
	 */	
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARIntPoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		NyARIntSize out_size=o_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this._picker.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_in_raster,o_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this._picker.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_in_raster,o_out);
		}
		return true;
	}
	/**
	 * read4Pointの入力型違いです。
	 */	
	public boolean read4Point(INyARRgbRaster i_in_raster,double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		NyARIntSize out_size=o_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4, this.__pickFromRaster_cpara)) {
				return false;
			}
			this._picker.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_in_raster,o_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4, this.__pickFromRaster_cpara)) {
				return false;
			}
			this._picker.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_in_raster,o_out);
		}
		return true;
	}	
}


//
//ここから先は入力画像毎のラスタドライバ
//

interface IPickupRasterImpl
{
	public void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException;
	public void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException;
}
/*
final class PPickup_Impl_BYTE1D_R8G8B8_24 implements IPickupRasterImpl
{
	public void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
		//出力形式による分岐
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			onePixel_INT1D_X8R8G8B8_32(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,(byte[])i_in_raster.getBuffer(),o_out);
			break;
		default:
			onePixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,(byte[])i_in_raster.getBuffer(),o_out);
			break;
		}
		return;
	}
	public void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
		//出力形式による分岐(分解能が高い時は大した差が出ないから、ANYだけ。)
		multiPixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),i_resolution,cpara,(byte[])i_in_raster.getBuffer(),o_out);
		return;		
	}
	
	private void onePixel_INT1D_X8R8G8B8_32(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		double d0,m0;
		int x,y;
		int bp;
		int[] pat_data=(int[])o_out.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];

		//ピクセルリーダーを取得
		int p=0;
		final int o_w=o_out.getWidth();
		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=o_out.getHeight()-1;iy>=0;iy--){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ


			for(int ix=o_w-1;ix>=0;ix--){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=in_w||y<0||y>=in_h){
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}			
				}
				bp = (x + y * in_w) * 3;
				pat_data[p]=((i_in_buf[bp + 0] & 0xff)<<16)|((i_in_buf[bp + 1] & 0xff)<<8)|((i_in_buf[bp + 2] & 0xff));
				p++;
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;
			}
			
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;
			
		}
		return;
	}
	private void onePixel_ANY(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		double d0,m0;
		int x,y;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];

		//ピクセルリーダーを取得
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getHeight();
		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=0;iy<o_h;iy++){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ


			for(int ix=0;ix<o_w;ix++){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=in_w||y<0||y>=in_h){
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}			
				}
				final int bp = (x + y * in_w) * 3;
				out_reader.setPixel(ix,iy,(i_in_buf[bp + 0] & 0xff),(i_in_buf[bp + 1] & 0xff),(i_in_buf[bp + 2] & 0xff));
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;

			}
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;		
		}
		return;
	}
	private void multiPixel_ANY(int pk_l,int pk_t,int in_w,int in_h,int i_resolution,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];

		int cy=pk_t;
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getWidth();
		for(int iy=0;iy<o_h;iy++){
			//解像度分の点を取る。
			int cx=pk_l;
			for(int ix=0;ix<o_w;ix++){
				int r,g,b;
				r=g=b=0;
				double xss=cp1*cy+cp2;
				double yss=cp4*cy+cp5;
				double dss=cp7*cy+1.0;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double xxx=xss+cp0*cx;
					double yyy=yss+cp3*cx;
					double d=dss+cp6*cx;
					xss+=cp1;
					yss+=cp4;
					dss+=cp7;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						int x=(int)(xxx/d);
						int y=(int)(yyy/d);
						xxx+=cp0;
						yyy+=cp3;
						d+=cp6;
						if(x<0||x>=in_w||y<0||y>=in_h){
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						}
						final int bp = (x + y * in_w) * 3;
						r+=(i_in_buf[bp + 0] & 0xff);
						g+=(i_in_buf[bp + 1] & 0xff);
						b+=(i_in_buf[bp + 2] & 0xff);
					}
				}
				cx+=i_resolution;
				out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
			}
			cy+=i_resolution;
		}
		return;
	}	
}






final class PPickup_Impl_BYTE1D_B8G8R8_24 implements IPickupRasterImpl
{
	public void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24));
		//出力形式による分岐
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			onePixel_INT1D_X8R8G8B8_32(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,(byte[])i_in_raster.getBuffer(),o_out);
			break;
		default:
			onePixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,(byte[])i_in_raster.getBuffer(),o_out);
			break;
		}
		return;
	}
	public void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24));
		//出力形式による分岐(分解能が高い時は大した差が出ないから、ANYだけ。)
		multiPixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),i_resolution,cpara,(byte[])i_in_raster.getBuffer(),o_out);
		return;		
	}
	
	private void onePixel_INT1D_X8R8G8B8_32(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		double d0,m0;
		int x,y;
		int bp;
		int[] pat_data=(int[])o_out.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];

		//ピクセルリーダーを取得
		int p=0;
		final int o_w=o_out.getWidth();
		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=o_out.getHeight()-1;iy>=0;iy--){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ


			for(int ix=o_w-1;ix>=0;ix--){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=in_w||y<0||y>=in_h){
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}			
				}
				bp = (x + y * in_w) * 3;
				pat_data[p]=((i_in_buf[bp + 2] & 0xff)<<16)|((i_in_buf[bp + 1] & 0xff)<<8)|((i_in_buf[bp + 0] & 0xff));
				p++;
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;
			}
			
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;
			
		}
		return;
	}
	private void onePixel_ANY(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		double d0,m0;
		int x,y;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];

		//ピクセルリーダーを取得
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getHeight();
		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=0;iy<o_h;iy++){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ


			for(int ix=0;ix<o_w;ix++){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=in_w||y<0||y>=in_h){
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}			
				}
				final int bp = (x + y * in_w) * 3;
				out_reader.setPixel(ix,iy,(i_in_buf[bp + 2] & 0xff),(i_in_buf[bp + 1] & 0xff),(i_in_buf[bp + 0] & 0xff));
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;

			}
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;		
		}
		return;
	}
	private void multiPixel_ANY(int pk_l,int pk_t,int in_w,int in_h,int i_resolution,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];

		int cy=pk_t;
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getWidth();
		for(int iy=0;iy<o_h;iy++){
			//解像度分の点を取る。
			int cx=pk_l;
			for(int ix=0;ix<o_w;ix++){
				int r,g,b;
				r=g=b=0;
				double xss=cp1*cy+cp2;
				double yss=cp4*cy+cp5;
				double dss=cp7*cy+1.0;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double xxx=xss+cp0*cx;
					double yyy=yss+cp3*cx;
					double d=dss+cp6*cx;
					xss+=cp1;
					yss+=cp4;
					dss+=cp7;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						int x=(int)(xxx/d);
						int y=(int)(yyy/d);
						xxx+=cp0;
						yyy+=cp3;
						d+=cp6;
						if(x<0||x>=in_w||y<0||y>=in_h){
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						}
						final int bp = (x + y * in_w) * 3;
						r+=(i_in_buf[bp + 2] & 0xff);
						g+=(i_in_buf[bp + 1] & 0xff);
						b+=(i_in_buf[bp + 0] & 0xff);
					}
				}
				cx+=i_resolution;
				out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
			}
			cy+=i_resolution;
		}
		return;
	}	
}




final class PPickup_Impl_BYTE1D_B8G8R8X8_32 implements IPickupRasterImpl
{
	public void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
		//出力形式による分岐
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			onePixel_INT1D_X8R8G8B8_32(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,(byte[])i_in_raster.getBuffer(),o_out);
			break;
		default:
			onePixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,(byte[])i_in_raster.getBuffer(),o_out);
			break;
		}
		return;
	}
	public void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
		//出力形式による分岐(分解能が高い時は大した差が出ないから、ANYだけ。)
		multiPixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),i_resolution,cpara,(byte[])i_in_raster.getBuffer(),o_out);
		return;		
	}
	
	private void onePixel_INT1D_X8R8G8B8_32(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		double d0,m0;
		int x,y;
		int bp;
		int[] pat_data=(int[])o_out.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];

		//ピクセルリーダーを取得
		int p=0;
		final int o_w=o_out.getWidth();
		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=o_out.getHeight()-1;iy>=0;iy--){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ


			for(int ix=o_w-1;ix>=0;ix--){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=in_w||y<0||y>=in_h){
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}			
				}
				bp = (x + y * in_w) * 4;
				pat_data[p]=((i_in_buf[bp + 2] & 0xff)<<16)|((i_in_buf[bp + 1] & 0xff)<<8)|((i_in_buf[bp + 0] & 0xff));
				p++;
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;
			}
			
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;
			
		}
		return;
	}
	private void onePixel_ANY(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		double d0,m0;
		int x,y;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];

		//ピクセルリーダーを取得
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getHeight();
		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=0;iy<o_h;iy++){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ


			for(int ix=0;ix<o_w;ix++){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=in_w||y<0||y>=in_h){
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}			
				}
				final int bp = (x + y * in_w) * 4;
				out_reader.setPixel(ix,iy,(i_in_buf[bp + 2] & 0xff),(i_in_buf[bp + 1] & 0xff),(i_in_buf[bp + 0] & 0xff));
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;

			}
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;		
		}
		return;
	}

	private void multiPixel_ANY(int pk_l,int pk_t,int in_w,int in_h,int i_resolution,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];

		int p=0;
		int cy=pk_t;
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getWidth();
		for(int iy=0;iy<o_h;iy++){
			//解像度分の点を取る。
			int cx=pk_l;
			for(int ix=0;ix<o_w;ix++){
				int r,g,b;
				r=g=b=0;
				double xss=cp1*cy+cp2;
				double yss=cp4*cy+cp5;
				double dss=cp7*cy+1.0;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double xxx=xss+cp0*cx;
					double yyy=yss+cp3*cx;
					double d=dss+cp6*cx;
					xss+=cp1;
					yss+=cp4;
					dss+=cp7;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						int x=(int)(xxx/d);
						int y=(int)(yyy/d);
						xxx+=cp0;
						yyy+=cp3;
						d+=cp6;
						if(x<0||x>=in_w||y<0||y>=in_h){
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						}
						final int bp = (x + y * in_w) * 4;
						r+=(i_in_buf[bp + 2] & 0xff);
						g+=(i_in_buf[bp + 1] & 0xff);
						b+=(i_in_buf[bp + 0] & 0xff);
					}
				}
				cx+=i_resolution;
				out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
				p++;
			}
			cy+=i_resolution;
		}
		return;
	}	
}
*/
/**
 * 全種類のNyARRasterを入力できるクラス
 */
final class PPickup_Impl_AnyRaster implements IPickupRasterImpl
{
	private final int[] __pickFromRaster_rgb_tmp = new int[3];
	public void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		//出力形式による分岐
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			onePixel_INT1D_X8R8G8B8_32(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,i_in_raster.getRgbPixelReader(),o_out);
			break;
		default:
			onePixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),cpara,i_in_raster.getRgbPixelReader(),o_out);
			break;
		}
		return;
	}
	public void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		//出力形式による分岐
		switch(o_out.getBufferType())
		{
		case NyARBufferType.INT1D_X8R8G8B8_32:
			multiPixel_INT1D_X8R8G8B8_32(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),i_resolution,cpara,i_in_raster.getRgbPixelReader(),o_out);
			break;
		default:
			multiPixel_ANY(pk_l,pk_t,i_in_raster.getWidth(),i_in_raster.getHeight(),i_resolution,cpara,i_in_raster.getRgbPixelReader(),o_out);
			break;
		}
		return;		
	}
	
	private void onePixel_INT1D_X8R8G8B8_32(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,INyARRgbPixelReader i_in_reader,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		double d0,m0;
		int x,y;
		int[] pat_data=(int[])o_out.getBuffer();
		
		final int img_x = in_w;
		final int img_y = in_h;

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;


		

		final double cp0=cpara[0];
		final double cp3=cpara[3];
		final double cp6=cpara[6];
		final double cp1=cpara[1];
		final double cp4=cpara[4];
		final double cp7=cpara[7];
		
		//ピクセルリーダーを取得
		int p=0;

		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=o_out.getHeight()-1;iy>=0;iy--){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ

			for(int ix=o_out.getWidth()-1;ix>=0;ix--){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=img_x||y<0||y>=img_y){
					if(x<0){x=0;}else if(x>=img_x){x=img_x-1;}
					if(y<0){y=0;}else if(y>=img_y){y=img_y-1;}			
				}
				i_in_reader.getPixel(x, y,rgb_tmp);
				pat_data[p]=(rgb_tmp[0]<<16)|(rgb_tmp[1]<<8)|((rgb_tmp[2]&0xff));
				p++;
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;
			}
			
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;
			}

		return;
	}
	private void onePixel_ANY(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,INyARRgbPixelReader i_in_reader,INyARRgbRaster o_out)throws NyARException
	{
		double d0,m0;
		int x,y;

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];

		//ピクセルリーダーを取得
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getHeight();
		
		double cp0cx0,cp3cx0;
		double cp1cy_cp20=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4cy_cp50=cp4*pk_t+cpara[5]+cp3*pk_l;
		double cp7cy_10=cp7*pk_t+1.0+cp6*pk_l;
		

		for(int iy=0;iy<o_h;iy++){
			m0=1/(cp7cy_10);
			d0=-cp6/(cp7cy_10*(cp7cy_10+cp6));			

			cp0cx0=cp1cy_cp20;
			cp3cx0=cp4cy_cp50;
			
			//ピックアップシーケンス
			
			//0番目のピクセル(検査対象)をピックアップ


			for(int ix=0;ix<o_w;ix++){
				//1ピクセルを作成
				x=(int)(cp0cx0*m0);
				y=(int)(cp3cx0*m0);
				if(x<0||x>=in_w||y<0||y>=in_h){
					if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
					if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}			
				}
				i_in_reader.getPixel(x, y, rgb_tmp);
				out_reader.setPixel(ix,iy,rgb_tmp);
				cp0cx0+=cp0;
				cp3cx0+=cp3;
				m0+=d0;

			}
			
			cp1cy_cp20+=cp1;
			cp4cy_cp50+=cp4;
			cp7cy_10+=cp7;
			
		}
		return;
	}

	private void multiPixel_INT1D_X8R8G8B8_32(int pk_l,int pk_t,int in_w,int in_h,int i_resolution,double[] cpara,INyARRgbPixelReader i_in_reader,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		final int res_pix=i_resolution*i_resolution;

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		int[] pat_data=(int[])o_out.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
	
		int p=0;
		int cy=pk_t;
		final int o_w=o_out.getWidth();
		for(int iy=o_out.getHeight()-1;iy>=0;iy--){
			//解像度分の点を取る。
			int cx=pk_l;
			for(int ix=o_w-1;ix>=0;ix--){
				int r,g,b;
				r=g=b=0;
				double xss=cp1*cy+cp2;
				double yss=cp4*cy+cp5;
				double dss=cp7*cy+1.0;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double xxx=xss+cp0*cx;
					double yyy=yss+cp3*cx;
					double d=dss+cp6*cx;
					xss+=cp1;
					yss+=cp4;
					dss+=cp7;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						int x=(int)(xxx/d);
						int y=(int)(yyy/d);
						xxx+=cp0;
						yyy+=cp3;
						d+=cp6;
						if(x<0||x>=in_w||y<0||y>=in_h){
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						}
						i_in_reader.getPixel(x, y, rgb_tmp);
						r+=rgb_tmp[0];
						g+=rgb_tmp[1];
						b+=rgb_tmp[2];
					}
				}
				cx+=i_resolution;
				pat_data[p]=(((r/res_pix)&0xff)<<16)|(((g/res_pix)&0xff)<<8)|(((b/res_pix)&0xff));
				p++;
			}
			cy+=i_resolution;
		}
		return;
	}
	private void multiPixel_ANY(int pk_l,int pk_t,int in_w,int in_h,int i_resolution,double[] cpara,INyARRgbPixelReader i_in_reader,INyARRgbRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;
		INyARRgbPixelReader out_reader=o_out.getRgbPixelReader();

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];

		int cy=pk_t;
		final int o_w=o_out.getWidth();
		final int o_h=o_out.getWidth();
		for(int iy=0;iy<o_h;iy++){
			//解像度分の点を取る。
			int cx=pk_l;
			for(int ix=0;ix<o_w;ix++){
				int r,g,b;
				r=g=b=0;
				double xss=cp1*cy+cp2;
				double yss=cp4*cy+cp5;
				double dss=cp7*cy+1.0;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double xxx=xss+cp0*cx;
					double yyy=yss+cp3*cx;
					double d=dss+cp6*cx;
					xss+=cp1;
					yss+=cp4;
					dss+=cp7;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						int x=(int)(xxx/d);
						int y=(int)(yyy/d);
						xxx+=cp0;
						yyy+=cp3;
						d+=cp6;
						if(x<0||x>=in_w||y<0||y>=in_h){
							if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
							if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						}
						i_in_reader.getPixel(x, y, rgb_tmp);
						r+=rgb_tmp[0];
						g+=rgb_tmp[1];
						b+=rgb_tmp[2];
					}
				}
				cx+=i_resolution;
				rgb_tmp[0]=r/res_pix;
				rgb_tmp[1]=g/res_pix;
				rgb_tmp[2]=b/res_pix;
				out_reader.setPixel(ix,iy,rgb_tmp);
			}
			cy+=i_resolution;
		}
		return;
	}	
}


