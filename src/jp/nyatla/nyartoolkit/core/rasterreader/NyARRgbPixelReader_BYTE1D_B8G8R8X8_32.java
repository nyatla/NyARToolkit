package jp.nyatla.nyartoolkit.core.rasterreader;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARRgbPixelReader_BYTE1D_B8G8R8X8_32 implements INyARRgbPixelReader
{
	protected byte[] _ref_buf;
	private NyARIntSize _ref_size;

	public NyARRgbPixelReader_BYTE1D_B8G8R8X8_32(byte[] i_ref_buf, NyARIntSize i_size)
	{
		this._ref_buf=i_ref_buf;
		this._ref_size = i_size;
	}

	public void getPixel(int i_x, int i_y, int[] o_rgb)
	{
		final byte[] ref_buf =this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 4;
		o_rgb[0] = (ref_buf[bp + 2] & 0xff);// R
		o_rgb[1] = (ref_buf[bp + 1] & 0xff);// G
		o_rgb[2] = (ref_buf[bp + 0] & 0xff);// B
		return;
	}

	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
		int bp;
		final int width = this._ref_size.w;
		final byte[] ref_buf =this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 4;
			o_rgb[i * 3 + 0] = (ref_buf[bp + 2] & 0xff);// R
			o_rgb[i * 3 + 1] = (ref_buf[bp + 1] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref_buf[bp + 0] & 0xff);// B
		}
		return;
	}
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		final byte[] ref_buf =this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 4;
		ref_buf[bp+0] = (byte)i_rgb[0];// R
		ref_buf[bp+1] = (byte)i_rgb[1];// G
		ref_buf[bp+2] = (byte)i_rgb[2];// B	
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void switchBuffer(Object i_ref_buffer) throws NyARException
	{
		assert(((byte[])i_ref_buffer).length>=this._ref_size.w*this._ref_size.h*4);
		this._ref_buf=(byte[])i_ref_buffer;
	}		
}