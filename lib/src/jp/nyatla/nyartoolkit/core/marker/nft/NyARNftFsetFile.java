package jp.nyatla.nyartoolkit.core.marker.nft;




import java.io.File;
import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.marker.nft.fset.FsetFileDataParserV4;
import jp.nyatla.nyartoolkit.core.marker.nft.fset.NyARSurfaceFeatureMap;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceTracker;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARTemplatePatchImage;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;





public class NyARNftFsetFile
{
	public static class NyAR2FeatureCoord
	{
		public int                x;
		public int                y;
		public double             mx;
		public double             my;
		public double             maxSim;
		public static NyAR2FeatureCoord[] createArray(int i_num_of_coord)
		{
			NyAR2FeatureCoord[] r=new NyAR2FeatureCoord[i_num_of_coord];
			for(int i=0;i<i_num_of_coord;i++){
				r[i]=new NyAR2FeatureCoord();
			}
			return r;
		}
		public void setValue(NyAR2FeatureCoord i_val)
		{
			this.x=i_val.x;
			this.y=i_val.y;
			this.mx=i_val.mx;
			this.my=i_val.my;
			this.maxSim=i_val.maxSim;
		}
	};
	public static class NyAR2FeaturePoints
	{
		public static final int AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE1=10;
		public static final int AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE2=2;	
		public static final double AR2_DEFAULT_MAX_SIM_THRESH2=0.95;
		public static final double AR2_DEFAULT_SD_THRESH2=0.5;
		public NyAR2FeatureCoord[] coord;
		public int               scale;
		public double            maxdpi;
		public double            mindpi;
		public NyAR2FeaturePoints(int i_num_of_coord,int i_scale,double i_maxdpi,double i_mindpi)
		{
			this.scale=i_scale;
			this.maxdpi=i_maxdpi;
			this.mindpi=i_mindpi;
			this.coord=NyAR2FeatureCoord.createArray(i_num_of_coord);
		}
		public NyAR2FeaturePoints(NyARNftIsetFile.ReferenceImage i_refimg,int i_occ_size,double i_max_sim_thresh,double i_min_sim_thresh,double i_sd_th,double i_max_dpi,double i_min_dpi,int i_scale)
		{
			long s=System.currentTimeMillis();
			NyARSurfaceFeatureMap fmap=new NyARSurfaceFeatureMap(
				i_refimg,
				NyARSurfaceTracker.AR2_DEFAULT_TS1*NyARTemplatePatchImage.AR2_TEMP_SCALE,
				NyARSurfaceTracker.AR2_DEFAULT_TS2*NyARTemplatePatchImage.AR2_TEMP_SCALE,
				AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE1,AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE2,
				AR2_DEFAULT_MAX_SIM_THRESH2,AR2_DEFAULT_SD_THRESH2);
			System.out.println(System.currentTimeMillis()-s+"ms");
			this.coord=fmap.ar2SelectFeature(i_refimg.dpi, AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE2, i_occ_size, i_max_sim_thresh, i_min_sim_thresh, i_sd_th);

			this.maxdpi=i_max_dpi;
			this.mindpi=i_min_dpi;
			this.scale=i_scale;
			return;
		}
	};
	final public NyAR2FeaturePoints[] list;
	
	public static NyARNftFsetFile loadFromFsetFile(File i_src){
		return loadFromFsetFile(BinaryReader.toArray(i_src));
	}
	public static NyARNftFsetFile loadFromFsetFile(InputStream i_src){
		return loadFromFsetFile(BinaryReader.toArray(i_src));
	}
	public static NyARNftFsetFile loadFromFsetFile(byte[] i_src)
	{
		FsetFileDataParserV4 fsr=new FsetFileDataParserV4(i_src);
		return new NyARNftFsetFile(fsr.points);
	}	
	public NyARNftFsetFile(NyAR2FeaturePoints[] i_list)
	{
		this.list=i_list;
		return;
	}
	public static void main(String[] args){
		NyARNftFsetFile f=NyARNftFsetFile.loadFromFsetFile(new File("../Data/pinball.fset"));
		System.out.println(f);
		return;
	}
	
}






