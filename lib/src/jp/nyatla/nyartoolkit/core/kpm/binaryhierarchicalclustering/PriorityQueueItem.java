package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;



/**
 * The nodes in the tree are sorted as they are visited when a QUERY is done.
 * This class represents an entry in a priority queue to revisit certains nodes
 * in a back-trace.
 */
public class PriorityQueueItem implements Comparable
{
	// Pointer to the node
	public BinaryHierarchicalNode mNode;
	// Distance from cluster center
	public int mDistance;
	
	public PriorityQueueItem() {
		this.mNode = null;
		this.mDistance = 0;
	}

	public PriorityQueueItem(BinaryHierarchicalNode i_node, int i_dist) {
		this.mNode = i_node;
		this.mDistance = i_dist;
	}

	/**
	 * Get pointer to node.
	 */
	public BinaryHierarchicalNode node() {
		return this.mNode;
	}

	/**
	 * Distance to cluster center.
	 */
	public int dist() {
		return this.mDistance;
	}




	@Override
	public int compareTo(Object o) {
		PriorityQueueItem p=(PriorityQueueItem)o;
		if(this.mDistance>p.mDistance){
			return 1;
		}else if(this.mDistance<p.mDistance){
			return -1;
		}
		return 0;
	}

}
