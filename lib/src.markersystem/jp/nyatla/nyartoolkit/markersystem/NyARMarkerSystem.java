/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.markersystem;

import java.io.FileInputStream;
import java.io.InputStream;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.coord2liner.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.histogram.algo.INyARHistogramAnalyzer_Threshold;
import jp.nyatla.nyartoolkit.core.marker.artk.NyARCode;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.perspectivecopy.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.core.rasterdriver.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.rasterdriver.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.utils.*;





/**
 * このクラスは、マーカベースARの制御クラスです。
 * 複数のARマーカとNyIDの検出情報の管理機能、撮影画像の取得機能を提供します。
 * このクラスは、ARToolKit固有の座標系を出力します。他の座標系を出力するときには、継承クラスで変換してください。
 * レンダリングシステム毎にクラスを派生させて使います。Javaの場合には、OpenGL用の{@link NyARGlMarkerSystem}クラスがあります。
 */
public class NyARMarkerSystem extends NyARSingleCameraSystem
{
	/**　定数値。自動敷居値を示す値です。　*/
	public final static int THLESHOLD_AUTO=0x7fffffff;
	/** マーカ消失時の、消失までのﾃﾞｨﾚｲ(フレーム数)の初期値です。*/
	public final static int LOST_DELAY_DEFAULT=5;
	
	
	private static final int MASK_IDTYPE=0x7ffff000;
	private static final int MASK_IDNUM =0x00000fff;
	private static final int IDTYPE_ARTK=0x00000000;
	private static final int IDTYPE_NYID=0x00001000;
	private static final int IDTYPE_PSID=0x00002000;

	final protected INyARMarkerSystemSquareDetect _sqdetect;
	final protected INyARHistogramAnalyzer_Threshold _hist_th;
	private int _last_gs_th;
	private int _bin_threshold=THLESHOLD_AUTO;
	final private TrackingList _tracking_list;
	final private ARMarkerList _armk_list;
	final private NyIdList _idmk_list;
	final private ARPlayCardList _psmk_list;
	private int lost_th=5;
	final private INyARTransMat _transmat;
	final private static int INITIAL_MARKER_STACK_SIZE=10;


	/**
	 * コンストラクタです。{@link INyARMarkerSystemConfig}を元に、インスタンスを生成します。
	 * @param i_config
	 * 初期化済の{@link MarkerSystem}を指定します。
	 * @throws NyARRuntimeException
	 */
	public NyARMarkerSystem(INyARMarkerSystemConfig i_config)
	{
		super(i_config.getNyARSingleCameraView());
		NyARIntSize s=i_config.getScreenSize();
		this._sqdetect=new SquareDetect(s.w,s.h);
		this._hist_th=i_config.createAutoThresholdArgorism();
		this._armk_list=new ARMarkerList();
		this._idmk_list=new NyIdList();
		this._psmk_list=new ARPlayCardList();
		this._tracking_list=new TrackingList();
		this._transmat=i_config.createTransmatAlgorism();
		this._on_sq_handler=new OnSquareDetect(this._view.getARParam(),this._armk_list,this._idmk_list,this._psmk_list,this._tracking_list,INITIAL_MARKER_STACK_SIZE);
		
	}

	

	/**
	 * この関数は、1個のIdマーカをシステムに登録して、検出可能にします。
	 * 関数はマーカに対応したID値（ハンドル値）を返します。
	 * @param i_id
	 * 登録するNyIdマーカのid値
	 * @param i_marker_size
	 * マーカの四方サイズ[mm]
	 * @return
	 * マーカID（ハンドル）値。この値はIDの値ではなく、マーカのハンドル値です。
	 * @throws NyARRuntimeException
	 */
	public int addNyIdMarker(long i_id,double i_marker_size)
	{
		return this.addNyIdMarker(i_id,i_id, i_marker_size);
	}
	/**
	 * この関数は、1個の範囲を持つidマーカをシステムに登録して、検出可能にします。
	 * インスタンスは、i_id_s<=n<=i_id_eの範囲にあるマーカを検出します。
	 * 例えば、1番から5番までのマーカを検出する場合に使います。
	 * 関数はマーカに対応したID値（ハンドル値）を返します。
	 * @param i_id_s
	 * Id範囲の開始値
	 * @param i_id_e
	 * Id範囲の終了値
	 * @param i_marker_size
	 * マーカの四方サイズ[mm]
	 * @return
	 * マーカID（ハンドル）値。この値はNyIDの値ではなく、マーカのハンドル値です。
	 * @throws NyARRuntimeException
	 */
	public int addNyIdMarker(long i_id_s,long i_id_e,double i_marker_size)
	{
		NyIdList.Item target=new NyIdList.Item(i_id_s,i_id_e,i_marker_size);
		if(!this._idmk_list.add(target)){
			throw new NyARRuntimeException();
		}
		this._tracking_list.add(target);
		this._on_sq_handler.setMaxDetectMarkerCapacity(this._tracking_list.size());
		return (this._idmk_list.size()-1)|IDTYPE_NYID;
	}
	/**
	 * この関数は、1個の範囲を持つARプレイマーカをシステムに登録して、検出可能にします。
	 * インスタンスは、i_id_s<=n<=i_id_eの範囲にあるマーカを検出します。
	 * 例えば、1番から5番までのマーカを検出する場合に使います。
	 * 関数はマーカに対応したID値（ハンドル値）を返します。
	 * @param i_id_s
	 * Id範囲の開始値 (1<=n<=6)
	 * @param i_id_e
	 * Id範囲の終了値 (1<=n<=6)
	 * @param i_marker_size
	 * マーカの四方サイズ[mm]
	 * @return
	 * マーカID（ハンドル）値。この値はIDの値ではなく、マーカのハンドル値です。
	 * @throws NyARRuntimeException
	 */
	public int addPsARPlayCard(int i_id_s,int i_id_e,double i_marker_size)
	{
		assert(i_id_s>0 && i_id_s<=6);
		assert(i_id_e>0 && i_id_e<=6);
		ARPlayCardList.Item target=new ARPlayCardList.Item(i_id_s,i_id_e,i_marker_size);
		if(!this._psmk_list.add(target)){
			throw new NyARRuntimeException();
		}
		this._tracking_list.add(target);
		this._on_sq_handler.setMaxDetectMarkerCapacity(this._tracking_list.size());
		return (this._psmk_list.size()-1)|IDTYPE_PSID;
	}
	/**
	 * この関数は、1個のARプレイマーカをシステムに登録して、検出可能にします。
	 * 関数はマーカに対応したID値（ハンドル値）を返します。
	 * @param i_id
	 * PSARプレイマーカのID。1-6までの数値です。
	 * @param i_marker_size
	 * マーカの四方サイズ[mm]
	 * @return
	 * マーカID（ハンドル）値。この値はIDの値ではなく、マーカのハンドル値です。
	 * @throws NyARRuntimeException
	 */
	public int addPsARPlayCard(int i_id,double i_marker_size)
	{
		return this.addPsARPlayCard(i_id,i_id,i_marker_size);
	}	
	/**
	 * この関数は、ARToolKitスタイルのマーカーを登録します。
	 * @param i_code
	 * 登録するマーカパターンオブジェクト
	 * @param i_patt_edge_percentage
	 * エッジ割合。ARToolkitと同じ場合は25を指定します。
	 * @param i_marker_size
	 * マーカの平方サイズ[mm]
	 * @return
	 * マーカID（ハンドル）値。
	 * @throws NyARRuntimeException
	 */
	public int addARMarker(NyARCode i_code,int i_patt_edge_percentage,double i_marker_size)
	{
		ARMarkerList.Item target=new ARMarkerList.Item(i_code,i_patt_edge_percentage,i_marker_size);
		if(!this._armk_list.add(target)){
			throw new NyARRuntimeException();
		}
		this._tracking_list.add(target);
		this._on_sq_handler.setMaxDetectMarkerCapacity(this._tracking_list.size());
		return (this._armk_list.size()-1)| IDTYPE_ARTK;
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
	 * マーカID（ハンドル）値。
	 * @throws NyARRuntimeException
	 */
	public int addARMarker(InputStream i_stream,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size)
	{
		NyARCode c=NyARCode.loadFromARPattFile(i_stream,i_patt_resolution,i_patt_resolution);
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
	 * マーカID（ハンドル）値。
	 * @throws NyARRuntimeException
	 */
	public int addARMarker(String i_file_name,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size)
	{
		try{
			NyARCode c=NyARCode.loadFromARPattFile(new FileInputStream(i_file_name),i_patt_resolution,i_patt_resolution);
			return this.addARMarker(c,i_patt_edge_percentage, i_marker_size);
		}catch(Exception e){
			throw new NyARRuntimeException(e);
		}
	}
	/**
	 * この関数は、画像からARマーカパターンを生成して、登録します。
	 * ビットマップ等の画像から生成したパターンは、撮影画像から生成したパターンファイルと比較して、撮影画像の色調変化に弱くなります。
	 * 注意してください。
	 * @param i_raster
	 * マーカ画像を格納したラスタオブジェクト
	 * @param i_patt_resolution
	 * マーカの解像度
	 * @param i_patt_edge_percentage
	 * マーカのエッジ領域のサイズ。マーカパターンは、i_rasterからエッジ領域を除いたパターンから生成します。
	 * ARToolKitスタイルの画像を用いる場合は、25を指定します。
	 * @param i_marker_size
	 * マーカの平方サイズ[mm]
	 * @return
	 * マーカID（ハンドル）値。
	 * @throws NyARRuntimeException
	 */
	public int addARMarker(INyARRgbRaster i_raster,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size)
	{
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		NyARIntSize s=i_raster.getSize();
		//ラスタからマーカパターンを切り出す。
		INyARPerspectiveCopy pc=(INyARPerspectiveCopy)i_raster.createInterface(INyARPerspectiveCopy.class);
		INyARRgbRaster tr=NyARRgbRaster.createInstance(i_patt_resolution,i_patt_resolution);
		pc.copyPatt(0,0,s.w,0,s.w,s.h,0,s.h,i_patt_edge_percentage, i_patt_edge_percentage,4, tr);
		//切り出したパターンをセット
		c.setRaster(tr);
		return this.addARMarker(c,i_patt_edge_percentage,i_marker_size);
	}
	
	
	/**
	 * この関数は、 マーカIDに対応するマーカが検出されているかを返します。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @return
	 * マーカを検出していればtrueを返します。
	 * @throws NyARRuntimeException 
	 */
	public boolean isExist(int i_id)
	{
		return this.getLife(i_id)>0;
	}
	/**
	 * この関数は、ARマーカの最近の一致度を返します。
	 * {@link #isExist(int)}がtrueの時にだけ使用できます。
	 * 値は初期の一致度であり、トラッキング中は変動しません。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @return
	 * 0&lt;n&lt;1の一致度。
	 */
	public double getConfidence(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._armk_list.get(i_id &MASK_IDNUM).cf;
		}
		//Idマーカ？
		throw new NyARRuntimeException();
	}
	/**
	 * この関数は、NyIdマーカのID値を返します。
	 * 範囲指定で登録したNyIdマーカから、実際のIDを得るために使います。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @return
	 * 現在のNyIdの値
	 * @throws NyARRuntimeException
	 */
	public long getNyId(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_NYID){
			//Idマーカ
			return this._idmk_list.get(i_id &MASK_IDNUM).nyid;
		}
		//ARマーカ？
		throw new NyARRuntimeException();
	}
	/**
	 * この関数は、現在の２値化敷居値を返します。
	 * 自動敷居値を選択している場合は、直近に検出した敷居値を返します。
	 * @return
	 * 敷居値(0-255)
	 */
	public int getCurrentThreshold()
	{
		return this._last_gs_th;
	}
	/**
	 * この関数は、マーカのライフ値を返します。
	 * ライフ値は、フレーム毎に加算される寿命値です。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @return
	 * ライフ値
	 */
	public long getLife(int i_id)
	{
		switch(i_id & MASK_IDTYPE)
		{
		case IDTYPE_ARTK:
			return this._armk_list.get(i_id & MASK_IDNUM).life;
		case IDTYPE_NYID:
			return this._idmk_list.get(i_id & MASK_IDNUM).life;
		case IDTYPE_PSID:
			return this._psmk_list.get(i_id & MASK_IDNUM).life;
		default:
			throw new NyARRuntimeException();
		}
	}
	/**
	 * この関数は、マーカの消失カウンタの値を返します。
	 * 消失カウンタの値は、マーカを一時的にロストした時に加算される値です。再度検出した時に0にリセットされます。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @return
	 * 消失カウンタの値
	 * @throws NyARRuntimeException 
	 */
	public long getLostCount(int i_id)
	{
		switch(i_id & MASK_IDTYPE)
		{
		case IDTYPE_ARTK:
			return this._armk_list.get(i_id & MASK_IDNUM).lost_count;
		case IDTYPE_NYID:
			return this._idmk_list.get(i_id & MASK_IDNUM).lost_count;
		case IDTYPE_PSID:
			return this._psmk_list.get(i_id & MASK_IDNUM).lost_count;
		default:
			throw new NyARRuntimeException();

		}
	}
	/**
	 * この関数は、スクリーン座標点をマーカ平面の点に変換します。
	 * {@link #isExist(int)}がtrueの時にだけ使用できます。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @param i_x
	 * 変換元のスクリーン座標
	 * @param i_y
	 * 変換元のスクリーン座標
	 * @param i_out
	 * 結果を格納するオブジェクト
	 * @return
	 * 結果を格納したi_outに設定したオブジェクト
	 */
	public NyARDoublePoint3d getPlanePos(int i_id,int i_x,int i_y,NyARDoublePoint3d i_out)
	{
		this.getFrustum().unProjectOnMatrix(i_x, i_y,this.getTransformMatrix(i_id),i_out);
		return i_out;
	}
	final private NyARDoublePoint3d _wk_3dpos=new NyARDoublePoint3d();
	/**
	 * この関数は、マーカ座標系の点をスクリーン座標へ変換します。
	 * {@link #isExist(int)}がtrueの時にだけ使用できます。
	 * @param i_id
	 * マーカID（ハンドル）値。
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
		this.getTransformMatrix(i_id).transform3d(i_x, i_y, i_z,_wk_3dpos);
		this.getFrustum().project(_wk_3dpos,i_out);
		return i_out;
	}	
	private NyARDoublePoint3d[] __pos3d=NyARDoublePoint3d.createArray(4);
	private NyARDoublePoint2d[] __pos2d=NyARDoublePoint2d.createArray(4);

	
	/**
	 * この関数は、マーカ平面上の任意の４点で囲まれる領域から、画像を射影変換して返します。
	 * {@link #isExist(int)}がtrueの時にだけ使用できます。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト。通常は{@link #update(NyARSensor)}関数に入力したものと同じものを指定します。
	 * @param i_x1
	 * 頂点1[mm]
	 * @param i_y1
	 * 頂点1[mm]
	 * @param i_x2
	 * 頂点2[mm]
	 * @param i_y2
	 * 頂点2[mm]
	 * @param i_x3
	 * 頂点3[mm]
	 * @param i_y3
	 * 頂点3[mm]
	 * @param i_x4
	 * 頂点4[mm]
	 * @param i_y4
	 * 頂点4[mm]
	 * @param i_raster
	 * 取得した画像を格納するオブジェクト
	 * @return
	 * 結果を格納したi_rasterオブジェクト
	 * @throws NyARRuntimeException
	 */
	public INyARRgbRaster getPlaneImage(
		int i_id,
		NyARSensor i_sensor,
		double i_x1,double i_y1,
		double i_x2,double i_y2,
		double i_x3,double i_y3,
		double i_x4,double i_y4,
	    INyARRgbRaster i_raster)
	{
		NyARDoublePoint3d[] pos  = this.__pos3d;
		NyARDoublePoint2d[] pos2 = this.__pos2d;
		NyARDoubleMatrix44 tmat=this.getTransformMatrix(i_id);
		tmat.transform3d(i_x1, i_y1,0,	pos[1]);
		tmat.transform3d(i_x2, i_y2,0,	pos[0]);
		tmat.transform3d(i_x3, i_y3,0,	pos[3]);
		tmat.transform3d(i_x4, i_y4,0,	pos[2]);
		for(int i=3;i>=0;i--){
			this.getFrustum().project(pos[i],pos2[i]);
		}
		return i_sensor.getPerspectiveImage(pos2[0].x, pos2[0].y,pos2[1].x, pos2[1].y,pos2[2].x, pos2[2].y,pos2[3].x, pos2[3].y,i_raster);
	}
	/**
	 * この関数は、マーカ平面上の任意の矩形で囲まれる領域から、画像を射影変換して返します。
	 * {@link #isExist(int)}がtrueの時にだけ使用できます。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト。通常は{@link #update(NyARSensor)}関数に入力したものと同じものを指定します。
	 * @param i_l
	 * 矩形の左上点です。
	 * @param i_t
	 * 矩形の左上点です。
	 * @param i_w
	 * 矩形の幅です。
	 * @param i_h
	 * 矩形の幅です。
	 * @param i_raster
	 * 出力先のオブジェクト
	 * @return
	 * 結果を格納したi_rasterオブジェクト
	 * @throws NyARRuntimeException
	 */
	public INyARRgbRaster getPlaneImage(int i_id,NyARSensor i_sensor,double i_l,double i_t,double i_w,double i_h,INyARRgbRaster i_raster)
    {
		return this.getPlaneImage(i_id,i_sensor,i_l+i_w-1,i_t+i_h-1,i_l,i_t+i_h-1,i_l,i_t,i_l+i_w-1,i_t,i_raster);
    }
	/**
	 * この関数は、マーカの姿勢変換行列を返します。
	 * マーカID（ハンドル）値。
	 * @return
	 * [readonly]
	 * 姿勢行列を格納したオブジェクト。座標系は、ARToolKit座標系です。
	 */
	public NyARDoubleMatrix44 getTransformMatrix(int i_id)
	{
		switch(i_id & MASK_IDTYPE)
		{
		case IDTYPE_ARTK:
			return this._armk_list.get(i_id &MASK_IDNUM).tmat;
		case IDTYPE_NYID:
			return this._idmk_list.get(i_id &MASK_IDNUM).tmat;
		case IDTYPE_PSID:
			return this._psmk_list.get(i_id &MASK_IDNUM).tmat;
		default:
			throw new NyARRuntimeException();
		}		
	}
	/**
	 * この関数は、マーカの4頂点の、スクリーン上の二次元座標を返します。
	 * @param i_id
	 * マーカID（ハンドル）値。
	 * @return
	 * [readonly]
	 */
	public NyARIntPoint2d[] getVertex2D(int i_id)
	{
		switch(i_id & MASK_IDTYPE)
		{
		case IDTYPE_ARTK:
			return this._armk_list.get(i_id &MASK_IDNUM).tl_vertex;
		case IDTYPE_NYID:
			return this._idmk_list.get(i_id &MASK_IDNUM).tl_vertex;
		case IDTYPE_PSID:
			return this._psmk_list.get(i_id &MASK_IDNUM).tl_vertex;
		default:
			throw new NyARRuntimeException();
		}		
	}
	/**
	 * {@link #getPlaneImage}をしようとして下さい。
	 * @deprecated
	 */
	public INyARRgbRaster getMarkerPlaneImage(int i_id,NyARSensor i_sensor,double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,INyARRgbRaster i_raster)
	{
		return this.getPlaneImage(i_id, i_sensor, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4, i_raster);
	}
	/**
	 * {@link #getPlameImage}を使用してください。
	 * @deprecated
	 */
	final public INyARRgbRaster getMarkerPlaneImage(int i_id,NyARSensor i_sensor,double i_l,double i_t,double i_w,double i_h,INyARRgbRaster i_raster)
	{
		return this.getPlaneImage(i_id, i_sensor, i_l, i_t, i_w, i_h, i_raster);
	}
	
	
	/**
	 * {@link #getTransformMatrix}を使用してください。
	 * @deprecated
	 */
	final public NyARDoubleMatrix44 getMarkerMatrix(int i_id)
	{
		return this.getTransformMatrix(i_id);
	}
	/**
	 * {@link #getVertex2D}を使用してください。
	 * @deprecated
	 */
	final public NyARIntPoint2d[] getMarkerVertex2D(int i_id)
	{
		return this.getVertex2D(i_id);
	}
	/**
	 * {@link #getPlanePos}を使用してください。
	 * @deprecated
	 */	
	public NyARDoublePoint3d getMarkerPlanePos(int i_id,int i_x,int i_y,NyARDoublePoint3d i_out)
	{
		return this.getPlanePos(i_id, i_x, i_y, i_out);
	}
	/**
	 * {@link #isExist}を使用してください。
	 * @deprecated
	 */
	public boolean isExistMarker(int i_id)
	{
		return this.isExist(i_id);
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
	 * この関数は、ARマーカ検出の、敷居値を設定します。
	 * ここで設定した値以上の一致度のマーカを検出します。
	 * @param i_val
	 * 敷居値。0.0&lt;n&lt;1.0の値を指定すること。
	 */
	public void setConfidenceThreshold(double i_val)
	{
		this._armk_list.setConficenceTh(i_val);
	}
	/**
	 * この関数は、消失時のディレイ値を指定します。
	 * デフォルト値は、{@link NyARMarkerSystem#LOST_DELAY_DEFAULT}です。
	 * MarkerSystemは、ここで指定した回数を超えて連続でマーカを検出できないと、マーカが消失したと判定します。
	 * @param i_delay
	 * 回数を指定します。
	 */
	public void setLostDelay(int i_delay)
	{
		this.lost_th=i_delay;
	}
	private long _time_stamp=-1;
	private OnSquareDetect _on_sq_handler;
	/**
	 * この関数は、入力したセンサ入力値から、インスタンスの状態を更新します。
	 * 関数は、センサオブジェクトから画像を取得して、マーカ検出、一致判定、トラッキング処理を実行します。
	 * @param i_sensor
	 * {@link MarkerSystem}に入力する画像を含むセンサオブジェクト。
	 * @throws NyARRuntimeException 
	 */
	public void update(NyARSensor i_sensor)
	{
		long time_stamp=i_sensor.getTimeStamp();
		//センサのタイムスタンプが変化していなければ何もしない。
		if(this._time_stamp==time_stamp){
			return;
		}
		int th=this._bin_threshold==THLESHOLD_AUTO?this._hist_th.getThreshold(i_sensor.getGsHistogram()):this._bin_threshold;
		//解析
		this._tracking_list.prepare();
		this._idmk_list.prepare();
		this._armk_list.prepare();
		this._psmk_list.prepare();
		//検出
		this._on_sq_handler.prepare(i_sensor.getPerspectiveCopy(),i_sensor.getGsImage(),th);
		this._sqdetect.detectMarkerCb(i_sensor,th,this._on_sq_handler);

		//検出結果の反映処理
		this._tracking_list.finish();
		this._armk_list.finish();
		this._idmk_list.finish();
		this._psmk_list.finish();
		//期限切れチェック
		for(int i=this._tracking_list.size()-1;i>=0;i--){
			TMarkerData item=this._tracking_list.get(i);
			if(item.lost_count>this.lost_th){
				//連続で検出できなかった場合
				item.life=0;//活性off
			}else if(item.sq!=null){
				//直前のsqを検出できた場合
//				System.out.println("[Tracking]");
				if(!this._transmat.transMatContinue(item.sq,item.marker_offset,item.tmat,item.last_param.last_error,item.tmat,item.last_param))
				{
					if(!this._transmat.transMat(item.sq,item.marker_offset,item.tmat,item.last_param)){
						item.life=0;//活性off
					}
				}
			}
		}
		//各ターゲットの更新
		for(int i=this._armk_list.size()-1;i>=0;i--){
			TMarkerData target=this._armk_list.get(i);
			if(target.lost_count==0){
				target.time_stamp=time_stamp;
				//lifeが1(開始時検出のときのみ)
				if(target.life!=1){
					continue;
				}
//				System.out.println("[ARMarker]");
				this._transmat.transMat(target.sq,target.marker_offset,target.tmat,target.last_param);
			}
		}
		for(int i=this._idmk_list.size()-1;i>=0;i--){
			TMarkerData target=this._idmk_list.get(i);
			if(target.lost_count==0){
				target.time_stamp=time_stamp;
				//lifeが1(開始時検出のときのみ)
				if(target.life!=1){
					continue;
				}
//				System.out.println("[NyIdARMarker]");
				this._transmat.transMat(target.sq,target.marker_offset,target.tmat,target.last_param);
			}
		}
		for(int i=this._psmk_list.size()-1;i>=0;i--){
			TMarkerData target=this._psmk_list.get(i);
			if(target.lost_count==0){
				target.time_stamp=time_stamp;
				//lifeが1(開始時検出のときのみ)
				if(target.life!=1){
					continue;
				}
//				System.out.println("[PSARMarker]");
				this._transmat.transMat(target.sq,target.marker_offset,target.tmat,target.last_param);
			}
		}
		//解析/
		//タイムスタンプを更新
		this._time_stamp=time_stamp;
		this._last_gs_th=th;
	}
}

/**
 * コールバック関数の隠蔽用クラス。
 * このクラスは、{@link NyARMarkerSystem}からプライベートに使います。
 */
class OnSquareDetect implements NyARSquareContourDetector.CbHandler
{
	private TrackingList _ref_tracking_list;
	private ARMarkerList _ref_armk_list;
	private NyIdList _ref_idmk_list;
	private ARPlayCardList _ref_psmk_list;
	public SquareStack _sq_stack;
	public INyARPerspectiveCopy _ref_input_rfb;
	public INyARGrayscaleRaster _ref_input_gs;
	public int _ref_th;
	
	private NyARCoord2Linear _coordline;		
	public OnSquareDetect(
		NyARParam i_params,
		ARMarkerList i_armk_list,NyIdList i_idmk_list,ARPlayCardList i_psmk_list,
		TrackingList i_tracking_list,int i_initial_stack_size)
	{
		this._coordline=new NyARCoord2Linear(i_params.getScreenSize(),i_params.getDistortionFactor());
		this._ref_armk_list=i_armk_list;
		this._ref_idmk_list=i_idmk_list;
		this._ref_psmk_list=i_psmk_list;
		this._ref_tracking_list=i_tracking_list;
		//同時に判定待ちにできる矩形の数
		this._sq_stack=new SquareStack(i_initial_stack_size);
	}
	/**
	 * 同時に検出するマーカの最大数を設定します。
	 * 関数は、少なくともi_max_number_of_marker以上のマーカを同時に検出できるようにインスタンスを設定します。
	 * @throws NyARRuntimeException 
	 */
	public void setMaxDetectMarkerCapacity(int i_max_number_of_marker)
	{
		//prepare enough stack size.
		if(this._sq_stack.getArraySize()<i_max_number_of_marker){
			this._sq_stack=new SquareStack(i_max_number_of_marker+5);
		}
		return;
	}
	/**
	 * {@link #detectMarkerCallback}コール前に1度だけ呼び出してください。
	 * @param i_max_detect_marker
	 * @param i_pcopy
	 * @param i_gs
	 * @param th
	 * @throws NyARRuntimeException
	 */
	public void prepare(INyARPerspectiveCopy i_pcopy, INyARGrayscaleRaster i_gs, int th)
	{
		this._ref_input_rfb=i_pcopy;
		this._ref_input_gs=i_gs;
		this._ref_th=th;
		// initialize square stack
		this._sq_stack.clear();		
	}
	public void detectMarkerCallback(NyARIntCoordinates i_coord,int[] i_vertex_index)
	{
		//とりあえずSquareスタックを予約
		SquareStack.Item sq_tmp=this._sq_stack.prePush();
		//確保できない(1つのdetectorが複数の候補を得る場合(同じARマーカが多くある場合など)に発生することがある。)
		if(sq_tmp==null){
			return;
		}
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
			if(this._ref_tracking_list.update(sq_tmp)){
				//トラッキング対象ならブレーク
				is_target_marker=true;
				break;
			}
			//@todo 複数マーカ時に、トラッキング済のarmarkerを探索対象外に出来ない？
			
			//nyIdマーカの特定(IDマーカの特定はここで完結する。)
			if(this._ref_idmk_list.size()>0){
				if(this._ref_idmk_list.update(this._ref_input_gs,sq_tmp)){
					is_target_marker=true;
					break;//idマーカを特定
				}
			}
			//PSARマーカの特定(IDマーカの特定はここで完結する。)
			if(this._ref_psmk_list.size()>0){
				if(this._ref_psmk_list.update(this._ref_input_gs,sq_tmp)){
					is_target_marker=true;
					break;//idマーカを特定
				}
			}			
			//ARマーカの特定
			if(this._ref_armk_list.size()>0){
				//敷居値により1個のマーカに対して複数の候補が見つかることもある。
				if(this._ref_armk_list.update(this._ref_input_rfb,sq_tmp)){
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
					throw new NyARRuntimeException();//まずない。ありえない。
				}
			}
		}else{
			//この矩形は検出対象にマークされなかったので、解除
			this._sq_stack.pop();
		}
	}	
}




class SquareDetect implements INyARMarkerSystemSquareDetect
{
	private NyARSquareContourDetector_Rle _sd;
	public SquareDetect(int i_width,int i_height)
	{
		this._sd=new NyARSquareContourDetector_Rle(new NyARIntSize(i_width,i_height));
	}
	public void detectMarkerCb(NyARSensor i_sensor,int i_th,NyARSquareContourDetector.CbHandler i_handler)
	{
		this._sd.detectMarker(i_sensor.getGsImage(), i_th,i_handler);
	}
}

















