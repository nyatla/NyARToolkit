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
package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.math.NyARLCGsRandomizer;
import jp.nyatla.nyartoolkit.j2se.ArrayUtils;

public class BinaryHierarchicalClusterBuilder
{

	final private BinarykMedoids mBinarykMedoids;
	// Minimum number of feature at a node
	final private int mMinFeaturePerNode;
	public BinaryHierarchicalClusterBuilder(int i_feature_num,int i_NumHypotheses,int i_MinFeaturesPerNode)
	{
		this(i_feature_num,i_NumHypotheses,8,i_MinFeaturesPerNode);
	}
	public BinaryHierarchicalClusterBuilder(int i_feature_num,int i_NumHypotheses, int i_NumCenters, int i_MinFeaturesPerNode)
	{
		this.mMinFeaturePerNode = i_MinFeaturesPerNode;
		this.mBinarykMedoids = new BinarykMedoids(i_feature_num,new NyARLCGsRandomizer(1234), i_NumCenters, i_NumHypotheses);
	}

	// Clustering algorithm
	private int mNextNodeId = 0;
	/**
	 * Get the next node id
	 */
	private synchronized int nextNodeId() {
		return this.mNextNodeId++;
	}
	public BinaryHierarchicalNode build(FreakMatchPointSetStack features)
	{
		this.mNextNodeId=0;
		int[] indices = new int[features.getLength()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = (int) i;
		}
		return this.build(features.getArray(), null,indices, indices.length);
	}

	private BinaryHierarchicalNode build(FreakMatchPointSetStack.Item[] features,FreakFeaturePoint i_center,int[] i_indices, int num_indices)
	{
		int t=mBinarykMedoids.k();
		if(t<this.mMinFeaturePerNode){
			t=this.mMinFeaturePerNode;
		}
		if (num_indices <= t) {
			FreakMatchPointSetStack.Item[] index=intArray2FeaturePointArray(features,i_indices);
			return new BinaryHierarchicalNode(this.nextNodeId(),i_center,true,index,null); 
		}
		Map<Integer, List<Integer>> cluster_map = new TreeMap<Integer, List<Integer>>();

		// Perform clustering
		// Get a list of features for each cluster center
		int[] assignment =this.mBinarykMedoids.assign(features, i_indices, num_indices);

		// ASSERT(assignment.size() == num_indices, "Assignment size wrong");
		for (int i = 0; i < num_indices; i++) {
			// ASSERT(assignment[i] != -1, "Assignment is invalid");
			// ASSERT(assignment[i] < num_indices, "Assignment out of range");
			// ASSERT(indices[assignment[i]] < num_features, "Assignment out of range");

			List<Integer> li = cluster_map.get(i_indices[assignment[i]]);
			if (li == null) {
				li = new ArrayList<Integer>();
				cluster_map.put(i_indices[assignment[i]], li);
			}
			li.add(i_indices[i]);
		}

		// If there is only 1 cluster then make this node a leaf
		if (cluster_map.size() == 1) {
			FreakMatchPointSetStack.Item[] index=intArray2FeaturePointArray(features,i_indices);
			return new BinaryHierarchicalNode(this.nextNodeId(),i_center,true,index,null);
		}
		int n=0;
		BinaryHierarchicalNode[] cl=new BinaryHierarchicalNode[cluster_map.size()];
		// Create a new node for each cluster center
		for (Map.Entry<Integer, List<Integer>> l : cluster_map.entrySet()) {
			int first = l.getKey();

			// Recursively build the tree
			int[] v = ArrayUtils.toIntArray_impl(l.getValue(), 0, l.getValue().size());
			cl[n]=this.build(features,features[first], v,v.length);
			n++;
		}
		return new BinaryHierarchicalNode(this.nextNodeId(),i_center,false,null,cl);
	}
	/**
	 * インデクス番号を特徴点配列に反変換する。
	 * @param features
	 * @param indics
	 * @return
	 */
	private static FreakMatchPointSetStack.Item[] intArray2FeaturePointArray(FreakMatchPointSetStack.Item[] features,int[] indics)
	{
		FreakMatchPointSetStack.Item[] r=new FreakMatchPointSetStack.Item[indics.length];
		for(int i=0;i<r.length;i++){
			r[i]=features[indics[i]];
		}
		return r;
	}
}
