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

public class liner_algebr {
	static double Cofactor2x2(double a, double b, double c) {
		return (a * c) - (b * b);
	}
	static double Cofactor2x2(double a, double b, double c, double d)
	{
		return (a * d) - (b * c);
	}	
	public static double Determinant3x3(double[] A) {
		double C1 = Cofactor2x2(A[4], A[5], A[7], A[8]);
        double C2 = Cofactor2x2(A[3], A[5], A[6], A[8]);
        double C3 = Cofactor2x2(A[3], A[4], A[6], A[7]);
		return (A[0] * C1) - (A[1] * C2) + (A[2] * C3);
	}    	
	public static boolean MatrixInverse3x3(double[] B,double[] A, double threshold) {
		double det = Determinant3x3(A);
		
		if(Math.abs(det) <= threshold) {
			return false;
		}
		
		double one_over_det = (double) (1./det);
		
		B[0] = Cofactor2x2(A[4], A[5], A[7], A[8]) * one_over_det;
		B[1] = Cofactor2x2(A[2], A[1], A[8], A[7]) * one_over_det;
		B[2] = Cofactor2x2(A[1], A[2], A[4], A[5]) * one_over_det;
		B[3] = Cofactor2x2(A[5], A[3], A[8], A[6]) * one_over_det;
		B[4] = Cofactor2x2(A[0], A[2], A[6], A[8]) * one_over_det;
		B[5] = Cofactor2x2(A[2], A[0], A[5], A[3]) * one_over_det;
		B[6] = Cofactor2x2(A[3], A[4], A[6], A[7]) * one_over_det;
		B[7] = Cofactor2x2(A[1], A[0], A[7], A[6]) * one_over_det;
		B[8] = Cofactor2x2(A[0], A[1], A[3], A[4]) * one_over_det;
		
		return true;
	}
	
}
