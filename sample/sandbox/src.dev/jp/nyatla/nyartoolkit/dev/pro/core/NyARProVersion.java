package jp.nyatla.nyartoolkit.dev.pro.core;

public class NyARProVersion
{
	/**モジュール�?*/
	public final static String MODULE_NAME="NyARToolkitProfessional";
	/**メジャーバ�?�ジョン*/
	public final static int VERSION_MAJOR=1;
	/**マイナバージョン*/
	public final static int VERSION_MINOR=1;
	/**タグ*/
	public final static int VERSION_TAG=1;
	/**バ�?�ジョン�?字�??*/
	public final static String VERSION_STRING
		=MODULE_NAME+"/"+VERSION_MAJOR+"."+VERSION_MINOR+"."+VERSION_TAG+";"
		+jp.nyatla.nyartoolkit.core.NyARVersion.VERSION_STRING;
}
