package jp.nyatla.nyartoolkit.core.raster;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARGsPixelDriver;

/**
 * このインタフェイスは、グレースケール画像の操作インタフェイスを提供します。
 * {@link INyARGrayscaleRaster#createInterface(Class)}関数は、少なくともNyARLabeling_Rle.IImageDriver,NyARContourPickup.IImageDriver
 * インタフェイスを提供します。
 * @author nyatla
 *
 */
public interface INyARGrayscaleRaster extends INyARRaster
{
	public INyARGsPixelDriver getGsPixelDriver() throws NyARException;
}
