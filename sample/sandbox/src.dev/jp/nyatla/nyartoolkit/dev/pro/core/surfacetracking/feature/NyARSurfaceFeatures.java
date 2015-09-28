package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class NyARSurfaceFeatures extends NyARObjectStack<NyARSurfaceFeatureItem>
{
	public final static int AR2_TRACKING_CANDIDATE_MAX=200;

	public NyARSurfaceFeatures(int i_length) throws NyARRuntimeException
	{
		super.initInstance(i_length,NyARSurfaceFeatureItem.class);
	}
	/**
	 * こ�?�関数は�?配�?�要�?を作�?�します�??
	 */	
	protected NyARSurfaceFeatureItem createElement()
	{
		return new NyARSurfaceFeatureItem();
	}
}