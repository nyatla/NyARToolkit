package jp.nyatla.nyartoolkit.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markerar.*;

class NyARMarkerSystemConfig
{
	
}

public class ClassTestcase
{
	private final static String data_file = "../Data/320x240ABGR.raw";
	private final static String code_file = "../Data/patt.hiro";
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
			NyARRgbRaster ra = new NyARRgbRaster(320, 240,NyARBufferType.BYTE1D_B8G8R8X8_32,false);
			ra.wrapBuffer(buf);
			//ARParamの生成
			NyARParam ap = new NyARParam();
			ap.loadDefaultParameter();
			ap.changeScreenSize(320, 240);
			NyARSensor sensor=new NyARSensor(ap);
			//マーカシステムの起動
			NyARMarkerSystem s=new NyARMarkerSystem(ap);
			int aid=s.addARMarker(new FileInputStream(code_file),16,25,80);
			sensor.update(ra);
			s.update(sensor);
			Date d2 = new Date();
			for(int i=0;i<1000;i++){
				sensor.update(ra);
				s.update(sensor);
			}
			Date d = new Date();
			System.out.println(d.getTime() - d2.getTime());

			if(s.isExistMarker(aid)){
				NyARDoubleMatrix44 mat=s.getMarkerMatrix(aid);
				System.out.println(s.getConfidence(aid));
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
