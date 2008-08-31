package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;

public class NyARSquareList
{
	private final NyARSquare[] _square_array;

	private int _square_array_count;

	public NyARSquareList(int i_number_of_holder)
	{
		this._square_array = new NyARSquare[i_number_of_holder];

		// マーカーホルダに実体を割り当てる。
		for (int i = 0; i < this._square_array.length; i++) {
			this._square_array[i] = new NyARSquare();
		}
		this._square_array_count = 0;
	}

	/**
	 * NyARMarkerListを走査して、有効なsquareを取得します。
	 */
	public final void pickupSquare(NyARParam i_param, NyARMarkerList i_markers)throws NyARException
	{
		int j = 0;
		for (int i = 0; i < i_markers.getMarkerNum(); i++) {
			// マーカーのライン情報を確保する。
			if (!i_markers.getMarker(i).getLine(i_param, this._square_array[j])) {
				continue;
			}
			j++;
		}
		// 発見したマーカーの個数を保存
		this._square_array_count = j;
	}

	/**
	 * スクエア配列に格納されている要素数を返します。
	 * 
	 * @return
	 */
	public final int getCount()
	{
		return this._square_array_count;
	}

	/**
	 * スクエア配列の要素を返します。 スクエア配列はマーカーアレイをさらにフィルタした結果です。
	 * 
	 * @param i_index
	 * @return
	 * @throws NyARException
	 */
	public final NyARSquare getSquare(int i_index) throws NyARException
	{
		if (i_index >= this._square_array_count) {
			throw new NyARException();
		}
		return this._square_array[i_index];
	}
}
