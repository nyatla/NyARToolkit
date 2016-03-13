package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;

import jp.nyatla.nyartoolkit.core.kpm.pyramid.GaussianScaleSpacePyramid;

public class DoGPyramid {
	public DoGPyramid(int i_width, int i_height, int i_num_of_octaves, int i_num_scales_per_octaves) {




		this.mNumOctaves = i_num_of_octaves;
		this.mNumScalesPerOctave = i_num_scales_per_octaves - 1;

		// Allocate DoG images 同一サイズのDoG画像ピラミッドを作る
		mImages = new LaplacianImage[this.mNumOctaves * this.mNumScalesPerOctave];
		for (int i = 0; i < this.mNumOctaves; i++) {
			for (int j = 0; j < this.mNumScalesPerOctave; j++) {
				mImages[i * mNumScalesPerOctave + j] = new LaplacianImage(i_width >> i, i_height >> i);// 多分あってるんじゃないか的な
			}
		}
	}

	/**
	 * Compute the Difference-of-Gaussian from a Gaussian Pyramid.
	 */
	public void compute(GaussianScaleSpacePyramid pyramid) {
		for (int i = 0; i < this.mNumOctaves; i++) {
			for (int j = 0; j < this.mNumScalesPerOctave; j++) {
				this.mImages[i * mNumScalesPerOctave + j].difference_image_binomial(pyramid.get(i, j), pyramid.get(i, j + 1));
			}
		}
	}


	/**
	 * Get vector of images.
	 */
	public LaplacianImage[] images() {
		return this.mImages;
	}

	/**
	 * Get a Laplacian image at an index.
	 */
	public LaplacianImage get(int index) {
		return mImages[index];
	}

	/**
	 * Get the number of octaves and scales.
	 */
	public int numOctaves() {
		return this.mNumOctaves;
	}

	public int numScalePerOctave() {
		return this.mNumScalesPerOctave;
	}

	public int size() {
		return this.mImages.length;
	}

	/**
	 * Get the octave from the Laplacian image index.
	 */
	public int octaveFromIndex(int index) {
		return (int)Math.floor((Math.log((mImages[0].getWidth() / this.mImages[index].getWidth())) / Math.log(2))+0.5);

	}

	/**
	 * Get the scale from the Laplacian image index.
	 */
	public int scaleFromIndex(int index) {
		return index % this.mNumScalesPerOctave;
	}

	// DoG images
	private LaplacianImage[] mImages;

	// Number of octaves and scales
	private int mNumOctaves;
	private int mNumScalesPerOctave;


}
