package jp.nyatla.nyartoolkit.core.kpm.pyramid;

import jp.nyatla.nyartoolkit.core.kpm.KpmImage;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

public class GaussianScaleSpacePyramid {
	/**
	 * Number of octaves
	 */
	final protected int mNumOctaves;
	final protected int mNumScalesPerOctave;
	/**
	 * 2^(1/(mNumScalesPerOctave-1))
	 */
	final private double mK;
	/**
	 * 1/log(k) precomputed for efficiency
	 */
	final private double mOneOverLogK;

	public GaussianScaleSpacePyramid(int i_num_octaves, int i_num_scales_per_octaves)
	{
		this.mNumOctaves = i_num_octaves;
		this.mNumScalesPerOctave = i_num_scales_per_octaves;
		this.mK = Math.pow(2.0, 1.0 / (this.mNumScalesPerOctave - 1));
		this.mOneOverLogK = 1.0 / Math.log(this.mK);
		return;
	}
	public int numOctaves() {
		return mNumOctaves;
	}
	/**
	 * Get the number of octaves and scales.
	 */
	public int numScalesPerOctave() {
		return mNumScalesPerOctave;
	}
	/**
	 * Get the constant k-factor.
	 */
	public double kfactor() {
		return mK;
	}

	/**
	 * Get the effective sigma given the octave and sub-pixel scale.
	 */
	public double effectiveSigma(int octave, double scale) {
		assert scale >= 0;// "Scale must be positive";
		assert scale < this.mNumScalesPerOctave;// "Scale must be less than number of scale per octave";
		return Math.pow(this.mK, scale) * (1 << octave);
	}

	final static public class LocateResult {
		public int octave;
		public int scale;
	};

	/**
	 * Locate a SIGMA on the pyramid.
	 */
	public void locate(double sigma, LocateResult result) {
		// octave = floor(log2(s))
		int octave = (int) Math.floor(math_utils.log2(sigma));
		// scale = logk(s/2^octave)
		// Here we ROUND the scale to an integer value
		double fscale = Math.log(sigma / (double) (1 << octave))
				* this.mOneOverLogK;
		int scale = (int)Math.floor((fscale+0.5));
		
		// The last scale in an octave has the same sigma as the first scale
		// of the next octave. We prefer coarser octaves for efficiency.
		if (scale == this.mNumScalesPerOctave - 1) {
			// If this octave is out of range, then it will be clipped to
			// be in range on the next step below.
			octave = octave + 1;
			scale = 0;
		}

		// Clip the octave/scale to be in range of the pyramid
		if (octave < 0) {
			result.octave = 0;
			result.scale = 0;
		} else if (octave >= this.mNumOctaves) {
			result.octave = this.mNumOctaves - 1;
			result.scale = this.mNumScalesPerOctave - 1;
		}else{
			result.octave=octave;
			result.scale=scale;
		}
		assert (octave >= 0);// , "Octave must be positive");
		assert (octave < this.mNumOctaves);// ,
											// "Octave must be less than number of octaves");
		assert (scale >= 0);// , "Scale must be positive");
		assert (scale < this.mNumScalesPerOctave);// ,
													// "Scale must be less than number of scale per octave");
	}

	protected KpmImage[] mPyramid;

	/**
	 * @return Get the vector of images.
	 */
	public KpmImage[] images() {
		return this.mPyramid;
	}

	/**
	 * Get a pyramid image.
	 */
	public KpmImage image(int octave, int scale) {
		assert (octave < mNumOctaves);// "Octave out of range");
		assert (scale < mNumScalesPerOctave);// , "Scale out of range");
		return this.mPyramid[octave * mNumScalesPerOctave + scale];
	}

	public int size() {
		return this.mPyramid.length;
	}

	public KpmImage get(int octave, int scale) {
		// ASSERT(octave < mNumOctaves, "Octave out of range");
		// ASSERT(scale < mNumScalesPerOctave, "Scale out of range");
		return this.mPyramid[octave * this.mNumScalesPerOctave + scale];
	}


    /**
     * Use this function to downsample a point to an octave that was found from
     * a bilinear downsampled pyramid.
     *
     * @param[out] xp Downsampled x location
     * @param[out] yp Downsampled y location
     * @param[in] x X location on fine image
     * @param[in] y Y location on fine image
     * @param[in] octave The octave to downsample (x,y) to
     */
    public static void bilinear_downsample_point(NyARDoublePoint2d ret,
                                          double x,
                                          double y,
                                          int octave) {
    	double a, b;
        a = 1.f/(1<<octave);
        b = 0.5f*a-0.5f;
        ret.x = x*a+b;
        ret.y = y*a+b;
    }
}