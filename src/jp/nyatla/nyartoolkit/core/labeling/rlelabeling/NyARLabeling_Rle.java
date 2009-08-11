/* このソースは実験用のソースです。
 * 動いたり動かなかったりします。
 * 
 */
package jp.nyatla.nyartoolkit.core.labeling.rlelabeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;



/**
 * [strage class]
 */


class RleElement
{
	int l;
	int r;
	short id;
	public static RleElement[] createArray(int i_length)
	{
		RleElement[] ret = new RleElement[i_length];
		for (int i = 0; i < i_length; i++) {
			ret[i] = new RleElement();
		}
		return ret;
	}
}

// RleImageをラベリングする。
public class NyARLabeling_Rle
{
	private RleElement[] _rle1;

	private RleElement[] _rle2;

	private short number_of_fragment; // 現在のフラグメントの数

	public NyARLabeling_Rle(int i_width)
	{
		this._rle1 = RleElement.createArray(i_width/2+1);
		this._rle2 = RleElement.createArray(i_width/2+1);
		return;
	}

	/**
	 * i_bin_bufのbinイメージをREL圧縮する。
	 * 
	 * @param i_bin_raster
	 */
	private int toRel(int[] i_bin_buf, int i_st, int i_len, RleElement[] i_out)
	{
		int current = 0;
		int r = -1;
		// 行確定開始
		int x = i_st;
		final int right_edge = i_st + i_len - 1;
		while (x < right_edge) {
			// 暗点(0)スキャン
			if (i_bin_buf[x] != 0) {
				x++;
				continue;
			}
			// 暗点発見→暗点長を調べる
			r = (x - i_st);
			i_out[current].l = r;
			r++;// 暗点+1
			x++;
			while (x < right_edge) {
				if (i_bin_buf[x] != 0) {
					// 明点(1)→暗点(0)配列終了>登録
					i_out[current].r = r;
					current++;
					x++;// 次点の確認。
					r = -1;// 右端の位置を0に。
					break;
				} else {
					// 暗点(0)長追加
					r++;
					x++;
				}
			}
		}
		// 最後の1点だけ判定方法が少し違うの。
		if (i_bin_buf[x] != 0) {
			// 明点→rカウント中なら暗点配列終了>登録
			if (r >= 0) {
				i_out[current].r = r;
				current++;
			}
		} else {
			// 暗点→カウント中でなければl1で追加
			if (r >= 0) {
				i_out[current].r = (r + 1);
			} else {
				// 最後の1点の場合
				i_out[current].l = (i_st + i_len - 1);
				i_out[current].r = (i_st + i_len);
			}
			current++;
		}
		// 行確定
		return current;
	}

	private void addFragment(RleElement i_rel_img, short i_nof, int i_row_index, int i_rel_index,RleLabelFragmentInfoStack o_stack) throws NyARException
	{
		int l=i_rel_img.l;
		final int len=i_rel_img.r - l;
		i_rel_img.id = i_nof;// REL毎の固有ID
		RleLabelFragmentInfoStack.RleLabelFragmentInfo v = o_stack.prePush();
		v.id = i_nof;
		v.entry_x = l;
		v.area =len;
		v.clip_l=l;
		v.clip_r=i_rel_img.r;
		v.clip_t=i_row_index;
		v.clip_b=i_row_index;
		v.pos_x+=len*(2*l+(len-1)*1);
		v.pos_y+=i_row_index*len;

		
		return;
	}

	//
	public void labeling(NyARBinRaster i_bin_raster, int i_top, int i_bottom,RleLabelFragmentInfoStack o_stack) throws NyARException
	{
		// リセット処理
		this.number_of_fragment = 0;
		o_stack.clear();
		//
		RleElement[] rle_prev = this._rle1;
		RleElement[] rle_current = this._rle2;
		int len_prev = 0;
		int len_current = 0;
		final int width = i_bin_raster.getWidth();
		int[] in_buf = (int[]) i_bin_raster.getBufferReader().getBuffer();

		short nof = this.number_of_fragment;
		// 初段登録

		len_prev = toRel(in_buf, i_top, width, rle_prev);
		for (int i = 0; i < len_prev; i++) {
			// フラグメントID=フラグメント初期値、POS=Y値、RELインデクス=行
			addFragment(rle_prev[i], nof, i_top, i,o_stack);
			nof++;
			// nofの最大値チェック
		}
		RleLabelFragmentInfoStack.RleLabelFragmentInfo[] f_array = o_stack.getArray();
		// 次段結合
		for (int y = i_top + 1; y < i_bottom; y++) {
			// カレント行の読込
			len_current = toRel(in_buf, y * width, width, rle_current);
			int index_prev = 0;

			SCAN_CUR: for (int i = 0; i < len_current; i++) {
				// index_prev,len_prevの位置を調整する
				short id = -1;
				// チェックすべきprevがあれば確認
				SCAN_PREV: while (index_prev < len_prev) {
					if (rle_current[i].l - rle_prev[index_prev].r > 0) {// 0なら8方位ラベリング
						// prevがcurの左方にある→次のフラグメントを探索
						index_prev++;
						continue;
					} else if (rle_prev[index_prev].l - rle_current[i].r > 0) {// 0なら8方位ラベリングになる
						// prevがcur右方にある→独立フラグメント
						addFragment(rle_current[i], nof, y, i,o_stack);
						nof++;
						// 次のindexをしらべる
						continue SCAN_CUR;
					}
					// 結合対象->prevのIDをコピーして、対象フラグメントの情報を更新
					id = f_array[rle_prev[index_prev].id].id;
					RleLabelFragmentInfoStack.RleLabelFragmentInfo prev_ptr;
					final RleLabelFragmentInfoStack.RleLabelFragmentInfo id_ptr = f_array[id];
					prev_ptr = f_array[rle_prev[index_prev].id];
					rle_current[i].id = id;
					//
					final int l= rle_current[i].l;
					final int r= rle_current[i].r;
					final int len=rle_current[i].r - rle_current[i].l;
					id_ptr.area += len;
					id_ptr.entry_x = prev_ptr.entry_x;
					id_ptr.clip_l=l<prev_ptr.clip_l?l:prev_ptr.clip_l;
					id_ptr.clip_r=r>prev_ptr.clip_r?r:prev_ptr.clip_r;
					//id_ptr.clip_tは変更無し。
					id_ptr.clip_b=y;
					id_ptr.pos_x+=len*(2*l+(len-1)*1);
					id_ptr.pos_y+=y*len;
					// 多重リンクの確認

					index_prev++;
					while (index_prev < len_prev) {
						if (rle_current[i].l - rle_prev[index_prev].r > 0) {// 0なら8方位ラベリング
							// prevがcurの左方にある→prevはcurに連結していない。
							break SCAN_PREV;
						} else if (rle_prev[index_prev].l - rle_current[i].r > 0) {// 0なら8方位ラベリングになる
							// prevがcurの右方にある→prevはcurに連結していない。
							index_prev--;
							continue SCAN_CUR;
						}
						// prevとcurは連結している。
						final short prev_id = rle_prev[index_prev].id;
						prev_ptr = f_array[prev_id];
						if (id != prev_id) {
							id_ptr.area += prev_ptr.area;
							prev_ptr.area = 0;
							// 結合対象->現在のidをインデクスにセット
							prev_ptr.id = id;
							//tとentry_xの決定
							if (id_ptr.clip_t > prev_ptr.clip_t) {
								// 現在の方が下にある。
								id_ptr.clip_t = prev_ptr.clip_t;
								id_ptr.entry_x = prev_ptr.entry_x;
							}else if (id_ptr.clip_t < prev_ptr.clip_t) {
								// 現在の方が上にある。なにもしない。
							} else {
								// 水平方向で小さい方がエントリポイント。
								if (id_ptr.entry_x > prev_ptr.entry_x) {
									id_ptr.entry_x = prev_ptr.entry_x;
								}
							}							
							//bの決定(現状のまま)
							//lの決定
							if (id_ptr.clip_l > prev_ptr.clip_l) {
								id_ptr.clip_l=prev_ptr.clip_l;
							}
							//rの決定
							if (id_ptr.clip_r < prev_ptr.clip_r) {
								id_ptr.clip_r=prev_ptr.clip_r;
							}
							id_ptr.pos_x+=prev_ptr.pos_x;
							id_ptr.pos_y+=prev_ptr.pos_y;							
							

						}
						index_prev++;
					}
					index_prev--;
					break;
				}
				// curにidが割り当てられたかを確認
				// 右端独立フラグメントを追加
				if (id < 0) {
					addFragment(rle_current[i], nof, y, i,o_stack);
					nof++;
				}
			}
			// prevとrelの交換
			RleElement[] tmp = rle_prev;
			rle_prev = rle_current;
			len_prev = len_current;
			rle_current = tmp;
		}
		// フラグメントの数を更新
		this.number_of_fragment = nof;
	}	
}



