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
import jp.nyatla.nyartoolkit.core.rasterdriver.pixel.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.integralimage.NyARIntegralImage;

public class NyARIntegralImageGenerator_INT1D_Base implements NyARIntegralImage.IIntegralImageGenerator
{
	private int[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARIntegralImageGenerator_INT1D_Base(NyARIntegralImage i_ref_raster)
	{
		assert(i_ref_raster.hasBuffer()==true);
		assert(i_ref_raster.isEqualBufferType(NyARBufferType.INT1D));
		this._ref_buf=(int[])i_ref_raster.getBuffer();
		this._ref_size=i_ref_raster.getSize();
	}	
	/**
	 * Standard
	 * @param i_in
	 * @throws NyARRuntimeException
	 */
	public void genIntegralImage(INyARGrayscaleRaster i_in) throws NyARRuntimeException
	{
		assert(this._ref_size.isEqualSize(i_in.getSize()));

		int rs;
		NyARIntSize s=i_in.getSize();
	
		//0行目
		int[] buf=(int[]) this._ref_buf;
		INyARGsPixelDriver in_pixd=i_in.getGsPixelDriver();
		
		rs=0;
		for(int i = 0; i <s.w ; i++ ){
			rs += in_pixd.getPixel(i,0);
			buf[i]=rs;
		}
		//残り
		for( int j = 1; j < s.h; j++ ) {
			rs = 0;
			for( int i = 0; i < s.w; i++ ) {
				rs += in_pixd.getPixel(i,j);
				buf[i+j*s.w]=rs+buf[i+(j-1)*s.w];
			}
		}
		return;
	}	
}
