package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.types.*;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.INyARRasterFilter_Rgb2Bin;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.dev.tracking.detail.*;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;







public class NyARMarkerTracker
{
	private class SquareDetector extends NyARSquareContourDetector_Rle
	{
		public NyARMarkerTracker tag;
		public SquareDetector(NyARIntSize i_size) throws NyARException
		{
			super(i_size);
		}
	}
	
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquareCB implements NyARSquareContourDetector.IDetectMarkerCallback
	{
		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void onSquareDetect(NyARSquareContourDetector i_sender,NyARIntPoint2d[] i_coord,int i_coorx_num,int[] i_vertex_index) throws NyARException
		{
			NyARMarkerTracker inst=((SquareDetector)i_sender).tag;
			NyAROutlineTrackSrcTable.Item item=inst._outline_table.push(i_coord[i_vertex_index[0]], i_coord[i_vertex_index[1]], i_coord[i_vertex_index[2]], i_coord[i_vertex_index[3]]);
			if(item==null){
				return;
			}
			//矩形トラッキング対象なら、アウトライントラッキングはしない。
			if(inst._detail_tracker.isTrackTarget(item.center))
			{
				inst._detail_table.push(item,i_coord,i_coorx_num,i_vertex_index);
				return;
			}
			
			//トラッキング対象でなければ、追加
			if(!inst._outline_tracker.isTrackTarget(item.center))
			{
				inst._new_outline_table.push(item);
				return;
			}
			inst._track_outline_table.push(item);

		}
	}



	private NyAROutlineTrackSrcTable _outline_table;
	private NyAROutlineTrackSrcRefTable _track_outline_table;
	private NyAROutlineTrackSrcRefTable _new_outline_table;
	private NyAROutlineTracker _outline_tracker;
	private NyARDetailTracker _detail_tracker;
	private NyARDetailTrackSrcTable _detail_table;
	
	
	
	private SquareDetector _square_detect;
	//画処理用
	private NyARBinRaster _bin_raster;
	protected INyARRasterFilter_Rgb2Bin _tobin_filter;
	private DetectSquareCB _detect_cb;

	protected NyARMarkerTracker(NyARParam	i_ref_param,int i_input_raster_type)throws NyARException
	{
		final NyARIntSize scr_size=i_ref_param.getScreenSize();		
		this._square_detect = new SquareDetector(scr_size);
		this._square_detect.tag=this;
		this._tobin_filter=new NyARRasterFilter_ARToolkitThreshold(120,i_input_raster_type);
		this._bin_raster=new NyARBinRaster(scr_size.w,scr_size.h);
		this._detect_cb=new DetectSquareCB();
		//
		this._outline_table=new NyAROutlineTrackSrcTable(10);
		this._track_outline_table=new NyAROutlineTrackSrcRefTable(10);
		this._new_outline_table=new NyAROutlineTrackSrcRefTable(10);
		this._outline_tracker=new NyAROutlineTracker(this,10,10);
		this._detail_table=new NyARDetailTrackSrcTable(10,scr_size,i_ref_param.getDistortionFactor());
		this._detail_tracker=new NyARDetailTracker(this,i_ref_param,10,10);		
		return;
	}
	public boolean _exelusive_lock=false;

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
//		ret[2]=this._detect_cb._estimate_position;
		return ret;
	}
}
