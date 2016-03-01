package jp.nyatla.nyartoolkit.core.kpm.vision.math;

/**
 * このクラスは一時的なものです。
 * @author nyatla
 *
 */
public class math_utils
{
	final static public double PI=                 3.1415926535897932384626433832795;
	final static public double ONE_OVER_2PI=       0.159154943091895;
	final static public double SQRT2=              1.41421356237309504880;


    
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
    final static public int max2(int a,int b) {
        return a > b ? a : b;
    }
    final static public double min2(double a,double b) {
        return a < b ? a : b;
    }
    final static public int min2(int a,int b) {
        return a < b ? a : b;
    }
    final static public void ZeroVector(double[] x, int num_elements)
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
	public static boolean Quadratic3Points(double r[],
			double[] p1,
			double[] p2,
			double[] p3) {
		double d1 = (p3[0]-p2[0])*(p3[0]-p1[0]);
		double d2 = (p1[0]-p2[0])*(p3[0]-p1[0]);
		double d3 = p1[0]-p2[0];
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
			double a = p1[0]*p1[0];
			double b = p2[0]*p2[0];
			
            // Solve for the coefficients A,B,C
			double A,B;
			r[0] =A= ((p3[1]-p2[1])/d1)-((p1[1]-p2[1])/d2);
			r[1] =B= ((p1[1]-p2[1])+(A*(b-a)))/d3;
			r[2]   = p1[1]-(A*a)-(B*p1[0]);
			return true;
		}
	}
    
    /**
     * Evaluate a quatratic function.
     */
	final public static double QuadraticEval(double[] r,final double x) {
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
	final public static boolean QuadraticCriticalPoint(double[] x, double A, double B, double C) {
		if(A == 0) {
			return false;
		}
		x[0] = -B/(2*A);
		return true;
	}
    
    /**
     * Find the derivate of the quadratic at a point.
     */
	double QuadraticDerivative(double x, double A, double B) {
        return 2*A*x + B;
    }   
    
    
    /**
     * Create a similarity matrix.
     */
    public static void Similarity(double[] H, double x, double y, double angle, double scale) {
    	double c = (double) (scale*Math.cos(angle));
    	double s = (double) (scale*Math.sin(angle));
        H[0] = c;	H[1] = -s;	H[2] = x;
		H[3] = s;	H[4] = c;	H[5] = y;
		H[6] = 0;	H[7] = 0;	H[8] = 1;
    }
    

   
    /**
     * Multiply an in-homogenous point by a similarity.
     */
    //void MultiplyPointSimilarityInhomogenous(T xp[2], const T H[9], const T x[2]) {
    public static void MultiplyPointSimilarityInhomogenous(double[] xp,int idx, double[] H, double[] x,int idx2) {
        xp[idx+0] = H[0]*x[idx2+0] + H[1]*x[idx2+1] + H[2];
        xp[idx+1] = H[3]*x[idx2+0] + H[4]*x[idx2+1] + H[5];
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
