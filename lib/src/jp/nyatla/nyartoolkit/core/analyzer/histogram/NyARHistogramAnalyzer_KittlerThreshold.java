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
package jp.nyatla.nyartoolkit.core.analyzer.histogram;

import jp.nyatla.nyartoolkit.core.types.NyARHistogram;


/**
 * このクラスは、kittlerThresholdを用いて敷居値を求める機能を提供します。
 */
public class NyARHistogramAnalyzer_KittlerThreshold implements INyARHistogramAnalyzer_Threshold
{
	/**
	 * この関数は、kittlerThresholdを用いて敷居値を1個求めます。敷居値の範囲は、i_histogram引数の範囲と同じです。
	 */
	public int getThreshold(NyARHistogram i_histogram)
	{
		int i;		
		double min=Double.MAX_VALUE;
		int th=0;
		int da,sa,db,sb,pa,pb;
		double oa,ob;
		
		int[] hist=i_histogram.data;
		int n=i_histogram.length;
		//Low側
		da=pa=0;
		int h;
		for(i=0;i<n;i++){
			h=hist[i];
			da+=h*i;	//i*h[i]
			pa+=h*i*i;	//i*i*h[i]
		}
		sa=i_histogram.total_of_data;
		//High側(i=n-1)
		db=0;
		sb=0;
		pb=0;
		
		
		for(i=n-1;i>0;i--){
			//次のヒストグラムを計算
			int hist_count=hist[i];//h[i]
			int hist_val =hist_count*i;  //h[i]*i
			int hist_val2=hist_val*i;    //h[i]*i*i
			da-=hist_val;
			sa-=hist_count;
			pa-=hist_val2;
			db+=hist_val;
			sb+=hist_count;			
			pb+=hist_val2;

			//初期化
			double wa=(double)sa/(sa+sb);
			double wb=(double)sb/(sa+sb);
			if(wa==0 || wb==0){
				continue;
			}

			oa=ob=0;
			double ma=sa!=0?(double)da/sa:0;
			//Σ(i-ma)^2*h[i]=Σ(i^2*h[i])+Σ(ma^2*h[i])-Σ(2*i*ma*h[i])
			oa=((double)(pa+ma*ma*sa-2*ma*da))/sa;

			double mb=sb!=0?(double)db/sb:0;
			//Σ(i-mb)^2*h[i]=Σ(i^2*h[i])+Σ(mb^2*h[i])-Σ(2*i*mb*h[i])
			ob=((double)(pb+mb*mb*sb-2*mb*db))/sb;

			double kai=wa*Math.log(oa/wa)+wb*Math.log(ob/wb);
			if(kai>0 && min>kai){
				min=kai;
				th=i;
			}
			//System.out.println(kai);

		}
		return th;//129//7.506713872738873
	}
	/**
	 * デバック用関数
	 * @param args
	 * main関数引数
	 */	
	public static void main(String[] args)
	{
		NyARHistogram data=new NyARHistogram(256);
		for(int i=0;i<256;i++){
			data.data[i]=128-i>0?128-i:i-128;
		}
		data.total_of_data=data.getTotal(0,255);
		NyARHistogramAnalyzer_KittlerThreshold an=new NyARHistogramAnalyzer_KittlerThreshold();
		int th=an.getThreshold(data);
		System.out.print(th);
		return;
	}
}
