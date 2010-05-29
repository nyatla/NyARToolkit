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

import jp.nyatla.nyartoolkit.*;
/**
 * 方程式を解く関数を定義します。
 *
 */
public class NyAREquationSolver
{
	public static int solve2Equation(double i_a, double i_b, double i_c,double[] o_result)
	{
		assert i_a!=0;
		return solve2Equation(i_b/i_a,i_c/i_a,o_result,0);
	}
	
	public static int solve2Equation(double i_b, double i_c,double[] o_result)
	{
		return solve2Equation(i_b,i_c,o_result,0);
	}
	
	public static int solve2Equation(double i_b, double i_c,double[] o_result,int i_result_st)
	{
		double t=i_b*i_b-4*i_c;
		if(t<0){
			//虚数根
			return 0;
		}
		if(t==0){
			//重根
			o_result[i_result_st+0]=-i_b/(2);
			return 1;
		}
		//実根２個
		t=Math.sqrt(t);
		o_result[i_result_st+0]=(-i_b+t)/(2);
		o_result[i_result_st+1]=(-i_b-t)/(2);
		return 2;
	}

	/**
	 * ３次方程式 a*x^3+b*x^2+c*x+d=0の実根を求める。	 
	 * http://aoki2.si.gunma-u.ac.jp/JavaScript/src/3jisiki.html
	 * のコードを基にしてます。
	 * @param i_a
	 * X^3の係数
	 * @param i_b
	 * X^2の係数
	 * @param i_c
	 * X^1の係数
	 * @param i_d
	 * X^0の係数
	 * @param o_result
	 * 実根。double[3]を指定すること。
	 * @return
	 */
	public static int solve3Equation(double i_a, double i_b, double i_c, double i_d,double[] o_result)
	{
		assert (i_a != 0);
		return solve3Equation(i_b/i_a,i_c/i_a,i_d/i_a,o_result);
	}
	
	/**
	 * ３次方程式 x^3+b*x^2+c*x+d=0の実根を求める。
	 * だけを求める。
	 * http://aoki2.si.gunma-u.ac.jp/JavaScript/src/3jisiki.html
	 * のコードを基にしてます。
	 * @param i_b
	 * X^2の係数
	 * @param i_c
	 * X^1の係数
	 * @param i_d
	 * X^0の係数
	 * @param o_result
	 * 実根。double[1]以上を指定すること。
	 * @return
	 */
	public static int solve3Equation(double i_b, double i_c, double i_d,double[] o_result)
	{
		double tmp,b,   p, q;
		b = i_b/(3);
		p = b * b - i_c / 3;
		q = (b * (i_c - 2 * b * b) - i_d) / 2;
		if ((tmp = q * q - p * p * p) == 0) {
			// 重根
			q = Math.cbrt(q);
			o_result[0] = 2 * q - b;
			o_result[1] = -q - b;
			return 2;
		} else if (tmp > 0) {
			// 実根1,虚根2
			double a3 = Math.cbrt(q + ((q > 0) ? 1 : -1) * Math.sqrt(tmp));
			double b3 = p / a3;
			o_result[0] = a3 + b3 - b;
			// 虚根:-0.5*(a3+b3)-b,Math.abs(a3-b3)*Math.sqrt(3.0)/2
			return 1;
		} else {
			// 実根3
			tmp = 2 * Math.sqrt(p);
			double t = Math.acos(q / (p * tmp / 2));
			o_result[0] = tmp * Math.cos(t / 3) - b;
			o_result[1] = tmp * Math.cos((t + 2 * Math.PI) / 3) - b;
			o_result[2] = tmp * Math.cos((t + 4 * Math.PI) / 3) - b;
			return 3;
		}
	}

	
	
	/**
	 * ４次方程式の実根だけを求める。
	 * @param i_a
	 * X^3の係数
	 * @param i_b
	 * X^2の係数
	 * @param i_c
	 * X^1の係数
	 * @param i_d
	 * X^0の係数
	 * @param o_result
	 * 実根。double[3]を指定すること。
	 * @return
	 */
	public static int solve4Equation(double i_a, double i_b, double i_c, double i_d,double i_e,double[] o_result) throws NyARException
	{
		assert (i_a != 0);
		double A3,A2,A1,A0,B3;
		A3=i_b/i_a;
		A2=i_c/i_a;
		A1=i_d/i_a;
		A0=i_e/i_a;
		B3=A3/4;
		double p,q,r;
		double B3_2=B3*B3;
		p=A2-6*B3_2;//A2-6*B3*B3;
		q=A1+B3*(-2*A2+8*B3_2);//A1-2*A2*B3+8*B3*B3*B3;
		r=A0+B3*(-A1+A2*B3)-3*B3_2*B3_2;//A0-A1*B3+A2*B3*B3-3*B3*B3*B3*B3;
		if(q==0){
			double result_0,result_1;
			//複二次式
			int res=solve2Equation(p,r,o_result,0);
			switch(res){
			case 0:
				//全て虚数解
				return 0;
			case 1:
				//重根
				//解は0,1,2の何れか。
				result_0=o_result[0];
				if(result_0<0){
					//全て虚数解
					return 0;
				}
				//実根1個
				if(result_0==0){
					//NC
					o_result[0]=0-B3;
					return 1;
				}
				//実根2個
				result_0=Math.sqrt(result_0);
				o_result[0]=result_0-B3;
				o_result[1]=-result_0-B3;
				return 2;
			case 2:
				//実根２個だからt==t2==0はありえない。(case1)
				//解は、0,2,4の何れか。
				result_0=o_result[0];
				result_1=o_result[1];
				int number_of_result=0;
				if(result_0>0){
					//NC
					result_0=Math.sqrt(result_0);
					o_result[0]= result_0-B3;
					o_result[1]=-result_0-B3;
					number_of_result+=2;
				}
				if(result_1>0)
				{
					//NC
					result_1=Math.sqrt(result_1);
					o_result[number_of_result+0]= result_1-B3;
					o_result[number_of_result+1]=-result_1-B3;
					number_of_result+=2;
				}
				return number_of_result;
			default:
				throw new NyARException();
			}
		}else{
			//それ以外
			//最適化ポイント:
			//u^3  + (2*p)*u^2  +((- 4*r)+(p^2))*u -q^2= 0
			double u=solve3Equation_1((2*p),(- 4*r)+(p*p),-q*q);
			if(u<0){
				//全て虚数解
				return 0;
			}
			double ru=Math.sqrt(u);
			//2次方程式を解いてyを計算(最適化ポイント)
			int result_1st,result_2nd;
			result_1st=solve2Equation(-ru,(p+u)/2+ru*q/(2*u),o_result,0);
			//配列使い回しのために、変数に退避
			switch(result_1st){
			case 0:
				break;
			case 1:
				o_result[0]=o_result[0]-B3;
				break;
			case 2:
				o_result[0]=o_result[0]-B3;
				o_result[1]=o_result[1]-B3;
				break;
			default:
				throw new NyARException();
			}
			result_2nd=solve2Equation(ru,(p+u)/2-ru*q/(2*u),o_result,result_1st);
			//0,1番目に格納
			switch(result_2nd){
			case 0:
				break;
			case 1:
				o_result[result_1st+0]=o_result[result_1st+0]-B3;
				break;
			case 2:
				o_result[result_1st+0]=o_result[result_1st+0]-B3;
				o_result[result_1st+1]=o_result[result_1st+1]-B3;
				break;
			default:
				throw new NyARException();
			}
			return result_1st+result_2nd;
		}
	}
	/**
	 * 3次方程式の実根を１個だけ求める。
	 * 4字方程式で使う。
	 * @param i_b
	 * @param i_c
	 * @param i_d
	 * @param o_result
	 * @return
	 */
	private static double solve3Equation_1(double i_b, double i_c, double i_d)
	{
		double tmp,b,   p, q;
		b = i_b/(3);
		p = b * b - i_c / 3;
		q = (b * (i_c - 2 * b * b) - i_d) / 2;
		if ((tmp = q * q - p * p * p) == 0) {
			// 重根
			q = Math.cbrt(q);
			return 2 * q - b;
		} else if (tmp > 0) {
			// 実根1,虚根2
			double a3 = Math.cbrt(q + ((q > 0) ? 1 : -1) * Math.sqrt(tmp));
			double b3 = p / a3;
			return a3 + b3 - b;
		} else {
			// 実根3
			tmp = 2 * Math.sqrt(p);
			double t = Math.acos(q / (p * tmp / 2));
			return tmp * Math.cos(t / 3) - b;
		}
	}		
/*
	public static void main(String[] args)
	{
		NyAREquationSolver n = new NyAREquationSolver();
		int l=0;
		double[] r = new double[10];
		try{
			l=n.solve4Equation(1, 9, -18, -68, 120, r);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(l);
	}*/
}
