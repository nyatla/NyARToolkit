package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter768;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.BinaryHierarchicalClustering;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.Hamming;


public class Node {
	public Node(int id)
	{
		this.mId=id;
		this.mLeaf=true;
		this.mCenter=new LongDescripter768();
	}
	public Node(int id,FreakFeaturePoint i_feature)
	{
		this.mId=id;
		this.mLeaf=true;
		this.mCenter=new LongDescripter768();
		this.mCenter.setValue(i_feature.descripter);
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
                        LongDescripter768 feature)
    {
        int mind = Integer.MAX_VALUE;
        int mini = -1;
        
        // Compute the distance to each cluster center
//        std::vector<queue_item_t> v(mChildren.size());
        PriorityQueueItem[] v =new PriorityQueueItem[this.mChildren.length];
        for(int i = 0; i < v.length; i++) {
            int d = this.mChildren[i].mCenter.hammingDistance(feature);
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
    final private int mId;
//    
//    // Feature center
    final private LongDescripter768 mCenter;
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
