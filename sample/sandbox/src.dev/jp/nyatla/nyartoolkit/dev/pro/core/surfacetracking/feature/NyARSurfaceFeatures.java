package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class NyARSurfaceFeatures extends NyARObjectStack<NyARSurfaceFeatureItem>
{
	public final static int AR2_TRACKING_CANDIDATE_MAX=200;

	public NyARSurfaceFeatures(int i_length) throws NyARException
	{
		super.initInstance(i_length,NyARSurfaceFeatureItem.class);
	}
	/**
	 * ã“ã?®é–¢æ•°ã¯ã€?é…å?—è¦ç´?ã‚’ä½œæ?ã—ã¾ã™ã??
	 */	
	protected NyARSurfaceFeatureItem createElement()
	{
		return new NyARSurfaceFeatureItem();
	}
}