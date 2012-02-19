/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.NyARException;
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