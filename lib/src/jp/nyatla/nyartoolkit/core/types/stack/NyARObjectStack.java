/* 
 * PROJECT: NyARToolkit
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
package jp.nyatla.nyartoolkit.core.types.stack;
import jp.nyatla.nyartoolkit.NyARException;



/**
 * このクラスは、オブジェクトを格納する可変長配列です。
 * 配列に、オブジェクトの実態を所有します。
 * このクラスの実体化は禁止しています。継承して使ってください。
 * <p>継承クラスの実装方法 - 
 * 配列要素の生成シーケンスを実装するには、{@link #createElement}をオーバライドして、
 * コンストラクタから{@link #initInstance}を呼び出します。
 * {@link #initInstance}には２種類の関数があります。要素生成パラメータの有無で、どちらかを選択して呼び出してください。
 * </p>
 * @param <T>
 * 配列型を指定します。
 */
public class NyARObjectStack<T> extends NyARPointerStack<T>
{
	/**
	 * コンストラクタです。
	 * クラスの実体化を禁止するために宣言しています。
	 * 継承クラスから呼び出してください。
	 * @throws NyARException
	 */
	protected NyARObjectStack() throws NyARException
	{
		return;
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * 継承クラスのコンストラクタから呼び出します。
	 * {@link #initInstance(int, Class, Object)}との違いは、オブジェクトの生成に引数を渡すかどうかです。
	 * 引数が必要な時は、こちらの関数を使って、{@link #createElement()}をオーバライドします。
	 * @param i_length
	 * 配列の最大長さ
	 * @param i_element_type
	 * 配列型を示すクラスタイプ
	 * @throws NyARException
	 */
	protected void initInstance(int i_length,Class<T> i_element_type) throws NyARException
	{
		//領域確保
		super.initInstance(i_length,i_element_type);
		for (int i =0; i < i_length; i++){
			this._items[i] =createElement();
		}
		return;
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * 継承クラスのコンストラクタから呼び出します。
	 * {@link #initInstance(int, Class)}との違いは、オブジェクトの生成に引数を渡すかどうかです。
	 * 引数が必要な時は、こちらの関数を使って、{@link #createElement(Object)}をオーバライドします。
	 * @param i_length
	 * 配列の最大長さ
	 * @param i_element_type
	 * 配列型を示すクラスタイプ
	 * @param i_param
	 * 配列要素を生成するときに渡すパラメータ
	 * @throws NyARException
	 */
	protected void initInstance(int i_length,Class<T> i_element_type,Object i_param) throws NyARException
	{
		//領域確保
		super.initInstance(i_length,i_element_type);
		for (int i =0; i < i_length; i++){
			this._items[i] =createElement(i_param);
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
	/**
	 * この関数は、配列要素のオブジェクトを(引数付きで)１個作ります。
	 * {@link #initInstance(int, Class, Object)}から呼び出されます。
	 * 継承クラスでオーバライドして、要素オブジェクトを１個生成して返す処理を実装してください。
	 * @return
	 * 新しいオブジェクトを返してください。
	 * @throws NyARException
	 */
	protected T createElement(Object i_param) throws NyARException
	{
		throw new NyARException();
	}
	
	/**
	 * この関数は、配列から新しい要素を１個わりあてて返します。
	 * 関数が成功すると、有効な配列長が+1されます。
	 * @return
	 * 成功すると、新しい配列要素。
	 * 失敗するとnull
	 * @throws NyARException
	 */
	public final T prePush()
	{
		// 必要に応じてアロケート
		if (this._length >= this._items.length){
			return null;
		}
		// 使用領域を+1して、予約した領域を返す。
		T ret = this._items[this._length];
		this._length++;
		return ret;
	}
	/**
	 * この関数は機能しません。{@link #prePush}を使って下さい。
	 */
	public T push(T i_object)
	{
		return null;
	}
	/**
	 * この関数は、配列の有効長を設定します。
	 * @param i_reserv_length
	 * 設定するサイズ
	 */
	public final void init(int i_reserv_length) throws NyARException
	{
		// 必要に応じてアロケート
		if (i_reserv_length >= this._items.length){
			throw new NyARException();
		}
		this._length=i_reserv_length;
	}
	//override
	public final void remove(int i_index)
	{
		if(i_index!=this._length-1){
			T item=this._items[i_index];
			//要素をシフト
			super.remove(i_index);
			//外したオブジェクトを末端に取り付ける
			this._items[i_index]=item;
		}
		this._length--;
	}
	//override
	public final void removeIgnoreOrder(int i_index)
	{
		assert(this._length>i_index && i_index>=0);
		if(i_index!=this._length-1){
			//削除対象のオブジェクトを取り外す
			T item=this._items[i_index];
			//値の交換
			this._items[i_index]=this._items[this._length-1];
			this._items[this._length-1]=item;
		}
		this._length--;
	}
}
