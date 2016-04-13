/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math;

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
     * @return x*x
     */

    final static public double sqr(double x)
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
    
//    /**
//     * Create a similarity matrix.
//     */
//    template<typename T>
//    inline void Similarity2x2(T S[4], T angle, T scale) {
//        T c = scale*std::cos(angle);
//        T s = scale*std::sin(angle);
//        S[0] = c; S[1] = -s;
//        S[2] = s; S[3] = c;
//    }
//    
//    template<typename T>
//    inline void CreateSimilarityTransformation2d(T H[9], T cx, T cy, T angle, T scale) {
//        T c = scale*std::cos(angle);
//        T s = scale*std::sin(angle);
//        
//        H[0] = c;	H[1] = -s;
//        H[3] = s;	H[4] = c;
//        
//        H[2] = -(H[0]*cx + H[1]*cy) + cx;
//        H[5] = -(H[3]*cx + H[4]*cy) + cy;
//        
//        H[6] = 0;   H[7] = 0;   H[8] = 1;
//    }
//    
    /**
     * Multiply an in-homogenous point by a similarity.
     */
    //void MultiplyPointSimilarityInhomogenous(T xp[2], const T H[9], const T x[2]) {
    public static void MultiplyPointSimilarityInhomogenous(double[] xp,int idx, double[] H, double[] x,int idx2) {
        xp[idx+0] = H[0]*x[idx2+0] + H[1]*x[idx2+1] + H[2];
        xp[idx+1] = H[3]*x[idx2+0] + H[4]*x[idx2+1] + H[5];
    }
//
//    /**
//     * Multiply an in-homogenous point by a similarity.
//     */
//    template<typename T>
//    inline void MultiplyPointHomographyInhomogenous(T& xp, T& yp, const T H[9], T x, T y) {
//        float w = H[6]*x + H[7]*y + H[8];
//        xp = (H[0]*x + H[1]*y + H[2])/w;
//        yp = (H[3]*x + H[4]*y + H[5])/w;
//    }
//    
//    /**
//     * Multiply an in-homogenous point by a similarity.
//     */
//    template<typename T>
//    inline void MultiplyPointHomographyInhomogenous(T xp[2], const T H[9], const T x[2]) {
//        float w = H[6]*x[0] + H[7]*x[1] + H[8];
//        xp[0] = (H[0]*x[0] + H[1]*x[1] + H[2])/w;
//        xp[1] = (H[3]*x[0] + H[4]*x[1] + H[5])/w;
//    }
//    
//    template<typename T>
//    inline bool HomographyPointsGeometricallyConsistent(const T H[9], const T* x, int size) {
//        T xp1[2];
//        T xp2[2];
//        T xp3[2];
//        T first_xp1[2];
//        T first_xp2[2];
//        
//        // We need at least 3 points
//        if(size < 2) {
//            return true;
//        }
//        
//        const T* x1_ptr = x;
//        const T* x2_ptr = x+2;
//        const T* x3_ptr = x+4;
//        
//        T* xp1_ptr = xp1;
//        T* xp2_ptr = xp2;
//        T* xp3_ptr = xp3;
//        
//        //
//        // Check the first 3 points
//        //
//        
//        MultiplyPointHomographyInhomogenous(xp1, H, x1_ptr);
//        MultiplyPointHomographyInhomogenous(xp2, H, x2_ptr);
//        MultiplyPointHomographyInhomogenous(xp3, H, x3_ptr);
//        
//        CopyVector2(first_xp1, xp1);
//        CopyVector2(first_xp2, xp2);
//        
//        if(!Homography3PointsGeometricallyConsistent(x1_ptr, x2_ptr, x3_ptr, xp1_ptr, xp2_ptr, xp3_ptr)) {
//            return false;
//        }
//        
//        //
//        // Check the remaining points
//        //
//        
//        for(int i = 3; i < size; i++) {
//            x1_ptr += 2;
//            x2_ptr += 2;
//            x3_ptr += 2;
//            
//            MultiplyPointHomographyInhomogenous(xp1_ptr, H, x3_ptr);
//            
//            T* tmp_ptr = xp1_ptr;
//            xp1_ptr = xp2_ptr;
//            xp2_ptr = xp3_ptr;
//            xp3_ptr = tmp_ptr;
//            
//            if(!Homography3PointsGeometricallyConsistent(x1_ptr, x2_ptr, x3_ptr, xp1_ptr, xp2_ptr, xp3_ptr)) {
//                return false;
//            }
//        }
//        
//        //
//        // Check the last 3 points
//        //
//        
//        if(!Homography3PointsGeometricallyConsistent(x2_ptr, x3_ptr, x, xp2_ptr, xp3_ptr, first_xp1)) {
//            return false;
//        }
//        if(!Homography3PointsGeometricallyConsistent(x3_ptr, x, x+2, xp3_ptr, first_xp1, first_xp2)) {
//            return false;
//        }
//        
//        return true;
//    }
//    
//    /**
//     * Normalize a homography such that H(3,3) = 1.
//     */
//    template<typename T>
//    inline void NormalizeHomography(T H[9]) {
//        T one_over = 1./H[8];
//        H[0] *= one_over;  H[1] *= one_over;  H[2] *= one_over;
//        H[3] *= one_over;  H[4] *= one_over;  H[5] *= one_over;
//        H[6] *= one_over;  H[7] *= one_over;  H[8] *= one_over;
//    }
//    
//    /**
//     * Update a homography with an incremental translation update.
//     *
//     * H*(I+T) where T = [1 0 tx; 0 1 ty; 0 0 1].
//     */
//    template<typename T>
//	inline void UpdateHomographyTranslation(T H[9], T tx, T ty) {
//        H[2] += H[0]*tx + H[1]*ty;
//        H[5] += H[3]*tx + H[4]*ty;
//        H[8] += H[6]*tx + H[7]*ty;
//    }
//    
//    /**
//	 * Symmetrically scale a homography.
//	 *
//	 * y = Hp*x where Hp = inv(S)*H*S
//	 */
//	template<typename T>
//	inline void ScaleHomography(T H[9], T s) {
//		H[2] /= s;
//		H[5] /= s;
//		H[6] *= s;
//		H[7] *= s;
//	}    
//    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
