package jp.nyatla.nyartoolkit.core.marker.nft.fset;

import java.io.IOException;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;
import jp.nyatla.nyartoolkit.j2se.BinaryWriter;
import jp.nyatla.nyartoolkit.j2se.JpegIO;

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
	public FsetFileDataParserV4(NyARNftFsetFile.NyAR2FeaturePoints[] i_list)
	{
		this.points=i_list;
	}
	
	/**
	 * 格納している情報から、ファイルイメージを生成します。
	 * @param i_sub_dpis
	 * @return
	 */
	public byte[] makeBinary()
	{
		//初期メモリは2MB
		BinaryWriter bw=new BinaryWriter(BinaryReader.ENDIAN_LITTLE,2*1024*1024);
		bw.putInt(this.points.length);
		for(int i=0;i<this.points.length;i++){
			NyARNftFsetFile.NyAR2FeaturePoints p=this.points[i];
			bw.putInt(p.scale);
			bw.putFloat((float) p.maxdpi);
			bw.putFloat((float) p.mindpi);
			bw.putInt(p.coord.length);			
			for(int j=0;j<p.coord.length;j++){
				bw.putInt(p.coord[j].x);
				bw.putInt(p.coord[j].y);
				bw.putFloat((float)p.coord[j].mx);
				bw.putFloat((float)p.coord[j].my);
				bw.putFloat((float)p.coord[j].maxSim);
			}
		}
		return bw.getBinary();
	}	
}