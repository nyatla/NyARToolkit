package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureSet;

public class NyARFeatureCoordPtrList extends NyARPointerStack<NyARNftFsetFile.NyAR2FeatureCoord>
{
	public NyARFeatureCoordPtrList(int i_max_num) throws NyARRuntimeException
	{
		super.initInstance(i_max_num,jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature.NyAR2FeatureCoord.class);
	}
}