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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.utils;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.homography_estimation.RobustHomography;

public class parcial_sort {
    /**
     * Perform a partial sort of an array. This algorithm is based on
     * Niklaus Wirth's k-smallest.
     *
     * @param[in/out] a array of elements
     * @param[in] n size of a
     * @param[in] k kth element starting from 1, i.e. 1st smallest, 2nd smallest, etc.
     */
	double PartialSort(double a[], int n, int k) {
		int i, j, l, m, k_minus_1;
		double x;
        
//        ASSERT(n > 0, "n must be positive");
//        ASSERT(k > 0, "k must be positive");
        
        k_minus_1 = k-1;
		
		l=0 ; m=n-1;
		while(l<m) {
			x=a[k_minus_1];
			i=l;
			j=m;
			do {
				while(a[i]<x) i++;
				while(x<a[j]) j--;
				if(i<=j) {
//					std::swap(a[i], a[j]);
					double t=a[i];
					a[i]=a[j];
					a[j]=t;
					i++; j--;
				}
			} while (i<=j);
			if(j<k_minus_1) l=i;
			if(k_minus_1<i) m=j;
		}
		return a[k_minus_1];
	}
    /**
     * Find the median of an array.
     */
	double FastMedian(double a[], int n) {
		return PartialSort(a, n, (((n&1)==1)?((n)/2):(((n)/2)-1)));
    	
//		return PartialSort(a, n, (((n)&1)?((n)/2):(((n)/2)-1)));
	}
	
    static RobustHomography.CostPair PartialSort(RobustHomography.CostPair a[], int n, int k) {
		int i, j, l, m, k_minus_1;
		RobustHomography.CostPair x;
        
//        ASSERT(n > 0, "n must be positive");
//        ASSERT(k > 0, "k must be positive");
        
        k_minus_1 = k-1;
		
		l=0 ; m=n-1;
		while(l<m) {
			x=a[k_minus_1];
			i=l;
			j=m;
			do {
				while(RobustHomography.CostPair.operator_lt(a[i],x)) i++;
				while(RobustHomography.CostPair.operator_lt(x,a[j])) j--;
				if(i<=j) {
					//std::swap(a[i], a[j]); // FIXME: 
					RobustHomography.CostPair t=a[i];
					a[i]=a[j];
					a[j]=t;
					i++; j--;
				}
			} while (i<=j);
			if(j<k_minus_1) l=i;
			if(k_minus_1<i) m=j;
		}
		return a[k_minus_1];
	}    
    public static RobustHomography.CostPair FastMedian(RobustHomography.CostPair a[], int n)
    {
		return PartialSort(a, n, (((n&1)==1)?((n)/2):(((n)/2)-1)));
	}    
    
}
