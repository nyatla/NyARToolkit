package jp.nyatla.nyartoolkit.core.kpm.vision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.BinarykMedoids;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePointStack;
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

	final public int _NUM_BYTES_PER_FEATURE;
    // Clustering algorithm
	final private BinarykMedoids mBinarykMedoids;
    // Maximum nodes to pop off the priority queue
//    private int mMaxNodesToPop;
    // Minimum number of feature at a node
    private final int mMinFeaturePerNode;
    
	public BinaryHierarchicalClustering(int NUM_BYTES_PER_FEATURE,int i_NumHypotheses,int i_NumCenters,int i_MaxNodesToPop,int i_MinFeaturesPerNode) {
		this._NUM_BYTES_PER_FEATURE = NUM_BYTES_PER_FEATURE;
		
//        mIndex.setNumHypotheses(128);
//        mIndex.setNumCenters(8);
        this.mMaxNodesToPop=i_MaxNodesToPop;
        this.mMinFeaturePerNode=i_MinFeaturesPerNode;
        this.mBinarykMedoids=new BinarykMedoids(NUM_BYTES_PER_FEATURE,new NyARLCGsRandomizer(1234),8,i_NumHypotheses);
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
	public int query(LongDescripter feature) {
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
	private Queue mQueue=new Queue();
	//
	// Number of nodes popped off the priority queue
	private int mNumNodesPopped;

	// Maximum nodes to pop off the priority queue
	private final int mMaxNodesToPop;
	
	private int mNextNodeId=0;
    /**
     * Get the next node id
     */
    private synchronized int nextNodeId(){
        return this.mNextNodeId++;
    }
    
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
	private void query(Queue queue, Node node, LongDescripter feature)
	{

		if (node.leaf()) {
			// Insert all the leaf indices into the query index
			// mQueryReverseIndex.insert(mQueryReverseIndex.end(),node.reverseIndex().begin(),node.reverseIndex().end());
			// 追記
			mQueryReverseIndex = appendArray(mQueryReverseIndex,
					node.reverseIndex());
			return;
		} else {
			NodePtrStack nodes = new NodePtrStack(1000);
			node.nearest(nodes, queue, feature);
			for (int i = 0; i < nodes.getLength(); i++) {
				query(queue, nodes.getItem(i), feature);
			}

			// Pop a node from the queue
			if (mNumNodesPopped < mMaxNodesToPop && !queue.isEmpty()) {
				// Node q = queue.top().node();
				Node q = queue.poll().node();// pop();
				// queue.pop();
				mNumNodesPopped++;
				query(queue, q, feature);
			}
		}
	}
    void build(FreakFeaturePointStack features) {
    	int[] indices=new int[features.getLength()];
        for(int i = 0; i < indices.length; i++) {
            indices[i] = (int)i;
        }
        this.build(features, indices,indices.length);
    }
    void build(FreakFeaturePointStack features, int[] indices, int num_indices) {
        mRoot=new Node(this.nextNodeId());
        mRoot.leaf(false);
        this.build(mRoot, features, indices, num_indices);
        return;
    }
    
    void build(Node node, FreakFeaturePointStack features, int[] indices, int num_indices) {
        // Check if there are enough features to cluster.
        // If not, then assign all features to the same cluster.
        if(num_indices <= math_utils.max2(mBinarykMedoids.k(),this.mMinFeaturePerNode)) {
            node.leaf(true);
            node.resizeReverseIndex(num_indices);
            for(int i = 0; i < num_indices; i++) {
                node.reverseIndex()[i] = indices[i];
            }
        } else {
        	Map<Integer,List<Integer>> cluster_map=new TreeMap<Integer,List<Integer>>();
            
            // Perform clustering
            mBinarykMedoids.assign(features, indices, num_indices);
            
            // Get a list of features for each cluster center
            int[] assignment = mBinarykMedoids.assignment();
//            ASSERT(assignment.size() == num_indices, "Assignment size wrong");
            for(int i = 0; i < assignment.length; i++) {
//                ASSERT(assignment[i] != -1, "Assignment is invalid");
//                ASSERT(assignment[i] < num_indices, "Assignment out of range");
//                ASSERT(indices[assignment[i]] < num_features, "Assignment out of range");
                
//                cluster_map[indices[assignment[i]]].push_back(indices[i]);
            	List<Integer> li=cluster_map.get(indices[assignment[i]]);
            	if(li==null){
            		li=new ArrayList<Integer>();
            		cluster_map.put(indices[assignment[i]],li);
            	}
            	li.add(indices[i]);
            }

            // If there is only 1 cluster then make this node a leaf
            if(cluster_map.size() == 1) {
                node.leaf(true);
                node.resizeReverseIndex(num_indices);
                for(int i = 0; i < num_indices; i++) {
                    node.reverseIndex()[i] = indices[i];
                }
                return;
            }
            // Create a new node for each cluster center
            node.reserveChildren(cluster_map.size());
            for(Map.Entry<Integer,List<Integer>> l : cluster_map.entrySet())
            {
            	int first=l.getKey();
                Node new_node = new Node(nextNodeId(),features.getItem(first));
                new_node.leaf(false);
                
                // Make the new node a child of the input node
                node.children_push_back(new_node);
                
                // Recursively build the tree
                int[] v=ArrayUtils.toIntArray_impl(l.getValue(),0,l.getValue().size());
                
                this.build(new_node, features, v, (int)v.length);
            }            
            


        }
    }
}
