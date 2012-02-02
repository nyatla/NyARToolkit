package jp.nyatla.nyartoolkit.test;

import java.io.File;
import java.io.FileInputStream;

import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BGRA;

public class ClassTestcase
{
	private final static String data_file = "../Data/320x240ABGR.raw";
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try{
			// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
			File f = new File(data_file);
			FileInputStream fs = new FileInputStream(data_file);
			byte[] buf = new byte[(int) f.length()];
			fs.read(buf);
			INyARRgbRaster ra = new NyARRgbRaster_BGRA(320, 240,false);
			ra.wrapBuffer(buf);
			// TODO Auto-generated method stub
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
