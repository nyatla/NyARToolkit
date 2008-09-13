/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.detector;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.types.*;

class NyARDetectMarkerResult
{
	public int arcode_id;

	public int direction;

	public double confidence;

	public NyARSquare ref_square;
}

class NyARDetectMarkerResultHolder
{
	public NyARDetectMarkerResult[] result_array = new NyARDetectMarkerResult[1];

	/**
	 * result_holderを最大i_reserve_size個の要素を格納できるように予約します。
	 * 
	 * @param i_reserve_size
	 */
	public void reservHolder(int i_reserve_size)
	{
		if (i_reserve_size >= result_array.length) {
			int new_size = i_reserve_size + 5;
			result_array = new NyARDetectMarkerResult[new_size];
			for (int i = 0; i < new_size; i++) {
				result_array[i] = new NyARDetectMarkerResult();
			}
		}
	}
}

/**
 * 複数のマーカーを検出し、それぞれに最も一致するARコードを、コンストラクタで登録したARコードから 探すクラスです。最大300個を認識しますが、ゴミラベルを認識したりするので100個程度が限界です。
 * 
 */
public class NyARDetectMarker
{
	private static final int AR_SQUARE_MAX = 300;

	private boolean _is_continue = false;

	private NyARMatchPatt_Color_WITHOUT_PCA _match_patt;

	private NyARSquareDetector _square_detect;

	private final NyARSquareStack _square_list = new NyARSquareStack(AR_SQUARE_MAX);

	private NyARCode[] _codes;

	protected INyARTransMat _transmat;

	private double[] _marker_width;

	private int _number_of_code;

	// 検出結果の保存用
	private INyARColorPatt _patt;

	private NyARDetectMarkerResultHolder _result_holder = new NyARDetectMarkerResultHolder();

	/**
	 * 複数のマーカーを検出し、最も一致するARCodeをi_codeから検索するオブジェクトを作ります。
	 * 
	 * @param i_param
	 * カメラパラメータを指定します。
	 * @param i_code
	 * 検出するマーカーのARCode配列を指定します。配列要素のインデックス番号が、そのままgetARCodeIndex関数で 得られるARCodeインデックスになります。 例えば、要素[1]のARCodeに一致したマーカーである場合は、getARCodeIndexは1を返します。
	 * 先頭からi_number_of_code個の要素には、有効な値を指定する必要があります。
	 * @param i_marker_width
	 * i_codeのマーカーサイズをミリメートルで指定した配列を指定します。 先頭からi_number_of_code個の要素には、有効な値を指定する必要があります。
	 * @param i_number_of_code
	 * i_codeに含まれる、ARCodeの数を指定します。
	 * @throws NyARException
	 */
	public NyARDetectMarker(NyARParam i_param, NyARCode[] i_code, double[] i_marker_width, int i_number_of_code) throws NyARException
	{
		final NyARIntSize scr_size=i_param.getScreenSize();
		// 解析オブジェクトを作る
		this._square_detect = new NyARSquareDetector(i_param.getDistortionFactor(),scr_size);
		this._transmat = new NyARTransMat(i_param);
		// 比較コードを保存
		this._codes = i_code;
		// 比較コードの解像度は全部同じかな？（違うとパターンを複数種つくらないといけないから）
		final int cw = i_code[0].getWidth();
		final int ch = i_code[0].getHeight();
		for (int i = 1; i < i_number_of_code; i++) {
			if (cw != i_code[i].getWidth() || ch != i_code[i].getHeight()) {
				// 違う解像度のが混ざっている。
				throw new NyARException();
			}
		}
		// 評価パターンのホルダを作る
		this._patt = new NyARColorPatt_O3(cw, ch);
		this._number_of_code = i_number_of_code;

		this._marker_width = i_marker_width;
		// 評価器を作る。
		this._match_patt = new NyARMatchPatt_Color_WITHOUT_PCA();
		//２値画像バッファを作る
		this._bin_raster=new NyARBinRaster(scr_size.w,scr_size.h);		
	}

	private NyARBinRaster _bin_raster;

	private NyARRasterFilter_ARToolkitThreshold _tobin_filter = new NyARRasterFilter_ARToolkitThreshold(100);

	/**
	 * i_imageにマーカー検出処理を実行し、結果を記録します。
	 * 
	 * @param i_raster
	 * マーカーを検出するイメージを指定します。
	 * @param i_thresh
	 * 検出閾値を指定します。0～255の範囲で指定してください。 通常は100～130くらいを指定します。
	 * @return 見つかったマーカーの数を返します。 マーカーが見つからない場合は0を返します。
	 * @throws NyARException
	 */
	public int detectMarkerLite(INyARRgbRaster i_raster, int i_threshold) throws NyARException
	{
		// サイズチェック
		if (!this._bin_raster.getSize().isEqualSize(i_raster.getSize())) {
			throw new NyARException();
		}

		// ラスタを２値イメージに変換する.
		this._tobin_filter.setThreshold(i_threshold);
		this._tobin_filter.doFilter(i_raster, this._bin_raster);

		NyARSquareStack l_square_list = this._square_list;
		// スクエアコードを探す
		this._square_detect.detectMarker(this._bin_raster, l_square_list);

		final int number_of_square = l_square_list.getLength();
		// コードは見つかった？
		if (number_of_square < 1) {
			// ないや。おしまい。
			return 0;
		}
		// 保持リストのサイズを調整
		this._result_holder.reservHolder(number_of_square);

		// 1スクエア毎に、一致するコードを決定していく
		for (int i = 0; i < number_of_square; i++) {
			NyARSquare square = l_square_list.getItem(i);
			// 評価基準になるパターンをイメージから切り出す
			if (!this._patt.pickFromRaster(i_raster, square)) {
				// イメージの切り出しは失敗することもある。
				continue;
			}
			// パターンを評価器にセット
			if (!this._match_patt.setPatt(this._patt)) {
				// 計算に失敗した。
				throw new NyARException();
			}
			// コードと順番に比較していく
			int code_index = 0;
			_match_patt.evaluate(_codes[0]);
			double confidence = _match_patt.getConfidence();
			int direction = _match_patt.getDirection();
			for (int i2 = 1; i2 < this._number_of_code; i2++) {
				// コードと比較する
				_match_patt.evaluate(_codes[i2]);
				double c2 = _match_patt.getConfidence();
				if (confidence > c2) {
					continue;
				}
				// より一致するARCodeの情報を保存
				code_index = i2;
				direction = _match_patt.getDirection();
				confidence = c2;
			}
			// i番目のパターン情報を保存する。
			final NyARDetectMarkerResult result = this._result_holder.result_array[i];
			result.arcode_id = code_index;
			result.confidence = confidence;
			result.direction = direction;
			result.ref_square = square;
		}
		return number_of_square;
	}

	/**
	 * i_indexのマーカーに対する変換行列を計算し、結果値をo_resultへ格納します。 直前に実行したdetectMarkerLiteが成功していないと使えません。
	 * 
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @param o_result
	 * 結果値を受け取るオブジェクトを指定してください。
	 * @throws NyARException
	 */
	public void getTransmationMatrix(int i_index, NyARTransMatResult o_result) throws NyARException
	{
		final NyARDetectMarkerResult result = this._result_holder.result_array[i_index];
		// 一番一致したマーカーの位置とかその辺を計算
		if (_is_continue) {
			_transmat.transMatContinue(result.ref_square, result.direction, _marker_width[result.arcode_id], o_result);
		} else {
			_transmat.transMat(result.ref_square, result.direction, _marker_width[result.arcode_id], o_result);
		}
		return;
	}

	/**
	 * i_indexのマーカーの一致度を返します。
	 * 
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @return マーカーの一致度を返します。0～1までの値をとります。 一致度が低い場合には、誤認識の可能性が高くなります。
	 * @throws NyARException
	 */
	public double getConfidence(int i_index)
	{
		return this._result_holder.result_array[i_index].confidence;
	}

	/**
	 * i_indexのマーカーの方位を返します。
	 * 
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @return 0,1,2,3の何れかを返します。
	 */
	public int getDirection(int i_index)
	{
		return this._result_holder.result_array[i_index].direction;
	}

	/**
	 * i_indexのマーカーのARCodeインデックスを返します。
	 * 
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @return
	 */
	public int getARCodeIndex(int i_index)
	{
		return this._result_holder.result_array[i_index].arcode_id;
	}

	/**
	 * getTransmationMatrixの計算モードを設定します。
	 * 
	 * @param i_is_continue
	 * TRUEなら、transMatContinueを使用します。 FALSEなら、transMatを使用します。
	 */
	public void setContinueMode(boolean i_is_continue)
	{
		this._is_continue = i_is_continue;
	}

}
