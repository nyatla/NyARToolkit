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
		public NyARDoublePoint3d angle_v=new NyARDoublePoint3d();
		public NyARDoublePoint3d trans_v=new NyARDoublePoint3d();
		public NyARRectOffset offset=new NyARRectOffset();
		public int life=0;
		public int sirial;
		public Item()
		{
			this.trans_v.x=this.trans_v.y=this.trans_v.z=0;		
			this.angle_v.x=this.angle_v.y=this.angle_v.z=0;		
		}

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
				this._items[i].trans_v.x=this._items[i].trans_v.y=this._items[i].trans_v.z=0;
				this._items[i].angle_v.x=this._items[i].angle_v.y=this._items[i].trans_v.z=0;
			}else{
				//nothing.
			}
		}
	}
	
}


