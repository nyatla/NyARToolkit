package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.keyframe.BinaryHierarchicalClustering;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;


public class BinaryFeatureMatcher {

	public BinaryFeatureMatcher() {
		this.mThreshold = 0.7f;

	}



	/**
	 * Match two feature stores.
	 * 
	 * @return Number of matches
	 */
	public int match(FreakFeaturePointStack features1, FreakMatchPointSetStack features2,matchStack i_maches)
	{


		if (features1.getLength() == 0 || features2.getLength() == 0) {
			return 0;
		}

		// mMatches.reserve(features1.size());
		for (int i = 0; i < features1.getLength(); i++) {
			int first_best = Integer.MAX_VALUE;
			int second_best = Integer.MAX_VALUE;
			int best_index = Integer.MAX_VALUE;

			// Search for 1st and 2nd best match
			FreakFeaturePoint p1 = features1.getItem(i);
			for (int j = 0; j < features2.getLength(); j++) {
				// Both points should be a MINIMA or MAXIMA
				if (p1.maxima != features2.getItem(j).maxima) {
					continue;
				}

				// ASSERT(FEATURE_SIZE == 96, "Only 96 bytes supported now");
				int d = features1.getItem(i).descripter.hammingDistance(features2.getItem(j).descripter);
				if (d < first_best) {
					second_best = first_best;
					first_best = d;
					best_index = (int) j;
				} else if (d < second_best) {
					second_best = d;
				}
			}

			// Check if FIRST_BEST has been set
			if (first_best != Integer.MAX_VALUE) {
				// If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
				// Otherwise, do a ratio test.
				if (second_best == Integer.MAX_VALUE) {
					// mMatches.push_back(match_t((int)i, best_index));
					match_t t = i_maches.prePush();
					t.set(i, best_index);
				} else {
					// Ratio test
					double r = (double) first_best / (double) second_best;
					if (r < mThreshold) {
						match_t t = i_maches.prePush();
						t.set(i, best_index);
						// mMatches.push_back(match_t((int)i, best_index));
					}
				}
			}
		}
		// ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
		return i_maches.getLength();
	}

	/**
	 * Match two feature stores with an index on features2.
	 * 
	 * @return Number of matches
	 */
	public int match(FreakFeaturePointStack i_features1, FreakMatchPointSetStack features2, BinaryHierarchicalClustering index2,matchStack i_maches)
	{
		if (i_features1.getLength() == 0 || features2.getLength() == 0) {
			return 0;
		}

		// mMatches.reserve(features1.size());
		for (int i = 0; i < i_features1.getLength(); i++) {
			int first_best = Integer.MAX_VALUE;// std::numeric_limits<unsigned int>::max();
			int second_best = Integer.MAX_VALUE;// std::numeric_limits<unsigned int>::max();
			int best_index = Integer.MAX_VALUE;// std::numeric_limits<int>::max();

			// Perform an indexed nearest neighbor lookup
			FreakFeaturePoint fptr1 = i_features1.getItem(i);
			index2.query(fptr1.descripter);


			// Search for 1st and 2nd best match
			int[] v = index2.reverseIndex();
			for (int j = 0; j < v.length; j++) {
				FreakFeaturePoint fptr2=features2.getItem(v[j]);
				// Both points should be a MINIMA or MAXIMA
				if (fptr1.maxima != fptr2.maxima) {
					continue;
				}

				int d = fptr1.descripter.hammingDistance(fptr2.descripter);
				if (d < first_best) {
					second_best = first_best;
					first_best = d;
					best_index = v[j];
				} else if (d < second_best) {
					second_best = d;
				}
			}

			// Check if FIRST_BEST has been set
			if (first_best != Integer.MAX_VALUE) {
				// ASSERT(best_index != std::numeric_limits<size_t>::max(), "Something strange");

				// If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
				// Otherwise, do a ratio test.
				if (second_best == Integer.MAX_VALUE) {
					match_t t = i_maches.prePush();
					t.set((int) i, best_index);
				} else {
					// Ratio test
					double r = (double) first_best / (double) second_best;
					if (r < mThreshold) {
						// mMatches.push_back(match_t((int)i, best_index));
						match_t t = i_maches.prePush();
						t.set((int) i, best_index);
					}
				}
			}
		}
		// ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
		return i_maches.getLength();
	}



	/**
	 * Match two feature stores given a homography from the features in store 1 to store 2. The THRESHOLD is a spatial
	 * threshold in pixels to restrict the number of feature comparisons.
	 * 
	 * @return Number of matches
	 */
	int match(FreakFeaturePointStack features1, FreakMatchPointSetStack features2, NyARDoubleMatrix33 H,double tr,matchStack i_maches)
	{
		if (features1.getLength() == 0 || features2.getLength() == 0) {
			return 0;
		}

		double tr_sqr = tr*tr;

		HomographyMat ht = new HomographyMat();
		ht.setValue(H);
		if (!ht.inverse(ht)) {
			return 0;
		}
		NyARDoublePoint2d tmp = new NyARDoublePoint2d();
		// mMatches.reserve(features1.size());
		for (int i = 0; i < features1.getLength(); i++) {
			int first_best = Integer.MAX_VALUE;// std::numeric_limits<unsigned int>::max();
			int second_best = Integer.MAX_VALUE;// std::numeric_limits<unsigned int>::max();
			int best_index = Integer.MAX_VALUE;// std::numeric_limits<int>::max();


			FreakFeaturePoint fptr1 = features1.getItem(i);

			// Map p1 to p2 space through H
			ht.multiplyPointHomographyInhomogenous(fptr1.x, fptr1.y, tmp);
			

			// Search for 1st and 2nd best match
			for (int j = 0; j < features2.getLength(); j++) {
				FreakFeaturePoint fptr2 = features2.getItem(j);

				// Both points should be a MINIMA or MAXIMA
				if (fptr1.maxima != fptr2.maxima) {
					continue;
				}
				double tx=(tmp.x - fptr2.x);
				double ty=(tmp.y - fptr2.y);
				// Check spatial constraint
				if ((tx*tx)+(ty*ty) > tr_sqr) {
					continue;
				}

				// ASSERT(FEATURE_SIZE == 96, "Only 96 bytes supported now");
				// int d = HammingDistance768((unsigned int*)f1,(unsigned int*)features2->feature(j));
				int d = fptr1.descripter.hammingDistance(fptr2.descripter);
				if (d < first_best) {
					second_best = first_best;
					first_best = d;
					best_index = (int) j;
				} else if (d < second_best) {
					second_best = d;
				}
			}

			// Check if FIRST_BEST has been set
			if (first_best != Integer.MAX_VALUE) {
				// ASSERT(best_index != std::numeric_limits<size_t>::max(), "Something strange");

				// If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
				// Otherwise, do a ratio test.
				if (second_best == Integer.MAX_VALUE) {
					match_t t = i_maches.prePush();
					t.set((int) i, best_index);
					// mMatches.push_back(match_t((int)i, best_index));
				} else {
					// Ratio test
					double r = (double) first_best / (double) second_best;
					if (r < this.mThreshold) {
						// mMatches.push_back(match_t((int)i, best_index));
						match_t t = i_maches.prePush();
						t.set((int) i, best_index);
					}
				}
			}
		}
		// ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
		return i_maches.getLength();
	}





	// Threshold on the 1st and 2nd best matches
	private double mThreshold;




}
