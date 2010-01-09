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
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**このクラスは、単機能のNyARRasterです。
 * 特定タイプのバッファをラップする、INyARBufferReaderインタフェイスを提供します。
 *
 */
public final class NyARRaster extends NyARRaster_BasicClass
{
	private NyARBufferReader _reader;
	public INyARBufferReader getBufferReader()
	{
		return this._reader;
	}	
	public NyARRaster(NyARIntSize i_size,int i_buf_type) throws NyARException
	{
		super(i_size);
		init(i_size,i_buf_type);
		return;
	}
	private void init(NyARIntSize i_size,int i_buf_type) throws NyARException
	{
		Object buf;
		switch(i_buf_type)
		{
			case INyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32:
				buf=new int[i_size.w*i_size.h];
				break;
			default:
				throw new NyARException();
		}
		this._reader=new NyARBufferReader(buf,i_buf_type);
	}	
}