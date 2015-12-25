package jp.nyatla.nyartoolkit.core.surfacetracking.rasterdriver;

import jp.nyatla.nyartoolkit.core.surfacetracking.NyARTemplatePatchImage;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;


/**
 * INyARGrayscaleRasterのテンプレート検索ドライバ
 * @author nyatla
 *
 */
public interface INyARTemplateMatchingDriver
{
	/**
	 * 検索ウインドウの範囲を指定する。
	 * 検索ウインドウは(i_px*2+1)*(i_py*2+1)サイズの矩形。
	 * @param i_px
	 * @param i_py
	 */
	public void setSearchArea(int i_x,int i_y);

	/**
	 * N個の基準点から、最もテンプレートに一致した座標を返却する。
	 * 検索範囲は、{@link #setSearchArea}で与えたpx,pyを元に定義した矩形。
	 * i_pointsそれぞれについて検索する。
	 * @param i_template
	 * 検索するテンプレート
	 * @param ry
	 * @param i_points
	 * 検索する座標セット。(近い場所の場合に、同一条件の探索をキャンセルできる？)
	 * @param o_obs_point
	 * 観察座標系での一致点。returnが0の場合は無効。
	 * @return
	 * 一致率(値範囲調査中)
	 * 0の場合は一致せず。
	 * @throws NyARException
	 */
	public double ar2GetBestMatching(NyARTemplatePatchImage i_template, NyARIntPoint2d[] i_points,int i_number_of_point,
			NyARDoublePoint2d o_obs_point);
}