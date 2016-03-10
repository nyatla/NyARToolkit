package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class FreakMatchPointSetStack extends NyARObjectStack<FreakMatchPointSetStack.Item>
{
	public class Item extends FreakFeaturePoint
	{
		final public NyARDoublePoint3d pos3d=new NyARDoublePoint3d();
	}	
	public FreakMatchPointSetStack(int i_length)
	{
		super(i_length,FreakMatchPointSetStack.Item.class);
	}
	@Override
	final protected Item createElement()
	{
		return new Item();
	}
}