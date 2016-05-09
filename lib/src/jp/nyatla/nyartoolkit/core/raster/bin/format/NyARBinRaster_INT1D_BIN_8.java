/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.raster.bin.format;

import jp.nyatla.nyartoolkit.core.raster.bin.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

public class NyARBinRaster_INT1D_BIN_8 extends NyARBinRaster
{
	protected int[] _buf;
	public NyARBinRaster_INT1D_BIN_8(int i_width,int i_height,boolean i_is_alloc)
	{
		super(i_width,i_height,i_is_alloc);
		this._buf=i_is_alloc ? new int[i_width * i_height] : null;
	}

	@Override
	final public Object getBuffer()
	{
		return this._buf;
	}
	@Override
	final public int getBufferType()
	{
		return NyARBufferType.INT1D_BIN_8;
	}	
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * 外部参照バッファを持つインスタンスでのみ使用できます。内部参照バッファを持つインスタンスでは使用できません。
	 */
	@Override
	final public void wrapBuffer(Object i_buf)
	{
		assert (!this._is_attached_buffer);// バッファがアタッチされていたら機能しない。
		//ラスタの形式は省略。
		this._buf = (int[])i_buf;
	}
	@Override
	final public int[] getPixelSet(int[] i_x, int[] i_y, int i_n, int[] o_buf,int i_st_buf)
	{
		int bp;
		final int w = this._size.w;
		final int[] b = this._buf;
		for (int i = i_n - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * w);
			o_buf[i_st_buf+i] = (b[bp]);
		}
		return o_buf;	
	}
	@Override
	final public int getPixel(int i_x, int i_y)
	{
		final int[] buf = (int[])this._buf;
		return buf[(i_x + i_y * this._size.w)];
	}
	@Override
	final public void setPixel(int i_x, int i_y, int i_gs)
	{
		this._buf[(i_x + i_y * this._size.w)]=i_gs;	
	}
	@Override
	final public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intgs)
	{
		int w=this._size.w;
		int[] r=this._buf;
		for (int i = i_num - 1; i >= 0; i--){
			r[(i_x[i] + i_y[i] * w)]=i_intgs[i];
		}
	}
}
