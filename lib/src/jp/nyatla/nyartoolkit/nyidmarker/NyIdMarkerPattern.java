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
package jp.nyatla.nyartoolkit.nyidmarker;

/**
 * このクラスは、Idマーカのデータ部の値を格納します。
 * Idマーカのデータ仕様については、以下のURLを参照してください。
 * http://sourceforge.jp/projects/nyartoolkit/docs/standards_document0001/ja/2/standards_document0001.pdf
 *
 */
public class NyIdMarkerPattern
{
	/**
	 * マーカのModel番号
	 */
	public int model;
	/**
	 * コントロールビットのDoamin番号
	 */
	public int ctrl_domain;
	/**
	 * コントロールビットのマスク番号
	 */
	public int ctrl_mask;
	/**
	 * コントロールビットのチェック値
	 */
	public int check;
	/**
	 * データパケットの配列。有効長はモデルによって異なります。
	 */
	public final int[] data=new int[32];
}