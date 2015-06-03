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

import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;


public class SimilarityMatrix extends NyARDoubleMatrix33
{
	public void createMatrix(NyARIntPoint2d[] points)
	{
		//-------------------------------------------------------------------------
		// calculate the centroid
		//-------------------------------------------------------------------------
		int sumX, sumY;
		sumX = sumY = 0;
		int i;
		int num=points.length;
		//<tiling>
		for(i = num-1 ; i >=3 ; i-=4) 
		{
			sumX += points[i].x+points[i-1].x+points[i-2].x+points[i-3].x;
			sumY += points[i].y+points[i-1].y+points[i-2].y+points[i-3].y;
		}
		for(; i >=0 ; i--) 
		{
			sumX += points[i].x;
			sumY += points[i].y;
		}
		//</tiling>
		double meanX = sumX / num;
		double meanY = sumY / num;

		//-------------------------------------------------------------------------
		// calculate the mean distance
		//-------------------------------------------------------------------------

		double sumDist = 0;
		for(i = num-1 ; i >=0; i--)
		{
			double xx=points[i].x - meanX;
			double yy=points[i].y - meanY;
			sumDist += Math.sqrt(xx*xx+yy*yy);
		}
		double meanDist = sumDist / num;

		//-------------------------------------------------------------------------
		// set the similarity transform
		//-------------------------------------------------------------------------
		double scale = 1.f / meanDist;
		this.m00=scale;
		this.m01=0;
		this.m02=-scale * meanX;
		this.m10=0;
		this.m11=scale;
		this.m12=-scale * meanY;
		this.m20=0;
		this.m21=0;
		this.m22=1;		
	}
}