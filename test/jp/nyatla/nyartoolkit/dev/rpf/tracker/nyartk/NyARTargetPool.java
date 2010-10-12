package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObjectPool;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;

public class NyARTargetPool extends NyARManagedObjectPool<NyARTarget>
{
	private NyARNewTargetStatusPool _ref_pool;
	public NyARTargetPool(int i_size,NyARNewTargetStatusPool i_ref_status_pool) throws NyARException
	{
		this.initInstance(i_size,NyARTarget.class);
		this._ref_pool=i_ref_status_pool;
	}
	protected NyARTarget createElement() throws NyARException
	{
		return new NyARTarget(this._op_interface);
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
	public NyARTarget newNewTarget(long i_clock,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		NyARTarget t=super.newObject();
		if(t==null){
			return null;
		}
		t.age=0;
		t.last_update=i_clock;
		t.setValue(i_sample);
		t.serial=NyARTarget.getSerial();
		t.tag=null;
		t.ref_status=this._ref_pool.newObject();
		if(t.ref_status==null){
			t.releaseObject();
			return null;
		}
		((NyARNewTargetStatus)t.ref_status).setValue(i_sample);
		return t;
	}	
}