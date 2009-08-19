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
	int fid;
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
				i_out[current].l = (i_len - 1);
				i_out[current].r = (i_len);
			}
			current++;
		}
		// 行確定
		return current;
	}

	private void addFragment(RleElement i_rel_img, int i_nof, int i_row_index, int i_rel_index,RleLabelFragmentInfoStack o_stack) throws NyARException
	{
		int l=i_rel_img.l;
		final int len=i_rel_img.r - l;
		i_rel_img.fid = i_nof;// REL毎の固有ID
		RleLabelFragmentInfoStack.RleLabelFragmentInfo v = o_stack.prePush();
		v.entry_x = l;
		v.area =len;
		v.clip_l=l;
		v.clip_r=i_rel_img.r-1;
		v.clip_t=i_row_index;
		v.clip_b=i_row_index;
		v.pos_x+=(len*(2*l+(len-1)))/2;
		v.pos_y+=i_row_index*len;

		return;
	}

	//
	public int labeling(NyARBinRaster i_bin_raster, int i_top, int i_bottom,RleLabelFragmentInfoStack o_stack) throws NyARException
	{
		// リセット処理
		o_stack.clear();
		//
		RleElement[] rle_prev = this._rle1;
		RleElement[] rle_current = this._rle2;
		int len_prev = 0;
		int len_current = 0;
		final int width = i_bin_raster.getWidth();
		int[] in_buf = (int[]) i_bin_raster.getBufferReader().getBuffer();

		int id_max = 0;
		int label_count=0;
		// 初段登録

		len_prev = toRel(in_buf, i_top, width, rle_prev);
		for (int i = 0; i < len_prev; i++) {
			// フラグメントID=フラグメント初期値、POS=Y値、RELインデクス=行
			addFragment(rle_prev[i], id_max, i_top, i,o_stack);
			id_max++;
			// nofの最大値チェック
			label_count++;
		}
		RleLabelFragmentInfoStack.RleLabelFragmentInfo[] f_array = o_stack.getArray();
		// 次段結合
		for (int y = i_top + 1; y < i_bottom; y++) {
			// カレント行の読込
			len_current = toRel(in_buf, y * width, width, rle_current);
			int index_prev = 0;

			SCAN_CUR: for (int i = 0; i < len_current; i++) {
				// index_prev,len_prevの位置を調整する
				int id = -1;
				// チェックすべきprevがあれば確認
				SCAN_PREV: while (index_prev < len_prev) {
					if (rle_current[i].l - rle_prev[index_prev].r > 0) {// 0なら8方位ラベリング
						// prevがcurの左方にある→次のフラグメントを探索
						index_prev++;
						continue;
					} else if (rle_prev[index_prev].l - rle_current[i].r > 0) {// 0なら8方位ラベリングになる
						// prevがcur右方にある→独立フラグメント
						addFragment(rle_current[i], id_max, y, i,o_stack);
						id_max++;
						label_count++;
						// 次のindexをしらべる
						continue SCAN_CUR;
					}
					id=rle_prev[index_prev].fid;//ルートフラグメントid
					RleLabelFragmentInfoStack.RleLabelFragmentInfo id_ptr = f_array[id];
					//結合対象(初回)->prevのIDをコピーして、ルートフラグメントの情報を更新
					rle_current[i].fid = id;//フラグメントIDを保存
					//
					final int l= rle_current[i].l;
					final int r= rle_current[i].r;
					final int len=r-l;
					//結合先フラグメントの情報を更新する。
					id_ptr.area += len;
					//tとentry_xは、結合先のを使うので更新しない。
					id_ptr.clip_l=l<id_ptr.clip_l?l:id_ptr.clip_l;
					id_ptr.clip_r=r>id_ptr.clip_r?r-1:id_ptr.clip_r;
					id_ptr.clip_b=y;
					id_ptr.pos_x+=(len*(2*l+(len-1)))/2;
					id_ptr.pos_y+=y*len;
					//多重結合の確認（２個目以降）
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
						// prevとcurは連結している→ルートフラグメントの統合
						
						//結合するルートフラグメントを取得
						final int prev_id =rle_prev[index_prev].fid;
						RleLabelFragmentInfoStack.RleLabelFragmentInfo prev_ptr = f_array[prev_id];
						if (id != prev_id){
							if(prev_ptr.area==0){
								System.out.println("ERRRR");
							}
							label_count--;
							//prevとcurrentのフラグメントidを書き換える。
							for(int i2=index_prev;i2<len_prev;i2++){
								//prevは現在のidから最後まで
								if(rle_prev[i2].fid==prev_id){
									rle_prev[i2].fid=id;
								}
							}
							for(int i2=0;i2<i;i2++){
								//currentは0から現在-1まで
								if(rle_current[i2].fid==prev_id){
									rle_current[i2].fid=id;
								}
							}
							
							//現在のルートフラグメントに情報を集約
							id_ptr.area +=prev_ptr.area;
							id_ptr.pos_x+=prev_ptr.pos_x;
							id_ptr.pos_y+=prev_ptr.pos_y;
							//tとentry_xの決定
							if (id_ptr.clip_t > prev_ptr.clip_t) {
								// 現在の方が下にある。
								id_ptr.clip_t = prev_ptr.clip_t;
								id_ptr.entry_x = prev_ptr.entry_x;
							}else if (id_ptr.clip_t < prev_ptr.clip_t) {
								// 現在の方が上にある。prevにフィードバック
							} else {
								// 水平方向で小さい方がエントリポイント。
								if (id_ptr.entry_x > prev_ptr.entry_x) {
									id_ptr.entry_x = prev_ptr.entry_x;
								}else{
								}
							}
							//lの決定
							if (id_ptr.clip_l > prev_ptr.clip_l) {
								id_ptr.clip_l=prev_ptr.clip_l;
							}else{
							}
							//rの決定
							if (id_ptr.clip_r < prev_ptr.clip_r) {
								id_ptr.clip_r=prev_ptr.clip_r;
							}else{
							}
							//bの決定

							//結合済のルートフラグメントを無効化する。
							prev_ptr.area=0;
						}


						index_prev++;
					}
					index_prev--;
					break;
				}
				// curにidが割り当てられたかを確認
				// 右端独立フラグメントを追加
				if (id < 0){
					addFragment(rle_current[i], id_max, y, i,o_stack);
					id_max++;
					label_count++;
				}
			}
			// prevとrelの交換
			RleElement[] tmp = rle_prev;
			rle_prev = rle_current;
			len_prev = len_current;
			rle_current = tmp;
		}
		//ソートする。
		o_stack.sortByArea();
		//ラベル数を再設定
		o_stack.reserv(label_count);
		//posを計算
		for(int i=0;i<label_count;i++){
			final RleLabelFragmentInfoStack.RleLabelFragmentInfo tmp=f_array[i];
			tmp.pos_x/=tmp.area;
			tmp.pos_y/=tmp.area;
		}
		//ラベル数を返却
		return label_count;
	}	
}



