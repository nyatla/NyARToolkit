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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.KpmImage;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Point2d;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.detectors.GaussianScaleSpacePyramid;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.detectors.interpole;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.math_utils;

public class FREAKExtractor {
	/**
	 * The total number of receptors from all the rings and the center receptor.
	 * 
	 * NUM_RINGS*NUM_RECEPTORS_PER_RING+1
	 */
	final private static int freak84_num_receptors = 37;

	/**
	 * Total number of rings. This does not include the center receptor.
	 */
	final private static int freak84_num_rings = 6;

	/**
	 * Total number of receptor per ring.
	 */
	final private static int freak84_num_receptors_per_ring = 6;

	/**
	 * SIGMA value for the center receptor and the receptors in all the rings.
	 */

	final private static double freak84_sigma_center = 0.100000f;
	final private static double freak84_sigma_ring0 = 0.175000f;
	final private static double freak84_sigma_ring1 = 0.250000f;
	final private static double freak84_sigma_ring2 = 0.325000f;
	final private static double freak84_sigma_ring3 = 0.400000f;
	final private static double freak84_sigma_ring4 = 0.475000f;
	final private static double freak84_sigma_ring5 = 0.550000f;

	/**
	 * (x,y) locations of each receptor in the ring.
	 */

	final private static double[] freak84_points_ring0 = { 0.000000f, 0.362783f,
			-0.314179f, 0.181391f, -0.314179f, -0.181391f, -0.000000f,
			-0.362783f, 0.314179f, -0.181391f, 0.314179f, 0.181391f };
	final private static double[] freak84_points_ring1 = { -0.595502f,
			0.000000f, -0.297751f, -0.515720f, 0.297751f, -0.515720f,
			0.595502f, -0.000000f, 0.297751f, 0.515720f, -0.297751f, 0.515720f };
	final private static double[] freak84_points_ring2 = { -0.000000f,
			-0.741094f, 0.641806f, -0.370547f, 0.641806f, 0.370547f, 0.000000f,
			0.741094f, -0.641806f, 0.370547f, -0.641806f, -0.370547f };
	final private static double[] freak84_points_ring3 = { 0.847306f,
			-0.000000f, 0.423653f, 0.733789f, -0.423653f, 0.733789f,
			-0.847306f, 0.000000f, -0.423653f, -0.733789f, 0.423653f,
			-0.733789f };
	final private static double[] freak84_points_ring4 = { 0.000000f, 0.930969f,
			-0.806243f, 0.465485f, -0.806243f, -0.465485f, -0.000000f,
			-0.930969f, 0.806243f, -0.465485f, 0.806243f, 0.465485f };
	final private static double[] freak84_points_ring5 = { -1.000000f,
			0.000000f, -0.500000f, -0.866025f, 0.500000f, -0.866025f,
			1.000000f, -0.000000f, 0.500000f, 0.866025f, -0.500000f, 0.866025f };

	// Receptor locations
	final private double[] mPointRing0 = new double[12];
	final private double[] mPointRing1 = new double[12];
	final private double[] mPointRing2 = new double[12];
	final private double[] mPointRing3 = new double[12];
	final private double[] mPointRing4 = new double[12];
	final private double[] mPointRing5 = new double[12];

	// Sigma value
	private double mSigmaCenter;
	private double mSigmaRing0;
	private double mSigmaRing1;
	private double mSigmaRing2;
	private double mSigmaRing3;
	private double mSigmaRing4;
	private double mSigmaRing5;

	// Scale expansion factor
	private double mExpansionFactor;

	/**
	 * Implements the FREAK extractor.
	 */
	class receptor {
		receptor() {
		}

		receptor(double _x, double _y, double _s) {
			this.x = _x;
			this.y = _y;
			this.s = _s;
		}

		double x, y, s;
	};

	private void CopyVector(double[] out, double[] in, int len) {
		System.arraycopy(in, 0, out, 0, len);
	}

	public FREAKExtractor() {
		CopyVector(mPointRing0, freak84_points_ring0, 12);
		CopyVector(mPointRing1, freak84_points_ring1, 12);
		CopyVector(mPointRing2, freak84_points_ring2, 12);
		CopyVector(mPointRing3, freak84_points_ring3, 12);
		CopyVector(mPointRing4, freak84_points_ring4, 12);
		CopyVector(mPointRing5, freak84_points_ring5, 12);

		mSigmaCenter = freak84_sigma_center;
		mSigmaRing0 = freak84_sigma_ring0;
		mSigmaRing1 = freak84_sigma_ring1;
		mSigmaRing2 = freak84_sigma_ring2;
		mSigmaRing3 = freak84_sigma_ring3;
		mSigmaRing4 = freak84_sigma_ring4;
		mSigmaRing5 = freak84_sigma_ring5;

		mExpansionFactor = 7;

		// ASSERT(sizeof(freak84_points_ring0) == 48,
		// "Size should be 48 bytes");
		// ASSERT(sizeof(freak84_points_ring1) == 48,
		// "Size should be 48 bytes");
		// ASSERT(sizeof(freak84_points_ring2) == 48,
		// "Size should be 48 bytes");
		// ASSERT(sizeof(freak84_points_ring3) == 48,
		// "Size should be 48 bytes");
		// ASSERT(sizeof(freak84_points_ring4) == 48,
		// "Size should be 48 bytes");
		// ASSERT(sizeof(freak84_points_ring5) == 48,
		// "Size should be 48 bytes");
	}

	// /**
	// * Get a set of tests for an 84 byte descriptor.
	// */
	// void layout84(std::vector<receptor>& receptors,
	// std::vector<std::vector<int> >& tests) {
	// const int ring_size = 6;
	// const int num_rings = 6;
	//
	// const float radius_m = 4;
	// const float radius_b = 2;
	//
	// const float sigma_m = 2;
	// const float sigma_b = std::sqrt(2);
	//
	// float max_radius = -1;
	// float max_sigma = -1;
	//
	// float delta_theta = (2.f*PI)/ring_size;
	// for(int i = 0; i < num_rings+1; i++) {
	// float sigma = std::log(sigma_m*i+sigma_b);
	//
	// if(i == 0) {
	// receptor r;
	// r.x = 0;
	// r.y = 0;
	// r.s = sigma;
	//
	// receptors.push_back(r);
	// } else {
	// float radius = std::log(radius_m*i+radius_b);
	//
	// for(int j = 0; j < ring_size; j++) {
	//
	// float theta = j*delta_theta+i*PI/2.f;
	//
	// receptor r;
	// r.x = radius*std::cos(theta);
	// r.y = radius*std::sin(theta);
	// r.s = sigma;
	//
	// receptors.push_back(r);
	// }
	//
	// if(radius > max_radius) {
	// max_radius = radius;
	// }
	// }
	//
	// if(sigma > max_sigma) {
	// max_sigma = sigma;
	// }
	// }
	//
	// // Normalize
	// for(size_t i = 0; i < receptors.size(); i++) {
	// receptors[i].x /= max_radius;
	// receptors[i].y /= max_radius;
	// receptors[i].s /= max_sigma;
	// }
	//
	// // Generate tests
	// tests.resize(receptors.size());
	// for(size_t i = 0; i < receptors.size(); i++) {
	// for(size_t j = i+1; j < receptors.size(); j++) {
	// tests[i].push_back((int)j);
	// }
	// }
	// }
	//
	//
	/**
	 * Extract a 96 byte descriptor.
	 */
	void extract(BinaryFeatureStore store, GaussianScaleSpacePyramid pyramid,
			FeaturePoint[] points) {

		store.setNumBytesPerFeature(96);
		store.resize(points.length);
		ExtractFREAK84(store, pyramid, points, mPointRing0, mPointRing1,
				mPointRing2, mPointRing3, mPointRing4, mPointRing5,
				mSigmaCenter, mSigmaRing0, mSigmaRing1, mSigmaRing2,
				mSigmaRing3, mSigmaRing4, mSigmaRing5, mExpansionFactor);
	}

	/**
	 * Extract the descriptors for all the feature points.
	 */
	void ExtractFREAK84(BinaryFeatureStore store,
			GaussianScaleSpacePyramid pyramid, FeaturePoint[] points,
			double[] points_ring0, double[] points_ring1, double[] points_ring2,
			double[] points_ring3, double[] points_ring4, double[] points_ring5,
			double sigma_center, double sigma_ring0, double sigma_ring1,
			double sigma_ring2, double sigma_ring3, double sigma_ring4,
			double sigma_ring5, double expansion_factor) {
		// ASSERT(pyramid, "Pyramid is NULL");
		// ASSERT(store.size() == points.size(),
		// "Feature store has not been allocated");
		int num_points = 0;
		for (int i = 0; i < points.length; i++) {

			if (!ExtractFREAK84(store.feature(num_points), store.features(),
					pyramid, points[i], points_ring0, points_ring1,
					points_ring2, points_ring3, points_ring4, points_ring5,
					sigma_center, sigma_ring0, sigma_ring1, sigma_ring2,
					sigma_ring3, sigma_ring4, sigma_ring5, expansion_factor

			)) {
				continue;
			}
			store.points().prePush().set(points[i]);

//			store.point(num_points).set(points[i]);
			num_points++;
		}
		// ASSERT(num_points == points.size(), "Should be same size");

		// Shrink store down to the number of valid points
		store.resize(num_points);
	}

	/**
	 * Extract a descriptor from the pyramid for a single point.
	 */
	boolean ExtractFREAK84(
			int i_desc_idx,// unsigned char desc[84],
			byte[] i_desc,// unsigned char desc[84],
			GaussianScaleSpacePyramid pyramid, FeaturePoint point,
			double[] points_ring0, double[] points_ring1, double[] points_ring2,
			double[] points_ring3, double[] points_ring4, double[] points_ring5,
			double sigma_center, double sigma_ring0, double sigma_ring1,
			double sigma_ring2, double sigma_ring3, double sigma_ring4,
			double sigma_ring5, double expansion_factor) {
		double[] samples = new double[37];

		// Create samples
		if (!SamplePyramidFREAK84(samples, pyramid, point, points_ring0,
				points_ring1, points_ring2, points_ring3, points_ring4,
				points_ring5, sigma_center, sigma_ring0, sigma_ring1,
				sigma_ring2, sigma_ring3, sigma_ring4, sigma_ring5,
				expansion_factor)) {
			return false;
		}

		// Once samples are created compute descriptor
		CompareFREAK84(i_desc,i_desc_idx, samples);

		return true;
	}

	/**
	 * Sample all the receptors from the pyramid given a single point.
	 */
	boolean SamplePyramidFREAK84(double[] samples,
			GaussianScaleSpacePyramid pyramid, FeaturePoint point,
			double[] points_ring0, double[] points_ring1, double[] points_ring2,
			double[] points_ring3, double[] points_ring4, double[] points_ring5,
			double sigma_center, double sigma_ring0, double sigma_ring1,
			double sigma_ring2, double sigma_ring3, double sigma_ring4,
			double sigma_ring5, double expansion_factor) {
		double[] S = new double[9];

		double[] c = new double[2];
		double[] r0 = new double[2 * 6];
		double[] r1 = new double[2 * 6];
		double[] r2 = new double[2 * 6];
		double[] r3 = new double[2 * 6];
		double[] r4 = new double[2 * 6];
		double[] r5 = new double[2 * 6];

		double sc, s0, s1, s2, s3, s4, s5;

		// Ensure the scale of the similarity transform is at least "1".
		double transform_scale = point.scale * expansion_factor;
		if (transform_scale < 1) {
			transform_scale = 1;
		}

		// Transformation from canonical test locations to image
		math_utils
				.Similarity(S, point.x, point.y, point.angle, transform_scale);

		// Locate center points
		c[0] = S[2];
		c[1] = S[5];

		// Locate ring 0 points
		math_utils.MultiplyPointSimilarityInhomogenous(r0, 0, S, points_ring0,
				0);
		math_utils.MultiplyPointSimilarityInhomogenous(r0, 2, S, points_ring0,
				2);
		math_utils.MultiplyPointSimilarityInhomogenous(r0, 4, S, points_ring0,
				4);
		math_utils.MultiplyPointSimilarityInhomogenous(r0, 6, S, points_ring0,
				6);
		math_utils.MultiplyPointSimilarityInhomogenous(r0, 8, S, points_ring0,
				8);
		math_utils.MultiplyPointSimilarityInhomogenous(r0, 10, S, points_ring0,
				10);

		// Locate ring 1 points
		math_utils.MultiplyPointSimilarityInhomogenous(r1, 0, S, points_ring1,
				0);
		math_utils.MultiplyPointSimilarityInhomogenous(r1, 2, S, points_ring1,
				2);
		math_utils.MultiplyPointSimilarityInhomogenous(r1, 4, S, points_ring1,
				4);
		math_utils.MultiplyPointSimilarityInhomogenous(r1, 6, S, points_ring1,
				6);
		math_utils.MultiplyPointSimilarityInhomogenous(r1, 8, S, points_ring1,
				8);
		math_utils.MultiplyPointSimilarityInhomogenous(r1, 10, S, points_ring1,
				10);

		// Locate ring 2 points
		math_utils.MultiplyPointSimilarityInhomogenous(r2, 0, S, points_ring2,
				0);
		math_utils.MultiplyPointSimilarityInhomogenous(r2, 2, S, points_ring2,
				2);
		math_utils.MultiplyPointSimilarityInhomogenous(r2, 4, S, points_ring2,
				4);
		math_utils.MultiplyPointSimilarityInhomogenous(r2, 6, S, points_ring2,
				6);
		math_utils.MultiplyPointSimilarityInhomogenous(r2, 8, S, points_ring2,
				8);
		math_utils.MultiplyPointSimilarityInhomogenous(r2, 10, S, points_ring2,
				10);

		// Locate ring 3 points
		math_utils.MultiplyPointSimilarityInhomogenous(r3, 0, S, points_ring3,
				0);
		math_utils.MultiplyPointSimilarityInhomogenous(r3, 2, S, points_ring3,
				2);
		math_utils.MultiplyPointSimilarityInhomogenous(r3, 4, S, points_ring3,
				4);
		math_utils.MultiplyPointSimilarityInhomogenous(r3, 6, S, points_ring3,
				6);
		math_utils.MultiplyPointSimilarityInhomogenous(r3, 8, S, points_ring3,
				8);
		math_utils.MultiplyPointSimilarityInhomogenous(r3, 10, S, points_ring3,
				10);

		// Locate ring 4 points
		math_utils.MultiplyPointSimilarityInhomogenous(r4, 0, S, points_ring4,
				0);
		math_utils.MultiplyPointSimilarityInhomogenous(r4, 2, S, points_ring4,
				2);
		math_utils.MultiplyPointSimilarityInhomogenous(r4, 4, S, points_ring4,
				4);
		math_utils.MultiplyPointSimilarityInhomogenous(r4, 6, S, points_ring4,
				6);
		math_utils.MultiplyPointSimilarityInhomogenous(r4, 8, S, points_ring4,
				8);
		math_utils.MultiplyPointSimilarityInhomogenous(r4, 10, S, points_ring4,
				10);

		// Locate ring 5 points
		math_utils.MultiplyPointSimilarityInhomogenous(r5, 0, S, points_ring5,
				0);
		math_utils.MultiplyPointSimilarityInhomogenous(r5, 2, S, points_ring5,
				2);
		math_utils.MultiplyPointSimilarityInhomogenous(r5, 4, S, points_ring5,
				4);
		math_utils.MultiplyPointSimilarityInhomogenous(r5, 6, S, points_ring5,
				6);
		math_utils.MultiplyPointSimilarityInhomogenous(r5, 8, S, points_ring5,
				8);
		math_utils.MultiplyPointSimilarityInhomogenous(r5, 10, S, points_ring5,
				10);

		// Transfer all the SIGMA values to the image
		sc = sigma_center * transform_scale;
		s0 = sigma_ring0 * transform_scale;
		s1 = sigma_ring1 * transform_scale;
		s2 = sigma_ring2 * transform_scale;
		s3 = sigma_ring3 * transform_scale;
		s4 = sigma_ring4 * transform_scale;
		s5 = sigma_ring5 * transform_scale;

		//
		// Locate and sample ring 5
		//
		GaussianScaleSpacePyramid.LocateResult lr = new GaussianScaleSpacePyramid.LocateResult();
		pyramid.locate(s5, lr);
		samples[0] = SampleReceptor(pyramid, r5[0], r5[1], lr.octave, lr.scale);
		samples[1] = SampleReceptor(pyramid, r5[2], r5[3], lr.octave, lr.scale);
		samples[2] = SampleReceptor(pyramid, r5[4], r5[5], lr.octave, lr.scale);
		samples[3] = SampleReceptor(pyramid, r5[6], r5[7], lr.octave, lr.scale);
		samples[4] = SampleReceptor(pyramid, r5[8], r5[9], lr.octave, lr.scale);
		samples[5] = SampleReceptor(pyramid, r5[10], r5[11], lr.octave,
				lr.scale);

		//
		// Locate and sample ring 4
		//

		pyramid.locate(s4, lr);
		samples[6] = SampleReceptor(pyramid, r4[0], r4[1], lr.octave, lr.scale);
		samples[7] = SampleReceptor(pyramid, r4[2], r4[3], lr.octave, lr.scale);
		samples[8] = SampleReceptor(pyramid, r4[4], r4[5], lr.octave, lr.scale);
		samples[9] = SampleReceptor(pyramid, r4[6], r4[7], lr.octave, lr.scale);
		samples[10] = SampleReceptor(pyramid, r4[8], r4[9], lr.octave, lr.scale);
		samples[11] = SampleReceptor(pyramid, r4[10], r4[11], lr.octave,
				lr.scale);

		//
		// Locate and sample ring 3
		//

		pyramid.locate(s3, lr);
		samples[12] = SampleReceptor(pyramid, r3[0], r3[1], lr.octave, lr.scale);
		samples[13] = SampleReceptor(pyramid, r3[2], r3[3], lr.octave, lr.scale);
		samples[14] = SampleReceptor(pyramid, r3[4], r3[5], lr.octave, lr.scale);
		samples[15] = SampleReceptor(pyramid, r3[6], r3[7], lr.octave, lr.scale);
		samples[16] = SampleReceptor(pyramid, r3[8], r3[9], lr.octave, lr.scale);
		samples[17] = SampleReceptor(pyramid, r3[10], r3[11], lr.octave,
				lr.scale);

		//
		// Locate and sample ring 2
		//

		pyramid.locate(s2, lr);
		samples[18] = SampleReceptor(pyramid, r2[0], r2[1], lr.octave, lr.scale);
		samples[19] = SampleReceptor(pyramid, r2[2], r2[3], lr.octave, lr.scale);
		samples[20] = SampleReceptor(pyramid, r2[4], r2[5], lr.octave, lr.scale);
		samples[21] = SampleReceptor(pyramid, r2[6], r2[7], lr.octave, lr.scale);
		samples[22] = SampleReceptor(pyramid, r2[8], r2[9], lr.octave, lr.scale);
		samples[23] = SampleReceptor(pyramid, r2[10], r2[11], lr.octave,
				lr.scale);

		//
		// Locate and sample ring 1
		//

		pyramid.locate(s1, lr);
		samples[24] = SampleReceptor(pyramid, r1[0], r1[1], lr.octave, lr.scale);
		samples[25] = SampleReceptor(pyramid, r1[2], r1[3], lr.octave, lr.scale);
		samples[26] = SampleReceptor(pyramid, r1[4], r1[5], lr.octave, lr.scale);
		samples[27] = SampleReceptor(pyramid, r1[6], r1[7], lr.octave, lr.scale);
		samples[28] = SampleReceptor(pyramid, r1[8], r1[9], lr.octave, lr.scale);
		samples[29] = SampleReceptor(pyramid, r1[10], r1[11], lr.octave,
				lr.scale);

		//
		// Locate and sample ring 0
		//

		pyramid.locate(s0, lr);
		samples[30] = SampleReceptor(pyramid, r0[0], r0[1], lr.octave, lr.scale);
		samples[31] = SampleReceptor(pyramid, r0[2], r0[3], lr.octave, lr.scale);
		samples[32] = SampleReceptor(pyramid, r0[4], r0[5], lr.octave, lr.scale);
		samples[33] = SampleReceptor(pyramid, r0[6], r0[7], lr.octave, lr.scale);
		samples[34] = SampleReceptor(pyramid, r0[8], r0[9], lr.octave, lr.scale);
		samples[35] = SampleReceptor(pyramid, r0[10], r0[11], lr.octave,
				lr.scale);

		//
		// Locate and sample center
		//

		pyramid.locate(sc, lr);
		samples[36] = SampleReceptor(pyramid, c[0], c[1], lr.octave, lr.scale);

		return true;
	}

	/**
	 * Compute the descriptor given the 37 samples from each receptor.
	 */
	void CompareFREAK84(byte[] desc,int i_desc_index,double[] samples) {
		int pos = 0;
		for (int i = 0; i < 84; i++) {
			desc[i_desc_index+i] = 0;
		}// ZeroVector(desc, 84);
		for (int i = 0; i < 37; i++) {
			for (int j = i + 1; j < 37; j++) {
				bitstring_set_bit(desc,i_desc_index, pos, (samples[i] < samples[j]) ? 1 : 0);
				pos++;
			}
		}
		// ASSERT(pos == 666, "Position is not within range");
	}

	/**
	 * Set a bit in a bit string represented as an array of UNSIGNED CHAR bytes.
	 * The ordering on the bits is as follows:
	 * 
	 * [7 6 5 4 3 2 1 0, 15 14 13 12 11 10 9 8, 23 22 21 20 19 18 17 15, ...]
	 */
	private void bitstring_set_bit(byte[] bitstring,int i_desc_index, int pos, int bit) {
		bitstring[i_desc_index+(pos / 8)] |= (bit << (pos % 8));
	}

	// inline unsigned char bitstring_get_bit(const unsigned char* bitstring,
	// int pos) {
	// return (bitstring[pos/8] >> (pos%8)) & 1;
	// }
	//
	// /**
	// * Sample a receptor given (x,y) given an image using bilinear
	// interpolation.
	// */
	// inline float SampleReceptorBilinear(const Image& image,
	// float x,
	// float y) {
	// x = ClipScalar<float>(x, 0, image.width()-2);
	// y = ClipScalar<float>(y, 0, image.height()-2);
	// return bilinear_interpolation<float>(image, x, y);
	// }
	//
	// /**
	// * Sample a receptor given (x,y) given an image using nearest neighbor.
	// */
	// inline float SampleReceptorNN(const Image& image,
	// float x,
	// float y) {
	// x = ClipScalar<float>(x, 0, image.width()-1);
	// y = ClipScalar<float>(y, 0, image.height()-1);
	// return image.get<float>((int)y)[(int)x];
	// }
	//
	// /**
	// * Sample a receptor given (x,y) given an image.
	// */
	// inline float SampleReceptor(const Image& image,
	// float x,
	// float y) {
	// #ifdef FREAK_BILINEAR_SAMPLE
	// return SampleReceptorBilinear(image, x, y);
	// #else
	// return SampleReceptorNN(image, x, y);
	// #endif
	// }
	//
	/**
	 * Sample a receptor given (x,y,octave,scale) and a pyramid.
	 */
	private double SampleReceptor(GaussianScaleSpacePyramid pyramid, double x,
			double y, int octave, int scale) {
		// Get the correct image from the pyramid
		KpmImage image = pyramid.get(octave, scale);

		// Downsample the point to the octave
		Point2d p = new Point2d();
		GaussianScaleSpacePyramid.bilinear_downsample_point(p, x, y, octave);
		// Sample the receptor
		return SampleReceptor(image, p.x, p.y);
	}

	/**
	 * Sample a receptor given (x,y) given an image.
	 */
	private double SampleReceptor(KpmImage image, double x, double y) {
		return SampleReceptorBilinear(image, x, y);
	}

	/**
	 * Sample a receptor given (x,y) given an image using bilinear
	 * interpolation.
	 */
	private double SampleReceptorBilinear(KpmImage image, double x, double y) {
		x = ClipScalar(x, 0, image.getWidth() - 2);
		y = ClipScalar(y, 0, image.getHeight() - 2);
		return interpole.bilinear_interpolation(image, x, y);
	}

	/**
	 * Clip a scalar to be within a range.
	 */
	double ClipScalar(double x, double min, double max) {
		if (x < min) {
			x = min;
		} else if (x > max) {
			x = max;
		}
		return x;
	}

	//
	// /**
	// * Compute the descriptor given the 37 samples from each receptor.
	// */
	// inline void CompareFREAK84(unsigned char desc[84], const float
	// samples[37]) {
	// int pos = 0;
	// ZeroVector(desc, 84);
	// for(int i = 0; i < 37; i++) {
	// for(int j = i+1; j < 37; j++) {
	// bitstring_set_bit(desc, pos, samples[i] < samples[j]);
	// pos++;
	// }
	// }
	// ASSERT(pos == 666, "Position is not within range");
	// }
	//
	// /**
	// * Extract a descriptor from the pyramid for a single point.
	// */
	// inline bool ExtractFREAK84(unsigned char desc[84],
	// const GaussianScaleSpacePyramid* pyramid,
	// const FeaturePoint& point,
	// const float points_ring0[12],
	// const float points_ring1[12],
	// const float points_ring2[12],
	// const float points_ring3[12],
	// const float points_ring4[12],
	// const float points_ring5[12],
	// float sigma_center,
	// float sigma_ring0,
	// float sigma_ring1,
	// float sigma_ring2,
	// float sigma_ring3,
	// float sigma_ring4,
	// float sigma_ring5,
	// float expansion_factor
	// #ifdef FREAK_DEBUG
	// ,
	// float mapped_ring0[12],
	// float mapped_ring1[12],
	// float mapped_ring2[12],
	// float mapped_ring3[12],
	// float mapped_ring4[12],
	// float mapped_ring5[12],
	// float mapped_center[2],
	// float& mapped_s0,
	// float& mapped_s1,
	// float& mapped_s2,
	// float& mapped_s3,
	// float& mapped_s4,
	// float& mapped_s5,
	// float& mapped_sc
	// #endif
	// ) {
	// float samples[37];
	//
	// // Create samples
	// if(!SamplePyramidFREAK84(samples,
	// pyramid,
	// point,
	// points_ring0,
	// points_ring1,
	// points_ring2,
	// points_ring3,
	// points_ring4,
	// points_ring5,
	// sigma_center,
	// sigma_ring0,
	// sigma_ring1,
	// sigma_ring2,
	// sigma_ring3,
	// sigma_ring4,
	// sigma_ring5,
	// expansion_factor
	// #ifdef FREAK_DEBUG
	// ,
	// mapped_ring0,
	// mapped_ring1,
	// mapped_ring2,
	// mapped_ring3,
	// mapped_ring4,
	// mapped_ring5,
	// mapped_center,
	// mapped_s0,
	// mapped_s1,
	// mapped_s2,
	// mapped_s3,
	// mapped_s4,
	// mapped_s5,
	// mapped_sc
	// #endif
	// )) {
	// return false;
	// }
	//
	// // Once samples are created compute descriptor
	// CompareFREAK84(desc, samples);
	//
	// return true;
	// }
	//
	// /**
	// * Extract the descriptors for all the feature points.
	// */
	// inline void ExtractFREAK84(BinaryFeatureStore& store,
	// const GaussianScaleSpacePyramid* pyramid,
	// const std::vector<FeaturePoint>& points,
	// const float points_ring0[12],
	// const float points_ring1[12],
	// const float points_ring2[12],
	// const float points_ring3[12],
	// const float points_ring4[12],
	// const float points_ring5[12],
	// float sigma_center,
	// float sigma_ring0,
	// float sigma_ring1,
	// float sigma_ring2,
	// float sigma_ring3,
	// float sigma_ring4,
	// float sigma_ring5,
	// float expansion_factor
	// #ifdef FREAK_DEBUG
	// ,
	// std::vector<Point2d<float> >& mapped_ring0,
	// std::vector<Point2d<float> >& mapped_ring1,
	// std::vector<Point2d<float> >& mapped_ring2,
	// std::vector<Point2d<float> >& mapped_ring3,
	// std::vector<Point2d<float> >& mapped_ring4,
	// std::vector<Point2d<float> >& mapped_ring5,
	// std::vector<Point2d<float> >& mapped_ringC,
	// std::vector<float>& mapped_s0,
	// std::vector<float>& mapped_s1,
	// std::vector<float>& mapped_s2,
	// std::vector<float>& mapped_s3,
	// std::vector<float>& mapped_s4,
	// std::vector<float>& mapped_s5,
	// std::vector<float>& mapped_sc
	// #endif
	//
	// ) {
	// ASSERT(pyramid, "Pyramid is NULL");
	// ASSERT(store.size() == points.size(),
	// "Feature store has not been allocated");
	// size_t num_points = 0;
	// for(size_t i = 0; i < points.size(); i++) {
	//
	// #ifdef FREAK_DEBUG
	// std::vector<Point2d<float> > tmp_p0(6);
	// std::vector<Point2d<float> > tmp_p1(6);
	// std::vector<Point2d<float> > tmp_p2(6);
	// std::vector<Point2d<float> > tmp_p3(6);
	// std::vector<Point2d<float> > tmp_p4(6);
	// std::vector<Point2d<float> > tmp_p5(6);
	// Point2d<float> tmp_pc;
	// float tmp_s0;
	// float tmp_s1;
	// float tmp_s2;
	// float tmp_s3;
	// float tmp_s4;
	// float tmp_s5;
	// float tmp_sc;
	// #endif
	//
	// if(!ExtractFREAK84(store.feature(num_points),
	// pyramid,
	// points[i],
	// points_ring0,
	// points_ring1,
	// points_ring2,
	// points_ring3,
	// points_ring4,
	// points_ring5,
	// sigma_center,
	// sigma_ring0,
	// sigma_ring1,
	// sigma_ring2,
	// sigma_ring3,
	// sigma_ring4,
	// sigma_ring5,
	// expansion_factor
	// #ifdef FREAK_DEBUG
	// ,
	// (float*)&tmp_p0[0],
	// (float*)&tmp_p1[0],
	// (float*)&tmp_p2[0],
	// (float*)&tmp_p3[0],
	// (float*)&tmp_p4[0],
	// (float*)&tmp_p5[0],
	// (float*)&tmp_pc,
	// tmp_s0,
	// tmp_s1,
	// tmp_s2,
	// tmp_s3,
	// tmp_s4,
	// tmp_s5,
	// tmp_sc
	// #endif
	// )) {
	// continue;
	// }
	// #ifdef FREAK_DEBUG
	// mapped_ring0.insert(mapped_ring0.end(), tmp_p0.begin(), tmp_p0.end());
	// mapped_ring1.insert(mapped_ring1.end(), tmp_p1.begin(), tmp_p1.end());
	// mapped_ring2.insert(mapped_ring2.end(), tmp_p2.begin(), tmp_p2.end());
	// mapped_ring3.insert(mapped_ring3.end(), tmp_p3.begin(), tmp_p3.end());
	// mapped_ring4.insert(mapped_ring4.end(), tmp_p4.begin(), tmp_p4.end());
	// mapped_ring5.insert(mapped_ring5.end(), tmp_p5.begin(), tmp_p5.end());
	// mapped_ringC.push_back(tmp_pc);
	//
	// mapped_s0.push_back(tmp_s0);
	// mapped_s1.push_back(tmp_s1);
	// mapped_s2.push_back(tmp_s2);
	// mapped_s3.push_back(tmp_s3);
	// mapped_s4.push_back(tmp_s4);
	// mapped_s5.push_back(tmp_s5);
	// mapped_sc.push_back(tmp_sc);
	// #endif
	//
	// store.point(num_points) = points[i];
	// num_points++;
	// }
	// ASSERT(num_points == points.size(), "Should be same size");
	//
	// // Shrink store down to the number of valid points
	// store.resize(num_points);
	// }
	//
	// } // vision
}
