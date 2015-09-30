package jp.nyatla.nyartoolkit.core.raster.rgb;

/**
 * {@link INyARRgbRaster}で継承します。
 * RGB画像のピクセルアクセサを定義します。
 */
public interface INyARRgbPixelInterface {
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 * 実装クラスでは、バッファから指定した座標のRGB値を取得する処理を実装してください。
	 * 
	 * @param i_x
	 *            取得するピクセルの座標。画像の範囲内である事。
	 * @param i_y
	 *            取得するピクセルの座標。画像の範囲内である事。
	 * @param i_rgb
	 *            ピクセル値を返却する配列を指定します。3要素以上の配列が必要です。 値は、[R][G][B]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public int[] getPixel(int i_x, int i_y, int[] i_rgb);

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 * 実装クラスでは、バッファから、座標軍のRGB値を取得する処理を実装してください。
	 * 
	 * @param i_x
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_y
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_num
	 *            取得するピクセルの数を指定します。
	 * @param i_intrgb
	 *            ピクセル値を返却する配列を指定します。3要素以上の配列が必要です。
	 *            値は、[R1][G1][B1][R2][G2][B2]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public int[] getPixelSet(int[] i_x, int[] i_y, int i_num, int[] i_intrgb);

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_y
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_r
	 *            R成分のピクセル値。
	 * @param i_g
	 *            G成分のピクセル値。
	 * @param i_b
	 *            B成分のピクセル値。
	 * @throws NyARRuntimeException
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b);
	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_y
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_rgb
	 *            設定するピクセル値。3要素以上の配列が必要です。 値は、[R][G][B]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb);

	/**
	 * この関数は、座標群にピクセルごとのRGBデータをセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_y
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_num
	 *            設定するピクセルの数を指定します。
	 * @param i_intrgb
	 *            設定するピクセル値を格納する配列を指定します。3×i_num要素以上の配列が必要です。
	 *            値は、[R1][G1][B1][R2][G2][B2]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb);
}
