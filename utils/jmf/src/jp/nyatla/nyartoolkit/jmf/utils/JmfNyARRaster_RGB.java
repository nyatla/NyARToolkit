/* 
 * PROJECT: NyARToolkit JMF utilities.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nyartoolkit.jmf.utils;

import javax.media.format.*;
import javax.media.*;
import java.awt.Dimension;
import com.sun.media.codec.video.colorspace.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.param.*;




/**
 * 
 * RGB形式のJMFバッファをラップするNyARRasterです。
 * JMFから得たラスタデータのピクセル並び順を考慮します。
 *
 */
public class JmfNyARRaster_RGB implements INyARRgbRaster
{
	private JmfRGB24RasterHolder _holder;
	protected NyARIntSize _size;

	public JmfNyARRaster_RGB(NyARParam i_param,VideoFormat i_fmt) throws NyARException
	{
		initMember(i_param.getScreenSize().w,i_param.getScreenSize().h,i_fmt);
	}	
	/**
	 * i_fmtに一致する画素フォーマットのRasterを作成します。
	 * このコンストラクタで作成したクラスは、hasBuffer()がfalseを返すことがあります。
	 * @param i_width
	 * @param i_height
	 * @param i_fmt
	 * @throws NyARException
	 */
	public JmfNyARRaster_RGB(int i_width,int i_height,VideoFormat i_fmt) throws NyARException
	{
		initMember(i_width,i_height,i_fmt);
	}
	/**
	 * i_fmtに一致する画素フォーマットのRasterを作成します。
	 * このコンストラクタで作成したクラスは、hasBuffer()がfalseを返すことがあります。
	 * @param i_size
	 * @param i_fmt
	 * @throws NyARException
	 */
	public JmfNyARRaster_RGB(NyARIntSize i_size,VideoFormat i_fmt) throws NyARException
	{
		initMember(i_size.w,i_size.h,i_fmt);
	}
	private void initMember(int i_width,int i_height,VideoFormat i_fmt) throws NyARException
	{
		this._size= new NyARIntSize(i_width,i_height);
		// データサイズの確認
		final Dimension s = i_fmt.getSize();
		if (!this._size.isEqualSize(s.width,s.height)) {
			throw new NyARException();
		}
		// データ配列の確認
		if(i_fmt instanceof YUVFormat){
			//YUVフォーマット
			this._holder=new NyARGLPixelReader_YUV(this._size,(YUVFormat)i_fmt);			
		}else if(i_fmt instanceof RGBFormat){
			//RGBフォーマット
			this._holder=new NyARGLPixelReader_RGB24(this._size,(RGBFormat)i_fmt);
		}else{
			throw new NyARException();
		}		
	}
	public void setBuffer(javax.media.Buffer i_buffer) throws NyARException
	{
		this._holder.setMediaBuffer(i_buffer);
		return;
	}
	final public int getWidth()
	{
		return this._size.w;
	}

	final public int getHeight()
	{
		return this._size.h;
	}

	final public NyARIntSize getSize()
	{
		return this._size;
	}
	final public int getBufferType()
	{
		return this._holder.buffer_type;
	}
	final public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._holder;
	}
	/**
	 * インスタンスがバッファを所有するかを返します。
	 * @return
	 */	
	final public boolean hasBuffer()
	{
		return this._holder.buffer!=null;
	}
	final public Object getBuffer()
	{
		assert(this._holder.buffer!=null);
		return this._holder.buffer;
	}
	final public boolean isEqualBufferType(int i_type_value)
	{
		return this._holder.buffer_type==i_type_value;
	}
	final public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		NyARException.notImplement();
	}
	/**
	 *	@deprecated hasBuffer()関数を使ってください。
	 * 
	 */
	final public boolean hasData()
	{
		return this.hasBuffer();
	}

}


/**
 * JMFと汎用バッファを中継する拡張INyARRgbPixelReader
 * @author nyatla
 *
 */
abstract class JmfRGB24RasterHolder implements INyARRgbPixelReader
{
	public int buffer_type;
	public byte[] buffer;
	public abstract void setMediaBuffer(javax.media.Buffer i_buffer);
}

/**
 * RGB24フォーマットのデータを扱うピクセルリーダ
 */
class NyARGLPixelReader_RGB24 extends JmfRGB24RasterHolder
{
	protected boolean _is_flipped;
	private NyARIntSize _ref_size;
	public NyARGLPixelReader_RGB24(NyARIntSize i_ref_size,RGBFormat i_input_format) throws NyARException
	{
		this._ref_size=i_ref_size;
		//ピクセルフォーマットを設定(現状は24bitRGBを受けつける。)
		RGBFormat fm=(RGBFormat)i_input_format;
		if(fm.getBitsPerPixel()!=24){
			throw new NyARException();
		}
		int r=fm.getRedMask();
		int b=fm.getBlueMask();
		if(r==1 && b==3){
			this.buffer_type=NyARBufferType.BYTE1D_R8G8B8_24;
		}else if(r==3 && b==1){
			this.buffer_type=NyARBufferType.BYTE1D_B8G8R8_24;			
		}else{
			throw new NyARException();
		}
		//vertical反転の有無を確認
		this._is_flipped=i_input_format.getFlipped()!=0?true:false;
		this.buffer=new byte[i_ref_size.w*i_ref_size.h*3];
		//RGBフォーマット
		
		return;
	}
	public void setMediaBuffer(javax.media.Buffer i_buffer)
	{
		//vertical反転が必要ならば、反転した画像を作成する。
		byte[] src=(byte[])i_buffer.getData();
		if(this._is_flipped){
			final int length = this._ref_size.w * 3;
			int src_idx = 0;
			int dest_idx = (this._ref_size.h - 1) * length;			
			for (int i = 0; i < this._ref_size.h; i++) {
				System.arraycopy(src,src_idx, this.buffer, dest_idx, length);
				src_idx += length;
				dest_idx -= length;
			}
		}else{
			System.arraycopy(src,0,this.buffer,0,this.buffer.length);
			this.buffer=(byte[])i_buffer.getData();
		}
		return;
	}
	public void getPixel(int i_x, int i_y, int[] o_rgb) throws NyARException
	{
		int bp = (i_x + i_y * this._ref_size.w) * 3;
		byte[] ref = this.buffer;
		switch(this.buffer_type){
		case NyARBufferType.BYTE1D_R8G8B8_24:
			o_rgb[0] = (ref[bp + 0] & 0xff);// R
			o_rgb[1] = (ref[bp + 1] & 0xff);// G
			o_rgb[2] = (ref[bp + 2] & 0xff);// B
			break;
		case NyARBufferType.BYTE1D_B8G8R8_24:
			o_rgb[0] = (ref[bp + 2] & 0xff);// B
			o_rgb[1] = (ref[bp + 1] & 0xff);// G
			o_rgb[2] = (ref[bp + 0] & 0xff);// R
			break;
		default:
			throw new NyARException();
		}
		return;
	}
	public void getPixelSet(int[] i_x, int i_y[], int i_num, int[] o_rgb) throws NyARException
	{
		int width = this._ref_size.w;
		byte[] ref = this.buffer;
		int bp;
		switch(this.buffer_type){
		case NyARBufferType.BYTE1D_R8G8B8_24:
			for (int i = i_num - 1; i >= 0; i--) {
				bp = (i_x[i] + i_y[i] * width) * 3;
				o_rgb[i * 3 + 0] = (ref[bp + 0] & 0xff);// R
				o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
				o_rgb[i * 3 + 2] = (ref[bp + 2] & 0xff);// B
			}
			break;
		case NyARBufferType.BYTE1D_B8G8R8_24:
			for (int i = i_num - 1; i >= 0; i--) {
				bp = (i_x[i] + i_y[i] * width) * 3;
				o_rgb[i * 3 + 0] = (ref[bp + 2] & 0xff);// B
				o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
				o_rgb[i * 3 + 2] = (ref[bp + 0] & 0xff);// R
			}
			break;
		default:
			throw new NyARException();
		}
		return;
	}
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void switchBuffer(Object i_ref_object) throws NyARException
	{
		NyARException.notImplement();		
	}

}




/**
 * ソースがYUVフォーマットのデータをBGR24として扱うピクセルリーダ
 * ソースデータをセットした時に変換します。
 * （将来YUVをそのまま素通りさせるように書き換えるかも）
 */
class NyARGLPixelReader_YUV extends JmfRGB24RasterHolder
{
	private NyARIntSize _ref_size;	
	private YUVToRGB _yuv2rgb;

	private javax.media.Buffer _rgb_buf;
	/**
	 * フォーマットアレイから、BGR24フォーマットを探す
	 * @param i_formats
	 * @return
	 */
	private Format pickRGB24Format(Format[] i_formats)
	{
		for(int i=0;i<i_formats.length;i++){
			RGBFormat f=(RGBFormat)i_formats[i];
			if(f.getBitsPerPixel()!=24){
				continue;
			}
			if(f.getRedMask()!=3 ||f.getGreenMask()!=2 ||f.getBlueMask()!=1 || f.getFlipped()!=0)
			{
				continue;
			}
			return f;
		}
		return null;
	}
	public NyARGLPixelReader_YUV(NyARIntSize i_ref_size,YUVFormat i_input_format) throws NyARException
	{
		this._ref_size=i_ref_size;
		this.buffer_type=NyARBufferType.BYTE1D_B8G8R8_24;
		this.buffer=null;
		this._yuv2rgb=new YUVToRGB();
		this._rgb_buf=new javax.media.Buffer();
		//24bit-BGRフォーマットのものを探す
		Format output_format=pickRGB24Format(this._yuv2rgb.getSupportedOutputFormats(i_input_format));
		if(output_format==null){
			throw new NyARException();
		}
		this._yuv2rgb.setInputFormat(i_input_format);
		this._yuv2rgb.setOutputFormat(output_format);
		try{
			this._yuv2rgb.open();
		}catch(Exception e){
			throw new NyARException();
		}
		return;
	}
	public void setMediaBuffer(javax.media.Buffer i_buffer)
	{
		//エラー出した時のトラップ
		if(this._yuv2rgb.process(i_buffer, this._rgb_buf)!=YUVToRGB.BUFFER_PROCESSED_OK){
			System.err.println("YUVToRGB.process error:");
		}
		this.buffer=(byte[])this._rgb_buf.getData();
		return;
	}
	public void getPixel(int i_x, int i_y, int[] o_rgb) throws NyARException
	{
		//IN :BGRBGR
		//   :012012
		final int bp = (i_x + i_y * this._ref_size.w) * 3;
		final byte[] ref = this.buffer;
		o_rgb[0] = (ref[bp + 2] & 0xff);// R
		o_rgb[1] = (ref[bp + 1] & 0xff);// G
		o_rgb[2] = (ref[bp + 0] & 0xff);// B
		return;
	}
	public void getPixelSet(int[] i_x, int i_y[], int i_num, int[] o_rgb) throws NyARException
	{
		int bp;
		final int width = this._ref_size.w;
		final byte[] ref = this.buffer;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 3;
			o_rgb[i * 3 + 0] = (ref[bp + 2] & 0xff);// R
			o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref[bp + 0] & 0xff);// B
		}
		return;
	}
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void switchBuffer(Object i_ref_object) throws NyARException
	{
		NyARException.notImplement();		
	}	
}

