/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.core.labeling;

import java.lang.reflect.Array;


/**
 * ラベル同士の重なり（内包関係）を調べるクラスです。 
 * ラベルリストに内包するラベルを蓄積し、それにターゲットのラベルが内包されているか を確認します。
 */
public class NyARLabelOverlapChecker<T extends NyARLabelInfo>
{
	private T[] _labels;
	private int _length;
	private Class<T> _element_type;
	/*
	*/
	@SuppressWarnings("unchecked")
	public NyARLabelOverlapChecker(int i_max_label,Class<T> i_element_type)
	{
		this._element_type=i_element_type;
		this._labels = (T[])Array.newInstance(i_element_type, i_max_label);
	}

	/**
	 * チェック対象のラベルを追加する。
	 * 
	 * @param i_label_ref
	 */
	public void push(T i_label_ref)
	{
		this._labels[this._length] = i_label_ref;
		this._length++;
	}

	/**
	 * 現在リストにあるラベルと重なっているかを返す。
	 * 
	 * @param i_label
	 * @return 何れかのラベルの内側にあるならばfalse,独立したラベルである可能性が高ければtrueです．
	 */
	public boolean check(T i_label)
	{
		// 重なり処理かな？
		final T[] label_pt = this._labels;
		final int px1 = (int) i_label.pos_x;
		final int py1 = (int) i_label.pos_y;
		for (int i = this._length - 1; i >= 0; i--) {
			final int px2 = (int) label_pt[i].pos_x;
			final int py2 = (int) label_pt[i].pos_y;
			final int d = (px1 - px2) * (px1 - px2) + (py1 - py2) * (py1 - py2);
			if (d < label_pt[i].area / 4) {
				// 対象外
				return false;
			}
		}
		// 対象
		return true;
	}
	/**
	 * 最大i_max_label個のラベルを蓄積できるようにオブジェクトをリセットする
	 * 
	 * @param i_max_label
	 */
	@SuppressWarnings("unchecked")
	public void setMaxLabels(int i_max_label)
	{
		if (i_max_label > this._labels.length) {
			this._labels = (T[])Array.newInstance(this._element_type, i_max_label);
		}
		this._length = 0;
	}	
	
	
}
