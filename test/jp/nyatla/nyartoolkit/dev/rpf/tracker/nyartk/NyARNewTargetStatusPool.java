package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.*;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut.Item;


public class NyARNewTargetStatusPool extends NyARManagedObjectPool<NyARNewTargetStatus>
{
	/**
	 * コンストラクタです。
	 * @param i_size
	 * poolのサイズ
	 * @throws NyARException
	 */
	public NyARNewTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARNewTargetStatus.class);
	}
	/**
	 * @Override
	 */
	protected NyARNewTargetStatus createElement() throws NyARException
	{
		return new NyARNewTargetStatus(this._op_interface);
	}

}
