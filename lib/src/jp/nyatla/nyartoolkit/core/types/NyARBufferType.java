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
 * Copyright (C)2008-2010 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.types;

import jp.nyatla.nyartoolkit.core.raster.*;
/**
 * このクラスは、バッファの形式を表す定数を定義します。
 * 定数は、主に{@link INyARRaster}で使用する画素形式として使います。
 * バッファ形式定数は32bitの値で、フィールドの組合せで定義しています。
 * ユーザは、定数からバッファの構造を知ることができます。
 * <pre>
 * <table>
 * <tr><td>ビットイールド(ビット幅)</td><td>カテゴリ</td><td>備考</td></tr>
 * <tr><td>24-31(8)</td><td>予約</td><td></td></tr>
 * <tr><td>16-27(8)</td><td>型ID</td><td>00:無効/01:byte[]/02:int[][]/03:short[]</td></tr>
 * <tr><td>08-15(8)</td><td>ビットフォーマットID</td><td>00:24bit/01:32bit/02:16bit</td></tr>
 * <tr><td>00-07(8)</td><td>型番号</td><td></td></tr>
 * </table>
 * </pre>
 */
public class NyARBufferType
{
	private static final int T_BYTE1D =0x00010000;
	private static final int T_INT2D  =0x00020000;
	private static final int T_SHORT1D=0x00030000;
	private static final int T_INT1D  =0x00040000;
	private static final int T_OBJECT =0x00100000;
	private static final int T_USER   =0x00FF0000;
	//
	
	//
	//特殊な定数
	//
	
	/**　全ての画素が0。バッファオブジェクトは常にNULL。
	 */
	public static final int NULL_ALLZERO = 0x00000001;
	
	/** ユーザ定義のバッファ型。USER_DEFINE + (0x0000~0xFFFF)
	 * 実験等に使ってください。
	 */
	public static final int USER_DEFINE  = T_USER;
	
	//
	//byte形式
	//

	/** RGB形式。byte[3]で、R8G8B8の24ビットで画素が格納されている。
	 */
	public static final int BYTE1D_R8G8B8_24   = T_BYTE1D|0x0001;
	/** RGB形式。 byte[3]で、B8G8R8の24ビットの画素形式。
	 */
	public static final int BYTE1D_B8G8R8_24   = T_BYTE1D|0x0002;
	/** RGB形式。byte[4]で、R8G8B8X8の32ビットの画素形式。
	 */
	public static final int BYTE1D_B8G8R8X8_32 = T_BYTE1D|0x0101;
	/** RGB形式。byte[4]で、X8R8G8B8の32ビットの画素形式。
	 */
	public static final int BYTE1D_X8R8G8B8_32 = T_BYTE1D|0x0102;

	/**　RGB形式。byte[2]で、RGB565の16ビット(little endian)の画素形式。
	 */
	public static final int BYTE1D_R5G6B5_16LE = T_BYTE1D|0x0201;
	/**　RGB形式。byte[2]で、RGB565の16ビット(big endian)の画素形式。
	 */
    public static final int BYTE1D_R5G6B5_16BE = T_BYTE1D|0x0202;
	/**　RGB形式。short[1]で、RGB565の16ビット(little endian)の画素形式。
	 */	
    public static final int WORD1D_R5G6B5_16LE = T_SHORT1D|0x0201;
	/**　RGB形式。short[1]で、RGB565の16ビット(big endian)の画素形式。
	 */
    public static final int WORD1D_R5G6B5_16BE = T_SHORT1D|0x0202;

    //
    //int[][]形式
    //
	
	/**　int値形式。int[1][1]で、1pixel=1画素のフォーマット。
	 */
	public static final int INT2D        = T_INT2D|0x0000;
	/** フレースケール形式。int[1][1]で8itの(0-255)のグレイスケール画像
	 */
	public static final int INT2D_GRAY_8 = T_INT2D|0x0001;
	/** 二値形式。int[1][1]で、0 or 1の2値画像
	 */
	public static final int INT2D_BIN_8  = T_INT2D|0x0002;

	//
    //int[]形式
    //
	
	/** int値形式。int[1]で、1pixel=1画素のフォーマット。
	 */
	public static final int INT1D        = T_INT1D|0x0000;
	/** フレースケール形式。int[1]で8itの(0-255)のグレイスケール画像
	 */
	public static final int INT1D_GRAY_8 = T_INT1D|0x0001;
	/** 二値形式。int[1]で、0 or 1の2値画像
	 */
	public static final int INT1D_BIN_8  = T_INT1D|0x0002;
	
	/**　RGB形式。int[1]で、XRGB32の32ビットの画素形式。
	 *  (エンディアンはプラットフォーム依存。)
	 */
    public static final int INT1D_X8R8G8B8_32=T_INT1D|0x0102;

	/** HSV形式。int[1]で、H:9bit(0-359),S:8bit(0-255),V(0-255)の画素形式
	 */
	public static final int INT1D_X7H9S8V8_32=T_INT1D|0x0103;
    
	//
	//プラットフォーム固有形式
	//

	/** Javaプラットフォーム固有オブジェクトのドメインです。*/
	public static final int OBJECT_Java= T_OBJECT|0x0100;
	/** C#プラットフォーム固有オブジェクトのドメインです。*/
	public static final int OBJECT_CS  = T_OBJECT|0x0200;
	/** AS3プラットフォーム固有オブジェクトのドメインです。*/
	public static final int OBJECT_AS3 = T_OBJECT|0x0300;
	
	/**
	 * RGB形式。バッファは、JavaのBufferedImage型オブジェクト
	 */
	public static final int OBJECT_Java_BufferedImage= OBJECT_Java|0x01;
	
	
	/**
	 * RGB形式。バッファは、ActionScript3のBitmapData型オブジェクト
	 */
	public static final int OBJECT_AS3_BitmapData= OBJECT_AS3|0x01;

}
