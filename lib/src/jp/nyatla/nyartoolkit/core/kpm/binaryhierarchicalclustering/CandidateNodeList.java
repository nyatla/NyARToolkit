package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;




public class CandidateNodeList
{
	public class Item{
		public int distance;
		public BinaryHierarchicalNode node;
	}
	private Item[] _list;
	private int _len;
	public CandidateNodeList(int i_size)
	{
		this._list=new Item[i_size];
	}
	public boolean push(BinaryHierarchicalNode i_node,int i_distance)
	{
		Item[] list=this._list;
		if(this._len<list.length){
			//容量に余裕があるならそのまま追記
			Item item=this._list[this._len];
			item.distance=i_distance;
			item.node=i_node;
			this._len++;
			return true;
		}
		//容量に余裕がないなら最大スコアのアイテムに上書き
		Item max_item=list[0];
		for(int i=this._len-1;i>0;i--){
			if(max_item.distance<list[i].distance){
				max_item=list[i];
			}
		}
		if(max_item.distance>i_distance){
			max_item.distance=i_distance;
			max_item.node=i_node;
			return true;
		}
		return false;
	}
	/**
	 * 一番distanceの小さいアイテムを返す。
	 */
	public Item popSmallest()
	{
		//最小スコアのアイテムを検索
		int min_score=Integer.MAX_VALUE;
		int min_index=-1;
		Item[] list=this._list;
		for(int i=this._len-1;i>=0;i--){
			if(list[i].distance>min_score){
				continue;
			}
			min_score=list[i].distance;
			min_index=i;
		}
		if(min_score==Integer.MAX_VALUE){
			return null;
		}else{
			//対象のアイテムを取得
			Item r=list[min_index];
			//最後尾と差し替え
			list[min_index]=list[this._len-1];
			this._list[this._len-1]=r;
			this._len--;
			return r;
		}
	}
}
