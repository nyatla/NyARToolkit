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
package jp.nyatla.nyartoolkit.processor;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.*;
import jp.nyatla.nyartoolkit.core2.rasteranalyzer.threshold.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;

public abstract class SingleNyIdMarkerProcesser
{
	/**
	 * オーナーが自由に使えるタグ変数です。
	 */
	public Object tag;

	/**
	 * ロスト遅延の管理
	 */
	private int _lost_delay_count = 0;
	private int _lost_delay = 5;

	private NyARSquareDetector_Rle _square_detect;
	protected INyARTransMat _transmat;
	private double _marker_width=100;

	private NyARSquareStack _square_list = new NyARSquareStack(100);
	private INyIdMarkerDataEncoder _encoder;
	private boolean _is_active;
	private INyIdMarkerData _data_temp;
	private INyIdMarkerData _data_current;

	private int _current_threshold=110;
	// [AR]検出結果の保存用
	private NyARBinRaster _bin_raster;

	private NyARRasterFilter_ARToolkitThreshold _tobin_filter;

	private NyIdMarkerPickup _id_pickup = new NyIdMarkerPickup();


	protected SingleNyIdMarkerProcesser()
	{
		return;
	}
	protected void initInstance(NyARParam i_param,INyIdMarkerDataEncoder i_encoder,int i_raster_format) throws NyARException
	{
		NyARIntSize scr_size = i_param.getScreenSize();
		// 解析オブジェクトを作る
		this._square_detect = new NyARSquareDetector_Rle(i_param.getDistortionFactor(), scr_size);
		this._transmat = new NyARTransMat(i_param);
		this._encoder=i_encoder;

		// ２値画像バッファを作る
		this._bin_raster = new NyARBinRaster(scr_size.w, scr_size.h);
		//ワーク用のデータオブジェクトを２個作る
		this._is_active=false;
		this._data_temp=i_encoder.createDataInstance();
		this._data_current=i_encoder.createDataInstance();
		this._tobin_filter = new NyARRasterFilter_ARToolkitThreshold(110,i_raster_format);
		this._threshold_detect=new NyARRasterThresholdAnalyzer_SlidePTile(15,i_raster_format,4);
		return;
		
	}

	public void setMarkerWidth(int i_width)
	{
		this._marker_width=i_width;
		return;
	}

	public void reset(boolean i_is_force)
	{
		if (this._data_current!=null && i_is_force == false) {
			// 強制書き換えでなければイベントコール
			this.onLeaveHandler();
		}
		// カレントマーカをリセット
		this._data_current = null;
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

		NyARSquareStack square_stack = this._square_list;
		// スクエアコードを探す
		this._square_detect.detectMarker(this._bin_raster, square_stack);
		// 認識処理
		if (!this._is_active) {
			// マーカ未認識→新規認識
			detectNewMarker(i_raster, square_stack);
		} else {
			// マーカ認識依頼→継続認識
			detectExistMarker(i_raster, square_stack);
		}
		return;
	}

	
	private final NyIdMarkerPattern _marker_data=new NyIdMarkerPattern();
	private final NyIdMarkerParam _marker_param=new NyIdMarkerParam();
	private NyARRasterThresholdAnalyzer_SlidePTile _threshold_detect;
	
	/**新規マーカ検索 現在認識中のマーカがないものとして、最も認識しやすいマーカを１個認識します。
	 */
	private void detectNewMarker(INyARRgbRaster i_raster, NyARSquareStack i_stack) throws NyARException
	{
		NyIdMarkerParam param=this._marker_param;
		NyIdMarkerPattern patt_data  =this._marker_data;
		int number_of_square = i_stack.getLength();
		NyARSquare current_square=null;
		INyIdMarkerData marker_id=null;
		for (int i = 0; i < number_of_square; i++) {
			// 評価基準になるパターンをイメージから切り出す
			current_square=i_stack.getItem(i);
			if (!this._id_pickup.pickFromRaster(i_raster,current_square, patt_data, param)) {
				continue;
			}
			//エンコード
			if(!this._encoder.encode(patt_data,this._data_temp)){
				continue;
			}
			//認識率が一番高いもの（占有面積が一番大きいもの）を選択する(省略)
			//id認識が成功したら終了
			marker_id=this._data_temp;
			break;
		}
		
		// 認識状態を更新
		final boolean is_id_found=updateStatus(current_square,marker_id, param);

		//閾値フィードバック(detectExistMarkerにもあるよ)
		if(is_id_found){
			//マーカがあれば、マーカの周辺閾値を反映
			this._current_threshold=(this._current_threshold+param.threshold)/2;
		}else{
			//マーカがなければ、探索+DualPTailで基準輝度検索
			this._threshold_detect.analyzeRaster(i_raster);
			this._current_threshold=(this._current_threshold+this._threshold_detect.getThreshold())/2;
		}
		return;
	}

	/**マーカの継続認識 現在認識中のマーカを優先して認識します。 
	 * （注）この機能はたぶん今後いろいろ発展するからNewと混ぜないこと。
	 */
	private void detectExistMarker(INyARRgbRaster i_raster, NyARSquareStack i_stack) throws NyARException
	{
		NyIdMarkerParam param=this._marker_param;
		NyIdMarkerPattern patt_data  =this._marker_data;
		int number_of_square = i_stack.getLength();
		NyARSquare current_square=null;
		INyIdMarkerData marker_id=null;
		for (int i = 0; i < number_of_square; i++){
			//idマーカを認識
			current_square=i_stack.getItem(i);
			if (!this._id_pickup.pickFromRaster(i_raster, current_square, patt_data, param)) {
				continue;
			}
			if(!this._encoder.encode(patt_data,this._data_temp)){
				continue;
			}
			//現在認識中のidか確認
			if(!this._data_current.isEqual((this._data_temp))){
				continue;
			}
			//現在認識中のものであれば、終了
			marker_id=this._data_temp;
			break;
		}
		// 認識状態を更新
		final boolean is_id_found=updateStatus(current_square,marker_id,param);

		//閾値フィードバック(detectExistMarkerにもあるよ)
		if(is_id_found){
			//マーカがあれば、マーカの周辺閾値を反映
			this._current_threshold=(this._current_threshold+param.threshold)/2;
		}else{
			//マーカがなければ、探索+DualPTailで基準輝度検索
			this._threshold_detect.analyzeRaster(i_raster);
			this._current_threshold=(this._current_threshold+this._threshold_detect.getThreshold())/2;
		}
		return;
	}

	private NyARTransMatResult __NyARSquare_result = new NyARTransMatResult();

	/**オブジェクトのステータスを更新し、必要に応じてハンドル関数を駆動します。
	 */
	private boolean updateStatus(NyARSquare i_square, INyIdMarkerData i_marker_data,NyIdMarkerParam i_param)  throws NyARException
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
				this._transmat.transMat(i_square,i_param.direction, this._marker_width, result);
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
				this._transmat.transMat(i_square, i_param.direction, this._marker_width, result);
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
