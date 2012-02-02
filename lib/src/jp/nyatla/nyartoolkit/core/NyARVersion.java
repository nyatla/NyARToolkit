package jp.nyatla.nyartoolkit.core;

/**
 * このクラスは、NyARToolkitライブラリのバージョン情報を保持します。
 */
public class NyARVersion
{
	/**モジュール名*/
	public final static String MODULE_NAME="NyARToolkit";
	/**メジャーバージョン*/
	public final static int VERSION_MAJOR=4;
	/**マイナバージョン*/
	public final static int VERSION_MINOR=0;
	/**タグ*/
	public final static int VERSION_TAG=0;
	/**バージョン文字列*/
	public final static String VERSION_STRING=MODULE_NAME+"/"+VERSION_MAJOR+"."+VERSION_MINOR+"."+VERSION_TAG;
}
