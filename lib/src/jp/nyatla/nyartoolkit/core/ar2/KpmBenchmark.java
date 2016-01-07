package jp.nyatla.nyartoolkit.core.ar2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;


import jp.nyatla.nyartoolkit.core.kpm.ARParamLT;
import jp.nyatla.nyartoolkit.core.kpm.KpmHandle;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.rgb2gs.INyARRgb2GsFilterRgbAve;

import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;



public class KpmBenchmark
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			String img_file="../Data/testcase/test.raw";
			String cparam_file=	"../Data/testcase/camera_para5.dat";
			String fsetfile="../Data/testcase/pinball.fset";
			String fset3file="../Data/testcase/pinball.fset3";
			String isetfile="../Data/testcase/pinball.iset5";
			//カメラパラメータ
			NyARParam param=NyARParam.loadFromARParamFile(new FileInputStream(cparam_file),640,480,NyARParam.DISTFACTOR_RAW);
			
			INyARGrayscaleRaster gs=NyARGrayscaleRaster.createInstance(640,480);
			//試験画像の準備
			{
				INyARRgbRaster rgb=NyARRgbRaster.createInstance(640,480,NyARBufferType.BYTE1D_B8G8R8X8_32);
				FileInputStream fs = new FileInputStream(img_file);
				fs.read((byte[])rgb.getBuffer());
				INyARRgb2GsFilterRgbAve filter=(INyARRgb2GsFilterRgbAve) rgb.createInterface(INyARRgb2GsFilterRgbAve.class);
				filter.convert(gs);				
			}
			NyARNftFreakFsetFile f = NyARNftFreakFsetFile.loadFromfset3File(new FileInputStream(new File(fset3file)));
			KpmHandle kpm=new KpmHandle(new ARParamLT(param));
			kpm.kpmSetRefDataSet(f);
			kpm.kpmMatching(gs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
class TestPatt
{
	
}
