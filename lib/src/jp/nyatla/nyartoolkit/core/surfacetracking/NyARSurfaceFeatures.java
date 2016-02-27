package jp.nyatla.nyartoolkit.core.surfacetracking;


import jp.nyatla.nyartoolkit.core.surfacetracking.feature.NyARSurfaceFeatureItem;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class NyARSurfaceFeatures extends NyARObjectStack<NyARSurfaceFeatureItem>
{
	public final static int AR2_TRACKING_CANDIDATE_MAX=200;

	public NyARSurfaceFeatures(int i_length)
	{
		super(i_length,NyARSurfaceFeatureItem.class);
	}
	/**
	 * この関数は、配列要素を作成します。
	 */	
	protected NyARSurfaceFeatureItem createElement()
	{
		return new NyARSurfaceFeatureItem();
	}
}