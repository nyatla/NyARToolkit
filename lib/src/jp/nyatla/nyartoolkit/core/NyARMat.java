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
package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;



/**
 * ARMat構造体に対応するクラス typedef struct { double *m; int row; int clm; }ARMat;
 * 
 */
public class NyARMat
{
	/**
	 * 配列サイズと行列サイズは必ずしも一致しないことに注意 返された配列のサイズを行列の大きさとして使わないこと！
	 * 
	 */
	protected double[][] _m;
	private int[] __matrixSelfInv_nos;

	protected int clm;
	protected int row;

	/**
	 * デフォルトコンストラクタは機能しません。
	 * 
	 * @throws NyARException
	 */
	protected NyARMat() throws NyARException
	{
		throw new NyARException();
	}

	public NyARMat(int i_row, int i_clm)
	{
		this._m = new double[i_row][i_clm];
		this.clm = i_clm;
		this.row = i_row;
		this.__matrixSelfInv_nos=new int[i_row];
		return;
	}
	
	/**
	 * 行列の列数を返します。
	 * @return
	 */
	public int getClm()
	{
		return clm;
	}
	/**
	 * 行列の行数を返します。
	 * @return
	 */
	public int getRow()
	{
		return row;
	}	
	
	/**
	 * 行列のサイズを変更します。
	 * 実行後、行列の各値は不定になります。
	 * @param i_row
	 * @param i_clm
	 */
	public void realloc(int i_row, int i_clm)
	{
		if (i_row <= this._m.length && i_clm <= this._m[0].length) {
			// 十分な配列があれば何もしない。
		} else {
			// 不十分なら取り直す。
			this._m = new double[i_row][i_clm];
			this.__matrixSelfInv_nos=new int[i_row];
		}
		this.clm = i_clm;
		this.row = i_row;
		return;
	}
	
	
	
	/**
	 * i_mat_aとi_mat_bの積を計算して、thisへ格納します。
	 * @param i_mat_a
	 * @param i_mat_b
	 * @throws NyARException
	 */
	public void matrixMul(NyARMat i_mat_a, NyARMat i_mat_b) throws NyARException
	{
		assert i_mat_a.clm == i_mat_b.row && this.row==i_mat_a.row && this.clm==i_mat_b.clm;

		double w;
		int r, c, i;
		double[][] am = i_mat_a._m, bm = i_mat_b._m, dm = this._m;
		// For順変更禁止
		for (r = 0; r < this.row; r++) {
			for (c = 0; c < this.clm; c++) {
				w = 0.0;
				for (i = 0; i < i_mat_a.clm; i++) {
					w += am[r][i] * bm[i][c];
				}
				dm[r][c] = w;
			}
		}
		return;
	}	
	
	/**
	 * 逆行列を計算して、thisへ格納します。
	 * @throws NyARException
	 */
	public boolean matrixSelfInv() throws NyARException
	{
		double[][] ap = this._m;
		int dimen = this.row;
		int dimen_1 = dimen - 1;
		double[] ap_n, ap_ip, ap_i;// wap;
		int j, ip, nwork;
		int[] nos = __matrixSelfInv_nos;//ワーク変数
		// double epsl;
		double p, pbuf, work;

		/* check size */
		switch (dimen) {
		case 0:
			throw new NyARException();
		case 1:
			ap[0][0] = 1.0 / ap[0][0];// *ap = 1.0 / (*ap);
			return true;/* 1 dimension */
		}

		for (int n = 0; n < dimen; n++) {
			nos[n] = n;
		}

		/*
		 * nyatla memo ipが定まらないで計算が行われる場合があるので挿入。 ループ内で0初期化していいかが判らない。
		 */
		ip = 0;
		// For順変更禁止
		for (int n = 0; n < dimen; n++) {
			ap_n = ap[n];// wcp = ap + n * rowa;
			p = 0.0;
			for (int i = n; i < dimen; i++) {
				if (p < (pbuf = Math.abs(ap[i][0]))) {
					p = pbuf;
					ip = i;
				}
			}
			// if (p <= matrixSelfInv_epsl){
			if (p == 0.0) {
				return false;
				// throw new NyARException();
			}

			nwork = nos[ip];
			nos[ip] = nos[n];
			nos[n] = nwork;

			ap_ip = ap[ip];
			for (j = 0; j < dimen; j++) {// for(j = 0, wap = ap + ip * rowa,
											// wbp = wcp; j < dimen ; j++) {
				work = ap_ip[j]; // work = *wap;
				ap_ip[j] = ap_n[j];
				ap_n[j] = work;
			}

			work = ap_n[0];
			for (j = 0; j < dimen_1; j++) {
				ap_n[j] = ap_n[j + 1] / work;// *wap = *(wap + 1) / work;
			}
			ap_n[j] = 1.0 / work;// *wap = 1.0 / work;
			for (int i = 0; i < dimen; i++) {
				if (i != n) {
					ap_i = ap[i];// wap = ap + i * rowa;
					work = ap_i[0];
					for (j = 0; j < dimen_1; j++) {// for(j = 1, wbp = wcp,work = *wap;j < dimen ;j++, wap++, wbp++)
						ap_i[j] = ap_i[j + 1] - work * ap_n[j];// wap = *(wap +1) - work *(*wbp);
					}
					ap_i[j] = -work * ap_n[j];// *wap = -work * (*wbp);
				}
			}
		}

		for (int n = 0; n < dimen; n++) {
			for (j = n; j < dimen; j++) {
				if (nos[j] == n) {
					break;
				}
			}
			nos[j] = nos[n];
			for (int i = 0; i < dimen; i++) {
				ap_i = ap[i];
				work = ap_i[j];// work = *wap;
				ap_i[j] = ap_i[n];// *wap = *wbp;
				ap_i[n] = work;// *wbp = work;
			}
		}
		return true;
	}

	/**
	 * 行列をゼロクリアする。
	 */
	public void zeroClear()
	{
		int i, i2;
		// For順変更OK
		for (i = row - 1; i >= 0; i--) {
			for (i2 = clm - 1; i2 >= 0; i2--) {
				_m[i][i2] = 0.0;
			}
		}
	}

	/**
	 * i_copy_fromの内容を自分自身にコピーします。 高さ・幅は同一で無いと失敗します。
	 * 
	 * @param i_copy_from
	 */
	public void copyFrom(NyARMat i_copy_from) throws NyARException
	{
		// サイズ確認
		if (this.row != i_copy_from.row || this.clm != i_copy_from.clm) {
			throw new NyARException();
		}
		// 値コピー
		for (int r = this.row - 1; r >= 0; r--) {
			for (int c = this.clm - 1; c >= 0; c--) {
				this._m[r][c] = i_copy_from._m[r][c];
			}
		}
	}

	public double[][] getArray()
	{
		return _m;
	}


	/**
	 * sourceの転置行列をdestに得る。arMatrixTrans()の代替品
	 * 
	 * @param dest
	 * @param source
	 * @return
	 */
	public static void matrixTrans(NyARMat dest, NyARMat source) throws NyARException
	{
		if (dest.row != source.clm || dest.clm != source.row) {
			throw new NyARException();
		}
		NyARException.trap("未チェックのパス");
		// For順変更禁止
		for (int r = 0; r < dest.row; r++) {
			for (int c = 0; c < dest.clm; c++) {
				dest._m[r][c] = source._m[c][r];
			}
		}
	}

	/**
	 * unitを単位行列に初期化する。arMatrixUnitの代替品
	 * 
	 * @param unit
	 */
	public static void matrixUnit(NyARMat unit) throws NyARException
	{
		if (unit.row != unit.clm) {
			throw new NyARException();
		}
		NyARException.trap("未チェックのパス");
		// For順変更禁止
		for (int r = 0; r < unit.getRow(); r++) {
			for (int c = 0; c < unit.getClm(); c++) {
				if (r == c) {
					unit._m[r][c] = 1.0;
				} else {
					unit._m[r][c] = 0.0;
				}
			}
		}
	}

	/**
	 * sourceの内容を自身に複製する。 Optimized 2008.04.19
	 * 
	 * @param i_source
	 * @return
	 */
	public void matrixDup(NyARMat i_source) throws NyARException
	{
		// 自身の配列サイズを相手のそれより大きいことを保障する。
		this.realloc(i_source.row, i_source.clm);
		// 内容を転写
		int r, c;
		double[][] src_m, dest_m;
		src_m = i_source._m;
		dest_m = this._m;
		// コピーはFor順を変えてもOK
		for (r = this.row - 1; r >= 0; r--) {
			for (c = this.clm - 1; c >= 0; c--) {
				dest_m[r][c] = src_m[r][c];
			}
		}
	}

	public NyARMat matrixAllocDup() throws NyARException
	{
		NyARMat result = new NyARMat(this.row, this.clm);
		// コピー
		int r, c;
		double[][] dest_m, src_m;
		dest_m = result._m;
		src_m = this._m;
		// コピーはFor順を変えてもOK
		for (r = this.row - 1; r >= 0; r--) {
			for (c = this.clm - 1; c >= 0; c--) {
				dest_m[r][c] = src_m[r][c];
			}
		}
		return result;
	}


	private static final double PCA_EPS = 1e-6; // #define EPS 1e-6

	private static final int PCA_MAX_ITER = 100; // #define MAX_ITER 100

	private static final double PCA_VZERO = 1e-16; // #define VZERO 1e-16

	/**
	 * static int EX( ARMat *input, ARVec *mean )の代替関数 Optimize:STEP:[144->110]
	 * 
	 * @param input
	 * @param mean
	 * @return
	 * @throws NyARException
	 */
	private void PCA_EX(NyARVec mean) throws NyARException
	{
		int lrow, lclm;
		int i, i2;
		lrow = this.row;
		lclm = this.clm;
		double[][] lm = this._m;

		if (lrow <= 0 || lclm <= 0) {
			throw new NyARException();
		}
		if (mean.getClm() != lclm) {
			throw new NyARException();
		}
		// double[] mean_array=mean.getArray();
		// mean.zeroClear();
		final double[] mean_array = mean.getArray();
		double w;
		// For順変更禁止
		for (i2 = 0; i2 < lclm; i2++) {
			w = 0.0;
			for (i = 0; i < lrow; i++) {
				// *(v++) += *(m++);
				w += lm[i][i2];
			}
			mean_array[i2] = w / lrow;// mean->v[i] /= row;
		}
	}

	/**
	 * static int CENTER( ARMat *inout, ARVec *mean )の代替関数
	 * 
	 * @param inout
	 * @param mean
	 * @return
	 */
	private static void PCA_CENTER(NyARMat inout, NyARVec mean)throws NyARException
	{
		double[] v;
		int row, clm;

		row = inout.row;
		clm = inout.clm;
		if (mean.getClm() != clm) {
			throw new NyARException();
		}
		double[][] im = inout._m;
		double[] im_i;
		double w0, w1;
		v = mean.getArray();
		// 特にパフォーマンスが劣化するclm=1と2ときだけ、別パスで処理します。
		switch (clm) {
		case 1:
			w0 = v[0];
			for (int i = 0; i < row; i++) {
				im[i][0] -= w0;
			}
			break;
		case 2:
			w0 = v[0];
			w1 = v[1];
			for (int i = 0; i < row; i++) {
				im_i = im[i];
				im_i[0] -= w0;
				im_i[1] -= w1;
			}
			break;
		default:
			for (int i = 0; i < row; i++) {
				im_i = im[i];
				for (int j = 0; j < clm; j++) {
					// *(m++) -= *(v++);
					im_i[j] -= v[j];
				}
			}
			break;
		}
		return;
	}

	/**
	 * int x_by_xt( ARMat *input, ARMat *output )の代替関数
	 * 
	 * @param input
	 * @param output
	 * @throws NyARException
	 */
	private static void PCA_x_by_xt(NyARMat input, NyARMat output) throws NyARException
	{
		NyARException.trap("動作未チェック/配列化未チェック");
		int row, clm;
		// double[][] out;
		double[] in1, in2;

		NyARException.trap("未チェックのパス");
		row = input.row;
		clm = input.clm;
		NyARException.trap("未チェックのパス");
		if (output.row != row || output.clm != row) {
			throw new NyARException();
		}

		// out = output.getArray();
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				if (j < i) {
					NyARException.trap("未チェックのパス");
					output._m[i][j] = output._m[j][i];// *out =
													// output->m[j*row+i];
				} else {
					NyARException.trap("未チェックのパス");
					in1 = input._m[i];// input.getRowArray(i);//in1 = &(input->m[clm*i]);
					in2 = input._m[j];// input.getRowArray(j);//in2 = &(input->m[clm*j]);
					output._m[i][j] = 0;// *out = 0.0;
					for (int k = 0; k < clm; k++) {
						output._m[i][j] += (in1[k] * in2[k]);// *out += *(in1++)
															// * *(in2++);
					}
				}
				// out.incPtr();
			}
		}
	}

	/**
	 * static int xt_by_x( ARMat *input, ARMat *output )の代替関数
	 * Optimize:2008.04.19
	 * 
	 * @param input
	 * @param i_output
	 * @throws NyARException
	 */
	private static void PCA_xt_by_x(NyARMat input, NyARMat i_output) throws NyARException
	{
		double[] in_;
		int row, clm;

		row = input.row;
		clm = input.clm;
		if (i_output.row != clm || i_output.clm != clm) {
			throw new NyARException();
		}

		int k, j;
		double[][] out_m = i_output._m;
		double w;
		for (int i = 0; i < clm; i++) {
			for (j = 0; j < clm; j++) {
				if (j < i) {
					out_m[i][j] = out_m[j][i];// *out = output->m[j*clm+i];
				} else {
					w = 0.0;// *out = 0.0;
					for (k = 0; k < row; k++) {
						in_ = input._m[k];// in=input.getRowArray(k);
						w += (in_[i] * in_[j]);// *out += *in1 * *in2;
					}
					out_m[i][j] = w;
				}
			}
		}
	}

	private final NyARVec wk_PCA_QRM_ev = new NyARVec(1);

	/**
	 * static int QRM( ARMat *a, ARVec *dv )の代替関数
	 * 
	 * @param a
	 * @param dv
	 * @throws NyARException
	 */
	private void PCA_QRM(NyARVec dv) throws NyARException
	{
		double w, t, s, x, y, c;
		int dim, iter;
		double[] dv_array = dv.getArray();

		dim = this.row;
		if (dim != this.clm || dim < 2) {
			throw new NyARException();
		}
		if (dv.getClm() != dim) {
			throw new NyARException();
		}

		NyARVec ev = this.wk_PCA_QRM_ev;
		ev.realloc(dim);
		double[] ev_array = ev.getArray();
		if (ev == null) {
			throw new NyARException();
		}
		final double[][] L_m = this._m;
		this.vecTridiagonalize(dv, ev, 1);

		ev_array[0] = 0.0;// ev->v[0] = 0.0;
		for (int h = dim - 1; h > 0; h--) {
			int j = h;
			while (j > 0&& Math.abs(ev_array[j]) > PCA_EPS* (Math.abs(dv_array[j - 1]) + Math.abs(dv_array[j]))) {// while(j>0 && fabs(ev->v[j]) >EPS*(fabs(dv->v[j-1])+fabs(dv->v[j])))
				// j--;
				j--;
			}
			if (j == h) {
				continue;
			}
			iter = 0;
			do {
				iter++;
				if (iter > PCA_MAX_ITER) {
					break;
				}
				w = (dv_array[h - 1] - dv_array[h]) / 2;// w = (dv->v[h-1] -dv->v[h]) / 2;//ここ？
				t = ev_array[h] * ev_array[h];// t = ev->v[h] * ev->v[h];
				s = Math.sqrt(w * w + t);
				if (w < 0) {
					s = -s;
				}
				x = dv_array[j] - dv_array[h] + t / (w + s);// x = dv->v[j] -dv->v[h] +t/(w+s);
				y = ev_array[j + 1];// y = ev->v[j+1];
				for (int k = j; k < h; k++) {
					if (Math.abs(x) >= Math.abs(y)) {
						if (Math.abs(x) > PCA_VZERO) {
							t = -y / x;
							c = 1 / Math.sqrt(t * t + 1);
							s = t * c;
						} else {
							c = 1.0;
							s = 0.0;
						}
					} else {
						t = -x / y;
						s = 1.0 / Math.sqrt(t * t + 1);
						c = t * s;
					}
					w = dv_array[k] - dv_array[k + 1];// w = dv->v[k] -dv->v[k+1];
					t = (w * s + 2 * c * ev_array[k + 1]) * s;// t = (w * s +2 * c *ev->v[k+1]) *s;
					dv_array[k] -= t;// dv->v[k] -= t;
					dv_array[k + 1] += t;// dv->v[k+1] += t;
					if (k > j) {
						NyARException.trap("未チェックパス");
						{
							ev_array[k] = c * ev_array[k] - s * y;// ev->v[k]= c *ev->v[k]- s * y;
						}
					}
					ev_array[k + 1] += s * (c * w - 2 * s * ev_array[k + 1]);// ev->v[k+1]+= s * (c* w- 2* s *ev->v[k+1]);

					for (int i = 0; i < dim; i++) {
						x = L_m[k][i];// x = a->m[k*dim+i];
						y = L_m[k + 1][i];// y = a->m[(k+1)*dim+i];
						L_m[k][i] = c * x - s * y;// a->m[k*dim+i] = c * x - s* y;
						L_m[k + 1][i] = s * x + c * y;// a->m[(k+1)*dim+i] = s* x + c * y;
					}
					if (k < h - 1) {
						NyARException.trap("未チェックパス");
						{
							x = ev_array[k + 1];// x = ev->v[k+1];
							y = -s * ev_array[k + 2];// y = -s * ev->v[k+2];
							ev_array[k + 2] *= c;// ev->v[k+2] *= c;
						}
					}
				}
			} while (Math.abs(ev_array[h]) > PCA_EPS
					* (Math.abs(dv_array[h - 1]) + Math.abs(dv_array[h])));
		}
		for (int k = 0; k < dim - 1; k++) {
			int h = k;
			t = dv_array[h];// t = dv->v[h];
			for (int i = k + 1; i < dim; i++) {
				if (dv_array[i] > t) {// if( dv->v[i] > t ) {
					h = i;
					t = dv_array[h];// t = dv->v[h];
				}
			}
			dv_array[h] = dv_array[k];// dv->v[h] = dv->v[k];
			dv_array[k] = t;// dv->v[k] = t;
			this.flipRow(h, k);
		}
	}

	/**
	 * i_row_1番目の行と、i_row_2番目の行を入れ替える。
	 * 
	 * @param i_row_1
	 * @param i_row_2
	 */
	private void flipRow(int i_row_1, int i_row_2)
	{
		int i;
		double w;
		double[] r1 = this._m[i_row_1], r2 = this._m[i_row_2];
		// For順変更OK
		for (i = clm - 1; i >= 0; i--) {
			w = r1[i];
			r1[i] = r2[i];
			r2[i] = w;
		}
	}

	/**
	 * static int EV_create( ARMat *input, ARMat *u, ARMat *output, ARVec *ev
	 * )の代替関数
	 * 
	 * @param input
	 * @param u
	 * @param output
	 * @param ev
	 * @throws NyARException
	 */
	private static void PCA_EV_create(NyARMat input, NyARMat u, NyARMat output,NyARVec ev) throws NyARException
	{
		NyARException.trap("未チェックのパス");
		int row, clm;
		row = input.row;// row = input->row;
		clm = input.clm;// clm = input->clm;
		if (row <= 0 || clm <= 0) {
			throw new NyARException();
		}
		if (u.row != row || u.clm != row) {// if( u->row != row || u->clm !=
											// row ){
			throw new NyARException();
		}
		if (output.row != row || output.clm != clm) {// if( output->row !=
														// row || output->clm !=
														// clm ){
			throw new NyARException();
		}
		if (ev.getClm() != row) {// if( ev->clm != row ){
			throw new NyARException();
		}
		double[][] m, in_;
		double[] m1, ev_array;
		double sum, work;

		NyARException.trap("未チェックのパス");
		m = output._m;// m = output->m;
		in_ = input._m;
		int i;
		ev_array = ev.getArray();
		for (i = 0; i < row; i++) {
			NyARException.trap("未チェックのパス");
			if (ev_array[i] < PCA_VZERO) {// if( ev->v[i] < VZERO ){
				break;
			}
			NyARException.trap("未チェックのパス");
			work = 1 / Math.sqrt(Math.abs(ev_array[i]));// work = 1 /
														// sqrt(fabs(ev->v[i]));
			for (int j = 0; j < clm; j++) {
				sum = 0.0;
				m1 = u._m[i];// m1 = &(u->m[i*row]);
				// m2=input.getPointer(j);//m2 = &(input->m[j]);
				for (int k = 0; k < row; k++) {
					sum += m1[k] + in_[k][j];// sum += *m1 * *m2;
					// m1.incPtr(); //m1++;
					// m2.addPtr(clm);//m2 += clm;
				}
				m1[j] = sum * work;// *(m++) = sum * work;
				// {//*(m++) = sum * work;
				// m.set(sum * work);
				// m.incPtr();}
			}
		}
		for (; i < row; i++) {
			NyARException.trap("未チェックのパス");
			ev_array[i] = 0.0;// ev->v[i] = 0.0;
			for (int j = 0; j < clm; j++) {
				m[i][j] = 0.0;
				// m.set(0.0);//*(m++) = 0.0;
				// m.incPtr();
			}
		}
	}

	private NyARMat wk_PCA_PCA_u = null;

	/**
	 * static int PCA( ARMat *input, ARMat *output, ARVec *ev )
	 * 
	 * @param output
	 * @param o_ev
	 * @throws NyARException
	 */
	private void PCA_PCA(NyARMat o_output, NyARVec o_ev) throws NyARException
	{

		int l_row, l_clm, min;
		double[] ev_array = o_ev.getArray();

		l_row = this.row;// row = input->row;
		l_clm = this.clm;// clm = input->clm;
		min = (l_clm < l_row) ? l_clm : l_row;
		if (l_row < 2 || l_clm < 2) {
			throw new NyARException();
		}
		if (o_output.clm != this.clm) {// if( output->clm != input->clm ){
			throw new NyARException();
		}
		if (o_output.row != min) {// if( output->row != min ){
			throw new NyARException();
		}
		if (o_ev.getClm() != min) {// if( ev->clm != min ){
			throw new NyARException();
		}

		NyARMat u;// u =new NyARMat( min, min );
		if (this.wk_PCA_PCA_u == null) {
			u = new NyARMat(min, min);
			this.wk_PCA_PCA_u = u;
		} else {
			u = this.wk_PCA_PCA_u;
			u.realloc(min, min);
		}

		if (l_row < l_clm) {
			NyARException.trap("未チェックのパス");
			PCA_x_by_xt(this, u);// if(x_by_xt( input, u ) < 0 ) {
		} else {
			PCA_xt_by_x(this, u);// if(xt_by_x( input, u ) < 0 ) {
		}
		u.PCA_QRM(o_ev);

		double[][] m1, m2;
		if (l_row < l_clm) {
			NyARException.trap("未チェックのパス");
			PCA_EV_create(this, u, o_output, o_ev);
		} else {
			m1 = u._m;// m1 = u->m;
			m2 = o_output._m;// m2 = output->m;
			int i;
			for (i = 0; i < min; i++) {
				if (ev_array[i] < PCA_VZERO) {// if( ev->v[i] < VZERO ){
					break;
				}
				for (int j = 0; j < min; j++) {
					m2[i][j] = m1[i][j];// *(m2++) = *(m1++);
				}
			}
			for (; i < min; i++) {// for( ; i < min; i++){
				// コードを見た限りあってそうだからコメントアウト(2008/03/26)NyARException.trap("未チェックのパス");
				ev_array[i] = 0.0;// ev->v[i] = 0.0;
				for (int j = 0; j < min; j++) {
					m2[i][j] = 0.0;// *(m2++) = 0.0;
				}
			}
		}
	}
	/**
	 * 主成分分析を実行して、結果をthisと引数へ格納します。
	 * @param o_evec
	 * @param o_ev
	 * @param o_mean
	 * @throws NyARException
	 */
	public void pca(NyARMat o_evec, NyARVec o_ev, NyARVec o_mean)throws NyARException
	{
		final double l_row = this.row;// row = input->row;
		final double l_clm = this.clm;// clm = input->clm;
		final double check=(l_row < l_clm) ? l_row : l_clm;
		
		assert l_row >= 2 || l_clm >= 2;
		assert o_evec.clm == l_clm && o_evec.row == check;
		assert o_ev.getClm() == check;
		assert o_mean.getClm() == l_clm;
		
		final double srow = Math.sqrt((double) l_row);
		PCA_EX(o_mean);

		PCA_CENTER(this, o_mean);

		int i, j;
		// For順変更OK
		for (i = 0; i < l_row; i++) {
			for (j = 0; j < l_clm; j++) {
				this._m[i][j] /= srow;// work->m[i] /= srow;
			}
		}

		PCA_PCA(o_evec, o_ev);

		double sum = 0.0;
		double[] ev_array = o_ev.getArray();
		int ev_clm = o_ev.getClm();
		// For順変更禁止
		for (i = 0; i < ev_clm; i++) {// for(int i = 0; i < ev->clm; i++ ){
			sum += ev_array[i];// sum += ev->v[i];
		}
		// For順変更禁止
		for (i = 0; i < ev_clm; i++) {// for(int i = 0; i < ev->clm; i++ ){
			ev_array[i] /= sum;// ev->v[i] /= sum;
		}
		return;
	}
	


	private final NyARVec wk_vecTridiagonalize_vec = new NyARVec(0);

	private final NyARVec wk_vecTridiagonalize_vec2 = new NyARVec(0);

	/**
	 * arVecTridiagonalize関数の代替品 a,d,e間で演算をしてる。何をどうしているかはさっぱりさっぱり
	 * 
	 * @param a
	 * @param d
	 * @param e
	 * @param i_e_start
	 *            演算開始列(よくわからないけどarVecTridiagonalizeの呼び出し元でなんかしてる)
	 * @return
	 * @throws NyARException
	 */
	private void vecTridiagonalize(NyARVec d, NyARVec e, int i_e_start)throws NyARException
	{
		NyARVec vec = wk_vecTridiagonalize_vec;
		// double[][] a_array=a.getArray();
		double s, t, p, q;
		int dim;

		if (this.clm != this.row) {// if(a.getClm()!=a.getRow()){
			throw new NyARException();
		}
		if (this.clm != d.getClm()) {// if(a.getClm() != d.clm){
			throw new NyARException();
		}
		if (this.clm != e.getClm()) {// if(a.getClm() != e.clm){
			throw new NyARException();
		}
		dim = this.getClm();

		double[] d_vec, e_vec;
		d_vec = d.getArray();
		e_vec = e.getArray();
		double[] a_vec_k;

		for (int k = 0; k < dim - 2; k++) {

			a_vec_k = this._m[k];
			vec.setNewArray(a_vec_k, clm);// vec=this.getRowVec(k);//double[]
											// vec_array=vec.getArray();
			NyARException.trap("未チェックパス");
			d_vec[k] = a_vec_k[k];// d.v[k]=vec.v[k];//d.set(k,v.get(k));
									// //d->v[k] = v[k];

			// wv1.clm = dim-k-1;
			// wv1.v = &(v[k+1]);
			NyARException.trap("未チェックパス");
			e_vec[k + i_e_start] = vec.vecHousehold(k + 1);// e.v[k+i_e_start]=vec.vecHousehold(k+1);//e->v[k]= arVecHousehold(&wv1);
			if (e_vec[k + i_e_start] == 0.0) {// if(e.v[k+i_e_start]== 0.0){//if(e.v[k+i_e_start]== 0.0){
				continue;
			}

			for (int i = k + 1; i < dim; i++) {
				s = 0.0;
				for (int j = k + 1; j < i; j++) {
					NyARException.trap("未チェックのパス");
					s += this._m[j][i] * a_vec_k[j];// s += a_array[j][i] *vec.v[j];//s +=a.get(j*dim+i) *v.get(j);//s +=a->m[j*dim+i] * v[j];
				}
				for (int j = i; j < dim; j++) {
					NyARException.trap("未チェックのパス");
					s += this._m[i][j] * a_vec_k[j];// s += a_array[i][j] *vec.v[j];//s +=a.get(i*dim+j) *v.get(j);//s +=a->m[i*dim+j] * v[j];
				}
				NyARException.trap("未チェックのパス");
				d_vec[i] = s;// d.v[i]=s;//d->v[i] = s;
			}

			// wv1.clm = wv2.clm = dim-k-1;
			// wv1.v = &(v[k+1]);
			// wv2.v = &(d->v[k+1]);
			a_vec_k = this._m[k];
			vec.setNewArray(a_vec_k, clm);// vec=this.getRowVec(k);
			// vec_array=vec.getArray();
			NyARException.trap("未チェックパス");
			t = vec.vecInnerproduct(d, k + 1) / 2;
			for (int i = dim - 1; i > k; i--) {
				NyARException.trap("未チェックパス");
				p = a_vec_k[i];// p = v.get(i);//p = v[i];
				d_vec[i] -= t * p;
				q = d_vec[i];// d.v[i]-=t*p;q=d.v[i];//q = d->v[i] -= t*p；
				for (int j = i; j < dim; j++) {
					NyARException.trap("未チェックパス");
					this._m[i][j] -= p * (d_vec[j] + q * a_vec_k[j]);// a.m[i][j]-=p*(d.v[j] +q*vec.v[j]);//a->m[i*dim+j] -=p*(d->v[j]) + q*v[j];
				}
			}
		}

		if (dim >= 2) {
			d_vec[dim - 2] = this._m[dim - 2][dim - 2];// d.v[dim-2]=a.m[dim-2][dim-2];//d->v[dim-2]=a->m[(dim-2)*dim+(dim-2)];
			e_vec[dim - 2 + i_e_start] = this._m[dim - 2][dim - 1];// e.v[dim-2+i_e_start]=a.m[dim-2][dim-1];//e->v[dim-2] = a->m[(dim-2)*dim+(dim-1)];
		}

		if (dim >= 1) {
			d_vec[dim - 1] = this._m[dim - 1][dim - 1];// d.v[dim-1]=a_array[dim-1][dim-1];//d->v[dim-1] =a->m[(dim-1)*dim+(dim-1)];
		}
		NyARVec vec2 = this.wk_vecTridiagonalize_vec2;
		for (int k = dim - 1; k >= 0; k--) {
			a_vec_k = this._m[k];
			vec.setNewArray(a_vec_k, clm);// vec=this.getRowVec(k);//v =a.getPointer(k*dim);//v = &(a->m[k*dim]);
			if (k < dim - 2) {
				for (int i = k + 1; i < dim; i++) {
					// wv1.clm = wv2.clm = dim-k-1;
					// wv1.v = &(v[k+1]);
					// wv2.v = &(a->m[i*dim+k+1]);
					vec2.setNewArray(this._m[i], clm);// vec2=this.getRowVec(i);

					t = vec.vecInnerproduct(vec2, k + 1);
					for (int j = k + 1; j < dim; j++) {
						NyARException.trap("未チェックパス");
						this._m[i][j] -= t * a_vec_k[j];// a_array[i][j]-=t*vec.v[j];//a.subValue(i*dim+j,t*v.get(j));//a->m[i*dim+j]-= t * v[j];
					}
				}
			}
			for (int i = 0; i < dim; i++) {
				a_vec_k[i] = 0.0;// v.set(i,0.0);//v[i] = 0.0;
			}
			a_vec_k[k] = 1;// v.set(k,1);//v[k] = 1;
		}
		return;
	}
}