package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;


import jp.nyatla.nyartoolkit.core.kpm.pyramid.GaussianScaleSpacePyramid;


public class OrientationAssignment {


	
	final private int mNumOctaves;
	final private int mNumScalesPerOctave;



	// Factor to expand the Gaussian weighting function. The Gaussian sigma is computed
	// by expanding the feature point scale. The feature point scale represents the isometric
	// size of the feature.
	final private double mGaussianExpansionFactor;

	// Factor to expand the support region. This factor is multipled by the expanded
	// Gaussian sigma. It essentially acts at the "window" to collect gradients in.
	final private double mSupportRegionExpansionFactor;

	// Number of binomial smoothing iterations of the orientation histogram. The histogram
	// is smoothed before find the peaks.
	final private int mNumSmoothingIterations;

	// All the supporting peaks which are X percent of the absolute peak are considered
	// dominant orientations.
	final private double mPeakThreshold;

	// Orientation histogram
	final private BilinearHistogram mHistogram;

	// Vector of gradient images
	final private GradientsImage[] mGradients;

	public OrientationAssignment(int fine_width, int fine_height, int num_octaves, int num_scales_per_octave,
			int num_bins, double gaussian_expansion_factor, double support_region_expansion_factor,
			int num_smoothing_iterations, double peak_threshold) {
		this.mNumOctaves = num_octaves;
		this.mNumScalesPerOctave = num_scales_per_octave;
		this.mGaussianExpansionFactor = gaussian_expansion_factor;
		this.mSupportRegionExpansionFactor = support_region_expansion_factor;
		this.mNumSmoothingIterations = num_smoothing_iterations;
		this.mPeakThreshold = peak_threshold;

		this.mHistogram = new BilinearHistogram(num_bins);

		// Allocate gradient images
		this.mGradients = new GradientsImage[this.mNumOctaves * this.mNumScalesPerOctave];
		for (int i = 0; i < num_octaves; i++) {
			for (int j = 0; j < num_scales_per_octave; j++) {
				// mGradients[i*num_scales_per_octave+j].alloc(IMAGE_F32,
				// fine_width>>i,
				// fine_height>>i,
				// AUTO_STEP,
				// 2);
				// これKpmImageじゃなくて単純なfloatbufferにしようあとで。どうせバッファオーバフローで落ちるから。
				mGradients[i * num_scales_per_octave + j] = new GradientsImage(fine_width >> i, fine_height >> i);

			}
		}
	}




	/**
	 * Compute the gradients given a pyramid.
	 */
	public void computeGradients(GaussianScaleSpacePyramid pyramid) {
		// Loop over each pyramid image and compute the gradients
		for (int i = 0; i < pyramid.images().length; i++) {
			this.mGradients[i].computePolarGradients(pyramid.images()[i]);
		}
	}



	/**
	 * Compute orientations for a keypoint.
	 */
	public int compute(int octave, int scale, double x, double y, double sigma,double[] i_angles)
	{
		//gw_sigma = math_utils.max2(1.f, this.mGaussianExpansionFactor * sigma);
		double gw_sigma = this.mGaussianExpansionFactor * sigma;
		if(gw_sigma<1.0){
			gw_sigma=1.0;
		}

		// Radius of the support region
		double radius = this.mSupportRegionExpansionFactor * gw_sigma;
		double gw_scale = -1.f / (2 * (gw_sigma*gw_sigma));

		// Zero out the orientation histogram
		this.mHistogram.reset();
		int level = octave * mNumScalesPerOctave + scale;
		this.mGradients[level].buildOrientationHistogram(x, y, radius, gw_scale, this.mHistogram);

		// The orientation histogram is smoothed with a Gaussian
		this.mHistogram.smoothOrientationHistogram(mNumSmoothingIterations);
		
		// Find all the peaks.
		return this.mHistogram.findPeak(mPeakThreshold,i_angles);
	}




	
	
}
