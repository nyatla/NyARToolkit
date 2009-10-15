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
package jp.nyatla.nyartoolkit.core;

/**
 * ARUint32 arGetVersion(char **versionStringRef); 関数の置き換え
 */
public class NyARVersion
{
	private static final int AR_HEADER_VERSION_MAJOR = 2; // #define
															// AR_HEADER_VERSION_MAJOR
															// 2

	private static final int AR_HEADER_VERSION_MINOR = 72;// #define AR_HEADER_VERSION_MINOR 72

	private static final int AR_HEADER_VERSION_TINY = 0;// #define AR_HEADER_VERSION_TINY 0

	private static final int AR_HEADER_VERSION_BUILD = 0;// #define AR_HEADER_VERSION_BUILD 0

	private static final String AR_HEADER_VERSION_STRING = "2.72.0";// #define AR_HEADER_VERSION_STRING "2.72.0"

	public static final boolean AR_HAVE_HEADER_VERSION_2 = true;// #define AR_HAVE_HEADER_VERSION_2

	public static final boolean AR_HAVE_HEADER_VERSION_2_72 = true;// #define AR_HAVE_HEADER_VERSION_2_72

	public static String getARVersion()
	{
		return AR_HEADER_VERSION_STRING;
	}

	public static int getARVersionInt()
	{
		// Represent full version number (major, minor, tiny, build) in
		// binary coded decimal. N.B: Integer division.
		return (int) (0x10000000 * (AR_HEADER_VERSION_MAJOR / 10))
				+ (int) (0x01000000 * (AR_HEADER_VERSION_MAJOR % 10))
				+ (int) (0x00100000 * (AR_HEADER_VERSION_MINOR / 10))
				+ (int) (0x00010000 * (AR_HEADER_VERSION_MINOR % 10))
				+ (int) (0x00001000 * (AR_HEADER_VERSION_TINY / 10))
				+ (int) (0x00000100 * (AR_HEADER_VERSION_TINY % 10))
				+ (int) (0x00000010 * (AR_HEADER_VERSION_BUILD / 10))
				+ (int) (0x00000001 * (AR_HEADER_VERSION_BUILD % 10));

	}
}
