/* このソースは実験用のソースです。
 * 動いたり動かなかったりします。
 * 
 */
package jp.nyatla.nyartoolkit.dev;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BGRA;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.utils.j2se.*;



public class LabelingTest extends Frame
{
	private final String data_file = "../Data/320x240ABGR.raw";

	final int W=10;
	final int H=10;
	public void drawImage() throws Exception
	{
		File f = new File(data_file);
		FileInputStream fs = new FileInputStream(data_file);
		byte[] buf = new byte[(int) f.length()];
		fs.read(buf);
		NyARRgbRaster_BGRA ra = NyARRgbRaster_BGRA.wrap(buf, W, H);
		// 二値化
		NyARRasterFilterBuilder_RgbToBin filter = new NyARRasterFilterBuilder_RgbToBin(110, ra.getBufferReader().getBufferType());
		NyARBinRaster bin = new NyARBinRaster(W,240);
		filter.doFilter(ra, bin);
		int[] t = (int[]) bin.getBuffer();
		int[] s = {
				1,1,1,1,1,1,1,1,1,1,

				1,0,0,0,0,1,0,1,1,1,
				1,0,1,1,0,1,0,1,1,1,
				1,1,1,1,1,0,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,				0};
		System.arraycopy(s, 0, t, 0, 10*10);
		NyARRasterImageIO sink = new NyARRasterImageIO(W, H);
		RleImage rle = new RleImage(bin.getWidth(),bin.getHeight());
		RleLabelingTable table=createRelTable(rle,bin,sink);
		for(int i=0;;i++){
			int nof=i%table.number_of_fragment;
			if(table._fragment_area[nof]==0){
				continue;
			}
			drawLabelingImage(nof,rle,table,bin,sink);
			Graphics g;
			g=this.getGraphics();
			g.drawImage(sink, 100, 100,100,100, this);
			Thread.sleep(500);
		}
	}
	public void drawLabelingImage(int id,RleImage rle, RleLabelingTable i_table,NyARBinRaster i_source,NyARRasterImageIO i_img)
	{
//		RleImage rle = new RleImage(i_source.getWidth(),i_source.getHeight());
//		i_table.labeling(rle,0,10);
		int p=0;
		for(int i=0;i<H;i++){
			for(int i2=0;i2<W;i2++){
				i_img.setRGB(i2,i,0xffffff);
			}
			for(int i2=0;i2<rle.row_length[i];i2++){
				for(int i3=rle.rle_img_l[rle.row_index[i]+i2];i3<rle.rle_img_r[rle.row_index[i]+i2];i3++)
				{
					int c=0x0000ff;
					int tid=i_table._fragment_id[i_table.rle_img_id[p]];
					if(tid==id){
						c=0x00ff00;
					}
					i_img.setRGB(i3,i,c);
				}
				p++;
			}
		}

		i_img.setRGB(i_table._flagment_entry_x[id],i_table._flagment_entry_y[id],0xff0000);
//		i_img.setRGB(i_table._flagment_entry_x[id]+1,i_table._flagment_entry_y[id],0xff0000);
//		i_img.setRGB(i_table._flagment_entry_x[id],i_table._flagment_entry_y[id]+1,0xff0000);
//		i_img.setRGB(i_table._flagment_entry_x[id]+1,i_table._flagment_entry_y[id]+1,0xff0000);
	}
	
	public RleLabelingTable createRelTable(RleImage rle,NyARBinRaster i_source,NyARRasterImageIO i_img)
	{
		// RELイメージの作成
		rle.toRel(i_source);
		// 1行目のテーブル登録
		RleLabelingTable table = new RleLabelingTable(10000);
		table.labeling(rle,0,H);
		return table;
	}

	public LabelingTest() throws NyARException
	{
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	}

	public static void main(String[] args)
	{
		try {
			LabelingTest app = new LabelingTest();
			app.setVisible(true);
			app.setBounds(0, 0, 640, 480);
			app.drawImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class RleImage
	{
		int _width;

		int _height;

		// RELデータ
		short[] rle_img_l;// RELのフラグメント左

		short[] rle_img_r;// RELのフラグメント右

		// Rowデータ
		int[] row_index;

		short[] row_length;

		int rle_buf_size;

		public RleImage(int i_width, int i_height)
		{
			final int size = i_width * i_height / 2;
			// 1/2に圧縮できることを想定。
			this.rle_img_l = new short[size]; // RELのフラグメント長
			this.rle_img_r = new short[size]; // RELの開始位置
			this.rle_buf_size = size;

			this.row_index = new int[i_height];
			this.row_length = new short[i_height];
			this._width = i_width;
			this._height = i_height;
			return;
		}

		/**
		 * binラスタからRELに変換する
		 * 
		 * @param i_bin_raster
		 */
		public void toRel(NyARBinRaster i_bin_raster)
		{
			final int width = this._width;
			final int height = this._height;
			int[] in_buf = (int[]) i_bin_raster.getBuffer();

			short current = 0;
			short r = -1;
			for (int y = 0; y < height; y++) {
				this.row_index[y] = current;
				// 行確定開始
				int row_index = y * width;
				int x = row_index;
				final int right_edge = (y + 1) * width - 1;
				while (x < right_edge) {
					// 暗点(0)スキャン
					if (in_buf[x] != 0) {
						x++;
						continue;
					}
					// 暗点発見→暗点長を調べる
					r = (short) (x - row_index);
					this.rle_img_l[current] = r;
					r++;// 暗点+1
					x++;
					while (x < right_edge) {
						if (in_buf[x] != 0) {
							// 明点(1)→暗点(0)配列終了>登録
							this.rle_img_r[current] = r;
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
				if (in_buf[x] != 0) {
					// 明点→rカウント中なら暗点配列終了>登録
					if (r >= 0) {
						this.rle_img_r[current] = r;
						current++;
					}
				} else {
					// 暗点→カウント中でなければl1で追加
					if (r >= 0) {
						this.rle_img_r[current] = (short) (x + 1);
					} else {
						// 最後の1点の場合
						this.rle_img_l[current] = (short) (width - 1);
						this.rle_img_r[current] = (short) (width);
					}
					current++;
				}
				// 行確定
				this.row_length[y] = (short) (current - this.row_index[y]);
			}
		}

		public void fromRel(NyARBinRaster i_bin_raster)
		{

		}
	}

	// RleImageをラベリングする。
	class RleLabelingTable
	{
		short[] rle_img_id;

		short[] _fragment_id; // フラグメントラベルのインデクス

		int[] _fragment_area; // フラグメントラベルの領域数

		int[] _fragment_pos_x; // フラグメントラベルの位置

		int[] _fragment_pos_y; // フラグメントラベルの位置

		short[] _flagment_entry_x; // フラグメントラベルの位置

		short[] _flagment_entry_y; // フラグメントラベルの位置

		short number_of_fragment; // 現在のフラグメントの数

		public RleLabelingTable(int i_max_fragment)
		{
			this.rle_img_id = new short[i_max_fragment];
			this._fragment_id = new short[i_max_fragment];
			this._fragment_area = new int[i_max_fragment];
			this._fragment_pos_x = new int[i_max_fragment];
			this._fragment_pos_y = new int[i_max_fragment];
			this._flagment_entry_x = new short[i_max_fragment];
			this._flagment_entry_y = new short[i_max_fragment];
		}

		private void addFragment(RleImage i_rel_img, short i_nof, int i_row_index, int i_rel_index)
		{
			this.rle_img_id[i_rel_index] = i_nof;// REL毎の固有ID
			this._fragment_id[i_nof] = i_nof;
			this._flagment_entry_x[i_nof] = i_rel_img.rle_img_l[i_rel_index];
			this._flagment_entry_y[i_nof] = (short) i_row_index;
			this._fragment_area[i_nof] = i_rel_img.rle_img_r[i_rel_index] - i_rel_img.rle_img_l[i_rel_index];
			return;
		}



		// 指定した行のフラグメントをマージします。
		public void labeling(RleImage i_rel_img, int i_top, int i_bottom)
		{
			short[] rle_l = i_rel_img.rle_img_l;
			short[] rle_r = i_rel_img.rle_img_r;
			short nof = this.number_of_fragment;
			// 初段登録
			int index = i_rel_img.row_index[i_top];
			int eol = i_rel_img.row_length[i_top];
			for (int i = index; i < index + eol; i++) {
				// フラグメントID=フラグメント初期値、POS=Y値、RELインデクス=行
				addFragment(i_rel_img, nof, i_top, i);
				nof++;
				// nofの最大値チェック
			}
			// 次段結合
			for (int y = i_top+1; y < i_bottom; y++) {
				int index_prev = i_rel_img.row_index[y - 1];
				int eol_prev = index_prev + i_rel_img.row_length[y - 1];
				index = i_rel_img.row_index[y];
				eol = index + i_rel_img.row_length[y];

				SCAN_CUR:for (int i = index; i < eol; i++) {
					// index_prev,len_prevの位置を調整する
					short id = -1;
					//チェックすべきprevがあれば確認
					SCAN_PREV: while (index_prev < eol_prev) {
						if (rle_l[i] - rle_r[index_prev] > 0) {// 0なら8方位ラベリング
							// prevがcurの左方にある→次のフラグメントを探索
							index_prev++;
							continue;
						} else if (rle_l[index_prev] - rle_r[i] > 0) {// 0なら8方位ラベリングになる
							// prevがcur右方にある→独立フラグメント
							addFragment(i_rel_img, nof, y, i);
							nof++;
							// 次のindexをしらべる
							continue SCAN_CUR;
						}
						// 結合対象->prevのIDをコピーして、対象フラグメントの情報を更新
						id = this._fragment_id[this.rle_img_id[index_prev]];
						this.rle_img_id[i] = id;
						this._fragment_area[id] += (rle_r[i] - rle_l[i]);
						// エントリポイントの情報をコピー
						this._flagment_entry_x[id] = this._flagment_entry_x[this.rle_img_id[index_prev]];
						this._flagment_entry_y[id] = this._flagment_entry_y[this.rle_img_id[index_prev]];
						//多重リンクの確認

						index_prev++;
						while (index_prev < eol_prev) {
							if (rle_l[i] - rle_r[index_prev] > 0) {// 0なら8方位ラベリング
								// prevがcurの左方にある→prevはcurに連結していない。
								break SCAN_PREV;
							} else if (rle_l[index_prev] - rle_r[i] > 0) {// 0なら8方位ラベリングになる
								// prevがcurの右方にある→prevはcurに連結していない。
								index_prev--;
								continue SCAN_CUR;
							}
							// prevとcurは連結している。
							final short prev_id = this.rle_img_id[index_prev];
							if (id != prev_id) {
								this._fragment_area[id] += this._fragment_area[prev_id];
								this._fragment_area[prev_id] = 0;
								// 結合対象->現在のidをインデクスにセット
								this._fragment_id[prev_id]=id;
								// エントリポイントを訂正
								if (this._flagment_entry_y[id] > this._flagment_entry_y[prev_id]) {
									// 現在のエントリポイントの方が下にある。(何もしない)
								}
								if (this._flagment_entry_y[id] < this._flagment_entry_y[prev_id]) {
									// 現在のエントリポイントの方が上にある。（エントリポイントの交換）
									this._flagment_entry_y[id] = this._flagment_entry_y[prev_id];
									this._flagment_entry_x[id] = this._flagment_entry_x[prev_id];
								} else {
									// 水平方向で小さい方がエントリポイント。
									if (this._flagment_entry_x[id] > this._flagment_entry_x[prev_id]) {
										this._flagment_entry_y[id] = this._flagment_entry_y[prev_id];
										this._flagment_entry_x[id] = this._flagment_entry_x[prev_id];
									}
								}
							}
							index_prev++;
						}
						index_prev--;
						break;
					}
					// curにidが割り当てられたかを確認
					// 右端独立フラグメントを追加
					if (id < 0) {
						addFragment(i_rel_img, nof, y, i);
						nof++;
					}

				}
			}
			// フラグメントの数を更新
			this.number_of_fragment = nof;
		}
	}
}

// REL圧縮配列を作成
// REL結合
// 面積計算
// 参照インデクス化
// ラベルイメージ化
