/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DogFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.freak.FREAKExtractor;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.BinomialPyramid32f;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;
import jp.nyatla.nyartoolkit.j2se.BinaryWriter;

/**
 * FREAK用のfsetファイル形式(fset3)データを格納します。 KpmRefDataSetの一部と同じです。
 */
public class NyARNftFreakFsetFile {
	public static class ImageInfo {
		public ImageInfo(int i_w, int i_h, int i_image_no) {
			this.w = i_w;
			this.h = i_h;
			this.image_no = i_image_no;
		}

		public int w;
		public int h;
		public int image_no;
	}

	public static class PageInfo {
		public PageInfo(int i_page_no, ImageInfo[] i_image_info) {
			this.page_no = i_page_no;
			this.image_info = i_image_info;
		}

		final public int page_no;
		final public ImageInfo[] image_info;
	}

	public static class FreakFeature {
		final public static int FREAK_SUB_DIMENSION = 96;
		final public byte[] v = new byte[FREAK_SUB_DIMENSION];
		public double angle;
		public double scale;
		public int maxima;
	}

	public static class RefDataSet {
		final public NyARDoublePoint2d coord2D = new NyARDoublePoint2d();
		/** in millimeters*/
		final public NyARDoublePoint2d coord3D = new NyARDoublePoint2d();
		final public FreakFeature featureVec = new FreakFeature();
		public int pageNo;
		public int refImageNo;
	}

	final public RefDataSet[] ref_point;
	final public PageInfo[] page_info;

	public NyARNftFreakFsetFile(RefDataSet[] i_refdata, PageInfo[] i_page_info) {
		this.ref_point = i_refdata;
		this.page_info = i_page_info;
		return;
	}

	public static NyARNftFreakFsetFile loadFromfset3File(InputStream i_stream) {
		return loadFromfset3File(BinaryReader.toArray(i_stream));
	}

	public static NyARNftFreakFsetFile loadFromfset3File(File i_file) {
		try {
			return loadFromfset3File(new FileInputStream(i_file));
		} catch (FileNotFoundException e) {
			throw new NyARRuntimeException(e);
		}
	}

	public static NyARNftFreakFsetFile loadFromfset3File(byte[] i_source) {
		BinaryReader br = new BinaryReader(i_source, BinaryReader.ENDIAN_LITTLE);
		int num = br.getInt();

		RefDataSet[] rds = new RefDataSet[num];

		for (int i = 0; i < num; i++) {
			RefDataSet rd = new RefDataSet();
			rd.coord2D.x = br.getFloat();
			rd.coord2D.y = br.getFloat();
			rd.coord3D.x = br.getFloat();
			rd.coord3D.y = br.getFloat();
			br.getByteArray(rd.featureVec.v);
			rd.featureVec.angle = br.getFloat();
			rd.featureVec.scale = br.getFloat();
			rd.featureVec.maxima = br.getInt();
			rd.pageNo = br.getInt();
			rd.refImageNo = br.getInt();
			rds[i] = rd;
		}

		int page_num = br.getInt();
		PageInfo[] kpi = new PageInfo[page_num];
		for (int i = 0; i < page_num; i++) {
			int page_no = br.getInt();
			int img_num = br.getInt();
			ImageInfo[] kii = new ImageInfo[img_num];
			for (int i2 = 0; i2 < img_num; i2++) {
				kii[i2] = new ImageInfo(br.getInt(), br.getInt(), br.getInt());

			}
			kpi[i] = new PageInfo(page_no, kii);
		}
		return new NyARNftFreakFsetFile(rds, kpi);
	}
	public static NyARNftFreakFsetFile genFeatureSet3(NyARNftIsetFile i_iset_file)
	{
		int max_features=500;
		DogFeaturePointStack _dog_feature_points = new DogFeaturePointStack(max_features);
		FreakFeaturePointStack query_keypoint = new FreakFeaturePointStack(max_features);
		//
		List<NyARNftFreakFsetFile.RefDataSet> refdataset=new ArrayList<NyARNftFreakFsetFile.RefDataSet>();
		List<NyARNftFreakFsetFile.ImageInfo> imageinfo=new ArrayList<NyARNftFreakFsetFile.ImageInfo>();
		for(int ii=0;ii<i_iset_file.items.length;ii++){
			NyARNftIsetFile.ReferenceImage rimg=i_iset_file.items[ii];
			FREAKExtractor mFeatureExtractor=new FREAKExtractor();
			int octerves=BinomialPyramid32f.octavesFromMinimumCoarsestSize(rimg.width,rimg.height,8);
			BinomialPyramid32f _pyramid=new BinomialPyramid32f(rimg.width,rimg.height,octerves,3);
			DoGScaleInvariantDetector _dog_detector = new DoGScaleInvariantDetector(rimg.width,rimg.height,octerves,3,3,4,max_features);
		
			//RefDatasetの作成
			_pyramid.build(NyARGrayscaleRaster.createInstance(rimg.width,rimg.height,NyARBufferType.INT1D_GRAY_8,rimg.img));
			// Detect feature points
			_dog_feature_points.clear();
			_dog_detector.detect(_pyramid,_dog_feature_points);
	
			// Extract features
			query_keypoint.clear();
			mFeatureExtractor.extract(_pyramid,_dog_feature_points,query_keypoint);
			
			for(int i=0;i<query_keypoint.getLength();i++){
				FreakFeaturePoint ffp=query_keypoint.getItem(i);
				NyARNftFreakFsetFile.RefDataSet rds=new NyARNftFreakFsetFile.RefDataSet();
				rds.pageNo=1;
				rds.refImageNo=ii;
				rds.coord2D.setValue(ffp.x,ffp.y);
				rds.coord3D.setValue((ffp.x + 0.5f) / rimg.dpi * 25.4f,((rimg.height-0.5f) - ffp.y) / rimg.dpi * 25.4f);
				rds.featureVec.angle=ffp.angle;
				rds.featureVec.maxima=ffp.maxima?1:0;
				rds.featureVec.scale=ffp.scale;
				ffp.descripter.getValueLe(rds.featureVec.v);
				refdataset.add(rds);
			}
			imageinfo.add(new NyARNftFreakFsetFile.ImageInfo(rimg.width,rimg.height,ii));
		}
		NyARNftFreakFsetFile.PageInfo[] pi=new NyARNftFreakFsetFile.PageInfo[1];
		pi[0]=new NyARNftFreakFsetFile.PageInfo(1,imageinfo.toArray(new NyARNftFreakFsetFile.ImageInfo[0]));
		return new NyARNftFreakFsetFile(refdataset.toArray(new NyARNftFreakFsetFile.RefDataSet[0]),pi);
	}
	
	/**
	 * 現在のファイルイメージをbyte[]で返却します。
	 * @return
	 */
	public byte[] makeFset3Binary()
	{
		BinaryWriter bw=new BinaryWriter(BinaryWriter.ENDIAN_LITTLE,2*1024*1024);
		bw.putInt(this.ref_point.length);
		for (int i = 0; i < this.ref_point.length; i++) {
			RefDataSet rd = this.ref_point[i];
			bw.putFloat((float)rd.coord2D.x);
			bw.putFloat((float)rd.coord2D.y);
			bw.putFloat((float)rd.coord3D.x);
			bw.putFloat((float)rd.coord3D.y);
			bw.putByteArray(rd.featureVec.v);
			bw.putFloat((float)rd.featureVec.angle);
			bw.putFloat((float)rd.featureVec.scale);
			bw.putInt(rd.featureVec.maxima);
			bw.putInt(rd.pageNo);
			bw.putInt(rd.refImageNo);
		}
		bw.putInt(this.page_info.length);
		for(int i=0;i<this.page_info.length;i++){
			PageInfo pi=this.page_info[i];
			bw.putInt(pi.page_no);
			bw.putInt(pi.image_info.length);
			for(int j=0;j<pi.image_info.length;j++){
				bw.putInt(pi.image_info[j].w);
				bw.putInt(pi.image_info[j].h);
				bw.putInt(pi.image_info[j].image_no);
			}
		}
		return bw.getBinary();
	}	
	
	public static void main(String[] args) {
		try {
			NyARNftFreakFsetFile f = NyARNftFreakFsetFile.loadFromfset3File(new FileInputStream(new File("../Data/pinball.fset3")));
			NyARNftFreakFsetFile f2 = NyARNftFreakFsetFile.loadFromfset3File(f.makeFset3Binary());
			System.out.println(f);
			return;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
