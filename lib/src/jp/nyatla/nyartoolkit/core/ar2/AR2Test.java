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
		NyARDoubleMatrix44 DEST_MAT=new NyARDoubleMatrix44(
				new double[]{
				0.983216579802738	,0.004789670338920735	,-0.182379395452632	,-190.59060778155634,
				0.012860128650301084,-0.9989882776886819	,0.04309405286235391,64.04490608650205,
				-0.18198846949444153,-0.04471620834836307	,-0.9822833723761636,616.6427501051592,
				0,0,0,1});
		NyARDoubleMatrix44 SRC_MAT=new NyARDoubleMatrix44(new double[]{
			0.984363556,	0.00667689135,	-0.176022261,	-191.179672,
			0.0115975942,	-0.999569774,	0.0269410834,	63.0028076,
			-0.175766647,	-0.0285612550,	-0.984017432,	611.758728,
			0,0,0,1});

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
				tracking.setInitialTransmat(SRC_MAT);
				ret.setValue(SRC_MAT);
				tracking.ar2Tracking(gs, ret);
				System.out.println(ret.equals(DEST_MAT));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
