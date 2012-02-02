package jp.nyatla.nyartoolkit.core.rasterdriver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

/**
 * このインタフェイスは、ラスタから射影変換画像を取得するインタフェイスを提供します。
 *
 */
public interface INyARPerspectiveCopy
{
	/**
	 * この関数は、i_outへパターンを出力します。
	 * @param i_vertex
	 * @param i_edge_x
	 * @param i_edge_y
	 * @param i_resolution
	 * @param i_out
	 * @return
	 * @throws NyARException
	 */
	public boolean copyPatt(NyARIntPoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException;
}
