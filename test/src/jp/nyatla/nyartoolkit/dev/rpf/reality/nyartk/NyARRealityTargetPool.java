package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTarget;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObjectPool;

public class NyARRealityTargetPool extends NyARManagedObjectPool<NyARRealityTarget>
{
	public NyARRealityTargetPool(int i_size) throws NyARException
	{
		this.initInstance(i_size,NyARRealityTarget.class);
	}
	protected NyARRealityTarget createElement() throws NyARException
	{
		return new NyARRealityTarget(this._op_interface);
	}
	/**
	 * 新しいRealityTargetを作って返します。
	 * @param tt
	 * @return
	 * @throws NyARException 
	 */
	public NyARRealityTarget newNewTarget(NyARTarget tt) throws NyARException
	{
		NyARRealityTarget ret=super.newObject();
		if(ret==null){
			return null;
		}
		ret.target_age=0;
		ret.target_type=NyARRealityTarget.RT_UNKNOWN;
		ret.ref_ttarget=tt;
		tt.tag=ret;//タグに値設定しておく。
		return ret;
	}	
}