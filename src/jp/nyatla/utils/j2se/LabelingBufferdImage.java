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
package jp.nyatla.utils.j2se;

import java.awt.Graphics;
import java.awt.image.*;
import java.awt.color.*;
import java.awt.*;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.stack.*;


/**
 * bitmapとして利用可能なラベリングイメージです。
 * 
 * @author atla
 * 
 */
public class LabelingBufferdImage extends BufferedImage
{
	public final static int COLOR_125_COLOR = 0;// 125色ラベルモード

	public final static int COLOR_256_MONO = 1;// 64階調モノクロモード

	public final static int COLOR_64_MONO = 2;// 64階調モノクロモード

	public final static int COLOR_32_MONO = 3;// 32階調モノクロモード

	public final static int COLOR_16_MONO = 4;// 16階調モノクロモード

	public final static int COLOR_8_MONO = 5;// 16階調モノクロモード

	private int[] _rgb_table;

	private int _number_of_color;

	/**
	 * i_width x i_heightの大きさのイメージを作成します。
	 * 
	 * @param i_width
	 * @param i_height
	 */
	public LabelingBufferdImage(int i_width, int i_height, int i_color_mode)
	{
		super(i_width, i_height, ColorSpace.TYPE_RGB);
		// RGBテーブルを作成
		switch (i_color_mode) {
		case COLOR_125_COLOR:
			this._rgb_table = new int[125];
			this._number_of_color = 125;
			for (int i = 0; i < 5; i++) {
				for (int i2 = 0; i2 < 5; i2++) {
					for (int i3 = 0; i3 < 5; i3++) {
						this._rgb_table[((i * 5) + i2) * 5 + i3] = ((((i * 63) << 8) | (i2 * 63)) << 8) | (i3 * 63);
					}
				}
			}
			break;
		case COLOR_256_MONO:
			this._rgb_table = new int[256];
			this._number_of_color = 256;
			for (int i = 0; i < 256; i++) {
				this._rgb_table[i] = (i << 16) | (i << 8) | i;
			}
			break;
		case COLOR_64_MONO:
			this._rgb_table = new int[64];
			this._number_of_color = 64;
			for (int i = 0; i < 64; i++) {
				int m = (i * 4);
				this._rgb_table[i] = (m << 16) | (m << 8) | m;
			}
			break;
		case COLOR_32_MONO:
			this._rgb_table = new int[32];
			this._number_of_color = 32;
			for (int i = 0; i < 32; i++) {
				int m = (i * 8);
				this._rgb_table[i] = (m << 16) | (m << 8) | m;
			}
			break;
		case COLOR_16_MONO:
			this._rgb_table = new int[32];
			this._number_of_color = 16;
			for (int i = 0; i < 16; i++) {
				int m = (i * 8);
				this._rgb_table[i] = (m << 16) | (m << 8) | m;
			}
			break;
		}
	}



	public void drawImage(NyARGlayscaleRaster i_raster) throws NyARException
	{
		assert (i_raster.getBufferReader().getBufferType() == INyARBufferReader.BUFFERFORMAT_INT2D_GLAY_8);

		int w = this.getWidth();
		int h = this.getHeight();
		// サイズをチェック
		NyARIntSize size = i_raster.getSize();
		if (size.h > h || size.w > w) {
			throw new NyARException();
		}

		int[][] limg;
		// イメージの描画
		limg = (int[][]) i_raster.getBufferReader().getBuffer();
		for (int i = 0; i < h; i++) {
			for (int i2 = 0; i2 < w; i2++) {
				this.setRGB(i2, i, this._rgb_table[limg[i][i2] % this._number_of_color]);
			}
		}
		return;
	}

	public void drawImage(NyARBinRaster i_raster) throws NyARException
	{
		assert (i_raster.getBufferReader().getBufferType() == INyARBufferReader.BUFFERFORMAT_INT2D_BIN_8);

		int w = this.getWidth();
		int h = this.getHeight();
		// サイズをチェック
		NyARIntSize size = i_raster.getSize();
		if (size.h > h || size.w > w) {
			throw new NyARException();
		}

		int[][] limg;
		// イメージの描画
		limg = (int[][]) i_raster.getBufferReader().getBuffer();
		for (int i = 0; i < h; i++) {
			for (int i2 = 0; i2 < w; i2++) {
				this.setRGB(i2, i, limg[i][i2] > 0 ? 255 : 0);
			}
		}
		return;
	}

	public void overlayData(NyARIntPointStack i_stack)
	{
		int count = i_stack.getLength();
		NyARIntPoint[] items = i_stack.getArray();
		Graphics g = this.getGraphics();
		for (int i = 0; i < count; i++) {
			int x = items[i].x;
			int y = items[i].y;
			g.setColor(Color.red);
			g.drawLine(x - 5, y, x + 5, y);
			g.drawLine(x, y + 5, x, y - 5);
		}
		return;
	}
	public void overlayData(NyARIntRectStack i_stack)
	{
		Color[] c=new Color[]{Color.cyan,Color.red,Color.green};
		int count = i_stack.getLength();
		NyARIntRect[] items = i_stack.getArray();
		Graphics g = this.getGraphics();
		for (int i = 0; i < count; i++) {
			int x = items[i].x;
			int y = items[i].y;
			g.setColor(c[i%1]);
			g.drawRect(x,y,items[i].w,items[i].h);
		}
		return;
	}
}
