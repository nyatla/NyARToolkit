package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter;

/**
 * Represents a feature point in the visual database.
 */
public class FreakFeaturePoint
{
    final public LongDescripter descripter=new LongDescripter(96*8);

//	public FreakFeaturePoint(double i_x,double i_y,double i_angle,double i_scale,boolean i_maxima,byte[] i_descripter)
//    {
//    	this.x=i_x;
//    	this.y=i_y;
//    	this.angle=i_angle;
//    	this.scale=i_scale;
//    	this.maxima=i_maxima;
//    	this.descripter.setValueBe(i_descripter);
//    }   	

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
