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
package jp.nyatla.nyartoolkit.sandbox.x2;

import jp.nyatla.nyartoolkit.NyARException;



/**
 * ARMat構造体に対応するクラス typedef struct { double *m; int row; int clm; }ARMat;
 * 
 */
public class NyARFixedFloat16Mat
{
	/**
	 * 配列サイズと行列サイズは必ずしも一致しないことに注意 返された配列のサイズを行列の大きさとして使わないこと！
	 * 
	 */
	protected long[][] m;
	private int clm, row;

	/**
	 * デフォルトコンストラクタは機能しません。
	 * 
	 * @throws NyARException
	 */
	protected NyARFixedFloat16Mat() throws NyARException
	{
		throw new NyARException();
	}

	public NyARFixedFloat16Mat(int i_row, int i_clm)
	{
		this.m = new long[i_row][i_clm];
		clm = i_clm;
		row = i_row;
		return;
	}
	public int getClm()
	{
		return clm;
	}
	public int getRow()
	{
		return row;
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
				m[i][i2] = 0;
			}
		}
	}
	public long[][] getArray()
	{
		return this.m;
	}
	// public void getRowVec(int i_row,NyARVec o_vec)
	// {
	// o_vec.set(this.m[i_row],this.clm);
	// }
	/**
	 * aとbの積を自分自身に格納する。arMatrixMul()の代替品
	 * 
	 * @param a
	 * @param b
	 * @throws NyARException
	 */
	public void matrixMul(NyARFixedFloat16Mat a, NyARFixedFloat16Mat b) throws NyARException
	{
		if (a.clm != b.row || this.row != a.row || this.clm != b.clm) {
			throw new NyARException();
		}
		long w;
		int r, c, i;
		long[][] am = a.m, bm = b.m, dm = this.m;
		// For順変更禁止
		for (r = 0; r < this.row; r++) {
			for (c = 0; c < this.clm; c++) {
				w = 0L;// dest.setARELEM0(r, c,0.0);
				for (i = 0; i < a.clm; i++) {
					w += (am[r][i] * bm[i][c])>>16;// ARELEM0(dest, r, c) +=ARELEM0(a, r, i) * ARELEM0(b,i, c);
				}
				dm[r][c] = w;
			}
		}
	}
}