package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.imageset;

import java.io.InputStream;
import java.nio.ByteOrder;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.j2se.ByteBufferedInputStream;

/**
 * ARToolkitNFTの基準画像セ�?トを格納します�??
 * AR2ImageSetTと同等�?�機�?�です�??
 *
 */
public class NyARSurfaceImageSet
{
	public NyARSurfaceImageSet(ReferenceImage[] i_items)
	{
		this.items=i_items;
	}
	public ReferenceImage[] items;
	
	public static NyARSurfaceImageSet loadFromIsetFile(InputStream i_stream) throws NyARRuntimeException
	{
		ISetReader isr=new ISetReader(i_stream);
		int num=isr.getNumberOfItem();
		ReferenceImage[] ret=new ReferenceImage[num];
		for(int i=0;i<num;i++){
			ret[i]=isr.getAR2ImageT();
		}
		return new NyARSurfaceImageSet(ret);
	}	
	public static class ReferenceImage
	{
		public double dpi;
		public int width;
		public int height;
		public byte[] img;
		public ReferenceImage(int i_w,int i_h,double i_dpi)
		{
			this.width=i_w;
			this.height=i_h;
			this.dpi=i_dpi;
			this.img=new byte[i_w*i_h];
		}
	}
}


/**
 * ----
 * File structure of Iset
 * ----
 * int imageset_num;
 * AR2ImageT{
 * 		int           xsize;
 *		int           ysize;
 *		float         dpi;
 * 		byte          ARUint8[xsize*ysize];
 * }[imageset_num];
 * ----
 */
class ISetReader extends ByteBufferedInputStream
{
	public ISetReader(InputStream i_stream)
	{
		super(i_stream,512);
		this.order(ENDIAN_LITTLE);
	}
	public int getNumberOfItem() throws NyARRuntimeException
	{
		this.readToBuffer(4);
		return this.getInt();
	}
	public NyARSurfaceImageSet.ReferenceImage getAR2ImageT() throws NyARRuntimeException
	{
		this.readToBuffer(4*2+4);
		int w=this.getInt();
		int h=this.getInt();
		double dpi=this.getFloat();
		NyARSurfaceImageSet.ReferenceImage ret=new NyARSurfaceImageSet.ReferenceImage(w,h,dpi);
		this.readBytes(ret.img,w*h);
		return ret;
	}
}