/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.types.stack;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;



/**
 * このクラスは、割当済のオブジェクトを格納する可変長配列です。
 * 配列の要素には割当済のオブジェクトを格納します。
 * <p>継承クラスの実装方法 - 
 * 配列要素の生成シーケンスを実装するには、{@link #createElement}をオーバライドして、
 * コンストラクタから{@link #initInstance}を呼び出します。
 * {@link #initInstance}には２種類の関数があります。要素生成パラメータの有無で、どちらかを選択して呼び出してください。
 * </p>
 * @param <T>
 * 配列型を指定します。
 */
public abstract class NyARObjectStack<T> extends NyARPointerStack<T>
{

	/**
	 * この関数は、インスタンスを初期化します。
	 * 継承クラスのコンストラクタから呼び出します。
	 * {@link #NyARObjectStack(int, Class, Object)}との違いは、オブジェクトの生成に引数を渡すかどうかです。
	 * 引数が必要な時は、こちらの関数を使って、{@link #createElement()}をオーバライドします。
	 * @param i_length
	 * 配列の最大長さ
	 * @param i_element_type
	 * 配列型を示すクラスタイプ
	 * @throws NyARRuntimeException
	 */
	protected NyARObjectStack(int i_length,Class<T> i_element_type)
	{
		//領域確保
		super(i_length,i_element_type);
		for (int i =0; i < i_length; i++){
			this._items[i] =createElement();
		}
		return;
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * 継承クラスのコンストラクタから呼び出します。
	 * {@link #NyARObjectStack(int, Class)}との違いは、オブジェクトの生成に引数を渡すかどうかです。
	 * 引数が必要な時は、こちらの関数を使って、{@link #createElement(Object)}をオーバライドします。
	 * @param i_length
	 * 配列の最大長さ
	 * @param i_element_type
	 * 配列型を示すクラスタイプ
	 * @param i_param
	 * 配列要素を生成するときに渡すパラメータ
	 * @throws NyARRuntimeException
	 */
	protected NyARObjectStack(int i_length,Class<T> i_element_type,Object i_param)
	{
		//領域確保
		super(i_length,i_element_type);
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
	 */
	protected T createElement()
	{
		throw new UnsupportedOperationException();
	}
	/**
	 * この関数は、配列要素のオブジェクトを(引数付きで)１個作ります。
	 * {@link #initInstance(int, Class, Object)}から呼び出されます。
	 * 継承クラスでオーバライドして、要素オブジェクトを１個生成して返す処理を実装してください。
	 * @return
	 * 新しいオブジェクトを返してください。
	 * @throws NyARRuntimeException
	 */
	protected T createElement(Object i_param)
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * この関数は、配列から新しい要素を１個わりあてて返します。
	 * 関数が成功すると、有効な配列長が+1されます。
	 * @return
	 * 成功すると、新しい配列要素。
	 * 失敗するとnull
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
		throw new UnsupportedOperationException();
	}
	/**
	 * この関数は、配列の有効長を設定します。
	 * この関数はインスタンスの最大格納数を増加させるわけではなく、i_reserv_lengthまでの要素がアクティブになるだけです。
	 * @param i_reserv_length
	 * 設定するサイズ
	 */
	public final void init(int i_reserv_length)
	{
		// 必要に応じてアロケート
		if (i_reserv_length >= this._items.length){
			throw new NyARRuntimeException();
		}
		this._length=i_reserv_length;
	}
	@Override
	public final void remove(int i_index)
	{
		final int len=this._length-1;
		if(i_index!=len){
		//末端以外の場合は前方詰めと差し替え
			
			//削除対象のオブジェクトを保存
			T item=this._items[i_index];
			//前方詰め
			T[] items=this._items;
			for(int i=i_index;i<len;i++)
			{
				items[i]=items[i+1];
			}
			//外したオブジェクトを末端に取り付ける
			this._items[len]=item;
		}
		//要素数を1減らす
		this._length--;
	}
	@Override
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
