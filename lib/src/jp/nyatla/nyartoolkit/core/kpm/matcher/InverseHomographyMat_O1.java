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
package jp.nyatla.nyartoolkit.core.kpm.matcher;



final public class InverseHomographyMat_O1 extends InverseHomographyMat
{

	/**
	 * CheckHomographyHeuristics
	 * Check if a homography is valid based on some heuristics.
	 * @param i_h_inv
	 * inversed homography matrix;
	 * @param refWidth
	 * @param refHeight
	 * @return
	 */
	@Override
	public boolean checkHomographyHeuristics(int refWidth, int refHeight)
	{

		double w;

		w = this.m22;
		double x1x = (this.m02) / w;// XP
		double x1y = (this.m12) / w;// YP
		
		w = this.m20 * refWidth  + this.m22;
		double x2x = (this.m00 * refWidth  + this.m02) / w;// XP
		double x2y = (this.m10 * refWidth  + this.m12) / w;// YP
		
		w = this.m20 * refWidth + this.m21 * refHeight + this.m22;
		double x3x = (this.m00 * refWidth + this.m01 * refHeight + this.m02) / w;// XP
		double x3y = (this.m10 * refWidth + this.m11 * refHeight + this.m12) / w;// YP

		w = this.m21 * refHeight + this.m22;
		double x4x = (this.m01 * refHeight + this.m02) / w;// XP
		double x4y = (this.m11 * refHeight + this.m12) / w;// YP

		


		{	//		if (SmallestTriangleArea(x1, x2, x3, x4) < tr) {
			double tr = refWidth * refHeight * 0.0001f;
			double v12x=x2x-x1x;
			double v12y=x2y-x1y;
			double v13x=x3x-x1x;
			double v13y=x3y-x1y;
			double v14x=x4x-x1x;
			double v14y=x4y-x1y;
			
			//AreaOfTriangle
			double n;
			double a=Math.abs(v13x*v12y - v13y*v12x);
		    n=Math.abs(v14x*v13y - v14y*v13x);
		    if(n<a){a=n;}
		    n=Math.abs(v14x*v12y - v14y*v12x);
		    if(n<a){a=n;}
		    n=Math.abs(v14x*v12y - v14y*v12x);
		    if(n<a){a=n;}
		    n=Math.abs((x4x-x3x)*(x2y-x3y) - (x4y-x3y)*(x2x-x3x));
		    if(n<a){a=n;}
		    a*=0.5;
		    if(a<tr){
		    	return false;
		    }
		}
		{	//if (!QuadrilateralConvex(x1, x2, x3, x4)) {
			int s;
	        s= (((x2x-x1x)*(x3y-x1y)-(x2y-x1y)*(x3x-x1x))>0?1:-1);
	        s+=(((x3x-x2x)*(x4y-x2y)-(x3y-x2y)*(x4x-x2x))>0?1:-1);
	        s+=(((x4x-x3x)*(x1y-x3y)-(x4y-x3y)*(x1x-x3x))>0?1:-1);
	        s+=(((x1x-x4x)*(x2y-x4y)-(x1y-x4y)*(x2x-x4x))>0?1:-1);	        

	        return (Math.abs(s) == 4);
		}
	}
	
}
