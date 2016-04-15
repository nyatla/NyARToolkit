/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.marker.nft.fset;

import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

/**
 * byte配列を扱うためのテンポラリラスタ。
 * @todo imgをint配列化したら削除すること最終的には削除する。
 */
public class TemplateSourceRaster extends NyARGrayscaleRaster
{
	protected int[] _buf;
	public TemplateSourceRaster(int i_width,int i_height,int[] i_buf)
	{
		super(i_width,i_height,false);
		this._buf=i_buf;
	}

	@Override
	final public Object getBuffer()
	{
		return this._buf;
	}
	@Override
	final public int getBufferType()
	{
		return NyARBufferType.BYTE1D_GRAY_8;
	}	
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * 外部参照バッファを持つインスタンスでのみ使用できます。内部参照バッファを持つインスタンスでは使用できません。
	 */
	@Override
	final public void wrapBuffer(Object i_buf)
	{
		throw new UnsupportedOperationException();
	}
	@Override
	final public int[] getPixelSet(int[] i_x, int[] i_y, int i_n, int[] o_buf,int i_st_buf)
	{
		throw new UnsupportedOperationException();
	}
	@Override
	final public int getPixel(int i_x, int i_y)
	{
		final int[] buf = (int[])this._buf;
		return buf[(i_x + i_y * this._size.w)] &0xff;
	}
	@Override
	final public void setPixel(int i_x, int i_y, int i_gs)
	{
		throw new UnsupportedOperationException();
	}
	@Override
	final public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intgs)
	{
		throw new UnsupportedOperationException();
	}
}
