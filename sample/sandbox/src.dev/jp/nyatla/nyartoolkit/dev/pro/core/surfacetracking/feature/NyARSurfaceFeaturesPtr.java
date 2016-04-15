package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

public class NyARSurfaceFeaturesPtr extends NyARPointerStack<NyARSurfaceFeatureItem>
{
	public NyARSurfaceFeaturesPtr(int i_length) throws NyARRuntimeException
	{
		super.initInstance(i_length, NyARSurfaceFeatureItem.class);
	}
}