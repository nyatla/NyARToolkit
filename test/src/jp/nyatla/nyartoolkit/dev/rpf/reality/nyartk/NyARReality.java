package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat_ARToolKit;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
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
	//Realityでーた
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
	private NyARRealityTargetPool _pool;

	private NyARRealityTargetList target;

	//種類ごとのターゲットの数
	
	private int _number_of_unknown;
	private int _number_of_known;
	private int _number_of_dead;	
	//
	private NyARTracker _tracker;
	private INyARTransMat _transmat;
	
	/**
	 * コンストラクタ
	 * @param i_ref_prjmat
	 * カメラパラメータを指定します。
	 * @param i_max_known_target
	 * Knownターゲットの最大数を指定します。
	 * @param i_max_unknown_target
	 * UnKnownターゲットの最大数を指定します。
	 * @throws NyARException
	 */
	public NyARReality(NyARParam i_param,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		//定数とかいろいろ
		this.MAX_LIMIT_KNOWN=i_max_known_target;
		this.MAX_LIMIT_UNKNOWN=i_max_unknown_target;
		this.initInstance(i_param.getDistortionFactor(),i_param.getPerspectiveProjectionMatrix());
		return;
	}
	public NyARReality(NyARCameraDistortionFactor i_dist_factor,NyARPerspectiveProjectionMatrix i_prjmat,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		this.MAX_LIMIT_KNOWN=i_max_known_target;
		this.MAX_LIMIT_UNKNOWN=i_max_unknown_target;
		this.initInstance(i_dist_factor,i_prjmat);
	}
	protected void initInstance(NyARCameraDistortionFactor i_dist_factor,NyARPerspectiveProjectionMatrix i_prjmat) throws NyARException
	{
		int number_of_reality_target=this.MAX_LIMIT_KNOWN+this.MAX_LIMIT_UNKNOWN;
		//演算インスタンス
		this._transmat=new NyARTransMat(i_dist_factor,i_prjmat);

		//データインスタンス
		this._pool=new NyARRealityTargetPool(number_of_reality_target,i_prjmat);
		this.target=new NyARRealityTargetList(number_of_reality_target);
		//Trackerの特性値
		this._tracker=new NyARTracker(
			(this.MAX_LIMIT_KNOWN+this.MAX_LIMIT_UNKNOWN)*2,
			1,
			this.MAX_LIMIT_KNOWN*2);
		//トラック数は、newがi_max_known_target+i_max_unknown_target,rectがi_max_known_targetと同じ数です。

		//初期化
		this._number_of_dead=this._number_of_unknown=this._number_of_known=0;
		
		return;		
	}
	/**
	 * Unknown/Knownを維持できる条件
	 * @param i_target
	 * @return
	 */
	private final boolean isTargetAlive(NyARRealityTarget i_target)
	{
		return i_target._ref_tracktarget.st_type==NyARTargetStatus.ST_RECT;
	}
	/**
	 * Realityの状態を、i_inのRealitySourceを元に進めます。
	 * 
	 * 現在の更新ルールは以下の通りです。
	 * 0.呼び出されるごとに、トラックターゲットからUnknownターゲットを生成する。
	 * 1.一定時間捕捉不能なKnown,Unknownターゲットは、deadターゲットへ移動する。
	 * 2.knownターゲットは最新の状態を維持する。
	 * 3.deadターゲットは（次の呼び出しで）捕捉対象から削除する。
	 * Knownターゲットが捕捉不能になった時の動作は、以下の通りです。
	 * 4.[未実装]捕捉不能なターゲットの予測と移動
	 * @param i_in
	 * @throws NyARException
	 */
	public void progress(NyARRealitySource i_in) throws NyARException
	{
		//tracker進行
		this._tracker.progress(i_in.makeTrackSource());
		
		//トラックしてないrectターゲット1個探してunknownターゲットに入力
		NyARTarget tt=findEmptyTagItem(this._tracker._targets);
		if(tt!=null){
			this.addUnknownTarget(tt);
		}
		//リストのアップデート
		updateLists();
		//リストのアップグレード
		upgradeLists();
		return;
	}
	private final void upgradeLists() throws NyARException
	{
		NyARRealityTarget[] rt_array=this.target.getArray();
		for(int i=this.target.getLength()-1;i>=0;i--)
		{
			switch(rt_array[i]._target_type)
			{
			case NyARRealityTarget.RT_DEAD:
				//deadターゲットの削除
				this.deleteTarget(i);
				continue;
			case NyARRealityTarget.RT_KNOWN:
			case NyARRealityTarget.RT_UNKNOWN:
				//KNOWNとUNKNOWNは、生存チェックして、死んでたらdeadターゲットへ。自動死んでたの復帰機能を作るときは、この辺いじくる。
				if(!isTargetAlive(rt_array[i])){
					this.changeTargetToDead(rt_array[i]);
				}
				continue;
			default:
				throw new NyARException();
			}
		}
	}

	private final void updateLists() throws NyARException
	{
		NyARRealityTarget[] rt_array=this.target.getArray();
		
		for(int i=this.target.getLength()-1;i>=0;i--){
			NyARRealityTarget tar=rt_array[i];
			if(tar._ref_tracktarget.delay_tick==0){
				tar._grab_rate=(tar._grab_rate*5+(100)*3)>>3;
				switch(tar._target_type)
				{
				case NyARRealityTarget.RT_DEAD:
					//何もしない
					continue;
				case NyARRealityTarget.RT_KNOWN:
					//矩形座標計算
					setSquare(((NyARRectTargetStatus)(tar._ref_tracktarget.ref_status)).vertex,tar._screen_square);
					//3d座標計算
//					this._transmat.transMat(tar._screen_square,tar._offset,tar._transform_matrix);
					this._transmat.transMatContinue(tar._screen_square,tar._offset,tar._transform_matrix,tar._transform_matrix);
					continue;
				case NyARRealityTarget.RT_UNKNOWN:
					continue;
				default:
				}
			}else{
				//更新をパスして補足レートの再計算(5:3で混ぜて8で割る)
				tar._grab_rate=(tar._grab_rate*5+(100/tar._ref_tracktarget.delay_tick)*3)>>3;
			}
		}
	}
	private NyARLinear __tmp_l=new NyARLinear();


	/**
	 * 頂点データをNyARSquareにセットする関数
	 * @param i_vx
	 * @param i_s
	 */
	private final void setSquare(NyARDoublePoint2d[] i_vx,NyARSquare i_s)
	{		
		NyARLinear l=this.__tmp_l;
		//線分を平滑化。（ノイズが多いソースを使う時は線分の平滑化。ほんとは使いたくない。）
		for(int i=3;i>=0;i--){
			i_s.sqvertex[i].setValue(i_vx[i]);
			l.makeLinearWithNormalize(i_vx[i], i_vx[(i+1)%4]);
			i_s.line[i].a=i_s.line[i].a*0.6+l.a*0.4;
			i_s.line[i].b=i_s.line[i].b*0.6+l.b*0.4;
			i_s.line[i].c=i_s.line[i].c*0.6+l.c*0.4;
		}
		
		for(int i=3;i>=0;i--){
			i_s.line[i].crossPos(i_s.line[(i+3)%4],i_s.sqvertex[i]);
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
	//RealityTarget操作系関数
	
	/**
	 * Unknownリストへ新しいRealityTargetを追加する。
	 * @param i_track_target
	 * Unknowntargetの元にするTrackTarget
	 */
	private final NyARRealityTarget addUnknownTarget(NyARTarget i_track_target) throws NyARException
	{
		NyARRealityTarget rt=this._pool.newNewTarget(i_track_target);
		if(rt==null){
			return null;
		}
		//個数制限
		if(this._number_of_unknown>=this.MAX_LIMIT_UNKNOWN)
		{
			return null;
		}
		rt._target_type=NyARRealityTarget.RT_UNKNOWN;
		this.target.pushAssert(rt);
		this._number_of_unknown++;
		return rt;
	}	
	private final void deleteTarget(int i_index)
	{
		//削除できるのはdeadターゲットだけ
		assert(this.target.getItem(i_index)._target_type==NyARRealityTarget.RT_DEAD);
		//poolから開放してリストから削除
		this.target.getItem(i_index).releaseObject();
		this.target.removeIgnoreOrder(i_index);
		this._number_of_dead--;
	}
	/**
	 * Unknownターゲットから、指定したインデクス番号のターゲットをKnownターゲットへ移動します。
	 * @param i_item
	 * 移動するターゲット
	 * @param i_dir
	 * ターゲットの予備知識。ARToolkitのdirectionでどの方位であるかを示す値
	 * @param i_marker_width
	 * ターゲットの予備知識。マーカーのサイズがいくらであるかを示す値[mm単位]
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException 
	 * @throws NyARException 
	 */
	public final boolean changeTargetToKnown(NyARRealityTarget i_item,int i_dir,double i_marker_width) throws NyARException
	{
		//遷移元制限
		if(i_item._target_type!=NyARRealityTarget.RT_UNKNOWN){
			return false;
		}
		//ステータス制限
		if(i_item._ref_tracktarget.st_type!=NyARTargetStatus.ST_RECT){
			return false;
		}
		//個数制限
		if(this._number_of_known>=this.MAX_LIMIT_KNOWN)
		{
			return false;
		}
		//ステータス制限
		i_item._target_type=NyARRealityTarget.RT_KNOWN;
		
		//マーカのサイズを決めておく。
		i_item._offset.setSquare(i_marker_width);
		
		//directionに応じて、元矩形のrectを回転しておく。
		((NyARRectTargetStatus)(i_item._ref_tracktarget.ref_status)).shiftByArtkDirection(3-i_dir);		
		//矩形セット
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(i_item._ref_tracktarget.ref_status)).vertex;
		for(int i=3;i>=0;i--){
			i_item._screen_square.sqvertex[i].setValue(vx[i]);
			i_item._screen_square.line[i].makeLinearWithNormalize(vx[i],vx[(i+1)%4]);
		}
		//3d座標計算
		this._transmat.transMat(i_item._screen_square,i_item._offset,i_item._transform_matrix);
		
		//数の調整
		this._number_of_unknown--;
		this._number_of_known++;
		return true;
	}
	
	/**
	 * 指定したKnown,またはUnknownターゲットを、ターゲットをDeadターゲットにします。
	 * Deadターゲットは次回のサイクルでRealityTargetListから削除され、一定のサイクル期間の間システムから無視されます。
	 * @param i_item
	 * @throws NyARException 
	 */
	public final void changeTargetToDead(NyARRealityTarget i_item) throws NyARException
	{
		assert(i_item._target_type==NyARRealityTarget.RT_UNKNOWN || i_item._target_type==NyARRealityTarget.RT_KNOWN);
		//IG検出して遷移した場合
		if(i_item._ref_tracktarget.st_type!=NyARTargetStatus.ST_IGNORE){
			//所有するトラックターゲットがIGNOREに設定
			this._tracker.changeStatusToIgnore(i_item._ref_tracktarget,50);
		}
		//数の調整
		if(i_item._target_type==NyARRealityTarget.RT_UNKNOWN){
			this._number_of_unknown--;
		}else{
			this._number_of_known--;
		}
		i_item._target_type=NyARRealityTarget.RT_DEAD;
		this._number_of_dead++;
		return;
	}
	/**
	 * 指定したシリアル番号のUnknownターゲットを、Knownターゲットへ移動します。
	 * @param i_serial
	 * ターゲットのシリアル番号を示す値
	 * @param i_dir
	 * ターゲットの予備知識。ARToolkitのdirectionでどの方位であるかを示す値
	 * @param i_marker_width
	 * ターゲットの予備知識。マーカーのサイズがいくらであるかを示す値[mm単位]
	 * @return
	 * 成功すると、trueを返します。
	 * @throws NyARException 
	 */
	public final boolean changeTargetToKnownBySerial(int i_serial,int i_dir,double i_marker_width) throws NyARException
	{
		NyARRealityTarget item=this.target.getItemBySerial(i_serial);
		if(item==null){
			return false;
		}
		return changeTargetToKnown(item,i_dir,i_marker_width);
	}
	/**
	 * 指定したシリアル番号のKnown/UnknownターゲットをDeadターゲットへ移動します。
	 * @param i_serial
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
	//アクセサ
	
	/**
	 * 現在のUnKnownターゲットの数を返す。
	 * @return
	 */
	public final int getNumberOfUnknown()
	{
		return this._number_of_unknown;
	}
	/**
	 * 現在のKnownターゲットの数を返す。
	 * @return
	 */
	public final int getNumberOfKnown()
	{
		return this._number_of_known;
	}
	/**
	 * 現在のDeadターゲットの数を返す。
	 * @return
	 */
	public final int getNumberOfDead()
	{
		return this._number_of_dead;
	}
	/**
	 * ターゲットリストへの参照値を返す。このターゲットリストは、直接編集しないでください。
	 * @return
	 */
	public NyARRealityTargetList refTargetList()
	{
		return this.target;
	}

	/**
	 * Knownターゲットを検索して、配列に返します。
	 * @param o_result
	 * 結果を格納する配列です。格納されるターゲットの最大数は、コンストラクタの設定値と同じです。
	 * 配列サイズが不足した場合は、発見した順に最大数を返します。
	 * @return
	 * 配列に格納したターゲットの数を返します。
	 */
	public int selectKnownTargets(NyARRealityTarget[] o_result)
	{
		return this.target.selectTargetsByType(NyARRealityTarget.RT_KNOWN, o_result);
	}
	/**
	 * Unknownターゲットを検索して、配列に返します。
	 * @param o_result
	 * 結果を格納する配列です。格納されるターゲットの最大数は、コンストラクタの設定値と同じです。
	 * 配列サイズが不足した場合は、発見した順に最大数を返します。
	 * @return
	 * 配列に格納したターゲットの数を返します。
	 */
	public int selectUnKnownTargets(NyARRealityTarget[] o_result)
	{
		return this.target.selectTargetsByType(NyARRealityTarget.RT_UNKNOWN, o_result);
	}
	/**
	 * Unknownターゲットを1個検索して返します。
	 * @return
	 * 一番初めに発見したターゲットを返します。見つからないときはNULLです。
	 */
	public NyARRealityTarget selectSingleUnknownTarget()
	{
		return this.target.selectSingleTargetByType(NyARRealityTarget.RT_UNKNOWN);
	}
}
