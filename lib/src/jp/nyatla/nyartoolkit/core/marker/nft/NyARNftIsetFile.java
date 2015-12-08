package jp.nyatla.nyartoolkit.core.marker.nft;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;
import jp.nyatla.nyartoolkit.j2se.JpegIO;



/**
 * ARToolkitNFTの基準画像セット(ISET)を格納します。
 * AR2ImageSetTと同等の機能です。
 */
public class NyARNftIsetFile
{
	final public static int FILE_FORMAT_ARTK_V5=1;
	final public static int FILE_FORMAT_ARTK_V4=2;
	public static NyARNftIsetFile loadFromIsetFile(InputStream i_stream,int i_file_format)
	{
		return loadFromIsetFile(BinaryReader.toArray(i_stream),i_file_format);
	}
	public static NyARNftIsetFile loadFromIsetFile(InputStream i_stream)
	{
		return loadFromIsetFile(i_stream,FILE_FORMAT_ARTK_V5);
	}
	public static NyARNftIsetFile loadFromIsetFile(File i_file,int i_file_format)
	{
		return loadFromIsetFile(BinaryReader.toArray(i_file),i_file_format);
	}
	public static NyARNftIsetFile loadFromIsetFile(File i_file)
	{
		return loadFromIsetFile(BinaryReader.toArray(i_file),FILE_FORMAT_ARTK_V5);		
	}
	
	public static NyARNftIsetFile loadFromIsetFile(byte[] i_src,int i_file_format)
	{
		switch(i_file_format){
		case FILE_FORMAT_ARTK_V5:
		{
			IsetFileDataParserV5 iset= new IsetFileDataParserV5(i_src);
			ReferenceImage[] items=new ReferenceImage[iset.num_of_iset];
			//1stIset
			double dpi=iset.getImageDpi();
			items[0]=new ReferenceImage(iset.image_size.w,iset.image_size.h,dpi,iset.image);
			//2nd to end
			for(int i=1;i<iset.num_of_iset;i++){
				items[i]=new ReferenceImage(iset.image_size.w,iset.image_size.h,iset.image,dpi,iset.dpi[i-1]);
			}
			return new NyARNftIsetFile(items);
		}
		case FILE_FORMAT_ARTK_V4:
		{
			ISetFileDataParserV4 iset = new ISetFileDataParserV4(i_src);
			ReferenceImage[] items=new ReferenceImage[iset.ar2image.length];
			for(int i=0;i<items.length;i++){
				ISetFileDataParserV4.AR2ImageT tmp=iset.ar2image[i];
				items[i]=new ReferenceImage(tmp.width,tmp.height,tmp.dpi,tmp.img);
			}
			return new NyARNftIsetFile(items);
		}
		default:
			throw new NyARRuntimeException();
		}
	}	
	public NyARNftIsetFile(ReferenceImage[] i_items)
	{
		this.items=i_items;
	}
	public ReferenceImage[] items;
	
	/**
	 * ワーク関数
	 * @param x
	 * @return
	 */
	private static int lroundf(double x){
		return (int) ((x)>=0.0f?(long)((x)+0.5f):(long)((x)-0.5f));
	}	
	public static class ReferenceImage
	{
		public final double dpi;
		public final int width;
		public final int height;
		public final byte[] img;
		public ReferenceImage(int i_w,int i_h,double i_dpi,byte[] i_ref_buf)
		{
			this.width=i_w;
			this.height=i_h;
			this.dpi=i_dpi;
			this.img=i_ref_buf;
		}
		/**
		 * レイヤ2以降のイメージを生成する。
		 * idxは{@link #dpi }のlength-1まで。
		 * @param i_idx
		 * @return
		 */
		public ReferenceImage(int i_w,int i_h,byte[] i_src,double i_src_dpi,double i_dest_dpi)
		{
		    int wx = (int)lroundf(i_w * i_dest_dpi / i_src_dpi);
		    int wy = (int)lroundf(i_h * i_dest_dpi / i_src_dpi);

		    this.width=wx;
		    this.height=wy;
		    this.dpi=i_dest_dpi;
		    this.img=new byte[wx*wy];
		    int p2 = 0;//dst->imgBW;


		    for(int jj = 0; jj < wy; jj++ ) {
		        int sy = (int)lroundf( jj    * i_src_dpi / dpi);
		        int ey = (int)lroundf((jj+1) * i_src_dpi / dpi) - 1;
		        if( ey >= i_h ){
		        	ey = i_h - 1;
		        }
		        for(int ii = 0; ii < wx; ii++ ) {
		        	int sx = (int)lroundf( ii    * i_src_dpi / dpi);
		        	int ex = (int)lroundf((ii+1) * i_src_dpi / dpi) - 1;
		            if( ex >= i_w ){
		            	ex = i_w - 1;
		            }
		            int co =0;
		            int value = 0;
		            for(int jjj = sy; jjj <= ey; jjj++ ) {
		                int p1 = jjj*i_w+sx;
		                for(int iii = sx; iii <= ex; iii++ ) {
		                    value += i_src[p1++] & 0xff;
		                    co++;
		                }
		            }
		            this.img[p2++] = (byte) (value / co);
		        }
		    }
		    return;		
		}		
	}
	public static void main(String[] args){
		NyARNftIsetFile f=NyARNftIsetFile.loadFromIsetFile(new File("../Data/pinball.iset5"));
		for(int i=0;i<f.items.length;i++){
			int s=f.items[i].width*f.items[i].height;
			long sum=0;
			for(int i2=0;i2<s;i2++){
				sum+=(f.items[i].img[i2] & 0xff);
			}
			System.out.println(f.items[i].dpi+","+f.items[i].width+","+f.items[i].height+","+Long.toString(sum));
		}
		return;
	}	
}
/**
 * ARToolKitV5形式のIsetファイルを読み出します。
 * <pre>
 * //ファイル形式
 * int32 as numofiset
 * uint8 as jpeg binary
 * float[numofiset-1] as dpi[n+1]
 * </pre>
 */
class IsetFileDataParserV5
{
	final public int num_of_iset;
	final public byte[] image;
	final public double image_dpi_x;
	final public double image_dpi_y;
	final public int image_unit;
	final public NyARIntSize image_size;
	
	final public float[] dpi;

	
	public IsetFileDataParserV5(byte[] i_src)
	{
		BinaryReader br=new BinaryReader(i_src,BinaryReader.ENDIAN_LITTLE);
		//read raw data
		int size=br.size();
		int noi=br.getInt();
		int jpeg_size=size-4-(4*(noi-1));
		byte[] jpeg=br.getBytes(jpeg_size);
		float[] ldpi=br.getFloats(noi-1);
		
		JpegIO.DecodeResult d;
		try {
			d = JpegIO.decode(jpeg);
		} catch (IOException e) {
			//この例外はファイルアクセスを伴わないから握りつぶしてOK
			throw new NyARRuntimeException(e);
		}
		this.image=d.img;
		this.image_dpi_x=d.x_density;
		this.image_dpi_y=d.y_density;
		this.image_unit=d.density_unit;
		this.image_size=new NyARIntSize(d.width,d.height);
		this.num_of_iset=noi;
		this.dpi=ldpi;
		return;
	}

	public double getImageDpi()
	{
		if(this.image_unit==1 && this.image_dpi_x==this.image_dpi_y){
			return this.image_dpi_x;
		}else if(this.image_unit==2 && this.image_dpi_x==this.image_dpi_y){
			return this.image_dpi_x*2.54;
		}else if(this.image_unit>2 && this.image_dpi_x==0 && this.image_dpi_y==0){
			return this.image_unit;
		}else{
			return 0;
		}
	}
}




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
class ISetFileDataParserV4
{
	public class AR2ImageT
	{
		final public double dpi;
		final public int width;
		final int height;
		final byte[] img;
		public AR2ImageT(int i_w,int i_h,double i_dpi,byte[] i_img)
		{
			this.width=i_w;
			this.height=i_h;
			this.dpi=i_dpi;
			this.img=i_img;
		}
	}
	final public AR2ImageT[] ar2image;
	public ISetFileDataParserV4(byte[] i_src)
	{
		BinaryReader br=new BinaryReader(i_src,BinaryReader.ENDIAN_LITTLE);		
		int n=br.getInt();
		this.ar2image=new AR2ImageT[n];
		for(int i=0;i<n;i++){
			int w=br.getInt();
			int h=br.getInt();
			double dpi=br.getDouble();
			byte[] d=br.getBytes(w*h);
			this.ar2image[i]=new AR2ImageT(w,h,dpi,d);
		}
	}
}