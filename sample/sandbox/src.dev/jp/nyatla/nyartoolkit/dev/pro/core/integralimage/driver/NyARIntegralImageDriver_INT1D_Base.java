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

import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.integralimage.NyARIntegralImage;
import jp.nyatla.nyartoolkit.pro.core.surf.NyARGaussTable;

/**
 * {@link INyARIntegralImageDriver}クラスの画�?ドライバ�??
 * 
 * @author nyatla
 *
 */
public class NyARIntegralImageDriver_INT1D_Base implements INyARIntegralImageDriver
{
	private int[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARIntegralImageDriver_INT1D_Base(NyARIntegralImage i_ref_raster)
	{
		assert(i_ref_raster.hasBuffer()==true);
		assert(i_ref_raster.isEqualBufferType(NyARBufferType.INT1D));
		this._ref_buf=(int[])i_ref_raster.getBuffer();
		this._ref_size=i_ref_raster.getSize();
	}

	public int getDxx(int c,int r,int filter)
	{
		int b = (filter - 1) / 2; // border for this filter
		int l = filter / 3; // lobe for this filter (filter size / 3)
		int w = filter; // filter size
		return this.getBoxIntegral(c - b, r - l + 1, w, 2 * l - 1) - this.getBoxIntegral(c - (l - 1) / 2, r - l + 1, l, 2 * l - 1) * 3;
	}
	public int getDyy(int c,int r,int filter)
	{
		int b = (filter - 1) / 2; // border for this filter
		int l = filter / 3; // lobe for this filter (filter size / 3)
		int w = filter; // filter size
		return this.getBoxIntegral(c - l + 1, r - b, 2 * l - 1, w) - this.getBoxIntegral(c - l + 1, r - (l - 1) / 2, 2 * l - 1, l) * 3;
	}
	public int getDxy(int c,int r,int filter)
	{
		int l = filter / 3; // lobe for this filter (filter size / 3)
		return this.getBoxIntegral(c + 1, r - l, l, l) + this.getBoxIntegral(c - l, r + 1, l, l)
				- this.getBoxIntegral(c - l, r - l, l, l) - this.getBoxIntegral(c + 1, r + 1, l, l);	
	}
	public int getBoxIntegral(int sx, int sy, int xsize, int ysize )
	{
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;
		int   A, B, C, D;
		int c2 = (sx+xsize < width )? sx+xsize-1: width -1;
		int r2 = (sy+ysize < this._ref_size.h)? sy+ysize-1: this._ref_size.h-1;
		int c1 = (sx <= c2)? sx-1: c2;
		int r1 = (sy <= r2)? sy-1: r2;

		if (r1 >= 0 && c1 >= 0) {
			A = buf[r1 * width + c1];
			B = buf[r1 * width + c2];
			C = buf[r2 * width + c1];
			D = buf[r2 * width + c2];
		}
		else if( r1 >= 0 && c2 >= 0) {
			A = 0;
			B = buf[r1 * width + c2];
			C = 0;
			D = buf[r2 * width + c2];
		}
		else if( r2 >= 0 && c1 >= 0) {
			A = 0;
			B = 0;
			C = buf[r2 * width + c1];
			D = buf[r2 * width + c2];
		}
		else if (r2 >= 0 && c2 >= 0) {
			A = 0;
			B = 0;
			C = 0;
			D = buf[r2 * width + c2];
		}
		else {
			A = 0;
			B = 0;
			C = 0;
			D = 0;
		}
		int  ret = A - B - C + D;
		return (ret > 0)? ret: 0;
	}
	public double getOrientation(int i_x, int i_y, double i_scale)
	{
		//HarrResponse
		
		double[]    resX=new double[109];
		double[]    resY=new double[109];
		double[]    Ang=new double[109];
//		double    gauss = 0.0f;
		int      id[] = {6,5,4,3,2,1,0,1,2,3,4,5,6};
//		int      c = (int)(i_x + 0.5);
//		int      r = (int)(i_y + 0.5);
		int      s = (int)(i_scale + 0.5);
		// calculate haar responses for points within radius of 6*scale
		int idx = 0;
		for(int i = -6; i <= 6; i++) {
			for(int j = -6; j <= 6; j++) {
				if( i*i + j*j >= 36 ){
					continue;
				}
				double gauss = NyARGaussTable.gauss25[id[i+6]][id[j+6]]; // Or: gauss25[abs(i)][abs(j)]
				resX[idx] = gauss * haarX(i_y+j*s, i_x+i*s, 4*s);
				resY[idx] = gauss * haarY(i_y+j*s, i_x+i*s, 4*s);
				Ang[idx]  = getAngle(resX[idx], resY[idx]);
				idx++;
			}
		}

		// calculate the dominant direction 
		double sumX=0.0f, sumY=0.0f;
		double maxSumX=0.0f, maxSumY=0.0f;
		double max=0.0f;
		double ang1=0.0f, ang2=0.0f;

		// loop slides pi/3 window around feature point
		for(ang1 = 0; ang1 < 2.0f*Math.PI;  ang1+=0.15f) {
			ang2 = ( ang1 + Math.PI/3.0 > 2.0*Math.PI ? ang1 - 5.0*Math.PI/3 : ang1 +Math.PI/3.0);
			sumX = sumY = 0.0f; 
			for (int k = 0; k < idx; ++k) {
				// get angle from the x-axis of the sample point
				double ang = Ang[k];

				// determine whether the point is within the window
				if (ang1 < ang2 && ang1 < ang && ang < ang2) {
					sumX+=resX[k];  
					sumY+=resY[k];
				} 
				else if (ang2 < ang1 && ((ang > 0 && ang < ang2) || (ang > ang1 && ang < 2*Math.PI) )) {
					sumX+=resX[k];  
					sumY+=resY[k];
				}
			}
			// if the vector produced from this window is longer than all 
			// previous vectors then this forms the new dominant direction
			if (sumX*sumX + sumY*sumY > max) {
				// store largest orientation
				max = sumX*sumX + sumY*sumY;
				maxSumX = sumX;
				maxSumY = sumY;
			}
		}

		// assign orientation of the dominant response vector
		return getAngle(maxSumX, maxSumY);
	}

	//! Get the modified descriptor. See Agrawal ECCV 08
	//! Modified descriptor contributed by Pablo Fernandez
	public void getDescriptor(int i_x,int i_y,double i_scale,double i_orientation,double[] i_dest)
	{
		int        count=0;
		int        i = 0, ix = 0, j = 0, jx = 0, xs = 0, ys = 0;
		
		double      gauss_s1 = 0.0f, gauss_s2 = 0.0f;
		double      rx = 0.0f, ry = 0.0f, rrx = 0.0f, rry = 0.0f, len = 0.0f;
		double      cx = -0.5f, cy = 0.0f; //Subregion centers for the 4x4 gaussian weighting

//		int x = (int)(i_x + 0.5);
//		int y = (int)(i_y + 0.5);

		double co = Math.cos(i_orientation);
		double si = Math.sin(i_orientation);

		i = -8;

		//Calculate descriptor for this interest point
		while(i < 12) {
			j = -8;
			i = i-4;
			cx += 1.f;
			cy = -0.5f;

			while(j < 12) {
				double     dx, dy, mdx, mdy;
				dx=dy=mdx=mdy=0.0f;
				cy += 1.f;
				j  = j - 4;
				ix = i + 5;
				jx = j + 5;

				xs = round(i_x + (-jx*i_scale*si + ix*i_scale*co));
				ys = round(i_y + ( jx*i_scale*co + ix*i_scale*si));

				for (int k = i; k < i + 9; ++k) {
					for (int l = j; l < j + 9; ++l) {
						//Get coords of sample point on the rotated axis
						int sample_x = round(i_x + (-l*i_scale*si + k*i_scale*co));//modified 
						int sample_y = round(i_y + ( l*i_scale*co + k*i_scale*si));//modified

						//Get the gaussian weighted x and y responses
						gauss_s1 = NyARGaussTable.gaussian(xs-sample_x,ys-sample_y,2.5f*i_scale);
						rx = haarX(sample_y, sample_x, 2*((int)(i_scale+0.5)));
						ry = haarY(sample_y, sample_x, 2*((int)(i_scale+0.5)));

						//Get the gaussian weighted x and y responses on rotated axis
						rrx = gauss_s1*(-rx*si + ry*co);
						rry = gauss_s1*( rx*co + ry*si);

						dx += rrx;
						dy += rry;
						mdx += Math.abs(rrx);
						mdy += Math.abs(rry);
					}
				}

				//Add the values to the descriptor vector
				gauss_s2 = NyARGaussTable.gaussian(cx-2.0f,cy-2.0f,1.5f);

				i_dest[count++] = dx*gauss_s2;
				i_dest[count++] = dy*gauss_s2;
				i_dest[count++] = mdx*gauss_s2;
				i_dest[count++] = mdy*gauss_s2;

				len += (dx*dx + dy*dy + mdx*mdx + mdy*mdy) * gauss_s2*gauss_s2;

				j += 9;
			}
			i += 9;
		}

		//Convert to Unit Vector
		len = Math.sqrt(len);
		for(int k = 0; k <64; ++k) {
			i_dest[k] /= len;
		}
		return;
	}
	/**
	 * Calculate Haar wavelet responses in x direction
	 */
	private int haarX(int row, int column, int s)
	{
		return this.getBoxIntegral(column,row-s/2, s/2, s) - this.getBoxIntegral(column-s/2, row-s/2, s/2, s);
	}
	/**
	 * Calculate Haar wavelet responses in y direction
	 */
	private int haarY(int row, int column, int s)
	{
		return getBoxIntegral(column-s/2, row,     s, s/2) - getBoxIntegral(column-s/2, row-s/2, s, s/2);
	}
	/**
	 * 展開して早くした奴
	 * @param row
	 * @param column
	 * @param s
	 * @return
	 */
	private int haarX2(int row, int column, int s)
	{
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;

		int hs=s/2;
				
		int c1ac2b = ((column<=width)? column: width)-1;//X- 
		int c2a = ((column+hs <=width )? column+hs:width)-1;//X+
		int r1ar1b = (((row-hs<=this._ref_size.h)? row-hs:this._ref_size.h)-1)*width;//Y- 
		int r2ar2b = (((row+hs <=this._ref_size.h)? row+hs: this._ref_size.h)-1)*width;//Y+
		int c1b = ((column-hs<=width)? column-hs: width)-1;//X- 
		
		//(r2a>=r1a),(c2a>=c1a) //
		//(r2a==r2b>r1a==r1b)
		//c2a>c1a=c2b>c1b
		int ret2;
	    if (r1ar1b >= 0){
	    	if(c1b >= 0){
	    		ret2=(buf[r1ar1b+c1ac2b] - buf[r1ar1b+c2a] - buf[r2ar2b+c1ac2b] + buf[r2ar2b+c2a])-(buf[r1ar1b+c1b] - buf[r1ar1b+c1ac2b] - buf[r2ar2b+c1b] + buf[r2ar2b+c1ac2b]);
	    	}else if(c1ac2b >= 0){
	    		ret2=(buf[r1ar1b+c1ac2b] - buf[r1ar1b+c2a] - buf[r2ar2b+c1ac2b] + buf[r2ar2b+c2a])-(-buf[r1ar1b+c1ac2b]+buf[r2ar2b+c1ac2b]);
	    	}else if(c2a>=0){
	    		ret2=(-buf[r1ar1b+c2a]+buf[r2ar2b+c2a]);
	    	}else{
	    		ret2=0;
	    	}
	    }else if(r2ar2b >= 0){
	    	if(c1b >= 0){
	    		ret2=(-buf[r2ar2b+c1ac2b]+buf[r2ar2b+c2a])-(-buf[r2ar2b+c1b]+buf[r2ar2b+c1ac2b]);
	    	}else if(c1ac2b >= 0){
	    		ret2=(-buf[r2ar2b+c1ac2b]+buf[r2ar2b+c2a])-(buf[r2ar2b+c1ac2b]);
	    	}else if(c2a >= 0){
	    		ret2=(buf[r2ar2b+c2a]);
	    	}else{
	    		ret2=0;
	    	}
	    }else{
	    	ret2=0;
	    }
	    //もしかして0チェ�?ク要らな�?�?
	    assert(ret2>=0);
	    
	    int test=this.getBoxIntegral(column,row-s/2, s/2, s) - this.getBoxIntegral(column-s/2, row-s/2, s/2, s);
	    if(test!=ret2){
	    	System.out.println("NOOOOO");
	    }
		return ret2;
	}
	/**
	 * 展開して早くした奴
	 * @param row
	 * @param column
	 * @param s
	 * @return
	 */
	private int haarY2(int row, int column, int s)
	{
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;

		int hs=s/2;
		
		
		int c1ac1b = ((column-hs<=width)? column-hs: width)-1;//X- 
		int c2ac2b = ((column+hs <= width )? column+hs:width)-1;//X+
		int r1a = (((row<=this._ref_size.h)? row:this._ref_size.h)-1)*width;//Y- 
		int r2a = (((row+hs <= this._ref_size.h)? row+hs: this._ref_size.h)-1)*width;//Y+
		int r1b = (((row-hs<=this._ref_size.h)? row-hs:this._ref_size.h)-1)*width;//Y- 
		int r2b = (((row < this._ref_size.h)? row: this._ref_size.h)-1)*width;//Y+
		
		//(c2a>=c1a),(r2a>=r1a) //
		//(c2a==c2b>c1a==c1b)
		//r2a>r1a=r2b>r1b
		int ret1;
	    if (c1ac1b >= 0){
	    	if(r1b >= 0){
	    		ret1=(buf[r1a+c1ac1b] - buf[r1a+c2ac2b] - buf[r2a+c1ac1b] + buf[r2a+c2ac2b])-(buf[r1b+c1ac1b] - buf[r1b+c2ac2b] - buf[r2b+c1ac1b] + buf[r2b+c2ac2b]);
	    	}else if(r1a >= 0){
	    		ret1=(buf[r1a+c1ac1b] - buf[r1a+c2ac2b] - buf[r2a+c1ac1b] + buf[r2a+c2ac2b])-(- buf[r2b+c1ac1b] + buf[r2b+c2ac2b]);
	    	}else if(r2a>=0){
	    		ret1= - buf[r2a+c1ac1b] + buf[r2a+c2ac2b];
	    	}else{
	    		ret1=0;
	    	}
	    }else if(c2ac2b >= 0){
	    	if(r1b >= 0){
	    		ret1=(- buf[r1a+c2ac2b] + buf[r2a+c2ac2b])-(- buf[r1b+c2ac2b] + buf[r2b+c2ac2b]);
	    	}else if(r1a >= 0){
	    		ret1=(- buf[r1a+c2ac2b] + buf[r2a+c2ac2b])-(buf[r2b+c2ac2b]);
	    	}else if(r2a >= 0){
	    		ret1=buf[r2a+c2ac2b];
	    	}else{
	    		ret1=0;
	    	}
	    }else{
	    	ret1=0;
	    }
	    //もしかして0チェ�?ク要らな�?�?
	    assert(ret1>=0);

	    int test=this.getBoxIntegral(column-s/2, row,     s, s/2) - getBoxIntegral(column-s/2, row-s/2, s, s/2);
	    if(ret1!=test){
	    	System.out.println("NOOOOO");
	    }
		return ret1;
	}		
	//! Get the angle from the +ve x-axis of the vector given by (X Y)
	private static double getAngle(double X, double Y)
	{
		double  angle = Math.atan2(Y, X);
		if( angle < 0 ) angle += (double)Math.PI * 2.0f;

		return angle;
	}
	private static int round(double value)
	{
		if(value > 0)
		{
			return (int)(value + 0.5f);
		}
		else
		{
			return (int)(value - 0.5f);
		}
	}	
}