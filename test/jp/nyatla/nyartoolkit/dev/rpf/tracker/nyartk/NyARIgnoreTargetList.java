package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;

public class NyARIgnoreTargetList extends NyARTargetList
{
	private int EXPIRE_COUNT=10;
	public NyARIgnoreTargetList(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
	/**
	 * 所有しているTargetについて、ステータスをアップグレードします。
	 * アップグレード先のステータスは存在しませんが、一定の時間経過後に、ターゲットを削除します。
	 * @param i_clock
	 * システムクロック値を指定します。
	 */
	public void upgrade(long i_clock)
	{
		for(int i=this._length-1;i>=0;i--){
			NyARTarget t=this._items[i];
			if(i_clock-t.last_update>EXPIRE_COUNT)
			{
				//オブジェクトのリリース
				t.releaseObject();
				this.removeIgnoreOrder(i);				
			}
		}
	}
	/**
	 * このターゲットを、SampleStackを使って更新します。
	 * @param i_sample
	 */
	public void update(long i_clock,SampleStack i_sample)
	{
		SampleStack.Item[] source=i_sample.getArray();
		NyARTarget d_ptr;
		i_sample.getArray();
		//マップする。
		this._map.setPointDists(this._items,this._length,source,i_sample.getLength());
		//ターゲットの更新
		for(int i=this._length-1;i>=0;i--){
			d_ptr=this._items[i];
			int sample_index=this._index[i];
			//年齢を加算
			d_ptr.age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				continue;
			}
			d_ptr.setValue(source[sample_index].ref_sampleout);
			d_ptr.last_update=i_clock;
		}
	}	
}