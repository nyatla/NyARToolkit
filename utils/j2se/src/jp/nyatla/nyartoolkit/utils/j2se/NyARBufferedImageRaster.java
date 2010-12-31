package jp.nyatla.nyartoolkit.utils.j2se;

import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARRgbPixelReader;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * バッファにBufferedImageを使用するNyARRgbRasterです。
 * ラップするbufferedImageの種類によっては、パフォーマンスが低下する事があるので注意してください。
 * @author nyatla
 *
 */
public class NyARBufferedImageRaster extends NyARRgbRaster
{
	private BufferedImage _buffered_image;
	protected boolean initInstance(NyARIntSize i_size,int i_raster_type,boolean i_is_alloc)
	{
		if(super.initInstance(i_size, i_raster_type, i_is_alloc)){
			return true;
		}
		if(i_raster_type!=NyARBufferType.OBJECT_Java_BufferedImage)
		{
			return false;
		}
		//内部バッファは作れない。
		if(i_is_alloc){
			return false;
		}
		//Readerの作成。
		this._reader=new NyARRgbPixelReader_OBJECT_Java_BufferedImage(null);
		return true;
	}

	/**
	 * NyARBufferTypeと互換性のある形式のBufferedImageを作成します。
	 * @param i_width
	 * @param i_height
	 * @param i_raster_type
	 * バッファタイプを指定します。使用できるラスタタイプは、次の種類です。
	 * NyARBufferType.BYTE1D_R8G8B8_24,NyARBufferType.BYTE1D_B8G8R8_24,NyARBufferType.INT1D_X8R8G8B8_32,NyARBufferType.OBJECT_Java_BufferedImage
	 * NyARBufferType.OBJECT_Java_BufferedImageは低速低速です。パフォーマンスが必要なシステムでは他のバッファタイプを指定してください。
	 * NyARToolKitの入力・出力に使用する場合は、関数に最適なバッファタイプを使用するとパフォーマンスが向上します。詳細は関数のドキュメントを参照してください。
	 * (NyARBufferType.OBJECT_Java_BufferedImageは全ての関数で低速であり、他の直値タイプは概ね高速に動作します。)
	 * @throws NyARException
	 */
	public NyARBufferedImageRaster(int i_width,int i_height,int i_raster_type) throws NyARException
	{
		//一旦外部参照で作る。
		super(i_width,i_height,i_raster_type,false);
		//このラスタに合致したBufferedImageを作る。
		BufferedImage ret;
		switch(i_raster_type)
		{
		case NyARBufferType.BYTE1D_R8G8B8_24:{
			byte[] b=new byte[3*i_width*i_height];
			DataBufferByte d=new DataBufferByte(b,b.length);
			int[] bof={0,1,2};
			ret=new BufferedImage(
				new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),false,true,Transparency.OPAQUE,DataBuffer.TYPE_BYTE),
				Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(),i_width,i_height,3,3*i_width,bof),d,null),
				true,null);
			}
			break;
		case NyARBufferType.BYTE1D_B8G8R8_24:{
			byte[] b=new byte[3*i_width*i_height];
			DataBufferByte d=new DataBufferByte(b,b.length);
			int[] bof={2,1,0};
			ret=new BufferedImage(
				new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),false,true,Transparency.OPAQUE,DataBuffer.TYPE_BYTE),
				Raster.createWritableRaster(new ComponentSampleModel(d.getDataType(),i_width,i_height,3,3*i_width,bof),d,null),
				true,null);
			}
			break;
		case NyARBufferType.INT1D_X8R8G8B8_32:{
			int[] b=new int[i_width*i_height];
			DataBufferInt d=new DataBufferInt(b,b.length);
			int[] msk={0xff0000,0x00ff00,0x0000ff};
			ret=new BufferedImage(
				new DirectColorModel(24,msk[0],msk[1],msk[2]),
				Raster.createWritableRaster(new SinglePixelPackedSampleModel(d.getDataType(),i_width,i_height,msk),d,null),
				true,null);
			}
			break;
		case NyARBufferType.OBJECT_Java_BufferedImage:{
			ret=new BufferedImage(i_width,i_height,BufferedImage.TYPE_INT_RGB);
			}
			break;
		default:
			//このデータタイプのラスタは作れない。
			throw new NyARException();
		}
		if(i_raster_type==NyARBufferType.OBJECT_Java_BufferedImage){
			this.wrapBuffer(ret);
			//低速インタフェイス警告
			System.out.println("NyARToolKit Warning:"+this.getClass().getName()+":Low speed interface.");
		}else{
			//rastertype毎にDatabufferの実体の取得方法を変える。
			switch(ret.getRaster().getDataBuffer().getDataType())
			{
			case DataBuffer.TYPE_BYTE:
				this.wrapBuffer(((DataBufferByte)(ret.getRaster().getDataBuffer())).getData());
				break;
			case DataBuffer.TYPE_INT:
				this.wrapBuffer(((DataBufferInt)(ret.getRaster().getDataBuffer())).getData());
				break;
			default:
				throw new NyARException();
			}
		}
		//内部参照に切り替える。
		this._is_attached_buffer=true;
		this._buffered_image=ret;
		return;
	}
	/**
	 * 既にあるbufferedImageをラップしてラスタをつくります。BufferedImageの内容により、ラスタタイプは自動的に決定します。
	 * @param i_bfi
	 * ラップするbufferedImageを設定します。インスタンスが参照するのは、このインスタンスのデフォルトバンクのイメージです。
	 * NyARToolKitと互換性が低いBufferedImageを指定すると、パフォーマンスが劣化することがあります。注意してください。
	 * @throws NyARException
	 */
	public NyARBufferedImageRaster(BufferedImage i_bfi) throws NyARException
	{
		//ラスタタイプを確定させて、一旦外部参照で作る。
		super(i_bfi.getWidth(),i_bfi.getHeight(),getRasterTypeFromBufferedImage(i_bfi),false);
		if(this.getBufferType()!=NyARBufferType.OBJECT_Java_BufferedImage)
		{
			//RawImage系のバッファが割り当てられていたら、設定。
			switch(i_bfi.getRaster().getDataBuffer().getDataType())
			{
			case DataBuffer.TYPE_BYTE:
				this.wrapBuffer(((DataBufferByte)(i_bfi.getRaster().getDataBuffer())).getData());
				break;
			case DataBuffer.TYPE_INT:
				this.wrapBuffer(((DataBufferInt)(i_bfi.getRaster().getDataBuffer())).getData());
				break;
			default:
				throw new NyARException();
			}
		}else{
			this.wrapBuffer(i_bfi);
			//低速インタフェイス警告
			System.out.println("NyARToolKit Warning:"+this.getClass().getName()+":Low speed interface.");
		}
		//内部参照に切り替える。
		this._is_attached_buffer=true;
		this._buffered_image=i_bfi;
		return;
	}
	/**
	 * BufferedImageを返します。
	 * @return
	 */
	public final BufferedImage getBufferedImage()
	{
		return this._buffered_image;
	}
	/**
	 * BufferedImageのGraphicsを返します。
	 * @return
	 */
	public final Graphics getGraphics()
	{
		return this._buffered_image.getGraphics();
	}

	/**
	 * BufferedImageを分析して、WriterbleRasterと互換性のあるNyARBufferTypeを調べます。
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
				if(band_offset[0]==3 && band_offset[1]==2 && band_offset[2]==1)
				{
					return NyARBufferType.BYTE1D_B8G8R8X8_32;
				}
			}
			
		}else if(sp instanceof SinglePixelPackedSampleModel)
		{
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


/**
 * BufferedImage用の低速PixelReader
 * @author nyatla
 *
 */
final class NyARRgbPixelReader_OBJECT_Java_BufferedImage implements INyARRgbPixelReader
{
	protected BufferedImage _ref_buf;
	public NyARRgbPixelReader_OBJECT_Java_BufferedImage(BufferedImage _ref_buf)
	{
		this._ref_buf = _ref_buf;
	}

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
	public void switchBuffer(Object i_ref_buffer) throws NyARException
	{
		this._ref_buf=(BufferedImage)i_ref_buffer;
	}	
}