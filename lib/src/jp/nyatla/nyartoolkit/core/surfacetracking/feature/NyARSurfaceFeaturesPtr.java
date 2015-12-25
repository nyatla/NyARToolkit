package jp.nyatla.nyartoolkit.core.surfacetracking.feature;


import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

public class NyARSurfaceFeaturesPtr extends NyARPointerStack<NyARSurfaceFeatureItem>
{
	public NyARSurfaceFeaturesPtr(int i_length)
	{
		super(i_length, NyARSurfaceFeatureItem.class);
	}
}