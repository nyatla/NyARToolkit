package jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers;

import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

public class NodePtrStack extends NyARPointerStack<Node>{
	public NodePtrStack(int i_length) {
		super(i_length,Node.class);
	}
}