package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARFeatureCoordPtrList;

/**
 * 特徴候補から�?�現在選択済みの候補o_posを参照して�?適な候補を返します�??
 * @author nyatla
 */
public interface INyARSurfaceFeatureIndex
{
	public int ar2SelectTemplate(NyARSurfaceFeatures candidate, NyARFeatureCoordPtrList prelog,NyARSurfaceFeaturesPtr o_pos,NyARIntSize i_screen_size);
}
