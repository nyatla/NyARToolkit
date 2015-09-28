/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
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
package jp.nyatla.nyartoolkit.core;

/**
 * NyARToolkitライブラリが生成するExceptionのクラスです。
 * このクラスは、NyARToolkitで発生する例外を通知します。
 */
public class NyARRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタです。
	 * 例外オブジェクトを生成します。
	 */
	public NyARRuntimeException()
	{
		super();
	}
	/**
	 * コンストラクタです。
	 * 例外オブジェクト継承して、例外を生成します。
	 * @param e
	 * 継承する例外オブジェクト
	 */
	public NyARRuntimeException(Exception e)
	{
		super(e);
	}
	/**
	 * コンストラクタです。
	 * メッセージを指定して、例外を生成します。
	 * @param m
	 */
	public NyARRuntimeException(String m)
	{
		super(m);
	}
	/**
	 * ライブラリ開発者向けの関数です。
	 * 意図的に例外を発生するときに、コードに埋め込みます。
	 * @param m
	 * 例外メッセージを指定します。
	 * @throws NyARRuntimeException
	 */
	public static void trap(String m)
	{
		throw new NyARRuntimeException("トラップ:" + m);
	}
	/**
	 * ライブラリ開発者向けの関数です。
	 * "Not Implement!"メッセージを指定して、例外をスローします。
	 * この関数は、NyARToolkitの未実装部分に埋め込みます。
	 * @throws NyARRuntimeException
	 */
	public static void notImplement()
	{
		throw new NyARRuntimeException("Not Implement!");
	}
	/**
	 * ライブラリ開発者向けの関数です。
	 * 関数が使用不能である事を、例外で通知します。
	 * @throws NyARRuntimeException
	 */
	public static void unavailability()
	{
		throw new NyARRuntimeException("unavailability!");
	}
}
