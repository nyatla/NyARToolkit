package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.selector;

import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.BinaryHierarchicalNode;
import jp.nyatla.nyartoolkit.core.kpm.utils.LongDescripter768;

public interface IBinaryHierarchicalSelector {
	public int query(BinaryHierarchicalNode i_node,LongDescripter768 feature);
	public int[] getResult();
}
