package jp.nyatla.nyartoolkit.core.kpm.keyframe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter768;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.Node;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.NodePtrStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.PriorityQueueItem;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.core.math.NyARLCGsRandomizer;
import jp.nyatla.nyartoolkit.j2se.ArrayUtils;

public class BinaryHierarchicalClustering {
	public class Queue extends PriorityQueue<PriorityQueueItem> {
		private static final long serialVersionUID = 6120329703806461621L;

	}

	// Clustering algorithm
	final private BinarykMedoids mBinarykMedoids;
	// Minimum number of feature at a node
	final private int mMinFeaturePerNode;

	public BinaryHierarchicalClustering(int i_feature_num,int i_NumHypotheses, int i_NumCenters, int i_MaxNodesToPop,
			int i_MinFeaturesPerNode) {
		this.mMaxNodesToPop = i_MaxNodesToPop;
		this.mMinFeaturePerNode = i_MinFeaturesPerNode;
		this.mBinarykMedoids = new BinarykMedoids(i_feature_num,new NyARLCGsRandomizer(1234), 8, i_NumHypotheses);
	}

	/**
	 * Query the tree for a reverse index.
	 */
	public int query(LongDescripter768 feature) {
		mNumNodesPopped = 0;
		mQueryReverseIndex = new int[0];

		// while(!mQueue.empty()) {
		// mQueue.pop();
		// }
		this.mQueue.clear();

		this.query(mQueue, mRoot, feature);

		return (int) mQueryReverseIndex.length;
	}

	// /**
	// * @return Reverse index after a QUERY.
	// */
	public int[] reverseIndex() {
		return this.mQueryReverseIndex;
	}

	//
	// Root node
	private Node mRoot;
	//
	// // Clustering algorithm
	// kmedoids_t mBinarykMedoids;
	//
	// Reverse index for query
	int[] mQueryReverseIndex;
	//
	// Node queue
	private Queue mQueue = new Queue();
	//
	// Number of nodes popped off the priority queue
	private int mNumNodesPopped;

	// Maximum nodes to pop off the priority queue
	private final int mMaxNodesToPop;

	private int mNextNodeId = 0;

	/**
	 * Get the next node id
	 */
	private synchronized int nextNodeId() {
		return this.mNextNodeId++;
	}

	private static int[] appendArray(int[] f, int[] s) {
		int[] n = new int[f.length + s.length];
		System.arraycopy(f, 0, n, 0, f.length);
		System.arraycopy(s, 0, n, f.length, s.length);
		return n;
	}

	/**
	 * Recursive function query function.
	 */
	private void query(Queue queue, Node node, LongDescripter768 feature) {

		if (node.leaf()) {
			// Insert all the leaf indices into the query index
			// mQueryReverseIndex.insert(mQueryReverseIndex.end(),node.reverseIndex().begin(),node.reverseIndex().end());
			// 追記
			this.mQueryReverseIndex = appendArray(this.mQueryReverseIndex, node.reverseIndex());
			return;
		} else {
			NodePtrStack nodes = new NodePtrStack(1000);
			node.nearest(nodes, queue, feature);
			for (int i = 0; i < nodes.getLength(); i++) {
				query(queue, nodes.getItem(i), feature);
			}

			// Pop a node from the queue
			if (this.mNumNodesPopped < this.mMaxNodesToPop && !queue.isEmpty()) {
				// Node q = queue.top().node();
				Node q = queue.poll().node();// pop();
				// queue.pop();
				this.mNumNodesPopped++;
				query(queue, q, feature);
			}
		}
	}

	void build(FreakMatchPointSetStack features) {
		int[] indices = new int[features.getLength()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = (int) i;
		}
		this.build(features.getArray(), indices, indices.length);
	}

	private void build(FreakFeaturePoint[] features, int[] indices, int num_indices) {
		this.mRoot = new Node(this.nextNodeId());
		this.mRoot.leaf(false);
		this.build(this.mRoot, features, indices, num_indices);
		return;
	}

	private void build(Node node, FreakFeaturePoint[] features, int[] indices, int num_indices) {
		// Check if there are enough features to cluster.
		// If not, then assign all features to the same cluster.
		if (num_indices <= math_utils.max2(mBinarykMedoids.k(), this.mMinFeaturePerNode)) {
			node.leaf(true);
			node.resizeReverseIndex(num_indices);
			for (int i = 0; i < num_indices; i++) {
				node.reverseIndex()[i] = indices[i];
			}
		} else {
			Map<Integer, List<Integer>> cluster_map = new TreeMap<Integer, List<Integer>>();

			// Perform clustering
			// Get a list of features for each cluster center
			int[] assignment =this.mBinarykMedoids.assign(features, indices, num_indices);

			// ASSERT(assignment.size() == num_indices, "Assignment size wrong");
			for (int i = 0; i < num_indices; i++) {
				// ASSERT(assignment[i] != -1, "Assignment is invalid");
				// ASSERT(assignment[i] < num_indices, "Assignment out of range");
				// ASSERT(indices[assignment[i]] < num_features, "Assignment out of range");

				// cluster_map[indices[assignment[i]]].push_back(indices[i]);
				List<Integer> li = cluster_map.get(indices[assignment[i]]);
				if (li == null) {
					li = new ArrayList<Integer>();
					cluster_map.put(indices[assignment[i]], li);
				}
				li.add(indices[i]);
			}

			// If there is only 1 cluster then make this node a leaf
			if (cluster_map.size() == 1) {
				node.leaf(true);
				node.resizeReverseIndex(num_indices);
				for (int i = 0; i < num_indices; i++) {
					node.reverseIndex()[i] = indices[i];
				}
				return;
			}
			// Create a new node for each cluster center
			node.reserveChildren(cluster_map.size());
			for (Map.Entry<Integer, List<Integer>> l : cluster_map.entrySet()) {
				int first = l.getKey();
				Node new_node = new Node(nextNodeId(), features[first]);
				new_node.leaf(false);

				// Make the new node a child of the input node
				node.children_push_back(new_node);

				// Recursively build the tree
				int[] v = ArrayUtils.toIntArray_impl(l.getValue(), 0, l.getValue().size());

				this.build(new_node, features, v, (int) v.length);
			}

		}
	}
}
