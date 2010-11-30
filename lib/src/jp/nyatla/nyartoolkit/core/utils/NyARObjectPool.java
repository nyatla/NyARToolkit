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

import java.lang.reflect.Array;

import jp.nyatla.nyartoolkit.NyARException;



/**
 * このクラスは、型Tのオブジェクトプールを提供します。
 *
 * @param <T>
 */
public class NyARObjectPool<T extends Object>
{
	protected T[] _buffer;
	protected T[] _pool;
	protected int _pool_stock;

	/**
	 * オブジェクトプールからオブジェクトを取り出します。
	 * @return
	 */
	public T newObject()
	{
		if(this._pool_stock<1){
			return null;
		}
		this._pool_stock--;
		return this._pool[this._pool_stock];
		
	}
	/**
	 * オブジェクトプールへオブジェクトを返却します。
	 * @return
	 */
	public void deleteObject(T i_object)
	{
		assert(i_object!=null);
		assert(this._pool_stock<this._pool.length);
		//自身の提供したオブジェクトかを確認するのは省略。
		this._pool[this._pool_stock]=i_object;
		this._pool_stock++;
	}

	/**
	 * このクラスは実体化できません。
	 * @throws NyARException
	 */
	public NyARObjectPool() throws NyARException
	{
	}
	/**
	 * オブジェクトを初期化します。この関数は、このクラスを継承したクラスを公開するときに、コンストラクタから呼び出します。
	 * @param i_length
	 * @param i_element_type
	 * @throws NyARException
	 */
	@SuppressWarnings("unchecked")
	protected void initInstance(int i_length,Class<T> i_element_type) throws NyARException
	{
		//領域確保
		this._buffer = (T[])Array.newInstance(i_element_type, i_length);
		this._pool = (T[])Array.newInstance(i_element_type, i_length);
		//使用中個数をリセット
		this._pool_stock=i_length;
		//オブジェクトを作成
		for(int i=this._pool.length-1;i>=0;i--)
		{
			this._buffer[i]=this._pool[i]=createElement();
		}
		return;		
	}
	/**
	 * オブジェクトを作成します。
	 * @return
	 * @throws NyARException
	 */
	protected T createElement() throws NyARException
	{
		throw new NyARException();
	}
	
}