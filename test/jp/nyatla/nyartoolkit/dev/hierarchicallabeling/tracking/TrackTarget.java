package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

public class TrackTarget
{
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
