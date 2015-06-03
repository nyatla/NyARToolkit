package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;



/**
 * �?部クラス�?N個�?�{@link NyARSurfaceTransMatrixSet}オブジェク�?FIFO
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
	 * ログの配�??
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
	 * アイ�?�?数�?0にリセ�?トする�??
	 */
	public void init()
	{
		this.num_of_item=0;
	}
	/**
	 * 新しいMatrixをFifoへ追�?する�?
	 * @param i_trans
	 * @return
	 * 先�?�のFifo領域
	 */
	public NyARSurfaceTransMatrixSet preAdd()
	{
		int len=this.items.length;
		//巡�?(last->0)
		NyARSurfaceTransMatrixSet tmp=this.items[len-1];
		for(int i=len-1;i>0;i--){
			this.items[i]=this.items[i-1];
		}
		//要�?0に値を計�?
		this.items[0]=tmp;
		if(this.num_of_item<len){
			this.num_of_item++;
		}
		return tmp;
	}
}