package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;

/**
 * このクラスは、{@link NyARNewTargetStatus}型のプールクラスです。
 * 通常、ユーザが使うことはありません。
 */
public class NyARNewTargetStatusPool extends NyARManagedObjectPool<NyARNewTargetStatus>
{
	/**
	 * コンストラクタです。
	 * プールの最大サイズを利用して、インスタンスを生成します。
	 * @param i_size
	 * プールの最大サイズです。
	 * @throws NyARException
	 */
	public NyARNewTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARNewTargetStatus.class);
	}
	/**
	 * この関数は、リスト要素を生成して返します。
	 */
	protected NyARNewTargetStatus createElement() throws NyARException
	{
		return new NyARNewTargetStatus(this._op_interface);
	}

}
