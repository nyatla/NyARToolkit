package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureSet;

public class NyARFeatureCoordPtrList extends NyARPointerStack<NyARSurfaceFeatureSet.NyAR2FeatureCoord>
{
	public NyARFeatureCoordPtrList(int i_max_num) throws NyARException
	{
		super.initInstance(i_max_num,jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature.NyAR2FeatureCoord.class);
	}
}