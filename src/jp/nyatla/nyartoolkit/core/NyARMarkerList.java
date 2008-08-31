package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * このクラスは、detectMarkerがマーカーオブジェクトの蓄積に使うクラスです。 実体を伴うマーカーホルダと、これを参照するマーカーアレイを持ちます。
 * 
 * マーカーアレイはマーカーホルダに存在するマーカーリストを特定の条件でフィルタした 結果を格納します。
 * 
 * 一度作られたマーカーホルダは繰り返し使用されます。
 * 
 * 
 */
public class NyARMarkerList
{
	private int marker_holder_num;// marker_holderの使用中の数

	protected int marker_array_num;// marker_arrayの有効な数

	protected final NyARMarker[] marker_holder;// マーカーデータの保持配列

	protected final NyARMarker[] marker_array; // マーカーデータのインデックス配列

	/**
	 * 派生データ型をラップするときに使う
	 * 
	 * @param i_holder
	 *            値の保持配列。全要素に実体を割り当てる必要がある。
	 */
	protected NyARMarkerList(NyARMarker[] i_holder)
	{
		this.marker_holder = i_holder;
		this.marker_array = new NyARMarker[i_holder.length];
		this.marker_array_num = 0;
		this.marker_holder_num = 0;
	}

	public NyARMarkerList(int i_number_of_holder)
	{
		this.marker_holder = new NyARMarker[i_number_of_holder];
		// 先にマーカーホルダにオブジェクトを作っておく
		for (int i = 0; i < i_number_of_holder; i++) {
			this.marker_holder[i] = new NyARMarker();
		}
		this.marker_array = new NyARMarker[this.marker_holder.length];
		this.marker_array_num = 0;
		this.marker_holder_num = 0;
	}

	/**
	 * 現在位置のマーカーホルダを返す。 現在位置が終端の場合関数は失敗する。
	 * 
	 * @return
	 */
	public final NyARMarker getCurrentHolder() throws NyARException
	{
		if (this.marker_holder_num >= this.marker_holder.length) {
			throw new NyARException();
		}
		return this.marker_holder[this.marker_holder_num];
	}

	/**
	 * マーカーホルダの現在位置を1つ進めて、そのホルダを返す。 この関数を実行すると、使用中のマーカーホルダの数が1個増える。
	 * 
	 * @return 空いているマーカーホルダが無ければnullを返します。
	 * 
	 */
	public final NyARMarker getNextHolder()
	{
		// 現在位置が終端位置ならnullを返す。
		if (this.marker_holder_num + 1 >= this.marker_holder.length) {
			this.marker_holder_num = this.marker_holder.length;
			return null;
		}
		this.marker_holder_num++;
		return this.marker_holder[this.marker_holder_num];
	}

	/**
	 * マーカーアレイのi_indexの要素を返す。
	 * 
	 * @param i_index
	 * @return
	 * @throws NyARException
	 */
	public final NyARMarker getMarker(int i_index) throws NyARException
	{
		if (i_index >= marker_array_num) {
			throw new NyARException();
		}
		return this.marker_array[i_index];
	}

	/**
	 * マーカーアレイの要素数を返す。
	 * 
	 * @return
	 */
	public final int getMarkerNum()
	{
		return marker_array_num;
	}

	/**
	 * マーカーアレイの要素数と、マーカーホルダの現在位置をリセットする。
	 * 
	 * @return
	 */
	public final void reset()
	{
		this.marker_array_num = 0;
		this.marker_holder_num = 0;
	}

	/**
	 * マーカーホルダに格納済みのマーカーから重なっているのものを除外して、 マーカーアレイにフィルタ結果を格納します。
	 * [[この関数はマーカー検出処理と密接に関係する関数です。 NyARDetectMarkerクラス以外から呼び出さないで下さい。]]
	 * メモ:この関数はmarker_holderの内容を変化させまするので注意。
	 */
	public final void updateMarkerArray()
	{
		// 重なり処理かな？
		int i;
		double d;
		double[] pos_j, pos_i;
		// NyARMarker[] marker_holder;
		for (i = 0; i < this.marker_holder_num; i++) {
			pos_i = marker_holder[i].pos;
			for (int j = i + 1; j < this.marker_holder_num; j++) {
				pos_j = marker_holder[j].pos;
				d = (pos_i[0] - pos_j[0]) * (pos_i[0] - pos_j[0])
						+ (pos_i[1] - pos_j[1]) * (pos_i[1] - pos_j[1]);
				if (marker_holder[i].area > marker_holder[j].area) {
					if (d < marker_holder[i].area / 4) {
						marker_holder[j].area = 0;
					}
				} else {
					if (d < marker_holder[j].area / 4) {
						marker_holder[i].area = 0;
					}
				}
			}
		}
		// みつかったマーカーを整理する。
		int l_array_num = 0;
		// エリアが0のマーカーを外した配列を作って、その数もついでに計算
		for (i = 0; i < marker_holder_num; i++) {
			if (marker_holder[i].area == 0.0) {
				continue;
			}
			marker_array[l_array_num] = marker_holder[i];
			l_array_num++;
		}
		// マーカー個数を更新
		this.marker_array_num = l_array_num;
	}
}