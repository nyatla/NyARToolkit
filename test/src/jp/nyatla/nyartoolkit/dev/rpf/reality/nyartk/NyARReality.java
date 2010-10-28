package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
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
/**
 * Trackerのデータを都合よく解釈するレイヤ。
 * recallからはじまって、clearlyTargetかunclearlyTargetで消滅する。
 *
 */
public class NyARReality
{
	/**
	 * unknownターゲットの最大存在サイクル(数値×フレーム間隔が大体の寿命。15fpsなら約6秒)
	 */
	private final int UNKNOWN_TICK_LIMIT=100;
	private NyARRealityDB _db;
	private NyARRealityTargetPool _pool;
	UnknowonTarget unknown_target;
	RecallTargetList recall_target;
	KnowonTarget known_target;
	DeadTarget dead_target;
	public NyARReality(int i_max_target) throws NyARException
	{
		this._pool=new NyARRealityTargetPool(i_max_target);
		this.known_target=new KnowonTarget(i_max_target);
		this.recall_target=new RecallTargetList(1);
		this.unknown_target=new UnknowonTarget(i_max_target);
	}
	private boolean isUnknownTargetAlive(NyARRealityTarget i_rt,long i_reality_tick)
	{
		return (i_rt.target_age<UNKNOWN_TICK_LIMIT);
	}
	private boolean isTargetAlive(NyARRealityTarget i_target)
	{
		return false;
	}
	public void upgradeLists(long i_reality_tick) throws NyARException
	{
		NyARRealityTarget[] rt_array;
		//deadターゲットの削除
		rt_array=this.dead_target.getArray();
		for(int i=this.dead_target.getLength()-1;i>=0;i--)
		{
			rt_array[i].releaseObject();
		}
		this.dead_target.clear();
		
		//knownからの遷移。(->dead)
		rt_array=this.known_target.getArray();
		for(int i=this.known_target.getLength()-1;i>=0;i--){
			if(!isTargetAlive(rt_array[i])){
				if(!this.known_target.moveToDeadTarget(dead_target,i)){
					break;
				}
			}
		}
		//unknownからの遷移。(->known or unknown or recall)
		rt_array=this.unknown_target.getArray();
		for(int i=this.unknown_target.getLength()-1;i>=0;i--){
			if(!isTargetAlive(rt_array[i])){
				this.unknown_target.moveToDeadTarget(this.dead_target,i);
				continue;
			}
			switch(this._db.recall(rt_array[i]))
			{
			case NyARRealityDB.RET_RECALL_FAILED:
				//recallに失敗したら、unknownのローテーション?(今はとりあえずしない。)
				continue;
			case NyARRealityDB.RET_RECALL_SUCCESS:
				//成功したら、knownターゲット行き
				this.unknown_target.moveToKnownTarget(this.known_target,i);
				continue;
			default:
				//非同期系は今実装してないから。
				NyARException.notImplement();
			}
		}		
		//recallからの遷移。(->dead or unknown)
		rt_array=this.recall_target.getArray();
		for(int i=0;i<this.recall_target.getLength();i++){
			//dead遷移-ターゲットのdead条件を確認
			if(!isUnknownTargetAlive(rt_array[i],i_reality_tick)){
				this.recall_target.moveToDeadTarget(this.dead_target,i);
				continue;
			}
			if(this.recall_target.moveToUnknownTarget(this.unknown_target,i)){
				continue;
			}
		}
	}
	protected INyARTransMat _transmat;
	public void updateLists(long i_reality_tick)
	{
		//リアリティターゲットを更新
		NyARRealityTarget[] rt_array;
		rt_array=this.recall_target.getArray();
		for(int i=this.recall_target.getLength()-1;i>=0;i--){
			rt_array[i].target_age++;
		}
		rt_array=this.known_target.getArray();
		for(int i=this.known_target.getLength()-1;i>=0;i--){
			
			rt_array[i].target_age++;
		}
		rt_array=this.unknown_target.getArray();
		for(int i=this.unknown_target.getLength()-1;i>=0;i--){
			rt_array[i].target_age++;
		}
	}
	/**
	 * 指定したIDのターゲットを無視します。
	 * ここに指定できるターゲットは、knownターゲットリストにあるものだけです。
	 * @return
	 */
	public boolean deleteTarget(long i_serial,int i_tick)
	{
	}
	
	
	public void progress() throws NyARException
	{
		long tick;
//reality進行
		NyARTrackerOut trackout=null;

		//トラックしてないrectターゲット1個探してunknownターゲットに入力
		NyARTarget tt=trackout.recttarget.getEmptyTagItem();
		if(tt!=null){
			//RealityTargetを新規に作ってみる。
			NyARRealityTarget rt=this._pool.newNewTarget(tt);
			if(rt!=null){
				//unknownターゲットに突っ込む。
				if(this.unknown_target.push(rt)==null){
					//失敗したら何もしない。
					rt.releaseObject();
				}
			}
		}
		//リストのアップデート
		updateLists(tick);

//<排他セッション>	
		//リストのアップグレード
		upgradeLists(tick);

	}
}
