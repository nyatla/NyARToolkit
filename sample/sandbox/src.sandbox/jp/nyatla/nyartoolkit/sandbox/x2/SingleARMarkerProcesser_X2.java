/* 
 * Capture Test NyARToolkitCSサンプルプログラム
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.sandbox.x2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.analyzer.threshold.NyARRasterThresholdAnalyzerBuilder_Threshold;
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilterBuilder_RgbToBin;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;

/**
 * このクラスは、同時に１個のマーカを処理することのできる、アプリケーションプロセッサです。
 * マーカの出現・移動・消滅を、イベントで通知することができます。
 * クラスには複数のマーカを登録できます。一つのマーカが見つかると、プロセッサは継続して同じマーカを
 * １つだけ認識し続け、見失うまでの間は他のマーカを認識しません。
 * 
 * イベントは、 OnEnter→OnUpdate[n]→OnLeaveの順で発生します。
 * マーカが見つかるとまずOnEnterが１度発生して、何番のマーカが発見されたかがわかります。
 * 次にOnUpdateにより、現在の変換行列が連続して渡されます。最後にマーカを見失うと、OnLeave
 * イベントが発生します。
 * 
 */
public abstract class SingleARMarkerProcesser_X2
{
	/**selectARCodeIndexFromListが値を返す時に使う変数型です。
	 */

	private class TResult_selectARCodeIndex
	{
		public int direction;

		public double confidence;

		public int code_index;
	}
	/**オーナーが自由に使えるタグ変数です。
	 */
	public Object tag;

	private int _lost_delay_count = 0;

	private int _lost_delay = 5;

	private NyARSquareContourDetector _square_detect;

	//<X2 patch>
	protected NyARTransMat_X2 _transmat;
	//</X2 patch>
	private double _marker_width;

	private NyARMatchPatt_Color_WITHOUT_PCA[] _match_patt;

	private NyARSquareStack _square_list = new NyARSquareStack(100);

	private INyARColorPatt _patt = null;

	private double _cf_threshold_new = 0.30;
	private double _cf_threshold_exist = 0.15;
	
	private int _threshold = 110;
	// [AR]検出結果の保存用
	private NyARBinRaster _bin_raster;

	private NyARRasterFilter_ARToolkitThreshold _tobin_filter;

	protected int _current_arcode_index = -1;

	private NyARMatchPattDeviationColorData _deviation_data;
	private NyARRasterThresholdAnalyzerBuilder_Threshold _threshold_detect;
	
	protected SingleARMarkerProcesser_X2()
	{
		return;
	}


	protected void initInstance(NyARParam i_param,int i_raster_type) throws NyARException
	{
		NyARIntSize scr_size = i_param.getScreenSize();
		// 解析オブジェクトを作る
//<X2 patch>
		this._square_detect = new NyARSquareDetector_X2(i_param.getDistortionFactor(), scr_size);
		this._transmat = new NyARTransMat_X2(i_param);
//</X2 patch>

		this._tobin_filter=new NyARRasterFilter_ARToolkitThreshold(110,i_raster_type);

		// ２値画像バッファを作る
		this._bin_raster = new NyARBinRaster(scr_size.w, scr_size.h);
		this._threshold_detect=new NyARRasterThresholdAnalyzerBuilder_Threshold(15,i_raster_type,4);
		return;
	}

	/**検出するマーカコードの配列を指定します。 検出状態でこの関数を実行すると、
	 * オブジェクト状態に強制リセットがかかります。
	 */
	public void setARCodeTable(NyARCode[] i_ref_code_table, int i_code_resolution, double i_marker_width)
	{
		if (this._current_arcode_index != -1) {
			// 強制リセット
			reset(true);
		}
		//検出するマーカセット、情報、検出器を作り直す。(1ピクセル4ポイントサンプリング,マーカのパターン領域は50%)
		this._patt = new NyARColorPatt_Perspective_O2(i_code_resolution, i_code_resolution,4,25);
		this._deviation_data=new NyARMatchPattDeviationColorData(i_code_resolution, i_code_resolution);
		this._marker_width = i_marker_width;

		this._match_patt = new NyARMatchPatt_Color_WITHOUT_PCA[i_ref_code_table.length];
		for(int i=0;i<i_ref_code_table.length;i++){
			this._match_patt[i]=new NyARMatchPatt_Color_WITHOUT_PCA(i_ref_code_table[i]);
		}
		return;
	}

	public void reset(boolean i_is_force)
	{
		if (this._current_arcode_index != -1 && i_is_force == false) {
			// 強制書き換えでなければイベントコール
			this.onLeaveHandler();
		}
		// カレントマーカをリセット
		this._current_arcode_index = -1;
		return;
	}

	public void detectMarker(INyARRgbRaster i_raster) throws NyARException
	{
		// サイズチェック
		assert(this._bin_raster.getSize().isEqualSize(i_raster.getSize().w, i_raster.getSize().h));

		// コードテーブルが無ければここで終わり
		if (this._match_patt== null) {
			return;
		}

		// ラスタを(1/4の画像の)２値イメージに変換する.
		this._tobin_filter.setThreshold(this._threshold);
		this._tobin_filter.doFilter(i_raster, this._bin_raster);

		NyARSquareStack square_stack = this._square_list;
		// スクエアコードを探す
		this._square_detect.detectMarkerCB(this._bin_raster, square_stack);
		// 認識処理
		if (this._current_arcode_index == -1) { // マーカ未認識
			detectNewMarker(i_raster, square_stack);
		} else { // マーカ認識中
			detectExistMarker(i_raster, square_stack, this._current_arcode_index);
		}
		return;
	}

	
	private final NyARMatchPattResult __detectMarkerLite_mr=new NyARMatchPattResult();
	
	/**ARCodeのリストから、最も一致するコード番号を検索します。
	 */
	private boolean selectARCodeIndexFromList(INyARRgbRaster i_raster, NyARSquare i_square, TResult_selectARCodeIndex o_result) throws NyARException
	{
		// 現在コードテーブルはアクティブ？
		if (this._match_patt==null) {
			return false;
		}
		// 評価基準になるパターンをイメージから切り出す
		if (!this._patt.pickFromRaster(i_raster, i_square.imvertex)) {
			return false;
		}
		//評価データを作成して、評価器にセット
		this._deviation_data.setRaster(this._patt);		
		final NyARMatchPattResult mr=this.__detectMarkerLite_mr;
		int code_index = 0;
		int dir = 0;
		double c1 = 0;
		// コードと比較する
		for (int i = 0; i < this._match_patt.length; i++) {
			this._match_patt[i].evaluate(this._deviation_data,mr);
			double c2 = mr.confidence;
			if (c1 < c2) {
				code_index = i;
				c1 = c2;
				dir = mr.direction;
			}
		}
		o_result.code_index = code_index;
		o_result.direction = dir;
		o_result.confidence = c1;
		return true;
	}

	private TResult_selectARCodeIndex __detect_X_Marker_detect_result = new TResult_selectARCodeIndex();

	/**新規マーカ検索 現在認識中のマーカがないものとして、最も認識しやすいマーカを１個認識します。
	 */
	private void detectNewMarker(INyARRgbRaster i_raster, NyARSquareStack i_stack) throws NyARException
	{
		int number_of_square = i_stack.getLength();
		double cf = 0;
		int dir = 0;
		int code_index = -1;
		int square_index = 0;
		TResult_selectARCodeIndex detect_result = this.__detect_X_Marker_detect_result;
		for (int i = 0; i < number_of_square; i++) {
			if (!selectARCodeIndexFromList(i_raster, (i_stack.getItem(i)), detect_result)) {
				// 見つからない。
				return;
			}
			if (detect_result.confidence < this._cf_threshold_new) {
				continue;
			}
			if (detect_result.confidence < cf) {
				// 一致度が低い。
				continue;
			}
			cf = detect_result.confidence;
			code_index = detect_result.code_index;
			square_index = i;
			dir = detect_result.direction;
		}
		// 認識状態を更新
		final boolean is_id_found=updateStatus(this._square_list.getItem(square_index), code_index, cf, dir);
		//閾値フィードバック(detectExistMarkerにもあるよ)
		if(!is_id_found){
			//マーカがなければ、探索+DualPTailで基準輝度検索
			this._threshold_detect.analyzeRaster(i_raster);
			this._threshold=(this._threshold+this._threshold_detect.getThreshold())/2;
		}
	}

	/**マーカの継続認識 現在認識中のマーカを優先して認識します。 
	 * （注）この機能はたぶん今後いろいろ発展するからNewと混ぜないこと。
	 */
	private void detectExistMarker(INyARRgbRaster i_raster, NyARSquareStack i_stack, int i_current_id) throws NyARException
	{
		int number_of_square = i_stack.getLength();
		double cf = 0;
		int dir = 0;
		int code_index = -1;
		int square_index = 0;
		TResult_selectARCodeIndex detect_result = this.__detect_X_Marker_detect_result;
		for (int i = 0; i < number_of_square; i++) {
			if (!selectARCodeIndexFromList(i_raster,i_stack.getItem(i), detect_result)) {
				// 見つからない。
				return;
			}
			// 現在のマーカを認識したか？
			if (detect_result.code_index != i_current_id) {
				// 認識中のマーカではないので無視
				continue;
			}
			if (detect_result.confidence < this._cf_threshold_exist) {
				continue;
			}
			if (detect_result.confidence < cf) {
				// 一致度が高い方を選ぶ
				continue;
			}
			cf = detect_result.confidence;
			code_index = detect_result.code_index;
			dir = detect_result.direction;
			square_index = i;
		}
		// 認識状態を更新
		final boolean is_id_found=updateStatus(this._square_list.getItem(square_index), code_index, cf, dir);
		//閾値フィードバック(detectExistMarkerにもあるよ)
		if(!is_id_found){
			//マーカがなければ、探索+DualPTailで基準輝度検索
			this._threshold_detect.analyzeRaster(i_raster);
			this._threshold=(this._threshold+this._threshold_detect.getThreshold())/2;
		}
		
	}

	private NyARTransMatResult __NyARSquare_result = new NyARTransMatResult();

	/**	オブジェクトのステータスを更新し、必要に応じてハンドル関数を駆動します。
	 * 	戻り値は、「実際にマーカを発見する事ができたか」です。クラスの状態とは異なります。
	 */
	private boolean updateStatus(NyARSquare i_square, int i_code_index, double i_cf, int i_dir)  throws NyARException
	{
		NyARTransMatResult result = this.__NyARSquare_result;
		if (this._current_arcode_index < 0) {// 未認識中
			if (i_code_index < 0) {// 未認識から未認識の遷移
				// なにもしないよーん。
				return false;
			} else {// 未認識から認識の遷移
				this._current_arcode_index = i_code_index;
				// イベント生成
				// OnEnter
				this.onEnterHandler(i_code_index);
				// 変換行列を作成
				this._transmat.transMat(i_square, i_dir, this._marker_width, result);
				// OnUpdate
				this.onUpdateHandler(i_square, result);
				this._lost_delay_count = 0;
				return true;
			}
		} else {// 認識中
			if (i_code_index < 0) {// 認識から未認識の遷移
				this._lost_delay_count++;
				if (this._lost_delay < this._lost_delay_count) {
					// OnLeave
					this._current_arcode_index = -1;
					this.onLeaveHandler();
				}
				return false;
			} else if (i_code_index == this._current_arcode_index) {// 同じARCodeの再認識
				// イベント生成
				// 変換行列を作成
				this._transmat.transMat(i_square, i_dir, this._marker_width, result);
				// OnUpdate
				this.onUpdateHandler(i_square, result);
				this._lost_delay_count = 0;
				return true;
			} else {// 異なるコードの認識→今はサポートしない。
				throw new  NyARException();
			}
		}
	}

	protected abstract void onEnterHandler(int i_code);

	protected abstract void onLeaveHandler();

	protected abstract void onUpdateHandler(NyARSquare i_square, NyARTransMatResult result);
}
