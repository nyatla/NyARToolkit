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
	public final NyARRealityTarget moveTarget(NyARRealityTargetList<T> i_to,int i_index,int i_new_type) throws NyARException
	{
		//入力制限
		NyARRealityTarget ret=i_to.push(this._items[i_index]);
		if(ret==null){
			return null;
		}
		this.remove(i_index);
		ret.target_type=i_new_type;
		return ret;
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
	public final NyARRealityTarget moveTargetNoOrder(NyARRealityTargetList<T> i_to,int i_index,int i_new_type) throws NyARException
	{
		//入力制限
		NyARRealityTarget ret=i_to.push(this._items[i_index]);
		if(ret==null){
			return null;
		}
		this.removeIgnoreOrder(i_index);
		ret.target_type=i_new_type;
		return ret;
	}
	/**
	 * シリアルIDがi_serialに一致するターゲットのインデクス番号を返します。
	 * @param i_serial
	 * @return
	 * @throws NyARException
	 */
	public final int getIndexBySerial(int i_serial)
	{
		for(int i=this._length-1;i>=0;i--)
		{
			if(this._items[i].serial==i_serial){
				return i;
			}
		}
		return -1;
	}	
}
