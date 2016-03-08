package jp.nyatla.nyartoolkit.core.kpm.freak;


import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class FreakFeaturePointStack extends NyARObjectStack<FreakFeaturePoint>
{
	public FreakFeaturePointStack(int i_length)
	{
		super(i_length,FreakFeaturePoint.class);
	}
	public FreakFeaturePointStack()
	{
		super(9999,FreakFeaturePoint.class);
		System.out.println("force set BinaryFeatureStore size to 9999");
	}
	
	@Override
	final protected FreakFeaturePoint createElement()
	{
		return new FreakFeaturePoint();
	}

}