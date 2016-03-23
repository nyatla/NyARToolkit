package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;


import java.util.PriorityQueue;

import com.sun.corba.se.impl.orbutil.graph.Node;

import jp.nyatla.nyartoolkit.core.kpm.utils.LongDescripter768;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;


public class BinaryHierarchicalSelector
{
	public class Queue extends PriorityQueue<PriorityQueueItem> {
		private static final long serialVersionUID = 6120329703806461621L;

	}
	final public int[] _result;
	private int _num_of_result;
	public BinaryHierarchicalSelector(int i_MaxNodesToPop,int i_max_result)
	{
		this.mMaxNodesToPop = i_MaxNodesToPop;
		this._result=new int[i_max_result];
		return;
	}


//	/**
//	 * @return Reverse index after a QUERY.
//	 */
//	public int[] reverseIndex() {
//		return this.mQueryReverseIndex;
//	}


	//
	// // Clustering algorithm
	// kmedoids_t mBinarykMedoids;
	//
	// Reverse index for query
	//	private int[] mQueryReverseIndex;
	//
	// Node queue
	private Queue mQueue = new Queue();
	//
	// Number of nodes popped off the priority queue
	private int mNumNodesPopped;

	// Maximum nodes to pop off the priority queue
	final private int mMaxNodesToPop;


	public class NodeList extends NyARObjectStack<PriorityQueueItem>{
		public NodeList(int i_length) {
			super(i_length,PriorityQueueItem.class);
		}
	}
	private void append(BinaryHierarchicalNode i_node)
	{
		assert i_node.is_leaf==true;
		//末端なら結果配列へ値を追加
		int p=this._num_of_result;
		int l=i_node.reserv_index.length;
		//
		if(l+p>this._result.length){
			l=this._result.length-this._num_of_result;
		}
		for(int i=0;i<l;i++){
			this._result[p+i]=i_node.reserv_index[i];
		}
		this._num_of_result+=l;
		return;
	}
	/**
	 * Recursive function query function.
	 */
	private void query(Queue queue,NodePtrStack i_nodes,BinaryHierarchicalNode node, LongDescripter768 feature)
	{
		if (node.is_leaf) {
			return;
		}
		
		int sp=i_nodes.getLength();
		nearest(node,i_nodes, queue, feature);
		int ep=i_nodes.getLength();
		for (int i = sp; i < ep; i++) {
			BinaryHierarchicalNode n=i_nodes.getItem(i);
			if(n.is_leaf){
				this.append(n);
			}else{
				this.query(queue,i_nodes,n,feature);
			}
		}
		i_nodes.pops(ep-sp);

		// Pop a node from the queue
		if (this.mNumNodesPopped < this.mMaxNodesToPop && !queue.isEmpty()) {
			BinaryHierarchicalNode n = queue.poll().node();// pop();
			this.mNumNodesPopped++;
			if(n.is_leaf){
				this.append(n);
			}else{
				this.query(queue,i_nodes,n,feature);
			}
		}
	}
	/**
	 * Query the tree for a reverse index.
	 */
	public int query(BinaryHierarchicalNode i_node,LongDescripter768 feature)
	{
		this._num_of_result=0;
		this.mNumNodesPopped = 0;
//		this.mQueryReverseIndex = new int[0];

		// while(!mQueue.empty()) {
		// mQueue.pop();
		// }
		this.mQueue.clear();
		NodePtrStack nodes = new NodePtrStack(1000);
		this.query(mQueue,nodes, i_node, feature);

		return (int) this._num_of_result;
	}
    /**
     * Get a queue of all the children nodes sorted by distance from node center.
     */
    /**
     * Get a queue of all the children nodes sorted by distance from node center.
     */
    private static void nearest(BinaryHierarchicalNode i_node,NodePtrStack nodes,BinaryHierarchicalSelector.Queue queue,LongDescripter768 feature)
    {
        int mind = Integer.MAX_VALUE;
        int mini = -1;
        
        // Compute the distance to each cluster center
        PriorityQueueItem[] v =new PriorityQueueItem[i_node.children.length];
        for(int i = 0; i < v.length; i++) {
            int d = i_node.children[i].center.hammingDistance(feature);
            v[i] = new PriorityQueueItem(i_node.children[i], d);
            if(d < mind) {
                mind = d;
                mini = (int)i;
            }
        }
        // Store the closest child
        nodes.push(i_node.children[mini]);
        
        // Any nodes that are the SAME distance as the minimum node are added
        // to the output vector, otherwise it's pushed onto the queue.
        for(int i = 0; i < v.length; i++) {
            if(i == mini) {
                continue;
            } else if(v[i].dist() == v[mini].dist()) {
                nodes.push(i_node.children[i]);
            } else {
                queue.add(v[i]);
            }
        }
        return;
    }
	


}
