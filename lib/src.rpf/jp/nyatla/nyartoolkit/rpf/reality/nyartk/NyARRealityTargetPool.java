package jp.nyatla.nyartoolkit.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARTarget;

public class NyARRealityTargetPool extends NyARManagedObjectPool<NyARRealityTarget>
{
	//targetでの共有オブジェクト
	public NyARPerspectiveProjectionMatrix _ref_prj_mat;
	/** Target間での共有ワーク変数。*/
	public NyARDoublePoint3d[] _wk_da3_4=NyARDoublePoint3d.createArray(4);
	public NyARDoublePoint2d[] _wk_da2_4=NyARDoublePoint2d.createArray(4);
	
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
		ret.grab_rate=50;//開始時の捕捉レートは10%
		ret._ref_tracktarget=(NyARTarget) tt.refObject();
		ret._serial=NyARRealityTarget.createSerialId();
		ret.tag=null;
		tt.tag=ret;//トラックターゲットのタグに自分の値設定しておく。
		return ret;
	}	
}