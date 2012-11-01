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

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.NyARHistogramAnalyzer_SlidePTile;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARHistogramFromRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilter;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
/**
 * このクラスは、1個のNyARマーカの検出処理を管理します。このクラスを継承してアプリケーションを作成することで、
 * マーカの状態変化に対応するイベントドリブンなアプリケーションを構築できます。
 * クラスは、マーカの出現・移動・消滅を、自己コールバック関数（イベントハンドラ）により通知します。イベントハンドラは、
 * 引数に状態変化の詳細を与えるので、アプリケーションではそれに応じた処理を実装します。
 * 拡張機能として、独自のNyIdマーカのデコーダを指定することができます。デコーダの種類を変えることで、異なる種類のNyIdマーカを同じクラスで
 * 取り扱うことができます。
 * <p>自己コールバック関数の説明-
 * このクラスには、３個の自己コールバック関数（イベントハンドラ）があります。{@link SingleARMarkerProcesser}は、以下のタイミングでこれらを呼び出します。
 * <ul>
 * <li>　{@link #onEnterHandler} - 登録したマーカが初めて見つかった時に呼び出されます。ここに、発見したマーカに対応した初期処理を書きます。
 * <li>　{@link #onLeaveHandler} - 検出中のマーカが消失した時に呼び出されます。ここに、マーカの終期処理を書きます。
 * <li>　{@link #onUpdateHandler}- 検出中のマーカの位置姿勢が更新されたときに呼び出されます。ここに、マーカ位置の更新処理を書きます。
 * </ul>
 * </p>
 * <p>特性-
 * <ul>
 * <li>自動敷居値調整を行うため、環境光の変化に耐性があります。
 * <li>複数のNyIdマーカが画像にある場合は、一番初めに認識したIdのマーカを優先して認識します。
 * <li>複数の同一IDのNyIdマーカが画像にある場合は、区別できません。
 * </ul>
 * </p>
 * このクラスの定義は古いため、特別な事情が無い限り、{@link jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem}を使うべきです。
 */
public abstract class SingleNyIdMarkerProcesser
{
	/**
	 * Rle矩形Detectorのブリッジ
	 */
	private class RleDetector extends NyARSquareContourDetector_Rle implements NyARSquareContourDetector.CbHandler
	{
		//公開プロパティ
		public final NyARSquare square=new NyARSquare();
		public INyIdMarkerData marker_data;
		public int threshold;
		//参照
		private INyARGrayscaleRaster _ref_raster;
		//所有インスタンス
		private INyIdMarkerData _current_data;
		private final NyIdMarkerPickup _id_pickup;
		private NyARCoord2Linear _coordline;
		private INyIdMarkerDataEncoder _encoder;
		private INyIdMarkerData _data_temp;
		private INyIdMarkerData _prev_data;
		
		public RleDetector(NyARParam i_param,INyIdMarkerDataEncoder i_encoder,NyIdMarkerPickup i_id_pickup) throws NyARException
		{
			super(i_param.getScreenSize());
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
			this._data_temp=i_encoder.createDataInstance();
			this._current_data=i_encoder.createDataInstance();
			this._encoder=i_encoder;
			this._id_pickup=i_id_pickup;
			return;
		}
		private NyARIntPoint2d[] __ref_vertex=new NyARIntPoint2d[4];
		/**
		 * Initialize call back handler.
		 */
		public void init(INyARGrayscaleRaster i_raster,INyIdMarkerData i_prev_data)
		{
			this.marker_data=null;
			this._prev_data=i_prev_data;
			this._ref_raster=i_raster;
		}
		private final NyIdMarkerParam _marker_param=new NyIdMarkerParam();
		private final NyIdMarkerPattern _marker_data=new NyIdMarkerPattern();

		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void detectMarkerCallback(NyARIntCoordinates i_coord,int[] i_vertex_index)  throws NyARException
		{
			//既に発見済なら終了
			if(this.marker_data!=null){
				return;
			}
			//輪郭座標から頂点リストに変換
			NyARIntPoint2d[] vertex=this.__ref_vertex;
			vertex[0]=i_coord.items[i_vertex_index[0]];
			vertex[1]=i_coord.items[i_vertex_index[1]];
			vertex[2]=i_coord.items[i_vertex_index[2]];
			vertex[3]=i_coord.items[i_vertex_index[3]];
		
			NyIdMarkerParam param=this._marker_param;
			NyIdMarkerPattern patt_data  =this._marker_data;			
			// 評価基準になるパターンをイメージから切り出す
			if (!this._id_pickup.pickFromRaster(this._ref_raster.getGsPixelDriver(),vertex, patt_data, param)){
				return;
			}
			//エンコード
			if(!this._encoder.encode(patt_data,this._data_temp)){
				return;
			}

			//継続認識要求されている？
			if (this._prev_data==null){
				//継続認識要求なし
				this._current_data.copyFrom(this._data_temp);
			}else{
				//継続認識要求あり
				if(!this._prev_data.isEqual((this._data_temp))){
					return;//認識請求のあったIDと違う。
				}
			}
			//新しく認識、または継続認識中に更新があったときだけ、Square情報を更新する。
			//ココから先はこの条件でしか実行されない。
			NyARSquare sq=this.square;
			//directionを考慮して、squareを更新する。
			for(int i=0;i<4;i++){
				int idx=(i+4 - param.direction) % 4;
				this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coord,sq.line[i]);
			}
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				if(!sq.line[i].crossPos(sq.line[(i + 3) % 4],sq.sqvertex[i])){
					throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
				}
			}
			this.threshold=param.threshold;
			this.marker_data=this._current_data;//みつかった。
		}	
	}	

	
	/**　ユーザーが自由に使えるタグ変数です。イベントハンドラの中で生成したオブジェクト等を、一時的に所有させたい場合などに使います。*/
	public Object tag;

	/**
	 * ロスト遅延の管理
	 */
	private int _lost_delay_count = 0;
	private int _lost_delay = 5;

	private RleDetector _square_detect;
	protected INyARTransMat _transmat;
	private NyARRectOffset _offset; 
	private boolean _is_active;
	private int _current_threshold=110;
	// [AR]検出結果の保存用
	private NyARGrayscaleRaster _gs_raster;
	private INyIdMarkerData _data_current;
	private NyARHistogramAnalyzer_SlidePTile _threshold_detect;
	private NyARHistogram _hist=new NyARHistogram(256);
	private INyARHistogramFromRaster _histmaker;
	/**
	 * デフォルトコンストラクタ。
	 * クラスを継承するときは、このコンストラクタを呼び出した後に、{@link #initInstance}関数でインスタンスの初期化処理を実装します。
	 */
	protected SingleNyIdMarkerProcesser()
	{
		return;
	}
	private boolean _initialized=false;
	/**
	 * この関数は、インスタンスを初期化します。
	 * 継承先のクラスから呼び出してください。
	 * @param i_param
	 * カメラパラメータオブジェクト。このサイズは、{@link #detectMarker}に入力する画像と同じサイズである必要があります。
	 * @param i_encoder
	 * IDマーカの値エンコーダを指定します。
	 * @param i_marker_width
	 * マーカの物理縦横サイズをmm単位で指定します。
	 * @throws NyARException
	 */
	protected void initInstance(NyARParam i_param,INyIdMarkerDataEncoder i_encoder,double i_marker_width) throws NyARException
	{
		//初期化済？
		assert(this._initialized==false);
		
		NyARIntSize scr_size = i_param.getScreenSize();
		// 解析オブジェクトを作る
		this._square_detect = new RleDetector(
			i_param,
			i_encoder,
			new NyIdMarkerPickup());
		this._transmat = new NyARTransMat(i_param);

		// ２値画像バッファを作る
		this._gs_raster = new NyARGrayscaleRaster(scr_size.w, scr_size.h);
		this._histmaker=(INyARHistogramFromRaster) this._gs_raster.createInterface(INyARHistogramFromRaster.class);
		//ワーク用のデータオブジェクトを２個作る
		this._data_current=i_encoder.createDataInstance();
		this._threshold_detect=new NyARHistogramAnalyzer_SlidePTile(15);
		this._initialized=true;
		this._is_active=false;
		this._offset=new NyARRectOffset();
		this._offset.setSquare(i_marker_width);
		return;
		
	}
	/**
	 * この関数は、マーカの物理サイズを変更します。
	 * @param i_width
	 * マーカの物理縦横サイズをmm単位で指定します。
	 */
	public void setMarkerWidth(int i_width)
	{
		this._offset.setSquare(i_width);
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
		if (i_is_force == false && this._is_active){
			// 強制書き換えでなければイベントコール
			this.onLeaveHandler();
		}
		//マーカ無効
		this._is_active=false;
		return;
	}
	private INyARRgbRaster _last_input_raster;
	private INyARRgb2GsFilter _togs_filter;
	
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
		if (!this._gs_raster.getSize().isEqualSize(i_raster.getSize().w, i_raster.getSize().h)) {
			throw new NyARException();
		}
		// ラスタをGSへ変換する。
		if(this._last_input_raster!=i_raster){
			this._togs_filter=(INyARRgb2GsFilter) i_raster.createInterface(INyARRgb2GsFilter.class);
			this._last_input_raster=i_raster;
		}
		this._togs_filter.convert(this._gs_raster);

		// スクエアコードを探す(第二引数に指定したマーカ、もしくは新しいマーカを探す。)
		this._square_detect.init(this._gs_raster,this._is_active?this._data_current:null);
		this._square_detect.detectMarker(this._gs_raster,this._current_threshold,this._square_detect);

		// 認識状態を更新(マーカを発見したなら、current_dataを渡すかんじ)
		final boolean is_id_found=updateStatus(this._square_detect.square,this._square_detect.marker_data);

		//閾値フィードバック(detectExistMarkerにもあるよ)
		if(is_id_found){
			//マーカがあれば、マーカの周辺閾値を反映
			this._current_threshold=(this._current_threshold+this._square_detect.threshold)/2;
		}else{
			//マーカがなければ、探索+DualPTailで基準輝度検索
			this._histmaker.createHistogram(4,this._hist);
			int th=this._threshold_detect.getThreshold(this._hist);
			this._current_threshold=(this._current_threshold+th)/2;
		}		
		return;
	}

	private NyARDoubleMatrix44 _transmat_result = new NyARDoubleMatrix44();
	private NyARTransMatResultParam _last_result_param = new NyARTransMatResultParam();
	

	/**オブジェクトのステータスを更新し、必要に応じて自己コールバック関数を駆動します。
	 */
	private boolean updateStatus(NyARSquare i_square, INyIdMarkerData i_marker_data)  throws NyARException
	{
		boolean is_id_found=false;
		if (!this._is_active) {// 未認識中
			if (i_marker_data==null) {// 未認識から未認識の遷移
				// なにもしないよーん。
				this._is_active=false;
			} else {// 未認識から認識の遷移
				this._data_current.copyFrom(i_marker_data);
				// イベント生成
				// OnEnter
				this.onEnterHandler(this._data_current);
				// 変換行列を作成
				this._transmat.transMat(i_square, this._offset,this._transmat_result,this._last_result_param);
				// OnUpdate
				this.onUpdateHandler(i_square, this._transmat_result);
				this._lost_delay_count = 0;
				this._is_active=true;
				is_id_found=true;
			}
		} else {// 認識中
			if (i_marker_data==null) {
				// 認識から未認識の遷移
				this._lost_delay_count++;
				if (this._lost_delay < this._lost_delay_count) {
					// OnLeave
					this.onLeaveHandler();
					this._is_active=false;
				}
			} else if(this._data_current.isEqual(i_marker_data)) {
				//同じidの再認識
				if(!this._transmat.transMatContinue(i_square,  this._offset, this._transmat_result, this._last_result_param.last_error, this._transmat_result, this._last_result_param)){
					this._transmat.transMat(i_square, this._offset,this._transmat_result,this._last_result_param);
				}
				// OnUpdate
				this.onUpdateHandler(i_square, this._transmat_result);
				this._lost_delay_count = 0;
				is_id_found=true;
			} else {// 異なるコードの認識→今はサポートしない。
				throw new  NyARException();
			}
		}
		return is_id_found;
	}	
	/**
	 * 自己コールバック関数です。
	 * 継承したクラスで、マーカ発見時の処理を実装してください。
	 * @param i_code
	 * 検出したIDマーカの内容をエンコードしたデータです。
	 * 使用したエンコーダに合せて、キャストしてください。
	 * (例えば、{@link NyIdMarkerDataEncoder_RawBit}をエンコーダに用いた時は、{@link NyIdMarkerDataEncoder_RawBit}にキャストできます。)
	 */
	protected abstract void onEnterHandler(INyIdMarkerData i_code);
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
