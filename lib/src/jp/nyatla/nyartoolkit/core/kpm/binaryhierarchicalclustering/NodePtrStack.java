package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;

import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

public class NodePtrStack extends NyARPointerStack<BinaryHierarchicalNode>{
	public NodePtrStack(int i_length) {
		super(i_length,BinaryHierarchicalNode.class);
	}
}