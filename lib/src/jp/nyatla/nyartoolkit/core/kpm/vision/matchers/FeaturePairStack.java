package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class FeaturePairStack extends NyARObjectStack<FeaturePairStack.Item>
{
	protected FeaturePairStack(int i_length)
	{
		super(i_length, FeaturePairStack.Item.class);
	}
	public class Item {
		public FreakFeaturePoint query;
		public FreakFeaturePoint ref;
	}
	protected FeaturePairStack.Item createElement()
	{
		return new Item();
	}	
}
