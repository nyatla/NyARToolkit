/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
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
