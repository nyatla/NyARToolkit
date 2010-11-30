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
 * NyARManagedObjectPoolの要素クラスです。
 *
 */
public class NyARManagedObject
{
	/**
	 * このインタフェイスは、NyARManagedObjectがPoolを操作するために使います。
	 */	
	public interface INyARManagedObjectPoolOperater
	{
		public void deleteObject(NyARManagedObject i_object);	
	}
	/**
	 * オブジェクトの参照カウンタ
	 */
	private int _count;
	/**
	 * オブジェクトの解放関数へのポインタ
	 */
	private INyARManagedObjectPoolOperater _pool_operater;
	/**
	 * NyARManagedObjectPoolのcreateElement関数が呼び出すコンストラクタです。
	 * @param i_ref_pool_operator
	 * Pool操作の為のインタフェイス
	 */
	protected NyARManagedObject(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		this._count=0;
		this._pool_operater=i_ref_pool_operator;
	}
	public final NyARManagedObject initObject()
	{
		assert(this._count==0);
		this._count=1;
		return this;
	}
	/**
	 * このオブジェクトに対する、新しい参照オブジェクトを返します。
	 * @return
	 */
	public final NyARManagedObject refObject()
	{
		assert(this._count>0);
		this._count++;
		return this;
	}
	/**
	 * 参照オブジェクトを開放します。
	 * @return
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
	 * 現在の参照カウンタを返します。
	 * @return
	 */
	public final int getCount()
	{
		return this._count;
	}
}