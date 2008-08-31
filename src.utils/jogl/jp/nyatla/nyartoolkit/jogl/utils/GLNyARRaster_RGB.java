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
package jp.nyatla.nyartoolkit.jogl.utils;

import javax.media.format.RGBFormat;
import javax.media.opengl.GL;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.raster.TNyRasterType;

/**
 * NyARRaster_RGBにOpenGL用のデータ変換機能を追加したものです。
 */
public class GLNyARRaster_RGB extends JmfNyARRaster_RGB
{
	private byte[] _gl_buf;

	private int _gl_flag;

	public GLNyARRaster_RGB(NyARParam i_param)
	{
		super(i_param.getX(), i_param.getY());
		this._gl_flag = GL.GL_RGB;
		this._gl_buf = new byte[this._size.w * this._size.h * 3];
	}

	public void setBuffer(javax.media.Buffer i_buffer, boolean i_is_reverse) throws NyARException
	{
		//JMFデータでフォーマットプロパティを初期化
		analyzeBufferType((RGBFormat) i_buffer.getFormat());

		byte[] src_buf = (byte[]) i_buffer.getData();
		//GL用のデータを準備
		if (i_is_reverse) {
			int length = this._size.w * 3;
			int src_idx = 0;
			int dest_idx = (this._size.h - 1) * length;
			for (int i = 0; i < this._size.h; i++) {
				System.arraycopy(src_buf, src_idx, this._gl_buf, dest_idx, length);
				src_idx += length;
				dest_idx -= length;
			}
		} else {
			System.arraycopy(src_buf, 0, this._gl_buf, 0, src_buf.length);
		}
	
		//GLのフラグ設定
		switch (this._buffer_type){
		case TNyRasterType.BUFFERFORMAT_BYTE1D_B8G8R8_24:
			this._gl_flag = GL.GL_BGR;
			break;
		case TNyRasterType.BUFFERFORMAT_BYTE1D_R8G8B8_24:
			this._gl_flag = GL.GL_RGB;
			break;
		default:
			throw new NyARException();
		}
		//ref_bufをgl_bufに差し替える
		this._ref_buf = this._gl_buf;
	}

	/**
	 * GLでそのまま描画できるRGBバッファを返す。
	 * @return
	 */
	public byte[] getGLRgbArray()
	{
		return this._ref_buf;
	}

	/**
	 * GL用のRGBバッファのバイト並びタイプを返す。
	 * @return
	 */
	public int getGLPixelFlag()
	{
		return this._gl_flag;
	}
}
