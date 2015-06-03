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
package jp.nyatla.nyartoolkit.dev.pro.core.integralimage.driver;



/**
 * {@link INyARIntegralImageDriver}の画�?アクセスインターフェイス
 */
public interface INyARIntegralImageDriver
{
	public int getBoxIntegral(int sx, int sy, int xsize, int ysize );
	public double getOrientation(int i_x,int i_y,double i_scale);
	public void getDescriptor(int i_x,int i_y,double i_scale,double i_orientation,double[] i_dest);
	public int getDyy(int sx,int sy,int filter);
	public int getDxx(int sx,int sy,int filter);
	public int getDxy(int sx,int sy,int filter);

}