package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.core.utils.*;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.*;


/**
 * このクラスは、トラックターゲットのステータスを格納するマネージドオブジェクトのベースクラスです。
 * トラックターゲットのステータス毎に、格納するパラメータを追加して使います。
 * <p>メモ - 
 * ST_XXXの定数値は、{@link NyARTracker}のコンストラクタ実装と密接に絡んでいます。
 * 変更するときは注意すること！
 * </p>
 *
 */
public class NyARTargetStatus extends NyARManagedObject
{
	/** 定数値。IGNOREステータスを表します。*/
	public final static int ST_IGNORE=0;
	/** 定数値。NEWステータスを表します。*/
	public final static int ST_NEW=1;
	/** 定数値。RECTステータスを表します。*/
	public final static int ST_RECT=2;
	/** 定数値。CONTUREステータスを表します。*/
	public final static int ST_CONTURE=3;
	/** 定数値。ステータスの種類を表します。*/
	public final static int MAX_OF_ST_KIND=3;
	/**
	 * コンストラクタです。
	 * 所有されるプールオブジェクトを指定して、インスタンスを生成します。
	 * @param iRefPoolOperator
	 * プールオブジェクトのコントロールインタフェイス。
	 */
	protected NyARTargetStatus(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
	}
}
