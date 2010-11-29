/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * 色調テーブルを使用したフィルターです。
 * 色調テーブルクラスのベースクラスです。
 */
public class NyARRasterFilter_CustomToneTable implements INyARRasterFilter
{
	protected final int[] table=new int[256];
	private IdoFilterImpl _dofilterimpl;
	protected NyARRasterFilter_CustomToneTable(int i_raster_type) throws NyARException
	{
		switch (i_raster_type) {
		case NyARBufferType.INT1D_GRAY_8:
			this._dofilterimpl=new IdoFilterImpl_INT1D_GRAY_8();
			break;
		default:
			throw new NyARException();
		}
		this._dofilterimpl._table_ref=this.table;
	}
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._dofilterimpl.doFilter(i_input,i_output,i_input.getSize());
	}
	
	private abstract class IdoFilterImpl
	{
		public int[] _table_ref;
		public abstract void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException;
		
	}
	private class IdoFilterImpl_INT1D_GRAY_8 extends IdoFilterImpl
	{
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
		{
			assert(		i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			int[] in_buf = (int[]) i_input.getBuffer();
			for(int i=i_size.h*i_size.w-1;i>=0;i--)
			{
				out_buf[i]=this._table_ref[in_buf[i]];
			}
			return;
		}
	}
}
