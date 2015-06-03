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
public class NyARIntegralImageDriver_INT1D implements INyARIntegralImageDriver
{
	private int[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARIntegralImageDriver_INT1D(NyARIntegralImage i_ref_raster)
	{
		assert(i_ref_raster.hasBuffer()==true);
		assert(i_ref_raster.isEqualBufferType(NyARBufferType.INT1D));
		this._ref_buf=(int[])i_ref_raster.getBuffer();
		this._ref_size=i_ref_raster.getSize();
	}	
	public int getDxx(int c,int r,int filter)
	{		
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;
		int f3=filter / 3;
		int sx1=c - (filter- 1) / 2;
		int sx2=c - (f3 - 1) / 2;
		int sy =r - f3 + 1;
		int ysize=2 * f3 - 1;

		int c1a = ((sx1<=width)? sx1: width)-1;//X- 
		int c2a = ((sx1+filter < width )? sx1+filter:width)-1;//X+
		int c1b = ((sx2<=width)? sx2: width)-1;//X- 
		int c2b = ((sx2+f3 < width )? sx2+f3:width)-1;//X+		
		int r1 = (((sy<=this._ref_size.h)? sy:this._ref_size.h)-1)*width;//Y- 
		int r2 = (((sy+ysize < this._ref_size.h)? sy+ysize: this._ref_size.h)-1)*width;//Y+		
		//c2a>=c1b>=c1a
		//(r2>=r1),(c2>=c1)
	    if (r1 >= 0){
	    	if(c1a >= 0){
	    		return (buf[r1+c1a] - buf[r1+c2a] - buf[r2+c1a] + buf[r2+c2a])-(buf[r1+c1b] - buf[r1+c2b] - buf[r2+c1b] + buf[r2+c2b])*3;
	    	}else if(c1b >= 0){
	    		return (- buf[r1+c2a] + buf[r2+c2a])-(buf[r1+c1b] - buf[r1+c2b] - buf[r2+c1b] + buf[r2+c2b])*3;
	    	}else if(c2a >= 0){
	    		return (- buf[r1+c2a]  + buf[r2+c2a])-(- buf[r1+c2b] + buf[r2+c2b])*3;
	    	}else if(c2b>=0){
	    		return -(- buf[r1+c2b] + buf[r2+c2b])*3;// never called?
	    	}
	    }else if(r2 >= 0){
	    	if(c1a >= 0){
	    		return (-buf[r2+c1a]+buf[r2+c2a])-(-buf[r2+c1b]+buf[r2+c2b])*3;
	    	}else if(c1b >= 0){
	    		return buf[r2+c2a] - (-buf[r2+c1b]+buf[r2+c2b])*3;
	    	}else if(c2a >= 0){
	    		return buf[r2+c2a] - buf[r2+c2b]*3;
	    	}else if(c2b>=0){
	    		return - buf[r2+c2b]*3;// never called?
	    	}
	    }
    	return 0;
	}
	
	
	public int getDyy(int c,int r,int filter)
	{		
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;
		int height=this._ref_size.h;

		int f3=filter / 3;
		int sx=c - f3 + 1;
		int sy1= r - ((filter - 1) / 2);
		int xsize=2 * f3 - 1;
		int sy2= r - (f3 - 1) / 2;

		int r1a = (((sy1<=height)? sy1:height)-1)*width;//Y- 
		int r2a = (((sy1+filter < height)? sy1+filter: height)-1)*width;//Y+
		int r2b = (((sy2+f3 < height)? sy2+f3: height)-1)*width;//Y+	
		int r1b = (((sy2<=height)? sy2:height)-1)*width;//Y- 
	    
		int c1 = ((sx<=width)? sx: width)-1;//X- 
		int c2 = ((sx+xsize < width )? sx+xsize:width)-1;//X+

		//r1a<r1b<r2a
    	if(c1 >= 0){
    	    if (r1a >= 0){
	    		return (buf[r1a+c1] - buf[r1a+c2] - buf[r2a+c1] + buf[r2a+c2])-3*(buf[r1b+c1] - buf[r1b+c2] - buf[r2b+c1] + buf[r2b+c2]);	
    	    }else if(r1b>=0){
    	    	return (- buf[r2a+c1] + buf[r2a+c2])-(buf[r1b+c1] - buf[r1b+c2] - buf[r2b+c1] + buf[r2b+c2])*3;
     	    }else if(r2b>=0){
	    		return (- buf[r2a+c1] + buf[r2a+c2])-(- buf[r2b+c1] + buf[r2b+c2])*3;
		    }else if(r2a>=0){
	    		return (- buf[r2a+c1] + buf[r2a+c2]); //never called?
		    }
    	}else if(c2 >= 0){
    	    if (r1a >= 0){
	    		return (-buf[r1a+c2]+buf[r2a+c2])-(-buf[r1b+c2]+buf[r2b+c2])*3;
    	    }else if(r1b >= 0){
	    		return buf[r2a+c2]-(-buf[r1b+c2]+buf[r2b+c2])*3;
    	    }else if(r2b >= 0){
	    		return buf[r2a+c2]-buf[r2b+c2]*3;
    	    }else if(r2a>=0){
    	    	return buf[r2a+c2]; //never called?
    	    }
    	}
    	return 0;
	}
	public int getDxy(int c,int r,int filter)
	{
		int l = filter / 3; // lobe for this filter (filter size / 3)
		
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;
		int height=this._ref_size.h;

		int c1ad = ((c + 1<=width)? c + 1: width)-1;//X- 
		int c2ad = ((c + 1+l < width )? c + 1+l:width)-1;//X+
		int c1bc = ((c - l<=width)? c - l: width)-1;//X- 
		int c2bc = ((c < width )? c:width)-1;//X+
		int r1ac = (((r - l<=height)? r - l:height)-1)*width;//Y- 
		int r2ac = (((r < height)? r: height)-1)*width;//Y+		
		int r1bd = (((r + 1<=height)? r + 1:height)-1)*width;//Y- 
		int r2bd = (((r + 1+l < height)? r + 1+l: height)-1)*width;//Y+		
		
//		r1ac<r1bd
		//r1ac<r2ac<r1db<r2db
		//c1bc<c2bc<c1ad<c2ad
		
	    if (r1ac >= 0){
	    	if(c1bc >= 0){
	    		return
	    		(buf[r1ac+c1ad] - buf[r1ac+c2ad] - buf[r2ac+c1ad] + buf[r2ac+c2ad])
	    		+ (buf[r1bd+c1bc] - buf[r1bd+c2bc] - buf[r2bd+c1bc] + buf[r2bd+c2bc])	
	    		- (buf[r1ac+c1bc] - buf[r1ac+c2bc] - buf[r2ac+c1bc] + buf[r2ac+c2bc])	
	    		- (buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c2bc >= 0){
	    		return
	    		(buf[r1ac+c1ad] - buf[r1ac+c2ad] - buf[r2ac+c1ad] + buf[r2ac+c2ad])
	    		+(  - buf[r1bd+c2bc]  + buf[r2bd+c2bc])
	    		-(  - buf[r1ac+c2bc]  + buf[r2ac+c2bc])
	    		-(buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);
	    	}else if(c1ad>=0){
	    		return
	    		(buf[r1ac+c1ad] - buf[r1ac+c2ad] - buf[r2ac+c1ad] + buf[r2ac+c2ad])
	    		//+ 0
	    		//- 0	
	    		- (buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c2ad >= 0){
	    		return
	    		(- buf[r1ac+c2ad]  + buf[r2ac+c2ad])
	    		//+ 0
	    		//- 0	
	    		- (- buf[r1bd+c2ad]  + buf[r2bd+c2ad]);	
	    	}
	    }else if(r2ac >= 0){
	    	if(c1bc >= 0){
	    		return
	    		( - buf[r2ac+c1ad] + buf[r2ac+c2ad])
	    		+(buf[r1bd+c1bc] - buf[r1bd+c2bc] - buf[r2bd+c1bc] + buf[r2bd+c2bc])
	    		-( - buf[r2ac+c1bc] + buf[r2ac+c2bc])
	    		-(buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c2bc >= 0){
	    		return
	    		(- buf[r2ac+c1ad] + buf[r2ac+c2ad])
	    		+(- buf[r1bd+c2bc]  + buf[r2bd+c2bc])
	    		-(  buf[r2ac+c2bc])
	    		-(buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);
	    	}else if(c1ad>=0){
	    		return
	    		(- buf[r2ac+c1ad] + buf[r2ac+c2ad])
	    		//+0
	    		//-0
	    		-(buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c2ad >= 0){
	    		return
	    		(buf[r2ac+c2ad])
	    		//+0
	    		//-0
	    		-(buf[r1bd+c1ad] - buf[r2bd+c1ad]);	
	    	}
	    }else if (r1bd >= 0){
	    	if(c1bc >= 0){
	    		return
	    		//0
	    		+(buf[r1bd+c1bc] - buf[r1bd+c2bc] - buf[r2bd+c1bc] + buf[r2bd+c2bc])
	    		//-0
	    		-(buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c2bc >= 0){
	    		return
	    		//0
	    		+(- buf[r1bd+c2bc]  + buf[r2bd+c2bc])
	    		//-0
	    		-(buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);
	    	}else if(c1ad>=0){
	    		return
	    		//0	
	    		//+0
	    		//-0
	    		-(buf[r1bd+c1ad] - buf[r1bd+c2ad] - buf[r2bd+c1ad] + buf[r2bd+c2ad]);
	    	}else if(c2ad >= 0){
	    		return
	    		//0	
	    		//+0
	    		//-0
	    		-(buf[r1bd+c1ad] - buf[r2bd+c1ad]);
	    	}
	    }else if(r2bd >= 0){
	    	if(c1bc >= 0){
	    		return
	    		//0	
	    		+(- buf[r2bd+c1bc] + buf[r2bd+c2bc])
	    		//-0
	    		-(- buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c2bc >= 0){
	    		return
	    		//0
	    		+( buf[r2bd+c2bc])
	    		//-0
	    		-(- buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c1ad>=0){
	    		return 
	    		//+0
	    		//+0
	    		//-0
	    		-(- buf[r2bd+c1ad] + buf[r2bd+c2ad]);	
	    	}else if(c2ad >= 0){
	    		return
	    		//+0	
	    		//+0
	    		//-0
	    		-(- buf[r2bd+c1ad]);	
	    	}	    	
	    }
		return 0;
	
	}	
	public int getBoxIntegral(int sx, int sy, int xsize, int ysize )
	{
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;

		int c1 = ((sx<=width)? sx: width)-1;//X- 
		int c2 = ((sx+xsize < width )? sx+xsize:width)-1;//X+
		int r1 = (((sy<=this._ref_size.h)? sy:this._ref_size.h)-1)*width;//Y- 
		int r2 = (((sy+ysize < this._ref_size.h)? sy+ysize: this._ref_size.h)-1)*width;//Y+		
		//(r2>=r1),(c2>=c1)
	    if (r1 >= 0){
	    	if(c1 >= 0){
	    		int ret= buf[r1+c1] - buf[r1+c2] - buf[r2+c1] + buf[r2+c2];	
	    		return ret<0?0:ret;
	    	}else if(c2 >= 0){
	    		int ret= -buf[r1+c2]+buf[r2+c2];
	    		return ret<0?0:ret;
	    	}
	    	return 0;
	    }
	    if(r2 >= 0){
	    	if(c1 >= 0){
	    		return -buf[r2+c1]+buf[r2+c2];
	    	}else if(c2 >= 0){
	    		return buf[r2+c2];
	    	}
	    }
    	return 0;	
	}
	
	

	//! Get the angle from the +ve x-axis of the vector given by (X Y)
	private static double getAngle(double X, double Y)
	{
		double  angle = Math.atan2(Y, X);
		if( angle < 0 ) angle += (double)Math.PI * 2.0f;

		return angle;
	}

	class HaarResponse{
		public double x;
		public double y;
	}


	/**
	 * Calculate Haar wavelet responses in x direction
	 */	
	private void haarXY(int column, int row, int s,HaarResponse o_dest)
	{
		int[] buf=(int[]) this._ref_buf;
		int width=this._ref_size.w;

		int hs=s/2;
				
		int c1ac2b = ((column<=width)? column: width)-1;//X- 
		int c2ac2b = ((column+hs <= width )? column+hs:width)-1;//X+
		int c1ac1b = ((column-hs<=width)? column-hs: width)-1;//X- 
		int r2ar2b = (((row+hs <=this._ref_size.h)? row+hs: this._ref_size.h)-1)*width;//Y+
		int r1ar1b = (((row-hs<=this._ref_size.h)? row-hs:this._ref_size.h)-1)*width;//Y- 
		int r2br1a = (((row<=this._ref_size.h)? row: this._ref_size.h)-1)*width;//Y+
		
		//(r2a>=r1a),(c2a>=c1a) //
		//(r2a==r2b>r1a==r1b)
		//c2a>c1a=c2b>c1b
	    if (r1ar1b >= 0){
	    	if(c1ac1b >= 0){
	    		o_dest.x=(buf[r1ar1b+c1ac2b]*2)+( - buf[r1ar1b+c2ac2b] - buf[r2ar2b+c1ac2b] + buf[r2ar2b+c2ac2b])-(buf[r1ar1b+c1ac1b] - buf[r2ar2b+c1ac1b] + buf[r2ar2b+c1ac2b]);
	    	}else if(c1ac2b >= 0){
	    		o_dest.x=(buf[r1ar1b+c1ac2b]*2) +(- buf[r1ar1b+c2ac2b] - buf[r2ar2b+c1ac2b] + buf[r2ar2b+c2ac2b])-(+buf[r2ar2b+c1ac2b]);
	    	}else if(c2ac2b>=0){
	    		o_dest.x=(-buf[r1ar1b+c2ac2b]+buf[r2ar2b+c2ac2b]);
	    	}else{
	    		o_dest.x=0;
	    	}
	    }else if(r2ar2b >= 0){
	    	if(c1ac1b >= 0){
	    		o_dest.x=(-buf[r2ar2b+c1ac2b]+buf[r2ar2b+c2ac2b])-(-buf[r2ar2b+c1ac1b]+buf[r2ar2b+c1ac2b]);
	    	}else if(c1ac2b >= 0){
	    		o_dest.x=(-buf[r2ar2b+c1ac2b]+buf[r2ar2b+c2ac2b])-(buf[r2ar2b+c1ac2b]);
	    	}else if(c2ac2b >= 0){
	    		o_dest.x=(buf[r2ar2b+c2ac2b]);
	    	}else{
	    		o_dest.x=0;
	    	}
	    }else{
	    	o_dest.x=0;
	    }
		
		//(c2a>=c1a),(r2a>=r1a) //
		//(c2a==c2b>c1a==c1b)
		//r2a>r1a=r2b>r1b
	    if (c1ac1b >= 0){
	    	if(r1ar1b >= 0){
	    		o_dest.y=(buf[r2br1a+c1ac1b]*2)+( - buf[r2br1a+c2ac2b] - buf[r2ar2b+c1ac1b] + buf[r2ar2b+c2ac2b])-(buf[r1ar1b+c1ac1b] - buf[r1ar1b+c2ac2b] + buf[r2br1a+c2ac2b]);
	    	}else if(r2br1a >= 0){
	    		o_dest.y=(buf[r2br1a+c1ac1b]- buf[r2br1a+c2ac2b])*2+( - buf[r2ar2b+c1ac1b] + buf[r2ar2b+c2ac2b]);
	    	}else if(r2ar2b>=0){
	    		o_dest.y= - buf[r2ar2b+c1ac1b] + buf[r2ar2b+c2ac2b];
	    	}else{
	    		o_dest.y=0;
	    	}
	    }else if(c2ac2b >= 0){
	    	if(r1ar1b >= 0){
	    		o_dest.y=(- (buf[r2br1a+c2ac2b]*2) + buf[r2ar2b+c2ac2b])-(- buf[r1ar1b+c2ac2b]);
	    	}else if(r2br1a >= 0){
	    		o_dest.y=(-(buf[r2br1a+c2ac2b]*2) + buf[r2ar2b+c2ac2b]);
	    	}else if(r2ar2b >= 0){
	    		o_dest.y=buf[r2ar2b+c2ac2b];
	    	}else{
	    		o_dest.y=0;
	    	}
	    }else{
	    	o_dest.y=0;
	    }
	    
/*	    
	    int test=this.getBoxIntegral(column,row-s/2, s/2, s) - this.getBoxIntegral(column-s/2, row-s/2, s/2, s);
	    if(test!=o_dest.x){
	    	System.out.println("NOOOOO");
	    }

	    test=this.getBoxIntegral(column-s/2, row,     s, s/2) - this.getBoxIntegral(column-s/2, row-s/2, s, s/2);
	    if(o_dest.y!=test){
	    	System.out.println("NOOOOO");
	    }*/
		return;
	}		

	private HaarResponse __tmp=new HaarResponse();
	private double[]    __resX=new double[109];
	private double[]    __resY=new double[109];
	private double[]    __Ang=new double[109];
    private static int[] __orientation_id={6,5,4,3,2,1,0,1,2,3,4,5,6};	
	public double getOrientation(int i_x,int i_y,double i_scale)
	{
		//HarrResponse
		HaarResponse tmp=this.__tmp;
		double[]    resX=this.__resX;
		double[]    resY=this.__resY;
		double[]    Ang=this.__Ang;
		
//		double    gauss = 0.0f;
		int      id[] = __orientation_id;
		int      s = (int)(i_scale + 0.5);
//		int      c = (int)(i_x + 0.5);
//		int      r = (int)(i_y + 0.5);
		// calculate haar responses for points within radius of 6*scale
		int idx = 0;
		for(int i = -6; i <= 6; i++){
			for(int j = -6; j <= 6; j++) {
				if( i*i + j*j >= 36 ){
					continue;
				}
				haarXY(i_x+i*s,i_y+j*s,  4*s,tmp);

				double gauss = NyARGaussTable.gauss25[id[i+6]][id[j+6]]; // Or: gauss25[abs(i)][abs(j)]	
				//Ang[idx]  = getAngle(tmp.x, tmp.y);
				double ang;
				Ang[idx]=ang= Math.atan2(tmp.y, tmp.x);
				if(ang<0){
					Ang[idx]+=Math.PI * 2.0f;
				}
				resX[idx] = gauss * tmp.x;//haarX(r+j*s, c+i*s, 4*s);
				resY[idx] = gauss * tmp.y;//haarY(r+j*s, c+i*s, 4*s);
//				Ang[idx]  = getAngle(resX[idx], resY[idx]);
				idx++;
			}
		}
		// calculate the dominant direction 
		double maxSumX=0.0f, maxSumY=0.0f;
		double max=0.0f;

		//@todo: 1/3 の領域�?割して、その�?大のところ�?けを得ることで雑音除去。これど�?にかなるんじゃな�?か�?
		//で�?0.15は3.14/21の近似値
		
		// loop slides pi/3 window around feature point
		for(double ang1 = 0; ang1 < 2.0f*Math.PI;  ang1+=0.15f) {
			double ang2 = ( ang1 + Math.PI/3.0 > 2.0*Math.PI ? ang1 - 5.0*Math.PI/3 : ang1 +Math.PI/3.0);
			double sumX, sumY;
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
		HaarResponse tmp=this.__tmp;
		int        count=0;
		
		
		double      len = 0.0f;
		double      cx = -0.5f, cy = 0.0f; //Subregion centers for the 4x4 gaussian weighting

//		int x = (int)(i_x + 0.5);
//		int y = (int)(i_y + 0.5);

		double co = Math.cos(i_orientation);
		double si = Math.sin(i_orientation);
		double ssi=i_scale*si;
		double coi=i_scale*co;

		int i = -8;

		//Calculate descriptor for this interest point
		while(i < 12) {
			int j = -8;
			i = i-4;
			cx += 1.f;
			cy = -0.5f;

			while(j < 12) {
				double     dx, dy, mdx, mdy;
				dx=dy=mdx=mdy=0.0f;
				cy += 1.f;
				j  = j - 4;
				int ix = i + 5;
				int jx = j + 5;
				
				double rtmp;
				//int xs = round(x + (-jx*ssi + ix*coi));
				rtmp = i_x + (-jx*ssi + ix*coi);
				int xs=(int)(rtmp>0?rtmp+0.5:rtmp-0.5);

				//int ys = round(y + ( jx*coi + ix*ssi));
				rtmp = i_y + ( jx*coi + ix*ssi);
				int ys=(int)(rtmp>0?rtmp+0.5:rtmp-0.5);

				for (int k = i; k < i + 9; ++k){
					for (int l = j; l < j + 9; ++l) {
						//Get coords of sample point on the rotated axis
						
						//int sample_x = round(x + (-l*ssi + k*coi));//modified 
						rtmp = i_x + (-l*ssi + k*coi);//modified 
						int sample_x=(int)(rtmp>0?rtmp+0.5:rtmp-0.5);
						
						rtmp=(i_y + ( l*coi + k*ssi));//modified
						int sample_y=(int)(rtmp>0?rtmp+0.5:rtmp-0.5);

						//Get the gaussian weighted x and y responses
						double gauss_s1 = NyARGaussTable.gaussian(xs-sample_x,ys-sample_y,2.5f*i_scale);

						haarXY(sample_x,sample_y, 2*((int)(i_scale+0.5)),tmp);
						

						//Get the gaussian weighted x and y responses on rotated axis
						double rrx = gauss_s1*(-tmp.x*si + tmp.y*co);
						double rry = gauss_s1*( tmp.x*co + tmp.y*si);

						dx += rrx;
						dy += rry;
						mdx += rrx<0?-rrx:rrx;//Math.abs(rrx);
						mdy += rry<0?-rry:rry;//Math.abs(rry);
					}
				}

				//Add the values to the descriptor vector
				double gauss_s2 = NyARGaussTable.gaussian(cx-2.0f,cy-2.0f,1.5f);

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
}