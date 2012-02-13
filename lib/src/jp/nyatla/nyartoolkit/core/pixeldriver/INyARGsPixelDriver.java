package jp.nyatla.nyartoolkit.core.pixeldriver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public interface INyARGsPixelDriver
{
	/**
	 * この関数は、ピクセルドライバの参照する画像のサイズを返します。
	 * @return
	 * [readonly]
	 */
	public NyARIntSize getSize();
	public void getPixelSet(int[] i_x,int[] i_y,int i_n,int[] o_buf,int i_st_buf) throws NyARException;
	public int getPixel(int i_x,int i_y) throws NyARException;
	public void switchRaster(INyARRaster i_ref_raster) throws NyARException;
	public boolean isCompatibleRaster(INyARRaster i_raster);
	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 * 書込むピクセルの座標。画像の範囲内である事。
	 * @param i_y
	 * 書込むピクセルの座標。画像の範囲内である事。
	 * @param i_rgb
	 * 設定するピクセル値。
	 * @throws NyARException
	 */
	public void setPixel(int i_x, int i_y, int i_gs) throws NyARException;
	/**
	 * この関数は、座標群にピクセルごとのRGBデータをセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 * 書き込むピクセルの座標配列。画像の範囲内である事。
	 * @param i_y
	 * 書き込むピクセルの座標配列。画像の範囲内である事。
	 * @param i_intgs
	 * 設定するピクセル値の数
	 * @throws NyARException
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intgs) throws NyARException;	
}

