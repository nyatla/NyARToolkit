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
package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.gradientimage;

import jp.nyatla.nyartoolkit.core.kpm.KpmImage;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.utils.BilinearHistogram;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.utils.FastMath;

public final class GradientsImage_O1 extends GradientsImage_ARTK{

	public GradientsImage_O1(int i_width, int i_height)
	{
		super(i_width, i_height);
	}
	/**
	 * OrientationAssignment.computeの一部
	 * @param x
	 * @param y
	 * @param radius
	 * @param gw_scale
	 * @param i_histogram
	 */
	public void buildOrientationHistogram(double x,double y,double radius,double gw_scale,BilinearHistogram i_histogram)
	{
		double[] a_buf=this._angle;
		double[] m_buf=this._mag;
		int xi = (int) (x + 0.5f);
		int yi = (int) (y + 0.5f);		
		// Box around feature point
		int x0 = xi - (int) (radius + 0.5f);
		int x1 = xi + (int) (radius + 0.5f);
		int y0 = yi - (int) (radius + 0.5f);
		int y1 = yi + (int) (radius + 0.5f);

		// Clip the box to be within the bounds of the image
		int width_1=this._size.w - 1;
		int height_1=this._size.h - 1;
		if(x0<0){ x0=0;}//x0 = math_utils.max2(0, x0);
		if(x1>width_1){x1=width_1;}//x1 = math_utils.min2(x1, (int) g.getWidth() - 1);
		if(y0<0){y0=0;}//y0 = math_utils.max2(0, y0);
		if(y1>height_1){y1=height_1;}//y1 = math_utils.min2(y1, (int) g.getHeight() - 1);
		
		double radius2 = Math.ceil(radius*radius);
		// Build up the orientation histogram
		for (int yp = y0; yp <= y1; yp++) {
			double dy = yp - y;
			double dy2 = (dy*dy);

			int y_ptr = this._size.w * yp;

			for (int xp = x0; xp <= x1; xp++) {
				double dx = xp - x;
				double r2 = (dx*dx) + dy2;

				// Only use the gradients within the circular window
				if (r2 > radius2) {
					continue;
				}
				int g2_ptr = y_ptr + xp; // const float* g = &y_ptr[xp<<1];
				double angle = a_buf[g2_ptr];// const float& angle = g[0];
				double mag = m_buf[g2_ptr];// const float& mag = g[1];

				// Compute the gaussian weight based on distance from center of keypoint
				double w = fastexp6(r2 * gw_scale);


				// Vote to the orientation histogram with a bilinear update
				i_histogram.bilinearHistogramUpdate(angle,w * mag);
			}
		}		
	}
	public void computePolarGradients(KpmImage i_img)
	{
		double[] a_gradient=this._angle;
		double[] m_gradient=this._mag;
		double dx, dy;
		int width=this._size.w;
		int height=this._size.h;
		double[] im=(double[])i_img.getBuffer();



		int p_ptr;


		int width_minus_2 = width - 2;
		int height_minus_2 = height - 2;


		//Left Top
		p_ptr = 0; // p_ptr = im;
		dx = im[p_ptr + 1] - im[p_ptr];// dx = p_ptr[1] - p_ptr[0];
		dy = im[p_ptr+width] - im[p_ptr];// dy = pp1_ptr[0] - pm1_ptr[0];
		// SET_GRADIENT(dx, dy)
		a_gradient[p_ptr] = FastMath.fastAtan2(dy, dx) + Math.PI;
		m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
		p_ptr++;

		//Top row
		for (int col = width_minus_2; col>0 ; col--) {
			dx = im[p_ptr + 1] - im[p_ptr - 1];
			dy = im[p_ptr+width] - im[p_ptr];
			// SET_GRADIENT(dx, dy)
			a_gradient[p_ptr] = FastMath.fastAtan2(dy, dx) + Math.PI;
			m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
			p_ptr++;
		}

		//Right Top
		dx = im[p_ptr] - im[p_ptr - 1];
		dy = im[p_ptr+width] - im[p_ptr];
		// SET_GRADIENT(dx, dy)
		a_gradient[p_ptr] = FastMath.fastAtan2(dy, dx) + Math.PI;
		m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);


		//Non-Border
		p_ptr =width;
		for (int row = height_minus_2; row>0; row--) {
			//Left
			dx = im[p_ptr + 1] - im[p_ptr];
			dy = im[p_ptr+width] - im[p_ptr-width];
			// SET_GRADIENT(dx, dy)
			a_gradient[p_ptr] = FastMath.fastAtan2(dy, dx) + Math.PI;
			m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
			p_ptr++;

			for (int col = width_minus_2; col>0 ; col--) {
				dx = im[p_ptr + 1] - im[p_ptr - 1];
				dy = im[p_ptr+width] - im[p_ptr-width];
				// SET_GRADIENT(dx, dy)
				a_gradient[p_ptr] = FastMath.fastAtan2(dy, dx)+Math.PI;//(Math.atan2(dy, dx) + PI);
				m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
				p_ptr++;
			}
			//Right
			dx = im[p_ptr] - im[p_ptr - 1];
			dy = im[p_ptr+width] - im[p_ptr-width];
			// SET_GRADIENT(dx, dy)
			a_gradient[p_ptr] = (FastMath.fastAtan2(dy, dx) + Math.PI);
			m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
			p_ptr++;
		}
		
		// Lower row
		p_ptr = (height-1) * width;// p_ptr = &im[height_minus_1*width];
		dx = im[p_ptr + 1] - im[p_ptr];
		dy = im[p_ptr] - im[p_ptr - width];
		// SET_GRADIENT(dx, dy)
		a_gradient[p_ptr] = (FastMath.fastAtan2(dy, dx) + Math.PI);
		m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
		p_ptr++;
		for (int col = width_minus_2; col>0 ; col--) {
			dx = im[p_ptr + 1] - im[p_ptr - 1];
			dy = im[p_ptr] - im[p_ptr - width];
			// SET_GRADIENT(dx, dy)
			a_gradient[p_ptr] = (FastMath.fastAtan2(dy, dx) + Math.PI);
			m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
			p_ptr++;

		}
		dx = im[p_ptr] - im[p_ptr - 1];
		dy = im[p_ptr] - im[p_ptr - width];
		// SET_GRADIENT(dx, dy)
		a_gradient[p_ptr] = (FastMath.fastAtan2(dy, dx) + Math.PI);
		m_gradient[p_ptr] = Math.sqrt(dx * dx + dy * dy);
	}
	
    /**
     * 0.01% error at 1.030
     * 0.10% error at 1.520
     * 1.00% error at 2.330
     * 5.00% error at 3.285
     */
    final private static double fastexp6(double x) {
        return (720+x*(720+x*(360+x*(120+x*(30+x*(6+x))))))*0.0013888888;
    }
    public static void main(String[] args){
    	KpmImage in=new KpmImage(640,480);
    	for(int i=0;i<640*480;i++){
    		((double[])in.getBuffer())[i]=Math.random()*255;
    	}
    	GradientsImage_ARTK gs1=new GradientsImage_ARTK(640,480);
    	GradientsImage_ARTK gs2=new GradientsImage_O1(640,480);
    	
       	gs1.computePolarGradients(in);
       	gs2.computePolarGradients(in);
		// Compute the gradient pyramid
		for(int i2=0;i2<10;i2++){
			long s=System.currentTimeMillis();
			for(int i=0;i<50;i++){
				gs1.computePolarGradients(in);
			}
			long s2=System.currentTimeMillis();
			for(int i=0;i<50;i++){
				gs2.computePolarGradients(in);
			}
			long e=System.currentTimeMillis();
			double d=0;
			for(int i=0;i<640*480;i++){
				double t=((double[])gs1.getAngle())[i]-((double[])gs2.getAngle())[i];
				double t2=((double[])gs1.getMag())[i]-((double[])gs2.getMag())[i];
				d+=t+t2;
			}
			double ga1=(s2-s)/50f;
			double ga2=(e-s2)/50f;
			System.out.println("GS1="+ga1+"ms");
			System.out.println("GS2="+ga2+"ms");
			System.out.println("DIFF="+(ga1/ga2)+"    "+d);
		}
   	
    }

}
