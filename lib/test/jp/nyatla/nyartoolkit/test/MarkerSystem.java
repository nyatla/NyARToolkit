/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
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
import java.util.Date;

import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;


public class MarkerSystem
{
	private final static String data_file = "../Data/320x240ABGR.raw";
	private final static String code_file = "../Data/patt.hiro";
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try{
			// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
			File f = new File(data_file);
			FileInputStream fs = new FileInputStream(data_file);
			byte[] buf = new byte[(int) f.length()];
			fs.read(buf);
			NyARRgbRaster ra = new NyARRgbRaster(320, 240,NyARBufferType.BYTE1D_B8G8R8X8_32,false);
			ra.wrapBuffer(buf);
			//Configurationの生成
			NyARMarkerSystemConfig config = new NyARMarkerSystemConfig(320,240);
			NyARSensor sensor=new NyARSensor(config.getScreenSize());
			//マーカシステムの起動
			NyARMarkerSystem s=new NyARMarkerSystem(config);
			int aid=s.addARMarker(new FileInputStream(code_file),16,25,80);
			sensor.update(ra);
			s.update(sensor);
			Date d2 = new Date();
			for(int i=0;i<1000;i++){
				sensor.update(ra);
				s.update(sensor);
			}
			Date d = new Date();
			System.out.println(d.getTime() - d2.getTime());

			if(s.isExistMarker(aid)){
				NyARDoubleMatrix44 mat=s.getMarkerMatrix(aid);
				System.out.println(s.getConfidence(aid));
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
