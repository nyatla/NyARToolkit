package jp.nyatla.nyartoolkit.core.surfacetracking.featuremap;

import jp.nyatla.nyartoolkit.core.marker.nft.fset.NyARSurfaceFeatureMap;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;






public class NyARSurfaceFeatureMapUtils
{



	public static class SelectFeatureResult extends NyARObjectStack<SelectFeatureResult.Item>
	{
		public class Item{
			public int x;
			public int y;
			public double min_sim;
		}
		public SelectFeatureResult(int i_length)
		{
			super(i_length,Item.class);
		}
		/**
		 * この関数は、配列要素を作成します。
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
	 * @throws NyARException
	 */
	public static SelectFeatureResult ar2SelectFeature(
		INyARGrayscaleRaster i_raster,double i_dpi,NyARSurfaceFeatureMap i_feature_map,
        int ts1, int ts2, int search_size1, int i_occ_size,
        double  max_sim_thresh, double  min_sim_thresh, double  sd_thresh)
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
	
	
	
	
	
}

