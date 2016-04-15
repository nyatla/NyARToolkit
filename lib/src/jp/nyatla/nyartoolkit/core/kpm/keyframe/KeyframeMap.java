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
package jp.nyatla.nyartoolkit.core.kpm.keyframe;


import java.util.TreeMap;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile.PageInfo;


public class KeyframeMap extends TreeMap<Integer,Keyframe>
{
	private static final long serialVersionUID = -2089174031892137840L;

	/**
	 * FsetFileデータから、page_idに一致したキーマップを生成します。
	 * @param i_refDataSet
	 * @param i_page_id
	 */
	public KeyframeMap(NyARNftFreakFsetFile i_refDataSet,int i_page_id)
	{
		PageInfo page_info=i_refDataSet.page_info[i_page_id];
		int db_id = 0;
		for (int m = 0; m < page_info.image_info.length; m++) {
			int image_no = page_info.image_info[m].image_no;
			int l=0;
			//格納予定のデータ数を数える
			for (int i = 0; i < i_refDataSet.ref_point.length; i++) {
				if (i_refDataSet.ref_point[i].refImageNo == image_no) {
					l++;
				}
			}
			FreakMatchPointSetStack fps = new FreakMatchPointSetStack(l);				
			for (int i = 0; i < i_refDataSet.ref_point.length; i++) {
				if (i_refDataSet.ref_point[i].refImageNo == image_no) {
					NyARNftFreakFsetFile.RefDataSet t = i_refDataSet.ref_point[i];
					FreakMatchPointSetStack.Item fp = fps.prePush();
					fp.x = t.coord2D.x;
					fp.y = t.coord2D.y;
					fp.angle = t.featureVec.angle;
					fp.scale = t.featureVec.scale;
					fp.maxima = t.featureVec.maxima > 0 ? true : false;
					if(i_refDataSet.ref_point[i].featureVec.v.length!=96){
						throw new NyARRuntimeException();
					}
					fp.descripter.setValueLe(i_refDataSet.ref_point[i].featureVec.v);
					fp.pos3d.x=t.coord3D.x;
					fp.pos3d.y=t.coord3D.y;
					fp.pos3d.z=0;
				}
			}
			Keyframe keyframe=new Keyframe(page_info.image_info[m].w, page_info.image_info[m].h,fps);
			this.put(db_id++,keyframe);
		}
		return;
	}
}