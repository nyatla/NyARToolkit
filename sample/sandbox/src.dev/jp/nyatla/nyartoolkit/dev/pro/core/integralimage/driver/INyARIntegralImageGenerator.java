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

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

public interface INyARIntegralImageGenerator
{
	public void genIntegralImage(INyARGrayscaleRaster i_in) throws NyARRuntimeException;

}
