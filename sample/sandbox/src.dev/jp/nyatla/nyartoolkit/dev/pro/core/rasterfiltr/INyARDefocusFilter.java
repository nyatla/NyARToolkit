package jp.nyatla.nyartoolkit.dev.pro.core.rasterfiltr;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

public interface INyARDefocusFilter
{
	/**
	 * i_outputへフィルタを適応したラスタを�?�力します�??
	 * @param i_output
	 * 参�?�して�?るラスタと同じも�?�は�?定できません�?
	 * @throws NyARRuntimeException
	 */
	public void doFilter(INyARGrayscaleRaster i_output) throws NyARRuntimeException;
	/**
	 * i_outputへ、i_loop回フィルタを適応したラスタを�?�力します�??
	 * @param i_output
	 * 参�?�して�?るラスタと同じも�?�は�?定できません�?
	 * @param i_loop
	 * 繰り返し回数
	 * @throws NyARRuntimeException
	 */
	public void doFilter(INyARGrayscaleRaster i_output,int i_loop) throws NyARRuntimeException;
}






