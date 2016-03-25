package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;



public class BinaryHierarchicalNode
{

	// ID of the node
	final public int id;

	// Feature center
	final public FreakFeaturePoint center;

	// True if a leaf node
	final public boolean is_leaf;

	// Child nodes
	final public BinaryHierarchicalNode[] children;

	// Index of the features at this node
	final public FreakMatchPointSetStack.Item[] reserv_index;
	
	
	public BinaryHierarchicalNode(int id,FreakFeaturePoint i_feature,boolean i_is_leaf,FreakMatchPointSetStack.Item[] i_reserv_index,BinaryHierarchicalNode[] i_children)
	{
		this.is_leaf=i_is_leaf;
		this.id=id;
    	this.reserv_index=i_reserv_index;
		if(i_feature!=null){
			this.center=i_feature;
		}else{
			this.center=null;
		}
    	this.children=i_children;
	}
	
}
