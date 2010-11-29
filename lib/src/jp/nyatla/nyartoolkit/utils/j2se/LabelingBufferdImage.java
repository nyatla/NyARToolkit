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
package jp.nyatla.nyartoolkit.utils.j2se;

import java.awt.Graphics;
import java.awt.image.*;
import java.awt.color.*;
import java.awt.*;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabel;

/**
 * bitmapとして利用可能なラベリングイメージです。
 * 
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

	private int[] _rgb_table_125;


	/**
	 * i_width x i_heightの大きさのイメージを作成します。
	 * 
	 * @param i_width
	 * @param i_height
	 */
	public LabelingBufferdImage(int i_width, int i_height)
	{
		super(i_width, i_height, ColorSpace.TYPE_RGB);
		// RGBテーブルを作成
		this._rgb_table_125 = new int[125];
		for (int i = 0; i < 5; i++) {
			for (int i2 = 0; i2 < 5; i2++) {
				for (int i3 = 0; i3 < 5; i3++) {
					this._rgb_table_125[((i * 5) + i2) * 5 + i3] = ((((i * 63) << 8) | (i2 * 63)) << 8) | (i3 * 63);
				}
			}
		}
/*		case COLOR_256_MONO:
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
		}*/
	}



	public void drawImage(NyARGrayscaleRaster i_raster) throws NyARException
	{
		assert (i_raster.getBufferType() == NyARBufferType.INT1D_GRAY_8);

		int w = this.getWidth();
		int h = this.getHeight();
		// サイズをチェック
		NyARIntSize size = i_raster.getSize();
		if (size.h > h || size.w > w) {
			throw new NyARException();
		}

		int[] limg;
		// イメージの描画
		limg = (int[]) i_raster.getBuffer();
		for (int i = 0; i < h; i++) {
			for (int i2 = 0; i2 < w; i2++) {
				this.setRGB(i2, i,limg[i*w+i2]);
			}
		}
		return;
	}
	/**
	 * バイナリラスタ
	 * @param i_raster
	 * @throws NyARException
	 */
	public void drawImage(NyARBinRaster i_raster) throws NyARException
	{
		assert (i_raster.getBufferType() == NyARBufferType.INT1D_BIN_8);

		int w = this.getWidth();
		int h = this.getHeight();
		// サイズをチェック
		NyARIntSize size = i_raster.getSize();
		if (size.h > h || size.w > w) {
			throw new NyARException();
		}

		int[] limg;
		// イメージの描画
		limg = (int[]) i_raster.getBuffer();
		for (int i = 0; i < h; i++) {
			for (int i2 = 0; i2 < w; i2++) {
				this.setRGB(i2, i, limg[i*w+i2] > 0 ? 255 : 0);
			}
		}
		return;
	}
		
	/**
	 * ラベリングイメージを書く
	 * @param i_raster
	 * @throws NyARException
	 */
	public void drawLabel(NyARLabelingImage i_image) throws NyARException
	{
		int w = this.getWidth();
		int h = this.getHeight();
		// サイズをチェック
		NyARIntSize size = i_image.getSize();
		if (size.h > h || size.w > w) {
			throw new NyARException();
		}
		int[] index_array=i_image.getIndexArray();

		int[] limg;
		// イメージの描画
		limg = (int[]) i_image.getBuffer();
		for (int i = 0; i < h; i++) {
			for (int i2 = 0; i2 < w; i2++) {
				int t=limg[i*w+i2]-1;
				if(t<0){
					t=0;
				}else{
					t=index_array[t];
				}
				this.setRGB(i2, i,_rgb_table_125[t% _rgb_table_125.length]);
			}
		}
		return;
	}
	/**
	 * 
	 * @param i_stack
	 */

	public void overlayData(NyARLabelingLabel i_label)
	{
		Graphics g = this.getGraphics();
		g.setColor(Color.red);
		g.drawRect(i_label.clip_l,i_label.clip_t,i_label.clip_r-i_label.clip_l,i_label.clip_b-i_label.clip_t);
		return;
	}	
	/**
	 * 
	 * @param i_stack
	 */

	public void overlayData(NyARIntPointStack i_stack)
	{
		int count = i_stack.getLength();
		NyARIntPoint2d[] items = i_stack.getArray();
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
