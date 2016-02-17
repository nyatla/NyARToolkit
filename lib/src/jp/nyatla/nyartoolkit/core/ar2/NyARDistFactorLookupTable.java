package jp.nyatla.nyartoolkit.core.ar2;

import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARDistFactorLookupTable
{
	final private double[] i2o;
	final private double[] o2i;
	final private int     xsize;
	final private int     ysize;
	final private int     xOff;
	final private int     yOff;
	public NyARDistFactorLookupTable(INyARCameraDistortionFactor i_base_factor,NyARIntSize i_screen_size,int i_offset_x,int i_offset_y)
	{
	    this.xsize = i_screen_size.w + i_offset_x*2;
	    this.ysize = i_screen_size.h + i_offset_y*2;
	    this.xOff = i_offset_x;
	    this.yOff = i_offset_y;
	    this.i2o=new double[this.xsize*this.ysize*2];
	    this.o2i=new double[this.xsize*this.ysize*2];
	    NyARDoublePoint2d tmp=new NyARDoublePoint2d();
	    for(int j = 0; j < this.ysize; j++ ) {
	        for(int i = 0; i < this.xsize; i++ ) {
	        	int ptr=(j*this.xsize+i)*2;
	        	i_base_factor.ideal2Observ(i-i_offset_x,j-i_offset_y, tmp);
	        	this.i2o[ptr+0]=tmp.x;
	        	this.i2o[ptr+1]=tmp.y;
	        	i_base_factor.observ2Ideal(i-i_offset_x,j-i_offset_y, tmp);
	        	this.o2i[ptr+0]=tmp.x;
	        	this.o2i[ptr+1]=tmp.y;	        	
	        }
	    }
	}
	
	public int arParamIdeal2ObservLTf(double ix, double  iy,NyARDoublePoint2d o)
	{
	    int      px, py;
	    
	    px = (int)(ix+0.5F) + this.xOff;
	    py = (int)(iy+0.5F) + this.yOff;
	    if( px < 0 || px >= this.xsize || py < 0 || py >= this.ysize ){
	    	return -1;
	    }
	    
	    int lt =  (py*this.xsize + px)*2;
	    o.x = this.i2o[lt+0];
	    o.y = this.i2o[lt+1];
	    return 0;
	}
	public int arParamObserv2IdealLTf(double  ox, double  oy,NyARDoublePoint2d o)
	{
	    int      px, py;
	    
	    px = (int)(ox+0.5F) + this.xOff;
	    py = (int)(oy+0.5F) + this.yOff;
	    if( px < 0 || px >= this.xsize || py < 0 || py >= this.ysize ){
	    	return -1;
	    }
	    
	    int lt = (py*this.xsize + px)*2;
	    o.x = o2i[lt+0];
	    o.y = o2i[lt+1];
	    return 0;
	}		
}
