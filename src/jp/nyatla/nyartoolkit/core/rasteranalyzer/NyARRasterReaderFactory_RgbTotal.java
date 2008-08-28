/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.rasteranalyzer;

import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;

public final class NyARRasterReaderFactory_RgbTotal implements INyARRasterReaderFactory{
    public INyARRasterReader createReader(INyARRaster i_raster) throws NyARException
    {
	switch(i_raster.getBufferType()){
	case INyARRaster.BUFFERFORMAT_BYTE_B8G8R8X8_32:
	    return new NyARRasterReader_RgbTotal_BYTE_C8C8C8X8(i_raster);
	case INyARRaster.BUFFERFORMAT_BYTE_B8G8R8_24:
	case INyARRaster.BUFFERFORMAT_BYTE_R8G8B8_24:
	    return new NyARRasterReader_RgbTotal_BYTE_C8C8C8(i_raster);
	case INyARRaster.BUFFERFORMAT_NULL_ALLZERO:
	    return new NyARRasterReader_RgbTotal_NULL_ALLZERO(i_raster);
	default:
	    throw new NyARException();
	}
    }
}

abstract class NyARRasterReader_RgbTotal_BaseClass implements INyARRasterReader
{
    protected INyARRaster _raster;
    protected TNyARIntSize _size;    
    public final TNyARIntSize getRasterSize()
    {
	return this._size;
    }
    public final int getThreshold()
    {
	int[] work=new int[this._size.w];
	int hi=0;int low=256*3;
	for(int i2=0;i2<8;i2++){	    
	    this.readRow(this._size.h*i2/8,work);
	    for(int i=0;i<this._size.w;i++){
		if(hi<work[i]){
		    hi=work[i];
		}
		if(low>work[i]){
		    low=work[i];
		}
	    }
	}
	return (hi-low)/3+low;
    }    
}


/**
 * byte[] RGB24/BGR24等、画素並びは問わない
 *
 */
 class NyARRasterReader_RgbTotal_BYTE_C8C8C8 extends NyARRasterReader_RgbTotal_BaseClass
 {
     public NyARRasterReader_RgbTotal_BYTE_C8C8C8(INyARRaster i_raster) throws NyARException
     {
	 this._raster=i_raster;
	 this._size=i_raster.getSize();
     }
     public void readRow(int i_row,int[] o_line)
     {
	 final byte[] buf=(byte[])this._raster.getBufferObject();
	 int bp=(i_row+1)*this._size.w*3-3;
	 for(int i=this._size.w-1;i>=0;i--){
	     o_line[i]=((buf[bp] & 0xff)+(buf[bp+1] & 0xff)+(buf[bp+2] & 0xff))/3;
	     bp-=3;
	 }
     }
 }

 /**
  * byte[] RGBX/BGRX等、先頭24bitの画素並びは問わない
  *
  */
 class NyARRasterReader_RgbTotal_BYTE_C8C8C8X8 extends NyARRasterReader_RgbTotal_BaseClass
 {
     public NyARRasterReader_RgbTotal_BYTE_C8C8C8X8(INyARRaster i_raster) throws NyARException
     {
	 this._raster=i_raster;
	 this._size=i_raster.getSize();
     }
     public void readRow(int i_row,int[] o_line)
     {
	 final byte[] buf=(byte[])this._raster.getBufferObject();
	 int bp=(i_row+1)*this._size.w*4-4;
	 for(int i=this._size.w-1;i>=0;i--){
	     o_line[i]=((buf[bp] & 0xff)+(buf[bp+1] & 0xff)+(buf[bp+2] & 0xff))/3;
	     bp-=4;
	 }
     }    
 }

 /**
  * ゼロ配列
  *
  */
 class NyARRasterReader_RgbTotal_NULL_ALLZERO extends NyARRasterReader_RgbTotal_BaseClass
 {
     public NyARRasterReader_RgbTotal_NULL_ALLZERO(INyARRaster i_raster) throws NyARException
     {
	 this._size=i_raster.getSize();
     }
     public void readRow(int i_row,int[] o_line)
     {
	 for(int i=this._size.w-1;i>=0;i--){
	     o_line[i]=0;
	 }
     }    
 }