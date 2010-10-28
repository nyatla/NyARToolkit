package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.*;

public class NyARRealityTargetList<T extends NyARRealityTarget> extends NyARPointerStack<T>
{
	public NyARRealityTargetList(int i_max_target) throws NyARException
	{
		super.initInstance(i_max_target,(Class<T>)NyARRealityTarget.class);
	}
	public final boolean moveToUnknownTarget(UnknowonTarget i_to,int i_index) throws NyARException
	{
		//入力制限
		NyARRealityTarget ret=i_to.push(this._items[i_index]);
		if(ret==null){
			return false;
		}
		this.removeIgnoreOrder(i_index);
		ret.target_type=NyARRealityTarget.RT_UNKNOWN;
		return true;
	}	
	public final boolean moveToKnownTarget(KnowonTarget i_to,int i_index) throws NyARException
	{
		//入力制限
		NyARRealityTarget ret=i_to.push(this._items[i_index]);
		if(ret==null){
			return false;
		}
		this.removeIgnoreOrder(i_index);
		ret.target_type=NyARRealityTarget.RT_KNOWN;
		return true;
	}	
	public final boolean moveToDeadTarget(DeadTarget i_to,int i_index) throws NyARException
	{
		//入力制限
		NyARRealityTarget ret=i_to.push(this._items[i_index]);
		if(ret==null){
			return false;
		}
		this.removeIgnoreOrder(i_index);
		ret.target_type=NyARRealityTarget.RT_DEAD;
		return true;
	}
}
