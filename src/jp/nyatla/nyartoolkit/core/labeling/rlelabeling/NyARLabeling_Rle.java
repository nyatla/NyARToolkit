/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.labeling.rlelabeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

class RleInfoStack extends NyARObjectStack<NyARRleLabelFragmentInfo>
{	
	public RleInfoStack(int i_length) throws NyARException
	{
		super();
		super.initInstance(i_length, NyARRleLabelFragmentInfo.class);
		return;
	}

	protected NyARRleLabelFragmentInfo createElement()
	{
		return new NyARRleLabelFragmentInfo();
	}
}
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


/**
 * ラべリングクラスです。入力画像をラべリングして、そのエントリポイントと情報を、ハンドラ関数を介して返します。
 * 
 *
 */
public abstract class NyARLabeling_Rle
{
	private static final int AR_AREA_MAX = 100000;// #define AR_AREA_MAX 100000
	private static final int AR_AREA_MIN = 70;// #define AR_AREA_MIN 70
	
	private RleInfoStack _rlestack;
	private RleElement[] _rle1;
	private RleElement[] _rle2;
	private int _max_area;
	private int _min_area;
	/**
	 * 処理対象のラスタサイズ
	 */
	protected NyARIntSize _raster_size=new NyARIntSize();

	public NyARLabeling_Rle(int i_width,int i_height) throws NyARException
	{
		this._raster_size.setValue(i_width,i_height);
		this._rlestack=new RleInfoStack(i_width*i_height*2048/(320*240)+32);
		this._rle1 = RleElement.createArray(i_width/2+1);
		this._rle2 = RleElement.createArray(i_width/2+1);
		this._max_area=AR_AREA_MAX;
		this._min_area=AR_AREA_MIN;

		return;
	}
	/**
	 * 対象サイズ
	 * @param i_max
	 * @param i_min
	 */
	public void setAreaRange(int i_max,int i_min)
	{
		assert(i_min>0 && i_max>i_min);
		this._max_area=i_max;
		this._min_area=i_min;
		return;
	}

	/**
	 * i_bin_bufのgsイメージをREL圧縮する。
	 * @param i_bin_buf
	 * @param i_st
	 * @param i_len
	 * @param i_out
	 * @param i_th
	 * BINラスタのときは0,GSラスタの時は閾値を指定する。
	 * この関数は、閾値を暗点と認識します。
	 * 暗点<=th<明点
	 * @return
	 */
	private final int toRel(int[] i_bin_buf, int i_st, int i_len, RleElement[] i_out,int i_th)
	{
		int current = 0;
		int r = -1;
		// 行確定開始
		int x = i_st;
		final int right_edge = i_st + i_len - 1;
		while (x < right_edge) {
			// 暗点(0)スキャン
			if (i_bin_buf[x] > i_th) {
				x++;//明点
				continue;
			}
			// 暗点発見→暗点長を調べる
			r = (x - i_st);
			i_out[current].l = r;
			r++;// 暗点+1
			x++;
			while (x < right_edge) {
				if (i_bin_buf[x] > i_th) {
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
		if (i_bin_buf[x] > i_th) {
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

	private final boolean addFragment(RleElement i_rel_img, int i_nof, int i_row_index,RleInfoStack o_stack) throws NyARException
	{
		int l=i_rel_img.l;
		final int len=i_rel_img.r - l;
		i_rel_img.fid = i_nof;// REL毎の固有ID
		NyARRleLabelFragmentInfo v = o_stack.prePush();
		if(o_stack==null){
			System.err.println("addFragment force recover!");
			return false;
		}
		v.entry_x = l;
		v.area =len;
		v.clip_l=l;
		v.clip_r=i_rel_img.r-1;
		v.clip_t=i_row_index;
		v.clip_b=i_row_index;
		v.pos_x=(len*(2*l+(len-1)))/2;
		v.pos_y=i_row_index*len;

		return true;
	}
	/**
	 * BINラスタをラベリングします。
	 * @param i_bin_raster
	 * @param o_stack
	 * 結果を蓄積するスタックオブジェクトを指定します。
	 * 関数は、このオブジェクトに結果を追記します。
	 * @return
	 * @throws NyARException
	 */
	public void labeling(NyARBinRaster i_bin_raster) throws NyARException
	{
		assert(i_bin_raster.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
		NyARIntSize size=i_bin_raster.getSize();
		this.imple_labeling(i_bin_raster,0,0,0,size.w,size.h);
	}
	public void labeling(NyARBinRaster i_bin_raster,NyARIntRect i_area) throws NyARException
	{
		assert(i_bin_raster.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
		this.imple_labeling(i_bin_raster,0,i_area.x,i_area.y,i_area.w,i_area.h);
	}
	/**
	 * GSラスタの２値ラべリングを実行します。
	 * @param i_gs_raster
	 * @param i_th
	 * 二値化の敷居値を指定します。
	 * @param o_stack
	 * 結果を蓄積するスタックオブジェクトを指定します。
	 * 関数は、このオブジェクトに結果を追記します。
	 * @return
	 * @throws NyARException
	 */
	public void labeling(NyARGrayscaleRaster i_gs_raster,int i_th) throws NyARException
	{
		assert(i_gs_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		NyARIntSize size=i_gs_raster.getSize();
		this.imple_labeling(i_gs_raster,i_th,0,0,size.w,size.h);
	}
	/**
	 * 範囲付きでGSラスタの２値ラべリングを実行します。
	 * @param i_gs_raster
	 * @param i_area
	 * @param i_th
	 * @param o_stack
	 * 結果を蓄積するスタックオブジェクトを指定します。
	 * 関数は、このオブジェクトに結果を追記します。
	 * @return
	 * @throws NyARException
	 */
	public void labeling(NyARGrayscaleRaster i_gs_raster,NyARIntRect i_area,int i_th) throws NyARException
	{
		assert(i_gs_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		this.imple_labeling(i_gs_raster,i_th,i_area.x,i_area.y,i_area.w,i_area.h);
	}
	private void imple_labeling(INyARRaster i_raster,int i_th,int i_left,int i_top,int i_width, int i_height) throws NyARException
	{
		//ラスタのサイズを確認
		assert(i_raster.getSize().isEqualSize(this._raster_size));
		
		RleElement[] rle_prev = this._rle1;
		RleElement[] rle_current = this._rle2;
		// リセット処理
		final RleInfoStack rlestack=this._rlestack;
		rlestack.clear();

		//
		int len_prev = 0;
		int len_current = 0;
		final int bottom=i_top+i_height;
		final int row_stride=this._raster_size.w;
		int[] in_buf = (int[]) i_raster.getBuffer();

		int id_max = 0;
		int label_count=0;
		int rle_top_index=i_left+row_stride*i_top;
		// 初段登録

		len_prev = toRel(in_buf, rle_top_index, i_width, rle_prev,i_th);
		for (int i = 0; i < len_prev; i++) {
			// フラグメントID=フラグメント初期値、POS=Y値、RELインデクス=行
			if(addFragment(rle_prev[i], id_max, i_top,rlestack)){
				id_max++;
				// nofの最大値チェック
				label_count++;
			}
		}
		NyARRleLabelFragmentInfo[] f_array = rlestack.getArray();
		// 次段結合
		for (int y = i_top + 1; y < bottom; y++) {
			// カレント行の読込
			rle_top_index+=row_stride;
			len_current = toRel(in_buf,rle_top_index, i_width, rle_current,i_th);
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
						if(addFragment(rle_current[i], id_max, y,rlestack)){
							id_max++;
							label_count++;
						}
						// 次のindexをしらべる
						continue SCAN_CUR;
					}
					id=rle_prev[index_prev].fid;//ルートフラグメントid
					NyARRleLabelFragmentInfo id_ptr = f_array[id];
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
						NyARRleLabelFragmentInfo prev_ptr = f_array[prev_id];
						if (id != prev_id){
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
					if(addFragment(rle_current[i], id_max, y,rlestack)){
						id_max++;
						label_count++;
					}
				}
			}
			// prevとrelの交換
			RleElement[] tmp = rle_prev;
			rle_prev = rle_current;
			len_prev = len_current;
			rle_current = tmp;
		}
		//対象のラベルだけを追記
		final int max=this._max_area;
		final int min=this._min_area;
		for(int i=id_max-1;i>=0;i--){
			final NyARRleLabelFragmentInfo src_info=f_array[i];
			final int area=src_info.area;
			if(area<min || area>max){//対象外のエリア0のもminではじく
				continue;
			}
			//値を相対位置に補正
			src_info.clip_l+=i_left;
			src_info.clip_r+=i_left;
			src_info.entry_x+=i_left;
			src_info.pos_x/=area;
			src_info.pos_y/=area;
			//コールバック関数コール
			this.onLabelFound(src_info);
		}
	}
	/**
	 * ハンドラ関数です。継承先クラスでオーバライドしてください。
	 * i_labelのインスタンスは、次のラべリング実行まで保証されていますが、将来にわたり保証されないかもしれません。(恐らく保証されますが)
	 * コールバック関数から参照を使用する場合は、互換性を確認するために、念のため、assertで_af_label_array_safe_referenceフラグをチェックしてください。
	 * @param i_label
	 */
	protected abstract void onLabelFound(NyARRleLabelFragmentInfo i_ref_label) throws NyARException;
	
	/**
	 * クラスの仕様確認フラグです。ラベル配列の参照アクセスが可能かを返します。
	 * 
	 */
	public final static boolean _sf_label_array_safe_reference=true;
}



