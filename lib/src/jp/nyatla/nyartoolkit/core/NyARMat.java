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
 * Copyright (C)2008-2012 Ryo Iizuka
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

import jp.nyatla.nyartoolkit.core.types.matrix.*;

/**
 * このクラスは、可変サイズの行列計算機能を提供します。
 * ARToolKitのARMat構造体と、その処理関数に相当する機能があります。
 * 2x2,3x3,4x4行列については、NyAToolKitの行列計算クラス({@link NyARDoubleMatrix44},{@link NyARDoubleMatrix33},{@link NyARDoubleMatrix22},)
 * がありますので、こちらを使ってください。
 * <p>memo:
 * このクラスは、今後統合・削除する可能性があります。
 * </p>
 */
public class NyARMat
{
	/**
	 * 配列値を格納するバッファ。配列サイズと行列サイズは必ずしも一致しないことに注意。配列のサイズを行列の大きさとして使わないこと！
	 */
	protected double[][] _m;
	/**
	 * 逆行列計算に使うワークエリア
	 */
	private int[] __matrixSelfInv_nos;
	/**
	 * 列数
	 */
	protected int clm;
	/**
	 * 行数
	 */
	protected int row;
	/**
	 * デフォルトコンストラクタ。
	 * 機能しません。
	 * @throws NyARException
	 */
	protected NyARMat() throws NyARException
	{
		throw new NyARException();
	}
	/**
	 * 配列i_mをラップしてインスタンスを生成します。
	 * 
	 * @param i_row
	 * @param i_clm
	 * @param i_m
	 * @param i_is_attached_buffer
	 * i_mをインスタンスが管理するかを示します。trueの場合、i_mの所有権はインスタンスに移ります。
	 */
	public NyARMat(int i_row, int i_clm,double[][] i_m,boolean i_is_attached_buffer)
	{
		this.clm=i_clm;
		this.row=i_row;
		this._m=i_m;
	}
	
	/**
	 * コンストラクタです。
	 * 行列のサイズを指定して、NyARMatオブジェクトを作成します。
	 * @param i_row
	 * 行数です。
	 * @param i_clm
	 * 列数です。
	 */
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
	 * 行列の列数。
	 */
	public int getClm()
	{
		return clm;
	}
	/**
	 * 行列の行数を返します。
	 * @return
	 * 行列の列数
	 */
	public int getRow()
	{
		return row;
	}	
	/**
	 * 行列のサイズを変更します。
	 * 関数を実行すると、行列の各値は不定になります。
	 * @param i_row
	 * 新しい行数
	 * @param i_clm
	 * 新しい列数。
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
	 * 行列同士の掛け算を実行します。
	 * i_mat_aとi_mat_bの積を計算して、thisへ格納します。
	 * @param i_mat_a
	 * 計算元の行列A
	 * @param i_mat_b
	 * 計算元の行列B
	 * @throws NyARException
	 */
	public void mul(NyARMat i_mat_a, NyARMat i_mat_b) throws NyARException
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
	 * @return
	 *　逆行列が計算できたかの、真偽値を返します。trueなら、逆行列が存在します。falseなら、逆行列はありません。
	 * 失敗すると、thisの内容は不定になります。
	 * @throws NyARException
	 */
	public boolean inverse() throws NyARException
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
		//ipが定まらないで計算が行われる場合があるので挿入。 ループ内で0初期化していいかが判らない。
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
	 * i_copy_fromの内容を、thisへコピーします。 
	 * @param i_copy_from
	 * コピー元の行列です。
	 * この行列のサイズは、thisと同じでなければなりません。
	 */
	public void setValue(NyARMat i_copy_from) throws NyARException
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
	/**
	 * 行列のバッファを返します。
	 * 返却値の有効期間に注意してください。
	 * この値の有効時間は、次にこのこのインスタンスの関数を実行するまでの間です。
	 * @return
	 * 行列のバッファ
	 */
	public double[][] getArray()
	{
		return _m;
	}
	/**
	 * 行列の要素を、全て0にします。
	 */
	public void loadZero()
	{
		// For順変更OK
		for (int i = this.row - 1; i >= 0; i--) {
			for (int i2 = this.clm - 1; i2 >= 0; i2--) {
				this._m[i][i2] = 0.0;
			}
		}
	}
	/**
	 * i_srcの転置行列をdestに得ます。
	 * この関数は未チェックの為、実行すると例外が発生します。
	 * @param dest
	 * 出力先のオブジェクト
	 * @param source
	 * 入力元のオブジェクト
	 */
	public void transpose(NyARMat i_src) throws NyARException
	{
		if (this.row != i_src.clm || this.clm != i_src.row) {
			throw new NyARException();
		}
		for (int r = 0; r < this.row; r++) {
			for (int c = 0; c < this.clm; c++) {
				this._m[r][c] = i_src._m[c][r];
			}
		}		
	}

	/**
	 * ARToolKitの、arMatrixUnit関数と同等な関数です。unitを単位行列に初期化します。
	 * この関数は未チェックの為、実行すると例外が発生します。
	 * @param unit
	 * 操作するオブジェクト。
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















	



	/** @deprecated */
	public void zeroCrear(){this.loadZero();}
	/** @deprecated */
	public void copyFrom(NyARMat i_copy_from) throws NyARException {this.setValue(i_copy_from);}
	/** @deprecated */
	public boolean matrixSelfInv() throws NyARException{ return this.inverse();}
	/** @deprecated */
	public void matrixMul(NyARMat i_mat_a, NyARMat i_mat_b) throws NyARException{this.mul(i_mat_a, i_mat_b);}

}