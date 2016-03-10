package jp.nyatla.nyartoolkit.core.kpm.matcher;



import jp.nyatla.nyartoolkit.core.kpm.vision.math.geometry;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class homography
{
	
    /**
     * Multiply an in-homogenous point by a similarity.
     * H[9] この関数は実装済みだからあとで消す。
     */
    public static void MultiplyPointHomographyInhomogenous(NyARDoublePoint2d v, NyARDoubleMatrix33 H, double x, double y) {
    	double w = H.m20*x + H.m21*y + H.m22;
        v.x = (H.m00*x + H.m01*y + H.m02)/w;//XP
        v.y = (H.m10*x + H.m11*y + H.m12)/w;//YP
    }
    /**
     * Multiply an in-homogenous point by a similarity.
     */
    public static void MultiplyPointHomographyInhomogenous(NyARDoublePoint2d xp,NyARDoubleMatrix33 H, NyARDoublePoint2d[] x,int x_idx) {
    	
//    	double w = H[6]*x[x_idx].x + H[7]*x[x_idx].y + H[8];
 //       xp.x = (H[0]*x[x_idx].x + H[1]*x[x_idx].y + H[2])/w;
 //       xp.y = (H[3]*x[x_idx].x + H[4]*x[x_idx].y + H[5])/w;
    	double w = H.m20*x[x_idx].x + H.m21*x[x_idx].y + H.m22;
        xp.x = (H.m00*x[x_idx].x + H.m01*x[x_idx].y + H.m02)/w;
        xp.y = (H.m10*x[x_idx].x + H.m11*x[x_idx].y + H.m12)/w;
    }



//    boolean HomographyPointsGeometricallyConsistent(const T H[9], const T* x, int size) {
    public static boolean HomographyPointsGeometricallyConsistent(NyARDoubleMatrix33 H, NyARDoublePoint2d[] x,int i_x_ptr, int size) {
    	NyARDoublePoint2d xp1=new NyARDoublePoint2d();
    	NyARDoublePoint2d xp2=new NyARDoublePoint2d();
    	NyARDoublePoint2d xp3=new NyARDoublePoint2d();
    	NyARDoublePoint2d first_xp1=new NyARDoublePoint2d();
    	NyARDoublePoint2d first_xp2=new NyARDoublePoint2d();
        
        // We need at least 3 points
        if(size < 2) {
            return true;
        }
        
        int x1_ptr = i_x_ptr;
        int x2_ptr = i_x_ptr+1;
        int x3_ptr = i_x_ptr+2;
        
        NyARDoublePoint2d xp1_ptr = xp1;
        NyARDoublePoint2d xp2_ptr = xp2;
        NyARDoublePoint2d xp3_ptr = xp3;
        
        //
        // Check the first 3 points
        //
        
        MultiplyPointHomographyInhomogenous(xp1, H,x, x1_ptr);
        MultiplyPointHomographyInhomogenous(xp2, H,x, x2_ptr);
        MultiplyPointHomographyInhomogenous(xp3, H,x, x3_ptr);
        
        first_xp1.setValue(xp1);//indexing.CopyVector2(first_xp1,0, xp1,0);
        first_xp2.setValue(xp2);//indexing.CopyVector2(first_xp2,0, xp2,0);
//    	public boolean Homography4PointsGeometricallyConsistent(float[] x1, float[] x2, float[] x3, float[] x4,float[] x1p,float[] x2p,float[] x3p,float[] x4p) {
        
        
        if(!geometry.Homography3PointsGeometricallyConsistent(
        		x[x1_ptr],x[x2_ptr],x[x3_ptr],
        		xp1_ptr, xp2_ptr, xp3_ptr)) {
            return false;
        }
        
        //
        // Check the remaining points
        //
        
        for(int i = 3; i < size; i++) {
            x1_ptr += 1;
            x2_ptr += 1;
            x3_ptr += 1;
            
            MultiplyPointHomographyInhomogenous(xp1_ptr, H, x,x3_ptr);
            
            NyARDoublePoint2d tmp_ptr = xp1_ptr;
            xp1_ptr = xp2_ptr;
            xp2_ptr = xp3_ptr;
            xp3_ptr = tmp_ptr;

            if(!geometry.Homography3PointsGeometricallyConsistent(
            		x[x1_ptr],x[x2_ptr],x[x3_ptr],
            		xp1_ptr, xp2_ptr, xp3_ptr)) {
                return false;
            }
        }
        
        //
        // Check the last 3 points
        //
        
        if(!geometry.Homography3PointsGeometricallyConsistent(
        		x[x2_ptr],x[x3_ptr],x[0],
        		xp2_ptr, xp3_ptr, first_xp1)) {
            return false;
        }
        if(!geometry.Homography3PointsGeometricallyConsistent(
        		x[x3_ptr],x[0],x[2],
        		xp3_ptr, first_xp1, first_xp2)) {
            return false;
        }
        
        return true;
    }
}
