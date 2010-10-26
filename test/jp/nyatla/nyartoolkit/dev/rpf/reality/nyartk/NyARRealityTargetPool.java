package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARNewTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARNewTargetStatusPool;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObjectPool;

public class NyARRealityTargetPool extends NyARManagedObjectPool<NyARRealityTarget>
{
	private NyARNewTargetStatusPool _ref_pool;
	public NyARRealityTargetPool(int i_size) throws NyARException
	{
		this.initInstance(i_size,NyARRealityTarget.class);
	}
	protected NyARRealityTarget createElement() throws NyARException
	{
		return new NyARRealityTarget(this._op_interface);
	}
	/**
	 * NyARTargetStatusを持つターゲットを新規に作成します。
	 * @param i_clock
	 * システムクロック値
	 * @param i_sample
	 * 初期化元のサンプリングアイテム
	 * @return
	 * @throws NyARException 
	 */
	public NyARRealityTarget newNewTarget(long i_clock,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		NyARRealityTarget t=super.newObject();
		if(t==null){
			return null;
		}
		return t;
	}	
}