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

import jp.nyatla.nyartoolkit.core.utils.NyARLinkList;



public class VertexSortTable extends NyARLinkList<VertexSortTable.Item>
{
	public class Item extends NyARLinkList.Item
	{
		public int sq_dist;
		public TMarkerData marker;
		public int shift;
		public SquareStack.Item ref_sq;
	};
	public VertexSortTable(int iNumOfItem)
	{
		super(iNumOfItem);
	}
	final protected Item createElement()
	{
		return new Item();
	}
	public void reset()
	{
		Item ptr=this._head_item;
		for(int i=this._num_of_item-1;i>=0;i--)
		{
			ptr.sq_dist=Integer.MAX_VALUE;
			ptr=(Item) ptr.next;
		}
	}
	/**
	 * 挿入ポイントを返す。挿入ポイントは、i_sd_point(距離点数)が
	 * 登録済のポイントより小さい場合のみ返却する。
	 * @return
	 */
	public Item getInsertPoint(int i_sd_point)
	{
		Item ptr=_head_item;
		//先頭の場合
		if(ptr.sq_dist>i_sd_point){
			return ptr;
		}
		//それ以降
		ptr=(Item) ptr.next;
		for(int i=this._num_of_item-2;i>=0;i--)
		{
			if(ptr.sq_dist>i_sd_point){
				return ptr;
			}
			ptr=(Item) ptr.next;
		}
		//対象外。
		return null;		
	}
	/**
	 * 指定したターゲットと同じマーカと同じ矩形候補を参照している
	 * @param i_topitem
	 */
	public void disableMatchItem(Item i_topitem)
	{
		Item ptr=this._head_item;
		for(int i=this._num_of_item-1;i>=0;i--)
		{
			if(ptr.marker!=null){
				if(ptr.marker==i_topitem.marker || ptr.marker.sq==i_topitem.ref_sq){
					ptr.marker=null;
				}
			}
			ptr=(Item) ptr.next;
		}	
	}
	public Item getTopItem()
	{
		Item ptr=this._head_item;
		for(int i=this._num_of_item-1;i>=0;i--)
		{
			if(ptr.marker==null){
				ptr=(Item) ptr.next;
				continue;
			}
			return ptr;
		}
		return null;
	}
}