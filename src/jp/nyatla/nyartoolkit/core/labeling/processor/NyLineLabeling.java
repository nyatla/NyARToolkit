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
package jp.nyatla.nyartoolkit.core.labeling.processor;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.rasteranalyzer.*;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.raster.*;
/**
 * 1ラインづつラベリングするラベリングプロセッサです。 左上(1,1)を基点に、左と上のピクセルを優先して結合したラベリングを行います。
 * 
 */
public class NyLineLabeling implements INyARLabeling
{


	private TNyARIntSize _dest_size;

	private NyARLabelingImage _out_image;

	public NyLineLabeling()
	{
	}

	public void setThresh(int i_thresh)
	{
	}

	// コンストラクタで作ること
	private int[] wk_reservLineBuffer_buf;

	public void attachDestination(NyARLabelingImage i_destination_image)throws NyARException
	{
		// サイズチェック
		TNyARIntSize size = i_destination_image.getSize();
		this._out_image = i_destination_image;

		// ラインバッファの準備
		if (this.wk_reservLineBuffer_buf == null) {
			this.wk_reservLineBuffer_buf = new int[size.w];
		} else if (this.wk_reservLineBuffer_buf.length < size.w) {
			this.wk_reservLineBuffer_buf = new int[size.w];
		}

		// NyLabelingImageのイメージ初期化(枠書き)
		int[][] img = i_destination_image.getBufferObject();
		for (int i = 0; i < size.w; i++) {
			img[0][i] = 0;
			img[size.h - 1][i] = 0;
		}
		for (int i = 0; i < size.h; i++) {
			img[i][0] = 0;
			img[i][size.w - 1] = 0;
		}

		// サイズ(参照値)を保存
		this._dest_size = size;
	}

	public void labeling(NyARBinRaster i_input_raster) throws NyARException
	{
		int x, y;
		NyARLabelingImage out_image = this._out_image;
/*
		// サイズチェック
		TNyARIntSize in_size = i_input_raster.getRasterSize();
		this._dest_size.isEqualSize(in_size);

		int lxsize = in_size.w;// lxsize = arUtil_c.arImXsize;
		int lysize = in_size.h;// lysize = arUtil_c.arImYsize;
		int[][] label_img = out_image.getImage();

		int[] in_line = this.wk_reservLineBuffer_buf;
		int[] out_line0, out_line1;
		int thresh = i_input_raster.getThreshold();

		// 枠作成はインスタンスを作った直後にやってしまう。
		for (y = 1; y < lysize; y++) {
			i_input_raster.readRow(y, in_line);
			out_line0 = label_img[y];
			out_line1 = label_img[y - 1];
			for (x = 1; x < lxsize; x++) {
				/*
				 * if(in_line[x]<thmap.getThreshold(x,y)){ out_line0[x]=0;
				 * continue; }
				 */
//				out_line0[x] = in_line[x];
//			}
//		}
//		return;
	}
}
