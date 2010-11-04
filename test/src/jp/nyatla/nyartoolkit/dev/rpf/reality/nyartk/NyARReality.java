package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.*;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;




/**
 * NyARRealitySnapshotを更新するクラス。
 * 
 *
 */
public class NyARReality
{
	private LowResolutionLabelingSampler _sampler;
	private NyARTracker _tracker;
	protected INyARTransMat _transmat;
	
	/**
	 * コンストラクタ
	 * @param i_width
	 * 入力画像の幅を指定します。
	 * @param i_height
	 * 入力画像の高さを指定します。
	 * @param i_depth
	 * 解析画像の解像度を指定します。今使用できるのは、2のみです。
	 * @param i_ref_prjmat
	 * カメラパラメータを指定します。
	 * @param i_max_known_target
	 * Knownターゲットの最大数を指定します。
	 * @param i_max_unknown_target
	 * UnKnownターゲットの最大数を指定します。
	 * @throws NyARException
	 */
	public NyARReality(int i_width,int i_height,int i_depth,NyARPerspectiveProjectionMatrix i_ref_prjmat,
			int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		this._transmat=new NyARTransMat(null,i_ref_prjmat);
		//この数は、NyARRealitySnapshotの値と合せておくこと。
		this._tracker=new NyARTracker(i_max_known_target+i_max_unknown_target,1,i_max_known_target);
		this._sampler=new LowResolutionLabelingSampler(i_width,i_height,i_depth);
		return;
	}
private boolean isTargetAlive(NyARRealityTarget i_target)
{
	return false;
}
	/**
	 * o_outにあるRealitySnapshotの状態を、i_inのRealitySourceを元に進めます。
	 * 関数を実行すると、RealitySnapshotのターゲットの状態は更新され、ターゲットの所属リストが書き換えられます。
	 * 
	 * 現在の更新ルールは以下の通りです。
	 * 1.一定時間捕捉不能なターゲットはUnknownターゲットからdeadターゲットへ移動する。
	 * 2.knownターゲットは状態を確認して最新の状態を維持する。
	 * 3.deadターゲットは捕捉対象から削除する。
	 * 
	 * Knownターゲットが捕捉不能になった時の動作は、以下の通りです。
	 * 4.[未実装]捕捉不能なターゲットの予測と移動
	 * @param i_in
	 * @param o_out
	 * @throws NyARException
	 */
	public void progress(NyARRealityIn i_in,NyARRealitySnapshot o_out) throws NyARException
	{
		long tick=0;
		//sampler進行
		this._sampler.sampling(i_in.lrsamplerin,o_out._samplerout);
		//tracker進行
		this._tracker.progress(o_out._samplerout, o_out._trackout);
	
		//トラックしてないrectターゲット1個探してunknownターゲットに入力
		NyARTarget tt=findEmptyTagItem(o_out._trackout.target_list);
		if(tt!=null){
			o_out.addUnknownTarget(tt);
		}
		//リストのアップデート
		updateLists(tick,o_out);
		//リストのアップグレード
		upgradeLists(tick,o_out);
		return;
	}
	private final void upgradeLists(long i_reality_tick,NyARRealitySnapshot o_out) throws NyARException
	{
		NyARRealityTarget[] rt_array=o_out.target.getArray();
		for(int i=o_out.target.getLength()-1;i>=0;i--)
		{
			switch(rt_array[i].target_type)
			{
			case NyARRealityTarget.RT_DEAD:
				//deadターゲットの削除
				o_out.deleteTarget(i);
				continue;
			case NyARRealityTarget.RT_KNOWN:
			case NyARRealityTarget.RT_UNKNOWN:
				//KNOWNとUNKNOWNは、生存チェックして、死んでたらdeadターゲットへ。自動死んでたの復帰機能を作るときは、この辺いじくる。
				if(!isTargetAlive(rt_array[i])){
					o_out.changeTargetToDead(rt_array[i]);
				}
				continue;
			default:
				throw new NyARException();
			}
		}
	}

	private final void updateLists(long i_reality_tick,NyARRealitySnapshot o_out) throws NyARException
	{
		NyARRealityTarget[] rt_array=o_out.target.getArray();
		for(int i=o_out.target.getLength()-1;i>=0;i--){
			NyARRealityTarget tar=rt_array[i];
			switch(tar.target_type)
			{
			case NyARRealityTarget.RT_DEAD:
				//何もしたくない。
				continue;
			case NyARRealityTarget.RT_KNOWN:

				//矩形座標計算
				setSquare(((NyARRectTargetStatus)(tar.ref_tracktarget.ref_status)).vertex,tar.ideal_square);
				//3d座標計算
				this._transmat.transMatContinue(tar.ideal_square,tar.offset,tar.transmat,tar.transmat);
				continue;
			case NyARRealityTarget.RT_UNKNOWN:
				continue;
			default:
			}
			tar.target_age++;
		}
	}


	/**
	 * 頂点データをNyARSquareにセットする関数
	 * @param i_vx
	 * @param i_s
	 */
	private final static void setSquare(NyARDoublePoint2d[] i_vx,NyARSquare i_s)
	{
		//点から直線を再計算
		for(int i=3;i>=0;i--){
			i_s.sqvertex[i].setValue(i_vx[i]);
			i_s.line[i].makeLinearWithNormalize(i_vx[i],i_vx[(i+1)%4]);
		}
	}
	/**
	 * リストから、tagがNULLのアイテムを探して返します。
	 * @return
	 */
	private final static NyARTarget findEmptyTagItem(NyARTargetList i_list)
	{
		NyARTarget[] items=i_list.getArray();
		for(int i=i_list.getLength()-1;i>=0;i--){
			if(items[i].st_type!=NyARTargetStatus.ST_RECT){
				continue;
			}
			if(items[i].tag!=null){
				continue;
			}
			return items[i];
		}
		return null;
	}
}
