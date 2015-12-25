package jp.nyatla.nyartoolkit.core.surfacetracking;



/**
 * 内部クラス。N個の{@link NyARSurfaceTransMatrixSet}オブジェクトFIFO
 * @author nyatla
 *
 */
public class NyARSurfaceTransMatrixSetFifo
{
	/**
	 * [readonly]
	 * ログの数
	 */
	public int num_of_item;
	/**
	 * [readonly]
	 * ログの配列
	 */
	public NyARSurfaceTransMatrixSet[] items;
	public NyARSurfaceTransMatrixSetFifo(int i_number_of_log)
	{
		this.num_of_item=0;
		this.items=new NyARSurfaceTransMatrixSet[i_number_of_log];
		for(int i=0;i<i_number_of_log;i++){
			this.items[i]=new NyARSurfaceTransMatrixSet();
		}
	}
	/**
	 * アイテム数を0にリセットする。
	 */
	public void init()
	{
		this.num_of_item=0;
	}
	/**
	 * 新しいMatrixをFifoへ追加する。
	 * @param i_trans
	 * @return
	 * 先頭のFifo領域
	 */
	public NyARSurfaceTransMatrixSet preAdd()
	{
		int len=this.items.length;
		//巡回(last->0)
		NyARSurfaceTransMatrixSet tmp=this.items[len-1];
		for(int i=len-1;i>0;i--){
			this.items[i]=this.items[i-1];
		}
		//要素0に値を計算
		this.items[0]=tmp;
		if(this.num_of_item<len){
			this.num_of_item++;
		}
		return tmp;
	}
}