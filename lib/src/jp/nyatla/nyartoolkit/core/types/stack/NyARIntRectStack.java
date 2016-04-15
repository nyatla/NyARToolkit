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
package jp.nyatla.nyartoolkit.core.types.stack;


import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
/**
 * このクラスは、{@link NyARIntRect}型の可変長配列です。
 */
public class NyARIntRectStack extends NyARObjectStack<NyARIntRect>
{
	/**
	 * コンストラクタです。
	 * 配列の最大長さを指定して、インスタンスを生成します。
	 * @param i_length
	 * 配列の最大長さ
	 * @throws NyARRuntimeException
	 */	
	public NyARIntRectStack(int i_length)
	{
		super(i_length,NyARIntRect.class);
	}
	/**
	 * この関数は、配列要素を作成します。
	 */
	@Override	
	protected NyARIntRect createElement()
	{
		return new NyARIntRect();
	}
	
}
