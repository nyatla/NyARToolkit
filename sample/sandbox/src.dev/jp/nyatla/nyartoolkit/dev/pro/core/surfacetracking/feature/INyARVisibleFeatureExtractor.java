package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARSurfaceTransMatrixSet;

public interface INyARVisibleFeatureExtractor
{
	/**
	 * 有効な�?ンプレートデータを抽出し�?�展開して返します�??
	 * @param i_fset
	 * 展開�?の�?ンプレー�?
	 * @param i_trans
	 * 座標変換行�?��?�セ�?�?
	 * @param i_trans
	 * �?ンプレート�?�抽出条件-回転行�??
	 * @param candidate
	 * 高精度な�?ンプレート�?��?
	 * @param candidate2
	 * 低精度な�?ンプレート�?��?
	 * @return
	 */
	public void extractVisibleFeatures(NyARSurfaceFeatureSet i_fset,
		NyARSurfaceTransMatrixSet i_trans, 
		NyARSurfaceFeatures candidate,
		NyARSurfaceFeatures candidate2);

}
