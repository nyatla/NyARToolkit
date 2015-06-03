package jp.nyatla.nyartoolkit.dev.pro.core.rasterfiltr;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;

public interface INyARDefocusFilter
{
	/**
	 * i_outputへフィルタを適応したラスタを�?�力します�??
	 * @param i_output
	 * 参�?�して�?るラスタと同じも�?�は�?定できません�?
	 * @throws NyARException
	 */
	public void doFilter(INyARGrayscaleRaster i_output) throws NyARException;
	/**
	 * i_outputへ、i_loop回フィルタを適応したラスタを�?�力します�??
	 * @param i_output
	 * 参�?�して�?るラスタと同じも�?�は�?定できません�?
	 * @param i_loop
	 * 繰り返し回数
	 * @throws NyARException
	 */
	public void doFilter(INyARGrayscaleRaster i_output,int i_loop) throws NyARException;
}






