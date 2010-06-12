package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.labeling.NyARDetailLabelingTrackSrcTable;
import jp.nyatla.nyartoolkit.dev.tracking.detail.labeling.NyARDetailLabelingTracker;







public class NyARMarkerLabelingTracker extends NyARMarkerTracker
{

	private NyARDetailLabelingTrackSrcTable _labaling_data_source;
	
	protected NyARMarkerLabelingTracker(NyARParam	i_ref_param,int i_input_raster_type)throws NyARException
	{
		super(i_ref_param,i_input_raster_type);
		this._detail_tracker=new NyARDetailLabelingTracker(this,i_ref_param,10,10);
		this._labaling_data_source=new NyARDetailLabelingTrackSrcTable(10,i_ref_param.getScreenSize(),i_ref_param.getDistortionFactor());
		//
		return;
	}

	/**
	 * i_imageにマーカー検出処理を実行し、結果を記録します。
	 * 
	 * @param i_raster
	 * マーカーを検出するイメージを指定します。イメージサイズは、カメラパラメータ
	 * と一致していなければなりません。
	 * @return マーカーが検出できたかを真偽値で返します。
	 * @throws NyARException
	 */
	public void tracking(INyARRgbRaster i_raster,INyARMarkerTrackerListener i_listener) throws NyARException
	{
		//サイズチェック
		if(!this._bin_raster.getSize().isEqualSize(i_raster.getSize())){
			throw new NyARException();
		}

		//ラスタを２値イメージに変換する.
		this._tobin_filter.doFilter(i_raster,this._bin_raster);

		//コールバックハンドラの準備
		
		//矩形を探す(戻り値はコールバック関数で受け取る。)
		this._square_detect.detectMarkerCB(this._bin_raster,_detect_cb);

		
		//アウトライントラッキングを実行
		this._outline_tracker.trackTarget(this._track_outline_table,i_listener);

		//データソース内容を更新
		for(int i=this._detail_tracker.getNumberOfSquare()-1;i>=0;i--){
			this._labaling_data_source.update(this._detail_tracker.getSquares()[i], i_raster);
		}
		
		//ディティールトラッキングを実行
		this._detail_tracker.trackTarget(this._detail_table,i_listener);
		

		//アウトライントラッキングからのアップグレードを試行
		this._outline_tracker.charangeUpgrade(this._detail_tracker);
		
		//新規項目をトラッキング対象に追加
		for(int i=this._new_outline_table.getLength()-1;i>=0;i--){
			this._outline_tracker.addTrackTarget(this._new_outline_table,i_listener);
		}
		
		//後片付け
		this._outline_table.clear();
		this._track_outline_table.clear();
		this._new_outline_table.clear();
		this._detail_table.clear();

		return;
	}
	public Object[] _probe()
	{
		Object[] ret=new Object[10];
		ret[0]=this._outline_tracker;
		ret[1]=this._detail_tracker;
		ret[2]=this._labaling_data_source;
		return ret;
	}
}
