package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;

public class NyARGsFilterFactory
{
	public static INyARGsToneTableFilter createToneTable(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsToneTableFilter_Any(i_raster);
	}
	public static INyARGsEqualizeHistFilter createEqualizeHist(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsEqualizeHistFilter_Any(i_raster);
	}
}

