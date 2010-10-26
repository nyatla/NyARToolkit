package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;

import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObjectPool;



/*
 * 輪郭情報を保管します。
 * このクラスの要素は、他の要素から参照する可能性があります。
 */
public final class NyARContourTargetStatusPool extends NyARManagedObjectPool<NyARContourTargetStatus>
{	
	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @param i_cood_max
	 * 輪郭ベクトルの最大数
	 * @throws NyARException
	 */
	public NyARContourTargetStatusPool(int i_size,int i_cood_max) throws NyARException
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