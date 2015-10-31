package jp.nyatla.nyartoolkit.core.kpm.vision;

public class BinaryHierarchicalClustering {
	final private int _NUM_BYTES_PER_FEATURE;
	public BinaryHierarchicalClustering(int NUM_BYTES_PER_FEATURE){
		this._NUM_BYTES_PER_FEATURE=NUM_BYTES_PER_FEATURE;
	}
    typedef Node<NUM_BYTES_PER_FEATURE> node_t;
    typedef std::unique_ptr<node_t> node_ptr_t;
    typedef BinarykMedoids<NUM_BYTES_PER_FEATURE> kmedoids_t;
    typedef std::unordered_map<int, std::vector<int> > cluster_map_t;
    
    typedef PriorityQueueItem<NUM_BYTES_PER_FEATURE> queue_item_t;
    typedef std::priority_queue<queue_item_t> queue_t;
    
    BinaryHierarchicalClustering();
    ~BinaryHierarchicalClustering() {}
    
    /**
     * Build the tree.
     */
    void build(const unsigned char* features, int num_features);
    
    /**
     * Query the tree for a reverse index.
     */
    int query(const unsigned char* feature) const;
    
    /**
     * @return Reverse index after a QUERY.
     */
    inline const std::vector<int>& reverseIndex() const { return mQueryReverseIndex; }

    /**
     * Set/Get number of hypotheses
     */
    inline void setNumHypotheses(int n) { mBinarykMedoids.setNumHypotheses(n); }
    inline int numHypotheses() const { return mBinarykMedoids.numHypotheses(); }
    
    /**
     * Set/Get number of center.
     */
    inline void setNumCenters(int k) { mBinarykMedoids.setk(k); }
    inline int numCenters() const { return mBinarykMedoids.k(); }
    
    /**
     * Set/Get max nodes to pop from queue.
     */
    inline void setMaxNodesToPop(int n) { mMaxNodesToPop = n; }
    inline int maxNodesPerPop() const { return mMaxNodesToPop; }
    
    /**
     * Set/Get minimum number of features per node.
     */
    inline void setMinFeaturesPerNode(int n) { mMinFeaturePerNode = n; }
    inline int minFeaturesPerNode() const { return mMinFeaturePerNode; }
    
private:
    
    // Random number seed
    int mRandSeed;
    
    // Counter for node id's
    int mNextNodeId;
    
    // Root node
    node_ptr_t mRoot;
    
    // Clustering algorithm
    kmedoids_t mBinarykMedoids;
    
    // Reverse index for query
    mutable std::vector<int> mQueryReverseIndex;
    
    // Node queue
    mutable queue_t mQueue;
    
    // Number of nodes popped off the priority queue
    mutable int mNumNodesPopped;
    
    // Maximum nodes to pop off the priority queue
    int mMaxNodesToPop;
    
    // Minimum number of feature at a node
    int mMinFeaturePerNode;
    
    /**
     * Get the next node id
     */
    inline int nextNodeId() {
        return mNextNodeId++;
    }
    
    /**
     * Private build function with a set of indices.
     */
    void build(const unsigned char* features, int num_features, const int* indices, int num_indices);
    
    /**
     * Recursive function to build the tree.
     */
    void build(node_t* node, const unsigned char* features, int num_features, const int* indices, int num_indices);
    
    /**
     * Recursive function query function.
     */
    void query(queue_t& queue, const node_t* node, const unsigned char* feature) const;
	
	
	
}
