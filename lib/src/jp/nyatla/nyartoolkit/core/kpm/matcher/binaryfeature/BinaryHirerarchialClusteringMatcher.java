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
package jp.nyatla.nyartoolkit.core.kpm.matcher.binaryfeature;

import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.BinaryHierarchicalNode;
import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.selector.BinaryHierarchicalSelector_O2;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;


final public class BinaryHirerarchialClusteringMatcher extends BinaryFeatureMatcher
{
	public BinaryHierarchicalSelector_O2 _selector;
	public BinaryHirerarchialClusteringMatcher() {
		super();
		this._selector=new BinaryHierarchicalSelector_O2(8,100);
	}

	/**
	 * Match two feature stores with an index on features2.
	 * 
	 * @return Number of matches
	 */
	@Override
	public int match(FreakFeaturePointStack i_query, Keyframe i_key_frame,FeaturePairStack i_maches)
	{
		//indexが無いときはベースクラスを使う。
		BinaryHierarchicalNode index2=i_key_frame.getIndex();
		if(index2==null){
			return super.match(i_query, i_key_frame, i_maches);
		}
		FreakMatchPointSetStack ref=i_key_frame.getFeaturePointSet();
		if (i_query.getLength()*ref.getLength() == 0) {
			return 0;
		}
		FreakFeaturePoint[] query_buf=i_query.getArray();
		int q_len=i_query.getLength();
		
		for (int i = 0; i < q_len; i++) {
			int first_best = Integer.MAX_VALUE;// std::numeric_limits<unsigned int>::max();
			int second_best = Integer.MAX_VALUE;// std::numeric_limits<unsigned int>::max();
			FreakMatchPointSetStack.Item best_index = null;// std::numeric_limits<int>::max();

			// Perform an indexed nearest neighbor lookup
			FreakFeaturePoint fptr1 = query_buf[i];
			
			int num_of_fp=this._selector.query(index2,fptr1.descripter);
			// Search for 1st and 2nd best match
			FreakMatchPointSetStack.Item[] v = this._selector._result;
			for (int j = 0; j < num_of_fp; j++) {
				FreakFeaturePoint fptr2=v[j];
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

				// If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
				// Otherwise, do a ratio test.
				if (second_best == Integer.MAX_VALUE) {
					FeaturePairStack.Item t = i_maches.prePush();
					t.query=fptr1;
					t.ref=best_index;
				} else {
					// Ratio test
					double r = (double) first_best / (double) second_best;
					if (r < mThreshold) {
						// mMatches.push_back(match_t((int)i, best_index));
						FeaturePairStack.Item t = i_maches.prePush();
						t.query=fptr1;
						t.ref=best_index;
					}
				}
			}
		}
		return i_maches.getLength();
	}
}
