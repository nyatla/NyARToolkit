package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import java.awt.ItemSelectable;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.*;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.*;

class SampleStack extends NyARPointerStack<SampleStack.Item>
{

	class Item
	{
		LowResolutionLabelingSamplerOut.Item ref_sampleout;
	}
	protected SampleStack() throws NyARException
	{
		super();
	}
	
	
}







class NyARTargetPool extends NyARManagedObjectPool<NyARTarget>
{
	private NyARNewTargetStatusPool _ref_pool;
	protected NyARTarget createElement() throws NyARException
	{
		return new NyARTarget(this._inner_pool);
	}
	/**
	 * NyARTargetStatusを持つターゲットを新規に作成します。
	 * @param i_clock
	 * システムクロック値
	 * @param i_sample
	 * 初期化元のサンプリングアイテム
	 * @return
	 */
	public NyARTarget newNewTarget(long i_clock,LowResolutionLabelingSamplerOut.Item i_sample)
	{
		NyARTarget t=super.newObject();
		if(t==null){
			return null;
		}
		t.age=0;
		t.last_update=i_clock;
		t.setValue(i_sample);
		t.serial=NyARTarget.getSerial();
		t.tag=null;
		t.ref_status=this._ref_pool.newObject();
		if(t.ref_status==null){
			t.releaseObject();
			return null;
		}
		return t;
	}	
}


public class NyARTracker
{
	private long _clock=0;
	private NyARTargetPool _target_pool;
	private SampleStack _newsource;
	private SampleStack _igsource;
	private SampleStack _coordsource;

	private NyARNewTargetList _newtarget;
	private NyARIgnoreTargetList _igtarget;
	private NyARCoordTargetList _coordtarget;
	
	public void update(LowResolutionLabelingSamplerOut i_source) throws NyARException
	{
		//クロック進行
		this._clock++;
		long clock=this._clock;

		//サンプルのマッピング
		sampleMapper(i_source);
		
		//それぞれのターゲットの更新処理
		
		this._coordtarget.update(clock,i_source.ref_base_raster,this._coordsource);
		this._newtarget.update(clock,this._newsource);
		this._igtarget.update(clock,this._igsource);
	}
	/**
	 * ターゲットのステータスをアップグレードします。
	 * @throws NyARException 
	 */
	public void upgrade(LowResolutionLabelingSamplerOut i_source) throws NyARException
	{
		long clock=this._clock;
		//coord targetの遷移
		//new targetの遷移
		this._newtarget.upgrade(clock,i_source.ref_base_raster,this._igtarget,this._coordtarget);
		//ignore targetの遷移
		this._igtarget.upgrade(clock);
	}
	private void sampleMapper(LowResolutionLabelingSamplerOut i_source)
	{
		LowResolutionLabelingSamplerOut.Item[] sample_items=i_source.getArray();
		for(int i=i_source.getLength()-1;i>=0;i--)
		{
			//サンプラからの値を其々のターゲットのソースへ分配
			LowResolutionLabelingSamplerOut.Item sample_item=sample_items[i];
			int id;
			//coord
			id=this._coordtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._coordsource.push(null);
			}
			//newtarget
			id=this._newtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._newsource.push(null);
			}
			//ignore target
			id=this._igtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._igsource.push(null);
			}
			//マップできなかったものは、NewTragetへ登録
			NyARTarget t=this._target_pool.newNewTarget(this._clock,sample_item);
			if(t==null){
				continue;
			}
			if(this._newtarget.push(t)==null){
				//todo:次回に重複チェックでが発生するけど、どうしようか。回避するには、一旦エントリリストにしないといけないね？
				//newtargetへの遷移が失敗したら、ターゲットを解放。
				t.releaseObject();
			}
		}
	}
}
