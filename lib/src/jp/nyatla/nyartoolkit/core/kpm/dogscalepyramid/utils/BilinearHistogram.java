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
package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.utils;

import jp.nyatla.nyartoolkit.core.math.NyARMath;

/**
 * {@link OrientationAssignment}クラスの内部クラス。
 */
public class BilinearHistogram
{
	final static private double ONE_OVER_2PI=       0.159154943091895;	
	final private static double PI=NyARMath.PI;
	final private double[] mHistogram;
	public BilinearHistogram(int i_nbits){
		this.mHistogram=new double[i_nbits];
	}
	/**
	 * Smooth the orientation histogram with a kernel.
	 * 
	 * @param[out] y Destination histogram (in-place processing supported)
	 * @param[in] x Source histogram
	 * @param[in] kernel size=3
	 */
	public void smoothOrientationHistogram(int i_num_smoothing)
	{
		int n=this.mHistogram.length;
		double[] h=this.mHistogram;
		double kernel[] = { 0.274068619061197f, 0.451862761877606f, 0.274068619061197f };
		for (int iter = 0; iter < i_num_smoothing; iter++) {
			// sigma=1
			double first = h[0];
			double prev = h[n - 1];
			for (int i = 0; i < n - 1; i++) {
				double cur = h[i];
				h[i] = kernel[0] * prev + kernel[1] * cur + kernel[2] * h[i + 1];
				prev = cur;
			}
			h[n - 1] = kernel[0] * prev + kernel[1] * h[n - 1] + kernel[2] * first;
		}		
	}
	/**
	 * Update a histogram with bilinear interpolation.
	 * bilinear_histogram_update関数
	 * @param[in/out] hist Histogram
	 * @param[in] fbin Decimal bin position to vote
	 * @param[in] magnitude Magnitude of the vote
	 * @param[in] num_bin Number of bins in the histogram
	 */
	public void bilinearHistogramUpdate(double angle, double magnitude)
	{

		double[] h=this.mHistogram;
		int n=this.mHistogram.length;

		// Compute the sub-bin location
		double fbin = (double) (n * angle * ONE_OVER_2PI);

		
		int bin = (int) Math.floor(fbin - 0.5f);
		double w2 = fbin - (double) bin - 0.5f;
		double w1 = (1.f - w2);
		int b1 = (bin + n) % n;
		int b2 = (bin + 1) % n;
		// Vote to 2 weighted bins
		h[b1] += w1 * magnitude;
		h[b2] += w2 * magnitude;
	}
	public void reset()
	{
		for(int i=this.mHistogram.length-1;i>=0;i--){
			this.mHistogram[i]=0;
		}
	}
	public int findPeak(double mPeakThreshold,double[] i_list)
	{
		double[] R = new double[3];
		int mNumBins=this.mHistogram.length;
		double max_height=0;
		// Find the peak of the histogram.
		for (int i = 0; i < mNumBins; i++) {
			if (mHistogram[i] > max_height) {
				max_height = mHistogram[i];
			}
		}
		// The max height should be positive.
		if (max_height == 0) {
			return 0;
		}
		int num_of_peak=0;
		// Find all the peaks.
		for (int i = 0; i < mNumBins; i++) {
			double p0[] = { i, mHistogram[i] };
			double pm1[] = { (i - 1), mHistogram[(i - 1 + mNumBins) % mNumBins] };
			double pp1[] = { (i + 1), mHistogram[(i + 1 + mNumBins) % mNumBins] };

			// Ensure that "p0" is a relative peak w.r.t. the two neighbors
			if ((mHistogram[i] > mPeakThreshold * max_height) && (p0[1] > pm1[1]) && (p0[1] > pp1[1])) {
				double fbin;
				// The default sub-pixel bin location is the discrete location if the quadratic
				// fitting fails.
				fbin = i;

				// Fit a quatratic to the three bins
				if (Quadratic3Points(R, pm1, p0, pp1)) {
					// If "QuadraticCriticalPoint" fails, then "fbin" is not updated.
					QuadraticCriticalPoint(R, R[0], R[1], R[2]);// チェックしなくていいの？
					fbin = R[0];
				}

				// The sub-pixel angle needs to be in the range [0,2*pi)
				// angles[num_angles] = std::fmod((2.f*PI)*((fbin+0.5f+(float)mNumBins)/(float)mNumBins), 2.f*PI);
				i_list[num_of_peak] = ((2.f * PI) * ((fbin + 0.5 + (double) mNumBins) / (double) mNumBins)) % (double) (2.f * PI);

				// Increment the number of angles
				num_of_peak++;
			}
		}
		return num_of_peak;
	}
    /**
     * Fit a quatratic to 3 points. The system of equations is:
     *
     * y0 = A*x0^2 + B*x0 + C
     * y1 = A*x1^2 + B*x1 + C
     * y2 = A*x2^2 + B*x2 + C
     *
     * This system of equations is solved for A,B,C.
     *
     * @param[out] A
     * @param[out] B
     * @param[out] C
     * @param[in] p1 2D point 1
     * @param[in] p2 2D point 2
     * @param[in] p3 2D point 3
     * @return True if the quatratic could be fit, otherwise false.
     */
	final private static boolean Quadratic3Points(double r[],
			double[] p1,
			double[] p2,
			double[] p3) {
		double d1 = (p3[0]-p2[0])*(p3[0]-p1[0]);
		double d2 = (p1[0]-p2[0])*(p3[0]-p1[0]);
		double d3 = p1[0]-p2[0];
        // If any of the denominators are zero then return FALSE.
		if(d1 == 0 || d2 == 0 || d3 == 0) {
			r[0] = 0;
			r[1] = 0;
			r[2] = 0;
			return false;
		}
		else {
			double a = p1[0]*p1[0];
			double b = p2[0]*p2[0];
			
            // Solve for the coefficients A,B,C
			double A,B;
			r[0] =A= ((p3[1]-p2[1])/d1)-((p1[1]-p2[1])/d2);
			r[1] =B= ((p1[1]-p2[1])+(A*(b-a)))/d3;
			r[2]   = p1[1]-(A*a)-(B*p1[0]);
			return true;
		}
	}
    /**
	 * Find the critical point of a quadratic.
     *
     * y = A*x^2 + B*x + C
     *
     * This function finds where "x" where dy/dx = 0.
	 *
     * @param[out] x Parameter of the critical point.
     * @param[in] A
     * @param[in] B
     * @param[in] C
	 * @return True on success.
	 */
	final private static boolean QuadraticCriticalPoint(double[] x, double A, double B, double C) {
		if(A == 0) {
			return false;
		}
		x[0] = -B/(2*A);
		return true;
	}
}
