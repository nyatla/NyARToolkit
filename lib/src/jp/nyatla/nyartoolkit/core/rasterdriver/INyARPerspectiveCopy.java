package jp.nyatla.nyartoolkit.core.rasterdriver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
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
	public boolean copyPatt(NyARDoublePoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException;
	public boolean copyPatt(double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster i_out) throws NyARException;
}
