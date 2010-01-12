package jp.nyatla.nyartoolkit.utils.j2se;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import java.awt.image.BufferedImage;


public class NyARRGBRaster_BufferedImage implements INyARRgbRaster
{
	protected INyARRgbPixelReader _reader;
	protected INyARBufferReader _buffer_reader;	
	final protected NyARIntSize _size;
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
	/**
	 * i_imageをラップするRGBラスタを定義する。
	 * @param i_image
	 */
	protected NyARRGBRaster_BufferedImage(BufferedImage i_image)
	{
		this._buffer_reader=new NyARBufferReader(i_image,INyARBufferReader.BUFFERFORMAT_OBJECT_Java_BufferedImage);
		this._reader=new PixelReader(i_image);
		this._size= new NyARIntSize(i_image.getWidth(),i_image.getHeight());
	}
	public INyARRgbPixelReader getRgbPixelReader() throws NyARException
	{
		return this._reader;
	}
	public INyARBufferReader getBufferReader()
	{
		return this._buffer_reader;
	}
	
	
	/**
	 * byte[]配列に、パディング無しの8bit画素値が、RGBRGBの順で並んでいる
	 * バッファに使用できるピクセルリーダー
	 *
	 */
	private class PixelReader implements INyARRgbPixelReader
	{
		protected BufferedImage _ref_buf;

		public PixelReader(BufferedImage i_buf)
		{
			this._ref_buf = i_buf;
		}

		public void getPixel(int i_x, int i_y, int[] o_rgb)
		{
			final BufferedImage ref_buf = this._ref_buf;
			int p=ref_buf.getRGB(i_x, i_y);
			o_rgb[0] = (p>>16) & 0xff;// R
			o_rgb[1] = (p>>8) & 0xff;// G
			o_rgb[2] = (p>>0) & 0xff;// B
			return;
		}

		public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
		{
			final BufferedImage ref_buf = this._ref_buf;
			for (int i = i_num - 1; i >= 0; i--) {
				int p=ref_buf.getRGB(i_x[i], i_y[i]);
				o_rgb[i * 3 + 0] = (p>>16) & 0xff;// R
				o_rgb[i * 3 + 1] = (p>>8) & 0xff;// G
				o_rgb[i * 3 + 2] = (p>>0) & 0xff;// B
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
		
	}	
	
	
	
	
}
