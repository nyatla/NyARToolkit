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
package jp.nyatla.nyartoolkit.dev.pro.core.kpm.hest.utils;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

/**
 * HomographyMatrixの計算機�?�を持つNyARDoubleMatrix33
 * 
 * このクラスは多数のリソースを消費します�?�で、データストレージとしては使用しないでください
 * もし多数の行を同時に保持するなら�?�計算器に改�?すること?�?
 */
public class HomographyMatrix extends NyARDoubleMatrix33
{
	private SimilarityMatrix __T1=new SimilarityMatrix();
	private SimilarityMatrix __T2=new SimilarityMatrix();
	private HomographySvd __svd=new HomographySvd();
	private NyARDoubleMatrix33 __Htmp=new NyARDoubleMatrix33();
	private NyARDoubleMatrix33 __temp=new NyARDoubleMatrix33();
	private NyARDoublePoint2d[] _p1_normal=NyARDoublePoint2d.createArray(4);
	private NyARDoublePoint2d[] _p2_normal=NyARDoublePoint2d.createArray(4);
	/**
	 * サンプル数4に�?適化したcomputeHomography関数
	 * @param samples
	 * @param i_dest
	 */
	public void computeHomography4Points(NyARIntPoint2d[] pt1,NyARIntPoint2d[] pt2)
	{
		assert(pt1.length==pt2.length);//Need more correspondence points! for computing H
		assert(pt1.length==4);//Need more correspondence points! for computing H
		// normalization
		this.__T1.createMatrix(pt1);
		dataNormalization(pt1, this.__T1,this._p1_normal);

		this.__T2.createMatrix(pt2);
		dataNormalization(pt2, this.__T2,this._p2_normal);

		this.__svd.setPoints(this._p1_normal, this._p2_normal);
		this.__svd.svd_u(this.__Htmp); // A = U^T D V : opencv setting
		
		// denormalization : H = invT2 * Htmp * T1 <- Htmp = T2 * H * invT1
		this.__T2.inverse(this.__T2);
		NyARDoubleMatrix33 temp=this.__temp;
		temp.mul(this.__T2,this.__Htmp);
		this.mul(temp,this.__T1);
	}
	private static void dataNormalization(NyARIntPoint2d[] points,SimilarityMatrix i_mat,NyARDoublePoint2d[] o_points)
	{
		double scale=i_mat.m00;
		double a=i_mat.m02;
		double b=i_mat.m12;
		int i = points.length-1;
		for(; i >=3  ; i-=4)
		{
			o_points[i].x = (scale * points[i].x) + a;
			o_points[i].y = (scale * points[i].y) + b;
			o_points[i-1].x = (scale * points[i-1].x) + a;
			o_points[i-1].y = (scale * points[i-1].y) + b;
			o_points[i-2].x = (scale * points[i-2].x) + a;
			o_points[i-2].y = (scale * points[i-2].y) + b;
			o_points[i-3].x = (scale * points[i-3].x) + a;
			o_points[i-3].y = (scale * points[i-3].y) + b;
		}
		for(; i >=0  ; i--)
		{
			o_points[i].x = (scale * points[i].x) + a;
			o_points[i].y = (scale * points[i].y) + b;
		}
	}		
}