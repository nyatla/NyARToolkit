/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import jp.nyatla.nyartoolkit.core.marker.artk.NyARCode;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.detector.NyARSingleDetectMarker;

/**
 * このサンプルは、{@link NyARSingleDetectMarker}クラスの動作チェック＆ベンチマークプログラムです。
 * 静止画から1個のHiroマーカを認識して、その姿勢変換行列、パターン一致率を得る動作を確認できます。
 * 同時に、この処理を1000回実行して、処理時間を計測します。この数値は、NyARToolkitの基本性能の
 * 指標として使うことができます。
 * <p>必要なファイル - 
 * このプログラムの実行には、以下の外部ファイルが必要です。
 * <ul>
 * <li>camera_para.dat - ARToolKit付属のカメラパラメータファイル
 * <li>patt.hiro - ARToolKit付属のHiroマーカのパターンファイル
 * <li>320x240ABGR.raw　- Hiroマーカを撮影した、QVGAサイズのXBGR形式のサンプル画像
 * </ul>
 */
public class RawFileTest
{
	private final String code_file = "../Data/patt.hiro";

	private final String data_file = "../Data/320x240ABGR.raw";

	private final String camera_file = "../Data/camera_para4.dat";

	/**
	 * コンストラクタです。
	 * ここでは処理を行いません。
	 */
	public RawFileTest()
	{
	}
	private static NyARDoubleMatrix44 NYAR_RESULT=new NyARDoubleMatrix44(
		new double[]{
				0.5909987641957225,0.7892874219064485,0.16657078477152182,4.626500743720311,
				0.8037047107848796,-0.558438121742177,-0.20544002055895158,0.3231728645920852,
				-0.06913174799845362,0.2552885226666906,-0.9643902589788725,145.94003247396344,
				0,0,0,1
		});
	/**
	 * この関数は、テスト関数の本体です。
	 * カメラ設定ファイル、ARマーカのパターン読出しを読み込み、試験イメージに対してマーカ検出を実行します。
	 * マーカ検出を1000回繰り返して、経過した時間をms単位で表示します。
	 * @throws Exception
	 */
	public void Test_arDetectMarkerLite() throws Exception
	{
		// AR用カメラパラメタファイルをロード
		NyARParam ap = NyARParam.loadFromARParamFile(new FileInputStream(camera_file),320,240);

		// AR用のパターンコードを読み出し
		NyARCode code = NyARCode.loadFromARPattFile(new FileInputStream(code_file),16, 16);

		// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
		File f = new File(data_file);
		FileInputStream fs = new FileInputStream(data_file);
		byte[] buf = new byte[(int) f.length()];
		fs.read(buf);
		INyARRgbRaster ra = NyARRgbRaster.createInstance(320, 240,NyARBufferType.BYTE1D_B8G8R8X8_32,false);
		ra.wrapBuffer(buf);
		// Blank_Raster ra=new Blank_Raster(320, 240);

		// １パターンのみを追跡するクラスを作成
		NyARSingleDetectMarker ar = NyARSingleDetectMarker.createInstance(
			ap, code, 80.0,NyARSingleDetectMarker.PF_NYARTOOLKIT);
		NyARDoubleMatrix44 result_mat = new NyARDoubleMatrix44();
		ar.setContinueMode(true);
		ar.detectMarkerLite(ra, 100);
		ar.getTransmat(result_mat);

		// マーカーを検出
		Date d2 = new Date();
		for (int i = 0; i < 1000; i++) {
			// 変換行列を取得
			ar.detectMarkerLite(ra, 100);
			ar.getTransmat(result_mat);
		}
		Date d = new Date();
		NyARDoublePoint3d ang=new NyARDoublePoint3d();
		result_mat.getZXYAngle(ang);
		System.out.println(d.getTime() - d2.getTime());
		System.out.println(NYAR_RESULT.equals(result_mat));
		System.out.print(		ar.getConfidence());
		
		
	}
    /**
     * プログラムのエントリーポイントです。
     * サンプルプログラム{@link RawFileTest}を実行します。
     * @param args
     * 引数はありません。
     */
	public static void main(String[] args)
	{

		try {
			RawFileTest t = new RawFileTest();
			// t.Test_arGetVersion();
			t.Test_arDetectMarkerLite();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
