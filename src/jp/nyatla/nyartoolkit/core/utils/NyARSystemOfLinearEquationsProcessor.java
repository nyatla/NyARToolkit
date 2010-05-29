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
package jp.nyatla.nyartoolkit.core.utils;

/**
 * 連立方程式を解くためのプロセッサクラスです。
 *
 */
public class NyARSystemOfLinearEquationsProcessor
{
	/**
	 * i_reftとi_rightの整合性を確認します。
	 * @param i_left
	 * @param i_right
	 * @return
	 */
	private static boolean isValid2dArray(double[][] i_left,double[] i_right)
	{		
		final int sm=i_left.length;
		final int sn=i_left[0].length;
		if(i_left.length!=sm){
			return false;
		}
		if(i_right.length!=sm){
			return false;
		}
		for(int i=1;i<sm;i++){
			if(i_left[i].length!=sn){
				return false;
			}
		}
		return true;
	}
	/**
	 * [i_left_src]=[i_right_src]の式にガウスの消去法を実行して、[x][x]の要素が1になるように基本変形します。
	 * i_mとi_nが等しくない時は、最終行までの[x][x]要素までを1になるように変形します。
	 * @param i_left
	 * 連立方程式の左辺値を指定します。[i_m][i_n]の配列を指定してください。
	 * @param i_right
	 * 連立方程式の右辺値を指定します。[i_m][i_n]の配列を指定してください。
	 * @param i_n
	 * 連立方程式の係数の数を指定します。
	 * @param i_m
	 * 連立方程式の数を指定します。
	 * @return
	 * 最終行まで基本変形ができてばtrueを返します。
	 */
	public static boolean doGaussianElimination(double[][] i_left,double[] i_right,int i_n,int i_m)
	{
		//整合性を確認する.
		assert isValid2dArray(i_left,i_right);
		

		//1行目以降
		for(int solve_row=0;solve_row<i_m;solve_row++)
		{
			{//ピボット操作
				int pivod=solve_row;
				double pivod_value=Math.abs(i_left[pivod][pivod]);
				for(int i=solve_row+1;i<i_m;i++){
					final double pivod_2=Math.abs(i_left[i][pivod]);
					if(pivod_value<Math.abs(pivod_2)){
						pivod=i;
						pivod_value=pivod_2;
					}
				}
				if(solve_row!=pivod){
					//行の入れ替え(Cの時はポインタテーブル使って！)
					final double[] t=i_left[solve_row];
					i_left[solve_row]=i_left[pivod];
					i_left[pivod]=t;
					final double t2=i_right[solve_row];
					i_right[solve_row]=i_right[pivod];
					i_right[pivod]=t2;
				}
			}
			final double[] dest_l_n=i_left[solve_row];
			final double dest_l_nn=i_left[solve_row][solve_row];
			if(dest_l_nn==0.0){
				//選択後の対角要素が0になってしまったら失敗する。
				return false;
			}			

			//消去計算(0 - solve_row-1項までの消去)
			for(int i=0;i<solve_row;i++){
				double s=dest_l_n[i];
				for(int i2=0;i2<i_n;i2++)
				{
					final double p=i_left[i][i2]*s;
					dest_l_n[i2]=dest_l_n[i2]-p;
				}
				final double k=i_right[i]*s;
				i_right[solve_row]=i_right[solve_row]-k;
				
			}
			//消去法の実行(割り算)
			final double d=dest_l_n[solve_row];
			for(int i2=0;i2<solve_row;i2++){
				dest_l_n[i2]=0;
			}
			if(d!=1.0){
				dest_l_n[solve_row]=1.0;
				for(int i=solve_row+1;i<i_n;i++){
					dest_l_n[i]/=d;
				}
				i_right[solve_row]/=d;
			}
		}
		return true;	
	}
	/**
	 * i_leftとi_rightの連立方程式を解いて、i_left,i_right内容を更新します。
	 * i_right[n]の内容が、i_left[x][n]番目の係数の解になります。
	 * @return
	 * 方程式が解ければtrueを返します。
	 */
	public static boolean solve(double[][] i_left,double[] i_right,int i_number_of_system)
	{
		return doGaussianElimination(i_left,i_right,i_number_of_system,i_number_of_system);
	}
}
