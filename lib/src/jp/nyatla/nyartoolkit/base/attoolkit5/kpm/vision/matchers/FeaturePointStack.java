package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class FeaturePointStack extends NyARObjectStack<FeaturePoint> implements Cloneable
{
	public FeaturePointStack(int i_length)
	{
		super(i_length,FeaturePoint.class);
	}
	@Override
	final protected FeaturePoint createElement()
	{
		return new FeaturePoint();
	}

}