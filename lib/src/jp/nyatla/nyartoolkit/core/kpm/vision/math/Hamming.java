package jp.nyatla.nyartoolkit.core.kpm.vision.math;

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
    
    /**
     * Hamming distance for 768 bits (96 bytes)
     */
    public static int HammingDistance768(byte[] a,int a_ptr, byte[] b,int b_ptr)
    {
    	//8bitできてるから32bitに直さないと。
        return  HammingDistance32(a[0+a_ptr],  b[0+b_ptr]) +
                HammingDistance32(a[1+a_ptr],  b[1+b_ptr]) +
                HammingDistance32(a[2+a_ptr],  b[2+b_ptr]) +
                HammingDistance32(a[3+a_ptr],  b[3+b_ptr]) +
                HammingDistance32(a[4+a_ptr],  b[4+b_ptr]) +
                HammingDistance32(a[5+a_ptr],  b[5+b_ptr]) +
                HammingDistance32(a[6+a_ptr],  b[6+b_ptr]) +
                HammingDistance32(a[7+a_ptr],  b[7+b_ptr]) +
                HammingDistance32(a[8+a_ptr],  b[8+b_ptr]) +
                HammingDistance32(a[9+a_ptr],  b[9+b_ptr]) +
                HammingDistance32(a[10+a_ptr], b[10+b_ptr]) +
                HammingDistance32(a[11+a_ptr], b[11+b_ptr]) +
                HammingDistance32(a[12+a_ptr], b[12+b_ptr]) +
                HammingDistance32(a[13+a_ptr], b[13+b_ptr]) +
                HammingDistance32(a[14+a_ptr], b[14+b_ptr]) +
                HammingDistance32(a[15+a_ptr], b[15+b_ptr]) +
                HammingDistance32(a[16+a_ptr], b[16+b_ptr]) +
                HammingDistance32(a[17+a_ptr], b[17+b_ptr]) +
                HammingDistance32(a[18+a_ptr], b[18+b_ptr]) +
                HammingDistance32(a[19+a_ptr], b[19+b_ptr]) +
                HammingDistance32(a[20+a_ptr], b[20+b_ptr]) +
                HammingDistance32(a[21+a_ptr], b[21+b_ptr]) +
                HammingDistance32(a[22+a_ptr], b[22+b_ptr]) +
                HammingDistance32(a[23+a_ptr], b[23+b_ptr]);
    }
    
    public static int HammingDistance(byte[] a,int a_ptr, byte[] b,int b_ptr) {
        switch(a.length) {
            case 96:
                return HammingDistance768(a,a_ptr,b,b_ptr);
        };
        return Integer.MAX_VALUE;
    }
}
