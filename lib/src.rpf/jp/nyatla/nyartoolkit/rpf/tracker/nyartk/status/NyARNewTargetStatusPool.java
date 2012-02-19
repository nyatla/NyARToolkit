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
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;

/**
 * このクラスは、{@link NyARNewTargetStatus}型のプールクラスです。
 * 通常、ユーザが使うことはありません。
 */
public class NyARNewTargetStatusPool extends NyARManagedObjectPool<NyARNewTargetStatus>
{
	/**
	 * コンストラクタです。
	 * プールの最大サイズを利用して、インスタンスを生成します。
	 * @param i_size
	 * プールの最大サイズです。
	 * @throws NyARException
	 */
	public NyARNewTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARNewTargetStatus.class);
	}
	/**
	 * この関数は、リスト要素を生成して返します。
	 */
	protected NyARNewTargetStatus createElement() throws NyARException
	{
		return new NyARNewTargetStatus(this._op_interface);
	}

}
