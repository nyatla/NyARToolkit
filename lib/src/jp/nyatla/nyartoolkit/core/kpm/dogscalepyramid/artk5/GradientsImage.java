package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.artk5;


import jp.nyatla.nyartoolkit.core.kpm.KpmImage;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.utils.BilinearHistogram;
import jp.nyatla.nyartoolkit.core.math.NyARMath;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class GradientsImage
{
	final protected static double PI=NyARMath.PI;
	final protected NyARIntSize _size;
	final protected double[] _angle;
	final protected double[] _mag;

	public GradientsImage(int i_width, int i_height) {
		this._size = new NyARIntSize(i_width, i_height);
		this._angle = new double[i_width * i_height];
		this._mag = new double[i_width * i_height];
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
		double dx, dy;
		double[] a_gradient=this._angle;
		double[] m_gradient=this._mag;
		int width=this._size.w;
		int height=this._size.h;
		assert this._size.isEqualSize(null);
		double[] im=(double[])i_img.getBuffer();


		int width_minus_1;
		int height_minus_1;

		int p_ptr;
		int pm1_ptr;
		int pp1_ptr;

		width_minus_1 = width - 1;
		height_minus_1 = height - 1;
		int gradient_ptr = 0;

		// Top row
		pm1_ptr = 0; // pm1_ptr = im;
		p_ptr = 0; // p_ptr = im;
		pp1_ptr = width;// pp1_ptr = p_ptr+width;

		dx = im[p_ptr + 1] - im[p_ptr];// dx = p_ptr[1] - p_ptr[0];
		dy = im[pp1_ptr] - im[pm1_ptr];// dy = pp1_ptr[0] - pm1_ptr[0];
		// SET_GRADIENT(dx, dy)
		a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
		m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
		gradient_ptr++;
		p_ptr++;
		pm1_ptr++;
		pp1_ptr++;

		for (int col = 1; col < width_minus_1; col++) {
			dx = im[p_ptr + 1] - im[p_ptr - 1];
			dy = im[pp1_ptr] - im[pm1_ptr];
			// SET_GRADIENT(dx, dy)
			a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
			m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
			gradient_ptr++;
			p_ptr++;
			pm1_ptr++;
			pp1_ptr++;
		}

		dx = im[p_ptr] - im[p_ptr - 1];
		dy = im[pp1_ptr] - im[pm1_ptr];
		// SET_GRADIENT(dx, dy)
		a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
		m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
		gradient_ptr++;
		p_ptr++;
		pm1_ptr++;
		pp1_ptr++;

		// Non-border pixels
		pm1_ptr = 0;// pm1_ptr = im;
		p_ptr = pm1_ptr + width;
		pp1_ptr = p_ptr + width;

		for (int row = 1; row < height_minus_1; row++) {
			dx = im[p_ptr + 1] - im[p_ptr];
			dy = im[pp1_ptr] - im[pm1_ptr];
			// SET_GRADIENT(dx, dy)
			a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
			m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
			gradient_ptr++;
			p_ptr++;
			pm1_ptr++;
			pp1_ptr++;

			for (int col = 1; col < width_minus_1; col++) {
				dx = im[p_ptr + 1] - im[p_ptr - 1];
				dy = im[pp1_ptr] - im[pm1_ptr];
				// SET_GRADIENT(dx, dy)
				a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
				m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
				gradient_ptr++;
				p_ptr++;
				pm1_ptr++;
				pp1_ptr++;
			}
			dx = im[p_ptr] - im[p_ptr - 1];
			dy = im[pp1_ptr] - im[pm1_ptr];
			// SET_GRADIENT(dx, dy)
			a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
			m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
			gradient_ptr++;
			p_ptr++;
			pm1_ptr++;
			pp1_ptr++;
		}

		// Lower row
		p_ptr = height_minus_1 * width;// p_ptr = &im[height_minus_1*width];
		pm1_ptr = p_ptr - width;
		pp1_ptr = p_ptr;

		dx = im[p_ptr + 1] - im[p_ptr];
		dy = im[pp1_ptr] - im[pm1_ptr];
		// SET_GRADIENT(dx, dy)
		a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
		m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
		gradient_ptr++;
		p_ptr++;
		pm1_ptr++;
		pp1_ptr++;

		for (int col = 1; col < width_minus_1; col++) {
			dx = im[p_ptr + 1] - im[p_ptr - 1];
			dy = im[pp1_ptr] - im[pm1_ptr];
			// SET_GRADIENT(dx, dy)
			a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
			m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
			gradient_ptr++;
			p_ptr++;
			pm1_ptr++;
			pp1_ptr++;

		}

		dx = im[p_ptr] - im[p_ptr - 1];
		dy = im[pp1_ptr] - im[pm1_ptr];
		// SET_GRADIENT(dx, dy)
		a_gradient[gradient_ptr] = (Math.atan2(dy, dx) + PI);
		m_gradient[gradient_ptr] = Math.sqrt(dx * dx + dy * dy);
		gradient_ptr++;
		p_ptr++;
		pm1_ptr++;
		pp1_ptr++;
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
	
}
