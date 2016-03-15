package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.utils;

/**
 * 速度重視の関数
 */
public class FastMath {
	public static double fastAtan2( double y, double x )
	{
		if ( x == 0.0f )
		{
			if ( y > 0.0f ){
				return (Math.PI/2);
			}
			if ( y < 0.0f ){
				return -(Math.PI/2);
			}
			return 0.0f;
		}
		double atan;
		double z = y/x;
		if (z*z < 1.0f )
		{
			atan = z/(1.0f + 0.28f*z*z);
			if ( x < 0.0f )
			{
				if ( y < 0.0f ){
					return atan - Math.PI;
				}
				return atan + Math.PI;
			}
		}
		else
		{
			atan = (Math.PI/2) - z/(z*z + 0.28f);
			if ( y < 0.0f ){
				return atan - Math.PI;
			}
		}
		return atan;
	}	
    /**
     * 0.01% error at 1.030
     * 0.10% error at 1.520
     * 1.00% error at 2.330
     * 5.00% error at 3.285
     */
    public static double fastexp6(double x) {
        return (720+x*(720+x*(360+x*(120+x*(30+x*(6+x))))))*0.0013888888;
    }		
}
