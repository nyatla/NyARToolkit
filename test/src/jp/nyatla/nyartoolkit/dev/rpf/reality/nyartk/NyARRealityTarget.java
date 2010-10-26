package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
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
	 * このターゲットが最後にアップデートされたtick
	 */
	public long last_update;
	/**
	 * このターゲットの寿命値
	 */
	public int age;
	/**
	 * このターゲットの位置と座標
	 */
	public NyARDoubleMatrix44 transmat;
	/**
	 * このターゲットの大きさ
	 */
	public NyARRectOffset offset=new NyARRectOffset();
	/**
	 * このターゲットの情報タイプ
	 * 0=わからん2Dのみ。
	 * 1=比率は判って相対3D
	 * 2=大きさもわかって絶対3D
	 */
	
	
	/**
	 * ユーザオブジェクトを配置するポインタータグ
	 */
	public Object tag;
//	//Samplerからの基本情報
	public NyARIntRect sample_area=new NyARIntRect();
	//アクセス用関数
	
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
		if(ret==0 && this.ref_status!=null)
		{
			this.ref_status.releaseObject();
			this.ref_status=null;
		}
		return ret;
	}	

}