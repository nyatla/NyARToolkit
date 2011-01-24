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
package jp.nyatla.nyartoolkit.nyidmarker.data;

import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPattern;

/**
 * このインタフェイスは、マーカパターンデータのエンコーダに共通な関数を定義します。
 * NyIdのマーカデータを、利用可能な他形式のデータに変換します。
 */
public interface INyIdMarkerDataEncoder
{
	/**
	 * この関数は、マーカパターンデータを他形式のデータに変換します。
	 * 実装クラスでは、{@link NyIdMarkerPattern}に格納されるデータを変換する処理を実装してください。
	 * @param i_data
	 * 変換元のデータ
	 * @param o_dest
	 * 変換先のデータ
	 * @return
	 * 変換に成功するとtrueを返します。
	 */
	public boolean encode(NyIdMarkerPattern i_data,INyIdMarkerData o_dest);
	/**
	 * この関数は、このエンコーダの出力形式のオブジェクトを生成して返します。
	 * 実装クラスでは、そのクラスの{@link #encode}に入力できるオブジェクトを生成してください。
	 * @return
	 * 新しいオブジェクト
	 */
	public INyIdMarkerData createDataInstance();
}
