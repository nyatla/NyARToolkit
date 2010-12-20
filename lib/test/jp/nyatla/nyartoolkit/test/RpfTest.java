package jp.nyatla.nyartoolkit.test;


import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARReality;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource_Reference;






/**
 * NyARRealityのテストプログラム。動作保証なし。
 * 
 * ターゲットプロパティの取得実験用のテストコードです。
 * クリックしたマーカや、その平面周辺から、画像を取得するテストができます。
 *
 */

public class RpfTest
{
	private final static String PARAM_FILE = "../Data/camera_para.dat";
	private final static String DATA_FILE = "../Data/320x240ABGR.raw";
	private static final long serialVersionUID = -2110888320986446576L;

    
	public static void main(String[] args)
	{

		try {
			NyARParam param=new NyARParam();
			param.loadARParamFromFile(PARAM_FILE);
			param.changeScreenSize(320,240);
			NyARReality reality=new NyARReality(param.getScreenSize(),10,1000,param.getPerspectiveProjectionMatrix(),null,10,10);
			NyARRealitySource reality_in=new NyARRealitySource_Reference(320,240,null,2,100,NyARBufferType.BYTE1D_B8G8R8X8_32);
			FileInputStream fs = new FileInputStream(DATA_FILE);
			fs.read((byte[])reality_in.refRgbSource().getBuffer());
			Date d2 = new Date();
			for(int i=0;i<1000;i++){
				reality.progress(reality_in);
			}
			Date d = new Date();
			System.out.println(d.getTime() - d2.getTime()+"ms");
			
			System.out.println(reality.getNumberOfKnown());
			System.out.println(reality.getNumberOfUnknown());
			System.out.println(reality.getNumberOfDead());
			NyARRealityTarget rt[]=new NyARRealityTarget[10];
			reality.selectUnKnownTargets(rt);
			reality.changeTargetToKnown(rt[0],2,80);
			System.out.println(rt[0]._transform_matrix.m00+","+rt[0]._transform_matrix.m01+","+rt[0]._transform_matrix.m02+","+rt[0]._transform_matrix.m03);
			System.out.println(rt[0]._transform_matrix.m10+","+rt[0]._transform_matrix.m11+","+rt[0]._transform_matrix.m12+","+rt[0]._transform_matrix.m13);
			System.out.println(rt[0]._transform_matrix.m20+","+rt[0]._transform_matrix.m21+","+rt[0]._transform_matrix.m22+","+rt[0]._transform_matrix.m23);
			System.out.println(rt[0]._transform_matrix.m30+","+rt[0]._transform_matrix.m31+","+rt[0]._transform_matrix.m32+","+rt[0]._transform_matrix.m33);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

