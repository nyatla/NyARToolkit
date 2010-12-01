package jp.nyatla.nyartoolkit.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.INyARDisposable;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARTarget;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.NyARRectTargetStatus;

/**
 * Realityターゲットを定義します。
 * {@link #tag}以外のクラスメンバに対する書き込み操作を行わないでください。
 *
 */
public class NyARRealityTarget extends NyARManagedObject
{
	/**　ユーザオブジェクトを配置するポインタータグ。ユーザが自由にオブジェクトポインタを配置できる。
	 * {@link INyARDisposable}インタフェイスを持つオブジェクトを指定すると、このターゲットを開放するときに{@link INyARDisposable#dispose()}をコールする。
	 * <p>{@link INyARDisposable}インタフェイスは、Managed環境下では通常不要。</p>
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
			//TAGオブジェクトがINyARDisposableインタフェイスを持てば、disposeをコール
			if(this._ref_tracktarget instanceof INyARDisposable)
			{
				((INyARDisposable)(this._ref_tracktarget)).dispose();
			}
			//参照ターゲットのタグをクリアして、参照解除
			this._ref_tracktarget.tag=null;
			this._ref_tracktarget.releaseObject();
		}
		return ret;
	}
	/** 無効なシリアルID値*/
	public final static int INVALID_REALITY_TARGET_ID=-1;
	private static Object _serial_lock=new Object();
	private static long _serial_counter=0;
	
	/**
	 * ID生成器。システムの稼働範囲内で一意なIDを持つこと。
	 * @return
	 */
	public static long createSerialId()
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

	/** 内部向けの公開メンバ変数です。{@link #getSerialId}を使ってください。*/
	public long _serial;
	/** 内部向けの公開メンバ変数です。{@link #refTransformMatrix}を使ってください。*/
	public NyARTransMatResult _transform_matrix=new NyARTransMatResult();

	/** ターゲットの種類。未知のターゲット。*/
	public final static int RT_UNKNOWN   =0;
	/** ターゲットの種類。既知のターゲット。*/
	public final static int RT_KNOWN     =2;
	/** ターゲットの種類。間もなく消失するターゲット。次回のprogressでリストから除去される。*/
	public final static int RT_DEAD      =4;

	/** 内部向けpublicメンバ変数。{@link #getTargetType()}を使ってください。*/
	public int _target_type;
	
	/** 内部向けpublicメンバ。 ターゲットのオフセット位置。*/
	public NyARRectOffset _offset=new NyARRectOffset();
	/** 内部向けpublicメンバ。このターゲットが参照しているトラックターゲット*/
	public NyARTarget _ref_tracktarget;
	/** 内部向けpublicメンバ。スクリーン上の歪み解除済み矩形。*/
	public NyARSquare _screen_square=new NyARSquare();
	/** 内部向けpublicメンバ。getGrabbRateを使ってください。*/
	public int grab_rate;
	

	
	/**
	 * カメラ座標系をターゲット座標系に変換する行列の参照値を返します。
	 * この値は変更しないでください。（編集するときは、コピーを作ってください。）
	 * @return
	 */
	public final NyARTransMatResult refTransformMatrix()
	{
		assert(this._target_type==RT_KNOWN);
		return this._transform_matrix;
	}
	/**
	 * このターゲットのタイプを返します。
	 * {@link #RT_UNKNOWN}=未確定ターゲット。2次元座標利用可能
	 * {@link #RT_KNOWN}  =確定した既知のターゲット。3次元座標利用可能
	 * {@link #RT_DEAD}   =次のprogressで削除するターゲット
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
	 * 20を切ると消失の可能性が高い？
	 * @return
	 */
	public int getGrabbRate()
	{
		return this.grab_rate;
	}
	/**
	 * ターゲットの頂点配列への参照値を返します。この値は、二次元検出系の出力値です。
	 * 値が有効なのは、次のサイクルを実行するまでの間です。
	 * @return
	 */
	public final NyARDoublePoint2d[] refTargetVertex()
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		return ((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex;
	}
	/**
	 * 対象矩形の頂点配列をコピーして返します。
	 * 樽型歪みの逆矯正は行いません。
	 * @param o_vertex
	 */
	public final void getTargetVertex(NyARDoublePoint2d[] o_vertex)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d[] v=((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex;
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
		NyARDoublePoint2d.makeCenter(((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex,4,o_center);
	}
	/**
	 * {@link #getTargetCenter}の出力型違いの関数です。
	 * @param o_center
	 */
	public final void getTargetCenter(NyARIntPoint2d o_center)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d.makeCenter(((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex,4,o_center);
	}
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
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex;
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
	 * <p>メモ:この関数にはnewが残ってるので注意</p>
	 */
	public final boolean isInnerRectPoint2d(int i_x,int i_y)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARIntRect rect=new NyARIntRect();
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex;
		rect.setAreaRect(vx,4);
		return rect.isInnerPoint(i_x, i_y);
	}
	
	/**
	 * ターゲット座標系の4頂点でかこまれる領域を射影した平面から、RGB画像をo_rasterに取得します。
	 * @param i_vertex
	 * ターゲットのオフセットを基準値とした、頂点座標。要素数は4であること。(mm単位)
	 * @param i_matrix
	 * i_vertexに適応する変換行列。
	 * ターゲットの姿勢行列を指定すると、ターゲット座標系になります。不要ならばnullを設定してください。
	 * @param i_resolution
	 * 1ピクセルあたりのサンプリング値(n^2表現)
	 * @param o_raster
	 * 出力ラスタ
	 * @return
	 * @throws NyARException
	 * <p>メモ:この関数にはnewが残ってるので注意</p>
	 */
	public final boolean getRgbPatt3d(NyARRealitySource i_src,NyARDoublePoint3d[] i_vertex,NyARDoubleMatrix44 i_matrix,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		assert(this._target_type==RT_KNOWN);
		NyARDoublePoint2d[] da4=this._ref_pool._wk_da2_4;
		NyARDoublePoint3d v3d=new NyARDoublePoint3d();
		if(i_matrix!=null){
			//姿勢変換してから射影変換
			for(int i=3;i>=0;i--){
				//姿勢を変更して射影変換
				i_matrix.transform3d(i_vertex[i],v3d);
				this._transform_matrix.transform3d(v3d,v3d);
				this._ref_pool._ref_prj_mat.project(v3d,da4[i]);
			}
		}else{
			//射影変換のみ
			for(int i=3;i>=0;i--){
				//姿勢を変更して射影変換
				this._transform_matrix.transform3d(i_vertex[i],v3d);
				this._ref_pool._ref_prj_mat.project(v3d,da4[i]);
			}
		}
		//パターンの取得
		return i_src.refPerspectiveRasterReader().read4Point(i_src.refRgbSource(),da4,0,0,i_resolution, o_raster);
	}
	/**
	 * ターゲットと同じ平面に定義した矩形から、パターンを取得します。
	 * @param i_src
	 * @param i_x
	 * ターゲットのオフセットを基準値とした、矩形の左上座標(mm単位)
	 * @param i_y
	 * ターゲットのオフセットを基準値とした、矩形の左上座標(mm単位)
	 * @param i_w
	 * ターゲットのオフセットを基準値とした、矩形の幅(mm単位)
	 * @param i_h
	 * ターゲットのオフセットを基準値とした、矩形の幅(mm単位)
	 * @param i_resolution
	 * 1ピクセルあたりのサンプリング値(n^2表現)
	 * @param o_raster
	 * 出力ラスタ
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbRectPatt3d(NyARRealitySource i_src,double i_x,double i_y,double i_w,double i_h,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		assert(this._target_type==RT_KNOWN);
		//RECT座標を作成
		NyARDoublePoint3d[] da4=this._ref_pool._wk_da3_4;
		da4[0].x=i_x;    da4[0].y=i_y+i_h;da4[0].z=0;//LB
		da4[1].x=i_x+i_w;da4[1].y=i_y+i_h;da4[1].z=0;//RB
		da4[2].x=i_x+i_w;da4[2].y=i_y;    da4[2].z=0;//RT
		da4[3].x=i_x;    da4[3].y=i_y;    da4[3].z=0;//LT
		return getRgbPatt3d(i_src,da4,null,i_resolution,o_raster);
	}
	
}