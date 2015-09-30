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
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.integralimage.NyARIntegralImage;

public class NyARIntegralImageGenerator_INT1D implements NyARIntegralImage.IIntegralImageGenerator
{
	private int[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARIntegralImageGenerator_INT1D(NyARIntegralImage i_ref_raster)
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
	
		int[] out_buf=(int[]) this._ref_buf;
		int[] in_buf=(int[])i_in.getBuffer();
//		INyARGsPixelDriver in_pixd=i_in.getGsPixelDriver();
		
		rs=0;
		int i=s.w-1;
		int p0,p1;
		
		//0行目
		p0=0;
		for(; i>=0 ;i-=8){
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
		}
		for(; i>=0 ;i--){
			rs += in_buf[p0];
			out_buf[p0]=rs;
			p0++;
		}
		//残り
		p0=0;
		p1=s.w;
		for( int j = s.h-2; j >=0; j-- ) {
			rs = 0;
			i=s.w-1;
			for(; i >=8; i-=8){
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
			}
			for(; i >=0; i-- ){
				rs += in_buf[p1];
				out_buf[p1]=rs+out_buf[p0];
				p1++;
				p0++;
			}
			
		}
		return;
	}
/*
		for( int j = 1; j < s.h; j++ ) {
			rs = 0;
			for( int i = 0; i < s.w; i++ ) {
				rs += in_pixd.getPixel(i,j);
				buf[i+j*s.w]=rs+buf[i+(j-1)*s.w];
			}
		}
 */	
}
