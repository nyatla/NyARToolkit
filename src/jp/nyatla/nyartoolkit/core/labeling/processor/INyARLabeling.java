package jp.nyatla.nyartoolkit.core.labeling.processor;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.raster.*;

public interface INyARLabeling
{
	public void attachDestination(NyARLabelingImage i_destination_image) throws NyARException;

	public void labeling(NyARBinRaster i_raster) throws NyARException;
}
