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
	protected SampleStack(int i_size) throws NyARException
	{
		super();
		this.initInstance(i_size,SampleStack.Item.class);
	}
	
	
}









class NyARTrackerOut
{
	public NyARNewTargetList newtarget;
	public NyARIgnoreTargetList igtarget;
	public NyARCoordTargetList coordtarget;
	
	public NyARNewTargetStatusPool newst_pool;
	public NyARContourTargetStatusPool contourst_pool;
	public NyARTargetPool target_pool;
	
	public final static int NUMBER_OF_NEW=5;
	public final static int NUMBER_OF_CONTURE=5;
	public final static int NUMBER_OF_IGNORE=10;
	public NyARTrackerOut() throws NyARException
	{
		//ステータスプール
		this.newst_pool=new NyARNewTargetStatusPool();
		this.contourst_pool=new NyARContourTargetStatusPool(NUMBER_OF_CONTURE*2,50);
		//ターゲットプール
		this.target_pool=new NyARTargetPool(NUMBER_OF_NEW+NUMBER_OF_CONTURE+NUMBER_OF_IGNORE,this.newst_pool);
		//ターゲット
		this.newtarget=new NyARNewTargetList(NUMBER_OF_NEW, this.newst_pool, this.contourst_pool);
		this.igtarget=new NyARIgnoreTargetList(NUMBER_OF_IGNORE);
		this.coordtarget=new NyARCoordTargetList(NUMBER_OF_CONTURE, this.contourst_pool);
	}	
}

public class NyARTracker
{
	private long _clock=0;

	
	private SampleStack _newsource;
	private SampleStack _igsource;
	private SampleStack _coordsource;

//	private NyARNewTargetList _newtarget;
//	private NyARIgnoreTargetList _igtarget;
//	private NyARCoordTargetList _coordtarget;
//	
//	private NyARNewTargetStatusPool _newst_pool;
//	private NyARContourTargetStatusPool _contourst_pool;
//	private NyARTargetPool _target_pool;
//	
//	private static int NUMBER_OF_NEW=5;
//	private static int NUMBER_OF_CONTURE=5;
//	private static int NUMBER_OF_IGNORE=10;
	/**
	 * コンストラクタです。
	 * @throws NyARException
	 */
	public NyARTracker() throws NyARException
	{
/*
		//ステータスプール
		this._newst_pool=new NyARNewTargetStatusPool();
		this._contourst_pool=new NyARContourTargetStatusPool(NUMBER_OF_CONTURE*2,50);
		//ターゲットプール
		this._target_pool=new NyARTargetPool(NUMBER_OF_NEW+NUMBER_OF_CONTURE+NUMBER_OF_IGNORE,this._newst_pool);
		//ターゲット
		this._newtarget=new NyARNewTargetList(NUMBER_OF_NEW, this._newst_pool, this._contourst_pool);
		this._igtarget=new NyARIgnoreTargetList(NUMBER_OF_IGNORE);
		this._coordtarget=new NyARCoordTargetList(NUMBER_OF_CONTURE, this._contourst_pool);
*/
		//ソースターゲット
		this._newsource=new SampleStack(NyARTrackerOut.NUMBER_OF_NEW);
		this._igsource=new SampleStack(NyARTrackerOut.NUMBER_OF_IGNORE);
		this._coordsource=new SampleStack(NyARTrackerOut.NUMBER_OF_CONTURE);
	}
	/**
	 * i_trackdataの状態を更新します。
	 * @param i_source
	 * @throws NyARException
	 */
	public void progress(LowResolutionLabelingSamplerOut i_source,NyARTrackerOut i_trackdata) throws NyARException
	{
		//クロック進行
		this._clock++;
		long clock=this._clock;

		//サンプルのマッピング
		sampleMapper(i_source,i_trackdata);
		
		//それぞれのターゲットの更新処理
		
		i_trackdata.coordtarget.update(clock,i_source.ref_base_raster,this._coordsource);
		i_trackdata.newtarget.update(clock,this._newsource);
		i_trackdata.igtarget.update(clock,this._igsource);
		//アップグレード
		upgradeTarget(i_source,i_trackdata);
	}
	/**
	 * ターゲットをアップグレードします。
	 * @throws NyARException 
	 */
	private void upgradeTarget(LowResolutionLabelingSamplerOut i_source,NyARTrackerOut i_trackdata) throws NyARException
	{
		long clock=this._clock;
		//coord targetの遷移
		//new targetの遷移
		i_trackdata.newtarget.upgrade(clock,i_source.ref_base_raster,i_trackdata.igtarget,i_trackdata.coordtarget);
		//ignore targetの遷移
		i_trackdata.igtarget.upgrade(clock);
	}
	/**
	 * 
	 * @param i_source
	 * @param i_trackdata
	 * @throws NyARException
	 */
	private void updateStatus(LowResolutionLabelingSamplerOut i_source,NyARTrackerOut i_trackdata) throws NyARException
	{
		
	}
	private void sampleMapper(LowResolutionLabelingSamplerOut i_source,NyARTrackerOut i_trackdata)
	{
		LowResolutionLabelingSamplerOut.Item[] sample_items=i_source.getArray();
		for(int i=i_source.getLength()-1;i>=0;i--)
		{
			//サンプラからの値を其々のターゲットのソースへ分配
			LowResolutionLabelingSamplerOut.Item sample_item=sample_items[i];
			int id;
			//coord
			id=i_trackdata.coordtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._coordsource.push(null);
			}
			//newtarget
			id=i_trackdata.newtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._newsource.push(null);
			}
			//ignore target
			id=i_trackdata.igtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._igsource.push(null);
			}
			//マップできなかったものは、NewTragetへ登録
			NyARTarget t=i_trackdata.target_pool.newNewTarget(this._clock,sample_item);
			if(t==null){
				continue;
			}
			if(i_trackdata.newtarget.push(t)==null){
				//todo:次回に重複チェックでが発生するけど、どうしようか。回避するには、一旦エントリリストにしないといけないね？
				//newtargetへの遷移が失敗したら、ターゲットを解放。
				t.releaseObject();
			}
		}
	}

	
}
