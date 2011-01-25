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
 * このインタフェイスは、２次元ラスタにアクセスする関数を定義します。
 * 二次元ラスタは、任意形式のバッファと、サイズ、バッファ形式を持つオブジェクトです。
 */
public interface INyARRaster
{
	/**
	 * この関数は、ラスタの幅を返します。
	 * 実装クラスでは、ラスタの幅を返す処理を実装してください。
	 * @return
	 * ラスタの幅
	 */
	public int getWidth();
	/**
	 * この関数は、ラスタの高さを返します。
	 * 実装クラスでは、ラスタの幅を返す処理を実装してください。
	 * @return
	 * ラスタの高さ
	 */
	public int getHeight();
	/**
	 * この関数は、ラスタのサイズを格納したオブジェクトの参照値を返します。
	 * 実装クラスでは、サイズオブジェクトの参照値を返す処理を実装してください。
	 * @return
	 * [read only]ラスタサイズの参照値
	 */
	public NyARIntSize getSize();
	/**
	 * この関数は、バッファオブジェクトを返します。
	 * 実装クラスでは、バッファを格納したオブジェクトを返してください。
	 * @return
	 * バッファを格納したオブジェクト。
	 */
	public Object getBuffer();
	/**
	 * この関数は、バッファの画素形式を返します。
	 * 実装クラスでは、{@link #getBuffer}の返すバッファの形式を返してください。
	 * @return
	 * バッファの形式。{@link NyARBufferType}の定義値です。
	 */
	public int getBufferType();
	/**
	 * この関数は、画素形式がi_type_valueであるか、チェックします。
	 * 実装クラスでは、格納しているバッファの画素形式がi_type_valueと等しいかを確認してください。
	 * @param i_type_value
	 * バッファタイプ値。{@link NyARBufferType}の定義値です。
	 * @return
	 * 真偽値。画素形式が一致していればtrue。
	 */
	public boolean isEqualBufferType(int i_type_value);
	/**
	 * この関数は、{@link #getBuffer}がオブジェクトを返せるかを真偽値返します。
	 * 外部参照バッファを使用できるクラスで使います。
	 * 実装クラスでは、{@link #getBuffer}がオブジェクトを返せるかの判定値を返してください。
	 * @return
	 * 真偽値。{@link #getBuffer}が利用可能ならtrue。
	 */
	public boolean hasBuffer();
	/**
	 * この関数は、外部参照バッファをラップして、ラスタのバッファにします。
	 * 実装クラスでは、できる限り整合性チェックをしたうえで、バッファを切り替える処理を実装してください。
	 * この関数は、実装しなくともかまいません。その場合は、{@link NyARException}例外を発生させてください。
	 * @param i_ref_buf
	 * 切り替える外部参照バッファオブジェクト。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException;
}