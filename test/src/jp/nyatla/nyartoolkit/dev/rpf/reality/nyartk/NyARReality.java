package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
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
	private LowResolutionLabelingSamplerOut _samplerout;
	/**
	 * samplerの出力値。この変数はNyARRealityからのみ使います。
	 */
	private NyARRealityTargetPool _pool;

	/**
	 * ターゲットのリストです。
	 */
	public NyARRealityTargetList<NyARRealityTarget> target;
	
	
	//種類ごとのターゲットの数
	
	private int _number_of_unknown;
	private int _number_of_known;
	private int _number_of_dead;	
	//
	private LowResolutionLabelingSampler _sampler;
	private NyARTracker _tracker;
	private INyARTransMat _transmat;
	
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
		int number_of_reality_target=i_max_known_target+i_max_unknown_target;
		//演算インスタンス
		this._transmat=new NyARTransMat(null,i_ref_prjmat);
		this._tracker=new NyARTracker(number_of_reality_target,1,i_max_known_target);
		this._sampler=new LowResolutionLabelingSampler(i_width,i_height,i_depth);

		//データインスタンス
		this._pool=new NyARRealityTargetPool(number_of_reality_target);
		this.target=new NyARRealityTargetList<NyARRealityTarget>(number_of_reality_target);
		//トラック数は、newがi_max_known_target+i_max_unknown_target,rectがi_max_known_targetと同じ数です。
		this._samplerout=new LowResolutionLabelingSamplerOut(100+number_of_reality_target);
		
		//定数とかいろいろ
		this.MAX_LIMIT_KNOWN=i_max_known_target;
		this.MAX_LIMIT_UNKNOWN=i_max_unknown_target;

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
		return i_target.ref_tracktarget.st_type==NyARTargetStatus.ST_RECT;
	}
	/**
	 * Realityの状態を、i_inのRealitySourceを元に進めます。
	 * 
	 * 現在の更新ルールは以下の通りです。
	 * 1.一定時間捕捉不能なターゲットはUnknownターゲットからdeadターゲットへ移動する。
	 * 2.knownターゲットは状態を確認して最新の状態を維持する。
	 * 3.deadターゲットは捕捉対象から削除する。
	 * 
	 * Knownターゲットが捕捉不能になった時の動作は、以下の通りです。
	 * 4.[未実装]捕捉不能なターゲットの予測と移動
	 * @param i_in
	 * @throws NyARException
	 */
	public void progress(NyARRealityIn i_in) throws NyARException
	{
		//sampler進行
		this._sampler.sampling(i_in.lrsamplerin,this._samplerout);
		//tracker進行
		this._tracker.progress(this._samplerout);
	
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
			switch(rt_array[i].target_type)
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
			switch(tar.target_type)
			{
			case NyARRealityTarget.RT_DEAD:
				//何もしたくない。
				continue;
			case NyARRealityTarget.RT_KNOWN:

				//矩形座標計算
				setSquare(((NyARRectTargetStatus)(tar.ref_tracktarget.ref_status)).vertex,tar.screen_square);
				//3d座標計算
				this._transmat.transMatContinue(tar.screen_square,tar.offset,tar.transform_matrix,tar.transform_matrix);
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
		rt.target_type=NyARRealityTarget.RT_UNKNOWN;
		this.target.pushAssert(rt);
		this._number_of_unknown++;
		return rt;
	}	
	private final void deleteTarget(int i_index)
	{
		//削除できるのはdeadターゲットだけ
		assert(this.target.getItem(i_index).target_type==NyARRealityTarget.RT_DEAD);
		//poolから開放してリストから削除
		this.target.getItem(i_index).releaseObject();
		this.target.removeIgnoreOrder(i_index);
		this._number_of_dead--;
	}
	/**
	 * Unknownターゲットから、指定したインデクス番号のターゲットをKnownターゲットへ移動します。
	 * @param i_index
	 * @return
	 * 成功すると、移動したターゲットを返します。
	 * @throws NyARException 
	 */
	public final NyARRealityTarget changeTargetToKnown(NyARRealityTarget i_item,int i_dir,double i_marker_width)
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
		if(this._number_of_known>=this.MAX_LIMIT_KNOWN)
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
		this._number_of_unknown--;
		this._number_of_known++;
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
		assert(i_item.ref_tracktarget.st_type!=NyARTargetStatus.ST_IGNORE);
		//所有するトラックターゲットがIGNOREに設定
		this._tracker.changeStatusToIgnore(i_item.ref_tracktarget);
		//数の調整
		if(i_item.target_type==NyARRealityTarget.RT_UNKNOWN){
			this._number_of_unknown--;
		}else{
			this._number_of_known--;
		}
		i_item.target_type=NyARRealityTarget.RT_DEAD;
		this._number_of_dead++;
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
	//アクセサ
	
	/**
	 * @return
	 * 現在のUnKnownターゲットの数を返す。
	 */
	public final int getNumberOfUnknown()
	{
		return this._number_of_unknown;
	}
	/**
	 * @return
	 * 現在のKnownターゲットの数を返す。
	 */
	public final int getNumberOfKnown()
	{
		return this._number_of_known;
	}
	/**
	 * @return
	 * 現在のDeadターゲットの数を返す。
	 */
	public final int getNumberOfDead()
	{
		return this._number_of_dead;
	}
	
}
