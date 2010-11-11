package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTarget;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARNewTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObject.INyARManagedObjectPoolOperater;

public class NyARRealityTarget extends NyARManagedObject
{
	
	private static Object _serial_lock=new Object();
	private static long _serial=0;
	
	/**
	 * システムの稼働範囲内で一意なIDを持つこと。
	 * @return
	 */
	public static long getSerial()
	{
		synchronized(NyARRealityTarget._serial_lock){
			return NyARRealityTarget._serial++;
		}
	}
	////////////////////////
	
	/**
	 * 親情報
	 */
	private NyARRealityTargetPool _ref_pool;
	////////////////////////
	//targetの基本情報

	/**
	 * Targetを識別するID値
	 */
	public long serial;
	/**
	 * このターゲットの年齢
	 */
	public long target_age;
	/**
	 * 内部向けの公開メンバ変数です。refTransformMatrix()関数で参照してください。
	 */
	public NyARTransMatResult _transform_matrix=new NyARTransMatResult();
	/**
	 * このターゲットの大きさ。3次元座標を計算するときに使います。
	 */
	public NyARRectOffset offset=new NyARRectOffset();
	/**
	 * このターゲットが参照しているトラックターゲット
	 */
	public NyARTarget ref_tracktarget;
	
	public NyARSquare screen_square=new NyARSquare();
	
	/**
	 * このターゲットの情報タイプ
	 * RT_UNKNOWN=未確定ターゲット。2次元座標利用可能
	 * RT_KNOWN  =確定した既知のターゲット。3次元座標利用可能
	 * RT_DEAD   =次のprogressで削除するターゲット
	 */
	public int target_type;
	
	/**
	 * このターゲットの遅延サイクル
	 */
	/**
	 * このターゲットの最終更新クロック(track_tick準拠)
	 */
	
	
	public final static int RT_UNKNOWN   =0;//よくわからないターゲット
	public final static int RT_KNOWN     =2;//知ってるターゲット
	public final static int RT_DEAD      =4;//間もなく死ぬターゲット
	
	/**
	 * カメラ座標系をターゲット座標系に変換する行列を返します。
	 * @return
	 */
	public final NyARTransMatResult refTransformMatrix()
	{
		assert(this.target_type==RT_KNOWN);
		return this._transform_matrix;
	}
	


	/**
	 * ターゲットの2次元座標を元に、i_sourceからターゲットのパターン取得します。
	 * @param i_source
	 * 入力元のRealityInオブジェクト。
	 * @param i_samle_per_pixel
	 * 1ピクセルあたりのサンプリング数。1が最も高速。2なら、1ピクセルあたり、2*2=4ピクセルをサンプリング。
	 * @param o_raster
	 * 出力先のラスタ
	 * @return
	 * @throws NyARException 
	 */
	public boolean getPerspectiveTargetPatt(NyARRealitySource i_source,int i_sample_per_pixel,INyARRgbRaster o_raster) throws NyARException
	{
		assert(this.target_type==RT_UNKNOWN || this.target_type==RT_KNOWN);
		//エッジサイズは0にする。
		return i_source._source_perspective_reader.read4Point(
			i_source._rgb_source,((NyARRectTargetStatus)(this.ref_tracktarget.ref_status)).vertex,0,0,i_sample_per_pixel,o_raster);
	};
	/**
	 * エッジサイズを考慮して、ターゲットの2次元座標を元に、i_sourceからターゲットのパターン取得します。
	 * @param i_source
	 * 入力元のRealityInオブジェクト。
	 * @param i_sample_per_pixel
	 * 1ピクセルあたりのサンプリング数。1が最も高速。2なら、1ピクセルあたり、2*2=4ピクセルをサンプリング。
	 * @param i_edge_percent_x
	 * X方向のエッジサイズを0-99の割合で指定します。
	 * @param i_edge_percent_y
	 * Y方向のエッジサイズを0-99の割合で指定します。
	 * @param o_raster
	 * 出力先のラスタ
	 * @return
	 * @throws NyARException
	 */
	public boolean getPerspectiveTargetPattWithEdge(NyARRealitySource i_source,int i_sample_per_pixel,int i_edge_percent_x,int i_edge_percent_y,INyARRgbRaster o_raster) throws NyARException
	{
		assert(this.target_type==RT_UNKNOWN || this.target_type==RT_KNOWN);
		return i_source._source_perspective_reader.read4Point(
			i_source._rgb_source,((NyARRectTargetStatus)(this.ref_tracktarget.ref_status)).vertex,i_edge_percent_x,i_edge_percent_y,i_sample_per_pixel,o_raster);
	}
	/**
	 * ターゲット座標平面上に定義した任意位置の矩形から、パターンを取得します。
	 * マーカサイズを正しく設定しなかった場合は、任意矩形の座標が相対的にずれます。
	 * @param i_source
	 * @param i_x
	 * ターゲット座標系上の左上X点。mm単位
	 * @param i_y
	 * ターゲット座標系上の左上Y点。mm単位
	 * @param i_w
	 * ターゲット座標系上の矩形幅。mm単位
	 * @param i_h
	 * ターゲット座標系上の矩形幅。mm単位
	 * @param i_sample_per_pixel
	 * 1ピクセルあたりのサンプリング数。1が最も高速。2なら、1ピクセルあたり、2*2=4ピクセルをサンプリング。
	 * @param o_raster
	 * @return
	 * @throws NyARException 
	 */
	public boolean getPerspectivePatt(NyARRealitySource i_source,double i_x,double i_y,double i_w,double i_h,int i_sample_per_pixel,INyARRgbRaster o_raster) throws NyARException
	{
		assert(this.target_type==RT_KNOWN);
		NyARDoublePoint2d[] da=this._ref_pool._wk_da4;
		this._transform_matrix.transformParallelRect2d(i_x,i_y,i_w,i_h,this._ref_pool._ref_prj_mat,this._ref_pool._wk_da4);
		
		return i_source._source_perspective_reader.read4Point(i_source._rgb_source,da,0,0,i_sample_per_pixel,o_raster);
	}
	/**
	 * カメラの撮像点に対応する、ターゲット座標平面上の点を求めます。
	 * @param i_px
	 * @param i_py
	 * @param i_pz
	 * @param o_pos
	 * @return
	 */
	public boolean cameraPos2TargetPos(double i_px,double i_py,double i_pz,NyARDoublePoint3d o_pos)
	{
		double nx=this._transform_matrix.m02;
		double ny=this._transform_matrix.m12;
		double nz=this._transform_matrix.m22;
		double mx=this._transform_matrix.m03;
		double my=this._transform_matrix.m13;
		double mz=this._transform_matrix.m23;
		double t=(nx*mx+ny*my+nz*mz)/(nx*i_px+ny*i_py+nz*i_pz);
NyARDoubleMatrix44 m=new NyARDoubleMatrix44();
		double sx=t*i_px;
		double sy=t*i_py;
		double sz=t*i_pz;
		if(!m.inverse(this._transform_matrix)){
			return false;
		}
		o_pos.x=m.m00*sx+m.m01*sy+m.m02*sz+m.m03;
		o_pos.y=m.m10*sx+m.m11*sy+m.m12*sz+m.m13;
		o_pos.z=m.m20*sx+m.m21*sy+m.m22*sz+m.m23;
		return true;
	}

	//[OPT:]指定したターゲットとの変換行列を求める。
	//[OPT:]ターゲット座標系の平面上の任意の矩形を変換行列で移動した四角形から、パターンを取得する。解像度は受け取り側のラスタに従う。
	/**
	 * 指定した点が、このターゲットの内側であるか判定します。この関数は、Known/Unknownターゲットに使用できます。
	 * @param i_x
	 * @param i_y
	 * @return
	 */
	public final boolean isInnerPoint2d(int i_x,int i_y)
	{
		assert(this.target_type==RT_UNKNOWN || this.target_type==RT_KNOWN);
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(this.ref_tracktarget.ref_status)).vertex;
		for(int i=3;i>=0;i--){
			if(NyARDoublePoint2d.crossProduct3Point(vx[i],vx[(i+1)%4],i_x,i_y)<0)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * ユーザオブジェクトを配置するポインタータグ
	 */
	public Object tag;

	public NyARRealityTarget(NyARRealityTargetPool i_pool)
	{
		super(i_pool._op_interface);
		this._ref_pool=i_pool;
	}
	/**
	 * @Override
	 */
	public int releaseObject()
	{
		int ret=super.releaseObject();
		if(ret==0)
		{
			//参照ターゲットのタグをクリアして、参照解除
			this.ref_tracktarget.tag=null;
			this.ref_tracktarget.refObject();
		}
		return ret;
	}
}