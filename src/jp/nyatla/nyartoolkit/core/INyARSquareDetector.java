package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;

public interface INyARSquareDetector
{
	public void detectMarker(NyARBinRaster i_raster, NyARSquareStack o_square_stack) throws NyARException;
}
