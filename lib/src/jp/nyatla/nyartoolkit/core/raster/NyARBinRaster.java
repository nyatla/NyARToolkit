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
import jp.nyatla.nyartoolkit.*;

/**
 * このクラスは、２値画像を格納するラスタクラスです。
 * 外部バッファ、内部バッファの両方に対応します。
 */
public class NyARBinRaster extends NyARRaster_BasicClass
{
	/** バッファオブジェクト。*/
	protected Object _buf;
	/** バッファオブジェクトがアタッチされていればtrue*/
	protected boolean _is_attached_buffer;
	/**
	 * コンストラクタです。
	 * 画像のサイズパラメータとバッファ形式を指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @param i_raster_type
	 * ラスタのバッファ形式。
	 * {@link NyARBufferType}に定義された定数値を指定してください。指定できる値は、以下の通りです。
	 * <ul>
	 * <li>{@link NyARBufferType#INT2D_BIN_8}
	 * <ul>
	 * @param i_is_alloc
	 * バッファを外部参照にするかのフラグ値。
	 * trueなら内部バッファ、falseなら外部バッファを使用します。
	 * falseの場合、初期のバッファはnullになります。インスタンスを生成したのちに、{@link #wrapBuffer}を使って割り当ててください。
	 * @throws NyARException
	 */
	public NyARBinRaster(int i_width, int i_height,int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,i_raster_type);
		if(!initInstance(this._size,i_raster_type,i_is_alloc)){
			throw new NyARException();
		}
	}
	/**
	 * コンストラクタです。
	 * 画像のサイズパラメータを指定して、{@link NyARBufferType#INT2D_BIN_8}形式のバッファを持つインスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @param i_is_alloc
	 * バッファを外部参照にするかのフラグ値。
	 * trueなら内部バッファ、falseなら外部バッファを使用します。
	 * falseの場合、初期のバッファはnullになります。インスタンスを生成したのちに、{@link #wrapBuffer}を使って割り当ててください。
	 * @throws NyARException
	 */
	public NyARBinRaster(int i_width, int i_height,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.INT1D_BIN_8);
		if(!initInstance(this._size,NyARBufferType.INT1D_BIN_8,i_is_alloc)){
			throw new NyARException();
		}
	}
	/**
	 * コンストラクタです。
	 * 画像のサイズパラメータを指定して、{@link NyARBufferType#INT2D_BIN_8}形式のバッファを持つインスタンスを生成します。
	 * このラスタは、内部参照バッファを持ちます。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @throws NyARException
	 */
	public NyARBinRaster(int i_width, int i_height) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.INT1D_BIN_8);
		if(!initInstance(this._size,NyARBufferType.INT1D_BIN_8,true)){
			throw new NyARException();
		}
	}
	/**
	 * この関数は、インスタンスの初期化シーケンスを実装します。
	 * コンストラクタから呼び出します。
	 * @param i_size
	 * ラスタのサイズ
	 * @param i_buf_type
	 * バッファ形式定数
	 * @param i_is_alloc
	 * 内部バッファ/外部バッファのフラグ
	 * @return
	 * 初期化に成功するとtrue
	 */
	protected boolean initInstance(NyARIntSize i_size,int i_buf_type,boolean i_is_alloc)
	{
		switch(i_buf_type)
		{
			case NyARBufferType.INT1D_BIN_8:
				this._buf = i_is_alloc?new int[i_size.w*i_size.h]:null;
				break;
			default:
				return false;
		}
		this._is_attached_buffer=i_is_alloc;
		return true;
	}
	/**
	 * この関数は、ラスタのバッファへの参照値を返します。
	 * バッファの形式は、コンストラクタに指定した形式と同じです。
	 */
	public Object getBuffer()
	{
		return this._buf;
	}
	/**
	 * この関数は、インスタンスがバッファを所有するかを返します。
	 * 内部参照バッファの場合は、常にtrueです。
	 * 外部参照バッファの場合は、バッファにアクセスする前に、このパラメタを確認してください。
	 */	
	public boolean hasBuffer()
	{
		return this._buf!=null;
	}
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * 外部参照バッファを持つインスタンスでのみ使用できます。内部参照バッファを持つインスタンスでは使用できません。
	 */
	public void wrapBuffer(Object i_ref_buf)
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		this._buf=i_ref_buf;
	}	
}
