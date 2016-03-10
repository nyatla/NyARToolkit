package jp.nyatla.nyartoolkit.core.kpm.keyframe;


import java.util.TreeMap;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile.PageInfo;


public class KeyframeMap extends TreeMap<Integer,Keyframe>
{
	private static final long serialVersionUID = -2089174031892137840L;

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