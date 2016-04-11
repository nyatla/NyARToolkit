package jp.nyatla.nyartoolkit.core.kpm.freak;

import jp.nyatla.nyartoolkit.core.kpm.utils.LongDescripter768;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

/**
 * Represents a feature point in the visual database.
 */
public class FreakFeaturePoint extends NyARDoublePoint2d
{
    final public LongDescripter768 descripter=new LongDescripter768();

 	

    public FreakFeaturePoint() {
	}
    


    /**
     * The orientation of the feature in the range [0,2*pi)
     */
    public double angle;

    /**
     * The radius (scale) of the feature in the image.
     */
    public double scale;

    /**
     * TRUE if this is maxima, FALSE if a minima.
     */
    public boolean maxima;

      
}
