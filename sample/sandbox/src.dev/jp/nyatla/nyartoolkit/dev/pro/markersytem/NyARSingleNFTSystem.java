package jp.nyatla.nyartoolkit.dev.pro.markersytem;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.markersystem.NyARSingleCameraSystem;
import jp.nyatla.nyartoolkit.pro.core.kpm.NyARKpmDataSet;
import jp.nyatla.nyartoolkit.pro.core.kpm.NyARSingleKpm;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARSurfaceDataSet;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARSurfaceTracker;
import jp.nyatla.nyartoolkit.pro.core.transmat.NyARNftTransMatUtils;

/**
 * こ�?�クラスは�?1つの特徴点セ�?ト�?�三次�?座標を推定します�??
 *
 */
public class NyARSingleNFTSystem extends NyARSingleCameraSystem
{
	public final static int MAX_RANSAC_RESULT = 200;
	public final static int MAX_SURFACE_TRACKING=20;
	private NyARSingleKpm _kpm;
	private NyARSurfaceTracker _stracker;
	private NyARNftTransMatUtils _transmat_utils;
	private int _tick=0;
	private static int[] _area_table={jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QLT,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QLB,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QRT,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QRB,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QCE};
	private NyARDoubleMatrix44 _current_transmat=new NyARDoubleMatrix44();
	private boolean _is_found=false;
	private NyARKpmDataSet _rds;
	private NyARNftDataSet _ss;
	private NyARNFTSystemConfig _config;
	
	public NyARSingleNFTSystem(NyARNFTSystemConfig i_config) throws NyARRuntimeException
	{
		super(i_config.getNyARParam());
		this._config=i_config;
		this._stracker=new NyARSurfaceTracker(i_config.getNyARParam(),MAX_SURFACE_TRACKING);
		this._transmat_utils=new NyARNftTransMatUtils(i_config.getNyARParam(),MAX_RANSAC_RESULT);
	}
	/**
	 * こ�?�関数は、{@link InputStream}から特徴セ�?トを読み出して、インスタンスにセ�?トします�??
	 * @param i_iset
	 * サーフェイス画像ファイルを読み出す{@link InputStream}
	 * @param i_fset
	 * サーフェイス特徴ファイルを読み出す{@link InputStream}
	 * @param i_kpm_fset
	 * キーポイントファイルを読み出す{@link InputStream}
	 * @throws NyARRuntimeException
	 */
	public void setARNftDataset(InputStream i_iset,InputStream i_fset,InputStream i_kpm_fset) throws NyARRuntimeException
	{
		this._ss=NyARNftDataSet.loadFromSurfaceFiles(i_iset,i_fset);
		this._rds=NyARKpmDataSet.loadFromFset2(i_kpm_fset);
		this._kpm=new NyARSingleKpm(this._config.getNyARParam(),this._rds);
	}
	public void update(NyARSensor i_sensor) throws NyARRuntimeException
	{
		if(this._is_found){
			if(updateTracking(i_sensor)){
				return;
			}
			if(this.updateKpm(i_sensor)){
				this._stracker.resetTrackingLog();
				return;
			}
			this._is_found=false;
			return;
		}else{
			if(this.updateKpm(i_sensor)){
				this._stracker.resetTrackingLog();
				this._is_found=true;
				return;
			}
			return;
		}
	}
	public boolean isExist()
	{
		return this._is_found;
	}
	/**
	 * [readonly]
	 * 現在の姿勢変換行�?�を返します�??{@link #isExist()}がtrueの時�?�み使用できます�??
	 * @param i_mat
	 * @return
	 */
	public NyARDoubleMatrix44 getMarkerMatrix() throws NyARRuntimeException
	{
		if(!this._is_found){
			throw new NyARRuntimeException();
		}
		return this._current_transmat;
	}
	private NyARDoublePoint2d[] __pos2d = NyARDoublePoint2d.createArray(MAX_SURFACE_TRACKING);
	private NyARDoublePoint3d[] __pos3d = NyARDoublePoint3d.createArray(MAX_SURFACE_TRACKING);
	private NyARTransMatResultParam _tresult=new NyARTransMatResultParam();
	/**
	 * SurfaceTrackingによる検�?�
	 * @param i_sensor
	 * @return
	 * @throws NyARRuntimeException
	 */
	public boolean updateTracking(NyARSensor i_sensor) throws NyARRuntimeException
	{
		int points=this._stracker.tracking(i_sensor.getGsImage(),this._ss,this._current_transmat,this.__pos2d,this.__pos3d,MAX_SURFACE_TRACKING);
		if(points<4){
			return false;
		}
		return this._transmat_utils.surfaceTrackingTransmat(this._current_transmat,this.__pos2d,this.__pos3d, points, this._current_transmat,this._tresult);
		
	}
	/**
	 * KPMによる初期検�?�
	 * @param i_sensor
	 * @return
	 * @throws NyARRuntimeException
	 */
	public boolean updateKpm(NyARSensor i_sensor) throws NyARRuntimeException
	{
		NyARSurfAnnMatch.ResultPtr match_items=new NyARSurfAnnMatch.ResultPtr(MAX_RANSAC_RESULT);
		this._kpm.updateMatching(i_sensor.getGsImage());
		if(this._kpm.getRansacMatchPoints(jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_ALL, match_items)){
			if(_transmat_utils.kpmTransmat(match_items, this._current_transmat)){
				return true;
			}
		}
		this._tick=(this._tick+1)%0x0fffffff;
		//1/5の確�?くら�?で調査
		if(this._kpm.getRansacMatchPoints(_area_table[this._tick%5], match_items)){
			if(_transmat_utils.kpmTransmat(match_items, this._current_transmat)){
				return true;
			}
		}
		return false;
	}
	/**
	 * こ�?�関数は、スクリーン座標点を�?��?�カ平面の点に変換します�??
	 * {@link #isExist()}がtrueの時に�?け使用できます�??
	 * @param i_x
	 * 変換�?のスクリーン座�?
	 * @param i_y
	 * 変換�?のスクリーン座�?
	 * @param i_out
	 * 結果を�?�納するオブジェク�?
	 * @return
	 * 結果を�?�納したi_outに設定したオブジェク�?
	 */
	public NyARDoublePoint3d getMarkerPlanePos(int i_x,int i_y,NyARDoublePoint3d i_out) throws NyARRuntimeException
	{
		this._frustum.unProjectOnMatrix(i_x, i_y,this.getMarkerMatrix(),i_out);
		return i_out;
	}
	private NyARDoublePoint3d _wk_3dpos=new NyARDoublePoint3d();
	/**
	 * こ�?�関数は、�?��?�カ座標系の点をスクリーン座標へ変換します�??
	 * {@link #isExist(int)}がtrueの時に�?け使用できます�??
	 * @param i_x
	 * マ�?�カ座標系のX座�?
	 * @param i_y
	 * マ�?�カ座標系のY座�?
	 * @param i_z
	 * マ�?�カ座標系のZ座�?
	 * @param i_out
	 * 結果を�?�納するオブジェク�?
	 * @return
	 * 結果を�?�納したi_outに設定したオブジェク�?
	 */
	public NyARDoublePoint2d getScreenPos(double i_x,double i_y,double i_z,NyARDoublePoint2d i_out) throws NyARRuntimeException
	{
		NyARDoublePoint3d _wk_3dpos=this._wk_3dpos;
		this.getMarkerMatrix().transform3d(i_x, i_y, i_z,_wk_3dpos);
		this._frustum.project(_wk_3dpos,i_out);
		return i_out;
	}	

	
	/**
	 * こ�?�関数は、�?��?�カ平面上�?�任意�?�?��点で囲まれる領域から、画像を�?影変換して返します�??
	 * {@link #isExist()}がtrueの時に�?け使用できます�??
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト�?��?�常は{@link #update(NyARSensor)}関数に入力したものと同じも�?�を指定します�??
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
	 * 取得した画像を格納するオブジェク�?
	 * @return
	 * 結果を�?�納したi_rasterオブジェク�?
	 * @throws NyARRuntimeException
	 */
	public INyARRgbRaster getMarkerPlaneImage(
		NyARSensor i_sensor,
		double i_x1,double i_y1,
		double i_x2,double i_y2,
		double i_x3,double i_y3,
		double i_x4,double i_y4,
	    INyARRgbRaster i_raster) throws NyARRuntimeException
	{
		NyARDoublePoint3d[] pos  = this.__pos3d;
		NyARDoublePoint2d[] pos2 = this.__pos2d;
		NyARDoubleMatrix44 tmat=this.getMarkerMatrix();
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
	 * こ�?�関数は、�?��?�カ平面上�?�任意�?�矩形で囲まれる領域から、画像を�?影変換して返します�??
	 * {@link #isExist(int)}がtrueの時に�?け使用できます�??
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト�?��?�常は{@link #update(NyARSensor)}関数に入力したものと同じも�?�を指定します�??
	 * @param i_l
	 * 矩形の左上点です�??
	 * @param i_t
	 * 矩形の左上点です�??
	 * @param i_w
	 * 矩形の�?です�??
	 * @param i_h
	 * 矩形の�?です�??
	 * @param i_raster
	 * 出力�?��?�オブジェク�?
	 * @return
	 * 結果を�?�納したi_rasterオブジェク�?
	 * @throws NyARRuntimeException
	 */
	public INyARRgbRaster getMarkerPlaneImage(
		NyARSensor i_sensor,
	    double i_l,double i_t,
	    double i_w,double i_h,
	    INyARRgbRaster i_raster) throws NyARRuntimeException
    {
		return this.getMarkerPlaneImage(i_sensor,i_l+i_w-1,i_t+i_h-1,i_l,i_t+i_h-1,i_l,i_t,i_l+i_w-1,i_t,i_raster);
    }	
}
