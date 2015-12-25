package jp.nyatla.nyartoolkit.core.surfacetracking;


import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

public class NyARFeatureCoordPtrList extends NyARPointerStack<NyARNftFsetFile.NyAR2FeatureCoord>
{
	public NyARFeatureCoordPtrList(int i_max_num)
	{
		super(i_max_num,NyARNftFsetFile.NyAR2FeatureCoord.class);
	}
}