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
package jp.nyatla.nyartoolkit.core.pickup;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このインタフェイスは、{@link INyARRgbRaster}に、RGBラスタから任意の４頂点で定義した領域を、自由変形して格納する関数を追加します。
 * 元々は、ARToolKitにある、画像からパターンを取得する処理用の関数です。
 */
public interface INyARColorPatt extends INyARRgbRaster
{
	/**
	 * この関数は、画像の４頂点でかこまれる領域から、自由変形したパターンを取得して格納します。
	 * 実装クラスでは、imageのi_vertexsで定義される四角形からパターンを取得して、インスタンスのバッファに格納する処理を書いてください。
	 * @param image
	 * 取得元の画像です。
	 * @param i_vertexs
	 * 収録元画像上の、４頂点を格納した配列です。要素数は4である必要があります。
	 * @return
	 * 取得に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean pickFromRaster(INyARRgbRaster image, NyARIntPoint2d[] i_vertexs) throws NyARException;
}