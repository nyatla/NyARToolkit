package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTarget;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObjectPool;

public class NyARRealityTargetPool extends NyARManagedObjectPool<NyARRealityTarget>
{
	//targetでの共有オブジェクト
	public NyARPerspectiveProjectionMatrix _ref_prj_mat;
	public NyARDoublePoint2d[] _wk_da4=NyARDoublePoint2d.createArray(4);
	
	public NyARRealityTargetPool(int i_size,NyARPerspectiveProjectionMatrix i_ref_prj_mat) throws NyARException
	{
		this.initInstance(i_size,NyARRealityTarget.class);
		this._ref_prj_mat=i_ref_prj_mat;
		return;
	}
	protected NyARRealityTarget createElement() throws NyARException
	{
		return new NyARRealityTarget(this);
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
		ret._grab_rate=50;//開始時の補足レートは50%
		ret._ref_tracktarget=(NyARTarget) tt.refObject();
		tt.tag=ret;//タグに値設定しておく。
		return ret;
	}	
}