/* 
 * PROJECT: NyARToolkit JOGL sample program.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nyartoolkit.j2se.test;


import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;
/**
 * JMFからの映像入力からマーカ1種を検出し、そこに立方体を重ねます。
 * ARマーカには、patt.hiroを使用して下さい。
 */
public class MarkerSystemTest
{

	private final static String ARCODE_FILE = "../../Data/patt.hiro";
	private final static String raw_file = "../../Data/320x240ABGR.raw";

	public static void main(String[] args)
	{
		try {
			NyARMarkerSystemConfig config = new NyARMarkerSystemConfig(320,240);
			NyARSensor s=new NyARSensor(new NyARIntSize(320,240));//create sensor system
			NyARMarkerSystem nyar=new NyARMarkerSystem(config);   //create MarkerSystem
			int id=nyar.addARMarker(ARCODE_FILE,16,25,80);
			Date d2 = new Date();
			if(false){
				File f = new File(raw_file);
				FileInputStream fs = new FileInputStream(raw_file);
				byte[] buf = new byte[(int) f.length()];
				fs.read(buf);
				INyARRgbRaster ra = new NyARRgbRaster(320, 240,NyARBufferType.BYTE1D_B8G8R8X8_32,false);
				ra.wrapBuffer(buf);
				s.update(ra);
			}else{
				s.update(new NyARBufferedImageRaster(ImageIO.read(new File("../../Data/320x240ABGR.png"))));
			}
			for (int i = 0; i < 1000; i++) {
				// 変換行列を取得
				s.updateTimeStamp();
				nyar.update(s);
			}
			Date d = new Date();
			System.out.println("Time:"+(d.getTime()-d2.getTime()));
			if(nyar.isExistMarker(id)){
				System.out.println(nyar.getConfidence(id));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
}
