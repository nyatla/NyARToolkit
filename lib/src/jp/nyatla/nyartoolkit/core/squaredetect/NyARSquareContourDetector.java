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
package jp.nyatla.nyartoolkit.core.squaredetect;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、矩形検出器のベースクラスです。
 * 矩形検出機能を提供する関数を定義します。
 */
public abstract class NyARSquareContourDetector
{
	/**
	 * この関数は、ラスタから矩形を検出して、自己コールバック関数{@link #onSquareDetect}で通知します。
	 * 実装クラスでは、矩形検出処理をして、結果を通知する処理を実装してください。
	 * @param i_raster
	 * 検出元のラスタ画像
	 * @throws NyARException
	 */
	public abstract void detectMarker(NyARBinRaster i_raster) throws NyARException;
	/**
	 * この関数は、自己コールバック関数です。{@link #detectMarker}が検出矩形を通知するために使います。
	 * 実装クラスでは、ここに矩形の発見時の処理を記述してください。
	 * @param i_coord
	 * 輪郭線オブジェクト
	 * @param i_vertex_index
	 * 矩形の４頂点に対応する、輪郭線オブジェクトのインデクス番号。
	 * @throws NyARException
	 */
	protected abstract void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index)  throws NyARException;
}

