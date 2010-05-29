package jp.nyatla.nyartoolkit.core.analyzer.histogram;

import jp.nyatla.nyartoolkit.core.types.NyARHistogram;


/**
 * kittlerThresholdの方式で閾値を求めます。
 * @param i_histogram
 * @return
 */
public class NyARHistogramAnalyzer_KittlerThreshold implements INyARHistogramAnalyzer_Threshold
{
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
