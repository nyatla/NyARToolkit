/* 
 * PROJECT: NyARToolkit Professional
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2012 Ryo Iizuka
 * wm@nyatla.jp
 * http://nyatla.jp
 * 
 * This work is based on the ARToolKit4.
 * Copyright 2010-2011 ARToolworks, Inc. All rights reserved.
 *
 */
package jp.nyatla.nyartoolkit.dev.pro.core.kpm.ann;

import jp.nyatla.nyartoolkit.pro.core.surf.NyARSurfDescriptor;

public class NyARLinearFeatureSearch extends NyARSurfAnnMatch
{
	protected double annThresh = ANN_THRESH;
	
	public NyARLinearFeatureSearch(NyARSurfFeatureSet i_template)
	{
		super(i_template);
	}
//	/**
//	 * 制限付で距離計算をします�?�i_thを�?える距離を計算しません�?
//	 * @param i_v
//	 * @param i_th
//	 * @return
//	 * i_thを�?える場合�?�{@link Double#MAX_VALUE}を返します�??
//	 */
//	private static double distLimit(double[] a,double[] b,double i_th) {
//		assert (a.length == b.length);
//		double ret = 0;
//		int i = a.length - 1;
//		//はじめは4回単位で計�?
//		for (; i >= 3; i -= 4) {
//			double d1 = (a[i] - b[i]);
//			double d2 = (a[i - 1] - b[i - 1]);
//			double d3 = (a[i - 2] - b[i - 2]);
//			double d4 = (a[i - 3] - b[i - 3]);
//			ret += d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4;
//			if(i_th<ret){
//				return Double.MAX_VALUE;
//			}
//		}
//		//1回単�?
//		for (; i >= 0; i--) {
//			double d = (a[i] - b[i]);
//			ret += d * d;
//		}
//		if(i_th<ret){
//			return Double.MAX_VALUE;
//		}
//		return ret;
//	}	
//	private static double matchVal(double[] i_a,double[] i_b)
//	{
//		double qds=0;
//		int l=i_a.length-1;
//		for(;l>=4;l-=4){
//			qds+=(i_a[l  ]*i_b[l  ]+i_a[l-1]*i_b[l-1]+i_a[l-2]*i_b[l-2]+i_a[l-3]*i_b[l-3]);
//			//判�?
//		}
//		for(;l>=0;l--){
//			qds+=i_a[l]*i_b[l];
//		}
//		return 2*(1-qds);
//	}
	
	
	public void match(NyARSurfDescriptor i_query,Result o_result)
	{
		NyARSurfFeatureSet.Item[] items = this._template.getItems();
		//クエリに�?致する特徴セ�?トをresultへ返す�?
		for (int i = 0; i < i_query.getLength(); i++) {
			//クエリに�?致する�?ータの検索
			NyARSurfDescriptor.Item key=i_query.getItem(i);
			double[] a=key.descriptor;
			double min_dist=this.annThresh*1.1;//敷�?値より少し大きめ
			NyARSurfFeatureSet.Item match = null;
			for (int i2 = items.length-1; i2 >=0; i2--)
			{
				double[] b=items[i2].v;
				double dist = 0;
//				double dist =distLimit(a,b,min_dist);
				{
					int i3 = a.length - 1;
					//はじめは4回単位で計�?
					for (; i3 >= 3; i3 -= 4) {
						double d1 = (a[i3] - b[i3]);
						double d2 = (a[i3 - 1] - b[i3 - 1]);
						double d3 = (a[i3 - 2] - b[i3 - 2]);
						double d4 = (a[i3 - 3] - b[i3 - 3]);
						dist += d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4;
						if(min_dist<dist){
							i3=-1;
							break;
						}
					}
					//1回単�?
					for (; i3 >= 0; i3--) {
						double d = (a[i3] - b[i3]);
						dist += d * d;
					}
				}
				if (dist < min_dist) {
					min_dist = dist;
					match = items[i2];
				}
			}
			if(min_dist > this.annThresh)
			{
				continue;
			}
			ResultItem item=o_result.prePush();
			if(item==null){
				return;
			}
			item.dist=min_dist;
			item.feature=match;
			item.key=key;
		}
		return;
	}

	/*
	public void match(SurfDescriptor i_query,Result o_result)
	{
		SurfFeatureSet.Item[] items = this._template.getItems();
		//クエリに�?致する特徴セ�?トをresultへ返す�?
		for (int i = 0; i < i_query.getLength(); i++) {			

			//クエリに�?致する�?ータの検索
			SurfDescriptor.Item key=i_query.getItem(i);
			double min_dist = items[items.length-1].dist(key.descriptor);
			SurfFeatureSet.Item match = items[items.length-1];
			for (int i2 = items.length-2; i2 >=0; i2--) {
				double dist = items[i2].limitedDist(key.descriptor,min_dist);
				if (dist < min_dist) {
					min_dist = dist;
					match = items[i2];
				}
			}
			if(min_dist > this.annThresh)
			{
				continue;
			}
			ResultItem item=o_result.prePush();
			if(item==null){
				return;
			}
			item.dist=min_dist;
			item.feature=match;
			item.key=key;
		}
		return;
	}*/

}