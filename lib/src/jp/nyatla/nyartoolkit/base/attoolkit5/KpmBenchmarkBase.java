/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.base.attoolkit5;

import java.io.File;
import java.io.FileInputStream;


import jp.nyatla.nyartoolkit.base.attoolkit5.ARParamLT;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.KpmHandle;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.rgb2gs.INyARRgb2GsFilterRgbAve;

import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;



public class KpmBenchmarkBase
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
			long st;
			st=System.currentTimeMillis();
			KpmHandle kpm=new KpmHandle(new ARParamLT(param));
			kpm.kpmSetRefDataSet(f);
			System.out.println(System.currentTimeMillis()-st);
			for(int j=0;j<10;j++){
				st=System.currentTimeMillis();
			for(int i=0;i<10;i++){
				kpm.kpmMatching(gs);
			}
			System.out.println(System.currentTimeMillis()-st);
			}
			NyARDoubleMatrix44 TEST_PATT=new NyARDoubleMatrix44(new double[]{	0.9843635410774265,0.006676891783837065,-0.17602226595996517,-191.17967199668533,
				0.011597578022657571,-0.9995697471256431,0.02694098764508235,63.00280574839347,
				-0.17576664981496215,-0.028561157958401542,-0.9840174516078957	,611.7587155355864,
				0,0,0,1});
			System.out.println(TEST_PATT.equals(kpm.result[0].camPose));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
class TestPatt
{
	
}
