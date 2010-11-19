package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
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
	private static long _serial_counter=0;
	
	/**
	 * システムの稼働範囲内で一意なIDを持つこと。
	 * @return
	 */
	public static long getSerial()
	{
		synchronized(NyARRealityTarget._serial_lock){
			return NyARRealityTarget._serial_counter++;
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
	 * 内部向けの公開メンバ変数です。getSerialを使ってください。
	 */
	public long _serial;
	/**
	 * 内部向けの公開メンバ変数です。{@link #refTransformMatrix}で参照してください。
	 */
	public NyARTransMatResult _transform_matrix=new NyARTransMatResult();

	public final static int RT_UNKNOWN   =0;//よくわからないターゲット
	public final static int RT_KNOWN     =2;//知ってるターゲット
	public final static int RT_DEAD      =4;//間もなく死ぬターゲット	
	/**
	 * 内部向けpublicメンバ。{@link #getTargetType()}を使ってください。
	 */
	public int _target_type;
	
	/**
	 * このターゲットの大きさ。3次元座標を計算するときに使います。
	 */
	public NyARRectOffset _offset=new NyARRectOffset();
	/**
	 * 内部向けpublicメンバ。このターゲットが参照しているトラックターゲット
	 */
	public NyARTarget _ref_tracktarget;
	
	public NyARSquare _screen_square=new NyARSquare();
	
	/**
	 * 内部向けpublicメンバ。getGrabbRateを使ってください。
	 */
	public int _grab_rate;
	

	
	/**
	 * カメラ座標系をターゲット座標系に変換する行列を返します。
	 * @return
	 */
	public final NyARTransMatResult refTransformMatrix()
	{
		assert(this._target_type==RT_KNOWN);
		return this._transform_matrix;
	}
	/**
	 * このターゲットのタイプを返します。
	 * RT_UNKNOWN=未確定ターゲット。2次元座標利用可能
	 * RT_KNOWN  =確定した既知のターゲット。3次元座標利用可能
	 * RT_DEAD   =次のprogressで削除するターゲット
	 * @return
	 */
	public int getTargetType()
	{
		return this._target_type;
	}
	/**
	 * Reality内で一意な、ターゲットのシリアルIDです。
	 * @return
	 */
	public long getSerialId()
	{
		return this._serial;
	}

	/**
	 * このターゲットの補足率を返します。0-100の数値です。
	 * 20%切ると消失の可能性が高い？
	 * @return
	 */
	public int getGrabbRate()
	{
		return this._grab_rate;
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
	public boolean getPerspectiveTargetPatt(NyARRealitySource i_source,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		//エッジサイズは0にする。
		return i_source.getRgbPerspectivePatt(((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex, i_resolution, o_raster);
	};
	/**
	 * エッジサイズを考慮して、ターゲットの2次元座標を元に、i_sourceからターゲットのパターン取得します。
	 * @param i_source
	 * 入力元のRealityInオブジェクト。
	 * @param i_resolution
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
	public boolean getPerspectiveTargetPattWithEdge(NyARRealitySource i_source,int i_resolution,int i_edge_percent_x,int i_edge_percent_y,INyARRgbRaster o_raster) throws NyARException
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		return i_source.getRgbPerspectivePatt(((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex,i_resolution, i_edge_percent_x, i_edge_percent_y, o_raster);
	}
	//[OPT:]指定したターゲットとの変換行列を求める。
	//[OPT:]ターゲット座標系の平面上の任意の矩形を変換行列で移動した四角形から、パターンを取得する。解像度は受け取り側のラスタに従う。
	/**
	 * 画面上の点が、このターゲットを構成する頂点の内側にあるか判定します。
	 * (範囲ではなく、頂点の内側であることに注意してください。)
	 * この関数は、Known/Unknownターゲットに使用できます。
	 * @param i_x
	 * @param i_y
	 * @return
	 */
	public final boolean isInnerVertexPoint2d(int i_x,int i_y)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex;
		for(int i=3;i>=0;i--){
			if(NyARDoublePoint2d.crossProduct3Point(vx[i],vx[(i+1)%4],i_x,i_y)<0)
			{
				return false;
			}
		}
		return true;
	}
	/**
	 * 画面上の点が、このターゲットを包括する矩形の内側にあるかを判定します。
	 * この関数は、Known/Unknownターゲットに使用できます。
	 * @param i_x
	 * @param i_y
	 * @return
	 */
	public final boolean isInnerRectPoint2d(int i_x,int i_y)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARIntRect rect=new NyARIntRect();
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex;
		rect.setAreaRect(vx,4);
		return rect.isInnerPoint(i_x, i_y);
	}
	/**
	 * 対象矩形の頂点配列への参照値を返します。
	 * 値が有効なのは、次のサイクルを実行するまでの間です。
	 * @return
	 */
	public final NyARDoublePoint2d[] refTargetVertex()
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		return ((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex;
	}
	/**
	 * 対象矩形の頂点配列をコピーして返します。
	 * 樽型歪みの逆矯正は行いません。
	 * @param o_vertex
	 */
	public final void getTargetVertex(NyARDoublePoint2d[] o_vertex)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d[] v=((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex;
		for(int i=3;i>=0;i--){
			o_vertex[i].setValue(v[i]);
		}
	}
	/**
	 * 対象矩形の中央点を返します。
	 * 樽型歪みの逆矯正は行いません。
	 * @param o_center
	 */
	public final void getTargetCenter(NyARDoublePoint2d o_center)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d.makeCenter(((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex,4,o_center);
	}
	/**
	 * {@link #getTargetCenter}の出力型違いの関数です。
	 * @param o_center
	 */
	public final void getTargetCenter(NyARIntPoint2d o_center)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d.makeCenter(((NyARRectTargetStatus)(this._ref_tracktarget.ref_status)).vertex,4,o_center);
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
			this._ref_tracktarget.tag=null;
			this._ref_tracktarget.releaseObject();
		}
		return ret;
	}
}