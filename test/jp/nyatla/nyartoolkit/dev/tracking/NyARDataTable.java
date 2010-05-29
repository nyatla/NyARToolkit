package jp.nyatla.nyartoolkit.dev.tracking;

import java.lang.reflect.Array;

public abstract class NyARDataTable<TItem extends Object>
{
	protected TItem[] _items;
	/**
	 * アイテムすべてを選択します。
	 * @return
	 */
	public final TItem[] selectAllItems()
	{
		return this._items;
	}
	public final TItem selectItem(int i_oid)
	{
		return this._items[i_oid];
	}
	@SuppressWarnings("unchecked")
	protected void initTable(int i_size,Class<TItem> i_elemtype)
	{
		this._items=(TItem[])Array.newInstance(i_elemtype, i_size);
		for(int i=0;i<i_size;i++){
			this._items[i]=createElement();
		}
		return;
	}
	protected abstract TItem createElement();	
}