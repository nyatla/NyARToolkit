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
 * このクラスは、Realityターゲット(RTターゲット)のプロパティを格納します。
 * プロパティには、ターゲットのメタデータ、二次元座標系データ、三次元座標系データの３個があります。
 * ターゲットのメタデータは常に利用が可能です。二次元座標系データは、{@link #RT_DEAD}以外のステータスを持つ
 * RTターゲットだけが利用できます。三次元座標データは、{@link #RT_KNOWN}ステータスを持つRTターゲットだけが利用できます。
 * ユーザーが直接書き込みできるのは{@link #tag}メンバのみです。他のメンバには、書き込みをしないでください。
 * 
 */
public class NyARRealityTarget extends NyARManagedObject
{
	/**　ユーザオブジェクトを配置するポインタータグです。ユーザが自由にオブジェクトを配置できます。
	 * {@link INyARDisposable}インタフェイスを持つオブジェクトを指定すると、このターゲットを開放するときに{@link INyARDisposable#dispose()}が自動的にコールされます。
	 * <p>{@link INyARDisposable}インタフェイスは、RTターゲットの消失時に特別な処理を実行したいときに追加してください。</p>
	 */
	public Object tag;
	/**
	 * コンストラクタです。
	 * この関数は、ユーザが使うことはありません。
	 * @param i_pool
	 * 親となるマネージドオブジェクトプール
	 */
	public NyARRealityTarget(NyARRealityTargetPool i_pool)
	{
		super(i_pool._op_interface);
		this._ref_pool=i_pool;
	}
	/**
	 * ファイナライザです。
	 * この関数は、ユーザが使うことはありません。
	 * {@link INyARDisposable}に関する処理を追加しています。
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
	/** 定数値。無効なシリアルID値を定義します。*/
	public final static int INVALID_REALITY_TARGET_ID=-1;
	private static Object _serial_lock=new Object();
	private static long _serial_counter=0;
	
	/**
	 * ID生成器。システムの稼働期間中、一意なRealityTargetSerialIDを返します。
	 * この関数は、ユーザが使うことはありません。
	 * @return
	 * 一意なID
	 */
	public static long createSerialId()
	{
		synchronized(NyARRealityTarget._serial_lock){
			return NyARRealityTarget._serial_counter++;
		}
	}
	////////////////////////
	
	/** 親情報*/
	private NyARRealityTargetPool _ref_pool;
	////////////////////////
	//targetの基本情報

	/** 内部向けのメンバ変数です。{@link #getSerialId}を使ってください。*/
	public long _serial;
	/** 内部向けのメンバ変数です。{@link #refTransformMatrix}を使ってください。*/
	public NyARTransMatResult _transform_matrix=new NyARTransMatResult();

	/** ターゲットの種類。Unknownステータスであることを示します。*/
	public final static int RT_UNKNOWN   =0;
	/** ターゲットの種類。Knownステータスである事を示します。*/
	public final static int RT_KNOWN     =2;
	/** ターゲットの種類。Deadステータスである事を示します。*/
	public final static int RT_DEAD      =4;

	/** 内部向けのメンバ変数です。{@link #getTargetType()}を使ってください。*/
	public int _target_type;
	
	/** 内部向けのメンバ変数です。ターゲットのオフセット位置。*/
	public NyARRectOffset _offset=new NyARRectOffset();
	/** 内部向けのメンバ変数です。このターゲットが参照しているトラックターゲット*/
	public NyARTarget _ref_tracktarget;
	/** 内部向けのメンバ変数です。スクリーン上の歪み解除済み矩形。*/
	public NyARSquare _screen_square=new NyARSquare();
	/** 内部向けのメンバ変数です。{@link #getGrabbRate}を使ってください。*/
	public int _grab_rate;
	

	
	/**
	 * この関数は、RTターゲットの姿勢変換行列の参照値を返します。
	 * 姿勢変換行列は、カメラ座標系をRTターゲット座標系に変換する行列です。
	 * この値は変更しないでください。（編集するときは、コピーを作ってください。）
	 * 値は、次のサイクルを実行するまで有効です。
	 * @return
	 * [READ ONLY]姿勢変換行列の参照値。
	 */
	public final NyARTransMatResult refTransformMatrix()
	{
		assert(this._target_type==RT_KNOWN);
		return this._transform_matrix;
	}
	/**
	 * この関数は、RTターゲットのステータスタイプを返します。
	 * <p>値のサマリ-
	 * 詳しくは、{@link NyARReality}を参考にして下さい。
	 * <ul>
	 * <li>{@link #RT_UNKNOWN}=未確定ターゲット。2次元座標利用可能
	 * <li>{@link #RT_KNOWN}  =確定した既知のターゲット。3次元座標利用可能
	 * <li>{@link #RT_DEAD}   =次のprogressで削除するターゲット
	 * </ul>
	 * @return
	 * RTターゲットのステータス値
	 */
	public int getTargetType()
	{
		return this._target_type;
	}
	/**
	 * この関数は、RTターゲットのシリアルIDを返します。
	 * 詳しくは、{@link NyARReality}を参考にして下さい。
	 * @return
	 * シリアルID。
	 */
	public long getSerialId()
	{
		return this._serial;
	}

	/**
	 * この関数は、RTターゲットの補足率を返します。
	 * 値は、一定期間における、ターゲットの認識率を元に計算します。
	 * 20を切ると消失の可能性が高い？様な気がします。
	 * @return
	 * 補足率の値。0-100の数値です。
	 */
	public int getGrabbRate()
	{
		return this._grab_rate;
	}
	/**
	 * この関数は、RTターゲットの四角系頂点配列への参照値を返します。
	 * 二次元系の座標値です。{@link #RT_KNOWN}と{@link #RT_UNKNOWN}ステータスのRTターゲットで使用できます。
	 * 樽型歪みの逆矯正は行いません。
	 * 値は、次のサイクルを実行するまで有効です。
	 * @return
	 * [READ ONLY]ターゲットの四角系の頂点配列(4要素)。
	 */
	public final NyARDoublePoint2d[] refTargetVertex()
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		return ((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex;
	}
	/**
	 * この関数は、RTターゲットの四角系頂点配列をコピーして返します。
	 * 二次元系の座標値です。{@link #RT_KNOWN}と{@link #RT_UNKNOWN}ステータスのRTターゲットで使用できます。
	 * 樽型歪みの逆矯正は行いません。
	 * @param o_vertex
	 * 値を格納する配列。4要素である事。
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
	 * この関数は、RTターゲットの四角系の中央点を返します。
	 * 二次元系の座標値です。{@link #RT_KNOWN}と{@link #RT_UNKNOWN}ステータスのRTターゲットで使用できます。
	 * 樽型歪み逆矯正は行いません。
	 * @param o_center
	 * 値を格納するオブジェクト。
	 */
	public final void getTargetCenter(NyARDoublePoint2d o_center)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d.makeCenter(((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex,4,o_center);
	}
	/**
	 * {@link #getTargetCenter(NyARDoublePoint2d)}の出力型違いの関数です。
	 * @param o_center
	 * 値を格納するオブジェクト。
	 */
	public final void getTargetCenter(NyARIntPoint2d o_center)
	{
		assert(this._target_type==RT_UNKNOWN || this._target_type==RT_KNOWN);
		NyARDoublePoint2d.makeCenter(((NyARRectTargetStatus)(this._ref_tracktarget._ref_status)).vertex,4,o_center);
	}
	/**
	 * この関数は、点がRTターゲットの二次元座標系の四角形の内側にあるか判定します。
	 * (範囲ではなく、頂点の内側であることに注意してください。)
	 * 二次元系の座標値です。{@link #RT_KNOWN}と{@link #RT_UNKNOWN}ステータスのRTターゲットで使用できます。
	 * 入力値の樽型歪み矯正は行いません。
	 * @param i_x
	 * 検査する座標x
	 * @param i_y
	 * 検査する座標y
	 * @return
	 * 点が内側にあるときはtrue,無い時はfalse
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
	 * この関数は、点がRTターゲットの二次元座標系の四角形を包括する矩形の内側にあるか判定します。
	 * 二次元系の座標値です。{@link #RT_KNOWN}と{@link #RT_UNKNOWN}ステータスのRTターゲットで使用できます。
	 * 入力値の樽型歪み矯正は行いません。
	 * <p>メモ-この関数にはnewが残っています。大量に呼び出すときにはnewの削除を検討しましょう。
	 * </p>
	 * @param i_x
	 * 検査する座標x
	 * @param i_y
	 * 検査する座標y
	 * @return
	 * 点が内側にあるときはtrue,無い時はfalse
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
	 * この関数は、RTターゲット座標系の4頂点で囲まれる領域を射影した平面から、RGB画像をo_rasterに取得します。
	 * 三次元系の座標値です。{@link #RT_KNOWN}ステータスのRTターゲットで使用できます。
	 * <p>メモ:この関数にはnewが残ってるので注意</p>
	 * @param i_src
	 * 画像ソース。このRTターゲットの親の{@link NyARReality}に入力した{@link NyARRealitySource}オブジェクト。
	 * @param i_vertex
	 * ターゲットのオフセットを基準値とした、頂点座標。要素数は4であること。(mm単位)
	 * @param i_matrix
	 * RTターゲット座標系の姿勢変換行列。値を指定すると、RTターゲット座標系と平行な面から、任意の姿勢に変換した後にパターンを取得します。
	 * nullを指定すると、RTターゲット座標系と同じ平面の座標系で取得します。
	 * この変数は、例えばマーカの40mm上のパターンを取得したりするときに役立ちます。(誤差の影響を強く受けるため、精密な測定には向いていません。)
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * 2なら4ピクセル、4なら16ピクセルの入力から、出力1ピクセルを生成します。
	 * @param o_raster
	 * 出力先のラスタオブジェクト。
	 * @return
	 * 画像取得に成功するとtrue
	 * @throws NyARException
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
	 * RTターゲット座標系の4頂点で定義する矩形から、RGB画像をo_rasterに取得します。
	 * 三次元系の座標値です。{@link #RT_KNOWN}ステータスのRTターゲットで使用できます。
	 * @param i_src
	 * 画像ソース。このRTターゲットの親の{@link NyARReality}に入力した{@link NyARRealitySource}オブジェクト。
	 * @param i_x
	 * RTターゲット座標系の矩形の左上座標X(mm単位)
	 * @param i_y
	 * RTターゲット座標系の矩形の左上座標Y(mm単位)
	 * @param i_w
	 * RTターゲット座標系の矩形の幅(mm単位)
	 * @param i_h
	 * RTターゲット座標系の矩形の高さ(mm単位)
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * 2なら4ピクセル、4なら16ピクセルの入力から、出力1ピクセルを生成します。
	 * @param o_raster
	 * 出力先のラスタオブジェクト。
	 * @return
	 * 画像取得に成功するとtrue
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