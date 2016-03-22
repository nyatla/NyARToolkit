package jp.nyatla.nyartoolkit.core.kpm.keyframe;

import jp.nyatla.nyartoolkit.core.kpm.LongDescripter768;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;


public class BinaryHierarchicalNode
{

	// ID of the node
	final public int id;

	// Feature center
	final public LongDescripter768 center;

	// True if a leaf node
	final public boolean is_leaf;

	// Child nodes
	final public BinaryHierarchicalNode[] children;

	// Index of the features at this node
	final public int[] reserv_index;
	
	
	public BinaryHierarchicalNode(int id,FreakFeaturePoint i_feature,boolean i_is_leaf,int[] i_reserv_index,BinaryHierarchicalNode[] i_children)
	{
		this.is_leaf=i_is_leaf;
		this.id=id;
    	this.reserv_index=i_reserv_index;
		if(i_feature!=null){
			this.center=new LongDescripter768();
			this.center.setValue(i_feature.descripter);
		}else{
			this.center=null;
		}
    	this.children=i_children;
	}



  




	
}
