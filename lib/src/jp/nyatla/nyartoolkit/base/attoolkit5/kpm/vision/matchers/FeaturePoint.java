package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

/**
 * Represents a feature point in the visual database.
 */
public class FeaturePoint
{
    public FeaturePoint(double i_x,double i_y,double i_angle,double i_scale,boolean i_maxima)
    {
    	this.x=i_x;
    	this.y=i_y;
    	this.angle=i_angle;
    	this.scale=i_scale;
    	this.maxima=i_maxima;
    }
    public FeaturePoint(double i_x,double i_y,double i_angle,double i_scale,double i_maxima)
    {
    	this(0,0,0,0,true);
    }    	

    public FeaturePoint() {
	}
	public void set(FeaturePoint i_src) {
		// TODO Auto-generated method stub
		this.x=i_src.x;
		this.y=i_src.y;
		this.angle=i_src.angle;
		this.scale=i_src.scale;
		this.maxima=i_src.maxima;
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
