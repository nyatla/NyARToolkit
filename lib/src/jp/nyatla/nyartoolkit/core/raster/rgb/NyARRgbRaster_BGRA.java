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
package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
/**
 * このクラスは、{@link NyARBufferType#BYTE1D_B8G8R8X8_32}形式のバッファを持つラスタです。
 * 外部参照バッファ、内部参照バッファの両方に対応します。
 */
public class NyARRgbRaster_BGRA extends NyARRgbRaster
{
	/**
	 * コンストラクタです。
	 * バッファの参照方法とラスタのサイズを指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタサイズ
	 * @param i_height
	 * ラスタサイズ
	 * @param i_is_alloc
	 * バッファ参照方法値。trueなら内部バッファ、falseなら外部参照バッファです。
	 */
	public NyARRgbRaster_BGRA(int i_width, int i_height,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.BYTE1D_B8G8R8X8_32,i_is_alloc);
	}
	/**
	 * コンストラクタです。
	 * ラスタのサイズを指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタサイズ
	 * @param i_height
	 * ラスタサイズ
	 */
	public NyARRgbRaster_BGRA(int i_width, int i_height) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.BYTE1D_B8G8R8X8_32);
		return;
	}
}
