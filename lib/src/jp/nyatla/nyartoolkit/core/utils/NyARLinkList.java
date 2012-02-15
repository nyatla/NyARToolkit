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
package jp.nyatla.nyartoolkit.core.utils;



/**
 * このクラスは、可変長のリンクリストです。
 */
public abstract class NyARLinkList<T extends NyARLinkList.Item>
{
	/**
	 * リンクリストのアイテムのベースクラスです。
	 */
	public static class Item
	{
		public Item next;
		public Item prev;
	};
	/**
	 * この関数は、クラスが新しい要素を要求するときに呼び出されます。
	 * 要素のインスタンスを返す関数を実装します。
	 * @return
	 * new T()を実装してください。
	 */
	protected abstract T createElement();
	/**
	 * リンクリストの要素数の合計です。
	 */
	protected int _num_of_item;
	protected T _head_item;
	/**
	 * i_num_of_item以上の要素を予約する。
	 * @param i_num_of_item
	 */
	public void reserv(int i_num_of_item)
	{
		if(this._num_of_item<i_num_of_item){
			this._head_item=this.createElement();
			T ptr=this._head_item;
			for(int i=1;i<i_num_of_item;i++){
				T n=this.createElement();
				ptr.next=n;
				n.prev=ptr;
				ptr=n;
			}
			ptr.next=this._head_item;
			this._head_item.prev=ptr;
			this._num_of_item=i_num_of_item;
		}
	}
	/**
	 * リストを1拡張する。
	 * @param i_num_of_item
	 */
	public void append()
	{
		T new_element=this.createElement();
		T tail=(T) this._head_item.prev;
		tail.next=new_element;
		new_element.next=this._head_item;
		new_element.prev=tail;
		this._head_item.prev=new_element;
		this._num_of_item++;
		
	}
	/**
	 * 初期値を指定してリンクリストを生成する。
	 * @param i_num_of_item
	 * 要素数の初期値。
	 */
	public NyARLinkList(int i_num_of_item)
	{
		this._num_of_item=0;
		reserv(1);
	}

	/**
	 * 最後尾のリストを削除して、リストのi_itemの直前に要素を追加する。
	 * @param i_id
	 * @param i_cf
	 * @param i_dir
	 * @return
	 * 追加した要素
	 */
	public T insertFromTailBefore(T i_item)
	{
		T ptr=this._head_item;
		//先頭の場合
		if(ptr==i_item){
			//リストを後方にシフトする。
			ptr=(T) ptr.prev;
			this._head_item=(T)ptr;
			return this._head_item;
		}
		//最後尾なら、そのまま返す
		if(i_item==this._head_item.prev){
			return i_item;
		}
		//最後尾切り離し
		T n=(T) this._head_item.prev;
		n.prev.next=this._head_item;
		this._head_item.prev=n.prev;
		
		n.next=i_item;
		n.prev=i_item.prev;
		i_item.prev=n;
		n.prev.next=n;
		return n;
	}
}