package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.KpmImage;

public class interpole {
    /**
     * Perform bilinear interpolation.
     *
     * @param[in] im Image
     * @param[in] width Widht of image
     * @param[in] height Height of image
     * @param[in] step Width step
     * @param[in] x x-location to interpolate
     * @param[in] y y-location to interpolate
     */
     public static double bilinear_interpolation(double[] im,
                                       int width,
                                       int height,
                                       double x,
                                       double y) {
        int xp, yp;
        int xp_plus_1, yp_plus_1;
        double w0, w1, w2, w3;
        double res;
        


        // Compute location of 4 neighbor pixels
        xp = (int)x;
        yp = (int)y;
        xp_plus_1 = xp+1;
        yp_plus_1 = yp+1;
        
        // Some sanity checks
        
        // Pointer to 2 image rows
        int p0 = width*yp;
        int p1 = p0+width;
        
        // Compute weights
        w0 = (xp_plus_1-x)*(yp_plus_1-y);
        w1 = (x-xp)*(yp_plus_1-y);
        w2 = (xp_plus_1-x)*(y-yp);
        w3 = (x-xp)*(y-yp);
        
        
        // Compute weighted pixel
        res = w0*im[p0+xp] + w1*im[p0+xp_plus_1] + w2*im[p1+xp] + w3*im[p1+xp_plus_1];
        
        return res;
    }

    
    /**
     * Bilinear interpolation. Integer pixel locations specify the center of the pixel.
     *
     * @param[in] im Image
     * @param[in] x
     * @param[in] y
     */
     public static double bilinear_interpolation(KpmImage im,
    		 double x,
    		 double y) {
    	return bilinear_interpolation((double[])im.getBuffer(), im.getWidth(), im.getHeight(), x, y);
    }
}
