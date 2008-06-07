/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.sample;


import java.io.*;
import java.util.*;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.detector.*;

/**
 * 320x240のBGRA32で記録されたRAWイメージから、１種類のパターンを認識し、
 * その変換行列を1000回求め、それにかかったミリ秒時間を表示します。
 * 
 * 
 * @author R.iizuka
 *
 */
public class RawFileTest {
    private final String code_file  ="../Data/patt.hiro";
    private final String data_file  ="../Data/320x240ABGR.raw";
    private final String camera_file="../Data/camera_para.dat";
    public RawFileTest()
    {
    }
    public void Test_arDetectMarkerLite() throws Exception
    {
	//AR用カメラパラメタファイルをロード
	NyARParam ap	=new NyARParam();
	ap.loadFromARFile(camera_file);
	ap.changeSize(320,240);

	//AR用のパターンコードを読み出し	
	NyARCode code=new NyARCode(16,16);
	code.loadFromARFile(code_file);

	//試験イメージの読み出し(320x240 BGRAのRAWデータ)
	File f=new File(data_file);
	FileInputStream fs=new FileInputStream(data_file);
	byte[] buf=new byte[(int)f.length()];
	fs.read(buf);
	NyARRaster_BGRA ra=NyARRaster_BGRA.wrap(buf, 320, 240);
	//		Blank_Raster ra=new Blank_Raster(320, 240);

	//１パターンのみを追跡するクラスを作成
	NyARSingleDetectMarker ar=new NyARSingleDetectMarker(ap,code,80.0);
	ar.detectMarkerLite(ra,100);
	ar.getTransmationMatrix();

	//マーカーを検出
	double[][] tm;
	Date d2=new Date();
	for(int i=0;i<1000;i++){
	    //変換行列を取得
	    ar.detectMarkerLite(ra,100);
	    ar.getTransmationMatrix();
	}
	Date d=new Date();
	tm=null;
	System.out.println(d.getTime()-d2.getTime()); 
    }
    public static void main(String[] args)
    {

	try{
	    RawFileTest t=new RawFileTest();
	    //t.Test_arGetVersion();
	    t.Test_arDetectMarkerLite();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }

}
