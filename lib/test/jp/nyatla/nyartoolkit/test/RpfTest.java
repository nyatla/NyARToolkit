package jp.nyatla.nyartoolkit.test;


import java.io.FileInputStream;
import java.util.Date;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARReality;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource_Reference;






/**
 * このサンプルは、{@link NyARReality}クラスの動作チェック＆ベンチマークプログラムです。
 * 静止画からからUnknownステータスのマーカを検出して、Knownステータスに遷移できるかと、
 * {@link NyARReality}が正常に動作するかを確認できます。
 * また、{@link NyARReality#progress}を1000回実行して、処理時間を計測します。
 * この数値は、{@link NyARReality}の基本性能の指標として使うことができます。
 * <p>必要なファイル - 
 * このプログラムの実行には、以下の外部ファイルが必要です。
 * <ul>
 * <li>camera_para.dat - ARToolKit付属のカメラパラメータファイル
 * <li>320x240ABGR.raw　- Hiroマーカを撮影した、QVGAサイズのXBGR形式のサンプル画像
 * </ul>
 * </p>
 */
public class RpfTest
{
	private final static String PARAM_FILE = "../Data/camera_para.dat";
	private final static String DATA_FILE = "../Data/320x240ABGR.raw";
	private static final long serialVersionUID = -2110888320986446576L;

    /**
     * メイン関数です。
     * 次のフローで処理を実行します。
     * <ol>
     * <li>{@link NyARParam}にカメラパラメタを読み込み。QVGAサイズに再設定。
     * <li>{@link NyARReality}オブジェクトの生成。歪み補正はなし。
     * <li>{@link NyARRealitySource_Reference}オブジェクトの生成。フォーマットは、{@link NyARBufferType#BYTE1D_B8G8R8X8_32}
     * <li>{@link NyARRealitySource_Reference}オブジェクトに、{@link #DATA_FILE}の内容を書き込み。
     * <li>{@link NyARReality#progress}を1000回実行。処理時間を出力。
     * <li>0番目のUnknowntargetをKnownターゲットにして、姿勢行列を計算して表示。
     * </ol>
     * @param args
     * 引数は必要ありません。
     */
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

