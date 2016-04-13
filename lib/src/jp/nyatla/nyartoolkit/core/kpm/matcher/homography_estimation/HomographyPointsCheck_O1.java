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
package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;

import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

public class HomographyPointsCheck_O1 extends HomographyPointsCheck
{

    /**
     * Check the geometric consistency between three correspondences.
     */
    private static boolean Homography3PointsGeometricallyConsistent(NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3,NyARDoublePoint2d x1p, NyARDoublePoint2d x2p, NyARDoublePoint2d x3p)
    {
    	//TODO 1は常に+だからbが+であるかだけをチェックすればいい。
    	double a=((x2.x-x1.x)*(x3.y-x1.y)-(x2.y-x1.y)*(x3.x-x1.x));
    	double b=((x2p.x-x1p.x)*(x3p.y-x1p.y)-(x2p.y-x1p.y)*(x3p.x-x1p.x));
        if((a>0) ^ (b>0) == true) {
            return false;
        }
        return true;
    } 
    private final NyARDoublePoint2d[] _x=NyARDoublePoint2d.createArray(4);
    private final NyARDoublePoint2d[] _xp=NyARDoublePoint2d.createArray(4);
    
    @Override
    public void setTestWindow(double i_w,double i_h)
    {
    	NyARDoublePoint2d[] x= this._x;
 		x[0].x = 0;
		x[0].y = 0;
		x[1].x = i_w;
		x[1].y = 0;
		x[2].x = i_w;
		x[2].y = i_h;
		x[3].x = 0;
		x[3].y = i_h; 
    }
    
	/**
	 * 4ポイント限定のHomographyPointsGeometricallyConsistent関数
	 * @param H
	 * @param i_width
	 * @param i_height
	 * @return
	 */
    @Override
    public boolean geometricallyConsistent(HomographyMat H)
    {
    	NyARDoublePoint2d[] x= this._x;
    	NyARDoublePoint2d[] xp=this._xp;  
        
    	//MultiplyPointHomographyInhomogenous
    	for(int i=0;i<4;i++){
    		double sx=x[i].x;
    		double sy=x[i].y;
        	double w = H.m20*sx + H.m21*sy + H.m22;
            xp[i].x = (H.m00*sx + H.m01*sy + H.m02)/w;
            xp[i].y = (H.m10*sx + H.m11*sy + H.m12)/w;
    	}

    	if(!Homography3PointsGeometricallyConsistent(x[0],x[1],x[2],xp[0],xp[1],xp[2])){
            return false;
        }
    	if(!Homography3PointsGeometricallyConsistent(x[1],x[2],x[3],xp[1],xp[2],xp[3])){
            return false;
        }
    	if(!Homography3PointsGeometricallyConsistent(x[2],x[3],x[0],xp[2],xp[3],xp[0])){
            return false;
        }
    	if(!Homography3PointsGeometricallyConsistent(x[3],x[0],x[1],xp[3],xp[0],xp[1])){
            return false;
        }
        return true;
    }

}