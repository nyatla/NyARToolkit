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
package jp.nyatla.nyartoolkit.core.raster;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;


public interface INyARRaster
{
	public int getWidth();
	public int getHeight();
	public NyARIntSize getSize();
	/**
	 * バッファオブジェクトを返します。
	 * @return
	 */
	public Object getBuffer();
	/**
	 * バッファオブジェクトのタイプを返します。
	 * @return
	 */
	public int getBufferType();
	/**
	 * バッファのタイプがi_type_valueであるか、チェックします。
	 * この値は、NyARBufferTypeに定義された定数値です。
	 * @param i_type_value
	 * @return
	 */
	public boolean isEqualBufferType(int i_type_value);
	/**
	 * getBufferがオブジェクトを返せるかの真偽値です。
	 * @return
	 */
	public boolean hasBuffer();
	/**
	 * i_ref_bufをラップします。できる限り整合性チェックを行います。
	 * バッファの再ラッピングが可能な関数のみ、この関数を実装してください。
	 * @param i_ref_buf
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException;
}