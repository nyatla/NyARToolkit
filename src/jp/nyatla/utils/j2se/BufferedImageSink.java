package jp.nyatla.utils.j2se;

import java.awt.image.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;

public class BufferedImageSink extends BufferedImage
{
	public BufferedImageSink(int i_width,int i_height)
	{
		super(i_width,i_height,TYPE_INT_RGB);
	}
	public void sinkFromRaster(INyARRgbRaster i_in) throws NyARException
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
}
