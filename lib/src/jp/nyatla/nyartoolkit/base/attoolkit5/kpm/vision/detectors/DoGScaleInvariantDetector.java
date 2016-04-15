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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.detectors;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.DoGPyramid;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.KpmImage;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.OrientationAssignment;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.OrientationAssignment.FloatVector;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;


public class DoGScaleInvariantDetector {
	final static public int kMaxNumFeaturePoints = 5000;
	final static public int kMaxNumOrientations = 36;

	public static class FeaturePoint {
		public double x, y;
		public double angle;
		public int octave;
		public int scale;
		public double sp_scale;
		public double score;
		public double sigma;
		public double edge_score;

		public FeaturePoint() {
		}

		public FeaturePoint(FeaturePoint i_src) {
			this.set(i_src);
		}

		public void set(FeaturePoint i_src) {
			this.x = i_src.x;
			this.y = i_src.y;
			this.angle = i_src.angle;
			this.octave = i_src.octave;
			this.scale = i_src.scale;
			this.sp_scale = i_src.sp_scale;
			this.score = i_src.score;
			this.sigma = i_src.sigma;
			this.edge_score = i_src.edge_score;
		}

	}; // FeaturePoint

	public static class FeaturePointStack extends NyARObjectStack<FeaturePoint>
			implements Cloneable {
		public FeaturePointStack(int i_length) {
			super(i_length, FeaturePoint.class);
		}

		@Override
		final protected FeaturePoint createElement() {
			return new FeaturePoint();
		}

		/**
		 * DeepCopy これあとで消してね。
		 */
		@Override
		final public Object clone() {
			FeaturePointStack n = new FeaturePointStack(this._items.length);
			for (int i = 0; i < this._length; i++) {
				n.prePush().set(this._items[i]);
			}
			return n;
		}
	}

	public DoGScaleInvariantDetector() {
		this.mWidth = 0;
		this.mHeight = 0;
		this.mNumBucketsX = 10;
		this.mNumBucketsY = 10;
		this.mFindOrientation = true;
		this.mLaplacianThreshold = 0;
		this.mEdgeThreshold = 10;
		this.mMaxSubpixelDistanceSqr = (3 * 3);
		this.setMaxNumFeaturePoints(kMaxNumFeaturePoints);
		this.mOrientations = new double[kMaxNumOrientations];
	}

	/**
	 * Allocate memory.
	 */
	public void alloc(GaussianScaleSpacePyramid pyramid) {
		mLaplacianPyramid.alloc(pyramid);

		mOrientationAssignment.alloc(pyramid.images()[0].getWidth(),
				pyramid.images()[0].getHeight(), pyramid.numOctaves(),
				pyramid.numScalesPerOctave(), kMaxNumOrientations, 3, 1.5f, 5,
				0.8f);

		mWidth = pyramid.images()[0].getWidth();
		mHeight = pyramid.images()[0].getHeight();

		// Allocate bucket container
		mBuckets = createBucketPairArray(mNumBucketsX, mNumBucketsY,300);
	}

	/**
	 * @return Width/Height of configured image
	 */
	public int width() {
		return this.mWidth;
	}

	public int height() {
		return this.mHeight;
	}

	/**
	 * Detect scale-invariant feature points given a pyramid.
	 */
	public void detect(GaussianScaleSpacePyramid pyramid) {
		// ASSERT(pyramid->numOctaves() > 0,
		// "Pyramid does not contain any levels");

		// Compute Laplacian images (DoG)
		// TIMED("DoG Pyramid") {
		mLaplacianPyramid.compute(pyramid);
		// }

		// Detect minima and maximum in Laplacian images
		// TIMED("Non-max suppression") {
		//数が合わない
		extractFeatures(pyramid, this.mLaplacianPyramid);
		// }

		// Sub-pixel refinement
		// TIMED("Subpixel") {
		findSubpixelLocations(pyramid);
		// }

		// Prune features
		// TIMED("pruneFeatures") {
		pruneFeatures();
		// }

		// Compute dominant angles
		// TIMED("Find Orientations") {
		findFeatureOrientations(pyramid);
		// }
	}

	/**
	 * Get/Set the Laplacian absolute threshold.
	 */
	public double laplacianThreshold() {
		return mLaplacianThreshold;
	}

	public void setLaplacianThreshold(double tr) {
		this.mLaplacianThreshold = tr;
	}

	/**
	 * Get/Set the maximum number of feature point.
	 */
	public int maxNumFeaturePoints() {
		return this.mMaxNumFeaturePoints;
	}

	public void setMaxNumFeaturePoints(int n) {
		this.mMaxNumFeaturePoints = n;
		this.mFeaturePoints = new FeaturePointStack(2000);
	}

	/**
	 * Get/Set the edge threshold.
	 */
	public double edgeThreshold() {
		return this.mEdgeThreshold;
	}

	public void setEdgeThreshold(double tr) {
		this.mEdgeThreshold = tr;
	}

	/**
	 * Set/Get find orientations.
	 */
	void setFindOrientation(boolean b) {
		this.mFindOrientation = b;
	}

	public boolean findOrientation() {
		return this.mFindOrientation;
	}

	/**
	 * @return Feature points
	 */
	public FeaturePointStack features() {
		return this.mFeaturePoints;
	}

	/**
	 * @return DoG Pyramid
	 */
	public DoGPyramid dogPyramid() {
		return this.mLaplacianPyramid;
	}

	// Width/Height of configured image
	private int mWidth;
	private int mHeight;

	// Number of buckets in X/Y
	private int mNumBucketsX;
	private int mNumBucketsY;

	private static class BucketPair {
		public double first;
		public int second;
	}

	private static class BucketStack extends NyARObjectStack<BucketPair> {
		public BucketStack(int i_length) {
			super(i_length, BucketPair.class);
		}

		@Override
		protected BucketPair createElement() {
			return new BucketPair();
		}

		// 先頭N個について降順で要素を選択する
		public void partialSort(int n) {
			BucketPair[] items = this._items;
			for (int i = 0; i < n; i++) {
				int max_idx = i;
				double max = items[max_idx].first;
				for (int i2 = i + 1; i2 < this._length; i2++) {
					double test = items[i2].first;
					if (max < test) {
						max = test;
						max_idx = i2;
					}
				}
				if (i != max_idx) {
					BucketPair t = items[i];
					items[i] = items[max_idx];
					items[max_idx] = t;
				}
			}
		}
	}

	public static BucketStack[][] createBucketPairArray(int x, int y,int pair_len) {
		System.out.println("Force set bucketpair size!!");
		BucketStack[][] r = new BucketStack[x][];
		for (int i = 0; i < r.length; i++) {
			r[i] = new BucketStack[y];
			for(int i2=0;i2<y;i2++){
				r[i][i2]=new BucketStack(pair_len);
			}
		}
		return r;
	}

	// Buckets for pruning points
	// std::vector<std::vector<std::vector<std::pair<float, size_t> > > >
	// mBuckets;
	BucketStack[][] mBuckets;

	// True if the orientation should be assigned
	private boolean mFindOrientation;

	// DoG pyramid
	final private DoGPyramid mLaplacianPyramid=new DoGPyramid();

	// Laplacian score threshold
	private double mLaplacianThreshold;

	// Edge threshold
	private double mEdgeThreshold;

	// Vector of extracted feature points
	private FeaturePointStack mFeaturePoints;

	// Maximum number of feature points
	private int mMaxNumFeaturePoints;

	// Maximum update allowed for sub-pixel refinement
	private double mMaxSubpixelDistanceSqr;

	// Orientation assignment
	final private OrientationAssignment mOrientationAssignment=new OrientationAssignment();

	// Vector of orientations. Pre-allocated to the maximum
	// number of orientations per feature point.
	private double[] mOrientations;

	private static double bilinear_interpolation(double[] im, int width,
			int height, int step, double x, double y) {
		int xp, yp;
		int xp_plus_1, yp_plus_1;
		double w0, w1, w2, w3;
		int p0;
		int p1;
		double res;

		// Integer casting and floor should be the same since (x,y) are always
		// positive
		assert (int) Math.floor(x) == (int) x;// ASSERT((int)std::floor(x) ==
												// (int)x,
												// "floor() and cast not the same");
		assert (int) Math.floor(y) == (int) y;// ASSERT((int)std::floor(y) ==
												// (int)y,
												// "floor() and cast not the same");

		// Compute location of 4 neighbor pixels
		xp = (int) x;
		yp = (int) y;
		xp_plus_1 = xp + 1;
		yp_plus_1 = yp + 1;

		// Some sanity checks
		assert yp >= 0 && yp < height;// ASSERT(yp >= 0 && yp < height,
										// "yp out of bounds");
		assert yp_plus_1 >= 0 && yp_plus_1 < height;// ASSERT(yp_plus_1 >= 0 &&
													// yp_plus_1 < height,
													// "yp_plus_1 out of bounds");
		assert xp >= 0 && xp < width;// ASSERT(xp >= 0 && xp < width,
										// "xp out of bounds");
		assert xp_plus_1 >= 0 && xp_plus_1 < width;// ASSERT(xp_plus_1 >= 0 &&
													// xp_plus_1 < width,
													// "xp_plus_1 out of bounds");

		// Pointer to 2 image rows
		p0 = step * yp;// p0 = (const Tin*)((const unsigned char*)im+step*yp);
		p1 = p0 + step;// p1 = (const Tin*)((const unsigned char*)p0+step);

		// Compute weights
		w0 = (xp_plus_1 - x) * (yp_plus_1 - y);
		w1 = (x - xp) * (yp_plus_1 - y);
		w2 = (xp_plus_1 - x) * (y - yp);
		w3 = (x - xp) * (y - yp);

		assert w0 >= 0 && w0 <= 1.0001; // ASSERT(w0 >= 0 && w0 <= 1.0001,
										// "Out of range");
		assert w1 >= 0 && w1 <= 1.0001; // ASSERT(w1 >= 0 && w1 <= 1.0001,
										// "Out of range");
		assert w2 >= 0 && w2 <= 1.0001; // ASSERT(w2 >= 0 && w2 <= 1.0001,
										// "Out of range");
		assert w3 >= 0 && w3 <= 1.0001; // ASSERT(w3 >= 0 && w3 <= 1.0001,
										// "Out of range");
		assert (w0 + w1 + w2 + w3) <= 1.0001;// ASSERT((w0+w1+w2+w3) <= 1.0001,
												// "Out of range");

		// Compute weighted pixel
		res = w0 * im[p0 + xp] + w1 * im[p0 + xp_plus_1] + w2 * im[p1 + xp]
				+ w3 * im[p1 + xp_plus_1];

		return res;
	}

	private static double bilinear_interpolation(KpmImage im, double x, double y) {
		return bilinear_interpolation((double[]) im.getBuffer(), im.getWidth(),
				im.getHeight(),im.getWidth(), x, y);
	}

	/**
	 * Use this function to upsample a point that has been found from a bilinear
	 * downsample pyramid.
	 * 
	 * xp = x*2^n + 2^(n-1) - 0.5 yp = y*2^n + 2^(n-1) - 0.5
	 * 
	 * n: Octave that the point was detected on.
	 * 
	 * @param[out] xp Upsampled x location
	 * @param[out] yp Upsampled y location
	 * @param[in] x X location on detected image
	 * @param[in] y Y location on detected image
	 * @param[in] octave The octave of the detected image
	 */
	private static void bilinear_upsample_point(double[] xyp, double x, double y,
			int octave) {
		double a, b;
		a = (double) Math.pow(2.f, octave - 1) - 0.5f;
		b = (1 << octave);
		xyp[0] = (x * b) + a;
		xyp[1] = (y * b) + a;
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
	private void bilinear_downsample_point(double[] xyp, double x, double y,
			int octave) {
		double a, b;
		a = 1.f / (1 << octave);
		b = 0.5f * a - 0.5f;
		xyp[0] = x * a + b;
		xyp[1] = y * a + b;
	}

	private void bilinear_downsample_point(double[] xysp, double x, double y,
			double s, int octave) {
		double a, b;
		a = 1.f / (1 << octave);
		b = 0.5f * a - 0.5f;
		xysp[0] = x * a + b;
		xysp[1] = y * a + b;
		xysp[2] = s * a;
	}

	/**
	 * Extract the minima/maxima.
	 */
	public void extractFeatures(GaussianScaleSpacePyramid pyramid,
			DoGPyramid laplacian) {

		// Clear old features
		mFeaturePoints.clear();

		double laplacianSqrThreshold = math_utils.sqr(mLaplacianThreshold);

		for (int i = 1; i < mLaplacianPyramid.size() - 1; i++) {
			KpmImage im0 = laplacian.get(i - 1);
			KpmImage im1 = laplacian.get(i);
			KpmImage im2 = laplacian.get(i + 1);
			double[] im0b = (double[]) im0.getBuffer();
			double[] im1b = (double[]) im1.getBuffer();
			double[] im2b = (double[]) im2.getBuffer();

			int octave = laplacian.octaveFromIndex((int) i);
			int scale = laplacian.scaleFromIndex((int) i);


			if (im0.getWidth() == im1.getWidth()
					&& im0.getWidth() == im2.getWidth()) { // All images are the
															// same size
			// ASSERT(im0.height() == im1.height(), "Height is inconsistent");
			// ASSERT(im0.height() == im2.height(), "Height is inconsistent");

				int width_minus_1 = im1.getWidth() - 1;
				int heigh_minus_1 = im1.getHeight() - 1;

				for (int row = 1; row < heigh_minus_1; row++) {
					int im0_ym1 = im0.get(row - 1);
					int im0_y = im0.get(row);
					int im0_yp1 = im0.get(row + 1);

					int im1_ym1 = im1.get(row - 1);
					int im1_y = im1.get(row);
					int im1_yp1 = im1.get(row + 1);

					int im2_ym1 = im2.get(row - 1);
					int im2_y = im2.get(row);
					int im2_yp1 = im2.get(row + 1);

					for (int col = 1; col < width_minus_1; col++) {
						double value = im1b[im1_y + col];

						// Check laplacian score
						if (math_utils.sqr(value) < laplacianSqrThreshold) {
							continue;
						}
						boolean extrema = false;
						if (value > im0b[im0_ym1 + col - 1]
								&& value > im0b[im0_ym1 + col]
								&& value > im0b[im0_ym1 + col + 1]
								&& value > im0b[im0_y + col - 1]
								&& value > im0b[im0_y + col]
								&& value > im0b[im0_y + col + 1]
								&& value > im0b[im0_yp1 + col - 1]
								&& value > im0b[im0_yp1 + col]
								&& value > im0b[im0_yp1 + col + 1] &&
								/* im1 - 8 evaluations */
								value > im1b[im1_ym1 + col - 1]
								&& value > im1b[im1_ym1 + col]
								&& value > im1b[im1_ym1 + col + 1]
								&& value > im1b[im1_y + col - 1]
								&& value > im1b[im1_y + col + 1]
								&& value > im1b[im1_yp1 + col - 1]
								&& value > im1b[im1_yp1 + col]
								&& value > im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value > im2b[im2_ym1 + col - 1]
								&& value > im2b[im2_ym1 + col]
								&& value > im2b[im2_ym1 + col + 1]
								&& value > im2b[im2_y + col - 1]
								&& value > im2b[im2_y + col]
								&& value > im2b[im2_y + col + 1]
								&& value > im2b[im2_yp1 + col - 1]
								&& value > im2b[im2_yp1 + col]
								&& value > im2b[im2_yp1 + col + 1]) { // if(NONMAX_CHECK(>,
																		// value))
																		// { //
																		// strictly
																		// greater
																		// than
							extrema = true;
						} else if (value < im0b[im0_ym1 + col - 1]
								&& value < im0b[im0_ym1 + col]
								&& value < im0b[im0_ym1 + col + 1]
								&& value < im0b[im0_y + col - 1]
								&& value < im0b[im0_y + col]
								&& value < im0b[im0_y + col + 1]
								&& value < im0b[im0_yp1 + col - 1]
								&& value < im0b[im0_yp1 + col]
								&& value < im0b[im0_yp1 + col + 1] &&
								/* im1 - 8 evaluations */
								value < im1b[im1_ym1 + col - 1]
								&& value < im1b[im1_ym1 + col]
								&& value < im1b[im1_ym1 + col + 1]
								&& value < im1b[im1_y + col - 1]
								&& value < im1b[im1_y + col + 1]
								&& value < im1b[im1_yp1 + col - 1]
								&& value < im1b[im1_yp1 + col]
								&& value < im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value < im2b[im2_ym1 + col - 1]
								&& value < im2b[im2_ym1 + col]
								&& value < im2b[im2_ym1 + col + 1]
								&& value < im2b[im2_y + col - 1]
								&& value < im2b[im2_y + col]
								&& value < im2b[im2_y + col + 1]
								&& value < im2b[im2_yp1 + col - 1]
								&& value < im2b[im2_yp1 + col]
								&& value < im2b[im2_yp1 + col + 1]) { // else
																		// if(NONMAX_CHECK(<,
																		// value))
																		// { //
																		// strictly
																		// less
																		// than
							extrema = true;
						}

						if (extrema) {
							FeaturePoint fp = this.mFeaturePoints.prePush();
							fp.octave = octave;
							fp.scale = scale;
							fp.score = value;
							fp.sigma =  pyramid.effectiveSigma(octave,
									scale);
							double[] tmp = new double[2];
							bilinear_upsample_point(tmp, col, row, octave);
							fp.x = tmp[0];
							fp.y = tmp[1];
						}

					}
				}
			} else if (im0.getWidth() == im1.getWidth()
					&& (im1.getWidth() >> 1) == im2.getWidth()) { // 0,1 are the
																	// same
																	// size, 2
																	// is half
																	// size
			// ASSERT(im0.height() == im1.height(), "Height is inconsistent");
			// ASSERT((im1.height()>>1) == im2.height(),
			// "Height is inconsistent");

				int end_x = (int) Math
						.floor(((im2.getWidth() - 1) - 0.5f) * 2.f + 0.5f);
				int end_y = (int) Math
						.floor(((im2.getHeight() - 1) - 0.5f) * 2.f + 0.5f);

				for (int row = 2; row < end_y; row++) {
					int im0_ym1 = im0.get(row - 1);
					int im0_y = im0.get(row);
					int im0_yp1 = im0.get(row + 1);

					int im1_ym1 = im1.get(row - 1);
					int im1_y = im1.get(row);
					int im1_yp1 = im1.get(row + 1);

					for (int col = 2; col < end_x; col++) {

						double value = im1b[im1_y + col];

						// Check laplacian score
						if (math_utils.sqr(value) < laplacianSqrThreshold) {
							continue;
						}

						// Compute downsampled point location
						double ds_x = col * 0.5f - 0.25f;
						double ds_y = row * 0.5f - 0.25f;

						boolean extrema = false;
						if (
						/* im0 - 9 evaluations */
						value > im0b[im0_ym1 + col - 1]
								&& value > im0b[im0_ym1 + col]
								&& value > im0b[im0_ym1 + col + 1]
								&& value > im0b[im0_y + col - 1]
								&& value > im0b[im0_y + col]
								&& value > im0b[im0_y + col + 1]
								&& value > im0b[im0_yp1 + col - 1]
								&& value > im0b[im0_yp1 + col]
								&& value > im0b[im0_yp1 + col + 1]
								&&
								/* im1 - 8 evaluations */
								value > im1b[im1_ym1 + col - 1]
								&& value > im1b[im1_ym1 + col]
								&& value > im1b[im1_ym1 + col + 1]
								&& value > im1b[im1_y + col - 1]
								&& value > im1b[im1_y + col + 1]
								&& value > im1b[im1_yp1 + col - 1]
								&& value > im1b[im1_yp1 + col]
								&& value > im1b[im1_yp1 + col + 1]
								&&
								/* im2 - 9 evaluations */
								value > bilinear_interpolation(im2,
										ds_x - 0.5f, ds_y - 0.5f)
								&& value > bilinear_interpolation(im2, ds_x,
										ds_y - 0.5f)
								&& value > bilinear_interpolation(im2,
										ds_x + 0.5f, ds_y - 0.5f)
								&& value > bilinear_interpolation(im2,
										ds_x - 0.5f, ds_y)
								&& value > bilinear_interpolation(im2, ds_x,
										ds_y)
								&& value > bilinear_interpolation(im2,
										ds_x + 0.5f, ds_y)
								&& value > bilinear_interpolation(im2,
										ds_x - 0.5f, ds_y + 0.5f)
								&& value > bilinear_interpolation(im2, ds_x,
										ds_y + 0.5f)
								&& value > bilinear_interpolation(im2,
										ds_x + 0.5f, ds_y + 0.5f)) { // if(NONMAX_CHECK(>,
																		// value))
																		// { //
																		// strictly
																		// greater
																		// than
							extrema = true;
						} else if (
						/* im0 - 9 evaluations */
						value < im0b[im0_ym1 + col - 1]
								&& value < im0b[im0_ym1 + col]
								&& value < im0b[im0_ym1 + col + 1]
								&& value < im0b[im0_y + col - 1]
								&& value < im0b[im0_y + col]
								&& value < im0b[im0_y + col + 1]
								&& value < im0b[im0_yp1 + col - 1]
								&& value < im0b[im0_yp1 + col]
								&& value < im0b[im0_yp1 + col + 1]
								&&
								/* im1 - 8 evaluations */
								value < im1b[im1_ym1 + col - 1]
								&& value < im1b[im1_ym1 + col]
								&& value < im1b[im1_ym1 + col + 1]
								&& value < im1b[im1_y + col - 1]
								&& value < im1b[im1_y + col + 1]
								&& value < im1b[im1_yp1 + col - 1]
								&& value < im1b[im1_yp1 + col]
								&& value < im1b[im1_yp1 + col + 1]
								&&
								/* im2 - 9 evaluations */
								value < bilinear_interpolation(im2,
										ds_x - 0.5f, ds_y - 0.5f)
								&& value < bilinear_interpolation(im2, ds_x,
										ds_y - 0.5f)
								&& value < bilinear_interpolation(im2,
										ds_x + 0.5f, ds_y - 0.5f)
								&& value < bilinear_interpolation(im2,
										ds_x - 0.5f, ds_y)
								&& value < bilinear_interpolation(im2, ds_x,
										ds_y)
								&& value < bilinear_interpolation(im2,
										ds_x + 0.5f, ds_y)
								&& value < bilinear_interpolation(im2,
										ds_x - 0.5f, ds_y + 0.5f)
								&& value < bilinear_interpolation(im2, ds_x,
										ds_y + 0.5f)
								&& value < bilinear_interpolation(im2,
										ds_x + 0.5f, ds_y + 0.5f)) { // if(NONMAX_CHECK(<,
																		// value))
																		// { //
																		// strictly
																		// less
																		// than
							extrema = true;
						}

						if (extrema) {
							FeaturePoint fp = this.mFeaturePoints.prePush();
							fp.octave = octave;
							fp.scale = scale;
							fp.score = value;
							fp.sigma =  pyramid.effectiveSigma(octave,
									scale);
							double[] tmp = new double[2];
							bilinear_upsample_point(tmp, col, row, octave);
							fp.x = tmp[0];
							fp.y = tmp[1];
						}
					}
				}
			} else if ((im0.getWidth() >> 1) == im1.getWidth()
					&& (im0.getWidth() >> 1) == im2.getWidth()) { // 0 is twice
																	// the size
																	// of 1 and
																	// 2
			// ASSERT((im0.height()>>1) == im1.height(),
			// "Height is inconsistent");
			// ASSERT((im0.height()>>1) == im2.height(),
			// "Height is inconsistent");

				int width_minus_1 = im1.getWidth() - 1;
				int height_minus_1 = im1.getHeight() - 1;

				for (int row = 1; row < height_minus_1; row++) {
					int im1_ym1 = im1.get(row - 1);
					int im1_y = im1.get(row);
					int im1_yp1 = im1.get(row + 1);

					int im2_ym1 = im2.get(row - 1);
					int im2_y = im2.get(row);
					int im2_yp1 = im2.get(row + 1);

					for (int col = 1; col < width_minus_1; col++) {
						double value = im1b[im1_y + col];

						// Check laplacian score
						if (math_utils.sqr(value) < laplacianSqrThreshold) {
							continue;
						}

						double us_x = (col << 1) + 0.5f;
						double us_y = (row << 1) + 0.5f;

						boolean extrema = false;
						if (value > im1b[im1_ym1 + col - 1]
								&& value > im1b[im1_ym1 + col]
								&& value > im1b[im1_ym1 + col + 1]
								&& value > im1b[im1_y + col - 1]
								&& value > im1b[im1_y + col + 1]
								&& value > im1b[im1_yp1 + col - 1]
								&& value > im1b[im1_yp1 + col]
								&& value > im1b[im1_yp1 + col + 1]
								&&
								/* im2 - 9 evaluations */
								value > im2b[im2_ym1 + col - 1]
								&& value > im2b[im2_ym1 + col]
								&& value > im2b[im2_ym1 + col + 1]
								&& value > im2b[im2_y + col - 1]
								&& value > im2b[im2_y + col]
								&& value > im2b[im2_y + col + 1]
								&& value > im2b[im2_yp1 + col - 1]
								&& value > im2b[im2_yp1 + col]
								&& value > im2b[im2_yp1 + col + 1]
								&&
								/* im2 - 9 evaluations */
								value > bilinear_interpolation(im0, us_x - 2.f,
										us_y - 2.f)
								&& value > bilinear_interpolation(im0, us_x,
										us_y - 2.f)
								&& value > bilinear_interpolation(im0,
										us_x + 2.f, us_y - 2.f)
								&& value > bilinear_interpolation(im0,
										us_x - 2.f, us_y)
								&& value > bilinear_interpolation(im0, us_x,
										us_y)
								&& value > bilinear_interpolation(im0,
										us_x + 2.f, us_y)
								&& value > bilinear_interpolation(im0,
										us_x - 2.f, us_y + 2.f)
								&& value > bilinear_interpolation(im0, us_x,
										us_y + 2.f)
								&& value > bilinear_interpolation(im0,
										us_x + 2.f, us_y + 2.f)) { // if(NONMAX_CHECK(>,
																	// value)) {
																	// //
																	// strictly
																	// greater
																	// than
							extrema = true;
						} else if (value < im1b[im1_ym1 + col - 1]
								&& value < im1b[im1_ym1 + col]
								&& value < im1b[im1_ym1 + col + 1]
								&& value < im1b[im1_y + col - 1]
								&& value < im1b[im1_y + col + 1]
								&& value < im1b[im1_yp1 + col - 1]
								&& value < im1b[im1_yp1 + col]
								&& value < im1b[im1_yp1 + col + 1]
								&&
								/* im2 - 9 evaluations */
								value < im2b[im2_ym1 + col - 1]
								&& value < im2b[im2_ym1 + col]
								&& value < im2b[im2_ym1 + col + 1]
								&& value < im2b[im2_y + col - 1]
								&& value < im2b[im2_y + col]
								&& value < im2b[im2_y + col + 1]
								&& value < im2b[im2_yp1 + col - 1]
								&& value < im2b[im2_yp1 + col]
								&& value < im2b[im2_yp1 + col + 1]
								&&
								/* im2 - 9 evaluations */
								value < bilinear_interpolation(im0, us_x - 2.f,
										us_y - 2.f)
								&& value < bilinear_interpolation(im0, us_x,
										us_y - 2.f)
								&& value < bilinear_interpolation(im0,
										us_x + 2.f, us_y - 2.f)
								&& value < bilinear_interpolation(im0,
										us_x - 2.f, us_y)
								&& value < bilinear_interpolation(im0, us_x,
										us_y)
								&& value < bilinear_interpolation(im0,
										us_x + 2.f, us_y)
								&& value < bilinear_interpolation(im0,
										us_x - 2.f, us_y + 2.f)
								&& value < bilinear_interpolation(im0, us_x,
										us_y + 2.f)
								&& value < bilinear_interpolation(im0,
										us_x + 2.f, us_y + 2.f)) { // if(NONMAX_CHECK(<,
																	// value)) {
																	// //
																	// strictly
																	// less than
							extrema = true;
						}

						if (extrema) {
							FeaturePoint fp = this.mFeaturePoints.prePush();
							fp.octave = octave;
							fp.scale = scale;
							fp.score = value;
							fp.sigma =  pyramid.effectiveSigma(octave,
									scale);
							double[] tmp = new double[2];
							bilinear_upsample_point(tmp, col, row, octave);
							fp.x = tmp[0];
							fp.y = tmp[1];
						}
					}
				}
			}
		}
		return;
	}

	/**
	 * Solve a 3x3 symmetric linear system.
	 */
	boolean SolveSymmetricLinearSystem3x3(double[] x, double[] A, double[] b) {
		NyARDoubleMatrix33 m = new NyARDoubleMatrix33();
		m.m00 = A[0];
		m.m01 = A[1];
		m.m02 = A[2];
		m.m10 = A[3];
		m.m11 = A[4];
		m.m12 = A[5];
		m.m20 = A[6];
		m.m21 = A[7];
		m.m22 = A[8];
		if (!m.inverse(m)) {
			return false;
		}
		x[0] = (m.m00 * b[0] + m.m01 * b[1] + m.m02 * b[2]);
		x[1] = (m.m10 * b[0] + m.m11 * b[1] + m.m12 * b[2]);
		x[2] = (m.m20 * b[0] + m.m21 * b[1] + m.m22 * b[2]);
		return true;
	}

	/**
	 * Sub-pixel refinement.
	 */
	public void findSubpixelLocations(GaussianScaleSpacePyramid pyramid) {
		double[] tmp = new double[2];
		double[] A = new double[9];
		double[] b = new double[3];
		double[] u = new double[3];
		int x, y;
		double xp, yp;
		int num_points;
		double laplacianSqrThreshold;
		double hessianThreshold;

		num_points = 0;
		laplacianSqrThreshold = math_utils.sqr(mLaplacianThreshold);
		hessianThreshold = (math_utils.sqr(mEdgeThreshold + 1) / mEdgeThreshold);

		for (int i = 0; i < mFeaturePoints.getLength(); i++) {
			FeaturePoint kp = mFeaturePoints.getItem(i);
			assert kp.scale < mLaplacianPyramid.numScalePerOctave();
			// ASSERT(kp.scale < mLaplacianPyramid.numScalePerOctave(),
			// "Feature point scale is out of bounds");
			int lap_index = kp.octave * mLaplacianPyramid.numScalePerOctave()
					+ kp.scale;
			// Downsample the feature point to the detection octave
			bilinear_downsample_point(tmp, kp.x, kp.y, kp.octave);
			xp = tmp[0];
			yp = tmp[1];
			// Compute the discrete pixel location
			x = (int) (xp + 0.5f);
			y = (int) (yp + 0.5f);

			// Get Laplacian images
			KpmImage lap0 = mLaplacianPyramid.images()[lap_index - 1];
			KpmImage lap1 = mLaplacianPyramid.images()[lap_index];
			KpmImage lap2 = mLaplacianPyramid.images()[lap_index + 1];

			// Compute the Hessian
			if (!ComputeSubpixelHessian(A, b, lap0, lap1, lap2, x, y)) {
				continue;
			}

			// A*u=b
			if (!SolveSymmetricLinearSystem3x3(u, A, b)) {
				continue;
			}

			// If points move too much in the sub-pixel update, then the point
			// probably
			// unstable.
			if (math_utils.sqr(u[0]) + math_utils.sqr(u[1]) > mMaxSubpixelDistanceSqr) {
				continue;
			}

			// Compute the edge score
			if (!ComputeEdgeScore(tmp, A)) {
				continue;
			}
			kp.edge_score = tmp[0];

			// Compute a linear estimate of the intensity
			// ASSERT(kp.score == lap1.get<float>(y)[x],
			// "Score is not consistent with the DoG image");
			double[] lap1_buf = (double[]) lap1.getBuffer();
			kp.score = lap1_buf[lap1.get(y) + x]
					- (b[0] * u[0] + b[1] * u[1] + b[2] * u[2]);

			// Update the location:
			// Apply the update on the downsampled location and then upsample
			// the result.
			// bilinear_upsample_point(kp.x, kp.y, xp+u[0], yp+u[1], kp.octave);
			bilinear_upsample_point(tmp, xp + u[0], yp + u[1], kp.octave);
			kp.x = tmp[0];
			kp.y = tmp[1];

			// Update the scale
			kp.sp_scale = kp.scale + u[2];
			kp.sp_scale = ClipScalar(kp.sp_scale, 0,
					mLaplacianPyramid.numScalePerOctave());

			if (Math.abs(kp.edge_score) < hessianThreshold
					&& math_utils.sqr(kp.score) >= laplacianSqrThreshold
					&& kp.x >= 0
					&& kp.x < mLaplacianPyramid.images()[0].getWidth()
					&& kp.y >= 0
					&& kp.y < mLaplacianPyramid.images()[0].getHeight()) {
				// Update the sigma
				kp.sigma =  pyramid.effectiveSigma(kp.octave,
						kp.sp_scale);
				mFeaturePoints.getItem(num_points++).set(kp);
			}
		}

		mFeaturePoints.setLength(num_points);
	}

	/**
	 * Prune the number of features.
	 */
	void pruneFeatures() {
		if (mFeaturePoints.getLength() <= mMaxNumFeaturePoints) {
			return;
		}

		// ASSERT(mBuckets.size() == mNumBucketsX, "Buckets are not allocated");
		// ASSERT(mBuckets[0].size() == mNumBucketsY,
		// "Buckets are not allocated");

		FeaturePointStack points = new FeaturePointStack(2000);// 適当
		PruneDoGFeatures(mBuckets, points, mFeaturePoints, (int) mNumBucketsX,
				(int) mNumBucketsY, (int) mWidth, (int) mHeight,
				(int) mMaxNumFeaturePoints);
		// オーバフローするから後で直す
		mFeaturePoints.clear();
		for (int i = 0; i < points.getLength(); i++) {
			mFeaturePoints.prePush().set(points.getItem(i));
		}
		assert mFeaturePoints.getLength() <= mMaxNumFeaturePoints;// ,
																	// "Too many feature points");

		// ASSERT(mFeaturePoints.size() <= mMaxNumFeaturePoints,
		// "Too many feature points");
	}

	/**
	 * Find feature orientations.
	 */
	private void findFeatureOrientations(GaussianScaleSpacePyramid pyramid) {
		double[] tmp = new double[3];
		if (!mFindOrientation) {
			for (int i = 0; i < mFeaturePoints.getLength(); i++) {
				mFeaturePoints.getItem(i).angle = 0;
			}
			return;
		}

		int num_angles;
		FeaturePoint[] mTmpOrientatedFeaturePoints = new FeaturePoint[mFeaturePoints
				.getLength() * kMaxNumOrientations];
		int mTmpOrientatedFeaturePoints_n = 0;
		// Compute the gradient pyramid
		mOrientationAssignment.computeGradients(pyramid);

		// Compute an orientation for each feature point
		for (int i = 0; i < mFeaturePoints.getLength(); i++) {
			double x, y, s;

			// Down sample the point to the detected octave
			bilinear_downsample_point(tmp, mFeaturePoints.getItem(i).x,
					mFeaturePoints.getItem(i).y,
					mFeaturePoints.getItem(i).sigma,
					mFeaturePoints.getItem(i).octave);
			x = tmp[0];
			y = tmp[1];
			s = tmp[2];

			// Downsampling the point can cause (x,y) to leave the image bounds
			// by
			// a tiny amount. Here we just clip it to be within the image
			// bounds.
			x = ClipScalar(
					x,
					0,
					pyramid.get(mFeaturePoints.getItem(i).octave, 0).getWidth() - 1);
			y = ClipScalar(y, 0,
					pyramid.get(mFeaturePoints.getItem(i).octave, 0)
							.getHeight() - 1);

			FloatVector f = new FloatVector(mOrientations, 0);
			// Compute dominant orientations
			mOrientationAssignment.compute(f, mFeaturePoints.getItem(i).octave,
					mFeaturePoints.getItem(i).scale, x, y, s);
			num_angles = f.num;
			// Create a feature point for each angle
			for (int j = 0; j < num_angles; j++) {
				// Copy the feature point
				//
				FeaturePoint fp = new FeaturePoint(mFeaturePoints.getItem(i));
				// Update the orientation
				fp.angle = mOrientations[j];
				// Store oriented feature point
				mTmpOrientatedFeaturePoints[mTmpOrientatedFeaturePoints_n] = (fp);
				mTmpOrientatedFeaturePoints_n++;
			}
		}
		// すごく無駄なことしてる。
		mFeaturePoints.clear();
		for (int i = 0; i < mTmpOrientatedFeaturePoints_n; i++) {
			FeaturePoint fp = mFeaturePoints.prePush();
			fp.set(mTmpOrientatedFeaturePoints[i]);
		}

	}

	// private boolean ComputeSubpixelHessian(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)

	private boolean ComputeSubpixelHessian(double[] H, double[] b, KpmImage lap0,
			KpmImage lap1, KpmImage lap2, int x, int y) {

		if (lap0.getWidth() == lap1.getWidth()
				&& lap1.getWidth() == lap2.getWidth()) {
			assert lap0.getHeight() == lap1.getHeight()
					&& lap1.getHeight() == lap2.getHeight();// ,
															// "Width/height are not consistent");
			ComputeSubpixelHessianSameOctave(H, b, lap0, lap1, lap2, x, y);
		} else if ((lap0.getWidth() == lap1.getWidth())
				&& ((lap1.getWidth() >> 1) == lap2.getWidth())) {
			assert (lap0.getHeight() == lap1.getHeight())
					&& ((lap1.getHeight() >> 1) == lap2.getHeight());// ,
																		// "Width/height are not consistent");
			ComputeSubpixelHessianFineOctavePair(H, b, lap0, lap1, lap2, x, y);
		} else if (((lap0.getWidth() >> 1) == lap1.getWidth())
				&& (lap1.getWidth() == lap2.getWidth())) {
			assert ((lap0.getWidth() >> 1) == lap1.getWidth())
					&& (lap1.getWidth() == lap2.getWidth());// ,
															// "Width/height are not consistent");
			ComputeSubpixelHessianCoarseOctavePair(H, b, lap0, lap1, lap2, x, y);
		} else {
			// ASSERT(0, "Image sizes are inconsistent");
			return false;
		}
		return true;
	}

	// private void ComputeSubpixelHessianCoarseOctavePair(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)
	private void ComputeSubpixelHessianCoarseOctavePair(double H[], double b[],
			KpmImage lap0, KpmImage lap1, KpmImage lap2, int x, int y) {
		double val;
		double x_mul_2, y_mul_2;
		double Dx, Dy, Ds;
		double Dxx, Dyy, Dxy;
		double Dss, Dxs, Dys;

		assert (x - 1) >= 0 && (x + 1) < lap1.getWidth();// ASSERT((x-1) >= 0 &&
															// (x+1) <
															// lap1.width(),
															// "x out of bounds");
		assert (y - 1) >= 0 && (y + 1) < lap1.getHeight();// ASSERT((y-1) >= 0
															// && (y+1) <
															// lap1.height(),
															// "y out of bounds");
		assert (lap0.getWidth() >> 1) == lap1.getWidth();// ASSERT((lap0.width()>>1)
															// == lap1.width(),
															// "Image dimensions inconsistent");
		assert (lap0.getWidth() >> 1) == lap2.getWidth();// ASSERT((lap0.width()>>1)
															// == lap2.width(),
															// "Image dimensions inconsistent");
		assert (lap0.getHeight() >> 1) == lap1.getHeight();// ASSERT((lap0.height()>>1)
															// == lap1.height(),
															// "Image dimensions inconsistent");
		assert (lap0.getHeight() >> 1) == lap2.getHeight();// ASSERT((lap0.height()>>1)
															// == lap2.height(),
															// "Image dimensions inconsistent");

		// const float* lap1_p = &lap1.get<float>(y)[x];;
		// const float* lap2_pm1 = &lap2.get<float>(y-1)[x];
		// const float* lap2_p = &lap2.get<float>(y)[x];
		// const float* lap2_pp1 = &lap2.get<float>(y+1)[x];
		int lap1_p = lap1.get(y) + x;
		int lap2_pm1 = lap2.get(y - 1) + x;
		int lap2_p = lap2.get(y) + x;
		int lap2_pp1 = lap2.get(y + 1) + x;

		double[] tmp = new double[5];
		// Upsample the point to the higher octave
		bilinear_upsample_point(tmp, x, y, 1);
		x_mul_2 = tmp[0];
		y_mul_2 = tmp[1];
		// Compute spatial derivatives
		// ComputeSubpixelDerivatives(Dx, Dy, Dxx, Dyy, Dxy, lap1, x, y);
		ComputeSubpixelDerivatives(tmp, lap1, x, y);
		Dx = tmp[0];
		Dy = tmp[1];
		Dxx = tmp[2];
		Dyy = tmp[3];
		Dxy = tmp[4];
		// Interpolate the VALUE at the finer octave
		val = bilinear_interpolation(lap0, x_mul_2, y_mul_2);
		double[] lap2buf = (double[]) lap2.getBuffer();
		double[] lap1buf = (double[]) lap1.getBuffer();

		Ds = 0.5f * (lap2buf[lap2_p + 0] - val);
		Dss = val + (-2.f * lap1buf[lap1_p + 0]) + lap2buf[lap2_p + 0];
		Dxs = 0.25f * ((bilinear_interpolation(lap0, x_mul_2 - 2, y_mul_2) + lap2buf[lap2_p + 1]) - (bilinear_interpolation(
				lap0, x_mul_2 + 2, y_mul_2) + lap2buf[lap2_p - 1]));
		Dys = 0.25f * ((bilinear_interpolation(lap0, x_mul_2, y_mul_2 - 2) + lap2buf[lap2_pp1 + 0]) - (bilinear_interpolation(
				lap0, x_mul_2, y_mul_2 + 2) + lap2buf[lap2_pm1 + 0]));

		// H
		H[0] = Dxx;
		H[1] = Dxy;
		H[2] = Dxs;
		H[3] = Dxy;
		H[4] = Dyy;
		H[5] = Dys;
		H[6] = Dxs;
		H[7] = Dys;
		H[8] = Dss;

		// h
		b[0] = -Dx;
		b[1] = -Dy;
		b[2] = -Ds;
	}

	// inline void ComputeSubpixelHessianFineOctavePair(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)
	private void ComputeSubpixelHessianFineOctavePair(double[] H, double[] b,
			KpmImage lap0, KpmImage lap1, KpmImage lap2, int x, int y) {
		double x_div_2, y_div_2;
		double val;
		double Dx, Dy, Ds;
		double Dxx, Dyy, Dxy;
		double Dss, Dxs, Dys;

		assert (x - 1) >= 0 && (x + 1) < lap1.getWidth();// ASSERT((x-1) >= 0 &&
															// (x+1) <
															// lap1.width(),
															// "x out of bounds");
		assert (y - 1) >= 0 && (y + 1) < lap1.getHeight();// ASSERT((y-1) >= 0
															// && (y+1) <
															// lap1.height(),
															// "y out of bounds");
		assert lap0.getWidth() == lap1.getWidth();// ASSERT(lap0.width() ==
													// lap1.width(),
													// "Image dimensions inconsistent");
		assert (lap0.getWidth() >> 1) == lap2.getWidth();// ASSERT((lap0.width()>>1)
															// == lap2.width(),
															// "Image dimensions inconsistent");
		assert lap0.getHeight() == lap1.getHeight();// ASSERT(lap0.height() ==
													// lap1.height(),
													// "Image dimensions inconsistent");
		assert (lap0.getHeight() >> 1) == lap2.getHeight();// ASSERT((lap0.height()>>1)
															// == lap2.height(),
															// "Image dimensions inconsistent");

		int lap0_pm1 = lap0.get(y - 1) + x;// const float* lap0_pm1 =
											// &lap0.get<float>(y-1)[x];
		int lap0_p = lap0.get(y) + x;// const float* lap0_p =
										// &lap0.get<float>(y)[x];
		int lap0_pp1 = lap0.get(y + 1) + x;// const float* lap0_pp1 =
											// &lap0.get<float>(y+1)[x];
		int lap1_p = lap1.get(y) + x;// const float* lap1_p =
										// &lap1.get<float>(y)[x];
		double[] tmp = new double[5];
		bilinear_downsample_point(tmp, x, y, 1);
		x_div_2 = tmp[0];
		y_div_2 = tmp[1];
		assert x_div_2 - 0.5f >= 0;// ASSERT(x_div_2-0.5f >= 0,
									// "x_div_2 out of bounds out of bounds for interpolation");
		assert y_div_2 - 0.5f >= 0;// ASSERT(y_div_2-0.5f >= 0,
									// "y_div_2 out of bounds out of bounds for interpolation");
		assert x_div_2 + 0.5f < lap2.getWidth();// ASSERT(x_div_2+0.5f <
												// lap2.width(),
												// "x_div_2 out of bounds out of bounds for interpolation");
		assert y_div_2 + 0.5f < lap2.getHeight();// ,
													// "y_div_2 out of bounds out of bounds for interpolation");

		// Compute spatial derivatives
		// ComputeSubpixelDerivatives(Dx, Dy, Dxx, Dyy, Dxy, lap1, x, y);
		ComputeSubpixelDerivatives(tmp, lap1, x, y);
		Dx = tmp[0];
		Dy = tmp[1];
		Dxx = tmp[2];
		Dyy = tmp[3];
		Dxy = tmp[4];
		// Interpolate the VALUE at the coarser octave
		val = bilinear_interpolation(lap2, x_div_2, y_div_2);

		// Ds = 0.5f*(val - lap0_p[0]);
		// Dss = lap0_p[0] + (-2.f*lap1_p[0]) + val;
		// Dxs = 0.25f*((lap0_p[-1] + bilinear_interpolation(lap2, x_div_2+.5f,
		// y_div_2)) -
		// (lap0_p[ 1] + bilinear_interpolation(lap2, x_div_2-.5f, y_div_2)));
		// Dys = 0.25f*((lap0_pm1[0] + bilinear_interpolation(lap2, x_div_2,
		// y_div_2+.5f)) -
		// (lap0_pp1[0] + bilinear_interpolation(lap2, x_div_2, y_div_2-.5f)));
		double[] lap0_buf = (double[]) lap0.getBuffer();
		double[] lap1_buf = (double[]) lap1.getBuffer();

		Ds = 0.5f * (val - lap0_buf[lap0_p + 0]);
		Dss = lap0_buf[lap0_p + 0] + (-2.f * lap1_buf[lap1_p + 0]) + val;
		Dxs = 0.25f * ((lap0_buf[lap0_p - 1] + bilinear_interpolation(lap2,
				x_div_2 + .5f, y_div_2)) - (lap0_buf[lap0_p + 1] + bilinear_interpolation(
				lap2, x_div_2 - .5f, y_div_2)));
		Dys = 0.25f * ((lap0_buf[lap0_pm1 + 0] + bilinear_interpolation(lap2,
				x_div_2, y_div_2 + .5f)) - (lap0_buf[lap0_pp1 + 0] + bilinear_interpolation(
				lap2, x_div_2, y_div_2 - .5f)));

		// H
		H[0] = Dxx;
		H[1] = Dxy;
		H[2] = Dxs;
		H[3] = Dxy;
		H[4] = Dyy;
		H[5] = Dys;
		H[6] = Dxs;
		H[7] = Dys;
		H[8] = Dss;

		// b
		b[0] = -Dx;
		b[1] = -Dy;
		b[2] = -Ds;
	}

	// private void ComputeSubpixelDerivatives(
	// float& Dx, float& Dy,
	// float& Dxx,float& Dyy,float& Dxy,
	// const Image& im,
	// int x,int y)
	private void ComputeSubpixelDerivatives(double[] dn, KpmImage im, int x,
			int y) {
		// Sanity checks
		// ASSERT((x-1) >= 0 && (x+1) < im.width(), "x out of bounds");
		// ASSERT((y-1) >= 0 && (y+1) < im.height(), "y out of bounds");
		assert (x - 1) >= 0 && (x + 1) < im.getWidth();// , "x out of bounds");
		assert (y - 1) >= 0 && (y + 1) < im.getHeight();// , "y out of bounds");

		// const float* pm1 = &im.get<float>(y-1)[x];
		// const float* p = &im.get<float>(y)[x];
		// const float* pp1 = &im.get<float>(y+1)[x];
		double[] im_buf = (double[]) im.getBuffer();
		int pm1 = im.get(y - 1) + x;
		int p = im.get(y) + x;
		int pp1 = im.get(y + 1) + x;

		// Dx = 0.5f*(p[1]-p[-1]);
		// Dy = 0.5f*(pp1[0]-pm1[0]);
		// Dxx = p[-1] + (-2.f*p[0]) + p[1];
		// Dyy = pm1[0] + (-2.f*p[0]) + pp1[0];
		// Dxy = 0.25f*((pm1[-1] + pp1[1]) - (pm1[1] + pp1[-1]));
		dn[0] = 0.5f * (im_buf[p + 1] - im_buf[p - 1]);
		dn[1] = 0.5f * (im_buf[pp1 + 0] - im_buf[pm1 + 0]);
		dn[2] = im_buf[p - 1] + (-2.f * im_buf[p + 0]) + im_buf[p + 1];
		dn[3] = im_buf[pm1 + 0] + (-2.f * im_buf[p + 0]) + im_buf[pp1 + 0];
		dn[4] = 0.25f * ((im_buf[pm1 - 1] + im_buf[pp1 + 1]) - (im_buf[pm1 + 1] + im_buf[pp1 - 1]));
	}

	// private void ComputeSubpixelHessianSameOctave(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)
	private void ComputeSubpixelHessianSameOctave(double[] H, double[] b,
			KpmImage lap0, KpmImage lap1, KpmImage lap2, int x, int y) {
		double Dx, Dy, Ds;
		double Dxx, Dyy, Dxy;
		double Dss, Dxs, Dys;

		assert (x - 1) >= 0 && (x + 1) < lap1.getWidth();// ASSERT((x-1) >= 0 &&
															// (x+1) <
															// lap1.width(),
															// "x out of bounds");
		assert (y - 1) >= 0 && (y + 1) < lap1.getHeight();// ASSERT((y-1) >= 0
															// && (y+1) <
															// lap1.height(),
															// "y out of bounds");
		assert lap0.getWidth() == lap1.getWidth(); // ASSERT(lap0.width() ==
													// lap1.width(),
													// "Image dimensions inconsistent");
		assert lap0.getWidth() == lap2.getWidth();// ASSERT(lap0.width() ==
													// lap2.width(),
													// "Image dimensions inconsistent");
		assert lap0.getHeight() == lap1.getHeight();// ASSERT(lap0.height() ==
													// lap1.height(),
													// "Image dimensions inconsistent");
		assert lap0.getHeight() == lap2.getHeight();// ASSERT(lap0.height() ==
													// lap2.height(),
													// "Image dimensions inconsistent");

		int lap0_pm1 = lap0.get(y - 1) + x;// const float* lap0_pm1 =
											// &lap0.get<float>(y-1)[x];
		int lap0_p = lap0.get(y) + x;// const float* lap0_p =
										// &lap0.get<float>(y)[x];
		int lap0_pp1 = lap0.get(y + 1) + x;// const float* lap0_pp1 =
											// &lap0.get<float>(y+1)[x];

		int lap1_p = lap1.get(y) + x;// const float* lap1_p =
										// &lap1.get<float>(y)[x];
		int lap2_pm1 = lap2.get(y - 1) + x;// const float* lap2_pm1 =
											// &lap2.get<float>(y-1)[x];
		int lap2_p = lap2.get(y) + x;// const float* lap2_p =
										// &lap2.get<float>(y)[x];
		int lap2_pp1 = lap2.get(y + 1) + x;// const float* lap2_pp1 =
											// &lap2.get<float>(y+1)[x];

		double[] tmp = new double[5];
		// Compute spatial derivatives
		// ComputeSubpixelDerivatives(Dx, Dy, Dxx, Dyy, Dxy, lap1, x, y);
		ComputeSubpixelDerivatives(tmp, lap1, x, y);
		Dx = tmp[0];
		Dy = tmp[1];
		Dxx = tmp[2];
		Dyy = tmp[3];
		Dxy = tmp[4];
		double[] lap0buf = (double[]) lap0.getBuffer();
		double[] lap1buf = (double[]) lap1.getBuffer();
		double[] lap2buf = (double[]) lap2.getBuffer();
		// Compute scale derivates
		Ds = 0.5f * (lap2buf[lap2_p + 0] - lap0buf[lap0_p + 0]);
		Dss = lap0buf[lap0_p + 0] + (-2.f * lap1buf[lap1_p + 0])
				+ lap2buf[lap2_p + 0];
		Dxs = 0.25f * ((lap0buf[lap0_p - 1] - lap0buf[lap0_p + 1]) + (-lap2buf[lap2_p - 1] + lap2buf[lap2_p + 1]));
		Dys = 0.25f * ((lap0buf[lap0_pm1 + 0] - lap0buf[lap0_pp1 + 0]) + (-lap2buf[lap2_pm1 + 0] + lap2buf[lap2_pp1 + 0]));

		// H
		H[0] = Dxx;
		H[1] = Dxy;
		H[2] = Dxs;
		H[3] = Dxy;
		H[4] = Dyy;
		H[5] = Dys;
		H[6] = Dxs;
		H[7] = Dys;
		H[8] = Dss;

		// b
		b[0] = -Dx;
		b[1] = -Dy;
		b[2] = -Ds;
	}

	// inline bool ComputeEdgeScore(float& score, const float H[9]) {
	private boolean ComputeEdgeScore(double[] score, double[] H) {
		double det;

		double Dxx = H[0];
		double Dyy = H[4];
		double Dxy = H[1];

		det = (Dxx * Dyy) - math_utils.sqr(Dxy);

		// The determinant cannot be zero
		if (det == 0) {
			return false;
		}

		// Compute a score based on the local curvature
		score[0] = math_utils.sqr(Dxx + Dyy) / det;

		return true;
	}

	double ClipScalar(double x, double min, double max) {
		if (x < min) {
			x = min;
		} else if (x > max) {
			x = max;
		}
		return x;
	}

	void PruneDoGFeatures(BucketStack[][] buckets, FeaturePointStack outPoints,
			FeaturePointStack inPoints, int num_buckets_X, int num_buckets_Y,
			int width, int height, int max_points) {

		int num_buckets = num_buckets_X * num_buckets_Y;
		int num_points_per_bucket = max_points / num_buckets;
		int dx = (int) Math.ceil( width / num_buckets_X);
		int dy = (int) Math.ceil( height / num_buckets_Y);

		//
		// Clear the previous state
		//
		outPoints.clear();
		// outPoints.reserv(max_points);
		for (int i = 0; i < buckets.length; i++) {
			for (int j = 0; j < buckets[i].length; j++) {
				buckets[i][j].clear();
			}
		}

		//
		// Insert each features into a bucket
		//
		for (int i = 0; i < inPoints.getLength(); i++) {
			FeaturePoint p = inPoints.getItem(i);
			int binX = (int) (p.x / dx);
			int binY = (int) (p.y / dy);
			// buckets[binX][binY].push_back(std::make_pair(std::abs(p.score),
			// i));
			BucketPair b = buckets[binX][binY].prePush();
			b.first = Math.abs(p.score);
			b.second = i;
		}

		//
		// Do a partial sort on the first N points of each bucket
		//
		for (int i = 0; i < buckets.length; i++) {
			for (int j = 0; j < buckets[i].length; j++) {
				BucketStack bucket = buckets[i][j];
				int n = Math.min(bucket.getLength(), num_points_per_bucket);
				if (n == 0) {
					continue;
				}
				// ここはfirstの大きい方からn個を選択するコードで多分良い
				// std::nth_element(bucket.begin(),
				// bucket.begin()+n,
				// bucket.end(), std::greater<std::pair<float, size_t> >());
				//
				// DEBUG_BLOCK(
				// if(n > bucket.size()) {
				// ASSERT(bucket[0].first >= bucket[n].first,
				// "nth_element failed");
				// }
				// )
				// for(int k = 0; k < n; k++) {
				// outPoints.push_back(inPoints[bucket[k].second]);
				// }
				bucket.partialSort(n);

				for (int k = 0; k < n; k++) {
					FeaturePoint p = outPoints.prePush();
					p.set(inPoints.getItem(bucket.getItem(k).second));
				}
			}
		}
	}
}
