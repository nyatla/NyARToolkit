package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

/**
 * Represents a feature point in the visual database.
 */
public class FreakFeaturePoint
{
    final public byte[] descripter=new byte[96];

	public FreakFeaturePoint(double i_x,double i_y,double i_angle,double i_scale,boolean i_maxima,byte[] i_descripter)
    {
    	this.x=i_x;
    	this.y=i_y;
    	this.angle=i_angle;
    	this.scale=i_scale;
    	this.maxima=i_maxima;
		System.arraycopy(i_descripter, 0, this.descripter, 0, this.descripter.length);
    }   	

    public FreakFeaturePoint() {
	}
	public void set(FreakFeaturePoint i_src) {
		// TODO Auto-generated method stub
		this.x=i_src.x;
		this.y=i_src.y;
		this.angle=i_src.angle;
		this.scale=i_src.scale;
		this.maxima=i_src.maxima;
		System.arraycopy(i_src.descripter, 0, this.descripter, 0, this.descripter.length);
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
