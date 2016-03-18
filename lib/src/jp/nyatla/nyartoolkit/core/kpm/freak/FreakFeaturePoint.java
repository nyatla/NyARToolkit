package jp.nyatla.nyartoolkit.core.kpm.freak;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter768;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

/**
 * Represents a feature point in the visual database.
 */
public class FreakFeaturePoint extends NyARDoublePoint2d
{
    final public LongDescripter768 descripter=new LongDescripter768();

 	

    public FreakFeaturePoint() {
	}
	public void set(FreakFeaturePoint i_src) {
		// TODO Auto-generated method stub
		this.x=i_src.x;
		this.y=i_src.y;
		this.angle=i_src.angle;
		this.scale=i_src.scale;
		this.maxima=i_src.maxima;
    	this.descripter.setValue(i_src.descripter);
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
