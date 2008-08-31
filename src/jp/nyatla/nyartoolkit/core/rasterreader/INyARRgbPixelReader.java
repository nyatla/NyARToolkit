package jp.nyatla.nyartoolkit.core.rasterreader;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * R8G8B8でピクセルを読み出すインタフェイス
 * 
 */
public interface INyARRgbPixelReader
{
	/**
	 * 1ピクセルをint配列にして返します。
	 * 
	 * @param i_x
	 * @param i_y
	 * @param i_rgb
	 */
	public void getPixel(int i_x, int i_y, int[] i_rgb) throws NyARException;

	/**
	 * 複数のピクセル値をi_rgbへ返します。
	 * 
	 * @param i_x
	 * xのインデックス配列
	 * @param i_y
	 * yのインデックス配列
	 * @param i_num
	 * 返すピクセル値の数
	 * @param i_rgb
	 * ピクセル値を返すバッファ
	 */
	public void getPixelSet(int[] i_x, int i_y[], int i_num, int[] i_rgb) throws NyARException;
}
