package jp.nyatla.nyartoolkit.utils.j2se;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARBufferedImageIo
{
	/**
	 * i_rasterとバッファを共有するBufferedImageを生成します。
	 * 
	 * @param i_raster
	 * @return
	 * @throws NyARException
	 */
	public static BufferedImage createWrappedBufferedImage(INyARGrayscaleRaster i_raster) throws NyARException {
		BufferedImage bfi;
		NyARIntSize s = i_raster.getSize();
		switch (i_raster.getBufferType()) {
		case NyARBufferType.INT1D_GRAY_8: {
			int[] b = (int[]) i_raster.getBuffer();
			DataBufferInt d = new DataBufferInt(b, b.length);
			int[] msk = { 0x0000ff, 0x0000ff, 0x0000ff };
			bfi = new BufferedImage(new DirectColorModel(24, msk[0], msk[1], msk[2]), Raster.createWritableRaster(
					new SinglePixelPackedSampleModel(d.getDataType(), s.w, s.h, msk), d, null), true, null);

		}
			break;
		default:
			throw new NyARException();
		}
		return bfi;
	}	
	/**
	 * i_rasterとバッファを共有するBufferedImageを生成します。
	 * 
	 * @param i_raster
	 * @return
	 * @throws NyARException
	 */
	public static BufferedImage createWrappedBufferedImage(INyARRgbRaster i_raster) throws NyARException {
		BufferedImage bfi;
		NyARIntSize s = i_raster.getSize();
		switch (i_raster.getBufferType()) {
		case NyARBufferType.BYTE1D_R8G8B8_24: {
			byte[] b = (byte[]) i_raster.getBuffer();
			DataBufferByte d = new DataBufferByte(b, b.length);
			int[] bof = { 0, 1, 2 };
			bfi = new BufferedImage(
					new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE),
					Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(), s.w, s.h, 3, 3 * s.w, bof), d, null), true, null);
		}
			break;
		case NyARBufferType.BYTE1D_B8G8R8_24: {
			byte[] b = (byte[]) i_raster.getBuffer();
			DataBufferByte d = new DataBufferByte(b, b.length);
			int[] bof = { 2, 1, 0 };
			bfi = new BufferedImage(
					new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE),
					Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(), s.w, s.h, 3, 3 * s.w, bof), d, null), true, null);
		}
			break;
		case NyARBufferType.INT1D_X8R8G8B8_32: {
			int[] b = (int[]) i_raster.getBuffer();
			DataBufferInt d = new DataBufferInt(b, b.length);
			int[] msk = { 0xff0000, 0x00ff00, 0x0000ff };
			bfi = new BufferedImage(new DirectColorModel(24, msk[0], msk[1], msk[2]), Raster.createWritableRaster(
					new SinglePixelPackedSampleModel(d.getDataType(), s.w, s.h, msk), d, null), true, null);
		}
			break;
		default:
			throw new NyARException();
		}
		return bfi;
	}

}
