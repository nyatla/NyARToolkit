/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.utils;
import java.lang.reflect.*;
import jp.nyatla.nyartoolkit.NyARException;




/**
 * オンデマンド割り当てをするオブジェクト配列。
 * 配列には実体を格納します。
 */
public abstract class NyObjectStack<T>
{
	private final static int ARRAY_APPEND_STEP = 64;

	protected final T[] _items;

	private int _allocated_size;

	protected int _length;

	/**
	 * 最大ARRAY_MAX個の動的割り当てバッファを準備する。
	 * 
	 * @param i_array
	 * @param i_element_type
	 * JavaのGenedicsの制限突破
	 */
	@SuppressWarnings("unchecked")
	protected NyObjectStack(int i_length,Class<T> i_element_type)
	{
		// ポインタだけははじめに確保しておく
		this._items = (T[])Array.newInstance(i_element_type, i_length);
		// アロケート済サイズと、使用中個数をリセット
		this._allocated_size = 0;
		this._length = 0;
		return;
	}
	protected abstract T createElement();
	/**
	 * ポインタを1進めて、その要素を予約し、その要素へのポインタを返します。
	 * 特定型に依存させるときには、継承したクラスでこの関数をオーバーライドしてください。
	 */
	public final T prePush() throws NyARException
	{
		// 必要に応じてアロケート
		if (this._length >= this._allocated_size) {
			// 要求されたインデクスは範囲外
			if (this._length >= this._items.length) {
				throw new NyARException();
			}
			// 追加アロケート範囲を計算
			int range = this._length + ARRAY_APPEND_STEP;
			if (range >= this._items.length) {
				range = this._items.length;
			}
			// アロケート
			this.onReservRequest(this._allocated_size, range, this._items);
			this._allocated_size = range;
		}
		// 使用領域を+1して、予約した領域を返す。
		T ret = this._items[this._length];
		this._length++;
		return ret;
	}
	/** 
	 * 見かけ上の要素数を1減らして、最後尾のアイテムを返します。
	 * @return
	 */
	public final T pop()
	{
		assert(this._length>=1);
		this._length--;
		return this._items[this._length];
	}
	/**
	 * 見かけ上の要素数をi_count個減らします。
	 * @param i_count
	 * @return
	 * NULLを返します。
	 */
	public final void pops(int i_count)
	{
		assert(this._length>=i_count);
		this._length-=i_count;
		return;
	}	
	
	/**
	 * 0～i_number_of_item-1までの領域を予約します。
	 * 予約済の領域よりも小さい場合には、現在の長さを調整します。
	 * @param i_number_of_reserv
	 */
	final public void reserv(int i_number_of_item) throws NyARException
	{
		// 必要に応じてアロケート
		if (i_number_of_item >= this._allocated_size) {
			// 要求されたインデクスは範囲外
			if (i_number_of_item >= this._items.length) {
				throw new NyARException();
			}
			// 追加アロケート範囲を計算
			int range = i_number_of_item+ARRAY_APPEND_STEP;
			if (range >= this._items.length) {
				range = this._items.length;
			}
			// アロケート
			this.onReservRequest(this._allocated_size, range, this._items);
			this._allocated_size = range;
		}
		//見かけ上の配列サイズを指定
		this._length=i_number_of_item;
		return;
	}
	/**
	 * 必要に応じて、この関数を継承先クラスで実装して下さい。
	 * i_bufferの配列の、i_start番目からi_end-1番目までの要素に、オブジェクトを割り当てて下さい。
	 * @param i_start
	 * @param i_end
	 * @param i_buffer
	 */	
	final protected void onReservRequest(int i_start, int i_end, Object[] i_buffer)
	{
		try {  
			for (int i = i_start; i < i_end; i++){
				i_buffer[i] =createElement();
			}
		} catch(Exception e) {  
			e.printStackTrace();  
		}
		return;
	}


	/**
	 * 配列を返します。
	 * 
	 * @return
	 */
	public final T[] getArray()
	{
		return this._items;
	}
	public final T getItem(int i_index)
	{
		return this._items[i_index];
	}
	/**
	 * 配列の見かけ上の要素数を返却します。
	 * @return
	 */
	public final int getLength()
	{
		return this._length;
	}
	/**
	 * 見かけ上の要素数をリセットします。
	 */
	public final void clear()
	{
		this._length = 0;
	}
}
