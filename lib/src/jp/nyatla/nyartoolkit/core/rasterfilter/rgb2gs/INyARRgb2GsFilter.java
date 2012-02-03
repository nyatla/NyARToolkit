package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;

public interface INyARRgb2GsFilter
{
	/**
	 * 元画像の指定範囲の矩形から、グレイスケール画像を生成して、i_rasterへコピーします。
	 * 範囲が元画像の一部の場合、その部分だけをコピーします。
	 * @param l
	 * @param t
	 * @param w
	 * @param h
	 * @param i_raster
	 * @throws NyARException
	 */
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster i_raster) throws NyARException;
	/**
	 * 同一サイズの画像にグレースケール画像を生成します。
	 * @param i_raster
	 * @throws NyARException
	 */
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException;
}