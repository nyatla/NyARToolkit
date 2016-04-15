package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import java.io.InputStream;
import java.nio.ByteOrder;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.j2se.ByteBufferedInputStream;

public class NyARSurfaceFeatureSet
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
	};	
	public NyAR2FeaturePoints[] list;
	public NyARSurfaceFeatureSet(NyAR2FeaturePoints[] i_list)
	{
		this.list=i_list;
		return;
	}
	public static NyARSurfaceFeatureSet loadFromFsetFile(InputStream i_stream) throws NyARRuntimeException
	{
		FsetReader fsr=new FsetReader(i_stream);
		int num=fsr.getNumberofPoints();
		NyAR2FeaturePoints[] ret=new NyAR2FeaturePoints[num];
		for(int i=0;i<num;i++){
			ret[i]=fsr.getNyAR2FeaturePointsT();
		}
		return new NyARSurfaceFeatureSet(ret);
	}
};









class FsetReader extends ByteBufferedInputStream
{
	public FsetReader(InputStream i_stream)
	{
		super(i_stream,512);
		this.order(ENDIAN_LITTLE);
	}
	public int getNumberofPoints() throws NyARRuntimeException
	{
		this.readToBuffer(4);
		return this.getInt();
	}
	public NyARSurfaceFeatureSet.NyAR2FeaturePoints getNyAR2FeaturePointsT() throws NyARRuntimeException
	{
		this.readToBuffer(4+4*2+4);
		int scale=this.getInt();
		double maxdpi=this.getFloat();
		double mindpi=this.getFloat();
		int num_of_coord=this.getInt();
		
		NyARSurfaceFeatureSet.NyAR2FeaturePoints ret=new NyARSurfaceFeatureSet.NyAR2FeaturePoints(num_of_coord,scale,maxdpi,mindpi);
//		ret.maxdpi=maxdpi;
//		ret.mindpi=mindpi;
//		ret.scale=scale;
		for(int i=0;i<num_of_coord;i++){
			this.readToBuffer(2*4+3*4);
			ret.coord[i].x=this.getInt();
			ret.coord[i].y=this.getInt();
			ret.coord[i].mx=this.getFloat();
			ret.coord[i].my=this.getFloat();
			ret.coord[i].maxSim=this.getFloat();
		}
		return ret;
	}
	
}
