/**
 * RGB形式のJMFバッファをラップするNyARRasterです。
 * JMFから得たラスタデータのピクセル並び順を考慮します。
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jmf.utils;

import javax.media.format.*;
import java.awt.Dimension;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;

public class JmfNyARRaster_RGB extends NyARRgbRaster_BasicClass
{
	protected class Reader implements INyARRgbPixelReader,INyARBufferReader
	{
		private int _buffer_type = INyARBufferReader.BUFFERFORMAT_NULL_ALLZERO;
		private byte[] _ref_buf;
		private NyARIntSize _size;

		public Reader(NyARIntSize i_size)
		{
			this._size = i_size;
		}
		//
		//INyARRgbPixelReader
		//
		public void getPixel(int i_x, int i_y, int[] o_rgb) throws NyARException
		{
			int bp = (i_x + i_y * this._size.w) * 3;
			byte[] ref = this._ref_buf;
			switch (this._buffer_type) {
			case INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24:
				o_rgb[0] = (ref[bp + 0] & 0xff);// R
				o_rgb[1] = (ref[bp + 1] & 0xff);// G
				o_rgb[2] = (ref[bp + 2] & 0xff);// B
				break;
			case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24:
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
			int width = this._size.w;
			byte[] ref = this._ref_buf;
			int bp;
			switch (this._buffer_type) {
			case INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24:
				for (int i = i_num - 1; i >= 0; i--) {
					bp = (i_x[i] + i_y[i] * width) * 3;
					o_rgb[i * 3 + 0] = (ref[bp + 0] & 0xff);// R
					o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
					o_rgb[i * 3 + 2] = (ref[bp + 2] & 0xff);// B
				}
				break;
			case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24:
				for (int i = i_num - 1; i >= 0; i--) {
					bp = (i_x[i] + i_y[i] * width) * 3;
					o_rgb[i * 3 + 0] = (ref[bp + 2] & 0xff);// R
					o_rgb[i * 3 + 1] = (ref[bp + 1] & 0xff);// G
					o_rgb[i * 3 + 2] = (ref[bp + 0] & 0xff);// B
				}
				break;
			default:
				throw new NyARException();
			}
			return;
		}

		public void changeBuffer(int i_buffer_type, byte[] i_buffer)
		{
			if(i_buffer_type==1){
				System.out.println("aaa");
			}
			
			this._buffer_type = i_buffer_type;
			this._ref_buf = i_buffer;
		}
		//
		//INyARBufferReader
		//
		public Object getBuffer()
		{
			return this._ref_buf;
		}
		public int getBufferType()
		{
			return _buffer_type;
		}
		public boolean isEqualBufferType(int i_type_value)
		{
			return this._buffer_type==i_type_value;
		}		
	}

	protected byte[] _ref_buf;
	protected Reader _reader;
	/**
	 * RGB形式のJMFバッファをラップするオブジェクトをつくります。 生成直後のオブジェクトはデータを持ちません。
	 * メンバ関数はsetBufferを実行後に使用可能になります。
	 */
	public JmfNyARRaster_RGB(NyARIntSize i_size)
	{
		this._size.w = i_size.w;
		this._size.h = i_size.h;
		this._ref_buf = null;
		this._reader = new Reader(this._size);
	}
	public JmfNyARRaster_RGB(int i_width,int i_height)
	{
		this._size.w = i_width;
		this._size.h = i_height;
		this._ref_buf = null;
		this._reader = new Reader(this._size);
	}	
	
	/**
	 * フォーマットを解析して、ラスタタイプを返します。
	 * 
	 * @param i_fmt
	 * @throws NyARException
	 */
	protected int analyzeBufferType(RGBFormat i_fmt) throws NyARException
	{
		// データサイズの確認
		Dimension s = i_fmt.getSize();
		if (!this._size.isEqualSize(s.width, s.height)) {
			throw new NyARException();
		}
		// データ配列の確認
		int r = i_fmt.getRedMask() - 1;
		int b = i_fmt.getBlueMask() - 1;

		// 色配列の特定
		if (r == 0 && b == 2) {
			return INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24;
		} else if (r == 2 && b == 0) {
			return INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24;
		} else {
			throw new NyARException("Unknown pixel order.");
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
		int buftype= analyzeBufferType((RGBFormat) i_buffer.getFormat());
		this._ref_buf = (byte[]) i_buffer.getData();
		this._reader.changeBuffer(buftype, this._ref_buf);
	}

	/**
	 * データを持っているかを返します。
	 * @return
	 */
	public boolean hasData()
	{
		return this._ref_buf != null;
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
