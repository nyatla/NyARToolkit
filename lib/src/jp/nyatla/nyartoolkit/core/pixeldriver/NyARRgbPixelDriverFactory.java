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
package jp.nyatla.nyartoolkit.core.pixeldriver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * この関数は、NyARRgbRasterからコールします。
 */
public class NyARRgbPixelDriverFactory
{
	/**
	 * この関数は、i_rasterを操作するピクセルドライバインスタンスを生成します。
	 * @param i_raster
	 * @return
	 * @throws NyARException
	 */
	public static INyARRgbPixelDriver createDriver(INyARRgbRaster i_raster) throws NyARException
	{
		INyARRgbPixelDriver ret;
		switch(i_raster.getBufferType()){
		case NyARBufferType.BYTE1D_B8G8R8_24:
			ret=new NyARRgbPixelDriver_BYTE1D_B8G8R8_24();
			break;
		case NyARBufferType.BYTE1D_B8G8R8X8_32:
			ret=new NyARRgbPixelDriver_BYTE1D_B8G8R8X8_32();
			break;
		case NyARBufferType.BYTE1D_R8G8B8_24:
			ret=new NyARRgbPixelDriver_BYTE1D_R8G8B8_24();
			break;
		case NyARBufferType.BYTE1D_X8R8G8B8_32:
			ret=new NyARRgbPixelDriver_BYTE1D_X8R8G8B8_32();
			break;
		case NyARBufferType.INT1D_GRAY_8:
			ret=new NyARRgbPixelDriver_INT1D_GRAY_8();
			break;
		case NyARBufferType.INT1D_X8R8G8B8_32:
			ret= new NyARRgbPixelDriver_INT1D_X8R8G8B8_32();
			break;
		case NyARBufferType.BYTE1D_R5G6B5_16BE:
			ret= new NyARRgbPixelDriver_WORD1D_R5G6B5_16LE();
			break;
		default:
			throw new NyARException();		
		}
		ret.switchRaster(i_raster);
		return ret;
	}
}
//--------------------------------------------------------------------------------
//ピクセルドライバの定義
//--------------------------------------------------------------------------------

/**
* このクラスは、{@link NyARBufferType#BYTE1D_B8G8R8_24}形式のラスタバッファに対応する、ピクセルリーダです。
*/
final class NyARRgbPixelDriver_BYTE1D_B8G8R8_24 implements INyARRgbPixelDriver {
	/** 参照する外部バッファ */
	protected byte[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb) {
		final byte[] ref_buf = this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 3;
		o_rgb[0] = (ref_buf[bp + 2] & 0xff);// R
		o_rgb[1] = (ref_buf[bp + 1] & 0xff);// G
		o_rgb[2] = (ref_buf[bp + 0] & 0xff);// B
		return;
	}

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb) {
		int bp;
		final int width = this._ref_size.w;
		final byte[] ref_buf = this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 3;
			o_rgb[i * 3 + 0] = (ref_buf[bp + 2] & 0xff);// R
			o_rgb[i * 3 + 1] = (ref_buf[bp + 1] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref_buf[bp + 0] & 0xff);// B
		}
		return;
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException {
		final byte[] ref_buf = this._ref_buf;
		final int idx = (i_y * this._ref_size.w + i_x) * 3;
		ref_buf[idx + 0] = (byte) i_rgb[2];// B
		ref_buf[idx + 1] = (byte) i_rgb[1];// G
		ref_buf[idx + 2] = (byte) i_rgb[0];// R
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARException {
		final byte[] ref_buf = this._ref_buf;
		final int idx = (i_y * this._ref_size.w + i_x) * 3;
		ref_buf[idx + 0] = (byte) i_b;// B
		ref_buf[idx + 1] = (byte) i_g;// G
		ref_buf[idx + 2] = (byte) i_r;// R
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARException {
		NyARException.notImplement();
	}

	public void switchRaster(INyARRgbRaster i_raster) throws NyARException {
		this._ref_buf = (byte[]) i_raster.getBuffer();
		this._ref_size = i_raster.getSize();
	}

}

/**
* このクラスは、{@link NyARBufferType#BYTE1D_B8G8R8X8_32}形式のラスタバッファに対応する、ピクセルリーダです。
*/
final class NyARRgbPixelDriver_BYTE1D_B8G8R8X8_32 implements
		INyARRgbPixelDriver {
	/** 参照する外部バッファ */
	protected byte[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb) {
		final byte[] ref_buf = this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 4;
		o_rgb[0] = (ref_buf[bp + 2] & 0xff);// R
		o_rgb[1] = (ref_buf[bp + 1] & 0xff);// G
		o_rgb[2] = (ref_buf[bp + 0] & 0xff);// B
		return;
	}

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb) {
		int bp;
		final int width = this._ref_size.w;
		final byte[] ref_buf = this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 4;
			o_rgb[i * 3 + 0] = (ref_buf[bp + 2] & 0xff);// R
			o_rgb[i * 3 + 1] = (ref_buf[bp + 1] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref_buf[bp + 0] & 0xff);// B
		}
		return;
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException {
		final byte[] ref_buf = this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 4;
		ref_buf[bp + 2] = (byte) i_rgb[0];// R
		ref_buf[bp + 1] = (byte) i_rgb[1];// G
		ref_buf[bp + 0] = (byte) i_rgb[2];// B
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARException {
		final byte[] ref_buf = this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 4;
		ref_buf[bp + 2] = (byte) i_r;// R
		ref_buf[bp + 1] = (byte) i_g;// G
		ref_buf[bp + 0] = (byte) i_b;// B
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARException {
		NyARException.notImplement();
	}

	public void switchRaster(INyARRgbRaster i_raster) throws NyARException {
		this._ref_buf = (byte[]) i_raster.getBuffer();
		this._ref_size = i_raster.getSize();
	}

}

/**
* このクラスは、{@link NyARBufferType#BYTE1D_R8G8B8_24}形式のラスタバッファに対応する、ピクセルリーダです。
*/
final class NyARRgbPixelDriver_BYTE1D_R8G8B8_24 implements INyARRgbPixelDriver {
	/** 参照する外部バッファ */
	protected byte[] _ref_buf;

	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb) {
		final byte[] ref_buf = this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 3;
		o_rgb[0] = (ref_buf[bp + 0] & 0xff);// R
		o_rgb[1] = (ref_buf[bp + 1] & 0xff);// G
		o_rgb[2] = (ref_buf[bp + 2] & 0xff);// B
		return;
	}

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb) {
		int bp;
		final int width = this._ref_size.w;
		final byte[] ref_buf = this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 3;
			o_rgb[i * 3 + 0] = (ref_buf[bp + 0] & 0xff);// R
			o_rgb[i * 3 + 1] = (ref_buf[bp + 1] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref_buf[bp + 2] & 0xff);// B
		}
		return;
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException {
		final byte[] ref_buf = this._ref_buf;
		final int idx = (i_y * this._ref_size.w + i_x) * 3;
		ref_buf[idx + 0] = (byte) i_rgb[0];// R
		ref_buf[idx + 1] = (byte) i_rgb[1];// G
		ref_buf[idx + 2] = (byte) i_rgb[2];// B
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARException {
		final byte[] ref_buf = this._ref_buf;
		final int idx = (i_y * this._ref_size.w + i_x) * 3;
		ref_buf[idx + 0] = (byte) i_r;// R
		ref_buf[idx + 1] = (byte) i_g;// G
		ref_buf[idx + 2] = (byte) i_b;// B
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARException {
		NyARException.notImplement();
	}

	public void switchRaster(INyARRgbRaster i_raster) throws NyARException {
		this._ref_buf = (byte[]) i_raster.getBuffer();
		this._ref_size = i_raster.getSize();
	}
}

/**
* このクラスは、{@link NyARBufferType#BYTE1D_X8R8G8B8_32}形式のラスタバッファに対応する、ピクセルリーダです。
*/
final class NyARRgbPixelDriver_BYTE1D_X8R8G8B8_32 implements
		INyARRgbPixelDriver {
	/** 参照する外部バッファ */
	protected byte[] _ref_buf;

	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb) {
		final byte[] ref_buf = this._ref_buf;
		final int bp = (i_x + i_y * this._ref_size.w) * 4;
		o_rgb[0] = (ref_buf[bp + 1] & 0xff);// R
		o_rgb[1] = (ref_buf[bp + 2] & 0xff);// G
		o_rgb[2] = (ref_buf[bp + 3] & 0xff);// B
		return;
	}

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb) {
		int bp;
		final int width = this._ref_size.w;
		final byte[] ref_buf = this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * width) * 4;
			o_rgb[i * 3 + 0] = (ref_buf[bp + 1] & 0xff);// R
			o_rgb[i * 3 + 1] = (ref_buf[bp + 2] & 0xff);// G
			o_rgb[i * 3 + 2] = (ref_buf[bp + 3] & 0xff);// B
		}
		return;
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException {
		NyARException.notImplement();
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARException {
		NyARException.notImplement();
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARException {
		NyARException.notImplement();
	}

	public void switchRaster(INyARRgbRaster i_raster) throws NyARException {
		this._ref_buf = (byte[]) i_raster.getBuffer();
		this._ref_size = i_raster.getSize();
	}
}

/**
* このクラスは、{@link NyARBufferType#INT1D_GRAY_8}形式のラスタバッファに対応する、ピクセルリーダです。
*/
final class NyARRgbPixelDriver_INT1D_GRAY_8 implements INyARRgbPixelDriver {
	/** 参照する外部バッファ */
	protected int[] _ref_buf;

	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb) {
		o_rgb[0] = o_rgb[1] = o_rgb[2] = this._ref_buf[i_x + i_y
				* this._ref_size.w];
		return;
	}

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb) {
		final int width = this._ref_size.w;
		final int[] ref_buf = this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			o_rgb[i * 3 + 0] = o_rgb[i * 3 + 1] = o_rgb[i * 3 + 2] = ref_buf[i_x[i]
					+ i_y[i] * width];
		}
		return;
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException {
		NyARException.notImplement();
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARException {
		NyARException.notImplement();
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARException {
		NyARException.notImplement();
	}

	public void switchRaster(INyARRgbRaster i_raster) throws NyARException {
		this._ref_buf = (int[]) i_raster.getBuffer();
		this._ref_size = i_raster.getSize();
	}
}

/**
* このクラスは、{@link NyARBufferType#INT1D_X8R8G8B8_32}形式のラスタバッファに対応する、ピクセルリーダです。
*/
final class NyARRgbPixelDriver_INT1D_X8R8G8B8_32 implements INyARRgbPixelDriver {
	/** 参照する外部バッファ */
	protected int[] _ref_buf;

	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb) {
		final int rgb = this._ref_buf[i_x + i_y * this._ref_size.w];
		o_rgb[0] = (rgb >> 16) & 0xff;// R
		o_rgb[1] = (rgb >> 8) & 0xff;// G
		o_rgb[2] = rgb & 0xff;// B
		return;
	}

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb) {
		final int width = this._ref_size.w;
		final int[] ref_buf = this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--) {
			int rgb = ref_buf[i_x[i] + i_y[i] * width];
			o_rgb[i * 3 + 0] = (rgb >> 16) & 0xff;// R
			o_rgb[i * 3 + 1] = (rgb >> 8) & 0xff;// G
			o_rgb[i * 3 + 2] = rgb & 0xff;// B
		}
		return;
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException {
		this._ref_buf[i_x + i_y * this._ref_size.w] = (i_rgb[0] << 16)
				| (i_rgb[1] << 8) | (i_rgb[2]);
	}

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARException {
		this._ref_buf[i_x + i_y * this._ref_size.w] = (i_r << 16) | (i_g << 8)
				| (i_b);
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARException {
		NyARException.notImplement();
	}

	public void switchRaster(INyARRgbRaster i_raster) throws NyARException {
		this._ref_buf = (int[]) i_raster.getBuffer();
		this._ref_size = i_raster.getSize();
	}
}

/**
* このクラスは、{@link NyARBufferType#WORD1D_R5G6B5_16LE}形式のラスタバッファに対応する、ピクセルリーダです。
* この形式は、WindowsMobile等のモバイルデバイスで使われる形式です。
*/
final class NyARRgbPixelDriver_WORD1D_R5G6B5_16LE implements
		INyARRgbPixelDriver {
	/** 参照する外部バッファ */
	protected short[] _ref_buf;

	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb) {
		short[] buf = this._ref_buf;
		int y = i_y;
		int idx = y * this._ref_size.w + i_x;
		int pixcel = (int) (buf[idx] & 0xffff);

		o_rgb[0] = (int) ((pixcel & 0xf800) >> 8);// R
		o_rgb[1] = (int) ((pixcel & 0x07e0) >> 3);// G
		o_rgb[2] = (int) ((pixcel & 0x001f) << 3);// B
		return;
	}

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb) {
		int stride = this._ref_size.w;
		short[] buf = this._ref_buf;

		for (int i = i_num - 1; i >= 0; i--) {
			int idx = i_y[i] * stride + i_x[i];

			int pixcel = (int) (buf[idx] & 0xffff);
			o_rgb[i * 3 + 0] = (int) ((pixcel & 0xf800) >> 8);// R
			o_rgb[i * 3 + 1] = (int) ((pixcel & 0x07e0) >> 3);// G
			o_rgb[i * 3 + 2] = (int) ((pixcel & 0x001f) << 3);// B
		}
		return;
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException {
		NyARException.notImplement();
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARException {
		NyARException.notImplement();
	}

	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARException {
		NyARException.notImplement();
	}

	public void switchRaster(INyARRgbRaster i_raster) throws NyARException {
		this._ref_buf = (short[]) i_raster.getBuffer();
		this._ref_size = i_raster.getSize();
	}
}
