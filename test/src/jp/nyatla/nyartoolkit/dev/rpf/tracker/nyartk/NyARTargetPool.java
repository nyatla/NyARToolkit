package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARNewTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARNewTargetStatusPool;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObjectPool;

final public class NyARTargetPool extends NyARManagedObjectPool<NyARTarget>
{
	public NyARTargetPool(int i_size) throws NyARException
	{
		this.initInstance(i_size,NyARTarget.class);
	}
	protected NyARTarget createElement() throws NyARException
	{
		return new NyARTarget(this._op_interface);
	}
	/**
	 * 新しいターゲットを生成します。ターゲットのserial,tagのみ初期化します。
	 * @param i_clock
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */
	public NyARTarget newNewTarget() throws NyARException
	{
		NyARTarget t=super.newObject();
		if(t==null){
			return null;
		}
		t.serial=NyARTarget.getSerial();
		t.tag=null;
		return t;
	}	
}