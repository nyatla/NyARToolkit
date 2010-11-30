package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.core.utils.NyARManagedObject;


/**
 * TargetStatusの基礎クラスです。TargetStatusは、ステータス毎に異なるターゲットのパラメータを格納します。
 * @note
 * ST_から始まるID値は、NyARTrackerのコンストラクタと密接に絡んでいるので、変更するときは気をつけて！
 *
 */
public class NyARTargetStatus extends NyARManagedObject
{
	public final static int ST_IGNORE=0;
	public final static int ST_NEW=1;
	public final static int ST_RECT=2;
	public final static int ST_CONTURE=3;
	public final static int MAX_OF_ST_KIND=3;
	protected NyARTargetStatus(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
	}
}
