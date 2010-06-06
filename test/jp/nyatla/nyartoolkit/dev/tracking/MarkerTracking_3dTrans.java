package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.INyARRasterFilter_Rgb2Bin;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;

















public class MarkerTracking_3dTrans
{
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquareCB implements NyARSquareContourDetector.IDetectMarkerCallback
	{
		public DetectSquareCB(
			INyARColorPatt i_inst_patt,
			NyARCode i_ref_code,
			NyARParam i_param,
			double i_marker_width) throws NyARException
		{
			this._outline_table=new NyAROutlineTrackSrcTable(10);
			this._track_outline_table=new NyAROutlineTrackSrcRefTable(10);
			this._new_outline_table=new NyAROutlineTrackSrcRefTable(10);
			this._tracker_items=new NyAROutlineTracker(10,10);
			return;
		}
		private NyAROutlineTrackSrcTable _outline_table;
		private NyAROutlineTrackSrcRefTable _track_outline_table;
		private NyAROutlineTrackSrcRefTable _new_outline_table;
		public NyAROutlineTracker _tracker_items;

		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void onSquareDetect(NyARSquareContourDetector i_sender,NyARIntPoint2d[] i_coord,int i_coor_num,int[] i_vertex_index) throws NyARException
		{
			NyAROutlineTrackSrcTable.Item item=this._outline_table.push(i_coord[i_vertex_index[0]], i_coord[i_vertex_index[1]], i_coord[i_vertex_index[2]], i_coord[i_vertex_index[3]]);
			if(item==null){
				return;
			}
			//矩形トラッキング対象なら、アウトライントラッキングはしない。
			
			
			//トラッキング対象でなければ、追加
			if(!this._tracker_items.isTrackTarget(item.center))
			{
				this._new_outline_table.push(item);
				return;
			}
			this._track_outline_table.push(item);

		}

		/**
		 * コールバックハンドラの初期化
		 * @param i_raster
		 */
		
		public final void init()
		{
			this._outline_table.clear();
			this._track_outline_table.clear();
			this._new_outline_table.clear();

			//現在位置のテーブルから、探索予定の一覧を作成。
			//継続認識候補のマーカリストをクリア
			
//			this._found_stack.clear();
//			this.table_operator.estimateMarkerPosition(this.table,this._estimate_position);
			return;
		}
		/**
		 * コールバックハンドラの終期化
		 */
		public final void finish()
		{
			//アウトライントラッキングを実行
			this._tracker_items.trackTarget(this._track_outline_table);
			//３次元トラッキングの実行
			
			//新規項目をトラッキング対象に追加
			for(int i=this._new_outline_table.getLength()-1;i>=0;i--){
				this._tracker_items.addTrackTarget(this._new_outline_table.getItem(i));
			}
/*			
			
			
			int number_of_found=this._found_stack.getLength();
			MarkerPositionStack.Item[] found_item=this._found_stack.getArray();
			EstimatePositionStack.Item[] est_item=this._estimate_position.getArray();

			//this._existstack.getLength()-1
			//予想マーカ位置に最適な認識済矩形をマップする。
			for(int i=this._estimate_position.getLength()-1;i>=0;i--){
				for(int i2=number_of_found-1;i2>=0;i2--){
//					current.getLength()					
				}
			}
			
		//	}*/
		}
		
		
		
		
	}
	//矩形トラッキングにアップグレードする。
	public void upgradeToSquareTracking()
	{
		
	}
	private NyARSquareContourDetector _square_detect;
	protected INyARTransMat _transmat;
	//画処理用
	private NyARBinRaster _bin_raster;
	protected INyARRasterFilter_Rgb2Bin _tobin_filter;
	private DetectSquareCB _detect_cb;
	
	public IntRectStack _next_marker;
	public TransMat2MarkerRect _estimator;


	protected MarkerTracking_3dTrans()
	{
		return;
	}
	protected void initInstance(
		INyARColorPatt i_patt_inst,
		NyARSquareContourDetector i_sqdetect_inst,
		INyARTransMat i_transmat_inst,
		INyARRasterFilter_Rgb2Bin i_filter,
		NyARParam	i_ref_param,
		NyARCode	i_ref_code,
		double		i_marker_width) throws NyARException
	{
		final NyARIntSize scr_size=i_ref_param.getScreenSize();		
		// 解析オブジェクトを作る
		this._square_detect = i_sqdetect_inst;
		this._transmat = i_transmat_inst;
		this._tobin_filter=i_filter;
		// 比較コードを保存
		//２値画像バッファを作る
		this._bin_raster=new NyARBinRaster(scr_size.w,scr_size.h);
		//_detect_cb
		this._detect_cb=new DetectSquareCB(i_patt_inst,i_ref_code,i_ref_param,i_marker_width);
		this._next_marker=new IntRectStack(10);
		this._estimator=new TransMat2MarkerRect(i_ref_param);


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
	public int detectMarkerLite(INyARRgbRaster i_raster) throws NyARException
	{
		//サイズチェック
		if(!this._bin_raster.getSize().isEqualSize(i_raster.getSize())){
			throw new NyARException();
		}

		//ラスタを２値イメージに変換する.
		this._tobin_filter.doFilter(i_raster,this._bin_raster);

		//コールバックハンドラの準備
		this._detect_cb.init();
		//矩形を探す(戻り値はコールバック関数で受け取る。)
		this._square_detect.detectMarkerCB(this._bin_raster,_detect_cb);
		this._detect_cb.finish();
		//時間を進める。
//		this._detect_cb.table_operator.updateTick(this._detect_cb.table);
		
		//矩形位置の予想
//		this._next_marker.clear();
//		this._estimator.convert(this._detect_cb.table.selectAllItems(), this._next_marker);
		return -1;
	}
	public Object[] _probe()
	{
		Object[] ret=new Object[10];
		ret[0]=this._detect_cb._tracker_items;
		ret[1]=this._next_marker;
//		ret[2]=this._detect_cb._estimate_position;
		return ret;
	}
}
