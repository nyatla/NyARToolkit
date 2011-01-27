package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;

/**
 * このクラスは、{@link NyARTarget}型のプールクラスです。
 * 通常、ユーザが使うことはありません。
 */
final public class NyARTargetPool extends NyARManagedObjectPool<NyARTarget>
{
	/**
	 * コンストラクタです。
	 * プールの最大サイズを利用して、インスタンスを生成します。
	 * @param i_size
	 * プールの最大サイズです。
	 * @throws NyARException
	 */
	public NyARTargetPool(int i_size) throws NyARException
	{
		this.initInstance(i_size,NyARTarget.class);
	}
	/**
	 * この関数は、リスト要素を生成して返します。
	 */
	protected NyARTarget createElement() throws NyARException
	{
		return new NyARTarget(this._op_interface);
	}
	/**
	 * この関数は、新しいオブジェクトを１個割り当てて返します。
	 * 基礎クラスとの違いは、割り当てたオブジェクトの初期化をおこなう点です。
	 * @return
	 * 初期化済のオブジェクト
	 * @throws NyARException
	 */
	public NyARTarget newNewTarget() throws NyARException
	{
		NyARTarget t=super.newObject();
		if(t==null){
			return null;
		}
		t._serial=NyARTarget.createSerialId();
		t._ref_status=null;
		t.tag=null;
		return t;
	}	
}