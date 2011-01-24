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
 * このクラスは、シンプルなオブジェクトプールの基本クラスです。
 * 生成済のオブジェクトを複数所有して、他のオブジェクトに貸付る機能を提供します。
 * 参照カウンタを持つ管理されたオブジェクトプールが必要な時には、{@link NyARManagedObjectPool}クラスを使用します。
 * @param <T>
 * 要素のオブジェクト型
 */
public class NyARObjectPool<T extends Object>
{
	/**　要素の保管リスト*/
	protected T[] _buffer;
	/**　未割当の要素トリスト*/
	protected T[] _pool;
	/** 未割当の要素の数*/
	protected int _pool_stock;

	/**
	 * この関数は、新しいオブジェクトを１個割り当てて返します。
	 * @return
	 * 新しいオブジェクト。失敗した場合はnull
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
	 * この関数は、オブジェクトプールへオブジェクトを返却します。
	 * @param　i_object
	 * {@link #newObject}で割り当てたオブジェクトを指定します。
	 * 必ず同じインスタンスで割り当てたオブジェクトを指定してください。
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
	 * コンストラクタです。
	 * 生成拒否の為に、コンストラクタを隠蔽します。
	 * 継承クラスを作成してください。
	 */
	public NyARObjectPool() throws NyARException
	{
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * 継承クラスのコンストラクタから呼び出します。
	 * @param i_length
	 * 配列の最大長さ
	 * @param i_element_type
	 * 配列型を示すクラスタイプ
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
	 * この関数は、配列要素のオブジェクトを１個作ります。
	 * {@link #initInstance(int, Class)}から呼び出されます。
	 * 継承クラスでオーバライドして、要素オブジェクトを１個生成して返す処理を実装してください。
	 * @return
	 * 新しいオブジェクトを返してください。
	 * @throws NyARException
	 */
	protected T createElement() throws NyARException
	{
		throw new NyARException();
	}
	
}