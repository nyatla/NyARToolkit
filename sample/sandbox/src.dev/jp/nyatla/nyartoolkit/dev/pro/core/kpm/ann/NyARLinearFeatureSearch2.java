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

/**
 * LinearFeatureSearchの計算部を整数化したもの
 */
public class NyARLinearFeatureSearch2 extends NyARSurfAnnMatch
{
	protected double annThresh = ANN_THRESH;
	private int[][] _int_feature;
	private final int  INT_SCALE=16384;
	public NyARLinearFeatureSearch2(NyARSurfFeatureSet i_template)
	{
		super(i_template);
		int d_len=i_template.getDescDim();
		//ハッシュ�?ーブルの生�??
		this._int_feature=new int[i_template._items.length][d_len];
		for(int i=this._int_feature.length-1;i>=0;i--){
			for(int i2=d_len-1;i2>=0;i2--){
				this._int_feature[i][i2]=(int)(INT_SCALE*i_template._items[i].v[i2]);
			}
		}
		this._query_v=new int[d_len];
		
	}
	

	private int[] _query_v=new int[64];
	public void match(NyARSurfDescriptor i_query,Result o_result)
	{
		NyARSurfFeatureSet.Item[] items = this._template.getItems();
		//クエリに�?致する特徴セ�?トをresultへ返す�?
		for (int i = 0; i < i_query.getLength(); i++) {
			//クエリに�?致する�?ータの検索
			NyARSurfDescriptor.Item key=i_query.getItem(i);

			//クエリの特徴量を整数へ変換
			double[] kv=key.descriptor;
			for(int i2=kv.length-1;i2>=0;i2--){
				this._query_v[i2]=(int)(INT_SCALE*kv[i2]);
			}
			int[] a=this._query_v;
			int min_dist=(int)(this.annThresh*INT_SCALE*INT_SCALE)+1;

			NyARSurfFeatureSet.Item match = items[items.length-1];
			for (int i2 = items.length-1; i2 >=0; i2--){
				int[] b=this._int_feature[i2];
				int dist = 0;
//				double dist =distLimit(a,b,min_dist);
				{
					int i3 = a.length - 1;
					//はじめは4回単位で計�?
					for (; i3 >= 3; i3 -= 4) {
						int d1 = (a[i3] - b[i3]);
						int d2 = (a[i3 - 1] - b[i3 - 1]);
						int d3 = (a[i3 - 2] - b[i3 - 2]);
						int d4 = (a[i3 - 3] - b[i3 - 3]);
						dist += d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4;
						if(min_dist<dist){
							i3=-1;
							break;
						}
					}
					//1回単�?
					for (; i3 >= 0; i3--) {
						int d = (a[i3] - b[i3]);
						dist += d * d;
					}
				}
				if (dist < min_dist) {
					min_dist = dist;
					match = items[i2];
				}
			}
			if(min_dist > this.annThresh*INT_SCALE*INT_SCALE)
			{
				continue;
			}
			ResultItem item=o_result.prePush();
			if(item==null){
				return;
			}

			item.dist=(double)min_dist/(INT_SCALE*INT_SCALE);
			item.feature=match;
			item.key=key;
//			System.out.println(min_h+","+min_dist);
		}
//		for(int i=0;i<64;i++){
//			System.out.println(c0[i]);
//		}
//		System.out.println("*//***");
//		for(int i=0;i<64;i++){
//			System.out.println(c1[i]);
//		}
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