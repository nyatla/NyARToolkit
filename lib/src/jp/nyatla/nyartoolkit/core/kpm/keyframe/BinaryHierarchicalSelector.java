package jp.nyatla.nyartoolkit.core.kpm.keyframe;


import java.util.PriorityQueue;
import jp.nyatla.nyartoolkit.core.kpm.LongDescripter768;


public class BinaryHierarchicalSelector
{
	public class Queue extends PriorityQueue<PriorityQueueItem> {
		private static final long serialVersionUID = 6120329703806461621L;

	}
	public BinaryHierarchicalSelector(int i_MaxNodesToPop) {
		this.mMaxNodesToPop = i_MaxNodesToPop;
	}


	/**
	 * @return Reverse index after a QUERY.
	 */
	public int[] reverseIndex() {
		return this.mQueryReverseIndex;
	}


	//
	// // Clustering algorithm
	// kmedoids_t mBinarykMedoids;
	//
	// Reverse index for query
	private int[] mQueryReverseIndex;
	//
	// Node queue
	private Queue mQueue = new Queue();
	//
	// Number of nodes popped off the priority queue
	private int mNumNodesPopped;

	// Maximum nodes to pop off the priority queue
	final private int mMaxNodesToPop;



	private static int[] appendArray(int[] f, int[] s) {
		int[] n = new int[f.length + s.length];
		System.arraycopy(f, 0, n, 0, f.length);
		System.arraycopy(s, 0, n, f.length, s.length);
		return n;
	}

	/**
	 * Recursive function query function.
	 */
	private void query(Queue queue, BinaryHierarchicalNode node, LongDescripter768 feature) {

		if (node.is_leaf) {
			// Insert all the leaf indices into the query index
			// mQueryReverseIndex.insert(mQueryReverseIndex.end(),node.reverseIndex().begin(),node.reverseIndex().end());
			// 追記
			this.mQueryReverseIndex = appendArray(this.mQueryReverseIndex, node.reserv_index);
			return;
		} else {
			NodePtrStack nodes = new NodePtrStack(1000);
			nearest(node,nodes, queue, feature);
			for (int i = 0; i < nodes.getLength(); i++) {
				this.query(queue, nodes.getItem(i), feature);
			}

			// Pop a node from the queue
			if (this.mNumNodesPopped < this.mMaxNodesToPop && !queue.isEmpty()) {
				// Node q = queue.top().node();
				BinaryHierarchicalNode q = queue.poll().node();// pop();
				// queue.pop();
				this.mNumNodesPopped++;
				this.query(queue, q, feature);
			}
		}
	}
	/**
	 * Query the tree for a reverse index.
	 */
	public int query(BinaryHierarchicalNode i_node,LongDescripter768 feature) {
		this.mNumNodesPopped = 0;
		this.mQueryReverseIndex = new int[0];

		// while(!mQueue.empty()) {
		// mQueue.pop();
		// }
		this.mQueue.clear();

		this.query(mQueue, i_node, feature);

		return (int) mQueryReverseIndex.length;
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
            } else if(v[i].dist() == v[mini].dist()) {
                nodes.push(i_node.children[i]);
            } else {
                queue.add(v[i]);
            }
        }
        return;
    }

}
