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



/**
 * RGB24フォーマットのデータを扱うピクセルリーダ
 */
class NyARBufferReader_Reader_RGB24 extends NyARBufferReader_Reader
{
	protected boolean _is_flipped;
	public NyARBufferReader_Reader_RGB24(RGBFormat i_input_format,NyARIntSize i_ref_size) throws NyARException
	{
		super(i_ref_size);
		//ピクセルフォーマットを設定(現状は24bitRGBを受けつける。)
		RGBFormat fm=(RGBFormat)i_input_format;
		if(fm.getBitsPerPixel()!=24){
			throw new NyARException();
		}
		int r=fm.getRedMask();
		int b=fm.getBlueMask();
		if(r==1 && b==3){
			this._buf_type=BUFFERFORMAT_BYTE1D_R8G8B8_24;
		}else if(r==3 && b==1){
			this._buf_type=BUFFERFORMAT_BYTE1D_B8G8R8_24;			
		}else{
			throw new NyARException();
		}
		//vertical反転の有無を確認
		this._is_flipped=i_input_format.getFlipped()!=0?true:false;
		this._ref_buf=new byte[i_ref_size.w*i_ref_size.h*3];
		//RGBフォーマット
		
		return;
	}
	public void changeBuffer(javax.media.Buffer i_buffer)
	{
		//vertical反転が必要ならば、反転した画像を作成する。
		byte[] src=(byte[])i_buffer.getData();
		if(this._is_flipped){
			final int length = this._ref_size.w * 3;
			int src_idx = 0;
			int dest_idx = (this._ref_size.h - 1) * length;			
			for (int i = 0; i < this._ref_size.h; i++) {
				System.arraycopy(src,src_idx, this._ref_buf, dest_idx, length);
				src_idx += length;
				dest_idx -= length;
			}
		}else{
			System.arraycopy(src,0,this._ref_buf,0,this._ref_buf.length);
			this._ref_buf=(byte[])i_buffer.getData();
		}
		return;
	}
	public void getPixel(int i_x, int i_y, int[] o_rgb) throws NyARException
	{
		int bp = (i_x + i_y * this._ref_size.w) * 3;
		byte[] ref = this._ref_buf;
		switch(this._buf_type){
		case BUFFERFORMAT_BYTE1D_R8G8B8_24:
			o_rgb[0] = (ref[bp + 0] & 0xff);// R
			o_rgb[1] = (ref[bp + 1] & 0xff);// G
			o_rgb[2] = (ref[bp + 2] & 0xff);// B
			break;
		case BUFFERFORMAT_BYTE1D_B8G8R8_24:
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
		byte[] ref = this._ref_buf;
		int bp;
		switch(this._buf_type){
		case BUFFERFORMAT_BYTE1D_R8G8B8_24:
			for (int i = i_num - 1; i >= 0; i--) {
				bp = (i_x[i] + i_y[i] * width) * 3;
				o_rgb[i * 3 + 0] = (ref[bp + 0] & 0xff);// R
				o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
				o_rgb[i * 3 + 2] = (ref[bp + 2] & 0xff);// B
			}
			break;
		case BUFFERFORMAT_BYTE1D_B8G8R8_24:
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
}




/**
 * ソースがYUVフォーマットのデータをBGR24として扱うピクセルリーダ
 * ソースデータをセットした時に変換します。
 * （将来YUVをそのまま素通りさせるように書き換えるかも）
 */
class NyARBufferReader_Reader_YUV extends NyARBufferReader_Reader
{
	
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
	public NyARBufferReader_Reader_YUV(YUVFormat i_input_format,NyARIntSize i_ref_size) throws NyARException
	{
		super(i_ref_size,BUFFERFORMAT_BYTE1D_B8G8R8_24);
		this._yuv2rgb=new YUVToRGB();
		this._rgb_buf=new javax.media.Buffer();
		this._ref_buf=null;
		//24bit-RGBフォーマットのものを探す
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
	public void changeBuffer(javax.media.Buffer i_buffer)
	{
		//エラー出した時のトラップ
		if(this._yuv2rgb.process(i_buffer, this._rgb_buf)!=YUVToRGB.BUFFER_PROCESSED_OK){
			System.err.println("YUVToRGB.process error:");
		}
		this._ref_buf=(byte[])this._rgb_buf.getData();
		return;
	}
	public void getPixel(int i_x, int i_y, int[] o_rgb) throws NyARException
	{
		int bp = (i_x + i_y * this._ref_size.w) * 3;
		byte[] ref = this._ref_buf;
		o_rgb[0] = (ref[bp + 2] & 0xff);// B
		o_rgb[1] = (ref[bp + 1] & 0xff);// G
		o_rgb[2] = (ref[bp + 0] & 0xff);// R
		return;
	}
	public void getPixelSet(int[] i_x, int i_y[], int i_num, int[] o_rgb) throws NyARException
	{
		int width = this._ref_size.w;
		byte[] ref = this._ref_buf;
		int bp;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 3;
			o_rgb[i * 3 + 0] = (ref[bp + 2] & 0xff);// B
			o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref[bp + 0] & 0xff);// R
		}
		return;
	}	
}


/**
 * 
 * RGB形式のJMFバッファをラップするNyARRasterです。
 * JMFから得たラスタデータのピクセル並び順を考慮します。
 *
 */
public class JmfNyARRaster_RGB extends NyARRgbRaster_BasicClass
{

	protected NyARBufferReader_Reader _reader;
	/**
	 * i_formatに一致する画素フォーマットの
	 * @param i_size
	 * @param i_format
	 * @throws NyARException
	 */


	public JmfNyARRaster_RGB(NyARIntSize i_ref_size,VideoFormat i_format) throws NyARException
	{
		super(new NyARIntSize(i_ref_size));		
		this._reader = createReader(i_format);
	}
	public JmfNyARRaster_RGB(int i_width,int i_height,VideoFormat i_format) throws NyARException
	{
		super(new NyARIntSize(i_width,i_height));
		this._reader = createReader(i_format);
	}
	
	/**
	 * フォーマットを解析して、マッチするリーダオブジェクトを返します。
	 * @param i_fmt
	 * ビデオフォーマットを指定します。
	 * @return
	 * リーダオブジェクト
	 * @throws NyARException
	 */
	private NyARBufferReader_Reader createReader(VideoFormat i_fmt) throws NyARException
	{
		// データサイズの確認
		final Dimension s = i_fmt.getSize();
		if (!this._size.isEqualSize(s.width, s.height)) {
			throw new NyARException();
		}
		// データ配列の確認
		if(i_fmt instanceof YUVFormat){
			//YUVフォーマット
			return new NyARBufferReader_Reader_YUV((YUVFormat)i_fmt,this._size);			
		}else if(i_fmt instanceof RGBFormat){
			//RGBフォーマット
			return new NyARBufferReader_Reader_RGB24((RGBFormat)i_fmt,this._size);
		}else{
			throw new NyARException();
		}
	}

	/**
	 * javax.media.Bufferを分析して、その分析結果をNyARRasterに適合する形で保持します。
	 * 関数実行後に外部でi_bufferの内容変更した場合には、再度setBuffer関数を呼び出してください。
	 * この関数を実行すると、getRgbPixelReaderで取得したReaderのプロパティが変化することがあります。
	 * @param i_buffer
	 * RGB形式のデータを格納したjavax.media.Bufferオブジェクトを指定してください。
	 * @return i_bufferをラップしたオブジェクトを返します。
	 * @throws NyARException
	 */
	public void setBuffer(javax.media.Buffer i_buffer) throws NyARException
	{
		this._reader.changeBuffer(i_buffer);
		return;
	}
	/**
	 * データを持っているかを返します。
	 * @return
	 */
	public boolean hasData()
	{
		return this._reader._ref_buf != null;
	}
	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._reader;
	}
	public INyARBufferReader getBufferReader()
	{
		return this._reader;
	}
}



