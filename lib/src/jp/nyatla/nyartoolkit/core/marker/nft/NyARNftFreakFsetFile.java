package jp.nyatla.nyartoolkit.core.marker.nft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;

/**
 * FREAK用のfsetファイル形式(fset3)データを格納します。 KpmRefDataSetの一部と同じです。
 */
public class NyARNftFreakFsetFile {
	public static class FloatPoint2d
	{
		public float x;
		public float y;
	}	
	public static class ImageInfo
	{
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
		public float angle;
		public float scale;
		public int maxima;
	}
	public static class RefDataSet
	{
	    final public FloatPoint2d        coord2D=new FloatPoint2d();
	    final public FloatPoint2d        coord3D=new FloatPoint2d();      // millimetres.
	    final public FreakFeature      featureVec=new FreakFeature();
	    public int               pageNo;
	    public int               refImageNo;
	}
	final public RefDataSet[] ref_point;
	final public PageInfo[] page_info;

	protected NyARNftFreakFsetFile(RefDataSet[] i_refdata,PageInfo[] i_page_info)
	{
		this.ref_point = i_refdata;
		this.page_info = i_page_info;
		return;
	}
	public static NyARNftFreakFsetFile loadFromfset3File(InputStream i_stream) {
		return loadFromfset3File(BinaryReader.toArray(i_stream));
	}

	public static NyARNftFreakFsetFile loadFromfset3File(byte[] i_source) {
		BinaryReader br = new BinaryReader(i_source, BinaryReader.ENDIAN_LITTLE);
		int num = br.getInt();

		RefDataSet[] rds = new RefDataSet[num];

		for (int i = 0; i < num; i++) {
			RefDataSet rd = new RefDataSet();
			rd.coord2D.x=br.getFloat();
			rd.coord2D.y=br.getFloat();
			rd.coord3D.x=br.getFloat();
			rd.coord3D.y=br.getFloat();
			br.getBytes(rd.featureVec.v);
			rd.featureVec.angle=br.getFloat();
			rd.featureVec.scale=br.getFloat();
			rd.featureVec.maxima=br.getInt();
			rd.pageNo = br.getInt();
			rd.refImageNo = br.getInt();
			rds[i]=rd;
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

	public static void main(String[] args) {
		try {
			NyARNftFreakFsetFile f = NyARNftFreakFsetFile
					.loadFromfset3File(new FileInputStream(new File(
							"../Data/pinball.fset3")));
			return;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
