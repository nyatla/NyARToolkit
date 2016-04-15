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
package jp.nyatla.nyartoolkit.core.marker.nft;






import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.marker.nft.fset.FsetFileDataParserV4;
import jp.nyatla.nyartoolkit.core.marker.nft.fset.NyARSurfaceFeatureMap;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.rgb2gs.INyARRgb2GsFilterRgbAve;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceTracker;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARTemplatePatchImage;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
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
		public NyAR2FeaturePoints(NyARNftIsetFile.ReferenceImage i_refimg,int i_occ_size,double i_max_sim_thresh,double i_min_sim_thresh,double i_sd_th,double i_max_dpi,double i_min_dpi,int i_scale) throws InterruptedException
		{

			NyARSurfaceFeatureMap fmap=new NyARSurfaceFeatureMap(
				i_refimg,
				NyARSurfaceTracker.AR2_DEFAULT_TS1*NyARTemplatePatchImage.AR2_TEMP_SCALE,
				NyARSurfaceTracker.AR2_DEFAULT_TS2*NyARTemplatePatchImage.AR2_TEMP_SCALE,
				AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE1,AR2_DEFAULT_GEN_FEATURE_MAP_SEARCH_SIZE2,
				AR2_DEFAULT_MAX_SIM_THRESH2,AR2_DEFAULT_SD_THRESH2);
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
	
	final public static double AR2_DEFAULT_MAX_SIM_THRESH_L0=0.80;
	final public static double AR2_DEFAULT_MAX_SIM_THRESH_L1=0.85;
	final public static double AR2_DEFAULT_MAX_SIM_THRESH_L2=0.90;
	final public static double AR2_DEFAULT_MAX_SIM_THRESH_L3=0.98;
	final public static double AR2_DEFAULT_MIN_SIM_THRESH_L0=0.70;
	final public static double AR2_DEFAULT_MIN_SIM_THRESH_L1=0.65;
	final public static double AR2_DEFAULT_MIN_SIM_THRESH_L2=0.55;
	final public static double AR2_DEFAULT_MIN_SIM_THRESH_L3=0.45;
	final public static double AR2_DEFAULT_SD_THRESH_L0=12.0;
	final public static double AR2_DEFAULT_SD_THRESH_L1=10.0;
	final public static double AR2_DEFAULT_SD_THRESH_L2=8.0;
	final public static double AR2_DEFAULT_SD_THRESH_L3=6.0;
	final private static int AR2_DEFAULT_OCCUPANCY_SIZE=24;	
	public static NyARNftFsetFile genFeatureSet(NyARNftIsetFile i_iset_file,int i_occupancy_size,double i_max_sim_th,double i_min_sim_th,double i_sd_th) throws InterruptedException
	{
		NyARNftIsetFile.ReferenceImage[] items=i_iset_file.items;
		NyARNftFsetFile.NyAR2FeaturePoints[] points=new NyARNftFsetFile.NyAR2FeaturePoints[items.length];
		for(int i=0;i<items.length;i++){
			NyARNftIsetFile.ReferenceImage rimg=items[i];
			//MAX-dpiとMIN-dpiを計算
			double max_dpi=Double.MAX_VALUE;
			double min_dpi=rimg.dpi*0.5;
			for(int j=0;j<items.length;j++){
				if(rimg.dpi<items[j].dpi && max_dpi>items[j].dpi){
					max_dpi=items[j].dpi;
				}
				if(rimg.dpi>items[j].dpi && min_dpi<items[j].dpi){
					min_dpi=items[j].dpi;
				}
			}
			if(max_dpi==Double.MAX_VALUE){
				max_dpi=rimg.dpi*2;
			}else{
				max_dpi=rimg.dpi*0.8+max_dpi*0.2;
			}
			//MIN-dpiの計算(一番近い小さい値
			points[i]=new NyARNftFsetFile.NyAR2FeaturePoints(rimg,i_occupancy_size,i_max_sim_th,i_min_sim_th,i_sd_th,max_dpi,min_dpi,i);
		}
		return new NyARNftFsetFile(points);
	}
	public static NyARNftFsetFile genFeatureSet(NyARNftIsetFile i_iset_file,int i_level) throws InterruptedException
	{
		final int[] occs={
			AR2_DEFAULT_OCCUPANCY_SIZE,
			AR2_DEFAULT_OCCUPANCY_SIZE,
			AR2_DEFAULT_OCCUPANCY_SIZE*2/3,
			AR2_DEFAULT_OCCUPANCY_SIZE*2/3,
			AR2_DEFAULT_OCCUPANCY_SIZE*1/2};
		double[][] data={
				{AR2_DEFAULT_SD_THRESH_L0,AR2_DEFAULT_MIN_SIM_THRESH_L0,AR2_DEFAULT_MAX_SIM_THRESH_L0},
				{AR2_DEFAULT_SD_THRESH_L1,AR2_DEFAULT_MIN_SIM_THRESH_L1,AR2_DEFAULT_MAX_SIM_THRESH_L1},
				{AR2_DEFAULT_SD_THRESH_L2,AR2_DEFAULT_MIN_SIM_THRESH_L2,AR2_DEFAULT_MAX_SIM_THRESH_L2},
				{AR2_DEFAULT_SD_THRESH_L3,AR2_DEFAULT_MIN_SIM_THRESH_L3,AR2_DEFAULT_MAX_SIM_THRESH_L3},
				{AR2_DEFAULT_SD_THRESH_L3,AR2_DEFAULT_MIN_SIM_THRESH_L3,AR2_DEFAULT_MAX_SIM_THRESH_L3}
		};
		return genFeatureSet(i_iset_file,occs[i_level],data[i_level][2],data[i_level][1],data[i_level][0]);
	}
	public static NyARNftFsetFile genFeatureSet(NyARNftIsetFile i_iset_file) throws InterruptedException
	{
		return genFeatureSet(i_iset_file, 2);
	}	
	
	
	
	
	public NyARNftFsetFile(NyAR2FeaturePoints[] i_list)
	{
		this.list=i_list;
		return;
	}

	/**
	 * 現在のファイルイメージをbyte[]で返却します。
	 * @return
	 */
	public byte[] makeFsetBinary()
	{
		FsetFileDataParserV4 ffp=new FsetFileDataParserV4(this.list);
		return ffp.makeBinary();
	}	
	
	public static void main(String[] args){
		INyARRgbRaster rgb;
//		rgb = NyARBufferedImageRaster.loadFromFile("d:\\sample.jpg");
		{
			rgb=NyARRgbRaster.createInstance(640,480,NyARBufferType.BYTE1D_B8G8R8_24);
			FileInputStream fs;
			try {
				fs = new FileInputStream("../Data/testcase/gensrc.raw");
				fs.read((byte[])rgb.getBuffer());				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
		//create same size grayscale raster
		INyARGrayscaleRaster gs=NyARGrayscaleRaster.createInstance(rgb.getSize());
		((INyARRgb2GsFilterRgbAve)rgb.createInterface(INyARRgb2GsFilterRgbAve.class)).convert(gs);


		NyARNftIsetFile iset=NyARNftIsetFile.genImageSet(gs,96);		
		try {
			NyARNftFsetFile f3=NyARNftFsetFile.genFeatureSet(iset);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		NyARNftFsetFile f=NyARNftFsetFile.loadFromFsetFile(new File("../Data/pinball.fset"));
		NyARNftFsetFile f2=NyARNftFsetFile.loadFromFsetFile(f.makeFsetBinary());
		
		System.out.println(f);
		return;
	}
	
}






