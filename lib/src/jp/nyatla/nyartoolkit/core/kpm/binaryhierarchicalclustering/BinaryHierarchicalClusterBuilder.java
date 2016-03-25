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
