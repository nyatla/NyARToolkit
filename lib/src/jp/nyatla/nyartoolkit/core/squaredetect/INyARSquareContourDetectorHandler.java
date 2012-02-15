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
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;

/**
 * このインタフェイスは、NyARSquareContourDetectorクラスのイベントハンドラです。
 */
public interface INyARSquareContourDetectorHandler
{
	/**
	 * この関数は、自己コールバック関数です。{@link #detectMarker}が検出結果を通知する為に使います。
	 * 実装クラスでは、ここに矩形の発見時の処理を記述してください。
	 * @param i_coord
	 * 輪郭線オブジェクト
	 * @param i_vertex_index
	 * 矩形の４頂点に対応する、輪郭線オブジェクトのインデクス番号。
	 * @throws NyARException
	 */
	public void detectMarkerCb(NyARIntCoordinates i_coord,int[] i_vertex_index)  throws NyARException;
}
