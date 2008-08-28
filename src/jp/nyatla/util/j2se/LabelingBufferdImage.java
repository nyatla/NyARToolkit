package jp.nyatla.util.j2se;

import java.awt.Graphics;
import java.awt.image.*;
import java.awt.color.*;
import java.awt.*;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;

import jp.nyatla.nyartoolkit.core.raster.*;

/**
 * bitmapとして利用可能なラベリングイメージです。
 * 
 * @author atla
 * 
 */
public class LabelingBufferdImage extends BufferedImage {
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
    public LabelingBufferdImage(int i_width, int i_height, int i_color_mode) {
	super(i_width, i_height, ColorSpace.TYPE_RGB);
	// RGBテーブルを作成
	switch (i_color_mode) {
	case COLOR_125_COLOR:
	    this._rgb_table = new int[125];
	    this._number_of_color = 125;
	    for (int i = 0; i < 5; i++) {
		for (int i2 = 0; i2 < 5; i2++) {
		    for (int i3 = 0; i3 < 5; i3++) {
			this._rgb_table[((i * 5) + i2) * 5 + i3] = ((((i * 63) << 8) | (i2 * 63)) << 8)
				| (i3 * 63);
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

    /**
     * ラベリングイメージをセットします。
     * 
     * @param i_labeling_image
     * @throws NyARException
     */
    public void drawImage(NyARLabelingImage i_labeling_image) throws NyARException
    {
	int w = this.getWidth();
	int h = this.getHeight();

	// サイズをチェック
	TNyARIntSize size = i_labeling_image.getSize();
	if (size.h > h || size.w > w) {
	    throw new NyARException();
	}

	// イメージの描画
	int[][] limg = i_labeling_image.getImage();
	for (int i = 0; i < h; i++) {
	    for (int i2 = 0; i2 < w; i2++) {
		this.setRGB(i2, i, this._rgb_table[limg[i][i2]
			% this._number_of_color]);
	    }
	}
    }

    public void drawImage(NyARGlayscaleRaster i_raster) throws NyARException
    {
	assert (i_raster.getBufferType() == TNyRasterType.BUFFERFORMAT_INT2D_GLAY_8);

	int w = this.getWidth();
	int h = this.getHeight();
	// サイズをチェック
	TNyARIntSize size = i_raster.getSize();
	if (size.h > h || size.w > w) {
	    throw new NyARException();
	}

	int[][] limg;
	// イメージの描画
	limg = (int[][]) i_raster.getBufferObject();
	for (int i = 0; i < h; i++) {
	    for (int i2 = 0; i2 < w; i2++) {
		this.setRGB(i2, i, this._rgb_table[limg[i][i2]
			% this._number_of_color]);
	    }
	}
	return;
    }

    public void drawImage(NyARBinRaster i_raster) throws NyARException
    {
	assert (i_raster.getBufferType() == TNyRasterType.BUFFERFORMAT_INT2D_GLAY_8);

	int w = this.getWidth();
	int h = this.getHeight();
	// サイズをチェック
	TNyARIntSize size = i_raster.getSize();
	if (size.h > h || size.w > w) {
	    throw new NyARException();
	}

	int[][] limg;
	//イメージの描画
	limg = (int[][]) i_raster.getBufferObject();
	for (int i = 0; i < h; i++) {
	    for (int i2 = 0; i2 < w; i2++) {
		this.setRGB(i2, i, this._rgb_table[limg[i][i2]
			% this._number_of_color]);
	    }
	}
	return;
    }
}
