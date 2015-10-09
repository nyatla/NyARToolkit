package jp.nyatla.nyartoolkit.core.kpm;

/**
 * このクラスは一時的なものです。
 * @author nyatla
 *
 */
class KpmMath
{
	final static public double PI=                 3.1415926535897932384626433832795;
	final static public double ONE_OVER_2PI=       0.159154943091895;
	final static public double SQRT2=              1.41421356237309504880;
    /**
     * @return x*x
     */
    final static public double sqr(double x)
    {
    	return x*x;
    }
    final static public float sqr(float x)
    {
    	return x*x;
    }
    
    /**
     * @return nearest rounding of x
     */
    final static public double round(double x)
    {
    	return Math.floor((x+0.5));
    }
    
    /**
     * Base 2 log
     */
    final static public double log2(double x)
    {
        return Math.log(x) / Math.log(2);
    }

    final static public double max2(double a,double b) {
        return a > b ? a : b;
    }
    final static public float max2(float a,float b) {
        return a > b ? a : b;
    }
    final static public int max2(int a,int b) {
        return a > b ? a : b;
    }
    final static public double min2(double a,double b) {
        return a < b ? a : b;
    }
    final static public int min2(int a,int b) {
        return a < b ? a : b;
    }
    final static public void ZeroVector(float[] x, int num_elements)
    {
    	for(int i=0;i<num_elements;i++){
    		x[i]=0;
    	}
    }
    /**
     * 0.01% error at 1.030
     * 0.10% error at 1.520
     * 1.00% error at 2.330
     * 5.00% error at 3.285
     */
    final static public double fastexp6(double x) {
        return (720+x*(720+x*(360+x*(120+x*(30+x*(6+x))))))*0.0013888888;
    }    
    final static public float fastexp6(float x) {
        return (float)((720+x*(720+x*(360+x*(120+x*(30+x*(6+x))))))*0.0013888888);
    }
    
    

    /**
     * Fit a quatratic to 3 points. The system of equations is:
     *
     * y0 = A*x0^2 + B*x0 + C
     * y1 = A*x1^2 + B*x1 + C
     * y2 = A*x2^2 + B*x2 + C
     *
     * This system of equations is solved for A,B,C.
     *
     * @param[out] A
     * @param[out] B
     * @param[out] C
     * @param[in] p1 2D point 1
     * @param[in] p2 2D point 2
     * @param[in] p3 2D point 3
     * @return True if the quatratic could be fit, otherwise false.
     */
	public static boolean Quadratic3Points(float r[],
                                 float[] p1,
                                 float[] p2,
                                 float[] p3) {
		float d1 = (p3[0]-p2[0])*(p3[0]-p1[0]);
		float d2 = (p1[0]-p2[0])*(p3[0]-p1[0]);
		float d3 = p1[0]-p2[0];
        // If any of the denominators are zero then return FALSE.
		if(d1 == 0 ||
           d2 == 0 ||
           d3 == 0) {
			r[0] = 0;
			r[1] = 0;
			r[2] = 0;
			return false;
		}
		else {
			float a = p1[0]*p1[0];
			float b = p2[0]*p2[0];
			
            // Solve for the coefficients A,B,C
			float A,B;
			r[0] =A= ((p3[1]-p2[1])/d1)-((p1[1]-p2[1])/d2);
			r[1] =B= ((p1[1]-p2[1])+(A*(b-a)))/d3;
			r[2]   = p1[1]-(A*a)-(B*p1[0]);
			return true;
		}
	}
    
    /**
     * Evaluate a quatratic function.
     */
	final public static float QuadraticEval(float[] r,final float x) {
//        return A*x*x + B*x + C;
        return r[0]*x*x + r[1]*x + r[2];
    }
    
    /**
	 * Find the critical point of a quadratic.
     *
     * y = A*x^2 + B*x + C
     *
     * This function finds where "x" where dy/dx = 0.
	 *
     * @param[out] x Parameter of the critical point.
     * @param[in] A
     * @param[in] B
     * @param[in] C
	 * @return True on success.
	 */
	final public static boolean QuadraticCriticalPoint(float[] x, float A, float B, float C) {
		if(A == 0) {
			return false;
		}
		x[0] = -B/(2*A);
		return true;
	}
    
    /**
     * Find the derivate of the quadratic at a point.
     */
    float QuadraticDerivative(float x, float A, float B) {
        return 2*A*x + B;
    }   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
