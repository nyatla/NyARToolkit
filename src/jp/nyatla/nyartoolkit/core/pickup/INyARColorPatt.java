/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.pickup;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.NyARSquare;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;

public interface INyARColorPatt
{
	/**
	 * カラーパターンの幅をピクセル値で返します。
	 * 
	 * @return
	 */
	public int getWidth();

	/**
	 * カラーパターンの高さをピクセル値で返します。
	 * 
	 * @return
	 */
	public int getHeight();
	/**
	 * カメラパターンを格納した配列への参照値を返します。 配列は最低でも[height][wight][3]のサイズを持ちますが、
	 * 配列のlengthとwidth,heightの数は一致しないことがあります。
	 * setSize関数を実行すると、以前に呼び出されたgetPatArrayが返した値は不定になります。
	 * 
	 * @return
	 */
	public int[][][] getPatArray();

	/**
	 * ラスタイメージからi_square部分のカラーパターンを抽出して、保持します。
	 * 
	 * @param image
	 * @param i_square
	 * @return ラスターの取得に成功するとTRUE/失敗するとFALSE
	 * @throws NyARException
	 */
	public boolean pickFromRaster(INyARRgbRaster image, NyARSquare i_square) throws NyARException;
}