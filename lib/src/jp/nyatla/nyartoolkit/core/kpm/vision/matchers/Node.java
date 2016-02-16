package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.vision.BinaryHierarchicalClustering;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.Hamming;


public class Node {
	public Node(int i_NUM_BYTES_PER_FEATURE,int id)
	{
		this.mId=id;
		this.mLeaf=true;
		this.mCenter=new byte[i_NUM_BYTES_PER_FEATURE];
	}
	public Node(int i_NUM_BYTES_PER_FEATURE,int id,byte[] i_feature,int i_st)
	{
		this.mId=id;
		this.mLeaf=true;
		this.mCenter=new byte[i_NUM_BYTES_PER_FEATURE];
		for(int i=0;i<i_NUM_BYTES_PER_FEATURE;i++){
			this.mCenter[i]=i_feature[i+i_st];
		}
	}
    /**
     * Set/Get leaf flag
     */
	public void leaf(boolean b){
		this.mLeaf=b;
	}
	public boolean leaf(){
		return this.mLeaf;
	}
    /**
     * @return Node id
     */
	public int id(){
		return this.mId;
	}
	
//    typedef int node_id_t;
//    typedef Node<NUM_BYTES_PER_FEATURE> node_t;
//    typedef PriorityQueueItem<NUM_BYTES_PER_FEATURE> queue_item_t;
//    typedef std::priority_queue<queue_item_t> queue_t;
//    

//    


//    /**
//     * @return Get children
//     */
//    inline std::vector<node_t*>& children() { return mChildren; }
//    inline const std::vector<node_t*>& children() const { return mChildren; }
//    
    /**
     * @return Get the reverse index
     */
	public int[] reverseIndex() { return mReverseIndex; }
//    inline const std::vector<int>& reverseIndex() const { return mReverseIndex; }
//    
	public void reserveChildren(int i_size)
	{
		this.mChildren=new Node[i_size];
		_num_of_children=0;
	}
    private int _num_of_children;
	public void children_push_back(Node new_node)
	{
		this.mChildren[_num_of_children]=new_node;
		_num_of_children++;
		// TODO Auto-generated method stub
		
	}	
    /**
     * Get a queue of all the children nodes sorted by distance from node center.
     */
    public void nearest(NodePtrStack nodes,
    		BinaryHierarchicalClustering.Queue queue,
                        byte[] feature,int i_ptr)
    {
        int mind = Integer.MAX_VALUE;
        int mini = -1;
        
        // Compute the distance to each cluster center
//        std::vector<queue_item_t> v(mChildren.size());
        PriorityQueueItem[] v =new PriorityQueueItem[this.mChildren.length];
        for(int i = 0; i < v.length; i++) {
            int d = Hamming.HammingDistance(this.mCenter.length,this.mChildren[i].mCenter,0, feature,i_ptr);
            v[i] = new PriorityQueueItem(this.mChildren[i], d);
            if(d < mind) {
                mind = d;
                mini = (int)i;
            }
        }
//        ASSERT(mini != -1, "Minimum index not set");
        
        // Store the closest child
        nodes.push(this.mChildren[mini]);
        
        // Any nodes that are the SAME distance as the minimum node are added
        // to the output vector, otherwise it's pushed onto the queue.
        for(int i = 0; i < v.length; i++) {
            if(i == mini) {
                continue;
            } else if(v[i].dist() == v[mini].dist()) {
                nodes.push(this.mChildren[i]);
            } else {
//                queue.push(v[i]);
                queue.add(v[i]);
            }
        }
    }

//    
//private:
//    
//    // ID of the node
    private int mId;
//    
//    // Feature center
    private byte mCenter[];
//    
//    // True if a leaf node
    private boolean mLeaf;
    
    // Child nodes
    private Node[] mChildren;
    
    // Index of the features at this node
    private int[] mReverseIndex;
    public void resizeReverseIndex(int i_size){
    	this.mReverseIndex=new int[i_size];
    }

	
	
}
