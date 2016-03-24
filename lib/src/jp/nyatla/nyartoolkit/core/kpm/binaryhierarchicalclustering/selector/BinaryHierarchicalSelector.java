package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.selector;


import java.util.PriorityQueue;



import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.BinaryHierarchicalNode;
import jp.nyatla.nyartoolkit.core.kpm.utils.LongDescripter768;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;


public class BinaryHierarchicalSelector
{
	/**
	 * The nodes in the tree are sorted as they are visited when a QUERY is done.
	 * This class represents an entry in a priority queue to revisit certains nodes
	 * in a back-trace.
	 */
	private static class PriorityQueueItem implements Comparable
	{
		// Pointer to the node
		public BinaryHierarchicalNode node;
		// Distance from cluster center
		public int distance;
		public PriorityQueueItem(BinaryHierarchicalNode i_node, int i_dist) {
			this.node = i_node;
			this.distance = i_dist;
		}
		@Override
		public int compareTo(Object o) {
			PriorityQueueItem p=(PriorityQueueItem)o;
			if(this.distance>p.distance){
				return 1;
			}else if(this.distance<p.distance){
				return -1;
			}
			return 0;
		}

	}	
	private class Queue extends PriorityQueue<PriorityQueueItem> {
		private static final long serialVersionUID = 6120329703806461621L;
	}
	private class NodePtrStack extends NyARPointerStack<BinaryHierarchicalNode>
	{
		public NodePtrStack(int i_length) {
			super(i_length,BinaryHierarchicalNode.class);
		}
	}	
		
	final public int[] _result;
	private int _num_of_result;
	
	public BinaryHierarchicalSelector(int i_MaxNodesToPop,int i_max_result)
	{
		this.mMaxNodesToPop = i_MaxNodesToPop;
		this._result=new int[i_max_result];
		return;
	}

	// Node queue
	final private Queue mQueue = new Queue();
	//
	// Number of nodes popped off the priority queue
	private int mNumNodesPopped;

	// Maximum nodes to pop off the priority queue
	final private int mMaxNodesToPop;

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
			BinaryHierarchicalNode n = queue.poll().node;// pop();
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

		this.mQueue.clear();
		NodePtrStack nodes = new NodePtrStack(1000);
		this.query(mQueue,nodes, i_node, feature);
		return (int) this._num_of_result;
	}


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
            } else if(v[i].distance == v[mini].distance) {
                nodes.push(i_node.children[i]);
            } else {
                queue.add(v[i]);
            }
        }
        return;
    }
	


}
