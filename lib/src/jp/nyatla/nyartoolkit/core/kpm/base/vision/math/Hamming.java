package jp.nyatla.nyartoolkit.core.kpm.base.vision.math;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;

public class Hamming {
    /**
     * Hamming distance for 32 bits.
     */
    private static int HammingDistance32(int a,int b)
    {
        final int m1  = 0x55555555; // 0101...
        final int m2  = 0x33333333; // 00110011..
        final int m4  = 0x0f0f0f0f; // 4 zeros,  4 ones
        final int h01 = 0x01010101; // the sum of 256 to the power of 0,1,2,...
        
        int x;
        
        x = a^b;
        x -= (x >> 1) & m1;             // put count of each 2 bits into those 2 bits
        x = (x & m2) + ((x >> 2) & m2); // put count of each 4 bits into those 4 bits
        x = (x + (x >> 4)) & m4;        // put count of each 8 bits into those 8 bits
        
        return (x * h01) >> 24;         // returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ...
    }
    private static int HammingDistance32(byte[] a,int a_idx,byte[] b,int b_idx)
    {
    	int ai=((0xff&a[a_idx+0])<<24)|((0xff&a[a_idx+1])<<16)|((0xff&a[a_idx+2])<<8)|((0xff&a[a_idx+3]));
    	int bi=((0xff&b[b_idx+0])<<24)|((0xff&b[b_idx+1])<<16)|((0xff&b[b_idx+2])<<8)|((0xff&b[b_idx+3]));
    	return HammingDistance32(ai,bi);
    }
    
    
    /**
     * Hamming distance for 768 bits (96 bytes)
     */
    public static int HammingDistance768(byte[] a,int a_ptr, byte[] b,int b_ptr)
    {
    	int dist=0;
    	for(int i=0;i<24;i++){
    		dist+=HammingDistance32(a,i*4+a_ptr,b,i*4+b_ptr);
    	}
    	return dist;
    }
    
    public static int HammingDistance(int i_length,byte[] a,int a_ptr, byte[] b,int b_ptr) {
        switch(i_length) {
            case 96:
                return HammingDistance768(a,a_ptr,b,b_ptr);
        };
        throw new NyARRuntimeException();
    }
}
