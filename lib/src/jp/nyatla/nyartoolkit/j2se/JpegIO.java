package jp.nyatla.nyartoolkit.j2se;

import java.awt.image.BufferedImage;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;

import org.w3c.dom.Element;

import com.sun.imageio.plugins.jpeg.JPEGImageReader;
import com.sun.imageio.plugins.jpeg.JPEGImageWriter;

/**
 * プラットフォーム依存のJpegデータIOを実装します。
 *
 */
public class JpegIO {
	public static class DecodeResult{
		final public int x_density;
		final public int y_density;
		final public int density_unit;
		final public byte[] img;
		final public int width;
		final public int height;
		public DecodeResult(int i_xd,int i_yd,byte[] i_img,int w,int h,int i_unit)
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
		int xd=Integer.parseInt(jfif.getAttribute("Xdensity"));
		int yd=Integer.parseInt(jfif.getAttribute("Ydensity"));
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
	/**
	 * http://stackoverflow.com/questions/233504/write-dpi-metadata-to-a-jpeg-image-in-java
	 * @param w
	 * @param h
	 * @param i_x_dpi
	 * @param i_y_dpi
	 * @param i_dpi_unit
	 * @param i_src
	 * @param i_quority
	 * @return
	 * @throws IOException
	 */
	public static byte[] encode(int w,int h,int i_x_dpi,int i_y_dpi,int i_dpi_unit,byte[] i_src,float i_quority) throws IOException
	{
		BufferedImage img = new BufferedImage(w ,h ,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr=img.getRaster();
		DataBufferByte buf=(DataBufferByte)wr.getDataBuffer();
		System.arraycopy(i_src, 0, buf.getData(),0,w*h);

		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		JPEGImageWriter jw=(JPEGImageWriter)ImageIO.getImageWritersBySuffix("jpeg").next();
		jw.setOutput(new MemoryCacheImageOutputStream(bout));

		// Compression
		JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) jw.getDefaultWriteParam();
		jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
		jpegParams.setCompressionQuality(i_quority);

		// Metadata (dpi)

		IIOMetadata data = jw.getDefaultImageMetadata(new ImageTypeSpecifier(img), jpegParams);
		Element tree = (Element)data.getAsTree("javax_imageio_jpeg_image_1.0");
		Element jfif = (Element)tree.getElementsByTagName("app0JFIF").item(0);
		jfif.setAttribute("Xdensity", Integer.toString(i_x_dpi));
		jfif.setAttribute("Ydensity", Integer.toString(i_y_dpi));
		jfif.setAttribute("resUnits", Integer.toString(i_dpi_unit)); // density is dots per inch
		data.setFromTree("javax_imageio_jpeg_image_1.0",tree);

        // Write and clean up
		jw.write(null,  new IIOImage(img, null, data), jpegParams);
		byte[] ret=bout.toByteArray();
		bout.close();
		jw.dispose();
		return ret;
	}
}
