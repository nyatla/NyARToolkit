package jp.nyatla.nyartoolkit.dev.tracking;


import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.transmat.*;



public class MarkerPositionTable extends NyARDataTable<MarkerPositionTable.Item>
{
	public static class Item
	{
		public boolean is_empty=true;
		public NyARDoublePoint3d angle=new NyARDoublePoint3d();
		public NyARDoublePoint3d trans=new NyARDoublePoint3d();
		public NyARRectOffset offset=new NyARRectOffset();
		public int life=0;
		public NyARDoublePoint3d velocity=new NyARDoublePoint3d();
		public NyARDoublePoint3d acceleration=new NyARDoublePoint3d();
		public int sirial;

	}
	public MarkerPositionTable(int i_size)
	{
		this.initTable(i_size,Item.class);
		this.clear();
		return;
	}
	/**
	 * 空のアイテムを1個選択します。
	 * @return
	 */
	public Item selectEmptyItem()
	{
		for(int i=this._items.length-1;i>=0;i--)
		{
			if(this._items[i].is_empty){
				return this._items[i];
			}
		}
		return null;
	}
	protected Item createElement()
	{
		return new Item();
	}
	
	public void clear()
	{
		for(int i=this._items.length-1;i>=0;i--)
		{
			if(!this._items[i].is_empty){
				this._items[i].is_empty=true;
			}else{
				//nothing.
			}
		}
	}
	
}


