package jp.nyatla.nyartoolkit.utils.j2se;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;


public class NyARRGBRaster_BufferedImage extends NyARRgbRaster_BasicClass
{
	private INyARRgbPixelReader _reader;
	private BufferedImage _buf; 
	
	/**
	 * バッファオブジェクトがアタッチされていればtrue
	 */
	protected boolean _is_attached_buffer;
	public NyARRGBRaster_BufferedImage(int i_width, int i_height,boolean i_is_alloc) throws NyARException
	{
		super( new NyARIntSize(i_width,i_height),NyARBufferType.OBJECT_Java_BufferedImage);
		if(!initInstance(this._size,i_is_alloc)){
			throw new NyARException();
		}
	}
	public NyARRGBRaster_BufferedImage(int i_width, int i_height) throws NyARException
	{
		super( new NyARIntSize(i_width,i_height),NyARBufferType.OBJECT_Java_BufferedImage);
		if(!initInstance(this._size,true)){
			throw new NyARException();
		}
	}
	protected boolean initInstance(NyARIntSize i_size,boolean i_is_alloc)
	{
		this._buf=i_is_alloc?new BufferedImage(i_size.w,i_size.h, ColorSpace.TYPE_RGB):null;
		this._reader=new PixelReader((BufferedImage)this._buf);
		this._is_attached_buffer=i_is_alloc;
		return true;
	}	
	

	public INyARRgbPixelReader getRgbPixelReader() throws NyARException
	{
		return this._reader;
	}
	public Object getBuffer()
	{
		return this._buf;
	}
	public boolean hasBuffer()
	{
		return this._buf!=null;
	}
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		BufferedImage buf=(BufferedImage)i_ref_buf;
		assert(buf.getWidth()==this._size.w && buf.getHeight()==this._size.h);
		this._buf=buf;
		//ピクセルリーダーの参照バッファを切り替える。
		this._reader.switchBuffer(i_ref_buf);
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
		public void switchBuffer(Object i_ref_buffer) throws NyARException
		{
			this._ref_buf = (BufferedImage)i_ref_buffer;
		}
		
	}	
	
	
	
	
}
