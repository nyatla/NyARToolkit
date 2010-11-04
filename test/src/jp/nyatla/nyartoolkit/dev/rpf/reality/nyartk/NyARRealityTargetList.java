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
	public final NyARRealityTarget getItemBySerial(int i_serial)
	{
		NyARRealityTarget[] items=this._items;
		for(int i=items.length-1;i>=0;i--)
		{
			if(items[i].serial==i_serial){
				return items[i];
			}
		}
		return null;
	}
	/**
	 * シリアルIDがi_serialに一致するターゲットのインデクス番号を返します。
	 * @param i_serial
	 * @return
	 * @throws NyARException
	 */
	public final int getIndexBySerial(int i_serial)
	{
		NyARRealityTarget[] items=this._items;
		for(int i=items.length-1;i>=0;i--)
		{
			if(items[i].serial==i_serial){
				return i;
			}
		}
		return -1;
	}
}
