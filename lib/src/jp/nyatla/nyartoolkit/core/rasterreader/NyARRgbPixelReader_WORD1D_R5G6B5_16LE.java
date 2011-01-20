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

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
/**
 * このクラスは、{@link NyARBufferType#WORD1D_R5G6B5_16LE}形式のラスタバッファに対応する、ピクセルリーダです。
 * この形式は、WindowsMobile等のモバイルデバイスで使われる形式です。
 */
final public class NyARRgbPixelReader_WORD1D_R5G6B5_16LE implements INyARRgbPixelReader
{
	/** 参照する外部バッファ*/
	protected short[] _ref_buf;

	private NyARIntSize _size;
	/**
	 * コンストラクタです。
	 * 参照するラスタのバッファとサイズを指定して、インスタンスを作成します。
	 * @param i_buf
	 * ラスタのバッファオブジェクトの参照値
	 * @param i_size
	 * ラスタのサイズオブジェクトの参照値。
	 */
	public NyARRgbPixelReader_WORD1D_R5G6B5_16LE(short[] i_buf, NyARIntSize i_size)
	{
		this._ref_buf = i_buf;
		this._size = i_size;
	}
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 */
	public void getPixel(int i_x, int i_y, int[] o_rgb)
	{
        short[] buf = this._ref_buf;
        int y = i_y;
        int idx = y * this._size.w + i_x;
        int pixcel =(int)(buf[idx] &0xffff);

        o_rgb[0] = (int)((pixcel & 0xf800) >> 8);//R
        o_rgb[1] = (int)((pixcel & 0x07e0) >> 3);//G
        o_rgb[2] = (int)((pixcel & 0x001f) << 3);//B
		return;
	}
	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
        int stride = this._size.w;
        short[] buf = this._ref_buf;

        for (int i = i_num - 1; i >= 0; i--)
        {
            int idx = i_y[i] * stride + i_x[i];

            int pixcel =(int)(buf[idx] &0xffff);
            o_rgb[i * 3 + 0] = (int)((pixcel & 0xf800) >> 8);//R
            o_rgb[i * 3 + 1] = (int)((pixcel & 0x07e0) >> 3);//G
            o_rgb[i * 3 + 2] = (int)((pixcel & 0x001f) << 3);//B
        }		
		return;
	}
	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	/**
	 * この関数は、機能しません。
	 */
	public void setPixel(int i_x, int i_y, int i_r,int i_g,int i_b) throws NyARException
	{
		NyARException.notImplement();		
	}
	/**
	 * この関数は、機能しません。
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	/**
	 * この関数は、参照しているバッファをi_ref_bufferへ切り替えます。
	 * 通常は、このインスタンスを所有するクラスが使います。ユーザが使うことはありません。
	 * 関数は、入力値のバッファサイズと、型だけを確認します。
	 */	
	public void switchBuffer(Object i_ref_buffer) throws NyARException
	{
		assert(((short[])i_ref_buffer).length>=this._size.w*this._size.h);
		this._ref_buf=(short[])i_ref_buffer;
	}
}