package jp.nyatla.nyartoolkit.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective_O2;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

/**
 * マーカの周辺領域からビットマップを取得する方法を提供します。
 * 比較的負荷が大きいので、連続してパターンを取得し続ける用途には向いていません。
 *
 */
class TransformedBitmapPickup extends NyARColorPatt_Perspective_O2
{
	private NyARIntPoint2d[] _work_points = NyARIntPoint2d.createArray(4);

	private NyARPerspectiveProjectionMatrix _ref_perspective;

	/**
	 * 
	 * @param i_width
	 * 取得するビットマップの幅
	 * @param i_height
	 * 取得するビットマップの解像度
	 * @param i_resolution
	 * resolution of reading pixel per point. ---- 取得時の解像度。高解像度のときは1を指定してください。低解像度のときは2以上を指定します。
	 */
	public TransformedBitmapPickup(NyARPerspectiveProjectionMatrix i_ref_cparam, int i_width, int i_height, int i_resolution)
	{
		super(i_width, i_height, i_resolution, 0);
		this._ref_perspective = i_ref_cparam;
	}

	/**
	 * This function retrieves bitmap from the area defined by RECT(i_l,i_t,i_r,i_b) above transform matrix i_base_mat. 
	 * ----
	 * この関数は、basementで示される平面のAで定義される領域から、ビットマップを読み出します。
	 * 例えば、8cmマーカでRECT(i_l,i_t,i_r,i_b)に-40,0,0,-40.0を指定すると、マーカの左下部分の画像を抽出します。
	 * 
	 * マーカから離れた場所になるほど、また、マーカの鉛直方向から外れるほど誤差が大きくなります。
	 * @param i_src_imege
	 * 詠み出し元の画像を指定します。
	 * @param i_l
	 * 基準点からの左上の相対座標（x）を指定します。
	 * @param i_t
	 * 基準点からの左上の相対座標（y）を指定します。
	 * @param i_r
	 * 基準点からの右下の相対座標（x）を指定します。
	 * @param i_b
	 * 基準点からの右下の相対座標（y）を指定します。
	 * @param i_base_mat
	 * @return 画像の取得の成否を返す。
	 */
	public boolean pickupImage2d(INyARRgbRaster i_src_imege, double i_l, double i_t, double i_r, double i_b, NyARTransMatResult i_base_mat) throws NyARException
	{
		double cp00, cp01, cp02, cp11, cp12;
		cp00 = this._ref_perspective.m00;
		cp01 = this._ref_perspective.m01;
		cp02 = this._ref_perspective.m02;
		cp11 = this._ref_perspective.m11;
		cp12 = this._ref_perspective.m12;
		//マーカと同一平面上にある矩形の4個の頂点を座標変換して、射影変換して画面上の
		//頂点を計算する。
		//[hX,hY,h]=[P][RT][x,y,z]

		//出力先
		NyARIntPoint2d[] poinsts = this._work_points;		
		
		double yt0,yt1,yt2;
		double x3, y3, z3;
		
		double m00=i_base_mat.m00;
		double m10=i_base_mat.m10;
		double m20=i_base_mat.m20;
		
		//yとtの要素を先に計算
		yt0=i_base_mat.m01 * i_t+i_base_mat.m03;
		yt1=i_base_mat.m11 * i_t+i_base_mat.m13;
		yt2=i_base_mat.m21 * i_t+i_base_mat.m23;
		// l,t
		x3 = m00 * i_l + yt0;
		y3 = m10 * i_l + yt1;
		z3 = m20 * i_l + yt2;
		poinsts[0].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		poinsts[0].y = (int) ((y3 * cp11 + z3 * cp12) / z3);
		// r,t
		x3 = m00 * i_r + yt0;
		y3 = m10 * i_r + yt1;
		z3 = m20 * i_r + yt2;
		poinsts[1].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		poinsts[1].y = (int) ((y3 * cp11 + z3 * cp12) / z3);

		//yとtの要素を先に計算
		yt0=i_base_mat.m01 * i_b+i_base_mat.m03;
		yt1=i_base_mat.m11 * i_b+i_base_mat.m13;
		yt2=i_base_mat.m21 * i_b+i_base_mat.m23;

		// r,b
		x3 = m00 * i_r + yt0;
		y3 = m10 * i_r + yt1;
		z3 = m20 * i_r + yt2;
		poinsts[2].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		poinsts[2].y = (int) ((y3 * cp11 + z3 * cp12) / z3);
		// l,b
		x3 = m00 * i_l + yt0;
		y3 = m10 * i_l + yt1;
		z3 = m20 * i_l + yt2;
		poinsts[3].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		poinsts[3].y = (int) ((y3 * cp11 + z3 * cp12) / z3);
		return this.pickFromRaster(i_src_imege, poinsts);
	}
}