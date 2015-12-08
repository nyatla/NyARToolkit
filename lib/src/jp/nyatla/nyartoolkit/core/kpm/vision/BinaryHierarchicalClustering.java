package jp.nyatla.nyartoolkit.core.kpm.vision;

import java.util.PriorityQueue;

import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.Node;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.NodePtrStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.PriorityQueueItem;

public class BinaryHierarchicalClustering {
	public class Queue extends PriorityQueue<PriorityQueueItem> {
		private static final long serialVersionUID = 6120329703806461621L;

	}

	final private int _NUM_BYTES_PER_FEATURE;

	public BinaryHierarchicalClustering(int NUM_BYTES_PER_FEATURE) {
		this._NUM_BYTES_PER_FEATURE = NUM_BYTES_PER_FEATURE;
	}

	// typedef Node<NUM_BYTES_PER_FEATURE> node_t;
	// typedef std::unique_ptr<node_t> node_ptr_t;
	// typedef BinarykMedoids<NUM_BYTES_PER_FEATURE> kmedoids_t;
	// typedef std::unordered_map<int, std::vector<int> > cluster_map_t;
	//
	// typedef PriorityQueueItem<NUM_BYTES_PER_FEATURE> queue_item_t;
	// typedef std::priority_queue<queue_item_t> queue_t;
	//
	//
	//
	// /**
	// * Build the tree.
	// */
	// void build(const unsigned char* features, int num_features);
	//
	/**
	 * Query the tree for a reverse index.
	 */
	public int query(byte[] feature, int i_ptr) {
		mNumNodesPopped = 0;
		mQueryReverseIndex = new int[0];

		// while(!mQueue.empty()) {
		// mQueue.pop();
		// }
		this.mQueue.clear();

		query(mQueue, mRoot, feature, i_ptr);

		return (int) mQueryReverseIndex.length;
	}

	// /**
	// * @return Reverse index after a QUERY.
	// */
	public int[] reverseIndex() {
		return this.mQueryReverseIndex;
	}

	//
	// /**
	// * Set/Get number of hypotheses
	// */
	// inline void setNumHypotheses(int n) {
	// mBinarykMedoids.setNumHypotheses(n); }
	// inline int numHypotheses() const { return
	// mBinarykMedoids.numHypotheses(); }
	//
	// /**
	// * Set/Get number of center.
	// */
	// inline void setNumCenters(int k) { mBinarykMedoids.setk(k); }
	// inline int numCenters() const { return mBinarykMedoids.k(); }
	//
	// /**
	// * Set/Get max nodes to pop from queue.
	// */
	// inline void setMaxNodesToPop(int n) { mMaxNodesToPop = n; }
	// inline int maxNodesPerPop() const { return mMaxNodesToPop; }
	//
	// /**
	// * Set/Get minimum number of features per node.
	// */
	// inline void setMinFeaturesPerNode(int n) { mMinFeaturePerNode = n; }
	// inline int minFeaturesPerNode() const { return mMinFeaturePerNode; }
	//
	// private:
	//
	// // Random number seed
	// int mRandSeed;
	//
	// // Counter for node id's
	// int mNextNodeId;
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
	private Queue mQueue;
	//
	// Number of nodes popped off the priority queue
	private int mNumNodesPopped;

	// Maximum nodes to pop off the priority queue
	int mMaxNodesToPop;

	//
	// // Minimum number of feature at a node
	// int mMinFeaturePerNode;
	//
	// /**
	// * Get the next node id
	// */
	// inline int nextNodeId() {
	// return mNextNodeId++;
	// }
	//
	// /**
	// * Private build function with a set of indices.
	// */
	// void build(const unsigned char* features, int num_features, const int*
	// indices, int num_indices);
	//
	// /**
	// * Recursive function to build the tree.
	// */
	// void build(node_t* node, const unsigned char* features, int num_features,
	// const int* indices, int num_indices);
	//
	private static int[] appendArray(int[] f, int[] s) {
		int[] n = new int[f.length + s.length];
		System.arraycopy(f, 0, n, 0, f.length);
		System.arraycopy(s, 0, n, f.length, s.length);
		return n;
	}

	/**
	 * Recursive function query function.
	 */
	private void query(Queue queue, Node node, byte[] feature, int i_ptr) {

		if (node.leaf()) {
			// Insert all the leaf indices into the query index
			// mQueryReverseIndex.insert(mQueryReverseIndex.end(),node.reverseIndex().begin(),node.reverseIndex().end());
			// 追記
			mQueryReverseIndex = appendArray(mQueryReverseIndex,
					node.reverseIndex());
			return;
		} else {
			NodePtrStack nodes = new NodePtrStack(1000);
			node.nearest(nodes, queue, feature, i_ptr);
			for (int i = 0; i < nodes.getLength(); i++) {
				query(queue, nodes.getItem(i), feature, i_ptr);
			}

			// Pop a node from the queue
			if (mNumNodesPopped < mMaxNodesToPop && !queue.isEmpty()) {
				// Node q = queue.top().node();
				Node q = queue.poll().node();// pop();
				// queue.pop();
				mNumNodesPopped++;
				query(queue, q, feature, i_ptr);
			}
		}
	}
}
