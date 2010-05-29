/* 
 * Capture Test NyARToolkitサンプルプログラム
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

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.raster.threshold.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;

public abstract class SingleNyIdMarkerProcesser
{
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquareCB implements NyARSquareContourDetector.IDetectMarkerCallback
	{
		//公開プロパティ
		public final NyARSquare square=new NyARSquare();
		public INyIdMarkerData marker_data;
		public int threshold;

		
		//参照
		private INyARRgbRaster _ref_raster;
		//所有インスタンス
		private INyIdMarkerData _current_data;
		private final NyIdMarkerPickup _id_pickup = new NyIdMarkerPickup();
		private NyARCoord2Linear _coordline;
		private INyIdMarkerDataEncoder _encoder;

		
		private INyIdMarkerData _data_temp;
		private INyIdMarkerData _prev_data;
		
		public DetectSquareCB(NyARParam i_param,INyIdMarkerDataEncoder i_encoder)
		{
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
			this._data_temp=i_encoder.createDataInstance();
			this._current_data=i_encoder.createDataInstance();
			this._encoder=i_encoder;
			return;
		}
		private NyARIntPoint2d[] __tmp_vertex=NyARIntPoint2d.createArray(4);
		/**
		 * Initialize call back handler.
		 */
		public void init(INyARRgbRaster i_raster,INyIdMarkerData i_prev_data)
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
		public void onSquareDetect(NyARSquareContourDetector i_sender,int[] i_coordx,int[] i_coordy,int i_coor_num,int[] i_vertex_index) throws NyARException
		{
			//既に発見済なら終了
			if(this.marker_data!=null){
				return;
			}
			//輪郭座標から頂点リストに変換
			NyARIntPoint2d[] vertex=this.__tmp_vertex;
			vertex[0].x=i_coordx[i_vertex_index[0]];
			vertex[0].y=i_coordy[i_vertex_index[0]];
			vertex[1].x=i_coordx[i_vertex_index[1]];
			vertex[1].y=i_coordy[i_vertex_index[1]];
			vertex[2].x=i_coordx[i_vertex_index[2]];
			vertex[2].y=i_coordy[i_vertex_index[2]];
			vertex[3].x=i_coordx[i_vertex_index[3]];
			vertex[3].y=i_coordy[i_vertex_index[3]];
		
			NyIdMarkerParam param=this._marker_param;
			NyIdMarkerPattern patt_data  =this._marker_data;			
			// 評価基準になるパターンをイメージから切り出す
			if (!this._id_pickup.pickFromRaster(this._ref_raster,vertex, patt_data, param)){
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
				this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coordx,i_coordy,i_coor_num,sq.line[i]);
			}
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
					throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
				}
			}
			this.threshold=param.threshold;
			this.marker_data=this._current_data;//みつかった。
		}
	}	
	/**
	 * オーナーが自由に使えるタグ変数です。
	 */
	public Object tag;

	/**
	 * ロスト遅延の管理
	 */
	private int _lost_delay_count = 0;
	private int _lost_delay = 5;

	private NyARSquareContourDetector_Rle _square_detect;
	protected INyARTransMat _transmat;
	private NyARRectOffset _offset; 
	private boolean _is_active;
	private int _current_threshold=110;
	// [AR]検出結果の保存用
	private NyARBinRaster _bin_raster;
	private NyARRasterFilter_ARToolkitThreshold _tobin_filter;
	private DetectSquareCB _callback;
	private INyIdMarkerData _data_current;


	protected SingleNyIdMarkerProcesser()
	{
		return;
	}
	private boolean _initialized=false;
	protected void initInstance(NyARParam i_param,INyIdMarkerDataEncoder i_encoder,double i_marker_width,int i_raster_format) throws NyARException
	{
		//初期化済？
		assert(this._initialized==false);
		
		NyARIntSize scr_size = i_param.getScreenSize();
		// 解析オブジェクトを作る
		this._square_detect = new NyARSquareContourDetector_Rle(scr_size);
		this._transmat = new NyARTransMat(i_param);
		this._callback=new DetectSquareCB(i_param,i_encoder);

		// ２値画像バッファを作る
		this._bin_raster = new NyARBinRaster(scr_size.w, scr_size.h);
		//ワーク用のデータオブジェクトを２個作る
		this._data_current=i_encoder.createDataInstance();
		this._tobin_filter =new NyARRasterFilter_ARToolkitThreshold(110,i_raster_format);
		this._threshold_detect=new NyARRasterThresholdAnalyzer_SlidePTile(15,i_raster_format,4);
		this._initialized=true;
		this._is_active=false;
		this._offset=new NyARRectOffset();
		this._offset.setSquare(i_marker_width);
		return;
		
	}

	public void setMarkerWidth(int i_width)
	{
		this._offset.setSquare(i_width);
		return;
	}

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

	public void detectMarker(INyARRgbRaster i_raster) throws NyARException
	{
		// サイズチェック
		if (!this._bin_raster.getSize().isEqualSize(i_raster.getSize().w, i_raster.getSize().h)) {
			throw new NyARException();
		}
		// ラスタを２値イメージに変換する.
		this._tobin_filter.setThreshold(this._current_threshold);
		this._tobin_filter.doFilter(i_raster, this._bin_raster);

		// スクエアコードを探す(第二引数に指定したマーカ、もしくは新しいマーカを探す。)
		this._callback.init(i_raster,this._is_active?this._data_current:null);
		this._square_detect.detectMarkerCB(this._bin_raster, this._callback);

		// 認識状態を更新(マーカを発見したなら、current_dataを渡すかんじ)
		final boolean is_id_found=updateStatus(this._callback.square,this._callback.marker_data);

		//閾値フィードバック(detectExistMarkerにもあるよ)
		if(is_id_found){
			//マーカがあれば、マーカの周辺閾値を反映
			this._current_threshold=(this._current_threshold+this._callback.threshold)/2;
		}else{
			//マーカがなければ、探索+DualPTailで基準輝度検索
			int th=this._threshold_detect.analyzeRaster(i_raster);
			this._current_threshold=(this._current_threshold+th)/2;
		}		
		return;
	}

	
	private NyARRasterThresholdAnalyzer_SlidePTile _threshold_detect;
	private NyARTransMatResult __NyARSquare_result = new NyARTransMatResult();

	/**オブジェクトのステータスを更新し、必要に応じてハンドル関数を駆動します。
	 */
	private boolean updateStatus(NyARSquare i_square, INyIdMarkerData i_marker_data)  throws NyARException
	{
		boolean is_id_found=false;
		NyARTransMatResult result = this.__NyARSquare_result;
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
				this._transmat.transMat(i_square, this._offset, result);
				// OnUpdate
				this.onUpdateHandler(i_square, result);
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
				this._transmat.transMatContinue(i_square, this._offset, result);
				// OnUpdate
				this.onUpdateHandler(i_square, result);
				this._lost_delay_count = 0;
				is_id_found=true;
			} else {// 異なるコードの認識→今はサポートしない。
				throw new  NyARException();
			}
		}
		return is_id_found;
	}	
	//通知ハンドラ
	protected abstract void onEnterHandler(INyIdMarkerData i_code);
	protected abstract void onLeaveHandler();
	protected abstract void onUpdateHandler(NyARSquare i_square, NyARTransMatResult result);
}
