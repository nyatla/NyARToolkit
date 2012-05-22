package jp.nyatla.nyartoolkit.utils.j2se;

import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.pixeldriver.NyARRgbPixelDriverFactory;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * このクラスは、BufferedImageをピクセルバッファに持つNyARToolKit互換のラスタクラスです。
 * バッファタイプは、BufferedImageのタイプにより異なります。
 * NyARToolKitと互換性が高い場合はメモリオブジェクトになりますが、そうでない場合はBufferedImageObjectになります。
 * BufferedImageインスタンスをラップしています。
 * 通常のJavaプログラムと相互運用する場合に便利です。
 * BufferedImageの形式はコンストラクタで指定できます。
 */
public class NyARBufferedImageRaster extends NyARRgbRaster
{
	private BufferedImage _buffered_image;
	/**
	 * BufferedImageを外部参照したラスタを構築します。
	 * @param i_img
	 * 参照するラスタ
	 * @throws NyARException
	 */
	public NyARBufferedImageRaster(BufferedImage i_img) throws NyARException
	{
		//NyARToolkit互換のラスタを定義する。
		super(i_img.getWidth(),i_img.getHeight(),getRasterTypeFromBufferedImage(i_img),false);
		this.wrapImage(i_img);
	}
	public NyARBufferedImageRaster(int i_width,int i_height,int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		//NyARToolkit互換のラスタを定義する。
		super(i_width,i_height,i_raster_type,i_is_alloc);
	}

	/**
	 * コンストラクタです。
	 *　{@link BufferedImage}と互換性のあるラスタを構築します。
	 *　BufferedImageをラップするには、i_is_alloc引数にfalseを設定して生成して、wrapImageでBitmapBufferをラップします。
	 * @param i_width
	 * ラスタの幅を指定します。
	 * @param i_height
	 * ラスタの高さを指定します。
	 * @param i_is_alloc
	 * BufferedImageを内部生成するかのフラグ。trueの場合、インスタンスはバッファを所有します。
	 * @throws NyARException
	 */	
	public NyARBufferedImageRaster(int i_width,int i_height,boolean i_is_alloc) throws NyARException
	{
		//NyARToolkit互換のラスタを定義する。
		super(i_width,i_height, NyARBufferType.INT1D_X8R8G8B8_32,i_is_alloc);
	}
	/**
	 * コンストラクタです。
	 * {@link BufferedImage}を所有したラスタを構築します。
	 * @param i_width
	 * ラスタの幅を指定します。
	 * @param i_height
	 * ラスタの高さを指定します。
	 * @throws NyARException
	 */
	public NyARBufferedImageRaster(int i_width,int i_height) throws NyARException
	{
		//NyARToolkit互換のラスタを定義する。
		super(i_width,i_height, NyARBufferType.INT1D_X8R8G8B8_32,true);
	}
	/**
	 * この関数は、画像ファイルからBufferedImageRasterを生成します。
	 * @param i_file
	 * @return
	 * @throws IOException
	 * @throws NyARException 
	 */
	public static NyARBufferedImageRaster createFromFile(String i_file) throws NyARException
	{
		BufferedImage img;
		try{
			img = ImageIO.read(new File(i_file));
		}catch(Exception e){
			throw new NyARException();
		}
		//画像フォーマットの解析
		NyARBufferedImageRaster ra=new NyARBufferedImageRaster(img.getWidth(),img.getHeight(),false);
		ra.wrapBuffer(img);
		return ra;
	}
	
	public Object createInterface(Class<?> i_iid) throws NyARException
	{
		//アクセラレータインタフェイスはここに追加する。
		return super.createInterface(i_iid);
	}

	/**
	 * この関数は、NyARRgbRasterに、NyARBufferedImageRasterの機能を追加します。
	 * @throws NyARException 
	 */
	protected void initInstance(NyARIntSize i_size,int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		super.initInstance(i_size, i_raster_type, i_is_alloc);
		//成功した場合、i_is_allocがtrueなら、BufferedImageの構築
		if(i_is_alloc){
			BufferedImage bfi;
			switch(i_raster_type){
			case NyARBufferType.BYTE1D_R8G8B8_24:{
				byte[] b=(byte[])this._buf;
				DataBufferByte d=new DataBufferByte(b,b.length);
				int[] bof={0,1,2};
				bfi=new BufferedImage(
					new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),false,true,Transparency.OPAQUE,DataBuffer.TYPE_BYTE),
					Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(),i_size.w,i_size.h,3,3*i_size.w,bof),d,null),
					true,null);
				}
				break;
			case NyARBufferType.BYTE1D_B8G8R8_24:{
				byte[] b=(byte[])this._buf;
				DataBufferByte d=new DataBufferByte(b,b.length);
				int[] bof={2,1,0};
				bfi=new BufferedImage(
					new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),false,true,Transparency.OPAQUE,DataBuffer.TYPE_BYTE),
					Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(),i_size.w,i_size.h,3,3*i_size.w,bof),d,null),
					true,null);
				}
				break;
			case NyARBufferType.INT1D_X8R8G8B8_32:{
				int[] b=(int[])this._buf;
				DataBufferInt d=new DataBufferInt(b,b.length);
				int[] msk={0xff0000,0x00ff00,0x0000ff};
				bfi=new BufferedImage(
					new DirectColorModel(24,msk[0],msk[1],msk[2]),
					Raster.createWritableRaster(new SinglePixelPackedSampleModel(d.getDataType(),i_size.w,i_size.h,msk),d,null),
					true,null);
				}
				break;
			default:
				throw new NyARException();
			}
			this._buffered_image=bfi;
		}
		//ピクセルドライバの生成
		this._rgb_pixel_driver=NyARRgbPixelDriverFactory.createDriver(this);
		return;
	}
	/**
	 * BitmapBufferをラップします。古いBitmapbufferへの参照は解除されます。
	 * @param i_ref_bmi
	 * ラップするBitmapBufferオブジェクト。このオブジェクトは、現在のラスタと同じフォーマットである必要があります。
	 * @throws NyARException
	 */
	public void wrapImage(BufferedImage i_ref_bmi) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		assert(this._size.isEqualSize(i_ref_bmi.getWidth(),i_ref_bmi.getHeight()));//サイズ確認
		//ラスタタイプの決定
		int raster_type=getRasterTypeFromBufferedImage(i_ref_bmi);
		//フォーマット確認
		if(!this.isEqualBufferType(raster_type)){
			throw new NyARException();
		}
		//参照しているImageを切り替え
		this._buffered_image=i_ref_bmi;
		//バッファの切替
		switch(raster_type){
			case NyARBufferType.BYTE1D_R8G8B8_24:
			case NyARBufferType.BYTE1D_B8G8R8_24:
			case NyARBufferType.BYTE1D_B8G8R8X8_32:
			case NyARBufferType.BYTE1D_X8B8G8R8_32:
				this._buf=((DataBufferByte)(i_ref_bmi.getRaster().getDataBuffer())).getData();
				break;
			case NyARBufferType.INT1D_X8R8G8B8_32:
				this._buf=((DataBufferInt)(i_ref_bmi.getRaster().getDataBuffer())).getData();
				break;
			case NyARBufferType.OBJECT_Java_BufferedImage:
				this._buf=i_ref_bmi;
				break;
			default:
				throw new NyARException();
		}
		//ピクセルドライバを更新
		this._rgb_pixel_driver.switchRaster(this);
	}
	/**
	 * この関数は使用できません。{@link BufferedImage}をセットするには、wrapImageを使用してください。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		throw new NyARException();
	}	
	/**
	 * この関数は、ラップしているBufferedImageを返します。
	 * @return
	 * ラップしているBufferedImageの実体
	 */
	public final BufferedImage getBufferedImage()
	{
		return this._buffered_image;
	}
	/**
	 * この関数は、BufferedImageのGraphicsを返します。
	 * @return
	 * ラップしているBufferedImageのGraphicsオブジェクト
	 */
	public final Graphics getGraphics()
	{
		return this._buffered_image.getGraphics();
	}
	/**
	 * この関数は、BufferedImageを分析して、WriterbleRasterと互換性のあるNyARBufferTypeを調べます。
	 * @param im
	 * @return
	 */
	private static int getRasterTypeFromBufferedImage(BufferedImage im)
	{
		WritableRaster wr=im.getRaster();
		SampleModel sp=wr.getSampleModel();
		int w=im.getWidth();
		if(sp instanceof ComponentSampleModel){
			ComponentSampleModel csp=(ComponentSampleModel)sp;
			int data_type=csp.getDataType();
			int band_num=csp.getNumBands();
			int pix_stride=csp.getPixelStride();
			int scan_stride=csp.getScanlineStride();
			int[] indices=csp.getBankIndices();
			int[] band_offset=csp.getBandOffsets();
			//BYTE1D_XXX_24の可能性があるかを確認。
			if(data_type==DataBuffer.TYPE_BYTE && band_num==3 && scan_stride==w*3 && pix_stride==3 && indices[0]==0 && indices[1]==0 && indices[2]==0)
			{
				if(band_offset[0]==0 && band_offset[1]==1 && band_offset[2]==2)
				{
					return NyARBufferType.BYTE1D_R8G8B8_24;
				}
				if(band_offset[0]==2 && band_offset[1]==1 && band_offset[2]==0)
				{
					return NyARBufferType.BYTE1D_B8G8R8_24;
				}
			}else if(data_type==DataBuffer.TYPE_BYTE && band_num==4&& scan_stride==w*4 && pix_stride==4 && indices[0]==0 && indices[1]==0 && indices[2]==0)
			{
				//B0G1R2X
				//R2,G1,B0
				if(band_offset[0]==2 && band_offset[1]==1 && band_offset[2]==0)
				{
					return NyARBufferType.BYTE1D_B8G8R8X8_32;
				}
				if(band_offset[0]==3 && band_offset[1]==2 && band_offset[2]==1)
				{
					return NyARBufferType.BYTE1D_X8B8G8R8_32;
				}				
			}			
		}else if(sp instanceof SinglePixelPackedSampleModel){
			SinglePixelPackedSampleModel ssp=(SinglePixelPackedSampleModel)sp;
			int data_type=ssp.getDataType();
			int[] mask=ssp.getBitMasks();
			int scan_stride=ssp.getScanlineStride();
			int[] offset=ssp.getBitOffsets();
			if(data_type==DataBuffer.TYPE_INT && scan_stride==w && offset[0]==16 && offset[1]==8 && offset[2]==0)
			{
				if(mask[0]==0x00ff0000 && mask[1]==0x0000ff00 && mask[2]==0x000000ff){
					return NyARBufferType.INT1D_X8R8G8B8_32;
				}
			}
		}
		//具体的なBufferが判らない。
		return NyARBufferType.OBJECT_Java_BufferedImage;
	}

}
//
//ラスタドライバ
//


/**
 * このクラスは、BufferedImage用の低速なPixelReaderです。
 * BufferedImageの形式に依存しない代わりに、ピクセルへのアクセス速度が極端に低速です。
 * 
 */
final class NyARRgbPixelReader_OBJECT_Java_BufferedImage implements INyARRgbPixelDriver
{
	protected BufferedImage _ref_buf;
	private NyARIntSize _ref_size;

	public void getPixel(int i_x, int i_y, int[] o_rgb)
	{
		int p=this._ref_buf.getRGB(i_x, i_y);
		o_rgb[0] = ((p>>16) & 0xff);// R
		o_rgb[1] = ((p>>8) & 0xff);// G
		o_rgb[2] = (p & 0xff);// B
		return;
	}
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
		for (int i = i_num - 1; i >= 0; i--) {
			int p=this._ref_buf.getRGB(i_x[i],i_y[i]);
			o_rgb[i * 3 +0] = ((p>>16) & 0xff);// R
			o_rgb[i * 3 +1] = ((p>>8) & 0xff);// G
			o_rgb[i * 3 +2] = (p & 0xff);// B			
		}
		return;
	}
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		this._ref_buf.setRGB(i_x, i_y, ((i_rgb[0]<<16)&0xff0000)|((i_rgb[1]<<8)&0x00ff00)|((i_rgb[2])&0x0000ff));
	}
	public void setPixel(int i_x, int i_y, int i_r,int i_g,int i_b) throws NyARException
	{
		this._ref_buf.setRGB(i_x, i_y, ((i_r<<16)&0xff0000)|((i_g<<8)&0x00ff00)|(i_b&0x0000ff));
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void switchRaster(INyARRgbRaster i_raster)throws NyARException
	{
		this._ref_buf=(BufferedImage)i_raster.getBuffer();
		this._ref_size=i_raster.getSize();
	}

	public boolean isCompatibleRaster(INyARRgbRaster iRaster)
	{
		return iRaster.isEqualBufferType(NyARBufferType.OBJECT_Java_BufferedImage);
	}
	public NyARIntSize getSize() {
		return this._ref_size;
	}
}
