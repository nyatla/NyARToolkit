package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;



/**
 * このクラスは、{@link NyARContourTargetStatus}型のプールクラスです。
 * 通常、ユーザが使うことはありません。
 */
public final class NyARContourTargetStatusPool extends NyARManagedObjectPool<NyARContourTargetStatus>
{	
	/**
	 * コンストラクタです。
	 * プールの最大サイズを利用して、インスタンスを生成します。
	 * @param i_size
	 * プールの最大サイズです。
	 * @throws NyARException
	 */
	public NyARContourTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARContourTargetStatus.class);
	}
	/**
	 * この関数は、リスト要素を生成して返します。
	 */
	protected NyARContourTargetStatus createElement()
	{
		return new NyARContourTargetStatus(this._op_interface);
	}
}