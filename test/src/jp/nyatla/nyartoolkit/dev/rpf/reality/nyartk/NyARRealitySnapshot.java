package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTarget;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTrackerSnapshot;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;

public class NyARRealitySnapshot
{
	/**
	 * Unknownターゲットの最大数です。
	 */
	private final int MAX_LIMIT_UNKNOWN;
	/**
	 * Knownターゲットの最大数です。
	 */
	private final int MAX_LIMIT_KNOWN;
	
	/**
	 * samplerの出力値。この変数はNyARRealityからのみ使います。
	 */
	public LowResolutionLabelingSamplerOut _samplerout;
	/**
	 * samplerの出力値。この変数はNyARRealityからのみ使います。
	 */
	public NyARTrackerSnapshot _trackout;
	/**
	 * samplerの出力値。この変数はNyARRealityからのみ使います。
	 */
	public NyARRealityTargetPool _pool;

	/**
	 * ターゲットのリストです。
	 */
	public NyARRealityTargetList<NyARRealityTarget> target;
	
	
	//種類ごとのターゲットの数
	
	private int number_of_unknown;
	private int number_of_known;
	private int number_of_dead;

	public NyARRealitySnapshot(int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		this._pool=new NyARRealityTargetPool(i_max_known_target+i_max_unknown_target);
		this.target=new NyARRealityTargetList<NyARRealityTarget>(i_max_known_target+i_max_unknown_target);
		//トラック数は、newがi_max_known_target+i_max_unknown_target,rectがi_max_known_targetと同じ数です。
		this._trackout=new NyARTrackerSnapshot(i_max_known_target+i_max_unknown_target,1,i_max_known_target);
		this.number_of_dead=this.number_of_unknown=this.number_of_known=0;
		this.MAX_LIMIT_KNOWN=i_max_known_target;
		this.MAX_LIMIT_UNKNOWN=i_max_unknown_target;
		return;
	}

	public NyARRealityTarget addUnknownTarget(NyARTarget i_track_target) throws NyARException
	{
		NyARRealityTarget rt=this._pool.newNewTarget(i_track_target);
		if(rt==null){
			return null;
		}
		//個数制限
		if(this.number_of_unknown>=this.MAX_LIMIT_UNKNOWN)
		{
			return null;
		}
		rt.target_type=NyARRealityTarget.RT_UNKNOWN;
		this.number_of_known++;
		return rt;
	}	
	public void deleteTarget(int i_index)
	{
		//削除できるのはdeadターゲットだけ
		assert(this.target.getItem(i_index).target_type==NyARRealityTarget.RT_DEAD);
		//poolから開放してリストから削除
		this.target.getItem(i_index).releaseObject();
		this.target.removeIgnoreOrder(i_index);
		this.number_of_dead--;
	}

	/**
	 * Unknownターゲットから、指定したインデクス番号のターゲットをKnownターゲットへ移動します。
	 * @param i_index
	 * @return
	 * 成功すると、移動したターゲットを返します。
	 * @throws NyARException 
	 */
	public final NyARRealityTarget changeTargetToKnown(NyARRealityTarget i_item,int i_dir,double i_marker_width) throws NyARException
	{
		//遷移元制限
		if(i_item.target_type!=NyARRealityTarget.RT_UNKNOWN){
			return null;
		}
		//ステータス制限
		if(i_item.ref_tracktarget.st_type!=NyARTargetStatus.ST_RECT){
			return null;
		}
		//個数制限
		if(this.number_of_known>=this.MAX_LIMIT_KNOWN)
		{
			return null;
		}
		//ステータス制限
		i_item.target_type=NyARRealityTarget.RT_KNOWN;
		
		//マーカのサイズを決めておく。
		i_item.offset.setSquare(i_marker_width);
		
		//directionに応じて、元矩形のrectを回転しておく。
		((NyARRectTargetStatus)(i_item.ref_tracktarget.ref_status)).shiftByArtkDirection(i_dir);		
		
		//数の調整
		this.number_of_unknown--;
		this.number_of_known++;
		return i_item;
	}
	
	/**
	 * 指定したKnown,またはUnknownターゲットを、ターゲットをDeadターゲットにします。
	 * @param i_item
	 * @throws NyARException 
	 */
	public final void changeTargetToDead(NyARRealityTarget i_item) throws NyARException
	{
		assert(i_item.target_type==NyARRealityTarget.RT_UNKNOWN || i_item.target_type==NyARRealityTarget.RT_KNOWN);
		//所有するトラックターゲットがIGNOREでなければIGNOREへ遷移
		if(i_item.ref_tracktarget.st_type!=NyARTargetStatus.ST_IGNORE){
			this._trackout.changeStatusToIgnore(i_item.ref_tracktarget);
		}
		i_item.target_type=NyARRealityTarget.RT_DEAD;
		//数の調整
		if(i_item.target_type==NyARRealityTarget.RT_UNKNOWN){
			this.number_of_unknown--;
		}else{
			this.number_of_known--;
		}
		this.number_of_dead++;
		return;
	}
	



	/**
	 * Unknownターゲットから、指定したシリアル番号のターゲットをKnownターゲットへ移動します。
	 * @param i_index
	 * @return
	 * 成功すると、移動したターゲットを返します。
	 * @throws NyARException 
	 */
	public final NyARRealityTarget changeTargetToKnownBySerial(int i_serial,int i_dir,double i_marker_width) throws NyARException
	{
		NyARRealityTarget item=this.target.getItemBySerial(i_serial);
		if(item==null){
			return null;
		}
		return changeTargetToKnown(item,i_dir,i_marker_width);
	}
	/**
	 * Knownターゲットから、指定したシリアル番号のターゲットをDeadターゲットへ移動します。
	 * @param i_index
	 * @throws NyARException 
	 */
	public final NyARRealityTarget changeTargetToDeadBySerial(int i_serial) throws NyARException
	{
		NyARRealityTarget item=this.target.getItemBySerial(i_serial);
		if(item==null){
			return null;
		}
		changeTargetToDead(item);
		return item;
	}



	
}