package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerIn;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTrackerSnapshot;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;

public class NyARRealitySnapshot
{
	LowResolutionLabelingSamplerOut samplerout;
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
	 * @return
	 * 成功すると、移動したターゲットを返します。
	 * @throws NyARException 
	 */
	public final NyARRealityTarget unknownToKnown(int i_index,int i_dir,double i_marker_width) throws NyARException
	{
		NyARRealityTarget r=this.unknown_target.moveTarget(this.known_target, i_index,NyARRealityTarget.RT_KNOWN);
		if(r==null){
			return null;
		}
		r.transmat.has_value=false;
//この操作はRECTステータスのものにしか適応できない。
//directionに応じて、元矩形のrectを回転しておく。
//マーカのサイズを決めておく。
		return r;
	}
	/**
	 * Unknownターゲットから、指定したシリアル番号のターゲットをKnownターゲットへ移動します。
	 * @param i_index
	 * @return
	 * 成功すると、移動したターゲットを返します。
	 * @throws NyARException 
	 */
	public final NyARRealityTarget unknownToKnownBySerial(int i_serial,int i_dir,double i_marker_width) throws NyARException
	{
		int idx=this.unknown_target.getIndexBySerial(i_serial);
		if(idx==-1){
			return null;
		}
		return unknownToKnown(idx,i_dir,i_marker_width);
	}	
	/**
	 * Knownターゲットから、指定したインデクス番号のターゲットをDeadターゲットへ移動します。
	 * @param i_index
	 * @return
	 * 成功すると、移動したターゲットを返します。
	 * @throws NyARException 
	 */
	public final NyARRealityTarget knownToDead(int i_index) throws NyARException
	{
//強制的にステータスをignoreへ
		return this.known_target.moveTarget(this.dead_target, i_index,NyARRealityTarget.RT_DEAD);
	}
	/**
	 * Knownターゲットから、指定したシリアル番号のターゲットをDeadターゲットへ移動します。
	 * @param i_index
	 * @throws NyARException 
	 */
	public final NyARRealityTarget knownToDeadBySerial(int i_serial) throws NyARException
	{
		int idx=this.known_target.getIndexBySerial(i_serial);
		if(idx==-1){
			return null;
		}
		return knownToDead(idx);
	}	
	
	/**
	 * Unknownターゲットから、指定したインデクス番号のターゲットをDeadターゲットへ移動します。
	 * deadターゲットは、次のサイクルでrealityから削除されます。
	 * @param i_index
	 * @return
	 * 成功すると、移動したターゲットを返します。
	 * @throws NyARException 
	 */
	public final NyARRealityTarget unknownToDead(int i_index) throws NyARException
	{
		NyARTargetStatus st=this.unknown_target.getItem(i_index).ref_ttarget.ref_status;
		
		//強制的にステータスをignoreへ
		return this.unknown_target.moveTarget(this.dead_target, i_index,NyARRealityTarget.RT_DEAD);
	}


	/**
	 * Unknownターゲットから、指定したシリアル番号のターゲットをDeadターゲットへ移動します。
	 * deadターゲットは、次のサイクルでrealityから削除されます。
	 * @param i_index
	 * @throws NyARException 
	 */
	public final NyARRealityTarget unknownToDeadBySerial(int i_serial) throws NyARException
	{
		int idx=this.unknown_target.getIndexBySerial(i_serial);
		if(idx==-1){
			return null;
		}
		return unknownToDead(idx);
	}	
}