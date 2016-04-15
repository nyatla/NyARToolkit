/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.marker.nft.fset;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

public class NyARSurfaceFeatureMap {
	final public double[] fimage;
	final private TemplateSourceRaster _refimage;
	final private TemplateImage_O1 _tmpimg;
	/**
	 * @param i_raster
	 * @param ts1
	 * @param ts2
	 * @param search_size1
	 * @param search_size2
	 * @param max_sim_thresh
	 * @param sd_thresh
	 * @return
	 * @throws InterruptedException 
	 */
	public NyARSurfaceFeatureMap(NyARNftIsetFile.ReferenceImage i_refimage,int ts1, int ts2,int search_size1,int search_size2,double  max_sim_thresh, double  sd_thresh ) throws InterruptedException
	{
		int w=i_refimage.width;
		int h=i_refimage.height;
		//アクセス用のテンポラリラスタ
		TemplateSourceRaster refraster=new TemplateSourceRaster(w,h,i_refimage.img);

		this._refimage=refraster;
		this.fimage=new double[w*h];
		this._tmpimg=new TemplateImage_O1(ts1, ts2);
		double max;

		double[] tmp_fimg2=new double[w*h];
		
		int fp2 = 0;
		//fimegeに数値を設定。エッジは-1
		for(int i = 0; i < w; i++ ) {
			tmp_fimg2[fp2++] = -1.0f;
		}
		for(int j = 1; j < h-1; j++ ) {
			tmp_fimg2[fp2++] = -1.0f;
			for(int i = 1; i < w-1; i++ )
			{
				double dx=(	refraster.getPixel(i+1,j-1)-refraster.getPixel(i-1,j-1)
						+	refraster.getPixel(i+1,j  )-refraster.getPixel(i-1,j  )
						+	refraster.getPixel(i+1,j+1)-refraster.getPixel(i-1,j+1))/(double)(3*256);
				double dy=(	refraster.getPixel(i+1,j+1)-refraster.getPixel(i+1,j-1)
						+	refraster.getPixel(i,  j+1)-refraster.getPixel(i,  j-1)
						+	refraster.getPixel(i-1,j+1)-refraster.getPixel(i-1,j-1))/(double)(3*256);
				
				tmp_fimg2[fp2++] = (double)Math.sqrt((dx*dx+dy*dy) / (double )2.0f );
			}
			tmp_fimg2[fp2++] = -1.0f;
		}
		for(int i = 0; i < w; i++ ) {
			tmp_fimg2[fp2++] = -1.0f;
		}
		int[] hist=new int[1000];
		for(int i = 0; i < hist.length; i++ ){
			hist[i] = 0;
		}
		fp2 = w + 1;//(1,1の位置)
		for(int j = 1; j < h-1; j++ ) {
			for(int i = 1; i < w-1; i++ ) {
				if( tmp_fimg2[fp2] > tmp_fimg2[fp2-1] && tmp_fimg2[fp2] > tmp_fimg2[fp2+1] && tmp_fimg2[fp2] > tmp_fimg2[fp2-w] && tmp_fimg2[fp2] > tmp_fimg2[fp2+w] ) {
					int k = (int)(tmp_fimg2[fp2] * 1000.0f);
					if( k > 999 ){
						k = 999;
					}
					if( k < 0 ){
						k = 0;
					}
					hist[k]++;
				}
				fp2++;
			}
			fp2 += 2;
		}
		int sum_of_hist = 0;
		int k2=0;
		for(int i = 999; i >= 0; i-- ) {
			sum_of_hist += hist[i];
			if( (double )sum_of_hist / (double )(w*h) >= 0.02f ){
				k2=i;
				break;
			}
		}	
		double[] fimg=this.fimage;
		int fp = 0;
		fp2 = 0;
		for(int i = 0; i < w; i++ ) {
			fimg[fp++] = 1.0f;
			fp2++;
		}
		TemplateImage_O1 ti=this._tmpimg;
		for(int j = 1; j < h-1; j++ ) {
			//長時間かかるループなので割り込み監視はブレーク可能。
			if(Thread.interrupted()){
				throw new InterruptedException();
			}
			fimg[fp++] = 1.0f;
			fp2++;
			for(int i = 1; i < w-1; i++ ) {
				if(tmp_fimg2[fp2] <= tmp_fimg2[fp2-1] || tmp_fimg2[fp2] <= tmp_fimg2[fp2+1] || tmp_fimg2[fp2] <= tmp_fimg2[fp2-w] || tmp_fimg2[fp2] <= tmp_fimg2[fp2+w] ) {
					fimg[fp++] = 1.0f;
					fp2++;
					continue;
				}
				if( (int)(tmp_fimg2[fp2] * 1000) < k2 ) {
					fimg[fp++] = 1.0f;
					fp2++;
					continue;
				}
				if(!ti.make_template(refraster, i, j, sd_thresh)) {
					fimg[fp++] = 1.0f;
					fp2++;
					continue;
				}
				max = -1.0f;
				for(int jj = -search_size1; jj <= search_size1; jj++ ) {
					for(int ii = -search_size1; ii <= search_size1; ii++ ){
						//円を作ってるね。
						if( ii*ii + jj*jj <= search_size2*search_size2 ){
							continue;
						}
						double sim=ti.get_similarity(refraster,i+ii, j+jj);
		
						if( sim > max ) {
							max = sim;
							if( max > max_sim_thresh ){
								break;
							}
						}
					}
					if( max > max_sim_thresh ){
						break;
					}
				}
				fimg[fp++] = (double)max;
				fp2++;
			}
			fimg[fp++] = 1.0f;
			fp2++;
		}
		for(int i = 0; i < w; i++ ) {
			fimg[fp++] = 1.0f;
			fp2++;
		}
		return;
	}
	/**
	 * 
	 * @param i_dpi
	 * @param ts1
	 * @param ts2
	 * @param search_size2
	 * @param i_occ_size
	 * @param max_sim_thresh
	 * @param min_sim_thresh
	 * @param sd_thresh
	 * @return
	 * @throws InterruptedException 
	 * @throws NyARException
	 */
	public NyARNftFsetFile.NyAR2FeatureCoord[] ar2SelectFeature(double i_dpi, int search_size2, int i_occ_size,double max_sim_thresh, double  min_sim_thresh, double sd_thresh) throws InterruptedException
	{

		i_occ_size*=2;

		int xsize = this._refimage.getWidth();
		int ysize = this._refimage.getHeight();
		double[] fimage2=new double[xsize*ysize];
		System.arraycopy(this.fimage,0, fimage2, 0, fimage2.length);
		TemplateImage_O1 ti=this._tmpimg;

		int div_size = (ti._ts1+ti._ts2+1)*3;
		int xdiv = xsize/div_size;
		int ydiv = ysize/div_size;

		//特徴点の最大数で配列を生成		
		NyARNftFsetFile.NyAR2FeatureCoord[] ret=NyARNftFsetFile.NyAR2FeatureCoord.createArray((xsize/i_occ_size)*(ysize/i_occ_size) + xdiv*ydiv);
		int num_of_ret=0;

		for(;;){
			//長時間かかるループなので割り込み監視はブレーク可能。
			if(Thread.interrupted()){
				throw new InterruptedException();
			}			
			double min_sim = max_sim_thresh;
			int fp2 = 0;
			int cx=-1;
			int cy=-1;
			cx = cy = -1;
			for(int j = 0; j < ysize; j++ ) {
				for(int i = 0; i < xsize; i++ ) {
					if(fimage2[fp2] < min_sim ) {
						min_sim = fimage2[fp2];
						cx = i;
						cy = j;
					}
					fp2++;
				}
			}
			if( cx == -1 ){
				break;
			}
			if(!ti.make_template(this._refimage,cx,cy,  0.0f)) {
				fimage2[cy*xsize+cx] = 1.0f;
				continue;
			}
			if(ti.vlen/(ti._ts1+ti._ts2+1) < sd_thresh ) {
				fimage2[cy*xsize+cx] = 1.0f;
				continue;
			}

			double min = 1.0f;
			double max = -1.0f;
			for(int j = -search_size2; j <= search_size2; j++ ) {
				for(int i = -search_size2; i <= search_size2; i++ ) {
					if( i*i + j*j > search_size2*search_size2 ){
						continue;
					}
					if( i == 0 && j == 0 ){
						continue;
					}
					double sim=ti.get_similarity(this._refimage,cx+i, cy+j);
					if(sim<0){
						continue;
					}
	
					if( sim < min ) {
						min = sim;
						if( min < min_sim_thresh && min < min_sim ){
							break;
						}
					}
					if( sim > max ) {
						max = sim;
						if( max > 0.99 ){
							break;
						}
					}
				}
				if( (min < min_sim_thresh && min < min_sim) || max > 0.99f ){
					break;
				}
			}
					
			if( (min < min_sim_thresh && min < min_sim) || max > 0.99f ) {
				fimage2[cy*xsize+cx] = 1.0f;
				continue;
			}

			NyARNftFsetFile.NyAR2FeatureCoord p=ret[num_of_ret];
			p.x=cx;
			p.y=cy;
			p.maxSim=min_sim;
	        p.mx = (float) cx          / i_dpi * 25.4f;
	        p.my = (float)(ysize - cy) / i_dpi * 25.4f;
//	        System.out.println(String.format("%3d: (%3d,%3d) : %f min=%f max=%f",num_of_ret, cx, cy, min_sim, min, max));
			num_of_ret++;
	        
			for(int j = -i_occ_size; j <= i_occ_size; j++ ) {
				for(int i = -i_occ_size; i <= i_occ_size; i++ ) {
					if( cy+j < 0 || cy+j >= ysize || cx+i < 0 || cx+i >= xsize ){
						continue;
					}
					fimage2[(cy+j)*xsize+(cx+i)] = 1.0f;
				}
			}
		}
		//Resize
		NyARNftFsetFile.NyAR2FeatureCoord[] resize_ret=new NyARNftFsetFile.NyAR2FeatureCoord[num_of_ret];
		System.arraycopy(ret,0, resize_ret, 0,num_of_ret);
		return resize_ret;
	}	
}

class TemplateImage_O1 {
	public final int _ts1;
	public final int _ts2;
	private double[] img;
	public double vlen;

	public TemplateImage_O1(int i_ts1, int i_ts2) {
		this._ts1 = i_ts1;
		this._ts2 = i_ts2;
		this.img = new double[(i_ts1 + 1 + i_ts2) * (i_ts1 + 1 + i_ts2)];
	}

	public boolean make_template(INyARGrayscaleRaster imageBW, int cx, int cy, double sd_thresh) 
	{

		int ts1 = this._ts1;
		int ts2 = this._ts2;
		int xsize = imageBW.getWidth();
		int ysize = imageBW.getHeight();
		if (cy - ts1 < 0 || cy + ts2 >= ysize || cx - ts1 < 0 || cx + ts2 >= xsize) {
			return false;
		}
		double ave = 0.0f;
		for (int j = -ts1; j <= ts2; j++) {
			// int ip = (cy+j)*xsize+(cx-ts1);
			for (int i = -ts1; i <= ts2; i++) {
				// ave += *(ip++);
				ave += imageBW.getPixel(cx + i, cy + j);
			}
		}
		ave /= (ts1 + ts2 + 1) * (ts1 + ts2 + 1);

		int tp = 0;
		double vlen1 = 0.0f;
		for (int j = -ts1; j <= ts2; j++) {
			for (int i = -ts1; i <= ts2; i++) {
				double p=(imageBW.getPixel(cx + i, cy + j)) - ave;
				this.img[tp]=p;
				vlen1 += p * p;
				tp++;
			}
		}
		if (vlen1 == 0.0f) {
			return false;
		}
		if (vlen1 / ((ts1 + ts2 + 1) * (ts1 + ts2 + 1)) < sd_thresh * sd_thresh) {
			return false;
		}
		this.vlen = Math.sqrt(vlen1);
		return true;
	}

	public double get_similarity(INyARGrayscaleRaster imageBW, int cx, int cy) {
		int ts1 = this._ts1;
		int ts2 = this._ts2;
		int xsize = imageBW.getWidth();
		int ysize = imageBW.getHeight();
		if (cy - ts1 < 0 || cy + ts2 >= ysize || cx - ts1 < 0 || cx + ts2 >= xsize)
			return -1;
		int tp = 0;
		double p_sum = 0.0f;
		double pxp_sum=0;
		double img_sum=0;
		double img_p_sum=0;
		for (int j = -ts1; j <= ts2; j++) {
			for (int i = -ts1; i <= ts2; i++) {
				int p=(imageBW.getPixel(cx + i, cy + j));
				double t=this.img[tp++];
				pxp_sum+=p*p;
				p_sum += p;
				img_sum+=t;
				img_p_sum+=t*p;
			}
		}
		double ave=p_sum/((ts1 + ts2 + 1) * (ts1 + ts2 + 1));
		double w1=img_p_sum-img_sum*ave;
		double vlen2=Math.sqrt((pxp_sum-2*p_sum*ave)+(ave*ave*(ts1 + ts2 + 1) * (ts1 + ts2 + 1)));
		return w1 / (this.vlen * vlen2);
	}
}