package jp.nyatla.nyartoolkit.core.marker.nft.fset;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;

/**
 * ARToolKitV4フォーマットのFSETデータ１セットをパースするクラス
 */
public class FsetFileDataParserV4
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