/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.processor;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.NyARHistogramAnalyzer_SlidePTile;
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARHistogramFromRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilter;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.squaredetect.*;

/**
 * このクラスは、登録した複数のARマーカのうち、同時に1個のARマーカの検出処理を管理します。
 * このクラスを継承してアプリケーションを作成することで、ARマーカの状態変化に対応するイベントドリブンなアプリケーションを構築できます。
 * クラスは、マーカの出現・移動・消滅を、自己コールバック関数（イベントハンドラ）により通知します。イベントハンドラは、
 * 引数に状態変化の詳細を与えるので、アプリケーションではそれに応じた処理を実装します。
 * <p>イベントの説明-
 * このクラスには、３個の自己コールバック関数があります。{@link SingleARMarkerProcesser}は、以下のタイミングでこれらを呼び出します。
 * ユーザは継承クラスでこれらの関数に実装を行い、イベント駆動のアプリケーションを作成できます。
 * <ul>
 * <li>　{@link #onEnterHandler} - 登録したマーカが初めて見つかった時に呼び出されます。ここに、発見したマーカに対応した初期処理を書きます。
 * <li>　{@link #onLeaveHandler} - 検出中のマーカが消失した時に呼び出されます。ここに、マーカの終期処理を書きます。
 * <li>　{@link #onUpdateHandler}- 検出中のマーカの位置姿勢が更新されたときに呼び出されます。ここに、マーカ位置の更新処理を書きます。
 * </ul>
 * <p>特性-
 * <ul>
 * <li>自動敷居値調整を行うため、環境光の変化に耐性があります。
 * <li>複数のマーカが画像にある場合は、一番初めに認識したマーカを優先して認識します。
 * <li>複数の同一パターンマーカが画像にある場合は、区別できません。
 * </ul>
 * 
 * </p>
 * このクラスの定義は古いため、特別な事情が無い限り、{@link jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem}を使うべきです。
 */
public abstract class SingleARMarkerProcesser
{
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquare extends NyARSquareContourDetector_Rle implements NyARSquareContourDetector.CbHandler
	{
		//公開プロパティ
		public final NyARSquare square=new NyARSquare();
		public double confidence=0.0;
		public int code_index=-1;		
		public double cf_threshold_new = 0.50;
		public double cf_threshold_exist = 0.30;
		
		//参照
		private INyARRgbRaster _ref_raster;
		//所有インスタンス
		private INyARColorPatt _inst_patt;
		private NyARMatchPattDeviationColorData _deviation_data;
		private NyARMatchPatt_Color_WITHOUT_PCA[] _match_patt;
		private final NyARMatchPattResult __detectMarkerLite_mr=new NyARMatchPattResult();
		private NyARCoord2Linear _coordline;
		
		public DetectSquare(NyARParam i_param) throws NyARException
		{
			super(i_param.getScreenSize());
			this._match_patt=null;
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
			return;
		}
		public void setNyARCodeTable(NyARCode[] i_ref_code,int i_code_resolution) throws NyARException
		{
			/*unmanagedで実装するときは、ここでリソース解放をすること。*/
			this._deviation_data=new NyARMatchPattDeviationColorData(i_code_resolution,i_code_resolution);
			this._inst_patt=new NyARColorPatt_Perspective(i_code_resolution,i_code_resolution,4,25);
			this._match_patt = new NyARMatchPatt_Color_WITHOUT_PCA[i_ref_code.length];
			for(int i=0;i<i_ref_code.length;i++){
				this._match_patt[i]=new NyARMatchPatt_Color_WITHOUT_PCA(i_ref_code[i]);
			}
		}
		private NyARIntPoint2d[] __ref_vertex=new NyARIntPoint2d[4];
		private int _target_id;
		/**
		 * Initialize call back handler.
		 */
		public void init(INyARRgbRaster i_raster,int i_target_id)
		{
			this._ref_raster=i_raster;
			this._target_id=i_target_id;
			this.code_index=-1;
			this.confidence=Double.MIN_VALUE;
		}

		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void detectMarkerCallback(NyARIntCoordinates i_coord,int[] i_vertex_index)  throws NyARException
		{
			if (this._match_patt==null) {
				return;
			}
			//輪郭座標から頂点リストに変換
			NyARIntPoint2d[] vertex=this.__ref_vertex;
			vertex[0]=i_coord.items[i_vertex_index[0]];
			vertex[1]=i_coord.items[i_vertex_index[1]];
			vertex[2]=i_coord.items[i_vertex_index[2]];
			vertex[3]=i_coord.items[i_vertex_index[3]];
		
			//画像を取得
			if (!this._inst_patt.pickFromRaster(this._ref_raster,vertex)){
				return;//取得失敗
			}
			//取得パターンをカラー差分データに変換して評価する。
			this._deviation_data.setRaster(this._inst_patt);

			
			//code_index,dir,c1にデータを得る。
			final NyARMatchPattResult mr=this.__detectMarkerLite_mr;
			int lcode_index = 0;
			int dir = 0;
			double c1 = 0;
			for (int i = 0; i < this._match_patt.length; i++) {
				this._match_patt[i].evaluate(this._deviation_data,mr);
				double c2 = mr.confidence;
				if (c1 < c2) {
					lcode_index = i;
					c1 = c2;
					dir = mr.direction;
				}
			}
			
			//認識処理
			if (this._target_id == -1) { // マーカ未認識
				//現在は未認識
				if (c1 < this.cf_threshold_new) {
					return;
				}
				if (this.confidence > c1) {
					// 一致度が低い。
					return;
				}
				//認識しているマーカIDを保存
				this.code_index=lcode_index;
			}else{
				//現在はマーカ認識中				
				// 現在のマーカを認識したか？
				if (lcode_index != this._target_id) {
					// 認識中のマーカではないので無視
					return;
				}
				//認識中の閾値より大きいか？
				if (c1 < this.cf_threshold_exist) {
					return;
				}
				//現在の候補よりも一致度は大きいか？
				if (this.confidence>c1) {
					return;
				}
				this.code_index=this._target_id;
			}
			//新しく認識、または継続認識中に更新があったときだけ、Square情報を更新する。
			//ココから先はこの条件でしか実行されない。
			
			//一致率の高い矩形があれば、方位を考慮して頂点情報を作成
			this.confidence=c1;
			NyARSquare sq=this.square;
			//directionを考慮して、squareを更新する。
			for(int i=0;i<4;i++){
				int idx=(i+4 - dir) % 4;
				this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coord,sq.line[i]);
			}
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				if(!sq.line[i].crossPos(sq.line[(i + 3) % 4],sq.sqvertex[i])){
					throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
				}
			}
		}

	}	
	/**　ユーザーが自由に使えるタグ変数です。*/
	public Object tag;

	private int _lost_delay_count = 0;
	private int _lost_delay = 5;
	/** 姿勢変換行列の計算オブジェクト*/
	protected INyARTransMat _transmat;

	private NyARRectOffset _offset; 
	// [AR]検出結果の保存用
	private NyARGrayscaleRaster _gs_raster;


	protected int _current_arcode_index = -1;


	/**
	 * デフォルトコンストラクタ。
	 * クラスを継承するときは、このコンストラクタを呼び出した後に、{@link #initInstance}関数でインスタンスの初期化処理を実装します。
	 */
	protected SingleARMarkerProcesser()
	{
		return;
	}

	private boolean _initialized=false;
	/**
	 * この関数は、インスタンスを初期化します。
	 * 継承先のクラスから呼び出してください。
	 * @param i_param
	 * カメラパラメータオブジェクト。このサイズは、{@link #detectMarker}に入力する画像と同じサイズである必要があります。
	 * @throws NyARException
	 */
	protected void initInstance(NyARParam i_param) throws NyARException
	{
		//初期化済？
		assert(this._initialized==false);
		
		NyARIntSize scr_size = i_param.getScreenSize();
		// 解析オブジェクトを作る
		this._transmat = new NyARTransMat(i_param);
		this._thdetect=new NyARHistogramAnalyzer_SlidePTile(15);

		// ２値画像バッファを作る
		this._gs_raster = new NyARGrayscaleRaster(scr_size.w, scr_size.h);
		this._initialized=true;
		//コールバックハンドラ
		this._detectmarker=new DetectSquare(i_param);
		this._offset=new NyARRectOffset();
		return;
	}

	/**
	 * この関数は、検出するマーカパターンテーブルの配列を指定します。 
	 * マーカパターンには、配列の先頭から、0から始まるID番号を割り当てられます。
	 * このIDは、{@link #onEnterHandler}イベントハンドラに通知されるID番号に対応し、マーカパターンの識別に使います。
	 * @param i_ref_code_table
	 * マーカパターンテーブルにセットする配列です。配列にあるマーカパターンの解像度は、i_code_resolutionに一致している必要があります。
	 * @param i_code_resolution
	 * マーカパターン縦横解像度です。
	 * @param i_marker_width
	 * <p>メモ:
	 * マーカを検出している状態で関数を実行すると、イベント通知なしに、認識中のマーカを見失います。
	 * </p>
	 * @throws NyARException 
	 */
	public void setARCodeTable(NyARCode[] i_ref_code_table, int i_code_resolution, double i_marker_width) throws NyARException
	{
		if (this._current_arcode_index != -1) {
			// 強制リセット
			reset(true);
		}
		//検出するマーカセット、情報、検出器を作り直す。(1ピクセル4ポイントサンプリング,マーカのパターン領域は50%)
		this._detectmarker.setNyARCodeTable(i_ref_code_table,i_code_resolution);
		this._offset.setSquare(i_marker_width);
		return;
	}
	/**
	 * この関数は、インスタンスの状態をリセットします。
	 * 状態をリセットすると、もしマーカを認識している場合には、{@link #onLeaveHandler}イベントハンドラがコールされ、未認識状態になります。
	 * @param i_is_force
	 * 強制フラグ。trueにすると、イベント通知なしにマーカ認識状態をリセットします。
	 */
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
	private DetectSquare _detectmarker;
	
	private INyARRaster _last_input_raster=null;
	
	private INyARRgb2GsFilter _togs_filter;
	private INyARHistogramFromRaster _histmaker;
	private NyARHistogramAnalyzer_SlidePTile _thdetect;
	private NyARHistogram _hist=new NyARHistogram(256);
	
	/**
	 * この関数は、画像を処理して、適切なマーカ検出イベントハンドラを呼び出します。
	 * イベントハンドラの呼び出しは、この関数を呼び出したスレッドが、この関数が終了するまでに行います。
	 * @param i_raster
	 * 検出処理をする画像を指定します。
	 * @throws NyARException
	 */
	public void detectMarker(INyARRgbRaster i_raster) throws NyARException
	{
		// サイズチェック
		assert(this._gs_raster.getSize().isEqualSize(i_raster.getSize().w, i_raster.getSize().h));
		if(this._last_input_raster!=i_raster){
			this._histmaker=(INyARHistogramFromRaster) this._gs_raster.createInterface(INyARHistogramFromRaster.class);
			this._togs_filter=(INyARRgb2GsFilter) i_raster.createInterface(INyARRgb2GsFilter.class);
			this._last_input_raster=i_raster;
		}

		//GSイメージへの変換とヒストグラムの生成
		this._togs_filter.convert(this._gs_raster);
		this._histmaker.createHistogram(4,this._hist);

		// スクエアコードを探す
		this._detectmarker.init(i_raster,this._current_arcode_index);
		this._detectmarker.detectMarker(this._gs_raster,this._thdetect.getThreshold(this._hist),this._detectmarker);
		
		// 認識状態を更新
		this.updateStatus(this._detectmarker.square,this._detectmarker.code_index);
		return;
	}
	/**
	 * この関数は、マーカパターンの一致率の敷居値を設定します。
	 * 敷居値は、0.0&lt;n&lt;1.0の範囲で指定します。
	 * @param i_new_cf
	 * 新しくマーカを発見するときの閾値です。
	 * @param i_exist_cf
	 * 継続してマーカを追跡するときの閾値です。
	 * i_new_cfの6割程度の値を指定すると良いでしょう。
	 */
	public void setConfidenceThreshold(double i_new_cf,double i_exist_cf)
	{
		this._detectmarker.cf_threshold_exist=i_exist_cf;
		this._detectmarker.cf_threshold_new=i_new_cf;
	}

	private NyARDoubleMatrix44 _transmat_result = new NyARDoubleMatrix44();
	private NyARTransMatResultParam _last_result_param = new NyARTransMatResultParam();

	/**	オブジェクトのステータスを更新し、必要に応じて自己コールバック関数を駆動します。
	 * 	戻り値は、「実際にマーカを発見する事ができたか」を示す真偽値です。クラスの状態とは異なります。
	 */
	private boolean updateStatus(NyARSquare i_square, int i_code_index)  throws NyARException
	{
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
				this._transmat.transMat(i_square, this._offset, this._transmat_result,this._last_result_param);
				// OnUpdate
				this.onUpdateHandler(i_square,this._transmat_result);
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
				if(!this._transmat.transMatContinue(i_square,  this._offset, this._transmat_result, this._last_result_param.last_error, this._transmat_result, this._last_result_param)){
					this._transmat.transMat(i_square, this._offset,this._transmat_result,this._last_result_param);
				}
				// OnUpdate
				this.onUpdateHandler(i_square, this._transmat_result);
				this._lost_delay_count = 0;
				return true;
			} else {// 異なるコードの認識→今はサポートしない。
				throw new  NyARException();
			}
		}
	}
	/**
	 * 自己コールバック関数です。
	 * 継承したクラスで、マーカ発見時の処理を実装してください。
	 * @param i_code
	 * 検出したマーカパターンのID番号です。ID番号については、{@link #setARCodeTable}の説明を参照してください。
	 */
	protected abstract void onEnterHandler(int i_code);
	/**
	 * 自己コールバック関数です。
	 * 継承したクラスで、マーカ消失時の処理を実装してください。
	 */
	protected abstract void onLeaveHandler();
	/**
	 * 自己コールバック関数です。
	 * 継承したクラスで、マーカ更新時の処理を実装してください。
	 * 引数の値の有効期間は、関数が終了するまでです。
	 * @param i_square
	 * 現在のマーカ検出位置です。
	 * @param o_result
	 * 現在の姿勢変換行列です。
	 */
	protected abstract void onUpdateHandler(NyARSquare i_square, NyARDoubleMatrix44 o_result);
}
