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
package jp.nyatla.nyartoolkit.core.kpm.pyramid;

import jp.nyatla.nyartoolkit.core.kpm.KpmImage;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

public class BinomialPyramid32f extends GaussianScaleSpacePyramid
{
    final private int[] mTemp_us16;
    final private double[] mTemp_f32_1;
    public BinomialPyramid32f(int i_width, int i_height, int i_num_of_octaves, int i_num_scales_per_octaves)
    {    	
    	super(i_num_of_octaves,i_num_scales_per_octaves);
		// Allocate the pyramid memory
		this.mPyramid=new KpmImage[i_num_of_octaves*this.mNumScalesPerOctave];
		for(int i = 0; i < i_num_of_octaves; i++) {
			for(int j = 0; j < this.mNumScalesPerOctave; j++){
				this.mPyramid[i*this.mNumScalesPerOctave+j]=new KpmImage(i_width>>i, i_height>>i);
			}
		}
		this.mTemp_us16=new int[i_width*i_height];
		this.mTemp_f32_1=new double[i_width*i_height];
		return;
	}


    /**
     * Compute the number of octaves for a width/height from the minimum size
     * at the coarsest octave.
     * Original function is inline int numOctaves(int width, int height, int min_size)
     * @param width
     * @param height
     * @param min_size
     * @return
     */
	public static int octavesFromMinimumCoarsestSize(int width, int height,int min_size) {
		int num_octaves = 0;
		while (width >= min_size && height >= min_size) {
			width >>= 1;
			height >>= 1;
			num_octaves++;
		}
		return num_octaves;
	}
    public void build(INyARGrayscaleRaster i_raster)
    {
        assert(i_raster.getSize().isEqualSize(this.mPyramid[0].getSize()));
        assert(this.mPyramid.length == mNumOctaves*mNumScalesPerOctave);
        
        // First octave
        apply_filter(mPyramid[0],i_raster);
        apply_filter(mPyramid[1], mPyramid[0]);
        apply_filter_twice(mPyramid[2], mPyramid[1]);
        
        // Remaining octaves
        for(int i = 1; i < mNumOctaves; i++) {
            // Downsample
            downsample_bilinear(
            	(double[])mPyramid[i*mNumScalesPerOctave].getBuffer(),
            	(double[])mPyramid[i*mNumScalesPerOctave-1].getBuffer(),
                mPyramid[i*mNumScalesPerOctave-1].getWidth(),
                mPyramid[i*mNumScalesPerOctave-1].getHeight());
            
            // Apply binomial filters
            apply_filter(mPyramid[i*mNumScalesPerOctave+1], mPyramid[i*mNumScalesPerOctave]);
            apply_filter_twice(mPyramid[i*mNumScalesPerOctave+2], mPyramid[i*mNumScalesPerOctave+1]);
        }
        return;
    }	
    private void binomial_4th_order(double[] dst,int[] tmp,int[] src,int width,int height)
    {
		int tmp_ptr=0;

		int width_minus_1, width_minus_2;
		int height_minus_2;
		
		assert(width >= 5);//), "Image is too small");
		assert(height >= 5);//, "Image is too small");
		
		width_minus_1 = width-1;
		width_minus_2 = width-2;
		height_minus_2 = height-2;
		
		
		// Apply horizontal filter
		for(int row = 0; row < height; row++)
		{
			int src_ptr=row*width;
			// Left border is computed by extending the border pixel beyond the image

			tmp[tmp_ptr++] = ((src[src_ptr+0]<<1)+(src[src_ptr+0]<<2)) + ((src[src_ptr+0]+src[src_ptr+1])<<2) + (src[src_ptr+0]+src[src_ptr+2]);
			tmp[tmp_ptr++] = ((src[src_ptr+1]<<1)+(src[src_ptr+1]<<2)) + ((src[src_ptr+0]+src[src_ptr+2])<<2) + (src[src_ptr+0]+src[src_ptr+3]);
			
			// Compute non-border pixels
			for(int col = 2; col < width_minus_2; col++) {
				tmp[tmp_ptr++] = ((src[src_ptr+col]<<1)+(src[src_ptr+col]<<2)) + ((src[src_ptr+col-1]+src[src_ptr+col+1])<<2) + (src[src_ptr+col-2]+src[src_ptr+col+2]);
			}			
			// Right border. Computed similarily as the left border.
			tmp[tmp_ptr++] = ((src[src_ptr+width_minus_2]<<1)+(src[src_ptr+width_minus_2]<<2)) + ((src[src_ptr+width_minus_2-1]+src[src_ptr+width_minus_2+1])<<2) + (src[src_ptr+width_minus_2-2]+src[src_ptr+width_minus_2+1]);
			tmp[tmp_ptr++] = ((src[src_ptr+width_minus_1]<<1)+(src[src_ptr+width_minus_1]<<2)) + ((src[src_ptr+width_minus_1-1]+src[src_ptr+width_minus_1])<<2)   + (src[src_ptr+width_minus_1-2]+src[src_ptr+width_minus_1]);
		}
		
		int pm2;
		int pm1;
		int p;
		int pp1;
		int pp2;
		
		// Apply vertical filter along top border. This is applied twice as there are two
		// border pixels.
		pm2     = 0;//tmp
		pm1     = 0;//tmp
		p       = 0;//tmp
		pp1     = width;//p+width
		pp2     = pp1+width;//pp1+width		
		int dst_ptr = 0;
		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (((tmp[p]<<1)+(tmp[p]<<2)) + ((tmp[pm1]+tmp[pp1])<<2) + (tmp[pm2]+tmp[pp2]))*(1.f/256.f);
		}
		
		pm2     = 0;//tmp;
		pm1     = 0;//tmp;
		p       = width;//tmp+width;
		pp1     = p+width;
		pp2     = pp1+width;
		dst_ptr = width;
		
		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (((tmp[p]<<1)+(tmp[p]<<2)) + ((tmp[pm1]+tmp[pp1])<<2) + (tmp[pm2]+tmp[pp2]))*(1.f/256.f);
		}
		
		// Apply vertical filter for non-border pixels.
		pm2 = 0;//tmp;
		pm1 = pm2+width;
		p   = pm1+width;
		pp1 = p+width;
		pp2 = pp1+width;
		
		for(int row = 2; row < height_minus_2; row++) {
			pm2 = (row-2)*width;//&tmp[(row-2)*width];
			pm1 = pm2+width;
			p   = pm1+width;
			pp1 = p+width;
			pp2 = pp1+width;
			
			dst_ptr = row*width;//&dst[row*width];
			
			for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
				dst[dst_ptr] = (((tmp[p]<<1)+(tmp[p]<<2)) + ((tmp[pm1]+tmp[pp1])<<2) + (tmp[pm2]+tmp[pp2]))*(1.f/256.f);
			}
		}

		// Apply vertical filter for bottom border. Similar to top border.
		pm2     = (height-4)*width;//tmp+(height-4)*width;
		pm1     = pm2+width;
		p       = pm1+width;
		pp1     = p+width;
		pp2     = pp1;
		dst_ptr = (height-2)*width;//dst+(height-2)*width;

		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (((tmp[p]<<1)+(tmp[p]<<2)) + ((tmp[pm1]+tmp[pp1])<<2) + (tmp[pm2]+tmp[pp2]))*(1.f/256.f);
		}
		
		pm2     = (height-3)*width;//tmp+(height-3)*width;
		pm1     = pm2+width;
		p       = pm1+width;
		pp1     = p;
		pp2     = p;
		dst_ptr = (height-1)*width;//dst+(height-1)*width;
		
		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (((tmp[p]<<1)+(tmp[p]<<2)) + ((tmp[pm1]+tmp[pp1])<<2) + (tmp[pm2]+tmp[pp2]))*(1.f/256.f);
		}
    }
    private void binomial_4th_order(double[] dst,double[] tmp,double[] src,int width,int height)
    {
		int tmp_ptr=0;

		int width_minus_1, width_minus_2;
		int height_minus_2;
		
		assert(width >= 5);//), "Image is too small");
		assert(height >= 5);//, "Image is too small");
		
		width_minus_1 = width-1;
		width_minus_2 = width-2;
		height_minus_2 = height-2;
		
		
		// Apply horizontal filter
		for(int row = 0; row < height; row++)
		{
//			const unsigned char* src_ptr = &src[row*width];
			int src_ptr=row*width;
			
			// Left border is computed by extending the border pixel beyond the image

			tmp[tmp_ptr++] = 6.f*src[src_ptr+0] + 4.f*(src[src_ptr+0]+src[src_ptr+1]) + src[src_ptr+0] + src[src_ptr+2];
			tmp[tmp_ptr++] = 6.f*src[src_ptr+1] + 4.f*(src[src_ptr+0]+src[src_ptr+2]) + src[src_ptr+0] + src[src_ptr+3];
			
			// Compute non-border pixels
			for(int col = 2; col < width_minus_2; col++, tmp_ptr++) {
				tmp[tmp_ptr] = (6.f*src[src_ptr+col] + 4.f*(src[src_ptr+col-1]+src[src_ptr+col+1]) + src[src_ptr+col-2] + src[src_ptr+col+2]);
			}
		
			// Right border. Computed similarily as the left border.
			tmp[tmp_ptr++] = 6.f*src[src_ptr+width_minus_2] + 4.f*(src[src_ptr+width_minus_2-1]+src[src_ptr+width_minus_2+1]) + src[src_ptr+width_minus_2-2] + src[src_ptr+width_minus_2+1];
			tmp[tmp_ptr++] = 6.f*src[src_ptr+width_minus_1] + 4.f*(src[src_ptr+width_minus_1-1]+src[src_ptr+width_minus_1])   + src[src_ptr+width_minus_1-2] + src[src_ptr+width_minus_1];

		}
		
		int pm2;
		int pm1;
		int p;
		int pp1;
		int pp2;
		
		// Apply vertical filter along top border. This is applied twice as there are two
		// border pixels.
		pm2     = 0;//tmp
		pm1     = 0;//tmp
		p       = 0;//tmp
		pp1     = width;//p+width
		pp2     = pp1+width;//pp1+width		
		int dst_ptr = 0;
		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (6.f*tmp[p] + 4.f*(tmp[pm1]+tmp[pp1]) + (tmp[pm2]) + (tmp[pp2]))*(1.f/256.f);;
		}

		
		pm2     = 0;//tmp;
		pm1     = 0;//tmp;
		p       = width;//tmp+width;
		pp1     = p+width;
		pp2     = pp1+width;
		dst_ptr = width;
		
		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (6.f*(tmp[p]) + 4.f*(tmp[pm1]+tmp[pp1]) + (tmp[pm2]) + (tmp[pp2]))*(1.f/256.f);;
		}
		
		// Apply vertical filter for non-border pixels.
		pm2 = 0;//tmp;
		pm1 = pm2+width;
		p   = pm1+width;
		pp1 = p+width;
		pp2 = pp1+width;
		
		for(int row = 2; row < height_minus_2; row++) {
			pm2 = (row-2)*width;//&tmp[(row-2)*width];
			pm1 = pm2+width;
			p   = pm1+width;
			pp1 = p+width;
			pp2 = pp1+width;
			
			dst_ptr = row*width;//&dst[row*width];
			
			for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
				dst[dst_ptr] = (6.f*(tmp[p]) + 4.f*(tmp[pm1]+tmp[pp1]) + (tmp[pm2]) + (tmp[pp2]))*(1.f/256.f);;
			}
		}

		// Apply vertical filter for bottom border. Similar to top border.
		pm2     = (height-4)*width;//tmp+(height-4)*width;
		pm1     = pm2+width;
		p       = pm1+width;
		pp1     = p+width;
		pp2     = pp1;
		dst_ptr = (height-2)*width;//dst+(height-2)*width;

		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (6.f*(tmp[p]) + 4.f*(tmp[pm1]+tmp[pp1]) + (tmp[pm2]) + (tmp[pp2]))*(1.f/256.f);;
		}
		
		pm2     = (height-3)*width;//tmp+(height-3)*width;
		pm1     = pm2+width;
		p       = pm1+width;
		pp1     = p;
		pp2     = p;
		dst_ptr = (height-1)*width;//dst+(height-1)*width;
		
		for(int col = 0; col < width; col++, dst_ptr++, pm2++, pm1++, p++, pp1++, pp2++) {
			dst[dst_ptr] = (6.f*(tmp[p]) + 4.f*(tmp[pm1]+tmp[pp1]) + (tmp[pm2]) + (tmp[pp2]))*(1.f/256.f);;
		}
    }   
        
    private void downsample_bilinear(double[] dst,double[] src, int src_width, int src_height)
    {
        int src_ptr1;
        int src_ptr2;
        
        int dst_width = src_width>>1;
        int dst_height = src_height>>1;
        int dst_ptr=0;
        for(int row = 0; row < dst_height; row++) {
            src_ptr1 = (row<<1)*src_width;
            src_ptr2 = src_ptr1 + src_width;
            for(int col = 0; col < dst_width; col++, src_ptr1+=2, src_ptr2+=2) {
                dst[dst_ptr++] = (src[src_ptr1+0]+src[src_ptr1+1]+src[src_ptr2+0]+src[src_ptr2+1])*0.25f;
            }
        }
    }

    private void apply_filter(KpmImage dst, INyARGrayscaleRaster src)
    {
    	assert(src.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
        binomial_4th_order((double[])dst.getBuffer(),this.mTemp_us16,(int[])src.getBuffer(),src.getWidth(),src.getHeight());
    }    
    private void apply_filter(KpmImage dst, KpmImage src)
    {
        binomial_4th_order(
        	(double[])dst.getBuffer(),this.mTemp_f32_1,(double[])src.getBuffer(),src.getWidth(),src.getHeight());
    }    
    private void apply_filter_twice(KpmImage dst,KpmImage src)
    {
    	KpmImage tmp=new KpmImage(src.getWidth(),src.getHeight());
        apply_filter(tmp, src);
        apply_filter(dst, tmp);
    }   
    
    


}