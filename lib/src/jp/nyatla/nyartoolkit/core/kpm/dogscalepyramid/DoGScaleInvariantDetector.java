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
package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;


import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.utils.AreaBuckit;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.GaussianScaleSpacePyramid;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class DoGScaleInvariantDetector {
	final static public int kMaxNumFeaturePoints = 5000;
	final static public int kMaxNumOrientations = 36;

	// DoG pyramid
	final private DoGPyramid mLaplacianPyramid;

	/** Laplacian score threshold */
	final private double mLaplacianThreshold;

	/** Edge threshold */
	final private double mEdgeThreshold;

	// number of orientations per feature point.
	final private double[] mOrientations;



	// Maximum update allowed for sub-pixel refinement
	final private double mMaxSubpixelDistanceSqr;

	// Orientation assignment
	final private OrientationAssignment mOrientationAssignment;

	/**
	 * Buckets for pruning points std::vector<std::vector<std::vector<std::pair<float, size_t> > > >
	 */
	final private AreaBuckit mBuckets;

	public DoGScaleInvariantDetector(int i_width,int i_height,int i_octerv,int i_num_of_scale_of_octerv, double i_LaplacianThreshold,
			double i_EdgeThreshold, int i_MaxNumFeaturePoints) {

		this.mLaplacianThreshold = i_LaplacianThreshold;
		this.mEdgeThreshold = i_EdgeThreshold;
		this.mMaxSubpixelDistanceSqr = (3 * 3);
		this.mOrientations = new double[kMaxNumOrientations];
		this.mLaplacianPyramid = new DoGPyramid(i_width,i_height,i_octerv,i_num_of_scale_of_octerv-1);
		this.mOrientationAssignment = new OrientationAssignment(i_width,i_height,i_octerv, i_num_of_scale_of_octerv,kMaxNumOrientations, 3, 1.5f, 5, 0.8f);
		this.mBuckets = new AreaBuckit(i_width,i_height,10,10,i_MaxNumFeaturePoints);
		this._tmp_fps=new DogFeaturePointStack(kMaxNumFeaturePoints);
	}

	final private DogFeaturePointStack _tmp_fps;

	/**
	 * Detect scale-invariant feature points given a pyramid.
	 * @param _i_dog_feature_points
	 * 検出したDOG特徴点
	 */
	public void detect(GaussianScaleSpacePyramid i_pyramid,DogFeaturePointStack i_dog_feature_points)
	{
		//clean up 1st feature stack
		DogFeaturePointStack tmp_fp=this._tmp_fps;
		tmp_fp.clear();

		// Compute Laplacian images (DoG)
		this.mLaplacianPyramid.compute(i_pyramid);

		// Detect minima and maximum in Laplacian images
		this.extractFeatures(i_pyramid, this.mLaplacianPyramid,tmp_fp);

		// Sub-pixel refinement
		this.findSubpixelLocations(i_pyramid,tmp_fp);

		// Compute the gradient pyramid		
		this.mOrientationAssignment.computeGradients(i_pyramid);		
		
		AreaBuckit abuckit=this.mBuckets;

		
		if (tmp_fp.getLength() <= abuckit._buckit.length) {
			//特徴点の数が要求数以下なら全てのポイントを使う。
			for (int i = 0; i < tmp_fp.getLength(); i++)
			{
				this.addFeatureOrientations(i_pyramid,tmp_fp.getItem(i),i_dog_feature_points);				
			}
		}else{
			//特徴点を選別(Prune features)
		
			// Clear the previous state
			abuckit.clear();
			
			// Insert each features into a bucket
			for (int i = 0; i < tmp_fp.getLength(); i++) {
				DogFeaturePoint p = tmp_fp.getItem(i);
				abuckit.put(p.x,p.y,i,Math.abs(p.score));
			}
			// Compute an orientation for each feature point
			for (int i = 0; i < abuckit._buckit.length; i++)
			{
				if(abuckit._buckit[i].first==0){
					continue;
				}
				this.addFeatureOrientations(i_pyramid,tmp_fp.getItem(abuckit._buckit[i].second),i_dog_feature_points);				
			}
		}

		return;
	}


	/**
	 * Use this function to upsample a point that has been found from a bilinear downsample pyramid.
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
	private static void bilinear_upsample_point(double[] xyp, double x, double y, int octave) {
		double a, b;
		a = (double) Math.pow(2.f, octave - 1) - 0.5f;
		b = (1 << octave);
		xyp[0] = (x * b) + a;
		xyp[1] = (y * b) + a;
	}

	/**
	 * Use this function to downsample a point to an octave that was found from a bilinear downsampled pyramid.
	 * 
	 * @param[out] xp Downsampled x location
	 * @param[out] yp Downsampled y location
	 * @param[in] x X location on fine image
	 * @param[in] y Y location on fine image
	 * @param[in] octave The octave to downsample (x,y) to
	 */
	private void bilinear_downsample_point(double[] xyp, double x, double y, int octave) {
		double a, b;
		a = 1.f / (1 << octave);
		b = 0.5f * a - 0.5f;
		xyp[0] = x * a + b;
		xyp[1] = y * a + b;
	}

	private void bilinear_downsample_point(double[] xysp, double x, double y, double s, int octave) {
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
	private void extractFeatures(GaussianScaleSpacePyramid pyramid, DoGPyramid laplacian,DogFeaturePointStack i_dog_fp)
	{
		double laplacianSqrThreshold = (this.mLaplacianThreshold * this.mLaplacianThreshold);

		for (int i = 1; i < mLaplacianPyramid.size() - 1; i++) {
			LaplacianImage im0 = laplacian.get(i - 1);
			LaplacianImage im1 = laplacian.get(i);
			LaplacianImage im2 = laplacian.get(i + 1);
			double[] im0b = (double[]) im0.getBuffer();
			double[] im1b = (double[]) im1.getBuffer();
			double[] im2b = (double[]) im2.getBuffer();

			int octave = laplacian.octaveFromIndex((int) i);
			int scale = laplacian.scaleFromIndex((int) i);

			if (im0.getWidth() == im1.getWidth() && im0.getWidth() == im2.getWidth()) { // All images are the
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
						if ((value * value) < laplacianSqrThreshold) {
							continue;
						}
						boolean extrema = false;
						if (value > im0b[im0_ym1 + col - 1] && value > im0b[im0_ym1 + col]
								&& value > im0b[im0_ym1 + col + 1] && value > im0b[im0_y + col - 1]
								&& value > im0b[im0_y + col] && value > im0b[im0_y + col + 1]
								&& value > im0b[im0_yp1 + col - 1] && value > im0b[im0_yp1 + col]
								&& value > im0b[im0_yp1 + col + 1] &&
								/* im1 - 8 evaluations */
								value > im1b[im1_ym1 + col - 1] && value > im1b[im1_ym1 + col]
								&& value > im1b[im1_ym1 + col + 1] && value > im1b[im1_y + col - 1]
								&& value > im1b[im1_y + col + 1] && value > im1b[im1_yp1 + col - 1]
								&& value > im1b[im1_yp1 + col] && value > im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value > im2b[im2_ym1 + col - 1] && value > im2b[im2_ym1 + col]
								&& value > im2b[im2_ym1 + col + 1] && value > im2b[im2_y + col - 1]
								&& value > im2b[im2_y + col] && value > im2b[im2_y + col + 1]
								&& value > im2b[im2_yp1 + col - 1] && value > im2b[im2_yp1 + col]
								&& value > im2b[im2_yp1 + col + 1])
						{
							extrema = true;
						} else if (value < im0b[im0_ym1 + col - 1] && value < im0b[im0_ym1 + col]
								&& value < im0b[im0_ym1 + col + 1] && value < im0b[im0_y + col - 1]
								&& value < im0b[im0_y + col] && value < im0b[im0_y + col + 1]
								&& value < im0b[im0_yp1 + col - 1] && value < im0b[im0_yp1 + col]
								&& value < im0b[im0_yp1 + col + 1] &&
								/* im1 - 8 evaluations */
								value < im1b[im1_ym1 + col - 1] && value < im1b[im1_ym1 + col]
								&& value < im1b[im1_ym1 + col + 1] && value < im1b[im1_y + col - 1]
								&& value < im1b[im1_y + col + 1] && value < im1b[im1_yp1 + col - 1]
								&& value < im1b[im1_yp1 + col] && value < im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value < im2b[im2_ym1 + col - 1] && value < im2b[im2_ym1 + col]
								&& value < im2b[im2_ym1 + col + 1] && value < im2b[im2_y + col - 1]
								&& value < im2b[im2_y + col] && value < im2b[im2_y + col + 1]
								&& value < im2b[im2_yp1 + col - 1] && value < im2b[im2_yp1 + col]
								&& value < im2b[im2_yp1 + col + 1])
						{ 
							extrema = true;
						}

						if (extrema) {
							DogFeaturePoint fp = i_dog_fp.prePush();
							if(fp==null){
								prepush_warning();
								break;
							}
							fp.octave = octave;
							fp.scale = scale;
							fp.score = value;
							fp.sigma = pyramid.effectiveSigma(octave, scale);
							double[] tmp = new double[2];
							bilinear_upsample_point(tmp, col, row, octave);
							fp.x = tmp[0];
							fp.y = tmp[1];
						}

					}
				}
			} else if (im0.getWidth() == im1.getWidth() && (im1.getWidth() >> 1) == im2.getWidth()) {

				int end_x = (int) Math.floor(((im2.getWidth() - 1) - 0.5f) * 2.f + 0.5f);
				int end_y = (int) Math.floor(((im2.getHeight() - 1) - 0.5f) * 2.f + 0.5f);

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
						if ((value * value) < laplacianSqrThreshold) {
							continue;
						}

						// Compute downsampled point location
						double ds_x = col * 0.5f - 0.25f;
						double ds_y = row * 0.5f - 0.25f;

						boolean extrema = false;
						if (
						/* im0 - 9 evaluations */
						value > im0b[im0_ym1 + col - 1] && value > im0b[im0_ym1 + col]
								&& value > im0b[im0_ym1 + col + 1] && value > im0b[im0_y + col - 1]
								&& value > im0b[im0_y + col] && value > im0b[im0_y + col + 1]
								&& value > im0b[im0_yp1 + col - 1] && value > im0b[im0_yp1 + col]
								&& value > im0b[im0_yp1 + col + 1] &&
								/* im1 - 8 evaluations */
								value > im1b[im1_ym1 + col - 1] && value > im1b[im1_ym1 + col]
								&& value > im1b[im1_ym1 + col + 1] && value > im1b[im1_y + col - 1]
								&& value > im1b[im1_y + col + 1] && value > im1b[im1_yp1 + col - 1]
								&& value > im1b[im1_yp1 + col] && value > im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value > im2.bilinearInterpolation( ds_x - 0.5f, ds_y - 0.5f)
								&& value > im2.bilinearInterpolation( ds_x, ds_y - 0.5f)
								&& value > im2.bilinearInterpolation( ds_x + 0.5f, ds_y - 0.5f)
								&& value > im2.bilinearInterpolation( ds_x - 0.5f, ds_y)
								&& value > im2.bilinearInterpolation( ds_x, ds_y)
								&& value > im2.bilinearInterpolation( ds_x + 0.5f, ds_y)
								&& value > im2.bilinearInterpolation( ds_x - 0.5f, ds_y + 0.5f)
								&& value > im2.bilinearInterpolation( ds_x, ds_y + 0.5f)
								&& value > im2.bilinearInterpolation( ds_x + 0.5f, ds_y + 0.5f)) {
							extrema = true;
						} else if (
						/* im0 - 9 evaluations */
						value < im0b[im0_ym1 + col - 1] && value < im0b[im0_ym1 + col]
								&& value < im0b[im0_ym1 + col + 1] && value < im0b[im0_y + col - 1]
								&& value < im0b[im0_y + col] && value < im0b[im0_y + col + 1]
								&& value < im0b[im0_yp1 + col - 1] && value < im0b[im0_yp1 + col]
								&& value < im0b[im0_yp1 + col + 1] &&
								/* im1 - 8 evaluations */
								value < im1b[im1_ym1 + col - 1] && value < im1b[im1_ym1 + col]
								&& value < im1b[im1_ym1 + col + 1] && value < im1b[im1_y + col - 1]
								&& value < im1b[im1_y + col + 1] && value < im1b[im1_yp1 + col - 1]
								&& value < im1b[im1_yp1 + col] && value < im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value < im2.bilinearInterpolation(ds_x - 0.5f, ds_y - 0.5f)
								&& value < im2.bilinearInterpolation(ds_x, ds_y - 0.5f)
								&& value < im2.bilinearInterpolation(ds_x + 0.5f, ds_y - 0.5f)
								&& value < im2.bilinearInterpolation(ds_x - 0.5f, ds_y)
								&& value < im2.bilinearInterpolation(ds_x, ds_y)
								&& value < im2.bilinearInterpolation(ds_x + 0.5f, ds_y)
								&& value < im2.bilinearInterpolation(ds_x - 0.5f, ds_y + 0.5f)
								&& value < im2.bilinearInterpolation(ds_x, ds_y + 0.5f)
								&& value < im2.bilinearInterpolation(ds_x + 0.5f, ds_y + 0.5f)) 
						{
							extrema = true;
						}

						if (extrema) {
							DogFeaturePoint fp = i_dog_fp.prePush();
							if(fp==null){
								prepush_warning();
								break;
							}
							fp.octave = octave;
							fp.scale = scale;
							fp.score = value;
							fp.sigma = pyramid.effectiveSigma(octave, scale);
							double[] tmp = new double[2];
							bilinear_upsample_point(tmp, col, row, octave);
							fp.x = tmp[0];
							fp.y = tmp[1];
						}
					}
				}
			} else if ((im0.getWidth() >> 1) == im1.getWidth() && (im0.getWidth() >> 1) == im2.getWidth()) {

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
						if ((value * value) < laplacianSqrThreshold) {
							continue;
						}

						double us_x = (col << 1) + 0.5f;
						double us_y = (row << 1) + 0.5f;

						boolean extrema = false;
						if (value > im1b[im1_ym1 + col - 1] && value > im1b[im1_ym1 + col]
								&& value > im1b[im1_ym1 + col + 1] && value > im1b[im1_y + col - 1]
								&& value > im1b[im1_y + col + 1] && value > im1b[im1_yp1 + col - 1]
								&& value > im1b[im1_yp1 + col] && value > im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value > im2b[im2_ym1 + col - 1] && value > im2b[im2_ym1 + col]
								&& value > im2b[im2_ym1 + col + 1] && value > im2b[im2_y + col - 1]
								&& value > im2b[im2_y + col] && value > im2b[im2_y + col + 1]
								&& value > im2b[im2_yp1 + col - 1] && value > im2b[im2_yp1 + col]
								&& value > im2b[im2_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value > im0.bilinearInterpolation(us_x - 2.f, us_y - 2.f)
								&& value > im0.bilinearInterpolation(us_x, us_y - 2.f)
								&& value > im0.bilinearInterpolation(us_x + 2.f, us_y - 2.f)
								&& value > im0.bilinearInterpolation(us_x - 2.f, us_y)
								&& value > im0.bilinearInterpolation(us_x, us_y)
								&& value > im0.bilinearInterpolation(us_x + 2.f, us_y)
								&& value > im0.bilinearInterpolation(us_x - 2.f, us_y + 2.f)
								&& value > im0.bilinearInterpolation(us_x, us_y + 2.f)
								&& value > im0.bilinearInterpolation(us_x + 2.f, us_y + 2.f))
						{ 
							extrema = true;
						} else if (value < im1b[im1_ym1 + col - 1] && value < im1b[im1_ym1 + col]
								&& value < im1b[im1_ym1 + col + 1] && value < im1b[im1_y + col - 1]
								&& value < im1b[im1_y + col + 1] && value < im1b[im1_yp1 + col - 1]
								&& value < im1b[im1_yp1 + col] && value < im1b[im1_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value < im2b[im2_ym1 + col - 1] && value < im2b[im2_ym1 + col]
								&& value < im2b[im2_ym1 + col + 1] && value < im2b[im2_y + col - 1]
								&& value < im2b[im2_y + col] && value < im2b[im2_y + col + 1]
								&& value < im2b[im2_yp1 + col - 1] && value < im2b[im2_yp1 + col]
								&& value < im2b[im2_yp1 + col + 1] &&
								/* im2 - 9 evaluations */
								value < im0.bilinearInterpolation(us_x - 2.f, us_y - 2.f)
								&& value < im0.bilinearInterpolation(us_x, us_y - 2.f)
								&& value < im0.bilinearInterpolation(us_x + 2.f, us_y - 2.f)
								&& value < im0.bilinearInterpolation(us_x - 2.f, us_y)
								&& value < im0.bilinearInterpolation(us_x, us_y)
								&& value < im0.bilinearInterpolation(us_x + 2.f, us_y)
								&& value < im0.bilinearInterpolation(us_x - 2.f, us_y + 2.f)
								&& value < im0.bilinearInterpolation(us_x, us_y + 2.f)
								&& value < im0.bilinearInterpolation(us_x + 2.f, us_y + 2.f))
						{ 
							extrema = true;
						}

						if (extrema) {
							DogFeaturePoint fp = i_dog_fp.prePush();
							if(fp==null){
								prepush_warning();
								break;
							}
							fp.octave = octave;
							fp.scale = scale;
							fp.score = value;
							fp.sigma = pyramid.effectiveSigma(octave, scale);
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
	 * Sub-pixel refinement.
	 */
	private void findSubpixelLocations(GaussianScaleSpacePyramid pyramid,DogFeaturePointStack i_dog_fp) {
		int num_points;
		double laplacianSqrThreshold;
		double hessianThreshold;

		num_points = 0;
		laplacianSqrThreshold = (this.mLaplacianThreshold * this.mLaplacianThreshold);
		double te = (mEdgeThreshold + 1);
		hessianThreshold = ((te * te) / mEdgeThreshold);

		for (int i = 0; i < i_dog_fp.getLength(); i++) {
			DogFeaturePoint kp = i_dog_fp.getItem(i);
			//assert kp.scale < mLaplacianPyramid.numScalePerOctave();
			// ASSERT(kp.scale < mLaplacianPyramid.numScalePerOctave(),
			// "Feature point scale is out of bounds");
			int lap_index = kp.octave * mLaplacianPyramid.numScalePerOctave() + kp.scale;

			// Get Laplacian images
			LaplacianImage lap0 = mLaplacianPyramid.get(lap_index - 1);
			LaplacianImage lap1 = mLaplacianPyramid.get(lap_index);
			LaplacianImage lap2 = mLaplacianPyramid.get(lap_index + 1);

			// Compute the Hessian
			if (!this.updateLocation(kp,lap0, lap1, lap2)) {
				continue;
			}


			if (Math.abs(kp.edge_score) < hessianThreshold && (kp.score * kp.score) >= laplacianSqrThreshold
					&& kp.x >= 0 && kp.x < mLaplacianPyramid.get(0).getWidth() && kp.y >= 0
					&& kp.y < mLaplacianPyramid.get(0).getHeight()) {
				// Update the sigma
				kp.sigma = pyramid.effectiveSigma(kp.octave, kp.sp_scale);
				i_dog_fp.swap(i,num_points++);
			}
		}
		i_dog_fp.setLength(num_points);
	}



	final private double[] _addFeatureOrientations_tmp=new double[3];
	
	private void addFeatureOrientations(GaussianScaleSpacePyramid i_pyramid,DogFeaturePoint dfp,DogFeaturePointStack i_ot_fps)
	{
		double[] tmp = this._addFeatureOrientations_tmp;

		
		double x, y, s;

		// Down sample the point to the detected octave
		bilinear_downsample_point(tmp, dfp.x, dfp.y,dfp.sigma,dfp.octave);
		x = tmp[0];
		y = tmp[1];
		s = tmp[2];

		// Downsampling the point can cause (x,y) to leave the image bounds
		// by
		// a tiny amount. Here we just clip it to be within the image
		// bounds.
		x = ClipScalar(x, 0, i_pyramid.get(dfp.octave, 0).getWidth() - 1);
		y = ClipScalar(y, 0, i_pyramid.get(dfp.octave, 0).getHeight() - 1);

		// Compute dominant orientations
		int num_angles=mOrientationAssignment.compute(dfp.octave, dfp.scale, x, y,s,this.mOrientations);
		// Create a feature point for each angle
		for (int j = 0; j < num_angles; j++) {
			// Copy the feature point
			DogFeaturePoint fp = i_ot_fps.prePush();
			if(fp==null){
				//中断
//				prepush_warning();
				break;
			}
			fp.x = dfp.x;
			fp.y = dfp.y;
			fp.octave = dfp.octave;
			fp.scale = dfp.scale;
			fp.sp_scale = dfp.sp_scale;
			fp.score = dfp.score;
			fp.sigma = dfp.sigma;
			fp.edge_score = dfp.edge_score;				
			fp.angle=mOrientations[j];
		}
		return;

	}	
	
	
	
	
	
	
	
	

	// private boolean ComputeSubpixelHessian(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)

	private boolean updateLocation(DogFeaturePoint kp,LaplacianImage lap0, LaplacianImage lap1, LaplacianImage lap2)
	{
		double[] tmp = new double[2];
		double[] b = new double[3];
		
		
		// Downsample the feature point to the detection octave
		bilinear_downsample_point(tmp, kp.x, kp.y, kp.octave);
		double xp = tmp[0];
		double yp = tmp[1];
		// Compute the discrete pixel location
		int x = (int) (xp + 0.5f);
		int y = (int) (yp + 0.5f);		
		
		double[] H=new double[9];
		if (lap0.getWidth() == lap1.getWidth() && lap1.getWidth() == lap2.getWidth()) {
			//すべての画像サイズが同じ
			//assert lap0.getHeight() == lap1.getHeight() && lap1.getHeight() == lap2.getHeight();// "Width/height are not consistent");
			ComputeSubpixelHessianSameOctave(H, b, lap0, lap1, lap2, x, y);
		} else if ((lap0.getWidth() == lap1.getWidth()) && ((lap1.getWidth() >> 1) == lap2.getWidth())) {
			//0,1が同じで2がその半分
			//assert (lap0.getHeight() == lap1.getHeight()) && ((lap1.getHeight() >> 1) == lap2.getHeight());// Width/height are not consistent");
			ComputeSubpixelHessianFineOctavePair(H, b, lap0, lap1, lap2, x, y);
		} else if (((lap0.getWidth() >> 1) == lap1.getWidth()) && (lap1.getWidth() == lap2.getWidth())) {
			//0の半分が1,2
			//assert ((lap0.getWidth() >> 1) == lap1.getWidth()) && (lap1.getWidth() == lap2.getWidth());// Width/height are not consistent");
			ComputeSubpixelHessianCoarseOctavePair(H, b, lap0, lap1, lap2, x, y);
		} else {
			// ASSERT(0, "Image sizes are inconsistent");
			return false;
		}
		
		// A*u=b	//		if (!SolveSymmetricLinearSystem3x3(u, H, b)) {
		NyARDoubleMatrix33 m = new NyARDoubleMatrix33();
		m.m00 = H[0];
		m.m01 = H[1];
		m.m02 = H[2];
		m.m10 = H[3];
		m.m11 = H[4];
		m.m12 = H[5];
		m.m20 = H[6];
		m.m21 = H[7];
		m.m22 = H[8];
		if (!m.inverse(m)) {
			return false;
		}
		double u0 = (m.m00 * b[0] + m.m01 * b[1] + m.m02 * b[2]);
		double u1 = (m.m10 * b[0] + m.m11 * b[1] + m.m12 * b[2]);
		double u2 = (m.m20 * b[0] + m.m21 * b[1] + m.m22 * b[2]);


		// If points move too much in the sub-pixel update, then the point probably unstable.
		if ((u0 * u0) + (u1 * u1) > mMaxSubpixelDistanceSqr) {
			return false;
		}

		// Compute the edge score
		if (!ComputeEdgeScore(tmp, H)) {
			return false;
		}		
		kp.edge_score = tmp[0];

		// Compute a linear estimate of the intensity
		// ASSERT(kp.score == lap1.get<float>(y)[x],
		// "Score is not consistent with the DoG image");
		double[] lap1_buf = (double[]) lap1.getBuffer();
		kp.score = lap1_buf[lap1.get(y) + x] - (b[0] * u0 + b[1] * u1 + b[2] * u2);

		// Update the location:
		// Apply the update on the downsampled location and then upsample
		// the result.
		// bilinear_upsample_point(kp.x, kp.y, xp+u[0], yp+u[1], kp.octave);
		bilinear_upsample_point(tmp, xp + u0, yp + u1, kp.octave);
		kp.x = tmp[0];
		kp.y = tmp[1];

		// Update the scale
		kp.sp_scale = kp.scale + u2;
		kp.sp_scale = ClipScalar(kp.sp_scale, 0, mLaplacianPyramid.numScalePerOctave());
		return true;
	}

	// private void ComputeSubpixelHessianCoarseOctavePair(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)
	private void ComputeSubpixelHessianCoarseOctavePair(double H[], double b[], LaplacianImage lap0, LaplacianImage lap1,
			LaplacianImage lap2, int x, int y) {
		double val;
		double x_mul_2, y_mul_2;
		double Dx, Dy, Ds;
		double Dxx, Dyy, Dxy;
		double Dss, Dxs, Dys;

		//assert (x - 1) >= 0 && (x + 1) < lap1.getWidth();
		//assert (y - 1) >= 0 && (y + 1) < lap1.getHeight();
		//assert (lap0.getWidth() >> 1) == lap1.getWidth();
		//assert (lap0.getWidth() >> 1) == lap2.getWidth();
		//assert (lap0.getHeight() >> 1) == lap1.getHeight();
		//assert (lap0.getHeight() >> 1) == lap2.getHeight();


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
		lap1.computeSubpixelDerivatives(x, y,tmp);
		Dx = tmp[0];
		Dy = tmp[1];
		Dxx = tmp[2];
		Dyy = tmp[3];
		Dxy = tmp[4];
		// Interpolate the VALUE at the finer octave
		val = lap0.bilinearInterpolation(x_mul_2, y_mul_2);
		double[] lap2buf = (double[]) lap2.getBuffer();
		double[] lap1buf = (double[]) lap1.getBuffer();

		Ds = 0.5f * (lap2buf[lap2_p + 0] - val);
		Dss = val + (-2.f * lap1buf[lap1_p + 0]) + lap2buf[lap2_p + 0];
		Dxs = 0.25f * ((lap0.bilinearInterpolation(x_mul_2 - 2, y_mul_2) + lap2buf[lap2_p + 1]) - (lap0.bilinearInterpolation(
				x_mul_2 + 2, y_mul_2) + lap2buf[lap2_p - 1]));
		Dys = 0.25f * ((lap0.bilinearInterpolation(x_mul_2, y_mul_2 - 2) + lap2buf[lap2_pp1 + 0]) - (lap0.bilinearInterpolation(
				x_mul_2, y_mul_2 + 2) + lap2buf[lap2_pm1 + 0]));

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

	// inline void ComputeSubpixelHessianFineOctavePair(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)
	private void ComputeSubpixelHessianFineOctavePair(double[] H, double[] b, LaplacianImage lap0, LaplacianImage lap1,
			LaplacianImage lap2, int x, int y) {
		double x_div_2, y_div_2;
		double val;
		double Dx, Dy, Ds;
		double Dxx, Dyy, Dxy;
		double Dss, Dxs, Dys;

		//assert (x - 1) >= 0 && (x + 1) < lap1.getWidth();
		//assert (y - 1) >= 0 && (y + 1) < lap1.getHeight();
		//assert lap0.getWidth() == lap1.getWidth();
		//assert (lap0.getWidth() >> 1) == lap2.getWidth();
		//assert lap0.getHeight() == lap1.getHeight();
		//assert (lap0.getHeight() >> 1) == lap2.getHeight();

		int lap0_pm1 = lap0.get(y - 1) + x;
		int lap0_p = lap0.get(y) + x;
		int lap0_pp1 = lap0.get(y + 1) + x;
		int lap1_p = lap1.get(y) + x;
		double[] tmp = new double[5];
		bilinear_downsample_point(tmp, x, y, 1);
		x_div_2 = tmp[0];
		y_div_2 = tmp[1];
		//assert x_div_2 - 0.5f >= 0;
		//assert y_div_2 - 0.5f >= 0;
		//assert x_div_2 + 0.5f < lap2.getWidth();
		//assert y_div_2 + 0.5f < lap2.getHeight();

		// Compute spatial derivatives
		// ComputeSubpixelDerivatives(Dx, Dy, Dxx, Dyy, Dxy, lap1, x, y);
		lap1.computeSubpixelDerivatives(x, y, tmp);
		Dx = tmp[0];
		Dy = tmp[1];
		Dxx = tmp[2];
		Dyy = tmp[3];
		Dxy = tmp[4];
		// Interpolate the VALUE at the coarser octave
		val = lap2.bilinearInterpolation(x_div_2, y_div_2);


		double[] lap0_buf = (double[]) lap0.getBuffer();
		double[] lap1_buf = (double[]) lap1.getBuffer();

		Ds = 0.5f * (val - lap0_buf[lap0_p + 0]);
		Dss = lap0_buf[lap0_p + 0] + (-2.f * lap1_buf[lap1_p + 0]) + val;
		Dxs = 0.25f * (
			(lap0_buf[lap0_p - 1]+lap2.bilinearInterpolation(x_div_2 + .5f, y_div_2)) 
			-(lap0_buf[lap0_p + 1] + lap2.bilinearInterpolation(x_div_2 - .5f, y_div_2))
		);
		Dys = 0.25f * (
			(lap0_buf[lap0_pm1 + 0] + lap2.bilinearInterpolation(x_div_2, y_div_2 + .5f))
			-(lap0_buf[lap0_pp1 + 0] + lap2.bilinearInterpolation(x_div_2, y_div_2 - .5f))
		);

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



	// private void ComputeSubpixelHessianSameOctave(
	// float H[9],float b[3],
	// const Image& lap0,const Image& lap1,const Image& lap2,
	// int x,int y)
	private void ComputeSubpixelHessianSameOctave(double[] H, double[] b, LaplacianImage lap0, LaplacianImage lap1, LaplacianImage lap2,
			int x, int y) {
		double Dx, Dy, Ds;
		double Dxx, Dyy, Dxy;
		double Dss, Dxs, Dys;

		//assert (x - 1) >= 0 && (x + 1) < lap1.getWidth();
		//assert (y - 1) >= 0 && (y + 1) < lap1.getHeight();
		//assert lap0.getWidth() == lap1.getWidth();
		//assert lap0.getWidth() == lap2.getWidth();
		//assert lap0.getHeight() == lap1.getHeight();
		//assert lap0.getHeight() == lap2.getHeight();

		int lap0_pm1 = lap0.get(y - 1) + x;
		int lap0_p = lap0.get(y) + x;
		int lap0_pp1 = lap0.get(y + 1) + x;

		int lap1_p = lap1.get(y) + x;
		int lap2_pm1 = lap2.get(y - 1) + x;
		int lap2_p = lap2.get(y) + x;
		int lap2_pp1 = lap2.get(y + 1) + x;

		double[] tmp = new double[5];
		// Compute spatial derivatives
//	 ComputeSubpixelDerivatives(Dx, Dy, Dxx, Dyy, Dxy, lap1, x, y);
		lap1.computeSubpixelDerivatives(x, y, tmp);
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
		Dss = lap0buf[lap0_p + 0] + (-2.f * lap1buf[lap1_p + 0]) + lap2buf[lap2_p + 0];
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
	private static boolean ComputeEdgeScore(double[] score, double[] H) {
		double det;

		double Dxx = H[0];
		double Dyy = H[4];
		double Dxy = H[1];

		det = (Dxx * Dyy) - (Dxy * Dxy);

		// The determinant cannot be zero
		if (det == 0) {
			return false;
		}
		double t = Dxx + Dyy;
		// Compute a score based on the local curvature
		score[0] = (t * t) / det;

		return true;
	}

	private static double ClipScalar(double x, double min, double max) {
		if (x < min) {
			x = min;
		} else if (x > max) {
			x = max;
		}
		return x;
	}


	private static void prepush_warning(){
		System.out.println("DogFeaturePoint over flow");
	}
}
