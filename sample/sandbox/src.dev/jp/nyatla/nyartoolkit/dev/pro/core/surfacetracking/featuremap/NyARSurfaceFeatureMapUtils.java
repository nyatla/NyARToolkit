package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.featuremap;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.pixel.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.INyARDefocusFilter;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.NyARDefocusFilterFactory;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureSet;





public class NyARSurfaceFeatureMapUtils
{

	/**
	 * @param i_raster
	 * ARToolkitNftのar2GenFeatureMapと同じ結果を得るには、{@link INyARDefocusFilter}で1回フィルタをかけた画像を入力すること�?
	 * @param i_dpi
	 * @param ts1
	 * @param ts2
	 * @param search_size1
	 * @param search_size2
	 * @param max_sim_thresh
	 * @param sd_thresh
	 * @return
	 * @throws NyARRuntimeException
	 */
	public static NyARSurfaceFeatureMap ar2GenFeatureMap(
		INyARGrayscaleRaster i_raster,
		double i_dpi,
        int ts1, int ts2,
        int search_size1,
        double  max_sim_thresh, double  sd_thresh ) throws NyARRuntimeException
	{

		double           max;

		int xsize = i_raster.getWidth();
		int ysize = i_raster.getHeight();
		NyARSurfaceFeatureMap ret=new NyARSurfaceFeatureMap(xsize,ysize);

		INyARGsPixelDriver pxd=i_raster.getGsPixelDriver();

		double[] fimage2=new double[xsize*ysize];
	//	p = imgBW1;//ブラー1かいかけたやつっぽ�?
		
		int fp2 = 0;
		//fimegeに数値を設定�?�エ�?ジは-1
		for(int i = 0; i < xsize; i++ ) {
			fimage2[fp2++] = -1.0f;
		}
		for(int j = 1; j < ysize-1; j++ ) {
			fimage2[fp2++] = -1.0f;
			for(int i = 1; i < xsize-1; i++ )
			{
				double dx=(	pxd.getPixel(i+1,j-1)-pxd.getPixel(i-1,j-1)
						+	pxd.getPixel(i+1,j  )-pxd.getPixel(i-1,j  )
						+	pxd.getPixel(i+1,j+1)-pxd.getPixel(i-1,j+1))/(double)(3*256);
				double dy=(	pxd.getPixel(i+1,j+1)-pxd.getPixel(i+1,j-1)
						+	pxd.getPixel(i,  j+1)-pxd.getPixel(i,  j-1)
						+	pxd.getPixel(i-1,j+1)-pxd.getPixel(i-1,j-1))/(double)(3*256);
				
				fimage2[fp2++] = (double)Math.sqrt((dx*dx+dy*dy) / (double )2.0f );
			}
			fimage2[fp2++] = -1.0f;
		}
		for(int i = 0; i < xsize; i++ ) {
			fimage2[fp2++] = -1.0f;
		}
		int[] hist=new int[1000];
		for(int i = 0; i < hist.length; i++ ){
			hist[i] = 0;
		}
		fp2 = xsize + 1;//(1,1の位置)
		for(int j = 1; j < ysize-1; j++ ) {
			for(int i = 1; i < xsize-1; i++ ) {
				if( fimage2[fp2] > fimage2[fp2-1] && fimage2[fp2] > fimage2[fp2+1] && fimage2[fp2] > fimage2[fp2-xsize] && fimage2[fp2] > fimage2[fp2+xsize] ) {
					int k = (int)(fimage2[fp2] * 1000.0f);
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
			if( (double )sum_of_hist / (double )(xsize*ysize) >= 0.02f ){
				k2=i;
				break;
			}
		}	
		double[] fimage=ret.fimage;
		int fp = 0;
		fp2 = 0;
		for(int i = 0; i < xsize; i++ ) {
			fimage[fp++] = 1.0f;
			fp2++;
		}
		TemplateImage ti=new TemplateImage(ts1, ts2);
		for(int j = 1; j < ysize-1; j++ ) {
			fimage[fp++] = 1.0f;
			fp2++;
			for(int i = 1; i < xsize-1; i++ ) {
				if(fimage2[fp2] <= fimage2[fp2-1] || fimage2[fp2] <= fimage2[fp2+1] || fimage2[fp2] <= fimage2[fp2-xsize] || fimage2[fp2] <= fimage2[fp2+xsize] ) {
					fimage[fp++] = 1.0f;
					fp2++;
					continue;
				}
				if( (int)(fimage2[fp2] * 1000) < k2 ) {
					fimage[fp++] = 1.0f;
					fp2++;
					continue;
				}
				if(!ti.make_template(i_raster, i, j, sd_thresh)) {
					fimage[fp++] = 1.0f;
					fp2++;
					continue;
				}
				max = -1.0f;
				for(int jj = -search_size1; jj <= search_size1; jj++ ) {
					for(int ii = -search_size1; ii <= search_size1; ii++ ){
						//�?を作ってる�?��?
						if( ii*ii + jj*jj <= search_size1*search_size1 ){
							continue;
						}
						double sim=ti.get_similarity(i_raster,i+ii, j+jj);
		
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
				fimage[fp++] = (double)max;
				fp2++;
			}
			fimage[fp++] = 1.0f;
			fp2++;
		}
		for(int i = 0; i < xsize; i++ ) {
			fimage[fp++] = 1.0f;
			fp2++;
		}		
		return ret;
	}
	public static class SelectFeatureResult extends NyARObjectStack<SelectFeatureResult.Item>
	{
		public class Item{
			public int x;
			public int y;
			public double min_sim;
		}
		public SelectFeatureResult(int i_length) throws NyARRuntimeException
		{
			super.initInstance(i_length,Item.class);
		}
		/**
		 * こ�?�関数は�?配�?�要�?を作�?�します�??
		 */	
		protected Item createElement()
		{
			return new Item();
		}		
	}
	/**
	 * 
	 * @param i_raster
	 * @param i_dpi
	 * @param i_feature_map
	 * @param ts1
	 * @param ts2
	 * @param search_size1
	 * @param i_occ_size
	 * @param max_sim_thresh
	 * @param min_sim_thresh
	 * @param sd_thresh
	 * @return
	 * @throws NyARRuntimeException
	 */
	public static SelectFeatureResult ar2SelectFeature(
		INyARGrayscaleRaster i_raster,double i_dpi,NyARSurfaceFeatureMap i_feature_map,
        int ts1, int ts2, int search_size1, int i_occ_size,
        double  max_sim_thresh, double  min_sim_thresh, double  sd_thresh) throws NyARRuntimeException
	{
//		NyARSurfaceFeatureMap featureMap=ar2GenFeatureMap(i_raster, i_dpi, ts1, ts2, search_size1, max_sim_thresh, sd_thresh);
		
//		double              sim;
//		int                cx, cy;
		//int                i, j;
//		int                ii;
		assert(i_raster.getSize().isEqualSize(i_feature_map.width,i_feature_map.height));

		int xsize = i_feature_map.width;
		int ysize = i_feature_map.height;
	//arMalloc(x_template, double , (ts1+ts2+1)*(ts1+ts2+1));
	//arMalloc(fimage2, double, xsize*ysize);
		double[] fimage2=new double[xsize*ysize];
		System.arraycopy(i_feature_map.fimage,0, fimage2, 0, fimage2.length);
//			fp1 = featureMap->map;
//			fp2 = fimage2;
//			for( i = 0; i < xsize*ysize; i++ ) {
//				*(fp2++) = *(fp1++);
//			}

		int div_size = (ts1+ts2+1)*3;
		int xdiv = xsize/div_size;
		int ydiv = ysize/div_size;

		SelectFeatureResult ret=new SelectFeatureResult((xsize/i_occ_size)*(ysize/i_occ_size) + xdiv*ydiv);
//		int max_feature_num = ;
//			ARLOG("Max feature = %d\n", max_feature_num);
//		NyARSurfaceFeatureSet.NyAR2FeatureCoord[] coord=NyARSurfaceFeatureSet.NyAR2FeatureCoord.createArray(max_feature_num);
		
//			arMalloc( coord, AR2FeatureCoordT, max_feature_num );
//		int num=0;
		TemplateImage ti=new TemplateImage(ts1, ts2);
		for(;;){
		
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
			if(!ti.make_template(i_raster,cx,cy,  0.0f)) {
//			if( make_template( image2->imgBW1, xsize, ysize, cx, cy, ts1, ts2, 0.0f, x_template, &vlen ) < 0 ) {
				fimage2[cy*xsize+cx] = 1.0f;
				continue;
			}
			if(ti.vlen/(ts1+ts2+1) < sd_thresh ) {
				fimage2[cy*xsize+cx] = 1.0f;
				continue;
			}

			double min = 1.0f;
			double max = -1.0f;
			for(int j = -search_size1; j <= search_size1; j++ ) {
				for(int i = -search_size1; i <= search_size1; i++ ) {
					if( i*i + j*j > search_size1*search_size1 ){
						continue;
					}
					if( i == 0 && j == 0 ){
						continue;
					}
					double sim=ti.get_similarity(i_raster,cx+i, cy+j);
					if(sim<0){
						continue;
					}
//					if(ti.get_similarity(i_raster,cx+i, cy+j) < 0 ){
//						continue;
//					}
	
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

			SelectFeatureResult.Item item=ret.prePush();
			if(item==null){
				break;
			}
			item.x = cx;
			item.y = cy;
//			coord[num].mx = (double) cx          / i_dpi * 25.4f;
//			coord[num].my = (double)(ysize - cy) / i_dpi * 25.4f;
			item.min_sim = min_sim;
//			num++;
		
//				ARLOG("%3d: (%3d,%3d) : %f min=%f max=%f, sd=%f\n", *num, cx, cy, min_sim, min, max, vlen/(ts1+ts2+1));
			for(int j = -i_occ_size; j <= i_occ_size; j++ ) {
				for(int i = -i_occ_size; i <= i_occ_size; i++ ) {
					if( cy+j < 0 || cy+j >= ysize || cx+i < 0 || cx+i >= xsize ){
						continue;
					}
					fimage2[(cy+j)*xsize+(cx+i)] = 1.0f;
				}
			}
		}


	//	fp1 = featureMap->map;
	//	fp2 = fimage2;
/*
		System.arraycopy(featureMap.fimage,0, fimage2, 0, fimage2.length);
		for( ii = 0; ii < num; ii++ ) {
			cx = coord[ii].x;
			cy = coord[ii].y;
			for(int j = -occ_size; j <= occ_size; j++ ) {
				for(int i = -occ_size; i <= occ_size; i++ ) {
					if( cy+j < 0 || cy+j >= ysize || cx+i < 0 || cx+i >= xsize ){
						continue;
					}
					fimage2[(cy+j)*xsize+(cx+i)] = 1.0f;
				}
			}
		}
*/	
//		//resize
//		NyARSurfaceFeatureSet.NyAR2FeatureCoord[] ret=NyARSurfaceFeatureSet.NyAR2FeatureCoord.createArray(num);
//		for(int i=0;i<num;i++){
//			ret[i].setValue(coord[i]);
//		}
		return ret;
	}	
	
	
	
	
	/**
	 * �?時テンプレート�??{@link NyARTemplatePatchImage}と同じ�?けど制度がdouble
	 */
	private static class TemplateImage
	{
		private int _ts1;
		private int _ts2;
		private double[] img;
		public double vlen;
		public TemplateImage(int i_ts1,int i_ts2)
		{
			this._ts1=i_ts1;
			this._ts2=i_ts2;
			this.img=new double[(i_ts1+1+i_ts2)*(i_ts1+1+i_ts2)];
		}
		public boolean make_template(INyARGrayscaleRaster imageBW,
            int cx, int cy,double  sd_thresh) throws NyARRuntimeException
		{
			int      i, j;
			int ts1=this._ts1;
			int ts2=this._ts2;
			int xsize=imageBW.getWidth();
			int ysize=imageBW.getHeight();
			INyARGsPixelDriver  pxd=imageBW.getGsPixelDriver();
			if( cy - ts1 < 0 || cy + ts2 >= ysize || cx - ts1 < 0 || cx + ts2 >= xsize ){
				return false;
			}
		
			double ave = 0.0f;
			for( j = -ts1; j <= ts2; j++ ) {
	//			int ip = (cy+j)*xsize+(cx-ts1);
				for( i = -ts1; i <= ts2 ; i++ ){
					//ave += *(ip++);
					ave+=pxd.getPixel(cx+i,cy+j);
				}
			}
			ave /= (ts1+ts2+1)*(ts1+ts2+1);
		
			int tp = 0;
			double vlen1 = 0.0f;
			for( j = -ts1; j <= ts2; j++ ) {
	//			ip = &imageBW[(cy+j)*xsize+(cx-ts1)];
				for( i = -ts1; i <= ts2 ; i++ ) {
					this.img[tp] = (double)(pxd.getPixel(cx+i,cy+j)) - ave;
					vlen1 += this.img[tp] * this.img[tp];
					tp++;
				}
			}
			if( vlen1 == 0.0f ){
				return false;
			}
			if( vlen1/((ts1+ts2+1)*(ts1+ts2+1)) < sd_thresh*sd_thresh ){
				return false;
			}
			this.vlen = Math.sqrt(vlen1);
			return true;
		}
		public double get_similarity(INyARGrayscaleRaster imageBW,int cx, int cy) throws NyARRuntimeException
		{
//		    int       i, j;
		    int ts1=this._ts1;
		    int ts2=this._ts2;
			int xsize=imageBW.getWidth();
			int ysize=imageBW.getHeight();
		    INyARGsPixelDriver  pxd=imageBW.getGsPixelDriver();
		    if( cy - ts1 < 0 || cy + ts2 >= ysize || cx - ts1 < 0 || cx + ts2 >= xsize ) return -1;

		    double ave = 0.0f;
		    for(int j = -ts1; j <= ts2; j++ ) {
//		        ip = &imageBW[(cy+j)*xsize+(cx-ts1)];
		        for(int i = -ts1; i <= ts2 ; i++ ){
		        	ave += pxd.getPixel(cx+i,cy+j);
		        }
		    }
		    ave /= (ts1+ts2+1)*(ts1+ts2+1);

		    int tp = 0;
		    double w1 = 0.0f;
		    double vlen2 = 0.0f;
		    for(int j = -ts1; j <= ts2; j++ ) {
//		        ip = &imageBW[(cy+j)*xsize+(cx-ts1)];
		        for(int i = -ts1; i <= ts2 ; i++ ) {
		            double w2 = (double )(pxd.getPixel(cx+i,cy+j)) - ave;
		            vlen2 += w2 * w2;
//		            w1 += *(tp++) * w2;
		            w1 += this.img[tp++]* w2;
		        }
		    }
//		    if( vlen2 == 0.0f ){
//		    	return -1;
//		    }

		    vlen2 = Math.sqrt(vlen2);
		    return w1 / (this.vlen * vlen2);
		}
	}
}

