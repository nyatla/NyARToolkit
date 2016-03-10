package jp.nyatla.nyartoolkit.core.kpm.vision.utils;

import jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation.RobustHomography;

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
