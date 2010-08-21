package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;


public class TrackTarget
{
	private static Object _serial_lock=new Object();
	private static long _serial=0;
	public static long getSerial()
	{
		synchronized(TrackTarget._serial_lock){
			return TrackTarget._serial++;
		}
	}

	
	/**
	 * トラッキングシステム全体で一意なID
	 */
	public long serial;
	/**
	 * ユーザオブジェクトを配置するポインタータグ
	 */
	public Object tag;
	/**
	 * このターゲットが最後にアップデートされたtick
	 */
	public long last_update;
	/**
	 * 寿命値
	 */
	public int age;
}
