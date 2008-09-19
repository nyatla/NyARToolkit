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
package jp.nyatla.nyartoolkit.nymodel.x2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.labeling.*;

/**
 * NyARLabeling_ARToolKitと同じ処理をするけど、エリア計算にintを使う。 
 * 画面サイズが1600ｘ1600を超えると挙動が怪しくなる。
 * 
 */
public class NyARLabeling_ARToolKit_X2 implements INyARLabeling
{
	private static final int WORK_SIZE = 1024 * 32;// #define WORK_SIZE 1024*32

	private final NyARWorkHolder work_holder = new NyARWorkHolder(WORK_SIZE);

	private NyARIntSize _dest_size;

	private INyARLabelingImage _out_image;

	public void attachDestination(INyARLabelingImage i_destination_image) throws NyARException
	{
		// サイズチェック
		NyARIntSize size = i_destination_image.getSize();
		this._out_image = i_destination_image;

		// NyLabelingImageのイメージ初期化(枠書き)
		int[][] img = (int[][]) i_destination_image.getBufferReader().getBuffer();
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

	public INyARLabelingImage getAttachedDestination()
	{
		return this._out_image;
	}

	/**
	 * static ARInt16 *labeling2( ARUint8 *image, int thresh,int *label_num, int **area, double **pos, int **clip,int **label_ref, int LorR ) 関数の代替品
	 * ラスタimageをラベリングして、結果を保存します。 Optimize:STEP[1514->1493]
	 * 
	 * @param i_raster
	 * @throws NyARException
	 */
	public void labeling(NyARBinRaster i_raster) throws NyARException
	{
		int m, n; /* work */
		int i, j, k;
		INyARLabelingImage out_image = this._out_image;

		// サイズチェック
		NyARIntSize in_size = i_raster.getSize();
		this._dest_size.isEqualSize(in_size);

		final int lxsize = in_size.w;// lxsize = arUtil_c.arImXsize;
		final int lysize = in_size.h;// lysize = arUtil_c.arImYsize;
		int[][] label_img = (int[][]) out_image.getBufferReader().getBuffer();

		// 枠作成はインスタンスを作った直後にやってしまう。

		// ラベリング情報のリセット（ラベリングインデックスを使用）
		out_image.reset(true);

		int[] label_idxtbl = out_image.getIndexArray();

		int[] work2_pt;
		int wk_max = 0;

		int label_pixel;
		int[][] raster_buf = (int[][]) i_raster.getBufferReader().getBuffer();
		int[] line_ptr;
		int[][] work2 = this.work_holder.work2;
		int[] label_img_pt0, label_img_pt1;
		for (j = 1; j < lysize - 1; j++) {// for (int j = 1; j < lysize - 1;j++, pnt += poff*2, pnt2 += 2) {
			line_ptr = raster_buf[j];
			label_img_pt0 = label_img[j];
			label_img_pt1 = label_img[j - 1];
			for (i = 1; i < lxsize - 1; i++) {// for(int i = 1; i < lxsize-1;i++, pnt+=poff, pnt2++) {
				// RGBの合計値が閾値より小さいかな？
				if (line_ptr[i] == 0) {
					// pnt1 = ShortPointer.wrap(pnt2, -lxsize);//pnt1 =&(pnt2[-lxsize]);
					if (label_img_pt1[i] > 0) {// if( *pnt1 > 0 ) {
						label_pixel = label_img_pt1[i];// *pnt2 = *pnt1;

						work2_pt = work2[label_pixel - 1];
						work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
						work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
						work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
						work2_pt[6] = j;// work2[((*pnt2)-1)*7+6] = j;
					} else if (label_img_pt1[i + 1] > 0) {// }else if(*(pnt1+1) > 0 ) {
						if (label_img_pt1[i - 1] > 0) {// if( *(pnt1-1) > 0 ) {
							m = label_idxtbl[label_img_pt1[i + 1] - 1];// m =work[*(pnt1+1)-1];
							n = label_idxtbl[label_img_pt1[i - 1] - 1];// n =work[*(pnt1-1)-1];
							if (m > n) {
								label_pixel = n;// *pnt2 = n;
								// wk=IntPointer.wrap(work, 0);//wk =
								// &(work[0]);
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == m) {// if( *wk == m )
										label_idxtbl[k] = n;// *wk = n;
									}
								}
							} else if (m < n) {
								label_pixel = m;// *pnt2 = m;
								// wk=IntPointer.wrap(work,0);//wk = &(work[0]);
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == n) {// if( *wk == n ){
										label_idxtbl[k] = m;// *wk = m;
									}
								}
							} else {
								label_pixel = m;// *pnt2 = m;
							}
							work2_pt = work2[label_pixel - 1];
							work2_pt[0]++;
							work2_pt[1] += i;
							work2_pt[2] += j;
							work2_pt[6] = j;
						} else if ((label_img_pt0[i - 1]) > 0) {// }else if(*(pnt2-1) > 0) {
							m = label_idxtbl[(label_img_pt1[i + 1]) - 1];// m =work[*(pnt1+1)-1];
							n = label_idxtbl[label_img_pt0[i - 1] - 1];// n =work[*(pnt2-1)-1];
							if (m > n) {

								label_pixel = n;// *pnt2 = n;
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == m) {// if( *wk == m ){
										label_idxtbl[k] = n;// *wk = n;
									}
								}
							} else if (m < n) {
								label_pixel = m;// *pnt2 = m;
								for (k = 0; k < wk_max; k++) {
									if (label_idxtbl[k] == n) {// if( *wk == n ){
										label_idxtbl[k] = m;// *wk = m;
									}
								}
							} else {
								label_pixel = m;// *pnt2 = m;
							}
							work2_pt = work2[label_pixel - 1];
							work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
							work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
							work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
						} else {

							label_pixel = label_img_pt1[i + 1];// *pnt2 =
							// *(pnt1+1);

							work2_pt = work2[label_pixel - 1];
							work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
							work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
							work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
							if (work2_pt[3] > i) {// if(
								// work2[((*pnt2)-1)*7+3] >
								// i ){
								work2_pt[3] = i;// work2[((*pnt2)-1)*7+3] = i;
							}
							work2_pt[6] = j;// work2[((*pnt2)-1)*7+6] = j;
						}
					} else if ((label_img_pt1[i - 1]) > 0) {// }else if(
						// *(pnt1-1) > 0 ) {
						label_pixel = label_img_pt1[i - 1];// *pnt2 =
						// *(pnt1-1);

						work2_pt = work2[label_pixel - 1];
						work2_pt[0]++;// work2[((*pnt2)-1)*7+0] ++;
						work2_pt[1] += i;// work2[((*pnt2)-1)*7+1] += i;
						work2_pt[2] += j;// work2[((*pnt2)-1)*7+2] += j;
						if (work2_pt[4] < i) {// if( work2[((*pnt2)-1)*7+4] <i ){
							work2_pt[4] = i;// work2[((*pnt2)-1)*7+4] = i;
						}
						work2_pt[6] = j;// work2[((*pnt2)-1)*7+6] = j;
					} else if (label_img_pt0[i - 1] > 0) {// }else if(*(pnt2-1) > 0) {
						label_pixel = label_img_pt0[i - 1];// *pnt2 =*(pnt2-1);

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
					label_img_pt0[i] = label_pixel;
				} else {
					label_img_pt0[i] = 0;// *pnt2 = 0;
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
			out_image.getLabelStack().clear();
			return;
		}
		// ラベルの整理
		updateLabelStackLarge(out_image.getLabelStack(), label_idxtbl, in_size, work2, wk_max, wlabel_num);

		return;
	}
	private int[][] __updateLabelStackLarge_temp=new int[64][7];/*area,x,y,l,r,t,b*/

	/* 構造が変わるから、ハイスピード版実装するときに使う。 */
	private void updateLabelStackLarge(NyARLabelingLabelStack i_stack, int[] i_lindex, NyARIntSize i_size, int[][] i_work, int i_work_max, int i_number_of_label) throws NyARException
	{
		//計算用のワークを確保
		int[][] temp=this.__updateLabelStackLarge_temp;
		if(temp.length<i_number_of_label){
			temp=new int[i_number_of_label+64][7];
			this.__updateLabelStackLarge_temp=temp;
		}
		
		// ラベルバッファを予約
		i_stack.reserv(i_number_of_label);
		// エリアと重心、クリップ領域を計算
		final NyARLabelingLabel[] labels = (NyARLabelingLabel[])i_stack.getArray();
		for (int i = 0; i < i_number_of_label; i++) {
			final int[] temp_ptr = temp[i];
			temp_ptr[0]=0;//area
			temp_ptr[1]=0;//x
			temp_ptr[2]=0;//y
			temp_ptr[3]=i_size.w;//l
			temp_ptr[4]=0;//r
			temp_ptr[5]=i_size.h;//t
			temp_ptr[6]=0;//b
		}
		//計算！

		for (int i = 0; i < i_work_max; i++) {
			final int temp_ptr[] = temp[i_lindex[i] - 1];
			final int[] work2_pt = i_work[i];
			temp_ptr[0] += work2_pt[0];
			temp_ptr[1] += work2_pt[1];
			temp_ptr[2] += work2_pt[2];
			if (temp_ptr[3] > work2_pt[3]) {
				temp_ptr[3] = work2_pt[3];
			}
			if (temp_ptr[4] < work2_pt[4]) {
				temp_ptr[4] = work2_pt[4];
			}
			if (temp_ptr[5] > work2_pt[5]) {
				temp_ptr[5] = work2_pt[5];
			}
			if (temp_ptr[6] < work2_pt[6]) {
				temp_ptr[6] = work2_pt[6];
			}
		}
		//ストア
		for (int i = 0; i < i_number_of_label; i++) {// for(int i = 0; i < *label_num; i++ ) {
			final NyARLabelingLabel label_pt = labels[i];
			final int temp_ptr[] = temp[i];
			label_pt.id=i+1;
			label_pt.area=temp_ptr[0];			
			label_pt.pos_x= (double)temp_ptr[1]/label_pt.area;
			label_pt.pos_y= (double)temp_ptr[2]/label_pt.area;
			label_pt.clip_l= temp_ptr[3];
			label_pt.clip_r= temp_ptr[4];
			label_pt.clip_t= temp_ptr[5];
			label_pt.clip_b= temp_ptr[6];
		}
		return;
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

	public final int[] work;

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
		this.work = new int[i_holder_size];
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
		if (i_index >= this.work.length) {
			throw new NyARException();
		}
		// 追加アロケート範囲を計算
		int range = i_index + ARRAY_APPEND_STEP;
		if (range >= this.work.length) {
			range = this.work.length;
		}
		// アロケート
		for (int i = this.allocate_size; i < range; i++) {
			this.work2[i] = new int[8];
		}
		this.allocate_size = range;
	}
}
