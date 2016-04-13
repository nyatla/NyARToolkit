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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Point2d;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Utils;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.match.indexing;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.geometry;

public class homography
{
	
    /**
     * Multiply an in-homogenous point by a similarity.
     * H[9]
     */
//    public static void MultiplyPointHomographyInhomogenous(float xp, float yp, float[] H, float x, float y) {
    public static void MultiplyPointHomographyInhomogenous(double[] v, double[] H, double x, double y) {
    	double w = H[6]*x + H[7]*y + H[8];
        v[0] = (H[0]*x + H[1]*y + H[2])/w;//XP
        v[1] = (H[3]*x + H[4]*y + H[5])/w;//YP
    }
    /**
     * Multiply an in-homogenous point by a similarity.
     */
    public static void MultiplyPointHomographyInhomogenous(Point2d xp,double[] H, Point2d[] x,int x_idx) {
    	double w = H[6]*x[x_idx].x + H[7]*x[x_idx].y + H[8];
        xp.x = (H[0]*x[x_idx].x + H[1]*x[x_idx].y + H[2])/w;
        xp.y = (H[3]*x[x_idx].x + H[4]*x[x_idx].y + H[5])/w;
    }
    public static void MultiplyPointHomographyInhomogenous(Point2d xp,double[] H, double x,double y) {
    	double w = H[6]*x + H[7]*y + H[8];
        xp.x = (H[0]*x + H[1]*y + H[2])/w;
        xp.y = (H[3]*x + H[4]*y + H[5])/w;
    }    
    public static void MultiplyPointHomographyInhomogenous(Point2d xp,double[] H, Point2d x) {
    	double w = H[6]*x.x + H[7]*x.y + H[8];
        xp.x = (H[0]*x.x + H[1]*x.y + H[2])/w;
        xp.y = (H[3]*x.x + H[4]*x.y + H[5])/w;
    }

//    boolean HomographyPointsGeometricallyConsistent(const T H[9], const T* x, int size) {
    public static boolean HomographyPointsGeometricallyConsistent(double[] H, Point2d[] x,int i_x_ptr, int size) {
        Point2d xp1=new Point2d();
        Point2d xp2=new Point2d();
        Point2d xp3=new Point2d();
        Point2d first_xp1=new Point2d();
        Point2d first_xp2=new Point2d();
        
        // We need at least 3 points
        if(size < 2) {
            return true;
        }
        
        int x1_ptr = i_x_ptr;
        int x2_ptr = i_x_ptr+1;
        int x3_ptr = i_x_ptr+2;
        
        Point2d xp1_ptr = xp1;
        Point2d xp2_ptr = xp2;
        Point2d xp3_ptr = xp3;
        
        //
        // Check the first 3 points
        //
        
        MultiplyPointHomographyInhomogenous(xp1, H,x, x1_ptr);
        MultiplyPointHomographyInhomogenous(xp2, H,x, x2_ptr);
        MultiplyPointHomographyInhomogenous(xp3, H,x, x3_ptr);
        
        first_xp1.set(xp1);//indexing.CopyVector2(first_xp1,0, xp1,0);
        first_xp2.set(xp2);//indexing.CopyVector2(first_xp2,0, xp2,0);
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
            
            Point2d tmp_ptr = xp1_ptr;
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
