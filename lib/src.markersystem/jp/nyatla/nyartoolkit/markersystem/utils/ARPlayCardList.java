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
package jp.nyatla.nyartoolkit.markersystem.utils;

import java.util.ArrayList;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.psarplaycard.PsARPlayCardPickup;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

/**
 * このクラスは、ARプレイカードの検出結果をマッピングします。
 */
public class ARPlayCardList extends ArrayList<ARPlayCardList.Item>
{
	public static class Item extends TMarkerData
	{
		/** Idの情報。 反応するidの開始レンジ*/
		public final int nyid_range_s;
		/** Idの情報。 反応するidの終了レンジ*/
		public final int nyid_range_e;
		/** Idの情報。 実際のid値*/
		public long id;
		public int dir;
		/**
		 * コンストラクタです。初期値から、Idマーカのインスタンスを生成します。
		 * @param i_range_s
		 * @param i_range_e
		 * @param i_patt_size
		 * @throws NyARRuntimeException
		 */
		public Item(int i_id_range_s,int i_id_range_e,double i_patt_size)
		{
			super();
			this.marker_offset.setSquare(i_patt_size);
			this.nyid_range_s=i_id_range_s;
			this.nyid_range_e=i_id_range_e;
			return;
		}		
	}	
	private static final long serialVersionUID = -6446466460932931830L;
	/**輪郭推定器*/
	private PsARPlayCardPickup _pickup;
	private final PsARPlayCardPickup.PsArIdParam _id_param=new PsARPlayCardPickup.PsArIdParam();
	public ARPlayCardList()
	{
		this._pickup = new PsARPlayCardPickup();
	}
	public void prepare()
	{
		//nothing to do
		//sqはtrackingでnull初期化済み
		
	}
	public boolean update(INyARGrayscaleRaster i_raster,SquareStack.Item i_sq)
	{
		if(!this._pickup.getARPlayCardId(i_raster,i_sq.ob_vertex,this._id_param))
		{
			return false;
		}
		//IDを検出
		int s=this._id_param.id;
		for(int i=this.size()-1;i>=0;i--){
			Item target=this.get(i);
			if(target.nyid_range_s>s || s>target.nyid_range_e)
			{
				continue;
			}
			//既に認識済なら無視
			if(target.lost_count==0){
				continue;
			}
			//一致したよー。
			target.id=s;
			target.dir=this._id_param.direction;
			target.sq=i_sq;
			return true;
		}
		return false;
	}
	public void finish()
	{
		for(int i=this.size()-1;i>=0;i--)
		{
			Item target=this.get(i);
			if(target.sq==null){
				continue;
			}			
			if(target.lost_count>0){
				//参照はそのままで、dirだけ調整する。
				target.lost_count=0;
				target.life++;
				target.sq.rotateVertexL(4-target.dir);
				NyARIntPoint2d.shiftCopy(target.sq.ob_vertex,target.tl_vertex,4-target.dir);
				target.tl_center.setValue(target.sq.center2d);
				target.tl_rect_area=target.sq.rect_area;
			}
		}
	}	
}