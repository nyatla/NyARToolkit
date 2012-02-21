/* 
 * PROJECT: NyARToolkit(Extension)
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
package jp.nyatla.nyartoolkit.markersystem.utils;

import java.util.ArrayList;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;


/**
 * このクラスは、複数の異なる解像度の比較画像を保持します。
 */
public class MultiResolutionPattProvider
{
	private class Item
	{
		public INyARRgbRaster _patt;
		public NyARMatchPattDeviationColorData _patt_d;
		public int _patt_edge;
		public int _patt_resolution;
		public Item(int i_patt_w,int i_patt_h,int i_edge_percentage) throws NyARException
		{
			int r=1;
			//解像度は幅を基準にする。
			while(i_patt_w*r<64){
				r*=2;
			}
			this._patt=new NyARRgbRaster(i_patt_w,i_patt_h,NyARBufferType.INT1D_X8R8G8B8_32,true);
			this._patt_d=new NyARMatchPattDeviationColorData(i_patt_w,i_patt_h);
			this._patt_edge=i_edge_percentage;
			this._patt_resolution=r;
		}
	}
	/**
	 * インスタンスのキャッシュ
	 */
	private ArrayList<Item> items=new ArrayList<Item>();
	/**
	 * [readonly]マーカにマッチした{@link NyARMatchPattDeviationColorData}インスタンスを得る。
	 * @throws NyARException 
	 */
	public NyARMatchPattDeviationColorData getDeviationColorData(MarkerInfoARMarker i_marker,INyARPerspectiveCopy i_pix_drv, NyARIntPoint2d[] i_vertex) throws NyARException
	{
		int mk_edge=i_marker.patt_edge_percentage;
		for(int i=this.items.size()-1;i>=0;i--)
		{
			Item ptr=this.items.get(i);
			if(!ptr._patt.getSize().isEqualSize(i_marker.patt_w,i_marker.patt_h) || ptr._patt_edge!=mk_edge)
			{
				//サイズとエッジサイズが合致しない物はスルー
				continue;
			}
			//古かったら更新
			i_pix_drv.copyPatt(i_vertex,ptr._patt_edge,ptr._patt_edge,ptr._patt_resolution,ptr._patt);
			ptr._patt_d.setRaster(ptr._patt);
			return ptr._patt_d;
		}
		//無い。新しく生成
		Item item=new Item(i_marker.patt_w,i_marker.patt_h,mk_edge);
		//タイムスタンプの更新とデータの生成
		i_pix_drv.copyPatt(i_vertex,item._patt_edge,item._patt_edge,item._patt_resolution,item._patt);
		item._patt_d.setRaster(item._patt);
		this.items.add(item);
		return item._patt_d;
	}
	
}