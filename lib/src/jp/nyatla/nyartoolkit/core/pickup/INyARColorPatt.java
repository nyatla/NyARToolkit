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

public interface INyARColorPatt extends INyARRgbRaster
{
	/**
	 * ラスタイメージからi_square部分のカラーパターンを抽出して、thisメンバに格納します。
	 * 
	 * @param image
	 * Source raster object.
	 * ----
	 * 抽出元のラスタオブジェクト
	 * @param i_vertexs
	 * Vertexes of the square. Number of element must be 4.
	 * ----
	 * 射影変換元の４角形を構成する頂点群頂群。要素数は4であること。
	 * @return
	 * True if sucessfull; otherwise false.
	 * ----
	 * ラスターの取得に成功するとTRUE/失敗するとFALSE
	 * @throws NyARException
	 */
//	public boolean pickFromRaster(INyARRgbRaster image, NyARSquare i_square) throws NyARException;
	public boolean pickFromRaster(INyARRgbRaster image, NyARIntPoint2d[] i_vertexs) throws NyARException;
}