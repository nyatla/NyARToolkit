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
	/**
	 * i_index番目のターゲットを、i_toのリストへ移動します。
	 * 移動後のリストは、前方詰めします。
	 * @param i_to
	 * @param i_index
	 * @return
	 * @throws NyARException
	 */
	public final boolean moveTarget(NyARRealityTargetList<T> i_to,int i_index,int i_new_type) throws NyARException
	{
		//入力制限
		NyARRealityTarget ret=i_to.push(this._items[i_index]);
		if(ret==null){
			return false;
		}
		this.remove(i_index);
		ret.target_type=i_new_type;
		return true;
	}
	/**
	 * i_index番目のターゲットを、i_toのリストへ移動します。
	 * 移動元のリストは、removeIgnoreOrderの仕様に従い、データ順序が変更されます。
	 * @param i_to
	 * @param i_index
	 * @param i_new_type
	 * @return
	 * @throws NyARException
	 */
	public final boolean moveTargetNoOrder(NyARRealityTargetList<T> i_to,int i_index,int i_new_type) throws NyARException
	{
		//入力制限
		NyARRealityTarget ret=i_to.push(this._items[i_index]);
		if(ret==null){
			return false;
		}
		this.removeIgnoreOrder(i_index);
		ret.target_type=i_new_type;
		return true;
	}
	private final int getIndexBySerial(int i_serial)
	{
		for(int i=this._length-1;i>=0;i--)
		{
			
		}
	}
	public final boolean moveTargetBySerial(NyARRealityTargetList<T> i_to,int i_serial,int i_new_type)
	{
		// TODO Auto-generated method stub
		return false;
	}		
}
