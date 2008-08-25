package jp.nyatla.nyartoolkit.core.labeling.processor;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.raster.operator.*;

public interface INyARLabeling
{
    public void attachDestination(NyARLabelingImage i_destination_image) throws NyARException;
    public void labeling(INyARRasterReader i_reader) throws NyARException;
}
