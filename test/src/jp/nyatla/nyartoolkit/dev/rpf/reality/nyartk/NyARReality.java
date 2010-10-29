package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
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
		this._transmat=new NyARTransMat(null);
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
				if(o_out.known_target.moveTargetNoOrder(o_out.dead_target,i,NyARRealityTarget.RT_DEAD)==null){
					break;
				}
			}
		}
		//unknownからの遷移
		rt_array=o_out.unknown_target.getArray();
		for(int i=o_out.unknown_target.getLength()-1;i>=0;i--){
			if(!isTargetAlive(rt_array[i])){
				if(o_out.unknown_target.moveTargetNoOrder(o_out.dead_target,i,NyARRealityTarget.RT_DEAD)==null){
					break;
				}
			}
		}
	}
	/**
	 * 観察
	 * @param i_vx
	 * @param i_s
	 */
	private final static void setSquare(NyARDoublePoint2d[] i_vx,NyARSquare i_s)
	{
		i_s.sqvertex[0].setValue(i_vx[0]);
		i_s.sqvertex[1].setValue(i_vx[1]);
		i_s.sqvertex[2].setValue(i_vx[2]);
		i_s.sqvertex[3].setValue(i_vx[3]);
		//点から直線を再計算
		for(int i=3;i>=0;i--){
			i_s.line[i].makeLinearWithNormalize(i_vx[i],i_vx[(i+1)%4]);
		}
	}
	public void updateLists(long i_reality_tick,NyARRealitySnapshot o_out) throws NyARException
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
			NyARRealityTarget tar=rt_array[i];
			//矩形座標計算
			setSquare(tar.ref_ttarget.*,tar.ideal_square);
			//3d座標計算
			this._transmat.transMatContinue(tar.ideal_square,tar.offset,tar.transmat,tar.transmat);
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
