package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;

import jp.nyatla.nyartoolkit.core.kpm.KpmImage;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.GaussianScaleSpacePyramid;

public class DoGPyramid {
	public DoGPyramid(GaussianScaleSpacePyramid pyramid) {
		assert pyramid.size() > 0;

		int width = pyramid.get(0, 0).getWidth();
		int height = pyramid.get(0, 0).getHeight();

		this.mNumOctaves = pyramid.numOctaves();
		this.mNumScalesPerOctave = pyramid.numScalesPerOctave() - 1;

		// Allocate DoG images 同一サイズのDoG画像ピラミッドを作る
		mImages = new KpmImage[this.mNumOctaves * this.mNumScalesPerOctave];
		for (int i = 0; i < this.mNumOctaves; i++) {
			for (int j = 0; j < this.mNumScalesPerOctave; j++) {
				mImages[i * mNumScalesPerOctave + j] = new KpmImage(width >> i, height >> i);// 多分あってるんじゃないか的な
			}
		}
	}

	/**
	 * Compute the Difference-of-Gaussian from a Gaussian Pyramid.
	 */
	public void compute(GaussianScaleSpacePyramid pyramid) {
		for (int i = 0; i < this.mNumOctaves; i++) {
			for (int j = 0; j < this.mNumScalesPerOctave; j++) {
				difference_image_binomial(this.get(i, j), pyramid.get(i, j), pyramid.get(i, j + 1));
			}
		}
	}

	/**
	 * Get a Laplacian image at a level in the pyramid.
	 */
	public KpmImage get(int octave, int scale) {
		return mImages[octave * mNumScalesPerOctave + scale];
	}

	/**
	 * Get vector of images.
	 */
	public KpmImage[] images() {
		return this.mImages;
	}

	/**
	 * Get a Laplacian image at an index.
	 */
	public KpmImage get(int index) {
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
	private KpmImage[] mImages;

	// Number of octaves and scales
	private int mNumOctaves;
	private int mNumScalesPerOctave;

	/**
	 * Compute the difference image.
	 * 
	 * d = im1 - im2
	 */
	private void difference_image_binomial(KpmImage d, KpmImage im1, KpmImage im2) {
		// Compute diff
		double[] p0 = (double[]) d.getBuffer();
		double[] p1 = (double[]) im1.getBuffer();
		double[] p2 = (double[]) im2.getBuffer();
		for (int i = im1.getWidth()*im1.getHeight()-1; i>=0 ; i--) {
			p0[i] = p1[i] - p2[i];
		}
		return;
	}
}
