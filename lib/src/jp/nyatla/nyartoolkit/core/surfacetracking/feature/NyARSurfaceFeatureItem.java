package jp.nyatla.nyartoolkit.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

public class NyARSurfaceFeatureItem extends NyARDoublePoint2d
{
    public NyARNftFsetFile.NyAR2FeatureCoord ref_feature;
	public int scale;
};