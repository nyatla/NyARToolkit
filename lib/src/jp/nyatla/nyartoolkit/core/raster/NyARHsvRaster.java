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

/**
 * このクラスは、HSV画像を格納するラスタクラスです。
 * 内部バッファのみに対応します。
 */
public final class NyARHsvRaster extends NyARRaster_BasicClass
{

	private int[] _ref_buf;
	/**
	 * コンストラクタです。ラスタのサイズを指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズです。
	 * @param i_height
	 * ラスタのサイズです。
	 */
	public NyARHsvRaster(int i_width, int i_height)
	{
		//このクラスは外部参照バッファ/形式多重化が使えない簡易実装です。
		super(i_width,i_height,NyARBufferType.INT1D_X7H9S8V8_32);
		this._ref_buf = new int[i_height*i_width];
	}
	/**
	 * この関数は、ラスタのバッファへの参照値を返します。
	 * バッファの形式{@link NyARBufferType#INT1D_X7H9S8V8_32}です。
	 */
	public Object getBuffer()
	{
		return this._ref_buf;
	}
	/**
	 * この関数は、インスタンスがバッファを所有するかを返します。
	 * このクラスでは内部参照バッファのみをサポートするため、常にtrueです。
	 */	
	public boolean hasBuffer()
	{
		return true;
	}
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * 外部参照バッファを持つインスタンスでのみ使用できます。内部参照バッファを持つインスタンスでは使用できません。
	 */	
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		NyARException.notImplement();
	}	
}
