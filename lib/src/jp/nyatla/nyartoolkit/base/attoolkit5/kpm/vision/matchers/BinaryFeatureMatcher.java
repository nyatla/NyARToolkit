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

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.BinaryHierarchicalClustering;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.Hamming;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.liner_algebr;


public class BinaryFeatureMatcher {

	public BinaryFeatureMatcher(int i_size){
		this.mThreshold=0.7f;
		
	}

      
        /**
         * Match two feature stores.
         * @return Number of matches
         */
        int match(
        		BinaryFeatureStore features1,
        		BinaryFeatureStore features2) {
        
        this.mMatches=new matchStack(features1.size());
        
        if(features1.size() == 0 ||
           features2.size() == 0) {
            return 0;
        }
        
//        mMatches.reserve(features1.size());
        for(int i = 0; i < features1.size(); i++) {
            int first_best = Integer.MAX_VALUE;
            int second_best = Integer.MAX_VALUE;
            int best_index = Integer.MAX_VALUE;
            
            // Search for 1st and 2nd best match
            int f1 = features1.feature(i);
            FeaturePoint p1 = features1.point(i);
            for(int j = 0; j < features2.size(); j++) {
                // Both points should be a MINIMA or MAXIMA
                if(p1.maxima != features2.point(j).maxima) {
                    continue;
                }
                
//                ASSERT(FEATURE_SIZE == 96, "Only 96 bytes supported now");
                int d = Hamming.HammingDistance(VisualDatabase.NUM_BYTES_PER_FEATURE,features1.features(),f1,features2.features(),features2.feature(j));
                if(d < first_best) {
                    second_best = first_best;
                    first_best = d;
                    best_index = (int)j;
                } else if(d < second_best) {
                    second_best = d;
                }
            }
            
            // Check if FIRST_BEST has been set
            if(first_best !=Integer.MAX_VALUE) {
                // If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
                // Otherwise, do a ratio test.
                if(second_best == Integer.MAX_VALUE) {
//                    mMatches.push_back(match_t((int)i, best_index));
                	match_t t=this.mMatches.prePush();
                	t.set(i, best_index);
                } else {
                    // Ratio test
                	double r = (double)first_best / (double)second_best;
                    if(r < mThreshold) {
                    	match_t t=this.mMatches.prePush();
                    	t.set(i, best_index);
//                        mMatches.push_back(match_t((int)i, best_index));
                    }
                }
            }
        }
//        ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
        return mMatches.getLength();
    }
    
        /**
         * Match two feature stores with an index on features2.
         * @return Number of matches
         */
        int match(BinaryFeatureStore features1,
                     BinaryFeatureStore features2,
                     BinaryHierarchicalClustering index2)
 {
            this.mMatches=new matchStack(features1.size());
            
            if(features1.size() == 0 ||
               features2.size() == 0) {
                return 0;
            }
            
//            mMatches.reserve(features1.size());
            for(int i = 0; i < features1.size(); i++) {
                int first_best = Integer.MAX_VALUE;//std::numeric_limits<unsigned int>::max();
                int second_best = Integer.MAX_VALUE;//std::numeric_limits<unsigned int>::max();
                int best_index = Integer.MAX_VALUE;//std::numeric_limits<int>::max();
                
                // Perform an indexed nearest neighbor lookup
                int f1 = features1.feature(i);
                index2.query(features1.features(),f1);
                
                FeaturePoint p1 = features1.point(i);
                
                // Search for 1st and 2nd best match
                int[] v= index2.reverseIndex();
                for(int j = 0; j < v.length; j++) {
                    // Both points should be a MINIMA or MAXIMA
                    if(p1.maxima != features2.point(v[j]).maxima) {
                        continue;
                    }
                    
//                    ASSERT(FEATURE_SIZE == 96, "Only 96 bytes supported now");
//                    int d = Hamming.HammingDistance(features1.features(),f1,features2.features(),features2.feature(v[j]));
                    int d = Hamming.HammingDistance(VisualDatabase.NUM_BYTES_PER_FEATURE,features1.features(),f1,features2.features(),features2.feature(v[j]));
                    if(d < first_best) {
                        second_best = first_best;
                        first_best = d;
                        best_index = v[j];
                    } else if(d < second_best) {
                        second_best = d;
                    }
                }
                
                // Check if FIRST_BEST has been set
                if(first_best != Integer.MAX_VALUE) {
//                    ASSERT(best_index != std::numeric_limits<size_t>::max(), "Something strange");
                    
                    // If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
                    // Otherwise, do a ratio test.
                    if(second_best == Integer.MAX_VALUE) {
                    	match_t t=mMatches.prePush();
                    	t.set((int)i, best_index);
                    } else {
                        // Ratio test
                    	double r = (double)first_best / (double)second_best;
                        if(r < mThreshold) {
//                            mMatches.push_back(match_t((int)i, best_index));
                        	match_t t=mMatches.prePush();
                        	t.set((int)i, best_index);
                        }
                    }
                }
            }
//            ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
            return mMatches.getLength();
        }
        private static double sqr(double a){
        	return a*a;
        }
        /**
         * Match two feature stores given a homography from the features in store 1 to
         * store 2. The THRESHOLD is a spatial threshold in pixels to restrict the number
         * of feature comparisons.
         * @return Number of matches
         */
        int match(BinaryFeatureStore features1,
                     BinaryFeatureStore features2,
                     double[] H, //float[9]
                     double tr)
        {

        	this.mMatches=new matchStack(features1.size());
            
            if(features1.size() == 0 ||
               features2.size() == 0) {
                return 0;
            }
            
            double tr_sqr = sqr(tr);
            
            double[] Hinv=new double[9];
            if(!liner_algebr.MatrixInverse3x3(Hinv, H, 0.f)) {
//                ASSERT(0, "Failed to compute matrix inverse");
                return 0;
            }
            
//            mMatches.reserve(features1.size());
            for(int i = 0; i < features1.size(); i++) {
                int first_best = Integer.MAX_VALUE;//std::numeric_limits<unsigned int>::max();
                int second_best = Integer.MAX_VALUE;//std::numeric_limits<unsigned int>::max();
                int best_index = Integer.MAX_VALUE;//std::numeric_limits<int>::max();
                
                int f1 = features1.feature(i);
                FeaturePoint p1 = features1.point(i);
                
                // Map p1 to p2 space through H
                double xp1, yp1;
                double[] tmp=new double[2];
                homography.MultiplyPointHomographyInhomogenous(tmp, Hinv, p1.x, p1.y);
                xp1=tmp[0];
                yp1=tmp[1];
                
                // Search for 1st and 2nd best match
                for(int j = 0; j < features2.size(); j++) {
                    FeaturePoint p2 = features2.point(j);
                    
                    // Both points should be a MINIMA or MAXIMA
                    if(p1.maxima != p2.maxima) {
                        continue;
                    }
                    
                    // Check spatial constraint
                    if(sqr(xp1-p2.x) + sqr(yp1-p2.y) > tr_sqr) {
                        continue;
                    }
                    
//                    ASSERT(FEATURE_SIZE == 96, "Only 96 bytes supported now");
                    //int d = HammingDistance768((unsigned int*)f1,(unsigned int*)features2->feature(j));
                    int d = Hamming.HammingDistance768(features1.features(),f1,features2.features(),features2.feature(j));
                    if(d < first_best) {
                        second_best = first_best;
                        first_best = d;
                        best_index = (int)j;
                    } else if(d < second_best) {
                        second_best = d;
                    }
                }
                
                // Check if FIRST_BEST has been set
                if(first_best != Integer.MAX_VALUE) {
//                    ASSERT(best_index != std::numeric_limits<size_t>::max(), "Something strange");
                    
                    // If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
                    // Otherwise, do a ratio test.
                    if(second_best == Integer.MAX_VALUE) {
                    	match_t t=mMatches.prePush();
                    	t.set((int)i, best_index);             	
//                        mMatches.push_back(match_t((int)i, best_index));
                    } else {
                        // Ratio test
                    	double r = (double)first_best / (double)second_best;
                        if(r < mThreshold) {
//                            mMatches.push_back(match_t((int)i, best_index));
                        	match_t t=mMatches.prePush();
                        	t.set((int)i, best_index);             	
                        }
                    }
                }
            }
//            ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
            return mMatches.getLength();
        }        
        
        
        
//        
//        /**
//         * @return Vector of matches after a call to MATCH.
//         */
//        inline const matches_t& matches() const { return mMatches; }
//        
//    private:
        
        // Vector of indices that represent matches
        private matchStack mMatches;
        
        // Threshold on the 1st and 2nd best matches
        private double mThreshold;
        


        
        /**
         * @return Vector of matches after a call to MATCH.
         */
        matchStack matches() {
        	return this.mMatches;
        }        

	
	
}
