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
package jp.nyatla.nyartoolkit.core.rasterreader;
/**
 * このインタフェイスは、画素データを格納するバッファオブジェクト
 * へのアクセス方法と、その形式を定義します。
 *
 */
public interface INyARBufferReader
{
	public static final int BYTE1D =0x00010000;
	public static final int INT2D  =0x00020000;
	public static final int SHORT1D=0x00030000;
	public static final int INT1D  =0x00040000;
	public static final int OBJECT =0x00100000;    //  ID規約
	//  24-31(8)予約
	//  16-27(8)型ID
	//      00:無効/01:byte[]/02:int[][]/03:short[]
	//  08-15(8)ビットフォーマットID
	//      00:24bit/01:32bit/02:16bit
	//  00-07(8)型番号
	//
	/**
	 * RGB24フォーマットで、全ての画素が0
	 */
	public static final int BUFFERFORMAT_NULL_ALLZERO = 0x00000001;

	/**
	 * byte[]で、R8G8B8の24ビットで画素が格納されている。
	 */
	public static final int BUFFERFORMAT_BYTE1D_R8G8B8_24   = BYTE1D|0x0001;
	/**
	 * byte[]で、B8G8R8の24ビットで画素が格納されている。
	 */
	public static final int BUFFERFORMAT_BYTE1D_B8G8R8_24   = BYTE1D|0x0002;
	/**
	 * byte[]で、R8G8B8X8の32ビットで画素が格納されている。
	 */
	public static final int BUFFERFORMAT_BYTE1D_B8G8R8X8_32 = BYTE1D|0x0101;
	/**
	 * byte[]で、X8R8G8B8の32ビットで画素が格納されている。
	 */
	public static final int BUFFERFORMAT_BYTE1D_X8R8G8B8_32 = BYTE1D|0x0102;

	/**
	 * byte[]で、RGB565の16ビット(little/big endian)で画素が格納されている。
	 */
	public static final int BUFFERFORMAT_BYTE1D_R5G6B5_16LE = BYTE1D|0x0201;
    public static final int BUFFERFORMAT_BYTE1D_R5G6B5_16BE = BYTE1D|0x0202;
	/**
	 * short[]で、RGB565の16ビット(little/big endian)で画素が格納されている。
	 */	
    public static final int BUFFERFORMAT_WORD1D_R5G6B5_16LE = SHORT1D|0x0201;
    public static final int BUFFERFORMAT_WORD1D_R5G6B5_16BE = SHORT1D|0x0202;

	
	/**
	 * int[][]で特に値範囲を定めない
	 */
	public static final int BUFFERFORMAT_INT2D        = INT2D|0x0000;
	/**
	 * int[][]で0-255のグレイスケール画像
	 */
	public static final int BUFFERFORMAT_INT2D_GRAY_8 = INT2D|0x0001;
	/**
	 * int[][]で0/1の2値画像
	 * これは、階調値1bitのBUFFERFORMAT_INT2D_GRAY_1と同じです。
	 */
	public static final int BUFFERFORMAT_INT2D_BIN_8  = INT2D|0x0002;

	/**
	 * int[]で特に値範囲を定めない
	 */
	public static final int BUFFERFORMAT_INT1D        = INT1D|0x0000;
	/**
	 * int[]で0-255のグレイスケール画像
	 */
	public static final int BUFFERFORMAT_INT1D_GRAY_8 = INT1D|0x0001;
	/**
	 * int[]で0/1の2値画像
	 * これは、階調1bitのBUFFERFORMAT_INT1D_GRAY_1と同じです。
	 */
	public static final int BUFFERFORMAT_INT1D_BIN_8  = INT1D|0x0002;
	
	
	/**
	 * int[]で、XRGB32の32ビットで画素が格納されている。
	 */	
    public static final int BUFFERFORMAT_INT1D_X8R8G8B8_32=INT1D|0x0102;

	/**
	 * H:9bit(0-359),S:8bit(0-255),V(0-255)
	 */
	public static final int BUFFERFORMAT_INT1D_X7H9S8V8_32=INT1D|0x0103;
    
	public static final int BUFFERFORMAT_OBJECT_Javat= OBJECT|0x0100;
	public static final int BUFFERFORMAT_OBJECT_CS= OBJECT|0x0200;
	public static final int BUFFERFORMAT_OBJECT_AS3= OBJECT|0x0300;

	/**
	 * ActionScript3のBitmapDataを格納するラスタ
	 */

	public static final int BUFFERFORMAT_OBJECT_AS3_BitmapData= BUFFERFORMAT_OBJECT_AS3|0x01;

	
	
	/**
	 * バッファオブジェクトを返します。
	 * @return
	 */
	public Object getBuffer();
	/**
	 * バッファオブジェクトの形式を返します。
	 * @return
	 */
	public int getBufferType();
	/**
	 * バッファオブジェクトの形式が、i_type_valueにが一致するか返します。
	 * @param i_type_value
	 * @return
	 */
	public boolean isEqualBufferType(int i_type_value);
}
