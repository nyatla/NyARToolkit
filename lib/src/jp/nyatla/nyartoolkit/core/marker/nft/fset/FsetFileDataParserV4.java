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
import jp.nyatla.nyartoolkit.j2se.BinaryReader;
import jp.nyatla.nyartoolkit.j2se.BinaryWriter;


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