/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.raster;

import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;

public abstract class NyARRaster
{
    //
    //定数
    //
    
    //getBufferType関数の返すバッファフォーマット

    /**
     * RGB24フォーマットで、全ての画素が0
     */
    public static final int BUFFERFORMAT_NULL_ALLZERO=0x0001;
    /**
     * byte[]で、R8G8B8の24ビットで画素が格納されている。
     */
    public static final int BUFFERFORMAT_BYTE_R8G8B8_24=0x0101;
    /**
     * byte[]で、B8G8R8の24ビットで画素が格納されている。
     */
    public static final int BUFFERFORMAT_BYTE_B8G8R8_24=0x0102;
    /**
     * byte[]で、R8G8B8X8の32ビットで画素が格納されている。
     */
    public static final int BUFFERFORMAT_BYTE_B8G8R8X8_32=0x0201;

    public static final int BUFFERFORMAT_INT2D=0x0301;
    
    //
    //abstract関数
    //
    
    /**
     * 1ピクセルをint配列にして返します。
     * @param i_x
     * @param i_y
     * @param i_rgb
     */
    public abstract void getPixel(int i_x,int i_y,int[] i_rgb);
    /**
     * 複数のピクセル値をi_rgbへ返します。
     * @param i_x
     * xのインデックス配列
     * @param i_y
     * yのインデックス配列
     * @param i_num
     * 返すピクセル値の数
     * @param i_rgb
     * ピクセル値を返すバッファ
     */
    public abstract void getPixelSet(int[] i_x,int i_y[],int i_num,int[] o_rgb);
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract TNyARIntSize getSize();
    /**
     * バッファオブジェクトを返します。
     * @return
     */
    public abstract Object getBufferObject();
    /**
     * バッファオブジェクトのタイプを返します。
     * @return
     */
    public abstract int getBufferType() throws NyARException;
}



