package jp.nyatla.nyartoolkit.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.*;

/**
 * このクラスは、{@link NyARRealityTarget}の参照リストを定義します。
 * 基本的にユーザが生成することはありません。
 * {@link NyARReality}がメンバ変数としてオブジェクトを所有します。
 */
public class NyARRealityTargetList extends NyARPointerStack<NyARRealityTarget>
{
	/**
	 * コンストラクタです。
	 * @param i_max_target
	 * リストの最大格納数です。
	 * @throws NyARException
	 */
	public NyARRealityTargetList(int i_max_target) throws NyARException
	{
		super.initInstance(i_max_target,(Class<NyARRealityTarget>)NyARRealityTarget.class);
	}
	/**
	 * この関数は、シリアル番号をキーに、リストからターゲットを探索します。
	 * @param i_serial
	 * 検索するシリアルID。{@link NyARRealityTarget}を参照。
	 * @return
	 * 見つかると、そのオブジェクトの参照値。無ければnull
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
	 * この関数は、シリアル番号をキーに、リストからターゲットを探索して、そのインデクス番号を返します。
	 * @param i_serial
	 * 検索するシリアルID。{@link NyARRealityTarget}を参照。
	 * @return
	 * リスト中のインデクス番号。
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
	 * この関数は、特定のステータスのRTターゲットだけを選択して、一括でo_resultへ返します。
	 * 配列サイズが十分でない場合、見つかった順に、配列の上限まで要素を返します。
	 * @param i_type
	 * 検索するRTターゲットのステータス値。{@link NyARRealityTarget}で定義される、RT_から始まるステータスタイプ値を指定します。
	 * @param o_result
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
	 * この関数は、特定のステータスのRTターゲットを1個選択して返します。
	 * @param i_type
	 * 検索するRTターゲットのステータス値。{@link NyARRealityTarget}で定義される、RT_から始まるステータスタイプ値を指定します。
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
