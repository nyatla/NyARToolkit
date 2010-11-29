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
package jp.nyatla.nyartoolkit.core.labeling.artoolkit;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * ARToolKit互換のラベリングクラスです。 ARToolKitと同一な評価結果を返します。
 * 
 */
final public class NyARLabeling_ARToolKit
{
	private static final int WORK_SIZE = 1024 * 32;// #define WORK_SIZE 1024*32

	private final NyARWorkHolder work_holder = new NyARWorkHolder(WORK_SIZE);


	/**
	 * static ARInt16 *labeling2( ARUint8 *image, int thresh,int *label_num, int **area, double **pos, int **clip,int **label_ref, int LorR ) 関数の代替品
	 * ラスタimageをラベリングして、結果を保存します。 Optimize:STEP[1514->1493]
	 * 
	 * @param i_raster
	 * @throws NyARException
	 */
	public int labeling(NyARBinRaster i_raster,NyARLabelingImage o_destination) throws NyARException
	{
		assert(i_raster.getBufferType()==NyARBufferType.INT1D_BIN_8);
		int label_img_ptr1, label_pixel;
		int i, j;
		int n, k; /* work */
		
		// サイズチェック
		NyARIntSize in_size = i_raster.getSize();
		assert(o_destination.getSize().isEqualSize(in_size));

		final int lxsize = in_size.w;// lxsize = arUtil_c.arImXsize;
		final int lysize = in_size.h;// lysize = arUtil_c.arImYsize;
		final int[] label_img = (int[]) o_destination.getBuffer();

		// 枠作成はインスタンスを作った直後にやってしまう。

		// ラベリング情報のリセット（ラベリングインデックスを使用）
		o_destination.reset(true);

		int[] label_idxtbl = o_destination.getIndexArray();
		int[] raster_buf = (int[]) i_raster.getBuffer();

		int[] work2_pt;
		int wk_max = 0;

		int pixel_index;
		int[][] work2 = this.work_holder.work2;

		// [1,1](ptr0)と、[0,1](ptr1)のインデクス値を計算する。
		for (j = 1; j < lysize - 1; j++) {// for (int j = 1; j < lysize - 1;j++, pnt += poff*2, pnt2 += 2) {
			pixel_index = j * lxsize + 1;
			label_img_ptr1 = pixel_index - lxsize;// label_img_pt1 = label_img[j - 1];
			for (i = 1; i < lxsize - 1; i++, pixel_index++, label_img_ptr1++) {// for(int i = 1; i < lxsize-1;i++, pnt+=poff, pnt2++) {
				// RGBの合計値が閾値より小さいかな？
				if (raster_buf[pixel_index] != 0) {
					label_img[pixel_index] = 0;// label_img_pt0[i] = 0;// *pnt2 = 0;
				} else {
					// pnt1 = ShortPointer.wrap(pnt2, -lxsize);//pnt1 =&(pnt2[-lxsize]);
					if (label_img[label_img_ptr1] > 0) {// if (label_img_pt1[i] > 0) {// if( *pnt1 > 0 ) {
						label_pixel = label_img[label_img_ptr1];// label_pixel = label_img_pt1[i];// *pnt2 = *pnt1;

						work2_pt = work2[label_pixel - 1];
						work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
						work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
						work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
						work2_pt[6] = j;// work2[((*pnt2)-1)*7+6] = j;
					} else if (label_img[label_img_ptr1 + 1] > 0) {// } else if (label_img_pt1[i + 1] > 0) {// }else if(*(pnt1+1) > 0 ) {
						if (label_img[label_img_ptr1 - 1] > 0) {// if (label_img_pt1[i - 1] > 0) {// if( *(pnt1-1) > 0 ) {
							label_pixel = label_idxtbl[label_img[label_img_ptr1 + 1] - 1];// m = label_idxtbl[label_img_pt1[i + 1] - 1];// m
																							// =work[*(pnt1+1)-1];
							n = label_idxtbl[label_img[label_img_ptr1 - 1] - 1];// n = label_idxtbl[label_img_pt1[i - 1] - 1];// n =work[*(pnt1-1)-1];
							if (label_pixel > n) {
								// wk=IntPointer.wrap(work, 0);//wk = &(work[0]);
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == label_pixel) {// if( *wk == m )
										label_idxtbl[k] = n;// *wk = n;
									}
								}
								label_pixel = n;// *pnt2 = n;
							} else if (label_pixel < n) {
								// wk=IntPointer.wrap(work,0);//wk = &(work[0]);
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == n) {// if( *wk == n ){
										label_idxtbl[k] = label_pixel;// *wk = m;
									}
								}
							}
							work2_pt = work2[label_pixel - 1];
							work2_pt[0]++;
							work2_pt[1] += i;
							work2_pt[2] += j;
							work2_pt[6] = j;
						} else if ((label_img[pixel_index - 1]) > 0) {// } else if ((label_img_pt0[i - 1]) > 0) {// }else if(*(pnt2-1) > 0) {
							label_pixel = label_idxtbl[label_img[label_img_ptr1 + 1] - 1];// m = label_idxtbl[label_img_pt1[i + 1] - 1];// m =work[*(pnt1+1)-1];
							n = label_idxtbl[label_img[pixel_index - 1] - 1];// n = label_idxtbl[label_img_pt0[i - 1] - 1];// n =work[*(pnt2-1)-1];
							if (label_pixel > n) {
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == label_pixel) {// if( *wk == m ){
										label_idxtbl[k] = n;// *wk = n;
									}
								}
								label_pixel = n;// *pnt2 = n;
							} else if (label_pixel < n) {
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == n) {// if( *wk == n ){
										label_idxtbl[k] = label_pixel;// *wk = m;
									}
								}
							}
							work2_pt = work2[label_pixel - 1];
							work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
							work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
							work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
						} else {

							label_pixel = label_img[label_img_ptr1 + 1];// label_pixel = label_img_pt1[i + 1];// *pnt2 =
							// *(pnt1+1);

							work2_pt = work2[label_pixel - 1];
							work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
							work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
							work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
							if (work2_pt[3] > i) {// if(work2[((*pnt2)-1)*7+3] > i ){
								work2_pt[3] = i;// work2[((*pnt2)-1)*7+3] = i;
							}
							work2_pt[6] = j;// work2[((*pnt2)-1)*7+6] = j;
						}
					} else if ((label_img[label_img_ptr1 - 1]) > 0) {// } else if ((label_img_pt1[i - 1]) > 0) {// }else if(
						// *(pnt1-1) > 0 ) {
						label_pixel = label_img[label_img_ptr1 - 1];// label_pixel = label_img_pt1[i - 1];// *pnt2 =
						// *(pnt1-1);

						work2_pt = work2[label_pixel - 1];
						work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
						work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
						work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
						if (work2_pt[4] < i) {// if( work2[((*pnt2)-1)*7+4] <i ){
							work2_pt[4] = i;// work2[((*pnt2)-1)*7+4] = i;
						}
						work2_pt[6] = j;// work2[((*pnt2)-1)*7+6] = j;
					} else if (label_img[pixel_index - 1] > 0) {// } else if (label_img_pt0[i - 1] > 0) {// }else if(*(pnt2-1) > 0) {
						label_pixel = label_img[pixel_index - 1];// label_pixel = label_img_pt0[i - 1];// *pnt2 =*(pnt2-1);

						work2_pt = work2[label_pixel - 1];
						work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
						work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
						work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
						if (work2_pt[4] < i) {// if( work2[((*pnt2)-1)*7+4] <i ){
							work2_pt[4] = i;// work2[((*pnt2)-1)*7+4] = i;
						}
					} else {
						// 現在地までの領域を予約
						this.work_holder.reserv(wk_max);
						wk_max++;
						label_idxtbl[wk_max - 1] = wk_max;
						label_pixel = wk_max;// work[wk_max-1] = *pnt2 = wk_max;
						work2_pt = work2[wk_max - 1];
						work2_pt[0] = 1;
						work2_pt[1] = i;
						work2_pt[2] = j;
						work2_pt[3] = i;
						work2_pt[4] = i;
						work2_pt[5] = j;
						work2_pt[6] = j;
					}
					label_img[pixel_index] = label_pixel;// label_img_pt0[i] = label_pixel;
				}
			}

		}
		// インデックステーブルとラベル数の計算
		int wlabel_num = 1;// *label_num = *wlabel_num = j - 1;

		for (i = 0; i < wk_max; i++) {// for(int i = 1; i <= wk_max; i++,wk++) {
			label_idxtbl[i] = (label_idxtbl[i] == i + 1) ? wlabel_num++ : label_idxtbl[label_idxtbl[i] - 1];// *wk=(*wk==i)?j++:work[(*wk)-1];
		}
		wlabel_num -= 1;// *label_num = *wlabel_num = j - 1;
		if (wlabel_num == 0) {// if( *label_num == 0 ) {
			// 発見数0
			o_destination.getLabelStack().clear();
			return 0;
		}
		// ラベル情報の保存等
		NyARLabelingLabelStack label_list = o_destination.getLabelStack();

		// ラベルバッファを予約
		label_list.init(wlabel_num);

		// エリアと重心、クリップ領域を計算
		NyARLabelingLabel label_pt;
		NyARLabelingLabel[] labels =label_list.getArray();
		for (i = 0; i < wlabel_num; i++) {
			label_pt = labels[i];
			label_pt.id = (short)(i + 1);
			label_pt.area = 0;
			label_pt.pos_x = label_pt.pos_y = 0;
			label_pt.clip_l = lxsize;// wclip[i*4+0] = lxsize;
			label_pt.clip_t = lysize;// wclip[i*4+2] = lysize;
			label_pt.clip_r = label_pt.clip_b = 0;// wclip[i*4+3] = 0;
		}

		for (i = 0; i < wk_max; i++) {
			label_pt = labels[label_idxtbl[i] - 1];
			work2_pt = work2[i];
			label_pt.area += work2_pt[0];
			label_pt.pos_x += work2_pt[1];
			label_pt.pos_y += work2_pt[2];
			if (label_pt.clip_l > work2_pt[3]) {
				label_pt.clip_l = work2_pt[3];
			}
			if (label_pt.clip_r < work2_pt[4]) {
				label_pt.clip_r = work2_pt[4];
			}
			if (label_pt.clip_t > work2_pt[5]) {
				label_pt.clip_t = work2_pt[5];
			}
			if (label_pt.clip_b < work2_pt[6]) {
				label_pt.clip_b = work2_pt[6];
			}
		}

		for (i = 0; i < wlabel_num; i++) {// for(int i = 0; i < *label_num; i++ ) {
			label_pt = labels[i];
			label_pt.pos_x /= label_pt.area;
			label_pt.pos_y /= label_pt.area;
		}		
		return wlabel_num;
	}

}

/**
 * NyARLabeling_O2のworkとwork2を可変長にするためのクラス
 * 
 * 
 */
final class NyARWorkHolder
{
	private final static int ARRAY_APPEND_STEP = 256;

	public final int[][] work2;

	private int allocate_size;

	/**
	 * 最大i_holder_size個の動的割り当てバッファを準備する。
	 * 
	 * @param i_holder_size
	 */
	public NyARWorkHolder(int i_holder_size)
	{
		// ポインタだけははじめに確保しておく
		this.work2 = new int[i_holder_size][];
		this.allocate_size = 0;
	}

	/**
	 * i_indexで指定した番号までのバッファを準備する。
	 * 
	 * @param i_index
	 */
	public final void reserv(int i_index) throws NyARException
	{
		// アロケート済みなら即リターン
		if (this.allocate_size > i_index) {
			return;
		}
		// 要求されたインデクスは範囲外
		if (i_index >= this.work2.length) {
			throw new NyARException();
		}
		// 追加アロケート範囲を計算
		int range = i_index + ARRAY_APPEND_STEP;
		if (range >= this.work2.length) {
			range = this.work2.length;
		}
		// アロケート
		for (int i = this.allocate_size; i < range; i++) {
			this.work2[i] = new int[7];
		}
		this.allocate_size = range;
	}
}
