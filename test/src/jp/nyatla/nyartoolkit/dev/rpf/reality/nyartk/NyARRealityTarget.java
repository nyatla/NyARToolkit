package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
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
	 * このターゲットの位置と座標
	 */
	public NyARTransMatResult transmat;
	/**
	 * このターゲットの大きさ。3次元座標を計算するときに使います。
	 */
	public NyARRectOffset offset=new NyARRectOffset();
	/**
	 * このターゲットが参照しているトラックターゲット
	 */
	public NyARTarget ref_ttarget;
	
	public NyARSquare ideal_square;
	
	/**
	 * このターゲットの情報タイプ
	 * 0=わからん2Dのみ。
	 * 1=比率は判って相対3D
	 * 2=大きさもわかって絶対3D
	 */
	public int target_type;
	public final static int RT_UNKNOWN   =0;//思い出しまち
	public final static int RT_RECALL    =1;//思い出し中
	public final static int RT_KNOWN     =2;//知ってるターゲット
	public final static int RT_DEAD      =4;//間もなく死ぬターゲット
	
	/**
	 * ライブラリから得たRealityパラメータ
	 */
	public Object detail;
	

	/**
	 * ターゲットのパターンをラスタにコピーする。解像度は受け取り側ラスタに従う。
	 * @param i_resolution_mag
	 * @param o_raster
	 * @return
	 */
	public boolean getPerspectiveTargetPatt(int i_resolution_mag,INyARRgbRaster o_raster)
	{
		return false;
	};
	//[OPT:]ターゲット座標系の平面上で任意の矩形を定義し、そのパターンをラスタにコピーする。解像度は受け取り側ラスタに従う。
	//[OPT:]ターゲット座標系の平面上の任意の矩形を変換行列で移動した四角形から、パターンを取得する。解像度は受け取り側のラスタに従う。
	//[OPT:]指定したターゲットとの直線距離をもとめる。
	//[OPT:]指定したターゲットとの変換行列を求める。
	//[OPT:]画面上の点に対応するターゲット座標上の点を求める。
	//[OPT:]ターゲット座標上の点に対する、画面上の座標を求める。
	public boolean getPatt3d(int i_resolution_mag,INyARRgbRaster o_raster);
	public boolean getPatt2d(int i_resolution_mag,INyARRgbRaster o_raster);
	
	public void getAreaImage(){};
	public void getMetadata(){};
	public void getSquare(){};
	public void get(){};
	
	
	/**
	 * ユーザオブジェクトを配置するポインタータグ
	 */
	public Object tag;

	public NyARRealityTarget(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
	}
	/**
	 * @Override
	 */
	public int releaseObject()
	{
		int ret=super.releaseObject();
		if(ret==0)
		{
			//参照ターゲットのタグをクリア
			this.ref_ttarget.tag=null;
		}
		return ret;
	}
}