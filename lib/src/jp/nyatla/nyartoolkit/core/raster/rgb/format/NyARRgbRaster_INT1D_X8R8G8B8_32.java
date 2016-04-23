package jp.nyatla.nyartoolkit.core.raster.rgb.format;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

public class NyARRgbRaster_INT1D_X8R8G8B8_32 extends NyARRgbRaster
{
	protected int[] _buf;
	public NyARRgbRaster_INT1D_X8R8G8B8_32(int i_width, int i_height,boolean i_is_alloc)
	{
		super(i_width, i_height, i_is_alloc);
		this._buf=i_is_alloc?new int[i_width*i_height]:null;

	}
	public NyARRgbRaster_INT1D_X8R8G8B8_32(int i_width, int i_height,int[] i_buffer)
	{
		super(i_width, i_height,true);
		this._buf=i_buffer;
	}
	
	@Override
	final public Object getBuffer()
	{
		return this._buf;
	}
	@Override
	final public int getBufferType()
	{
		return NyARBufferType.INT1D_X8R8G8B8_32;
	}
	@Override
	final public void wrapBuffer(Object i_buf)
	{
		assert (!this._is_attached_buffer);// バッファがアタッチされていたら機能しない。
		//ラスタの形式は省略。
		this._buf = (int[])i_buf;	
	}
	@Override
	final public int[] getPixel(int i_x, int i_y, int[] o_rgb)
	{
		final int rgb = this._buf[i_x + i_y * this._size.w];
		o_rgb[0] = (rgb >> 16) & 0xff;// R
		o_rgb[1] = (rgb >> 8) & 0xff;// G
		o_rgb[2] = rgb & 0xff;// B
		return o_rgb;
	}
	@Override
	final public int[] getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
		final int width = this._size.w;
		final int[] ref_buf = this._buf;
		for (int i = i_num - 1; i >= 0; i--) {
			int rgb = ref_buf[i_x[i] + i_y[i] * width];
			o_rgb[i * 3 + 0] = (rgb >> 16) & 0xff;// R
			o_rgb[i * 3 + 1] = (rgb >> 8) & 0xff;// G
			o_rgb[i * 3 + 2] = rgb & 0xff;// B
		}
		return o_rgb;
	}
	@Override
	final public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
	{
		this._buf[i_x + i_y * this._size.w] = (i_r << 16) | (i_g << 8) | (i_b);		
	}
	@Override
	final public void setPixel(int i_x, int i_y, int[] i_rgb)
	{
		this._buf[i_x + i_y * this._size.w] = (i_rgb[0] << 16) | (i_rgb[1] << 8) | (i_rgb[2]);		
	}
	@Override
	final public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
	{
		NyARRuntimeException.notImplement();
	}
}
