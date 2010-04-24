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

/**このクラスは、単機能のNyARRasterです。
 *
 */
public class NyARRaster extends NyARRaster_BasicClass
{
	protected Object _buf;
	protected int _buf_type;
	/**
	 * バッファオブジェクトがアタッチされていればtrue
	 */
	protected boolean _is_attached_buffer;
	/**
	 * 指定したバッファタイプのラスタを作成します。
	 * @param i_width
	 * @param i_height
	 * @param i_buffer_type
	 * NyARBufferTypeに定義された定数値を指定してください。
	 * @param i_is_alloc
	 * @throws NyARException
	 */
	public NyARRaster(int i_width, int i_height,int i_buffer_type,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,i_buffer_type);
		if(!initInstance(this._size,i_buffer_type,i_is_alloc)){
			throw new NyARException();
		}
		return;
	}	

	public NyARRaster(int i_width, int i_height,int i_buffer_type) throws NyARException
	{
		super(i_width,i_height,i_buffer_type);
		if(!initInstance(this._size,i_buffer_type,true)){
			throw new NyARException();
		}
		return;
	}	
	protected boolean initInstance(NyARIntSize i_size,int i_buf_type,boolean i_is_alloc)
	{
		switch(i_buf_type)
		{
			case NyARBufferType.INT1D_X8R8G8B8_32:
				this._buf=i_is_alloc?new int[i_size.w*i_size.h]:null;
				break;
			default:
				return false;
		}
		this._is_attached_buffer=i_is_alloc;
		return true;
	}
	public Object getBuffer()
	{
		return this._buf;
	}
	/**
	 * インスタンスがバッファを所有するかを返します。
	 * コンストラクタでi_is_allocをfalseにしてラスタを作成した場合、
	 * バッファにアクセスするまえに、バッファの有無をこの関数でチェックしてください。
	 * @return
	 */	
	public boolean hasBuffer()
	{
		return this._buf!=null;
	}
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		this._buf=i_ref_buf;
	}
}