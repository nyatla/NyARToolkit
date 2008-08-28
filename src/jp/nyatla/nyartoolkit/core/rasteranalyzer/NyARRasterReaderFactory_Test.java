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

public final class NyARRasterReaderFactory_Test implements INyARRasterReaderFactory{
    public INyARRasterReader createReader(INyARRaster i_raster) throws NyARException
    {
	switch(i_raster.getBufferType()){
//	case NyARRaster.BUFFERFORMAT_BYTE_B8G8R8X8_32:
//	    return new NyARRasterReader_RgbMul_BYTE_C8C8C8X8(i_raster);
	case INyARRaster.BUFFERFORMAT_BYTE_B8G8R8_24:
	case INyARRaster.BUFFERFORMAT_BYTE_R8G8B8_24:
	    return new NyARRasterReader_RgbTest_BYTE_C8C8C8(i_raster);
//	case NyARRaster.BUFFERFORMAT_NULL_ALLZERO:
//	    return new NyARRasterReader_RgbMul_NULL_ALLZERO(i_raster);
	default:
	    throw new NyARException();
	}
    }
}

abstract class NyARRasterReader_RgbTest_BaseClass implements INyARRasterReader
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
	int hi=0;int low=16581375;
	int ave,sum2;
	ave=0;
	for(int i2=0;i2<8;i2++){	    
	    this.readRow(this._size.h*i2/8,work);
	    sum2=0;
	    for(int i=0;i<this._size.w;i++){
		int v=work[i];
		if(hi<v){
		    hi=v;
		}
		if(low>v){
		    low=v;
		}
		sum2+=v;
	    }
	    ave+=sum2;
	}
	
	return (((hi-low)/10*8)+low)>>2;
    } 
}


/**
 * byte[] RGB24/BGR24等、画素並びは問わない
 *
 */
class NyARRasterReader_RgbTest_BYTE_C8C8C8 extends NyARRasterReader_RgbMul_BaseClass
{
    public NyARRasterReader_RgbTest_BYTE_C8C8C8(INyARRaster i_raster) throws NyARException
    {
	this._raster=i_raster;
	this._size=i_raster.getSize();
    }
    public void readRow(int i_row,int[] o_line)
    {
	final byte[] buf=(byte[])this._raster.getBufferObject();
        int bp=(i_row)*this._size.w*3;
        int w;
        int prev=255;
        int ave=0;
        o_line[0]=0;
        for(int i=1;i<this._size.w-1;i++){            
            w=((buf[bp] & 0xff)*(buf[bp+1] & 0xff)*(buf[bp+2] & 0xff))>>16;
//            if(w>prev && (w>ave+16)){
//        	ave=w;
        	o_line[i]=Math.abs(w-ave);
//            }else if(w<prev && (ave-16>w)){
        	ave=w;
//            	o_line[i]=w;        	
//            }else{
        	ave=(w+ave)/2;
//        	o_line[i]=o_line[i-1];        	
//            }
            prev=w;
	    bp+=3;
	}
    }


  
}