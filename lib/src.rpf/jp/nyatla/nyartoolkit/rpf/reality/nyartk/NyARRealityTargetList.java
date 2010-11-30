package jp.nyatla.nyartoolkit.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.*;

public class NyARRealityTargetList extends NyARPointerStack<NyARRealityTarget>
{
	public NyARRealityTargetList(int i_max_target) throws NyARException
	{
		super.initInstance(i_max_target,(Class<NyARRealityTarget>)NyARRealityTarget.class);
	}
	/**
	 * RealityTargetのシリアル番号をキーに、ターゲットを探索します。
	 * @param i_serial
	 * @return
	 */
	public final NyARRealityTarget getItemBySerial(long i_serial)
	{
		NyARRealityTarget[] items=this._items;
		for(int i=this._length-1;i>=0;i--)
		{
			if(items[i]._serial==i_serial){
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
		for(int i=this._length-1;i>=0;i--)
		{
			if(items[i]._serial==i_serial){
				return i;
			}
		}
		return -1;
	}
	/**
	 * リストから特定のタイプのターゲットだけを選択して、一括でo_resultへ返します。
	 * @param i_type
	 * ターゲットタイプです。NyARRealityTarget.RT_*を指定してください。
	 * @param o_list
	 * 選択したターゲットを格納する配列です。
	 * @return
	 * 選択できたターゲットの個数です。o_resultのlengthと同じ場合、取りこぼしが発生した可能性があります。
	 */	
	public int selectTargetsByType(int i_type,NyARRealityTarget[] o_result)
	{
		int num=0;
		for(int i=this._length-1;i>=0 && num<o_result.length;i--)
		{
			if(this._items[i]._target_type!=i_type){
				continue;
			}
			o_result[num]=this._items[i];
			num++;
		}
		return num;
	}
	/**
	 * リストから特定のタイプのターゲットを1個選択して、返します。
	 * @param i_type
	 * ターゲットタイプです。NyARRealityTarget.RT_*を指定してください。
	 * @return
	 * 見つかるとターゲットへの参照を返します。見つからなければNULLです。
	 */
	public NyARRealityTarget selectSingleTargetByType(int i_type)
	{
		for(int i=this._length-1;i>=0;i--)
		{
			if(this._items[i]._target_type!=i_type){
				continue;
			}
			return this._items[i];
		}
		return null;
	}	
}
