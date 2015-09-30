package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.INyARDefocusFilter;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.NyARDefocusFilterFactory;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.featuremap.NyARSurfaceFeatureMap;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.featuremap.NyARSurfaceFeatureMapUtils;



public class NyARSurfaceFeatureSetUtils
{
	public final static int AR2_DEFAULT_TS1=11;
	public final static int AR2_DEFAULT_TS2=11;
	public final static int AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE2=2;
	public final static int AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE1=10;
	public final static int AR2_DEFALUT_OCCUPANCY_SIZE=24;
	public final static double AR2_DEFAULT_MAX_SIM_THRESH2=0.95;
	public final static double AR2_DEFAULT_MIN_SIM_THRESH2=0.40;
	public final static double AR2_DEFAULT_SD_THRESH2=5.0;
	public static NyARSurfaceFeatureSet makeSurfaceFeature(INyARGrayscaleRaster[] i_rasters,double[] i_dpis) throws NyARRuntimeException
    {
		return NyARSurfaceFeatureSetUtils.makeSurfaceFeature(i_rasters,i_dpis,		
				NyARSurfaceFeatureSetUtils.AR2_DEFAULT_TS1,NyARSurfaceFeatureSetUtils.AR2_DEFAULT_TS2,
				NyARSurfaceFeatureSetUtils.AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE1, NyARSurfaceFeatureSetUtils.AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE2,
				NyARSurfaceFeatureSetUtils.AR2_DEFALUT_OCCUPANCY_SIZE, NyARSurfaceFeatureSetUtils.AR2_DEFAULT_MAX_SIM_THRESH2,
				NyARSurfaceFeatureSetUtils.AR2_DEFAULT_MIN_SIM_THRESH2, NyARSurfaceFeatureSetUtils.AR2_DEFAULT_SD_THRESH2);		

    }
	public static NyARSurfaceFeatureSet makeSurfaceFeature(INyARGrayscaleRaster[] i_raster,double[] i_dpi,
		int i_ts1,int i_ts2,int i_map_search_size,int i_select_map_size,int i_occ_size,
	    double  max_sim_thresh, double  min_sim_thresh, double  sd_thresh) throws NyARRuntimeException
	{
		assert(i_raster.length==i_dpi.length);
		NyARSurfaceFeatureSet.NyAR2FeaturePoints[] list=new NyARSurfaceFeatureSet.NyAR2FeaturePoints[i_raster.length];
		for(int i2=0;i2<i_raster.length;i2++){
			//defocus 1
			INyARGrayscaleRaster src=i_raster[i2];
			INyARGrayscaleRaster imgBW1=new NyARGrayscaleRaster(src.getWidth(),src.getHeight());
			INyARDefocusFilter filter=NyARDefocusFilterFactory.createDriver(src);
			filter.doFilter(imgBW1,1);			
			//MAPの生�??
			NyARSurfaceFeatureMap map=NyARSurfaceFeatureMapUtils.ar2GenFeatureMap(imgBW1, i_dpi[i2], i_ts1, i_ts2, i_map_search_size, max_sim_thresh, sd_thresh);
			//キーの選�?
			NyARSurfaceFeatureMapUtils.SelectFeatureResult selected=NyARSurfaceFeatureMapUtils.ar2SelectFeature(imgBW1, i_dpi[i2],map,i_ts1, i_ts2,i_select_map_size,i_occ_size,max_sim_thresh,min_sim_thresh,sd_thresh);
			//max_dpiとmin_dpiの決�?
	        double scale1 = 0.0;
	        for(int j = 0; j < i_dpi.length; j++ ) {
	            if(i_dpi[j] < i_dpi[i2] ) {
	                if(i_dpi[j] > scale1 ){
	                	scale1 = i_dpi[j];
	                }
	            }
	        }
	        double mindpi;
	        if(scale1 == 0.0){
	            mindpi = i_dpi[i2] * 0.5;
	        }else {
	            mindpi = scale1;
	        }
	        scale1 = 0.0;
	        for(int j = 0; j < i_dpi.length; j++ ) {
	            if(i_dpi[j] > i_dpi[i2]) {
	                if( scale1 == 0.0 || i_dpi[j]< scale1 ){
	                	scale1 = i_dpi[j];
	                }
	            }
	        }
	        double maxdpi;
	        if( scale1 == 0.0 ) {
	            maxdpi = i_dpi[i2] * 2.0;
	        }else {
	            double scale2 = i_dpi[i2];
	           maxdpi = scale2*0.8 + scale1*0.2;
	        }			
			//�?ータ生�??
	        list[i2]=new NyARSurfaceFeatureSet.NyAR2FeaturePoints(selected.getLength(),i2,maxdpi,mindpi);
			NyARSurfaceFeatureSet.NyAR2FeaturePoints list_item=list[i2];
			int ysize=imgBW1.getHeight();
			for(int i=0;i<list_item.coord.length;i++){
				NyARSurfaceFeatureMapUtils.SelectFeatureResult.Item item=selected.getItem(i);
				list_item.coord[i].maxSim=item.min_sim;
				list_item.coord[i].mx=item.x/ i_dpi[i2] * 25.4f;
				list_item.coord[i].my=(ysize-item.y)/ i_dpi[i2] * 25.4f;
				list_item.coord[i].x=item.x;
				list_item.coord[i].y=item.y;
			}
		}
		return new NyARSurfaceFeatureSet(list);
	}
}
