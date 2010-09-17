package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.AreaDataPool;


public class NyARTarget
{
	private static Object _serial_lock=new Object();
	private static long _serial=0;
	/**
	 * システムの稼働範囲内で一意なIDを持つこと。
	 * @return
	 */
	public static long getSerial()
	{
		synchronized(NyARTarget._serial_lock){
			return NyARTarget._serial++;
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
	 * 寿命値
	 */
	public int age;
	////////////////////////
	//targetの基本情報(このTrackerの使用するSamplingSourceのObjectPoolの値を羅列)
	public AreaDataPool ref_area;
	
	/**
	 * ユーザオブジェクトを配置するポインタータグ
	 */
	public Object tag;
}
