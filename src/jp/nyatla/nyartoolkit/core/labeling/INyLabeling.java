package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.*;


public interface INyLabeling
{
    public void attachDestination(NyLabelingImage i_destination_image) throws NyARException;
    public void labeling(INyARRaster i_input_raster) throws NyARException;
}
