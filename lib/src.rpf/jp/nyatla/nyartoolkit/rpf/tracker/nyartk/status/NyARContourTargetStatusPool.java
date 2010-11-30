package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;



/*
 * 輪郭情報を保管します。
 * このクラスの要素は、他の要素から参照する可能性があります。
 */
public final class NyARContourTargetStatusPool extends NyARManagedObjectPool<NyARContourTargetStatus>
{	
	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @throws NyARException
	 */
	public NyARContourTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARContourTargetStatus.class);
	}
	/**
	 * @Override
	 */
	protected NyARContourTargetStatus createElement()
	{
		return new NyARContourTargetStatus(this._op_interface);
	}
}