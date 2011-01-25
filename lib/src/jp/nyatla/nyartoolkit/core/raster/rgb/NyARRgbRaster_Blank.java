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
package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARRgbPixelReader;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、空のラスタを定義します。
 * 空のラスタは、バッファを持たないサイズのみのラスタです。デバックなどに使います。
 */
public class NyARRgbRaster_Blank extends NyARRgbRaster_BasicClass
{
	private class PixelReader implements INyARRgbPixelReader
	{
		public void getPixel(int i_x, int i_y, int[] o_rgb)
		{
			o_rgb[0] = 0;// R
			o_rgb[1] = 0;// G
			o_rgb[2] = 0;// B
			return;
		}

		public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
		{
			for (int i = i_num - 1; i >= 0; i--) {
				o_rgb[i * 3 + 0] = 0;// R
				o_rgb[i * 3 + 1] = 0;// G
				o_rgb[i * 3 + 2] = 0;// B
			}
		}
		public void setPixel(int i_x, int i_y, int i_r,int i_g,int i_b) throws NyARException
		{
			NyARException.notImplement();			
		}
		public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
		{
			NyARException.notImplement();		
		}
		public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
		{
			NyARException.notImplement();		
		}
		public void switchBuffer(Object i_ref_buffer) throws NyARException
		{
			NyARException.notImplement();		
		}		
		
	}

	private INyARRgbPixelReader _reader;
	/**
	 * コンストラクタです。
	 * バッファの参照方法を指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタサイズ
	 * @param i_height
	 * ラスタサイズ
	 */
	public NyARRgbRaster_Blank(int i_width, int i_height)
	{
		super(i_width,i_height,NyARBufferType.NULL_ALLZERO);
		this._reader = new PixelReader();
		return;
	}
	/**
	 * この関数は、画素形式によらない画素アクセスを行うオブジェクトへの参照値を返します。
	 * @return
	 * オブジェクトの参照値
	 * @throws NyARException
	 */	
	public INyARRgbPixelReader getRgbPixelReader() throws NyARException
	{
		return this._reader;
	}
	/**
	 * この関数は、ラスタのバッファへの参照値を返します。
	 * 常にNULLです。
	 */	
	public Object getBuffer()
	{
		return null;
	}
	/**
	 * この関数は、インスタンスがバッファを所有するかを返します。
	 * 内部参照バッファの場合は、常にfalseです。
	 */		
	public boolean hasBuffer()
	{
		return false;
	}
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * このクラスでは使用できません。
	 */	
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		NyARException.notImplement();
	}
}
