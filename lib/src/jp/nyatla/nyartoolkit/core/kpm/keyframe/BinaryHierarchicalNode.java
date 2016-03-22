package jp.nyatla.nyartoolkit.core.kpm.keyframe;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter768;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;


public class BinaryHierarchicalNode
{

	// ID of the node
	final private int mId;

	// Feature center
	final private LongDescripter768 mCenter;

	// True if a leaf node
	final private boolean mLeaf;

	// Child nodes
	final private BinaryHierarchicalNode[] mChildren;
    private int _num_of_children;

	// Index of the features at this node
	final private int[] mReverseIndex;
	
	
	public BinaryHierarchicalNode(int id,FreakFeaturePoint i_feature,boolean i_is_leaf,int i_index_size,int i_num_of_children)
	{
		this.mLeaf=i_is_leaf;
		this.mId=id;
    	this.mReverseIndex=new int[i_index_size];
		if(i_feature!=null){
			this.mCenter=new LongDescripter768();
			this.mCenter.setValue(i_feature.descripter);
		}else{
			this.mCenter=null;
		}
    	if(i_num_of_children>0){
    		this.mChildren=new BinaryHierarchicalNode[i_num_of_children];
    		this._num_of_children=0;
    	}else{
    		this.mChildren=null;
    	}
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

	public void children_push_back(BinaryHierarchicalNode new_node)
	{
		this.mChildren[_num_of_children]=new_node;
		_num_of_children++;
	}	
    /**
     * Get a queue of all the children nodes sorted by distance from node center.
     */
    public void nearest(NodePtrStack nodes,BinaryHierarchicalSelector.Queue queue,LongDescripter768 feature)
    {
        int mind = Integer.MAX_VALUE;
        int mini = -1;
        
        // Compute the distance to each cluster center
        PriorityQueueItem[] v =new PriorityQueueItem[this.mChildren.length];
        for(int i = 0; i < v.length; i++) {
            int d = this.mChildren[i].mCenter.hammingDistance(feature);
            v[i] = new PriorityQueueItem(this.mChildren[i], d);
            if(d < mind) {
                mind = d;
                mini = (int)i;
            }
        }
        
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
                queue.add(v[i]);
            }
        }
        return;
    }

	
}
