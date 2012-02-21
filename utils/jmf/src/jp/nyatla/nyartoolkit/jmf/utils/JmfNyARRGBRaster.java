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
import com.sun.media.codec.video.colorspace.*;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;


public class JmfNyARRGBRaster extends NyARBufferedImageRaster
{

	/**
	 * コンストラクタ。i_fmtに合致するバッファを持つインスタンスを作成します。
	 * このコンストラクタで作成したクラスは、hasBuffer()がfalseを返すことがあります。
	 * @param i_fmt
	 * @throws NyARException
	 */
	public JmfNyARRGBRaster(VideoFormat i_fmt) throws NyARException
	{
		super(i_fmt.getSize().width,i_fmt.getSize().height,fmt2RasterType(i_fmt),true);
		if(i_fmt instanceof YUVFormat){
			this._buf_adapter=new YuvBufferAdapter(this,(YUVFormat) i_fmt);
		}else{
			this._buf_adapter=new RGBBufferAdapter(this,(RGBFormat) i_fmt);
		}
	}
	private static int fmt2RasterType(VideoFormat i_fmt) throws NyARException
	{
		// データ配列の確認
		if(i_fmt instanceof YUVFormat){
			return NyARBufferType.BYTE1D_B8G8R8_24;
		}else if(i_fmt instanceof RGBFormat){
			RGBFormat fm=(RGBFormat)i_fmt;
			if(fm.getBitsPerPixel()!=24){
				throw new NyARException();
			}
			int r=fm.getRedMask();
			int b=fm.getBlueMask();
			if(r==1 && b==3){
				return NyARBufferType.BYTE1D_R8G8B8_24;
			}else if(r==3 && b==1){
				return NyARBufferType.BYTE1D_B8G8R8_24;			
			}else{
				throw new NyARException();
			}
		}else{
			throw new NyARException();
		}		
	}
	/**
	 * JMFバッファをインスタンスにセットします。
	 * @param i_buffer
	 * @throws NyARException
	 */
	public void setBuffer(javax.media.Buffer i_buffer) throws NyARException
	{
		this._buf_adapter.setBuffer(i_buffer);
		return;
	}
	public void switchRaster(INyARRgbRaster i_raster)throws NyARException
	{
		NyARException.notImplement();		
	}
	private IBufferAdapter _buf_adapter;
	//
	// Buffer adapter
	//
	private interface IBufferAdapter
	{
		void setBuffer(Buffer i_buffer);
	}	
	private class RGBBufferAdapter implements IBufferAdapter
	{
		private JmfNyARRGBRaster _raster;
		boolean _is_flipped;
		public RGBBufferAdapter(JmfNyARRGBRaster i_raster,RGBFormat i_vfm)
		{
			this._raster=i_raster;
			this._is_flipped=i_vfm.getFlipped()!=0?true:false;
		}
		public void setBuffer(Buffer i_buffer)
		{
			NyARIntSize s=this._raster.getSize();
			byte[] buf=(byte[])this._raster.getBuffer();
			//vertical反転が必要ならば、反転した画像を作成する。
			byte[] src=(byte[])i_buffer.getData();
			if(this._is_flipped){
				final int length = s.w * 3;
				int src_idx = 0;
				int dest_idx = (s.h - 1) * length;			
				for (int i = 0; i < s.h; i++) {
					System.arraycopy(src,src_idx, buf, dest_idx, length);
					src_idx += length;
					dest_idx -= length;
				}
			}else{
				System.arraycopy(src,0,buf,0,buf.length);
			}
			return;		
		}
	}
	private class YuvBufferAdapter implements IBufferAdapter
	{
		private Buffer _rgb_buf=new javax.media.Buffer();
		private YUVToRGB _yuv2rgb;
		private JmfNyARRGBRaster _raster;
		public YuvBufferAdapter(JmfNyARRGBRaster i_raster,YUVFormat i_vfm) throws NyARException
		{
			this._yuv2rgb=new YUVToRGB();
			//24bit-BGRフォーマットのものを探す
			Format output_format=selectRGB24Format(this._yuv2rgb.getSupportedOutputFormats(i_vfm));
			if(output_format==null){
				throw new NyARException();
			}
			this._yuv2rgb.setInputFormat(i_vfm);
			this._yuv2rgb.setOutputFormat(output_format);
			try{
				this._yuv2rgb.open();
			}catch(Exception e){
				throw new NyARException(e);
			}
			return;
			
		}
		public void setBuffer(Buffer i_buffer)
		{
			//エラー出した時のトラップ
			if(this._yuv2rgb.process(i_buffer, this._rgb_buf)!=YUVToRGB.BUFFER_PROCESSED_OK){
				System.err.println("YUVToRGB.process error:");
			}
			byte[] buf=(byte[])this._raster.getBuffer();
			System.arraycopy((byte[])this._rgb_buf.getData(),0,buf,0,buf.length);
		}
		/**
		 * フォーマットアレイから、BGR24フォーマットを探す
		 * @param i_formats
		 * @return
		 */
		private Format selectRGB24Format(Format[] i_formats)
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
	}
}

