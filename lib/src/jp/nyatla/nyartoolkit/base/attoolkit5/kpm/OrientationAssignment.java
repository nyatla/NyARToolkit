/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.detectors.GaussianScaleSpacePyramid;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.math_utils;

public class OrientationAssignment {
    public OrientationAssignment()
    {
    	this.mNumOctaves=0;
    	this.mNumScalesPerOctave=0;
    	this.mGaussianExpansionFactor=0;
    	this.mSupportRegionExpansionFactor=0;
    	this.mNumSmoothingIterations=0;
    	this.mPeakThreshold=0;
    }

    
    /**
     * Allocate memory.
     */
    public void alloc(int fine_width,
               int fine_height,
               int num_octaves,
               int num_scales_per_octave,
               int num_bins,
               double gaussian_expansion_factor,
               double support_region_expansion_factor,
               int num_smoothing_iterations,
               double peak_threshold)
    {
        this.mNumOctaves = num_octaves;
        this.mNumScalesPerOctave = num_scales_per_octave;
        this.mNumBins = num_bins;
        this.mGaussianExpansionFactor = gaussian_expansion_factor;
        this.mSupportRegionExpansionFactor = support_region_expansion_factor;
        this.mNumSmoothingIterations = num_smoothing_iterations;
        this.mPeakThreshold = peak_threshold;
        
        this.mHistogram=new double[num_bins];
        
        // Allocate gradient images
        this.mGradients=new GradientsImage[this.mNumOctaves*this.mNumScalesPerOctave];
        for(int i = 0; i < num_octaves; i++) {
            for(int j = 0; j < num_scales_per_octave; j++) {
//                mGradients[i*num_scales_per_octave+j].alloc(IMAGE_F32,
//                                                            fine_width>>i,
//                                                            fine_height>>i,
//                                                            AUTO_STEP,
//                                                            2);
            	//これKpmImageじゃなくて単純なfloatbufferにしようあとで。どうせバッファオーバフローで落ちるから。
                mGradients[i*num_scales_per_octave+j]=new GradientsImage(fine_width>>i,fine_height>>i);
                
            }
        }    	
    }
    
    /**
     * Compute the gradients given a pyramid.
     */
    public void computeGradients(GaussianScaleSpacePyramid pyramid)
    {
        // Loop over each pyramid image and compute the gradients
        for(int i = 0; i < pyramid.images().length; i++) {
            KpmImage im = pyramid.images()[i];

            // Compute gradient image
//            ASSERT(im.width() == im.step()/sizeof(float), "Step size must be equal to width for now");
            ComputePolarGradients(
            	(double[])mGradients[i].getBuffer(),
            	(double[])im.getBuffer(),
	              im.getWidth(),
	              im.getHeight());
        }    	
    }
    public static class FloatVector{
    	public FloatVector(double[] mOrientations, int i)
    	{
    		this.v=mOrientations;
    		this.num=i;
		}
		public double[] v;
    	public int num;
    }
    /**
     * Compute orientations for a keypont.
     */
    public void compute(
		FloatVector angles,
		int octave,int scale,
		double x,double y,double sigma)
    {
        int xi, yi;
        double radius;
        double radius2;
        int x0, y0;
        int x1, y1;
        double max_height;
        double gw_sigma, gw_scale;
        
//        ASSERT(x >= 0, "x must be positive");
//        ASSERT(x < mGradients[octave*mNumScalesPerOctave+scale].width(), "x must be less than the image width");
//        ASSERT(y >= 0, "y must be positive");
//        ASSERT(y < mGradients[octave*mNumScalesPerOctave+scale].height(), "y must be less than the image height");
        
        int level = octave*mNumScalesPerOctave+scale;
        GradientsImage g = mGradients[level];
        double[] g_buf=(double[])g.getBuffer();
//        ASSERT(g.channels() == 2, "Number of channels should be 2");
        
        max_height = 0;
        angles.num = 0;
        
        xi = (int)(x+0.5f);
        yi = (int)(y+0.5f);
        
        // Check that the position is with the image bounds
        if(xi < 0 ||
           xi >= g.getWidth() ||
           yi < 0 ||
           yi >= g.getHeight())
        {
            return;
        }
        
        gw_sigma = math_utils.max2(1.f, mGaussianExpansionFactor*sigma);
        gw_scale = -1.f/(2*math_utils.sqr(gw_sigma));
        
        // Radius of the support region
        radius  = mSupportRegionExpansionFactor*gw_sigma;
        radius2 = Math.ceil(math_utils.sqr(radius));
        
        // Box around feature point
        x0 = xi-(int)(radius+0.5f);
        x1 = xi+(int)(radius+0.5f);
        y0 = yi-(int)(radius+0.5f);
        y1 = yi+(int)(radius+0.5f);
        
        // Clip the box to be within the bounds of the image
        x0 = math_utils.max2(0, x0);
        x1 = math_utils.min2(x1, (int)g.getWidth()-1);
        y0 = math_utils.max2(0, y0);
        y1 = math_utils.min2(y1, (int)g.getHeight()-1);
        
        // Zero out the orientation histogram
        math_utils.ZeroVector(this.mHistogram, mHistogram.length);
        
        // Build up the orientation histogram
        for(int yp = y0; yp <= y1; yp++) {
        	double dy = yp-y;
        	double dy2 = math_utils.sqr(dy);
            
            int y_ptr = g.get(yp);
            
            for(int xp = x0; xp <= x1; xp++) {
            	double dx = xp-x;
            	double r2 = math_utils.sqr(dx)+dy2;
                
                // Only use the gradients within the circular window
                if(r2 > radius2) {
                    continue;
                }
                int g2_ptr=y_ptr+(xp<<1);	//const float* g = &y_ptr[xp<<1];
                double angle = g_buf[g2_ptr+0];//const float& angle = g[0];
                double mag   = g_buf[g2_ptr+1];//const float& mag   = g[1];
                
                // Compute the gaussian weight based on distance from center of keypoint
                double w = math_utils.fastexp6(r2*gw_scale);
                
                // Compute the sub-bin location
                double fbin  = (double)(mNumBins*angle*math_utils.ONE_OVER_2PI);
                
                // Vote to the orientation histogram with a bilinear update
                this.bilinear_histogram_update(this.mHistogram, fbin, w*mag, mNumBins);
            }
        }
        
        // The orientation histogram is smoothed with a Gaussian
        for(int iter = 0; iter < mNumSmoothingIterations; iter++) {
            // sigma=1
        	double kernel[] = {
                0.274068619061197f,
                0.451862761877606f,
                0.274068619061197f};
            this.SmoothOrientationHistogram(mHistogram,mHistogram, mNumBins, kernel);
        }
        
        // Find the peak of the histogram.
        for(int i = 0; i < mNumBins; i++) {
            if(mHistogram[i] > max_height) {
                max_height = mHistogram[i];
            }
        }
        
        // The max height should be positive.
        if(max_height == 0) {
            return;
        }
        
 //       ASSERT(max_height > 0, "Maximum bin should be positive");
        
        // Find all the peaks.
        for(int i = 0; i < mNumBins; i++) {
        	double p0[]  = {i, mHistogram[i]};
        	double pm1[] = {(i-1), mHistogram[(i-1+mNumBins)%mNumBins]};
        	double pp1[] = {(i+1), mHistogram[(i+1+mNumBins)%mNumBins]};
            
            // Ensure that "p0" is a relative peak w.r.t. the two neighbors
            if((mHistogram[i] > mPeakThreshold*max_height) && (p0[1] > pm1[1]) && (p0[1] > pp1[1])) {
            	double A, B, C, fbin;
            	double[] R=new double[3];
                // The default sub-pixel bin location is the discrete location if the quadratic
                // fitting fails.
                fbin = i;
                
                // Fit a quatratic to the three bins
                if(math_utils.Quadratic3Points(R, pm1, p0, pp1)) {
                    // If "QuadraticCriticalPoint" fails, then "fbin" is not updated.
                	math_utils.QuadraticCriticalPoint(R, R[0],R[1],R[2]);//チェックしなくていいの？
                	fbin=R[0];
                }
                
                // The sub-pixel angle needs to be in the range [0,2*pi)
//                angles[num_angles] = std::fmod((2.f*PI)*((fbin+0.5f+(float)mNumBins)/(float)mNumBins), 2.f*PI);
                angles.v[angles.num] = ((2.f*math_utils.PI)*((fbin+0.5+(double)mNumBins)/(double)mNumBins))%(double)(2.f*math_utils.PI);
                
                // Increment the number of angles
                angles.num++;
            }
        }    	
    }
    
    /**
     * @return Vector of images.
     */
    public GradientsImage[] images(){ return this.mGradients; }
    
    /**
     * Get a gradient image at an index.
     */
    public GradientsImage get(int i){ return mGradients[i]; }
    
    
    private int mNumOctaves;
    private int mNumScalesPerOctave;
    
    // Number of bins in the histogram
    private int mNumBins;
    
    // Factor to expand the Gaussian weighting function. The Gaussian sigma is computed
    // by expanding the feature point scale. The feature point scale represents the isometric
    // size of the feature. 
    private double mGaussianExpansionFactor;
    
    // Factor to expand the support region. This factor is multipled by the expanded
    // Gaussian sigma. It essentially acts at the "window" to collect gradients in.
    private double mSupportRegionExpansionFactor;
    
    // Number of binomial smoothing iterations of the orientation histogram. The histogram
    // is smoothed before find the peaks.
    private int mNumSmoothingIterations;
    
    // All the supporting peaks which are X percent of the absolute peak are considered
    // dominant orientations. 
    private double mPeakThreshold;
    
    // Orientation histogram
    private double[] mHistogram;
    
    // Vector of gradient images
    private GradientsImage[] mGradients;
    
    
    /**
     * Update a histogram with bilinear interpolation.
     *
     * @param[in/out] hist Histogram
     * @param[in] fbin Decimal bin position to vote
     * @param[in] magnitude Magnitude of the vote
     * @param[in] num_bin Number of bins in the histogram
     */
    private void bilinear_histogram_update(double[] hist,
    		double fbin,
    		double magnitude,
                                          int num_bins) {
//        ASSERT(hist != NULL, "Histogram pointer is NULL");
//        ASSERT((fbin+0.5f) > 0 && (fbin-0.5f) < num_bins, "Decimal bin position index out of range");
//        ASSERT(magnitude >= 0, "Magnitude cannot be negative");
//        ASSERT(num_bins >= 0, "Number bins must be positive");
        
        int bin = (int)Math.floor(fbin-0.5f);
        double w2 = fbin-(double)bin-0.5f;
        double w1 = (1.f-w2);
        int b1 = (bin+num_bins)%num_bins;
        int b2 = (bin+1)%num_bins;
        
//        ASSERT(w1 >= 0, "w1 must be positive");
//        ASSERT(w2 >= 0, "w2 must be positive");        
//        ASSERT(b1 >= 0 && b1 < num_bins, "b1 bin index out of range");
//        ASSERT(b2 >= 0 && b2 < num_bins, "b2 bin index out of range");
        
        // Vote to 2 weighted bins
        hist[b1] += w1*magnitude;
        hist[b2] += w2*magnitude;
    }
    
    /**
     * Smooth the orientation histogram with a kernel.
     *
     * @param[out] y Destination histogram (in-place processing supported) 
     * @param[in] x Source histogram
     * @param[in] kernel
     */
    public void SmoothOrientationHistogram(double[] y,double[] x,int n,double[] kernel) {
    	double first = x[0];
    	double prev = x[n-1];
        for(int i = 0; i < n-1; i++) {
        	double cur = x[i];
            y[i] = kernel[0]*prev + kernel[1]*cur + kernel[2]*x[i+1];
            prev = cur;
        }
        y[n-1] = kernel[0]*prev + kernel[1]*x[n-1] + kernel[2]*first;
    }
 
    
    void ComputePolarGradients(
    		double[] gradient,
    		double[] im,
            int width,
            int height)
    {

//		#define SET_GRADIENT(dx, dy)                \
//		*(gradient++) = std::atan2(dy, dx)+PI;      \
//		*(gradient++) = std::sqrt(dx*dx+dy*dy);     \
//		p_ptr++; pm1_ptr++; pp1_ptr++;              \

		int width_minus_1;
		int height_minus_1;

		double dx, dy;
		int p_ptr;
		int pm1_ptr;
		int pp1_ptr;

		width_minus_1 = width-1;
		height_minus_1 = height-1;
		int gradient_ptr=0;

		// Top row
		pm1_ptr=0;	//pm1_ptr = im;
		p_ptr=0;	//p_ptr   = im;
		pp1_ptr=width;//pp1_ptr = p_ptr+width;
		
		dx=im[p_ptr+1]-im[p_ptr+0];//dx = p_ptr[1] - p_ptr[0];
		dy = im[pp1_ptr+0] - im[pm1_ptr+0];//dy = pp1_ptr[0] - pm1_ptr[0];
//		SET_GRADIENT(dx, dy)
		gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
		gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
		p_ptr++; pm1_ptr++; pp1_ptr++;   		
		
		for(int col = 1; col < width_minus_1; col++) {
			dx = im[p_ptr+1] - im[p_ptr-1];//dx = p_ptr[1] - p_ptr[-1];
			dy = im[pp1_ptr+0] - im[pm1_ptr+0];//dy = pp1_ptr[0] - pm1_ptr[0];
//			SET_GRADIENT(dx, dy)
			gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
			gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
			p_ptr++; pm1_ptr++; pp1_ptr++;   		
		}
		
		dx = im[p_ptr+0] - im[p_ptr-1];//dx = p_ptr[0] - p_ptr[-1];
		dy = im[pp1_ptr+0] - im[pm1_ptr+0];//dy = pp1_ptr[0] - pm1_ptr[0];
//		SET_GRADIENT(dx, dy)
		gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
		gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
		p_ptr++; pm1_ptr++; pp1_ptr++;   		

		// Non-border pixels
		pm1_ptr = 0;//pm1_ptr = im;
		p_ptr   = pm1_ptr+width;
		pp1_ptr = p_ptr+width;

		for(int row = 1; row < height_minus_1; row++) {
			dx = im[p_ptr+1] - im[p_ptr+0];
			dy = im[pp1_ptr+0] - im[pm1_ptr+0];
//			SET_GRADIENT(dx, dy)
			gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
			gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
			p_ptr++; pm1_ptr++; pp1_ptr++;   		
			
			for(int col = 1; col < width_minus_1; col++) {
				dx = im[p_ptr+1] - im[p_ptr-1];
				dy = im[pp1_ptr+0] - im[pm1_ptr+0];
//				SET_GRADIENT(dx, dy)
				gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
				gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
				p_ptr++; pm1_ptr++; pp1_ptr++;   		
			}		
			dx = im[p_ptr+0] - im[p_ptr-1];
			dy = im[pp1_ptr+0] - im[pm1_ptr+0];
//			SET_GRADIENT(dx, dy)
			gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
			gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
			p_ptr++; pm1_ptr++; pp1_ptr++;   		
		}

		// Lower row
		p_ptr   = height_minus_1*width;//p_ptr   = &im[height_minus_1*width];
		pm1_ptr = p_ptr-width;
		pp1_ptr = p_ptr;
		
		dx = im[p_ptr+1] - im[p_ptr+0];
		dy = im[pp1_ptr+0] - im[pm1_ptr+0];
//		SET_GRADIENT(dx, dy)
		gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
		gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
		p_ptr++; pm1_ptr++; pp1_ptr++;
	
		for(int col = 1; col < width_minus_1; col++) {
			dx = im[p_ptr+1] - im[p_ptr-1];
			dy = im[pp1_ptr+0] - im[pm1_ptr+0];
//			SET_GRADIENT(dx, dy)
			gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
			gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
			p_ptr++; pm1_ptr++; pp1_ptr++;

		}

		dx = im[p_ptr+0]   - im[p_ptr-1];
		dy = im[pp1_ptr+0] - im[pm1_ptr+0];
//		SET_GRADIENT(dx, dy)
		gradient[gradient_ptr++] = (double) (Math.atan2(dy, dx)+math_utils.PI);//*(gradient++) = std::atan2(dy, dx)+PI;
		gradient[gradient_ptr++] = (double) Math.sqrt(dx*dx+dy*dy);//*(gradient++) = std::sqrt(dx*dx+dy*dy);
		p_ptr++; pm1_ptr++; pp1_ptr++;
    }
}
