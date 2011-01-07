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


/**
 * このクラスは、ラべリング結果１個を格納するデータ型です。
 * ラベル１個の領域情報を格納します。
 *
 */
public class NyARLabelInfo
{
	/** 領域の画素数*/
	public int area;
	/** 領域範囲の右端*/
	public int clip_r;
	/** 領域範囲の左端*/
	public int clip_l;
	/** 領域範囲の下端*/
	public int clip_b;
	/** 領域範囲の上端*/
	public int clip_t;
	/** 領域の中心位置(x)*/
	public double pos_x;
	/** 領域の中心位置(y)*/
	public double pos_y;

}
