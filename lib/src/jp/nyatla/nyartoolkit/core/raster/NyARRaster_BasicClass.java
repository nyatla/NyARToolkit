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

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、ラスタクラスの基本処理を実装します。
 */
public abstract class NyARRaster_BasicClass implements INyARRaster
{
	protected final NyARIntSize _size;
	protected int _buffer_type;
	/**
	 * コンストラクタです。
	 * メンバ変数を初期化して、インスタンスを生成します。
	 * @param i_width
	 * ラスタの幅に設定する値
	 * @param i_height
	 * ラスタの高さに設定する値
	 * @param i_buffer_type
	 * バッファタイプ値に設定する値
	 */
	protected NyARRaster_BasicClass(int i_width,int i_height,int i_buffer_type)
	{
		this._size= new NyARIntSize(i_width,i_height);
		this._buffer_type=i_buffer_type;
	}
	/**
	 * この関数は、ラスタの幅を返します。
	 */
	public final int getWidth()
	{
		return this._size.w;
	}
	/**
	 * この関数は、ラスタの高さを返します。
	 */
	final public int getHeight()
	{
		return this._size.h;
	}
	/**
	 * この関数は、ラスタのサイズを格納したオブジェクトを返します。
	 */
	final public NyARIntSize getSize()
	{
		return this._size;
	}
	/**
	 * この関数は、ラスタのバッファへの参照値を返します。
	 * バッファの形式は、コンストラクタに指定した形式と同じです。
	 */	
	final public int getBufferType()
	{
		return _buffer_type;
	}
	/**
	 * この関数は、ラスタの幅を返します。
	 */
	final public boolean isEqualBufferType(int i_type_value)
	{
		return this._buffer_type==i_type_value;
	}
}
