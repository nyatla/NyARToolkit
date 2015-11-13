package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

/**
 * Represents a feature point in the visual database.
 */
public class FeaturePoint
{
    public FeaturePoint(float i_x,float i_y,float i_angle,float i_scale,boolean i_maxima)
    {
    	this.x=i_x;
    	this.y=i_y;
    	this.angle=i_angle;
    	this.scale=i_scale;
    	this.maxima=i_maxima;
    }
    public FeaturePoint(float i_x,float i_y,float i_angle,float i_scale,float i_maxima)
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
    public float x, y;

    /**
     * The orientation of the feature in the range [0,2*pi)
     */
    public float angle;

    /**
     * The radius (scale) of the feature in the image.
     */
    public float scale;

    /**
     * TRUE if this is maxima, FALSE if a minima.
     */
    public boolean maxima;

      
}
