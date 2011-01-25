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
 * Copyright (C)2008-2010 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.types;

/**
 * このクラスは、整数型の２次元輪郭線を格納します。
 * 輪郭線は、２次元座標{@link NyARIntPoint2d}の集合です。
 */
public class NyARIntCoordinates
{
	/** 点を格納する配列です。*/
	public NyARIntPoint2d[] items;
	/** 有効な要素の長さです。*/
	public int length;
	/**
	 * コンストラクタです。
	 * 最大長さを指定して、有効要素数0のインスタンスを作ります。
	 * @param i_length
	 * 輪郭の最大長
	 */
	public NyARIntCoordinates(int i_length)
	{
		this.items=NyARIntPoint2d.createArray(i_length);
		this.length=0;
	}
	/**
	 * この関数は２点を結ぶ直線を計算して、輪郭線を保存します。
	 * 輪郭線は、Bresenhamのアルゴリズムで計算します。
	 * ２点間の距離が、格納可能な最大長さ以下になるように注意してください。
	 * @param i_x0
	 * 点1のX座標
	 * @param i_y0
	 * 点1のY座標
	 * @param i_x1
	 * 点2のX座標
	 * @param i_y1
	 * 点2のY座標
	 * @return
	 * 成功するとtrueを返します。
	 */
	public boolean setLineCoordinates(int i_x0, int i_y0, int i_x1, int i_y1)
	{
		NyARIntPoint2d[] ptr=this.items;
		// 線分を定義
		int dx = (i_x1 > i_x0) ? i_x1 - i_x0 : i_x0 - i_x1;
		int dy = (i_y1 > i_y0) ? i_y1 - i_y0 : i_y0 - i_y1;
		int sx = (i_x1 > i_x0) ? 1 : -1;
		int sy = (i_y1 > i_y0) ? 1 : -1;

		// Bresenham
		int idx = 0;
		if (dx >= dy) {
			// 傾きが1以下の場合
			if (dx >= ptr.length) {
				return false;
			}
			int E = -dx;
			for (int i = 0; i <= dx; i++) {
				ptr[idx].x = i_x0;
				ptr[idx].y = i_y0;
				idx++;
				i_x0 += sx;
				E += 2 * dy;
				if (E >= 0) {
					i_y0 += sy;
					E -= 2 * dx;
				}
			}
		} else {
			// 傾きが1より大きい場合
			if (dy >= this.items.length) {
				return false;
			}
			int E = -dy;
			for (int i = 0; i <= dy; i++) {
				ptr[idx].x = i_x0;
				ptr[idx].y = i_y0;
				idx++;
				i_y0 += sy;
				E += 2 * dx;
				if (E >= 0) {
					i_x0 += sx;
					E -= 2 * dy;
				}
			}
		}
		this.length=idx;
		return true;
	}	
}
