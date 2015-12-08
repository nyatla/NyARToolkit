package jp.nyatla.nyartoolkit.core.marker.nft;




import java.io.File;
import java.io.InputStream;
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
	public NyAR2FeaturePoints[] list;
	public NyARNftFsetFile(NyAR2FeaturePoints[] i_list)
	{
		this.list=i_list;
		return;
	}
	public static void main(String[] args){
		NyARNftFsetFile f=NyARNftFsetFile.loadFromFsetFile(new File("../Data/pinball.fset"));
		return;
	}
	
}

/**
 * ARToolKitV4フォーマットのFSETデータ１セットをパースするクラス
 */
class FsetFileDataParserV4
{
	final public NyARNftFsetFile.NyAR2FeaturePoints[] points;
	
	public FsetFileDataParserV4(byte[] i_src)
	{
		BinaryReader br=new BinaryReader(i_src,BinaryReader.ENDIAN_LITTLE);
		int num_of_data=br.getInt();
		NyARNftFsetFile.NyAR2FeaturePoints[] l=new NyARNftFsetFile.NyAR2FeaturePoints[num_of_data];

		for(int i=0;i<l.length;i++){
			int scale=br.getInt();
			double maxdpi=br.getFloat();
			double mindpi=br.getFloat();
			int num_of_coord=br.getInt();
			NyARNftFsetFile.NyAR2FeaturePoints p=new NyARNftFsetFile.NyAR2FeaturePoints(num_of_coord,scale,maxdpi,mindpi);
			for(int i2=0;i2<num_of_coord;i2++){
				p.coord[i2].x=br.getInt();
				p.coord[i2].y=br.getInt();
				p.coord[i2].mx=br.getFloat();
				p.coord[i2].my=br.getFloat();
				p.coord[i2].maxSim=br.getFloat();
			}
			l[i]=p;
		}
		this.points=l;
	}
}




