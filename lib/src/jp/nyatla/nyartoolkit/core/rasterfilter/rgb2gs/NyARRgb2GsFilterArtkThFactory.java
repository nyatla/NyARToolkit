package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

public class NyARRgb2GsFilterArtkThFactory
{
	public static INyARRgb2GsFilterArtkTh createDriver(INyARRgbRaster i_raster)
	{
		switch (i_raster.getBufferType())
		{
		case NyARBufferType.BYTE1D_B8G8R8_24:
		case NyARBufferType.BYTE1D_R8G8B8_24:
			return new NyARRgb2GsFilterArtkTh_BYTE1D_C8C8C8_24(i_raster);
		case NyARBufferType.BYTE1D_B8G8R8X8_32:
			return new NyARRgb2GsFilterArtkTh_BYTE1D_B8G8R8X8_32(i_raster);
		case NyARBufferType.BYTE1D_X8R8G8B8_32:
			return new NyARRgb2GsFilterArtkTh_BYTE1D_X8R8G8B8_32(i_raster);
		case NyARBufferType.INT1D_X8R8G8B8_32:
			return new NyARRgb2GsFilterArtkTh_INT1D_X8R8G8B8_32(i_raster);
		case NyARBufferType.WORD1D_R5G6B5_16LE:
			return new NyARRgb2GsFilterArtkTh_WORD1D_R5G6B5_16LE(i_raster);
		default:
			return new NyARRgb2GsFilterArtkTh_Any(i_raster);
		}
	}
}