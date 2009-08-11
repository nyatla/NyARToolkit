package jp.nyatla.utils.j2se;

import java.awt.Color;
import java.awt.image.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;

public class BufferedImageSink extends BufferedImage
{
	public BufferedImageSink(int i_width,int i_height)
	{
		super(i_width,i_height,TYPE_INT_RGB);
	}
	/**
	 * i_inの内容を、このイメージにコピーします。
	 * @param i_in
	 * @throws NyARException
	 */
	public void copyFromRaster(INyARRgbRaster i_in) throws NyARException
	{
		assert i_in.getSize().isEqualSize(this.getWidth(), this.getHeight());
		
		//thisへ転写
		INyARRgbPixelReader reader=i_in.getRgbPixelReader();
		int[] rgb=new int[3];

		for(int y=this.getHeight()-1;y>=0;y--){
			for(int x=this.getWidth()-1;x>=0;x--){
				reader.getPixel(x,y,rgb);
				this.setRGB(x,y,(rgb[0]<<16)|(rgb[1]<<8)|rgb[2]);
			}
		}
		return;
	}
	/**
	 * BIN_8用
	 * @param i_in
	 * @throws NyARException
	 */
	public void copyFromRaster(INyARRaster i_in) throws NyARException
	{
		assert i_in.getSize().isEqualSize(this.getWidth(), this.getHeight());
		if(i_in.getBufferReader().isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT1D_BIN_8))
		{
			final int[] buf=(int[])i_in.getBufferReader().getBuffer();
			final int w=this.getWidth();
			final int h=this.getHeight();
			for(int y=h-1;y>=0;y--){
				for(int x=w-1;x>=0;x--){
					this.setRGB(x, y,buf[x+y*w]==0?0:0xffffff);
				}
			}
		}
		return;
	}	
	/**
	 * i_outへこのイメージを出力します。
	 * 
	 * @param i_out
	 * @throws NyARException
	 */
	public void copyToRaster(INyARRgbRaster i_out) throws NyARException
	{
		assert i_out.getSize().isEqualSize(this.getWidth(), this.getHeight());
		
		//thisへ転写
		INyARRgbPixelReader reader=i_out.getRgbPixelReader();
		int[] rgb=new int[3];
		for(int y=this.getHeight()-1;y>=0;y--){
			for(int x=this.getWidth()-1;x>=0;x--){
				int pix=this.getRGB(x, y);
				rgb[0]=(pix>>16)&0xff;
				rgb[1]=(pix>>8)&0xff;
				rgb[2]=(pix)&0xff;
				reader.setPixel(x,y,rgb);
			}
		}
		return;
	}
}
