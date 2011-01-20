package jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;

/**
 * このインタフェイスは、グレースケール画像から２値画像を生成する関数を定義します。
 */
public interface INyARRasterFilter_Gs2Bin
{
	/**
	 * この関数は、入力画像を２値化して、出力画像へ書込みます。
	 * 実装クラスでは、i_inputのラスタを２値化して、i_outputへ値を出力してください。
	 * @param i_input
	 * 入力画像。
	 * @param i_output
	 * 出力画像
	 * @throws NyARException
	 */	
	public void doFilter(NyARGrayscaleRaster i_input, NyARBinRaster i_output) throws NyARException;
}
