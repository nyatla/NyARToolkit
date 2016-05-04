package jp.nyatla.nyartoolkit.test;

import java.io.File;
import java.io.FileInputStream;


import jp.nyatla.nyartoolkit.base.attoolkit5.ARParamLT;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.KpmHandle;

import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FreakKeypointMatching;
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
			String fset3file="../Data/testcase/pinball.fset3";
			//カメラパラメータ
			NyARParam param=NyARParam.loadFromARParamFile(new FileInputStream(cparam_file),640,480,NyARParam.DISTFACTOR_LT_ARTK5);
			
			INyARGrayscaleRaster gs=NyARGrayscaleRaster.createInstance(640,480);
			//試験画像の準備
			{
				INyARRgbRaster rgb=NyARRgbRaster.createInstance(640,480,NyARBufferType.BYTE1D_B8G8R8X8_32);
				FileInputStream fs = new FileInputStream(img_file);
				fs.read((byte[])rgb.getBuffer());
				INyARRgb2GsFilterRgbAve filter=(INyARRgb2GsFilterRgbAve) rgb.createInterface(INyARRgb2GsFilterRgbAve.class);
				filter.convert(gs);				
			}
			NyARDoubleMatrix44 tmat=new NyARDoubleMatrix44();
			NyARNftFreakFsetFile f = NyARNftFreakFsetFile.loadFromfset3File(new FileInputStream(new File(fset3file)));
//			KpmHandle kpm=new KpmHandle(new ARParamLT(param));
			long st;
			st=System.currentTimeMillis();
			FreakKeypointMatching kpm=new FreakKeypointMatching(param);
			KeyframeMap keymap=new KeyframeMap(f,0);
			System.out.println(System.currentTimeMillis()-st);
			for(int j=0;j<4;j++){
				st=System.currentTimeMillis();
			for(int i=0;i<20;i++){
				kpm.updateInputImage(gs);
				kpm.updateFeatureSet();
				kpm.kpmMatching(keymap,tmat);
			}
			//FreakKeypointMatching#kMaxNumFeaturesを300にしてテストして。
			System.out.println("Total="+(System.currentTimeMillis()-st));
			NyARDoubleMatrix44 TEST_PATT=new NyARDoubleMatrix44(new double[]{	0.9843635410774265,0.006676891783837065,-0.17602226595996517,-191.17967199668533,
					0.011597578022657571,-0.9995697471256431,0.02694098764508235,63.00280574839347,
					-0.17576664981496215,-0.028561157958401542,-0.9840174516078957	,611.7587155355864,
					0,0,0,1});
				System.out.println(TEST_PATT.equals(tmat));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
class TestPatt
{
	
}
