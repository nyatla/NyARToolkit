package jp.nyatla.nyartoolkit.core.kpm.vision.match;

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
    public static void CopyVector2(float[] dst,int i_dst_idx, float[] src,int i_src_idx) {
    	CopyVector(dst,i_dst_idx,src,i_src_idx,2);
    }    
    public static void CopyVector(float[] dst,int i_dst_idx, float[] src,int i_src_idx,int i_len) {
    	for(int i=0;i<i_len;i++){
    		dst[i_dst_idx+i]=src[i_src_idx+i];
    	}
    }    
}
