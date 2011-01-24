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
package jp.nyatla.nyartoolkit.core.utils;

/**
 * このクラスは、{@link NyARManagedObjectPool}の要素の基本クラスです。
 * オブジェクトの有効性を判断するための、参照カウンタをもちます。
 * {@link NyARManagedObjectPool}に対して、オブジェクトの操作インタフェイスを提供します。
 */
public class NyARManagedObject
{
	/**
	 * このインタフェイスは、{@link NyARManagedObject}が{@link NyARManagedObjectPool}を
	 * 所有される操作する関数を定義します。
	 */
	public interface INyARManagedObjectPoolOperater
	{
		/**
		 * この関数は、指定したオブジェクトを、割り当て済みから未割り当てにします。
		 * @param i_object
		 * 未割当にするオブジェクト。
		 */
		public void deleteObject(NyARManagedObject i_object);	
	}
	
	/** オブジェクトの参照カウンタ*/
	private int _count;

	/** 所有されるオブジェクトプールの操作インタフェイスのポインタ*/
	private INyARManagedObjectPoolOperater _pool_operater;
	
	/**
	 * コンストラクタです。
	 * 所有される{@link NyARManagedObjectPool}を指定して、インスタンスを作成します。
	 * この関数は、{@link NyARManagedObjectPool#createElement}関数が呼び出します。ユーザが使うことはありません。
	 * @param i_ref_pool_operator
	 * このオブジェクトの所有者の持つ、操作インタフェイス
	 */
	protected NyARManagedObject(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		this._count=0;
		this._pool_operater=i_ref_pool_operator;
	}
	/**
	 * この関数は、オブジェクトを初期状態にします。
	 * この関数は、{@link NyARManagedObjectPool}が呼び出します。ユーザが呼び出すことはありません。
	 * @return
	 * このオブジェクトを初期化したオブジェクト。
	 */
	public final NyARManagedObject initObject()
	{
		assert(this._count==0);
		this._count=1;
		return this;
	}
	/**
	 * この関数は、オブジェクトの参照カウンタを1加算します。
	 * @return
	 * このオブジェクトの参照値。
	 */
	public final NyARManagedObject refObject()
	{
		assert(this._count>0);
		this._count++;
		return this;
	}
	/**
	 * この関数は、オブジェクトの参照カウンタを1減算します。
	 * 参照カウンタが0になると、オブジェクトは未参照状態となり、自動的に{@link NyARManagedObjectPool}へ返却されます。
	 * @return
	 * 減算後の参照カウンタ
	 */
	public int releaseObject()
	{
		assert(this._count>0);
		this._count--;
		if(this._count==0){
			this._pool_operater.deleteObject(this);
		}
		return this._count;
	}
	/**
	 * この関数は、現在のインスタンスの参照カウンタ値を返します。
	 * @return
	 * 参照カウンタ値
	 */
	public final int getCount()
	{
		return this._count;
	}
}