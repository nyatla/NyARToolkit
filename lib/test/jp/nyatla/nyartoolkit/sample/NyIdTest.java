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
package jp.nyatla.nyartoolkit.sample;

import java.io.*;


import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.*;
import jp.nyatla.nyartoolkit.processor.*;

/**
 * 320x240のBGRA32で記録されたIdmarkerを撮影したRAWイメージから、
 * Idマーカを認識します。
 *
 */
public class NyIdTest
{
    public class MarkerProcessor extends SingleNyIdMarkerProcesser
    {
        private Object _sync_object = new Object();
        public NyARTransMatResult transmat = null;
        public int current_id = -1;

        public MarkerProcessor(NyARParam i_cparam, int i_raster_format) throws Exception
        {
        	super();//
            initInstance(i_cparam, new NyIdMarkerDataEncoder_RawBit(),100, i_raster_format);
            //アプリケーションフレームワークの初期化
            return;
        }
        /**
         * アプリケーションフレームワークのハンドラ（マーカ出現）
         */
        protected void onEnterHandler(INyIdMarkerData i_code)
        {
            synchronized (this._sync_object)
            {
                NyIdMarkerData_RawBit code = (NyIdMarkerData_RawBit)i_code;
                if (code.length > 4)
                {
                    //4バイト以上の時はint変換しない。
                    this.current_id = -1;//undefined_id
                }
                else
                {
                    this.current_id = 0;
                    //最大4バイト繋げて１個のint値に変換
                    for (int i = 0; i < code.length; i++)
                    {
                        this.current_id = (this.current_id << 8) | code.packet[i];
                    }
                }
                this.transmat = null;
            }
        }
        /**
         * アプリケーションフレームワークのハンドラ（マーカ消滅）
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
         * アプリケーションフレームワークのハンドラ（マーカ更新）
         */
        protected void onUpdateHandler(NyARSquare i_square, NyARTransMatResult result)
        {
        	synchronized (this._sync_object)
            {
                this.transmat = result;
            }
        }
    }
	private final String data_file = "../Data/320x240NyId.raw";
	private final String camera_file = "../Data/camera_para.dat";
    public NyIdTest()
    {
    }
    public void Test() throws Exception
    {
        //AR用カメラパラメタファイルをロード
        NyARParam ap = new NyARParam();
        ap.loadARParamFromFile(camera_file);
        ap.changeScreenSize(320, 240);

		// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
		File f = new File(data_file);
		FileInputStream fs = new FileInputStream(data_file);
		byte[] buf = new byte[(int) f.length()];
		fs.read(buf);		

        NyARRgbRaster_RGB ra = new NyARRgbRaster_RGB(320, 240,false);
        ra.wrapBuffer(buf);

        MarkerProcessor pr = new MarkerProcessor(ap, ra.getBufferType());
        pr.detectMarker(ra);
        return;
    }
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
