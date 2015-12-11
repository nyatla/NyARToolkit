package jp.nyatla.nyartoolkit.core.ar2;

import java.io.FileInputStream;
import java.util.Date;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.rgb2gs.INyARRgb2GsFilterRgbAve;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;



public class AR2Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			String img_file="../Data/test.raw";
			String cparam="../Data/camera_para5.dat";
			String fsetfile="../Data/pinball.fset";
			String isetfile="../Data/pinball.iset5";
			//カメラパラメータ
			NyARParam param=NyARParam.createFromARParamFile(new FileInputStream(cparam));
			param.changeScreenSize(640,480);
			NyARDoublePoint2d d=new NyARDoublePoint2d();
			param.getDistortionFactor().ideal2Observ(100,100, d);
			param.getDistortionFactor().observ2Ideal(100,100, d);
			
			NyARGrayscaleRaster gs=NyARGrayscaleRaster.createInstance(640,480);
			//試験画像の準備
			{
				INyARRgbRaster rgb=NyARRgbRaster.createInstance(640,480,NyARBufferType.BYTE1D_B8G8R8X8_32);
				FileInputStream fs = new FileInputStream(img_file);
				fs.read((byte[])rgb.getBuffer());
				INyARRgb2GsFilterRgbAve filter=(INyARRgb2GsFilterRgbAve) rgb.createInterface(INyARRgb2GsFilterRgbAve.class);
				filter.convert(gs);			
			}
			NyARDoubleMatrix44 ret=new NyARDoubleMatrix44();
			NyARNftFsetFile fset=NyARNftFsetFile.loadFromFsetFile(new FileInputStream(fsetfile));
			NyARNftIsetFile iset=NyARNftIsetFile.loadFromIsetFile(new FileInputStream(isetfile));
			AR2Handle tracking=new AR2Handle(param,iset,fset);
			tracking.ar2SetTrackingThresh(5.0);
			tracking.ar2SetSimThresh(0.50);
			tracking.ar2SetSearchFeatureNum(16);
			tracking.ar2SetSearchSize(12);
			tracking.ar2SetTemplateSize1(6);
			tracking.ar2SetTemplateSize2(6);
			//validation test
			{
				tracking.setInitialTransmat(TestPattAR2.ar2Tracking2d_trans());
				ret.setValue(TestPattAR2.ar2Tracking2d_trans());
				tracking.ar2Tracking(gs, ret);
				System.out.println(1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
class TestPattAR2
{
	public static NyARDoubleMatrix44 ar2Tracking2d_trans()
	{
		NyARDoubleMatrix44 ret=new NyARDoubleMatrix44();
		ret.m00=0.984363556;
		ret.m01=0.00667689135;
		ret.m02=-0.176022261;
		ret.m03=-191.179672;

		ret.m10=0.0115975942;
		ret.m11=-0.999569774;
		ret.m12=0.0269410834;
		ret.m13=63.0028076;

		ret.m20=-0.175766647;
		ret.m21=-0.0285612550;
		ret.m22=-0.984017432;
		ret.m23=611.758728;
		
		ret.m30=0;
		ret.m31=0;
		ret.m32=0;
		ret.m33=1;
		return ret;
	}	
	public static NyARDoubleMatrix44 ar2Tracking2d_trans2()
	{
		NyARDoubleMatrix44 ret=new NyARDoubleMatrix44();
		ret.m00=-0.57428467373793857;
		ret.m01=-0.81758351060399137;
		ret.m02=-0.041884563960663847;
		ret.m03=153.13901584837785;

		ret.m10=-0.61372743931962814;
		ret.m11=0.46382384999455162;
		ret.m12=-0.63891006127810923;
		ret.m13=-29.730875975660652;

		ret.m20=0.54178939057154141;
		ret.m21=-0.34121054990237981;
		ret.m22=-0.76814036275893471;
		ret.m23=321.11397159739073;
		
		ret.m30=0;
		ret.m31=0;
		ret.m32=0;
		ret.m33=1;
		return ret;

	}		
}
