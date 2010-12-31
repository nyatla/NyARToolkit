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
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * NyARRasterインタフェイスの基本関数/メンバを実装したクラス
 * 
 * 
 */
public abstract class NyARRgbRaster_BasicClass implements INyARRgbRaster
{
	final protected NyARIntSize _size;
	private int _buffer_type;
	protected NyARRgbRaster_BasicClass(int i_width,int i_height,int i_buffer_type)
	{
		this._size= new NyARIntSize(i_width,i_height);
		this._buffer_type=i_buffer_type;
	}
	final public int getWidth()
	{
		return this._size.w;
	}
	final public int getHeight()
	{
		return this._size.h;
	}

	final public NyARIntSize getSize()
	{
		return this._size;
	}
	final public int getBufferType()
	{
		return _buffer_type;
	}
	final public boolean isEqualBufferType(int i_type_value)
	{
		return this._buffer_type==i_type_value;
	}
	/**
	 * ラスタのコピーを実行します。
	 * この関数は暫定です。低速なので注意してください。
	 * @param i_input
	 * @param o_output
	 * @throws NyARException 
	 */
	public static void copy(INyARRgbRaster i_input,INyARRgbRaster o_output) throws NyARException
	{
		assert(i_input.getSize().isEqualSize(o_output.getSize()));
		int width=i_input.getWidth();
		int height=i_input.getHeight();
		int[] rgb=new int[3];
		INyARRgbPixelReader inr=i_input.getRgbPixelReader();
		INyARRgbPixelReader outr=o_output.getRgbPixelReader();
		for(int i=height-1;i>=0;i--){
			for(int i2=width-1;i2>=0;i2--){
				inr.getPixel(i2,i,rgb);
				outr.setPixel(i2,i,rgb);
			}
		}
	}
	
}
