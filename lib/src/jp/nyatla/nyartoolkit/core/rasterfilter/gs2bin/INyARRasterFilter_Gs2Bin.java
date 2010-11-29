package jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;

public interface INyARRasterFilter_Gs2Bin
{
	public void doFilter(NyARGrayscaleRaster i_input, NyARBinRaster i_output) throws NyARException;

}
