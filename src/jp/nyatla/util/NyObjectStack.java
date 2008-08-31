package jp.nyatla.util;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * オンデマンド割り当てをするオブジェクト配列
 */
public abstract class NyObjectStack
{
	private final static int ARRAY_APPEND_STEP = 64;

	protected final Object[] _items;

	private int _allocated_size;

	private int _length;

	/**
	 * 最大ARRAY_MAX個の動的割り当てバッファを準備する。
	 * 
	 * @param i_array
	 */
	public NyObjectStack(Object[] i_array)
	{
		// ポインタだけははじめに確保しておく
		this._items = i_array;
		// アロケート済サイズと、使用中個数をリセット
		this._allocated_size = 0;
		this._length = 0;
	}

	/**
	 * ポインタを1進めて、その要素を予約し、その要素へのポインタを返します。
	 * 特定型に依存させるときには、継承したクラスでこの関数をオーバーライドしてください。
	 */
	public Object prePush() throws NyARException
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
		Object ret = this._items[this._length];
		this._length++;
		return ret;
	}
	/**
	 * 0～i_number_of_item-1までの領域を予約します。
	 * 予約すると、見かけ上の要素数は0にリセットされます。
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
			int range = this._length + ARRAY_APPEND_STEP;
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
	 * 配列を返します。
	 * 
	 * @return
	 */
	protected Object[] getArray()
	{
		return this._items;
	}

	/**
	 * この関数を継承先クラスで実装して下さい。
	 * i_bufferの配列の、i_start番目からi_end-1番目までの要素に、オブジェクトを割り当てて下さい。
	 * 
	 * @param i_start
	 * @param i_end
	 * @param i_buffer
	 */
	protected abstract void onReservRequest(int i_start, int i_end, Object[] i_buffer);

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
