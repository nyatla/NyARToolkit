/* 
 * PROJECT: NyARToolkit JOGL utilities.
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
package jp.nyatla.nyartoolkit.jogl.utils;


import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import javax.media.format.*;

/**
 * NyARRaster_RGBにOpenGL用のデータ変換機能を追加したものです。
 */
public class GLNyARRaster_RGB extends JmfNyARRaster_RGB
{
	private int _gl_flag;

	public GLNyARRaster_RGB(NyARParam i_param,VideoFormat i_format) throws NyARException
	{
		super(i_param.getScreenSize(),i_format);
		switch(this._reader.getBufferType()){
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24:
			this._gl_flag = GL.GL_BGR;
			break;
		case INyARBufferReader.BUFFERFORMAT_BYTE1D_R8G8B8_24:
			this._gl_flag = GL.GL_RGB;
			break;
		default:
			throw new NyARException();
		}
		return;
	}
	/**
	 * GLでそのまま描画できるRGBバッファを返す。
	 * 
	 * @return
	 */
	public byte[] getGLRgbArray()
	{
		return (byte[])this._reader.getBuffer();
	}

	/**
	 * GL用のRGBバッファのバイト並びタイプを返す。
	 * 
	 * @return
	 */
	public int getGLPixelFlag()
	{
		return this._gl_flag;
	}
}
