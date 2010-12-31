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
 * 可変長なオブジェクト配列です。
 * 型Tのオブジェクト配列を所有し、アクセス方法を提供します。
 */
public class NyARObjectStack<T> extends NyARPointerStack<T>
{

	protected NyARObjectStack() throws NyARException
	{
		return;
	}
	/**
	 * パラメータが不要なインスタンスを作るためのinitInstance
	 * コンストラクタから呼び出します。この関数を使うときには、 createElement()をオーバライドしてください。
	 * @param i_length
	 * @param i_element_type
	 * @param i_param
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
	 * パラメータが必要なインスタンスを作るためのinitInstance
	 * コンストラクタから呼び出します。この関数を使うときには、 createElement(Object i_param)をオーバライドしてください。
	 * @param i_length
	 * @param i_element_type
	 * @param i_param
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
	protected T createElement() throws NyARException
	{
		throw new NyARException();
	}
	protected T createElement(Object i_param) throws NyARException
	{
		throw new NyARException();
	}
	
	/**
	 * 新しい領域を予約します。
	 * @return
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
	 * このクラスは、オブジェクトをpushすることはできません。
	 * prePush()を使用してください。
	 */
	public T push(T i_object)
	{
		return null;
	}
	/**
	 * スタックを初期化します。
	 * @param i_reserv_length
	 * 使用済みにするサイズ
	 * @return
	 */
	public final void init(int i_reserv_length) throws NyARException
	{
		// 必要に応じてアロケート
		if (i_reserv_length >= this._items.length){
			throw new NyARException();
		}
		this._length=i_reserv_length;
	}	
	/**
	 * 指定した要素を削除します。
	 * 削除した要素は前方詰めで詰められます。
	 */
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
	/**
	 * 指定した要素を順序を無視して削除します。
	 * 削除後のスタックの順序は保証されません。
	 * このAPIは、最後尾の有効要素と、削除対象の要素を交換することで、削除を実現します。
	 * @param i_index
	 */
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
