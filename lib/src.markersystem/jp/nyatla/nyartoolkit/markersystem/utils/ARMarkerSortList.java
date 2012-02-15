package jp.nyatla.nyartoolkit.markersystem.utils;

import jp.nyatla.nyartoolkit.core.utils.NyARLinkList;



public class ARMarkerSortList extends NyARLinkList<ARMarkerSortList.Item>
{
	public class Item extends NyARLinkList.Item
	{
		MarkerInfoARMarker marker;
		double cf;
		int dir;
		SquareStack.Item ref_sq;
	};
	/**
	 * 指定個数のリンクリストを生成。
	 * @param i_num_of_item
	 */
	public ARMarkerSortList()
	{
		super(1);
	}
	protected Item createElement()
	{
		return new Item();
	}
	/**
	 * 挿入ポイントを返す。挿入ポイントは、i_sd_point(距離点数)が
	 * 登録済のポイントより小さい場合のみ返却する。
	 * @return
	 */
	public Item getInsertPoint(double i_cf)
	{
		Item ptr=_head_item;
		//先頭の場合
		if(ptr.cf<i_cf){
			return ptr;
		}
		//それ以降
		ptr=(Item) ptr.next;
		for(int i=this._num_of_item-2;i>=0;i--)
		{
			if(ptr.cf<i_cf){
				return ptr;
			}
			ptr=(Item) ptr.next;
		}
		//対象外。
		return null;		
	}		
	public void reset()
	{
		Item ptr=this._head_item;
		for(int i=this._num_of_item-1;i>=0;i--)
		{
			ptr.cf=0;
			ptr.marker=null;
			ptr.ref_sq=null;
			ptr=(Item) ptr.next;
		}
		
	}
	/**
	 * リストから最も高い一致率のアイテムを取得する。
	 */
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
	/**
	 * リスト中の、i_itemと同じマーカIDか、同じ矩形情報を参照しているものを無効に(ptr.idを-1)する。
	 */
	public void disableMatchItem(Item i_item)
	{
		Item ptr=this._head_item;
		for(int i=this._num_of_item-1;i>=0;i--)
		{			
			if((ptr.marker==i_item.marker) || (ptr.ref_sq==i_item.ref_sq)){
				ptr.marker=null;
			}
			ptr=(Item) ptr.next;
		}
	}
	public int getLength(){
		return this._num_of_item;
	}
}