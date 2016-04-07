package jp.nyatla.nyartoolkit.core.marker.nft.iset;


import jp.nyatla.nyartoolkit.j2se.BinaryReader;

/**
 * ARToolKitV4のisetファイルを読み出します。
 * <pre>
 * //ファイル形式
 * int imageset_num;
 * AR2ImageT{
 * 		int           xsize;
 *		int           ysize;
 *		float         dpi;
 * 		byte          ARUint8[xsize*ysize];
 * }[imageset_num];
 * </pre>
 */
public class IsetFileDataParserV4
{
	public class AR2ImageT
	{
		final public double dpi;
		final public int width;
		final public int height;
		final public byte[] img;
		public AR2ImageT(int i_w,int i_h,double i_dpi,byte[] i_img)
		{
			this.width=i_w;
			this.height=i_h;
			this.dpi=i_dpi;
			this.img=i_img;
		}
	}
	final public AR2ImageT[] ar2image;
	public IsetFileDataParserV4(byte[] i_src)
	{
		BinaryReader br=new BinaryReader(i_src,BinaryReader.ENDIAN_LITTLE);		
		int n=br.getInt();
		this.ar2image=new AR2ImageT[n];
		for(int i=0;i<n;i++){
			int w=br.getInt();
			int h=br.getInt();
			double dpi=br.getDouble();
			byte[] d=br.getByteArray(w*h);
			this.ar2image[i]=new AR2ImageT(w,h,dpi,d);
		}
	}
}
