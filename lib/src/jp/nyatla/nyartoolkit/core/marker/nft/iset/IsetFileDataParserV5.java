package jp.nyatla.nyartoolkit.core.marker.nft.iset;

import java.io.IOException;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;
import jp.nyatla.nyartoolkit.j2se.BinaryWriter;
import jp.nyatla.nyartoolkit.j2se.JpegIO;

/**
 * ARToolKitV5形式のIsetファイルを読み出します。
 * <pre>
 * //ファイル形式
 * int32 as numofiset
 * uint8 as jpeg binary
 * float[numofiset-1] as dpi[n+1]
 * </pre>
 */
public class IsetFileDataParserV5
{
	final public int num_of_iset;
	final public byte[] image;
	final public float image_dpi_x;
	final public float image_dpi_y;
	final public int image_unit;
	final public NyARIntSize image_size;
	/** サブ画像のdpiセット*/
	final public float[] sub_dpis;

	
	/**
	 * @param i_src
	 * isetファイルイメージを格納したbyte配列
	 */
	public IsetFileDataParserV5(byte[] i_src)
	{
		BinaryReader br=new BinaryReader(i_src,BinaryReader.ENDIAN_LITTLE);
		//read raw data
		int size=br.size();
		int noi=br.getInt();
		int jpeg_size=size-4-(4*(noi-1));
		byte[] jpeg=br.getByteArray(jpeg_size);
		float[] ldpi=br.getFloatArray(noi-1);
		
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
		this.sub_dpis=ldpi;
		return;
	}
	/**
	 * 格納している情報から、ファイルイメージを生成します。
	 * @param i_sub_dpis
	 * @return
	 */
	public byte[] makeFileImage(float[] i_sub_dpis)
	{
		try {
			//初期メモリは2MB
			BinaryWriter bw=new BinaryWriter(BinaryReader.ENDIAN_LITTLE,2*1024*1024);
			//dpiセット+1
			bw.putInt(i_sub_dpis.length+1);
			//jpgイメージ
			bw.putByteArray(JpegIO.encode(this.image_size.w,this.image_size.h,(int)this.image_dpi_x,(int)this.image_dpi_y,this.image_unit,this.image,0.8f));
			//サブdpi
			bw.putFloatArray(i_sub_dpis);
			return bw.getBynary();
		} catch (IOException e) {
			throw new NyARRuntimeException(e);
		}
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
