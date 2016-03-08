package jp.nyatla.nyartoolkit.core.kpm.freak;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter768;

/**
 * Represents a feature point in the visual database.
 */
public class FreakFeaturePoint
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
     * The (x,y) location of the center of the feature.
     */
    public double x, y;

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
