package jp.nyatla.nyartoolkit.core.kpm.base.vision.math;

public class rand {
	static final int FAST_RAND_MAX=32767;
    private int _seed;
    public rand(int i_seed)
    {
    	this._seed=i_seed;
    	
    }
    /**
     * Implements a fast random number generator. 
     *
     * http://software.intel.com/en-us/articles/fast-random-number-generator-on-the-intel-pentiumr-4-processor/
     */
    public int fastrandom()
    {
        this._seed = (214013*this._seed+2531011);
        return (this._seed>>16)&0x7FFF;
    }
    
    
    /**
     * Shuffle the elements of an array.
     *
     * @param[in/out] v Array of elements
     * @param[in] pop_size Population size, or size of the array v
     * @param[in] sample_size The first SAMPLE_SIZE samples of v will be shuffled
     * @param[in] seed Seed for random number generator
     */
    public void ArrayShuffle(int[] v,int idx,int pop_size, int sample_size) {
        for(int i = 0; i < sample_size; i++) {
            int k = fastrandom()%pop_size;
            int t=v[idx+i];
//            std::swap(v[i], v[k]);
            v[idx+i]=v[idx+k];
            v[idx+k]=t;
        }
    }
}
