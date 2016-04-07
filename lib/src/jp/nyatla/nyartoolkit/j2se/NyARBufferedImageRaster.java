package jp.nyatla.nyartoolkit.j2se;

import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このクラスはBufferedImageと共通のピクセルバッファを持つラスタクラスです。
 * {@link #getBufferedImage()}で記憶域を共有した{@link BufferedImage}を得ることができます。
 * 記憶域のバッファ形式はラップするBufferedImageのタイプにより異なります。
 * ラスタオブジェクトの形式を隠蔽するためのオーバヘッドは関数コール1回分です。
 * {@link #getBaseRaster}で得たオブジェクトにアクセスすることで、オーバヘッドを取り除くことができます。
 */
public class NyARBufferedImageRaster extends NyARRgbRaster
{
	protected final INyARRgbRaster _raster;
	private BufferedImage _buffered_image;
	
	/**
	 * この関数は、画像ファイルからBufferedImageRasterを生成します。
	 * @param i_file
	 * @return
	 * @throws IOException
	 * @throws NyARRuntimeException 
	 */
	public static NyARBufferedImageRaster loadFromFile(String i_file) throws IOException
	{
		BufferedImage img;
		img = ImageIO.read(new File(i_file));
		//画像フォーマットの解析
		NyARBufferedImageRaster ra=new NyARBufferedImageRaster(img);
		return ra;		
	}
	/**
	 * {@link #loadFromFile}を使ってください。
	 * @deprecated
	 */
	public static NyARBufferedImageRaster createFromFile(String i_file) throws IOException
	{
		return loadFromFile(i_file);	
	}	
	/**
	 * i_imgをラップするインスタンスを生成します。
	 * @param i_img
	 */
	public NyARBufferedImageRaster(BufferedImage i_img)
	{
		//外部参照のBufferdImageとして生成
		this(
			i_img.getWidth(),i_img.getHeight(),
			getCompatibleRasterType(i_img),false);
//		//ラスタにBitmapをラップさせる
		callWrapBuffer(this._raster,i_img);
		this._buffered_image=i_img;
	}
	public NyARBufferedImageRaster(int i_width,int i_height,int i_raster_type,boolean i_is_alloc)
	{
		super(i_width,i_height,i_is_alloc);
		if(i_is_alloc){
			this._buffered_image=createBufferedImage(this._size,i_raster_type);
			INyARRgbRaster r=NyARRgbRaster.createInstance(i_width,i_height,i_raster_type,false);
			callWrapBuffer(r,this._buffered_image);
			this._raster=r;
		}else{
			this._buffered_image=null;
			if(i_raster_type==NyARBufferType.OBJECT_Java_BufferedImage){
				this._raster=new NyARRgbRaster_OBJECT_Java_BufferedImage(i_width,i_height);
			}else{
				this._raster=NyARRgbRaster.createInstance(i_width,i_height,i_raster_type,false);			
			}
		}
	}

	/**
	 * コンストラクタです。
	 * {@link BufferedImage}を所有したラスタを構築します。
	 * @param i_width
	 * ラスタの幅を指定します。
	 * @param i_height
	 * ラスタの高さを指定します。
	 * @throws NyARRuntimeException
	 */	
	public NyARBufferedImageRaster(int i_width,int i_height)
	{
		this(i_width,i_height,NyARBufferType.INT1D_X8R8G8B8_32,true);
	}

	/**
	 * 実際の画素を格納するラスタを返します。
	 * このラスタを使うと、画素操作関数のオーバヘッドが関数コール1回分だけ削減できます。
	 * @return
	 */
	public final INyARRgbRaster getBaseRaster()
	{
		return this._raster;
	}
	/**
	 * i_raster_typeと互換性のあるBufferedImageを生成します。
	 * @throws NyARRuntimeException 
	 */
	private static BufferedImage createBufferedImage(NyARIntSize i_size,int i_raster_type)
	{
		BufferedImage bfi;
		switch(i_raster_type){
		case NyARBufferType.BYTE1D_R8G8B8_24:{
			byte[] b=new byte[i_size.w*i_size.h*3];
			DataBufferByte d=new DataBufferByte(b,b.length);
			int[] bof={0,1,2};
			bfi=new BufferedImage(
				new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),false,true,Transparency.OPAQUE,DataBuffer.TYPE_BYTE),
				Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(),i_size.w,i_size.h,3,3*i_size.w,bof),d,null),
				true,null);
			}
			break;
		case NyARBufferType.BYTE1D_B8G8R8_24:{
			byte[] b=new byte[i_size.w*i_size.h*3];
			DataBufferByte d=new DataBufferByte(b,b.length);
			int[] bof={2,1,0};
			bfi=new BufferedImage(
				new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),false,true,Transparency.OPAQUE,DataBuffer.TYPE_BYTE),
				Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(),i_size.w,i_size.h,3,3*i_size.w,bof),d,null),
				true,null);
			}
			break;
		case NyARBufferType.INT1D_X8R8G8B8_32:{
			int[] b=new int[i_size.w*i_size.h];
			DataBufferInt d=new DataBufferInt(b,b.length);
			int[] msk={0xff0000,0x00ff00,0x0000ff};
			bfi=new BufferedImage(
				new DirectColorModel(24,msk[0],msk[1],msk[2]),
				Raster.createWritableRaster(new SinglePixelPackedSampleModel(d.getDataType(),i_size.w,i_size.h,msk),d,null),
				true,null);
			}
			break;
		default:
			throw new NyARRuntimeException();
		}
		return bfi;
	}	
	/**
	 * この関数は、BufferedImageを分析して、WriterbleRasterと互換性のあるNyARBufferTypeを調べます。
	 * @param im
	 * @return
	 */
	private static int getCompatibleRasterType(BufferedImage im)
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
	
	
	
	/**
	 * i_rasterにi_bmiをラップさせます。
	 * @param i_ref_bmi
	 * @return
	 */
	private static void callWrapBuffer(INyARRgbRaster i_raster,BufferedImage i_bmi)
	{
		if(i_raster.isEqualBufferType(NyARBufferType.OBJECT_Java_BufferedImage)){
			i_raster.wrapBuffer(i_bmi);
			return;
		}
		switch(i_bmi.getRaster().getDataBuffer().getDataType()){
		case DataBuffer.TYPE_BYTE:
			i_raster.wrapBuffer(((DataBufferByte)(i_bmi.getRaster().getDataBuffer())).getData());
			break;
		case DataBuffer.TYPE_INT:
			i_raster.wrapBuffer(((DataBufferInt)(i_bmi.getRaster().getDataBuffer())).getData());
			break;
		default:
			throw new NyARRuntimeException();
		}
	}	
	/**
	 * この関数は、ラップしているBufferedImageを返します。
	 * @return
	 * ラップしているBufferedImageの実体
	 */
	final public BufferedImage getBufferedImage()
	{
		return this._buffered_image;
	}
	/**
	 * ラップしているビットマップイメージを交換します。
	 * @param i_img
	 * 現在ラップしているビットマップと同一の画素形式かつ同一サイズである必要があります。
	 */
	public final void wrapBufferedImage(BufferedImage i_img)
	{
		assert !this._is_attached_buffer;
		//互換性のあるラスタタイプ?(サイズと形式をチェック)
		if(!this._raster.isEqualBufferType(getCompatibleRasterType(i_img))|| !this._raster.getSize().isEqualSize(i_img.getWidth(),i_img.getHeight())){
			throw new NyARRuntimeException();
		}
		this._buffered_image=i_img;
		callWrapBuffer(this._raster,this._buffered_image);
	}	
	/**
	 * この関数は、BufferedImageのGraphicsを返します。
	 * @return
	 * ラップしているBufferedImageのGraphicsオブジェクト
	 */
	final public Graphics getGraphics()
	{
		return this._buffered_image.getGraphics();
	}
	/**
	 * このクラスのgetBufferはバインドされているBufferedImageと記憶域を共有するラスタのバッファです。
	 */
	@Override
	final public Object getBuffer()
	{
		return this._raster.getBuffer();
	}
	/**
	 * このクラスのgetBufferTypeはバインドされているBufferedImageと記憶域を共有するラスタのバッファ形式です。
	 */
	@Override
	final public int getBufferType()
	{
		return this._raster.getBufferType();
	}
	/**
	 * この関数は使用できません。
	 * {@link BufferedImage}をセットするには、{@link #wrapImage}を使用してください。
	 */	
	@Override
	final public void wrapBuffer(Object i_ref_buf)
	{
		throw new NyARRuntimeException();
	}
	@Override
	final public int[] getPixel(int i_x, int i_y, int[] i_rgb) {
		return this._raster.getPixel(i_x, i_y, i_rgb);
	}
	@Override
	final public int[] getPixelSet(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
	{
		return this._raster.getPixelSet(i_x, i_y, i_num, i_intrgb);
	}
	@Override
	final public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
	{
		this._raster.setPixel(i_x, i_y,i_r,i_g,i_b);
	}
	@Override
	final public void setPixel(int i_x, int i_y, int[] i_rgb) {
		this._raster.setPixel(i_x, i_y, i_rgb);
	}
	@Override
	final public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) {
		this._raster.setPixels(i_x, i_y, i_num, i_intrgb);
	}
}



//
//ラスタドライバ
//


/**
 * このクラスはBufferedImage用の低速なPixelReaderです。
 * 外部の{@link BufferedImage}をラップします。
 * 形式に依存しない代わりに、ピクセルへのアクセス速度が極端に低速です。
 */
final class NyARRgbRaster_OBJECT_Java_BufferedImage extends NyARRgbRaster
{
	protected NyARRgbRaster_OBJECT_Java_BufferedImage(int i_width,int i_height)
	{
		super(i_width,i_height,false);
		this._buf=null;
	}
	protected BufferedImage _buf;

	@Override
	public int[] getPixel(int i_x, int i_y, int[] o_rgb)
	{
		int p=this._buf.getRGB(i_x, i_y);
		o_rgb[0] = ((p>>16) & 0xff);// R
		o_rgb[1] = ((p>>8) & 0xff);// G
		o_rgb[2] = (p & 0xff);// B
		return o_rgb;
	}
	@Override
	public int[] getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
		for (int i = i_num - 1; i >= 0; i--) {
			int p=this._buf.getRGB(i_x[i],i_y[i]);
			o_rgb[i * 3 +0] = ((p>>16) & 0xff);// R
			o_rgb[i * 3 +1] = ((p>>8) & 0xff);// G
			o_rgb[i * 3 +2] = (p & 0xff);// B			
		}
		return o_rgb;
	}
	@Override
	public void setPixel(int i_x, int i_y, int[] i_rgb)
	{
		this._buf.setRGB(i_x, i_y, ((i_rgb[0]<<16)&0xff0000)|((i_rgb[1]<<8)&0x00ff00)|((i_rgb[2])&0x0000ff));
	}
	@Override
	public void setPixel(int i_x, int i_y, int i_r,int i_g,int i_b)
	{
		this._buf.setRGB(i_x, i_y, ((i_r<<16)&0xff0000)|((i_g<<8)&0x00ff00)|(i_b&0x0000ff));
	}
	@Override
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
	{
		NyARRuntimeException.notImplement();		
	}
	@Override
	public Object getBuffer()
	{
		return this._buf;
	}
	@Override
	public int getBufferType()
	{
		return NyARBufferType.OBJECT_Java_BufferedImage;
	}
	@Override
	public void wrapBuffer(Object i_ref_buf)
	{
		assert (!this._is_attached_buffer);// バッファがアタッチされていたら機能しない。
		this._buf=(BufferedImage) i_ref_buf;
	}
}
