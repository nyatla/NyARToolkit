package jp.nyatla.nyartoolkit.core.types.stack;

import java.lang.reflect.Array;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * 実体をもたない可変長配列です。
 * このクラスは実体化できません。継承して使います。
 *
 * @param <T>
 */
public class NyARPointerStack<T>
{
	protected T[] _items;
	protected int _length;
	
	/**
	 * このクラスは実体化できません。
	 * @throws NyARException
	 */
	protected NyARPointerStack() throws NyARException
	{
	}

	/**
	 * スタックのメンバ変数を初期化します。この関数は、このクラスを継承したクラスを公開するときに、コンストラクタから呼び出します。
	 * @param i_length
	 * @param i_element_type
	 * @throws NyARException
	 */
	@SuppressWarnings("unchecked")
	protected void initInstance(int i_length,Class<T> i_element_type) throws NyARException
	{
		//領域確保
		this._items = (T[])Array.newInstance(i_element_type, i_length);
		//使用中個数をリセット
		this._length = 0;
		return;		
	}

	/**
	 * スタックに参照を積みます。
	 * @return
	 * 失敗するとnull
	 */
	public T push(T i_object)
	{
		// 必要に応じてアロケート
		if (this._length >= this._items.length){
			return null;
		}
		// 使用領域を+1して、予約した領域を返す。
		this._items[this._length]=i_object;
		this._length++;
		return i_object;
	}
	/**
	 * スタックに参照を積みます。pushとの違いは、失敗した場合にassertすることです。
	 * @param i_object
	 * @return
	 */
	public T pushAssert(T i_object)
	{
		// 必要に応じてアロケート
		assert(this._length < this._items.length);
		// 使用領域を+1して、予約した領域を返す。
		this._items[this._length]=i_object;
		this._length++;
		return i_object;
	}
	
	/** 
	 * 見かけ上の要素数を1減らして、そのオブジェクトを返します。
	 * @return
	 */
	public T pop()
	{
		assert(this._length>=1);
		this._length--;
		return this._items[this._length];
	}
	/**
	 * 見かけ上の要素数をi_count個減らします。
	 * @param i_count
	 * @return
	 */
	public final void pops(int i_count)
	{
		assert(this._length>=i_count);
		this._length-=i_count;
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
	 * 指定した要素を削除します。
	 * 削除した要素は前方詰めで詰められます。
	 */
	public void remove(int i_index)
	{
		assert(this._length>i_index && i_index>=0);
		
		if(i_index!=this._length-1){
			int i;
			final int len=this._length-1;
			T[] items=this._items;
			for(i=i_index;i<len;i++)
			{
				items[i]=items[i+1];
			}
		}
		this._length--;
	}
	/**
	 * 指定した要素を順序を無視して削除します。
	 * @param i_index
	 */
	public void removeIgnoreOrder(int i_index)
	{
		assert(this._length>i_index && i_index>=0);
		//値の交換
		if(i_index!=this._length-1){
			this._items[i_index]=this._items[this._length-1];
		}
		this._length--;
	}
	/**
	 * 見かけ上の要素数をリセットします。
	 */
	public void clear()
	{
		this._length = 0;
	}
}