package jp.nyatla.nyartoolkit.core.surfacetracking.rasterfilter;

import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;



public interface INyARDefocusFilter
{
	/**
	 * i_outputへフィルタを適応したラスタを出力します。
	 * @param i_output
	 * 参照しているラスタと同じものは指定できません。
	 * @throws NyARException
	 */
	public void doFilter(INyARGrayscaleRaster i_output);
	/**
	 * i_outputへ、i_loop回フィルタを適応したラスタを出力します。
	 * @param i_output
	 * 参照しているラスタと同じものは指定できません。
	 * @param i_loop
	 * 繰り返し回数
	 * @throws NyARException
	 */
	public void doFilter(INyARGrayscaleRaster i_output,int i_loop);
}






