package jp.nyatla.nyartoolkit.j2se;

import java.awt.image.BufferedImage;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;

import java.io.ByteArrayInputStream;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.MemoryCacheImageInputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;

import org.w3c.dom.Element;

import com.sun.imageio.plugins.jpeg.JPEGImageReader;

/**
 * プラットフォーム依存のJpegデータIOを実装します。
 *
 */
public class JpegIO {
	public static class DecodeResult{
		final public double x_density;
		final public double y_density;
		final public int density_unit;
		final public byte[] img;
		final public int width;
		final public int height;
		protected DecodeResult(double i_xd,double i_yd,byte[] i_img,int w,int h,int i_unit)
		{
			this.height=h;
			this.width=w;
			this.img=i_img;
			this.x_density=i_xd;
			this.y_density=i_yd;
			this.density_unit=i_unit;
		}
	}
	public static DecodeResult decode(byte[] i_src) throws IOException
	{
		JPEGImageReader jr=(JPEGImageReader)ImageIO.getImageReadersBySuffix("jpeg").next();
		jr.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(i_src)));
		IIOMetadata data = jr.getImageMetadata(0);
		Element tree = (Element)data.getAsTree("javax_imageio_jpeg_image_1.0");
		Element jfif = (Element)tree.getElementsByTagName("app0JFIF").item(0);
		double xd=Double.parseDouble(jfif.getAttribute("Xdensity"));
		double yd=Double.parseDouble(jfif.getAttribute("Ydensity"));
		int unit=Integer.parseInt(jfif.getAttribute("resUnits"));
		BufferedImage b=jr.read(0);
		jr.dispose();//dispose

		//カラーモデルの分析
		SampleModel sp=b.getSampleModel();
		if(sp instanceof PixelInterleavedSampleModel){
			PixelInterleavedSampleModel ssp=(PixelInterleavedSampleModel)sp;
			int data_type=ssp.getDataType();
			int scan_stride=ssp.getScanlineStride();
			if(data_type==DataBuffer.TYPE_BYTE && scan_stride==b.getWidth()){
				DataBufferByte bb=(DataBufferByte)b.getData().getDataBuffer();
				return new DecodeResult(xd,yd,bb.getData(),b.getWidth(),b.getHeight(),unit);
			}else{
				//nothing todo
			}
		}else{
			//nothing todo
		}
		throw new NyARRuntimeException();
	}
}
