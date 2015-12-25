package jp.nyatla.nyartoolkit.pro.sample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilterRgbAve;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.pro.core.NyARProVersion;
import jp.nyatla.nyartoolkit.pro.core.integralimage.NyARIntegralImage;
import jp.nyatla.nyartoolkit.pro.core.kpm.NyARKpmDataSet;
import jp.nyatla.nyartoolkit.pro.core.kpm.NyARSingleKpm;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch.ResultPtr;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.INyARDefocusFilter;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.NyARDefocusFilterFactory;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARSurfaceDataSet;
import jp.nyatla.nyartoolkit.pro.core.transmat.NyARNftTransMatUtils;


public class KpmBenchmark
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			String img_file="../Data/nft/nftimg.raw";
			String cparam_file="../Data/camera_para4.dat";
			String fset2="../Data/nft/pinball/pinball.fset2";
			//カメラパラメータ
			NyARParam param=NyARParam.createFromARParamFile(new FileInputStream(cparam_file));
			param.changeScreenSize(640,480);
			
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(640,480);
			//試験画像の準備
			{
				NyARRgbRaster rgb=new NyARRgbRaster(640,480,NyARBufferType.BYTE1D_B8G8R8_24);
				FileInputStream fs = new FileInputStream(img_file);
				fs.read((byte[])rgb.getBuffer());
				INyARRgb2GsFilterRgbAve filter=(INyARRgb2GsFilterRgbAve) rgb.createInterface(INyARRgb2GsFilterRgbAve.class);
				filter.convert(gs);				
			}
			//TransmatUtilsの生成
			NyARNftTransMatUtils tu=new NyARNftTransMatUtils(param,200);
			//RDSの生成
			NyARKpmDataSet kpm_rds=NyARKpmDataSet.loadFromFset2(new FileInputStream(fset2));
			//KPMの生成
			NyARSingleKpm kpm=new NyARSingleKpm(param,kpm_rds);
			NyARDoubleMatrix44 ret=new NyARDoubleMatrix44();
			//KPMの試験
			NyARSurfAnnMatch.ResultPtr o_result=new NyARSurfAnnMatch.ResultPtr(200);
			kpm.updateMatching(gs);	
			kpm.getRansacMatchPoints(NyARSingleKpm.AREA_ALL, o_result);
			tu.kpmTransmat(o_result, ret);
			if(
					ret.m00==-0.5772596908983827 && ret.m01==-0.8148710939357383 &&  ret.m02==-0.05250094791413518 && ret.m03==153.30336171065707 &&
					ret.m10==-0.5962941544207391 && ret.m11==0.46459736642982447 &&  ret.m12==-0.6546621789214073 && ret.m13==-31.30269180746153 &&
					ret.m20==0.5578570880320154  && ret.m21==-0.34660407870428267 &&  ret.m22==-0.7540962020577963 && ret.m23==314.9515261631174)
			{
				System.out.println("KPM result check[OK]");
			}
			// マーカーを検出
			for(int y=0;y<10;y++){
			Date d3 = new Date();
			for(int i=0;i<10;i++){
				kpm.updateMatching(gs);	
				kpm.getRansacMatchPoints(NyARSingleKpm.AREA_ALL, o_result);
				tu.kpmTransmat(o_result, ret);
			}
			Date d4 = new Date();
			System.out.println("KPM="+(d4.getTime() - d3.getTime())+"[ms/10frame]");
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
