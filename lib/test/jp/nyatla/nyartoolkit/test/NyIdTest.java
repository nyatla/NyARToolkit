/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.nyidmarker.data.INyIdMarkerData;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerDataEncoder_RawBitId;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBit;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBitId;
import jp.nyatla.nyartoolkit.processor.SingleNyIdMarkerProcesser;

/**
 * このプログラムは、NyIdマーカ検出クラス{@link SingleNyIdMarkerProcesser}の動作チェックプログラムです。
 * 静止画から1個のIDマーカを読み取り、その数値を得る動作を確認できます。
 * 
 * このプログラムには結果を表示する機能がありません。
 * 数値の確認は、ブレークポイントを仕掛けるなどしてください。
 * <p>必要なファイル - 
 * このプログラムの実行には、以下の外部ファイルが必要です。
 * <ul>
 * <li>camera_para.dat - ARToolKit付属のカメラパラメータファイル
 * <li>320x240NyId.raw　- Idマーカを撮影した、QVGAサイズのR8G8B8形式のサンプル画像
 * </ul>
 * </p>
 */
public class NyIdTest
{
	/**
	 * このクラスは、{@link SingleNyIdMarkerProcesser}の自己コールバック関数を実装したクラスです。
	 * 自己コールバック関数は、{@link #detectMarker}の内部から呼び出さます。
	 * これにより、アプリケーションにマーカ状態の変化を通知します。
	 * 通知される条件については、それぞれの関数の説明を見てください。
	 */
    public class MarkerProcessor extends SingleNyIdMarkerProcesser
    {
        private Object _sync_object = new Object();
        /** {@link #onUpdateHandler}関数で得た姿勢行列のポインタ*/
        public NyARDoubleMatrix44 transmat = null;
        /** {@link #onEnterHandler}関数で得た姿勢行列のポインタ*/
        public int current_id = -1;
        /**
         * コンストラクタです。
         * パラメータを{@link #initInstance}へセットして初期化します。
         * ここでは、{@link #initInstance}へ値を引き渡すだけです。
         * @param i_cparam
         * カメラパラメータ。
         * @param i_raster_format
         * 入力ラスタのフォーマット。
         * @throws Exception
         */
        public MarkerProcessor(NyARParam i_cparam, int i_raster_format) throws Exception
        {
        	super();//
            initInstance(i_cparam, new NyIdMarkerDataEncoder_RawBitId(),100);
            //アプリケーションフレームワークの初期化
            return;
        }
        /**
         * この関数は、{@link #detectMarker}から呼び出される自己コールバック関数です。
         * 画像にマーカが現われたときに呼び出されます。
         * ここでは、例として、マーカの情報を読み取り、それを{@link NyIdMarkerData_RawBit}を使って
         * int値にエンコードする処理を実装しています。
         */
        protected void onEnterHandler(INyIdMarkerData i_code)
        {
            synchronized (this._sync_object)
            {
            	NyIdMarkerData_RawBitId code = (NyIdMarkerData_RawBitId)i_code;
                System.out.println("NyARId:"+code.marker_id);
                this.transmat = null;
            }
        }
        /**
         * この関数は、{@link #detectMarker}から呼び出される自己コールバック関数です。
         * 画像からマーカが消え去った時に呼び出されます。
         * ここでは、マーカが消えた場合の後始末処理をします。
         * このサンプルでは、メンバ変数をリセットしています。
         */
        protected void onLeaveHandler()
        {
        	synchronized (this._sync_object)
            {
                this.current_id = -1;
                this.transmat = null;
            }
            return;
        }
        /**
         * この関数は、{@link #detectMarker}から呼び出される自己コールバック関数です。
         * 画像中のマーカの位置が変化したときに呼び出されます。
         * この関数は、{@link #onEnterHandler}直後に呼び出されることもあります。
         * 
         * このサンプルでは、引数で通知されたマーカの姿勢を、メンバ変数に保存しています。
         */
        protected void onUpdateHandler(NyARSquare i_square, NyARDoubleMatrix44 result)
        {
        	synchronized (this._sync_object)
            {
                this.transmat = result;
            }
        }
    }
	private final String data_file = "../Data/320x240NyId.raw";
	private final String camera_file = "../Data/camera_para.dat";
	/**
	 * コンストラクタです。
	 * ここで行う処理はありません。
	 */
    public NyIdTest()
    {
    }
    /**
     * テスト関数の本体です。
     * 設定ファイル、サンプル画像の読み込んだのちに{@link MarkerProcessor}を生成し、
     * １回だけ画像を入力して、マーカ検出を試行します。
     * @throws Exception
     */
    public void Test() throws Exception
    {
        //AR用カメラパラメタファイルをロード
        NyARParam ap = NyARParam.createFromARParamFile(new FileInputStream(camera_file));
        ap.changeScreenSize(320, 240);

		// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
		File f = new File(data_file);
		FileInputStream fs = new FileInputStream(data_file);
		byte[] buf = new byte[(int) f.length()];
		fs.read(buf);		

        NyARRgbRaster ra = new NyARRgbRaster(320, 240,NyARBufferType.BYTE1D_R8G8B8_24,false);
        ra.wrapBuffer(buf);

        MarkerProcessor pr = new MarkerProcessor(ap, ra.getBufferType());
        pr.detectMarker(ra);
        return;
    }
    /**
     * プログラムのエントリーポイントです。
     * サンプルプログラム{@link NyIdTest}を実行します。
     * @param args
     * 引数はありません。
     */
	public static void main(String[] args)
	{

		try {
			NyIdTest t = new NyIdTest();
			// t.Test_arGetVersion();
			t.Test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}    
}
