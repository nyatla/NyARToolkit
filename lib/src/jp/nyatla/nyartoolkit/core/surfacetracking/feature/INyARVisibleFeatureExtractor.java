package jp.nyatla.nyartoolkit.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARSurfaceFeatures;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceTransMatrixSet;


public interface INyARVisibleFeatureExtractor
{
	/**
	 * 有効なテンプレートデータを抽出し、展開して返します。
	 * @param i_fset
	 * 展開元のテンプレート
	 * @param i_trans
	 * 座標変換行列のセット
	 * @param i_trans
	 * テンプレートの抽出条件-回転行列
	 * @param candidate
	 * 高精度なテンプレート候補
	 * @param candidate2
	 * 低精度なテンプレート候補
	 * @return
	 */
	public void extractVisibleFeatures(NyARNftFsetFile i_fset,
		NyARSurfaceTransMatrixSet i_trans, 
		NyARSurfaceFeatures candidate,
		NyARSurfaceFeatures candidate2);

}
