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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.match;

public class indexing {
    /**
     * Create a sequential vector {x0+0, x0+1, x0+2, ...}
     */
    public static void SequentialVector(int[] x, int i_ptr,int n, int x0) {
        if(n < 1) {
            return;
        }
        x[0+i_ptr] = x0;
        for(int i = 1; i < n; i++) {
            x[i+i_ptr] = x[i+i_ptr-1]+1;
        }
    }
    public static void CopyVector2(double[] dst,int i_dst_idx, double[] src,int i_src_idx) {
    	CopyVector(dst,i_dst_idx,src,i_src_idx,2);
    }    
    public static void CopyVector(double[] dst,int i_dst_idx, double[] src,int i_src_idx,int i_len) {
    	for(int i=0;i<i_len;i++){
    		dst[i_dst_idx+i]=src[i_src_idx+i];
    	}
    }  
	public static double min4(double a1, double a2, double a3, double a4)
	{
		double r=a1;
		if(r>a2){
			r=a2;
		}
		if(r>a3){
			r=a3;
		}
		if(r>a4){
			r=a4;
		}
		return r;
	}
}
