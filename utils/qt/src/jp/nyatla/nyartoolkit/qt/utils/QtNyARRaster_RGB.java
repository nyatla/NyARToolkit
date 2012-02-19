/* 
 * PROJECT: NyARToolkit Quicktime utilities.
 * --------------------------------------------------------------------------------
 * Copyright (C)2008 arc@dmz
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
 *	
 *	<arc(at)digitalmuseum.jp>
 * 
 */
package jp.nyatla.nyartoolkit.qt.utils;


import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;

/**
 * RGB形式のbyte配列をラップするNyARRasterです。
 * 保持したデータからBufferedImageを出力する機能も持ちます。
 */
public class QtNyARRaster_RGB extends NyARBufferedImageRaster
{
	/**
	 * QuickTimeオブジェクトからイメージを取得するラスタオブジェクトを作ります。
	 * この
	 * @param i_width
	 * @param i_height
	 * @throws NyARException 
	 */
	public QtNyARRaster_RGB(int i_width, int i_height) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.BYTE1D_R8G8B8_24,true);
	}
	public void setQtImage(byte[] i_img)
	{
		System.arraycopy(i_img,0,(byte[])this._buf,0,((byte[])this._buf).length);
	}
	/**
	 * この関数は使えません。{@link #setQtImage}を使用します。
	 */
	final public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		throw new NyARException();
	}
}
