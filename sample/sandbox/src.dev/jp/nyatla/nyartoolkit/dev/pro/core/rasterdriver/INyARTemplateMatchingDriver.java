package jp.nyatla.nyartoolkit.dev.pro.core.rasterdriver;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARTemplatePatchImage;

/**
 * INyARGrayscaleRasterの�?ンプレート検索ドライ�?
 * @author nyatla
 *
 */
public interface INyARTemplateMatchingDriver
{
	/**
	 * 検索ウインドウの�?囲を指定する�??
	 * 検索ウインドウは(i_px*2+1)*(i_py*2+1)サイズの矩形�?
	 * @param i_px
	 * @param i_py
	 */
	public void setSearchArea(int i_x,int i_y);

	/**
	 * N個�?�基準点から、最もテンプレートに�?致した座標を返却する�?
	 * 検索�?囲は、{@link #setSearchArea}で与えたpx,pyを�??に定義した矩形�?
	 * i_pointsそれぞれにつ�?て検索する�?
	 * @param i_template
	 * 検索する�?ンプレー�?
	 * @param ry
	 * @param i_points
	 * 検索する座標セ�?ト�??(近い場�?の場合に、同�?条件の探索をキャンセルできる?�?)
	 * @param o_obs_point
	 * 観察座標系での�?致点。return�?0の場合�?�無効�?
	 * @return
	 * �?致�?(値�?囲調査中)
	 * 0の場合�?��?致せず�?
	 * @throws NyARRuntimeException
	 */
	public double ar2GetBestMatching(NyARTemplatePatchImage i_template, NyARIntPoint2d[] i_points,int i_number_of_point,
			NyARDoublePoint2d o_obs_point) throws NyARRuntimeException;
}