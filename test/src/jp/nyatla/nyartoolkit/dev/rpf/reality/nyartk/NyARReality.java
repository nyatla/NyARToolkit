package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerIn;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.*;

class RecallTargetList extends NyARRealityTargetList<NyARRealityTarget>
{

	public RecallTargetList(int iMaxTarget) throws NyARException {
		super(iMaxTarget);
		// TODO Auto-generated constructor stub
	}	
}

class KnowonTarget extends NyARRealityTargetList<NyARRealityTarget>
{
	public KnowonTarget(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
}
class UnknowonTarget extends NyARRealityTargetList<NyARRealityTarget>
{
	public UnknowonTarget(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}


}

class DeadTarget extends NyARRealityTargetList<NyARRealityTarget>
{
	public DeadTarget(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
}

class NyARRealityIn
{
	public LowResolutionLabelingSamplerOut lrsamplerout;
}
class NyARRealitySnapshot
{
	NyARTrackerSnapshot _trackout;
	public NyARRealityTargetPool _pool;
	public UnknowonTarget unknown_target;
	public RecallTargetList recall_target;
	public KnowonTarget known_target;
	public DeadTarget dead_target;
	public NyARRealitySnapshot(int i_max_target) throws NyARException
	{
		this._pool=new NyARRealityTargetPool(i_max_target);
		this.known_target=new KnowonTarget(i_max_target);
		this.recall_target=new RecallTargetList(1);
		this.unknown_target=new UnknowonTarget(i_max_target);
	}
	/**
	 * Unknownターゲットから、指定したインデクス番号のターゲットをKnownターゲットへ移動します。
	 * @param i_index
	 * @throws NyARException 
	 */
	public void unknownToKnown(int i_index) throws NyARException
	{
		this.unknown_target.moveTarget(this.known_target, i_index,NyARRealityTarget.RT_KNOWN);
	}
	/**
	 * Knownターゲットから、指定したインデクス番号のターゲットをDeadターゲットへ移動します。
	 * @param i_index
	 * @throws NyARException 
	 */
	public void knownToDead(int i_index) throws NyARException
	{
		this.known_target.moveTarget(this.dead_target, i_index,NyARRealityTarget.RT_DEAD);
	}
	/**
	 * Unknownターゲットから、指定したインデクス番号のターゲットをDeadターゲットへ移動します。
	 * deadターゲットは、次のサイクルでrealityから削除されます。
	 * @param i_index
	 * @throws NyARException 
	 */
	public void unknownToDead(int i_index) throws NyARException
	{
		this.unknown_target.moveTarget(this.dead_target, i_index,NyARRealityTarget.RT_DEAD);
	}
	/**
	 * Unknownターゲットから、指定したシリアル番号のターゲットをKnownターゲットへ移動します。
	 * @param i_index
	 * @throws NyARException 
	 */
	public boolean unknownToKnownBySerial(int i_serial) throws NyARException
	{
		return this.unknown_target.moveTargetBySerial(this.known_target, i_serial,NyARRealityTarget.RT_KNOWN);
	}
	/**
	 * Knownターゲットから、指定したシリアル番号のターゲットをDeadターゲットへ移動します。
	 * @param i_index
	 * @throws NyARException 
	 */
	public boolean knownToDeadBySerial(int i_serial) throws NyARException
	{
		return this.known_target.moveTargetBySerial(this.dead_target, i_serial,NyARRealityTarget.RT_DEAD);
	}
	/**
	 * Unknownターゲットから、指定したシリアル番号のターゲットをDeadターゲットへ移動します。
	 * deadターゲットは、次のサイクルでrealityから削除されます。
	 * @param i_index
	 * @throws NyARException 
	 */
	public boolean unknownToDeadBySerial(int i_serial) throws NyARException
	{
		return this.unknown_target.moveTargetBySerial(this.dead_target, i_serial,NyARRealityTarget.RT_DEAD);
	}	
}
/**
 * Trackerのデータを都合よく解釈するレイヤ。
 * recallからはじまって、clearlyTargetかunclearlyTargetで消滅する。
 *
 */
public class NyARReality
{
	NyARTracker _tracker;
	protected INyARTransMat _transmat;
	/**
	 * unknownターゲットの最大存在サイクル(数値×フレーム間隔が大体の寿命。15fpsなら約6秒)
	 */
	private final int UNKNOWN_TICK_LIMIT=100;
	public NyARReality() throws NyARException
	{
		
	}
	private boolean isUnknownTargetAlive(NyARRealityTarget i_rt,long i_reality_tick)
	{
		return (i_rt.target_age<UNKNOWN_TICK_LIMIT);
	}
	private boolean isTargetAlive(NyARRealityTarget i_target)
	{
		return false;
	}
	public void upgradeLists(long i_reality_tick,NyARRealitySnapshot o_out) throws NyARException
	{
		NyARRealityTarget[] rt_array;
		//deadターゲットの削除
		rt_array=o_out.dead_target.getArray();
		for(int i=o_out.dead_target.getLength()-1;i>=0;i--)
		{
			rt_array[i].releaseObject();
		}
		o_out.dead_target.clear();
		
		//knownからの遷移。(->dead)
		rt_array=o_out.known_target.getArray();
		for(int i=o_out.known_target.getLength()-1;i>=0;i--){
			if(!isTargetAlive(rt_array[i])){
				if(!o_out.known_target.moveTargetNoOrder(o_out.dead_target,i,NyARRealityTarget.RT_DEAD)){
					break;
				}
			}
		}
		//unknownからの遷移
		rt_array=o_out.unknown_target.getArray();
		for(int i=o_out.unknown_target.getLength()-1;i>=0;i--){
			if(!isTargetAlive(rt_array[i])){
				if(!o_out.unknown_target.moveTargetNoOrder(o_out.dead_target,i,NyARRealityTarget.RT_DEAD)){
					break;
				}
			}
		}
	}
	public void updateLists(long i_reality_tick,NyARRealitySnapshot o_out)
	{
		//リアリティターゲットを更新
		NyARRealityTarget[] rt_array;
		rt_array=o_out.recall_target.getArray();
		for(int i=o_out.recall_target.getLength()-1;i>=0;i--){
			//特にやること無し。
			rt_array[i].target_age++;
		}
		rt_array=o_out.known_target.getArray();
		for(int i=o_out.known_target.getLength()-1;i>=0;i--){
			//3d座標計算しとくか。
			rt_array[i].target_age++;
		}
		rt_array=o_out.unknown_target.getArray();
		for(int i=o_out.unknown_target.getLength()-1;i>=0;i--){
			//やることねーっぽい。
			rt_array[i].target_age++;
		}
	}
	public void progress(NyARRealityIn i_in,NyARRealitySnapshot o_out) throws NyARException
	{
		long tick=0;
		//reality進行
		this._tracker.progress(i_in.lrsamplerout, o_out._trackout);

		//トラックしてないrectターゲット1個探してunknownターゲットに入力
		NyARTarget tt=o_out._trackout.recttarget.getEmptyTagItem();
		if(tt!=null){
			//RealityTargetを新規に作ってみる。
			NyARRealityTarget rt=o_out._pool.newNewTarget(tt);
			if(rt!=null){
				//unknownターゲットに突っ込む。
				if(o_out.unknown_target.push(rt)==null){
					//失敗したら何もしない。
					rt.releaseObject();
				}
			}
		}
		//リストのアップデート
		updateLists(tick,o_out);
		//リストのアップグレード
		upgradeLists(tick,o_out);

	}
}
