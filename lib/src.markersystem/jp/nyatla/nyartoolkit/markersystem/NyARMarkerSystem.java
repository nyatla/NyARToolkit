package jp.nyatla.nyartoolkit.markersystem;

import java.io.FileInputStream;
import java.io.InputStream;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.*;
import jp.nyatla.nyartoolkit.core.param.NyARFrustum;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.utils.*;





public class NyARMarkerSystem
{
	/**
	 * 定数値。敷居値を自動決定します。 
	 */
	public static int THLESHOLD_AUTO=0xffffffff;
	public static double FRUSTUM_DEFAULT_FAR_CLIP=10000;
	public static double FRUSTUM_DEFAULT_NEAR_CLIP=10;
	
	private static int MASK_IDTYPE=0xfffff000;
	private static int MASK_IDNUM =0x00000fff;
	private static int IDTYPE_ARTK=0x00000000;
	private static int IDTYPE_NYID=0x00001000;

	private RleDetector _rledetect;
	protected NyARParam _ref_param;
	protected NyARFrustum _frustum;
	private int _last_gs_th;
	private int _bin_threshold=THLESHOLD_AUTO;
	
	public NyARMarkerSystem(INyARMarkerSystemConfig i_config) throws NyARException
	{
		this._ref_param=i_config.getNyARParam();
		this._frustum=new NyARFrustum();
		this.initInstance(i_config);
		this.setProjectionMatrixClipping(FRUSTUM_DEFAULT_NEAR_CLIP, FRUSTUM_DEFAULT_FAR_CLIP);
	}
	protected void initInstance(INyARMarkerSystemConfig i_ref_config) throws NyARException
	{
		this._rledetect=new RleDetector(i_ref_config);
		this._hist_th=i_ref_config.createAutoThresholdArgorism();
	}
	/**
	 * 現在のフラスタムを返します。
	 * @return
	 * [readonly]
	 */
	public NyARFrustum getFrustum()
	{
		return this._frustum;
	}
	/**
	 * 射影変換行列の視錐台パラメータを設定します。
	 * @param i_near
	 * @param i_far
	 */
	public void setProjectionMatrixClipping(double i_near,double i_far)
	{
		NyARIntSize s=this._ref_param.getScreenSize();
		this._frustum.setValue(this._ref_param.getPerspectiveProjectionMatrix(),s.w,s.h,i_near,i_far);
	}
	/**
	 * この関数は、1個のIdマーカをシステムに登録します。
	 * インスタンスは、idに一致するNyIdマーカを検出します。
	 * @param i_id
	 * 登録するNyIdマーカのid値
	 * @param i_marker_size
	 * マーカの四方サイズ[mm]
	 * @return
	 * マーカID
	 * @throws NyARException
	 */
	public int addNyIdMarker(long i_id,double i_marker_size) throws NyARException
	{
		MarkerInfoNyId target=new MarkerInfoNyId(i_id,i_id,i_marker_size);
		if(!this._rledetect._idmk_list.add(target)){
			throw new NyARException();
		}
		this._rledetect._tracking_list.add(target);
		return (this._rledetect._idmk_list.size()-1)|IDTYPE_NYID;
	}
	/**
	 * この関数は、1個の範囲を持つidマーカをシステムに登録します。
	 * インスタンスは、i_id_s<=n<=i_id_eの範囲にあるマーカを検出します。
	 * 例えば、1番から5番までのマーカを検出する場合に使います。
	 * @param i_id_s
	 * Id範囲の開始値
	 * @param i_id_e
	 * Id範囲の終了値
	 * @param i_marker_size
	 * マーカの四方サイズ[mm]
	 * @return
	 * マーカID
	 * @throws NyARException
	 */
	public int addNyIdMarker(long i_id_s,long i_id_e,double i_marker_size) throws NyARException
	{
		MarkerInfoNyId target=new MarkerInfoNyId(i_id_s,i_id_e,i_marker_size);
		if(!this._rledetect._idmk_list.add(target)){
			throw new NyARException();
		}
		this._rledetect._tracking_list.add(target);
		return (this._rledetect._idmk_list.size()-1)|IDTYPE_NYID;
	}
	/**
	 * この関数は、ARToolKitスタイルのマーカーを登録します。
	 * @param i_code
	 * 登録するマーカオブジェクト
	 * @param i_patt_edge_percentage
	 * エッジ割合。ARToolkitと同じ場合は25を指定します。
	 * @param i_marker_size
	 * マーカの平方サイズ[mm]
	 * @return
	 * マーカID
	 * @throws NyARException
	 */
	public int addARMarker(NyARCode i_code,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		MarkerInfoARMarker target=new MarkerInfoARMarker(i_code,i_patt_edge_percentage,i_marker_size);
		if(!this._rledetect._armk_list.add(target)){
			throw new NyARException();
		}
		this._rledetect._tracking_list.add(target);
		return (this._rledetect._armk_list.size()-1)| IDTYPE_ARTK;
	}
	/**
	 * この関数は、ARToolKitスタイルのマーカーをストリームから読みだして、登録します。
	 * @param i_stream
	 * マーカデータを読み出すストリーム
	 * @param i_patt_edge_percentage
	 * エッジ割合。ARToolkitと同じ場合は25を指定します。
	 * @param i_marker_size
	 * マーカの平方サイズ[mm]
	 * @return
	 * マーカID
	 * @throws NyARException
	 */
	public int addARMarker(InputStream i_stream,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		c.loadARPatt(i_stream);
		return this.addARMarker(c, i_patt_edge_percentage, i_marker_size);
	}
	/**
	 * この関数は、ARToolKitスタイルのマーカーをファイルから読みだして、登録します。
	 * @param i_stream
	 * マーカデータを読み出すストリーム
	 * @param i_patt_edge_percentage
	 * エッジ割合。ARToolkitと同じ場合は25を指定します。
	 * @param i_marker_size
	 * マーカの平方サイズ[mm]
	 * @return
	 * マーカID
	 * @throws NyARException
	 */
	public int addARMarker(String i_file_name,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		try{
			c.loadARPatt(new FileInputStream(i_file_name));
		}catch(Exception e){
			throw new NyARException(e);
		}
		return this.addARMarker(c,i_patt_edge_percentage, i_marker_size);
	}
	
	/**
	 * この関数は、 マーカIDのマーカが検出されているかを返します。
	 * @param i_id
	 * マーカID
	 * @return
	 * マーカを認識していればtrue
	 */
	public boolean isExistMarker(int i_id)
	{
		return this.getLife(i_id)>0;
	}
	/**
	 * この関数は、ARマーカの最近の一致度を返します。
	 * {@link #isExistMarker(int)}がtrueの時にだけ使用できます。
	 * 値は初期一致度であり、トラッキング中は変動しません。
	 * @param i_id
	 * マーカID
	 * @return
	 * 0&lt;n&lt;1の一致度。
	 */
	public double getConfidence(int i_id) throws NyARException
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &MASK_IDNUM).cf;
		}
		//Idマーカ？
		throw new NyARException();
	}
	/**
	 * この関数は、NyIdマーカのID値を返します。
	 * 範囲指定で検出したマーカの、実際のIDを得る場合などに遣います。
	 * @param i_id
	 * マーカID
	 * @return
	 * nyIdのID値
	 * @throws NyARException
	 */
	public long getNyId(int i_id) throws NyARException
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_NYID){
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id &MASK_IDNUM).nyid;
		}
		//ARマーカ？
		throw new NyARException();
	}
	/**
	 * この関数は、現在の２値化敷居値を返します。
	 * 自動敷居値を選択している場合は、直近の敷居値を返します。
	 * @return
	 * 敷居値(0-255)
	 */
	public int getCurrentThreshold()
	{
		return this._last_gs_th;
	}
	/**
	 * この関数は、マーカIDのライフ値を返します。
	 * ライフ値については、{@link TMarkerData#life}を参照してください。
	 * @param i_id
	 * マーカID
	 * @return
	 */
	public long getLife(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id & MASK_IDNUM).life;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id & MASK_IDNUM).life;
		}
	}
	/**
	 * この関数は、マーカIDから消失カウンタの値を返します。
	 * @param i_id
	 * マーカID
	 * @return
	 * 消失カウンタの値
	 */
	public long getLostCount(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id & MASK_IDNUM).lost_count;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id & MASK_IDNUM).lost_count;
		}
	}
	/**
	 * この関数は、スクリーン座標点をマーカ平面の点に変換します。
	 * @param i_id
	 * @param i_x
	 * 変換元のスクリーン座標
	 * @param i_y
	 * 変換元のスクリーン座標
	 * @param i_out
	 * 結果を格納するオブジェクト
	 * @return
	 * 結果を格納したi_outに設定したオブジェクト
	 */
	public NyARDoublePoint3d getMarkerPlanePos(int i_id,int i_x,int i_y,NyARDoublePoint3d i_out)
	{
		this._frustum.unProjectOnMatrix(i_x, i_y,this.getMarkerMatrix(i_id),i_out);
		return i_out;
	}
	private NyARDoublePoint3d _wk_3dpos=new NyARDoublePoint3d();
	/**
	 * この関数は、idで示されるマーカ座標系の点をスクリーン座標へ変換します。
	 * @param i_id
	 * マーカidを指定します。
	 * @param i_x
	 * マーカ座標系のX座標
	 * @param i_y
	 * マーカ座標系のY座標
	 * @param i_z
	 * マーカ座標系のZ座標
	 * @param i_out
	 * 結果を格納するオブジェクト
	 * @return
	 * 結果を格納したi_outに設定したオブジェクト
	 */
	public NyARDoublePoint2d getScreenPos(int i_id,double i_x,double i_y,double i_z,NyARDoublePoint2d i_out)
	{
		NyARDoublePoint3d _wk_3dpos=this._wk_3dpos;
		this.getMarkerMatrix(i_id).transform3d(i_x, i_y, i_z,_wk_3dpos);
		this._frustum.project(_wk_3dpos,i_out);
		return i_out;
	}	
	private NyARDoublePoint3d[] __pos3d=NyARDoublePoint3d.createArray(4);
	private NyARDoublePoint2d[] __pos2d=NyARDoublePoint2d.createArray(4);

	
	/**
	 * この関数は、マーカ平面上の任意の４点で囲まれる領域から、画像を射影変換して返します。
	 * 画像は、最後に入力した入力画像から取得します。
	 * @param i_id
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト。通常は{@link #update(NyARSensor)}関数に入力したものと同じものを指定します。
	 * @param i_x1
	 * @param i_y1
	 * @param i_x2
	 * @param i_y2
	 * @param i_x3
	 * @param i_y3
	 * @param i_x4
	 * @param i_y4
	 * @param i_raster
	 * 出力先のオブジェクト
	 * @return
	 * 結果を格納したi_rasterオブジェクト
	 * @throws NyARException
	 */
	public INyARRgbRaster getMarkerPlaneImage(
		int i_id,
		NyARSensor i_sensor,
	    int i_x1,int i_y1,
	    int i_x2,int i_y2,
	    int i_x3,int i_y3,
	    int i_x4,int i_y4,
	    INyARRgbRaster i_raster) throws NyARException
	{
		NyARDoublePoint3d[] pos  = this.__pos3d;
		NyARDoublePoint2d[] pos2 = this.__pos2d;
		NyARDoubleMatrix44 tmat=this.getMarkerMatrix(i_id);
		tmat.transform3d(i_x1, i_y1,0,	pos[1]);
		tmat.transform3d(i_x2, i_y2,0,	pos[0]);
		tmat.transform3d(i_x3, i_y3,0,	pos[3]);
		tmat.transform3d(i_x4, i_y4,0,	pos[2]);
		for(int i=3;i>=0;i--){
			this._frustum.project(pos[i],pos2[i]);
		}
		return i_sensor.getPerspectiveImage(pos2[0].x, pos2[0].y,pos2[1].x, pos2[1].y,pos2[2].x, pos2[2].y,pos2[3].x, pos2[3].y,i_raster);
	}
	/**
	 * この関数は、マーカ平面上の任意の４点で囲まれる領域から、画像を射影変換して返します。
	 * 画像は、最後に入力した入力画像から取得します。
	 * @param i_id
	 * マーカid
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト。通常は{@link #update(NyARSensor)}関数に入力したものと同じものを指定します。
	 * @param i_l
	 * @param i_t
	 * @param i_w
	 * @param i_h
	 * @param i_raster
	 * 出力先のオブジェクト
	 * @return
	 * 結果を格納したi_rasterオブジェクト
	 * @throws NyARException
	 */
	public INyARRgbRaster getMarkerPlaneImage(
		int i_id,
		NyARSensor i_sensor,
	    int i_l,int i_t,
	    int i_w,int i_h,
	    INyARRgbRaster i_raster) throws NyARException
    {
		return this.getMarkerPlaneImage(i_id,i_sensor,i_l+i_w-1,i_t+i_h-1,i_l,i_t+i_h-1,i_l,i_t,i_l+i_w-1,i_t,i_raster);
    }
	/**
	 * この関数は、マーカの姿勢変換行列を返します。
	 * @param i_id
	 * マーカid
	 * @return
	 * [readonly]
	 */
	public NyARDoubleMatrix44 getMarkerMatrix(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &MASK_IDNUM).tmat;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id &MASK_IDNUM).tmat;
		}
	}
	/**
	 * この関数は、マーカ頂点の二次元座標を返します。
	 * @param i_id
	 * マーカID
	 * @return
	 * [readonly]
	 */
	public NyARIntPoint2d[] getMarkerVertex2D(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &MASK_IDNUM).tl_vertex;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id &MASK_IDNUM).tl_vertex;
		}
	}
	/**
	 * この関数は、2値化敷居値を設定します。
	 * @param i_th
	 * 2値化敷居値。{@link NyARMarkerSystem#THLESHOLD_AUTO}を指定すると、自動調整になります。
	 */
	public void setBinThreshold(int i_th)
	{
		this._bin_threshold=i_th;
	}
	/**
	 * ARマーカの一致敷居値を設定します。
	 * @param i_val
	 * 敷居値。0.0&lt;n&lt;1.0の値を指定すること。
	 */
	public void setConfidenceThreshold(double i_val)
	{
		this._rledetect._armk_list.setConficenceTh(i_val);
	}
	
	private long _time_stamp=-1;
	private INyARHistogramAnalyzer_Threshold _hist_th;
	/**
	 * 状況を更新する。
	 * @throws NyARException 
	 */
	public void update(NyARSensor i_sensor) throws NyARException
	{
		long time_stamp=i_sensor.getTimeStamp();
		//センサのタイムスタンプが変化していなければ何もしない。
		if(this._time_stamp==time_stamp){
			return;
		}
		int th=this._bin_threshold==THLESHOLD_AUTO?this._hist_th.getThreshold(i_sensor.getGsHistogram()):this._bin_threshold;

		//解析器にかけてマーカを抽出。
		this._rledetect.detectMarker(i_sensor, time_stamp, th);
		//タイムスタンプを更新
		this._time_stamp=time_stamp;
		this._last_gs_th=th;
	}

}









/**
 * {@link MultiMarker}向けの矩形検出器です。
 */
class RleDetector extends NyARSquareContourDetector_Rle
{
	private final static int INITIAL_MARKER_STACK_SIZE=10;
	private NyARCoord2Linear _coordline;		
	
	private SquareStack _sq_stack;
	public int lost_th=5;	
	public TrackingList _tracking_list;
	public ARMarkerList _armk_list;
	public NyIdList _idmk_list;
	public INyARTransMat _transmat;
	private INyARPerspectiveCopy _ref_input_rfb;
	private INyARGrayscaleRaster _ref_input_gs;
	public RleDetector(INyARMarkerSystemConfig i_config) throws NyARException
	{
		super( i_config.getNyARParam().getScreenSize());
		this._coordline=new NyARCoord2Linear(i_config.getNyARParam().getScreenSize(),i_config.getNyARParam().getDistortionFactor());
		this._armk_list=new ARMarkerList();
		this._idmk_list=new NyIdList();
		this._tracking_list=new TrackingList();
		this._transmat=i_config.createTransmatAlgorism();
		//同時に判定待ちにできる矩形の数
		this._sq_stack=new SquareStack(INITIAL_MARKER_STACK_SIZE);
	}

	protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
	{
		//とりあえずSquareスタックを予約
		SquareStack.Item sq_tmp=this._sq_stack.prePush();
		//観測座標点の記録
		for(int i2=0;i2<4;i2++){
			sq_tmp.ob_vertex[i2].setValue(i_coord.items[i_vertex_index[i2]]);
		}
		//頂点分布を計算
		sq_tmp.vertex_area.setAreaRect(sq_tmp.ob_vertex,4);
		//頂点座標の中心を計算
		sq_tmp.center2d.setCenterPos(sq_tmp.ob_vertex,4);
		//矩形面積
		sq_tmp.rect_area=sq_tmp.vertex_area.w*sq_tmp.vertex_area.h;

		boolean is_target_marker=false;
		for(;;){
			//トラッキング対象か確認する。
			if(this._tracking_list.update(sq_tmp)){
				//トラッキング対象ならブレーク
				is_target_marker=true;
				break;
			}
			//nyIdマーカの特定(IDマーカの特定はここで完結する。)
			if(this._idmk_list.size()>0){
				if(this._idmk_list.update(this._ref_input_gs,sq_tmp)){
					is_target_marker=true;
					break;//idマーカを特定
				}
			}
			//ARマーカの特定
			if(this._armk_list.size()>0){
				if(this._armk_list.update(this._ref_input_rfb,sq_tmp)){
					is_target_marker=true;
					break;
				}
			}
			break;
		}
		//この矩形が検出対象なら、矩形情報を精密に再計算
		if(is_target_marker){
			//矩形は検出対象にマークされている。
			for(int i2=0;i2<4;i2++){
				this._coordline.coord2Line(i_vertex_index[i2],i_vertex_index[(i2+1)%4],i_coord,sq_tmp.line[i2]);
			}
			for (int i2 = 0; i2 < 4; i2++) {
				//直線同士の交点計算
				if(!sq_tmp.line[i2].crossPos(sq_tmp.line[(i2 + 3) % 4],sq_tmp.sqvertex[i2])){
					throw new NyARException();//まずない。ありえない。
				}
			}
		}else{
			//この矩形は検出対象にマークされなかったので、解除
			this._sq_stack.pop();
		}
	}
	
	public void detectMarker(NyARSensor i_sensor,long i_time_stamp,int i_th) throws NyARException
	{
		//準備(ミスカウンタを+1する。)
		for(int i=this._idmk_list.size()-1;i>=0;i--){
			MarkerInfoNyId target=this._idmk_list.get(i);
			if(target.lost_count<Integer.MAX_VALUE){
				target.lost_count++;
			}
		}
		this._sq_stack.clear();//矩形情報の保持スタック初期化
		this._tracking_list.prepare();
		this._idmk_list.prepare();
		this._armk_list.prepare();
		//検出処理
		this._ref_input_rfb=i_sensor.getPerspectiveCopy();
		this._ref_input_gs=i_sensor.getGsImage();
		super.detectMarker(this._ref_input_gs,i_th);

		//検出結果の反映処理
		this._tracking_list.finish();
		this._armk_list.finish();
		this._idmk_list.finish();
		//期限切れチェック
		for(int i=this._tracking_list.size()-1;i>=0;i--){
			TMarkerData item=this._tracking_list.get(i);
			if(item.lost_count>this.lost_th){
				item.life=0;//活性off
			}
		}
		//各ターゲットの更新
		for(int i=this._armk_list.size()-1;i>=0;i--){
			MarkerInfoARMarker target=this._armk_list.get(i);
			if(target.lost_count==0){
				target.time_stamp=i_time_stamp;
				this._transmat.transMatContinue(target.sq,target.marker_offset,target.tmat,target.tmat);
			}
		}
		for(int i=this._idmk_list.size()-1;i>=0;i--){
			MarkerInfoNyId target=this._idmk_list.get(i);
			if(target.lost_count==0){
				target.time_stamp=i_time_stamp;
				this._transmat.transMatContinue(target.sq,target.marker_offset,target.tmat,target.tmat);
			}
		}
	}
}











