package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;

public class NyARGsFilterFactory
{
	public static INyARGsCustomToneTableFilter createToneTable(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsToneTableFilter_Any(i_raster);
	}
	public static INyARGsEqualizeHistFilter createEqualizeHist(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsEqualizeHistFilter_Any(i_raster);
	}
	public static INyARGsGaussianSmoothFilter createGaussianSmoothFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsGaussianSmoothFilter_GS8(i_raster);
	}
	public static INyARGsReverseFilter createReverseFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsReverseFilter_Any(i_raster);
	}
	public static INyARGsRobertsFilter createRobertsFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsRobertsFilter_GS8(i_raster);
	}		
}

