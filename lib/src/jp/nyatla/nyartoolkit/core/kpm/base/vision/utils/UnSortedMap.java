package jp.nyatla.nyartoolkit.core.kpm.base.vision.utils;

import java.util.ArrayList;
import java.util.List;

public class UnSortedMap<K,V> {
	public class Item
	{
		public K key;
		public V value;
	}
	private List<Item> _items=new ArrayList<Item>();
	private int getIndex(K k){
		for(int i=0;i<this._items.size();i++){
			if(this._items.get(i).key.equals(k)){
				return i;
			}
		}
		return -1;
	}
	public void put(K k,V v)
	{
		int idx=this.getIndex(k);
		if(idx==-1){
			Item i=new Item();
			i.key=k;
			i.value=v;
			this._items.add(i);
		}else{
			this._items.get(idx).value=v;
		}
	}
	public V get(K k)
	{
		int i=this.getIndex(k);
		if(i==-1){
			return null;
		}
		return this._items.get(i).value;
	}
	public int size()
	{
		return this._items.size();
	}
	public Item getItem(int i){
		return this._items.get(i);
	}

}
