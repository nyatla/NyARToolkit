package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.selector.utils;

import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.BinaryHierarchicalNode;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class NodeStack extends NyARObjectStack<NodeStack.Item>
{
	public class Item{
		public int distance;
		public BinaryHierarchicalNode node;			
	}
	public NodeStack(int i_length) {
		super(i_length,Item.class);
	}
	protected Item createElement()
	{
		return new Item();
	}
}