package jp.nyatla.nyartoolkit.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARFrustum;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.*;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.NyARTargetStatus;



/**
 * NyARRealityモデルの駆動クラスです。
 * Realityデータの保持と、更新を担当します。
 * <p>NyARRealityModel</p>
 * NyARRealityモデルは、ARToolKitのマーカー認識処理系をReality化します。
 * NyARRealityモデルでは、空間に存在する複数のマーカをターゲットとして取り扱います。
 * マーカは初め、Unknownターゲットとして、Realityの中に現れます。
 * Realityは、Unknownターゲットの存在を可能な限り維持し、そのリストと内容を公開します。
 * 
 * UnknownターゲットはKnownターゲットへ昇格させることができます。
 * その方法は、Unknownターゲットの具体化に必要な情報(マーカ方位と大きさ)を入力することです。
 * 大きさと方位を調べるために、Unknownターゲットはマーカに関するいくつかのアクセス関数を提供します。
 * ユーザは、それらの関数から得られる情報を元に値を推定し、UnknownターゲットをKnownターゲットに
 * 昇格させる処理を行います。
 * 
 * 昇格したKnownターゲットからは、マーカに関するさらに詳細な情報にアクセスする関数を提供します。
 * 
 * ユーザが不要なUnknown/Knownターゲットは、Deadターゲットへ降格させることもできます。
 * このターゲットは、次の処理サイクルで既知の不要ターゲットになり、しばらくの間Realityの
 * 管理から外されます。しばらくすると、またUnknownターゲットに現れます。Deadターゲットは意図的に
 * 発生させる場合以外に、自動的に発生してしまうことがあります。これは、マーカが視界から消えてしまったときです。
 * 
 * 
 *
 */
public class NyARReality
{
	//視野関係のデータ
	public final static double FRASTRAM_ARTK_NEAR=10;
	public final static double FRASTRAM_ARTK_FAR=10000;
	/**frastum*/
	protected NyARFrustum _frustum;
	protected NyARPerspectiveProjectionMatrix _ref_prjmat;

	
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
	 * コンストラクタ。
	 * 樽型歪みが少ない、または補正済みの画像を入力するときには、{@link #NyARReality(NyARIntSize, double, double, NyARPerspectiveProjectionMatrix, NyARCameraDistortionFactor, int, int)}
	 * のi_dist_factorにnullを指定すると、より高速な動作が期待できます。
	 * @param i_param
	 * カメラパラメータを指定します。
	 * @param i_near
	 * 視錐体のnear-pointをmm単位で指定します。
	 * default値は{@link #FRASTRAM_ARTK_NEAR}です。
	 * @param i_far
	 * 視錐体のfar-pointをmm単位で指定します。
	 * default値は{@link #FRASTRAM_ARTK_FAR}です。
	 * @param i_max_known_target
	 * Knownターゲットの最大数を指定します。
	 * @param i_max_unknown_target
	 * UnKnownターゲットの最大数を指定します。
	 * @throws NyARException
	 */
	public NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		//定数とかいろいろ
		this.MAX_LIMIT_KNOWN=i_max_known_target;
		this.MAX_LIMIT_UNKNOWN=i_max_unknown_target;
		this.initInstance(i_param.getScreenSize(),i_near,i_far,i_param.getPerspectiveProjectionMatrix(),i_param.getDistortionFactor());
		return;
	}
	/**
	 * コンストラクタ。
	 * @param i_screen
	 * スクリーン(入力画像)のサイズを指定します。
	 * @param i_near
	 * {@link #NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_far
	 * {@link #NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_prjmat
	 * ARToolKit形式の射影変換パラメータを指定します。
	 * @param i_dist_factor
	 * カメラ歪み矯正オブジェクトを指定します。歪み矯正が不要な時は、nullを指定します。
	 * @param i_max_known_target
	 * {@link #NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_max_unknown_target
	 * {@link #NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @throws NyARException
	 */
	public NyARReality(NyARIntSize i_screen,double i_near,double i_far,NyARPerspectiveProjectionMatrix i_prjmat,NyARCameraDistortionFactor i_dist_factor,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		this.MAX_LIMIT_KNOWN=i_max_known_target;
		this.MAX_LIMIT_UNKNOWN=i_max_unknown_target;
		this.initInstance(i_screen,i_near,i_far,i_prjmat,i_dist_factor);
	}
	/**
	 * コンストラクタから呼び出す共通な初期化部分です。
	 * @param i_dist_factor
	 * @param i_prjmat
	 * @throws NyARException
	 */
	protected void initInstance(NyARIntSize i_screen,double i_near,double i_far,NyARPerspectiveProjectionMatrix i_prjmat,NyARCameraDistortionFactor i_dist_factor) throws NyARException
	{
		int number_of_reality_target=this.MAX_LIMIT_KNOWN+this.MAX_LIMIT_UNKNOWN;
		//演算インスタンス
		this._transmat=new NyARTransMat(i_dist_factor,i_prjmat);

		//データインスタンス
		this._pool=new NyARRealityTargetPool(number_of_reality_target,i_prjmat);
		this.target=new NyARRealityTargetList(number_of_reality_target);
		//Trackerの特性値
		this._tracker=new NyARTracker((this.MAX_LIMIT_KNOWN+this.MAX_LIMIT_UNKNOWN)*2,1,this.MAX_LIMIT_KNOWN*2);
		//フラスタムの計算とスクリーンサイズの保存
		this._ref_prjmat=i_prjmat;
		this._frustum=new NyARFrustum(i_prjmat,i_screen.w,i_screen.h, i_near, i_far);

		//初期化
		this._number_of_dead=this._number_of_unknown=this._number_of_known=0;
		return;
	}
	/**
	 * Realityの状態を、i_inの{@link NyARRealitySource}を元に、１サイクル進めます。
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
	/**
	 * Realityターゲットリストの全ての項目を更新します。この関数内では、リスト要素の増減はありません。
	 * {@link #progress}のサブ関数です。
	 * @throws NyARException
	 */
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
	/**
	 * Realityターゲットリストの全ての項目のアップグレード処理を行います。この関数内でリスト要素の加算/減算/種別変更処理を行います。
	 * {@link #progress}のサブ関数です。
	 * @throws NyARException
	 */
	private final void updateLists() throws NyARException
	{
		NyARRealityTarget[] rt_array=this.target.getArray();
		
		for(int i=this.target.getLength()-1;i>=0;i--){
			NyARRealityTarget tar=rt_array[i];
			if(tar._ref_tracktarget._delay_tick==0){
				//30fps前後で1秒間の認識率とする。
				tar.grab_rate+=3;
				if(tar.grab_rate>100){tar.grab_rate=100;}
				switch(tar._target_type)
				{
				case NyARRealityTarget.RT_DEAD:
					//何もしない
					continue;
				case NyARRealityTarget.RT_KNOWN:
					//矩形座標計算
					setSquare(((NyARRectTargetStatus)(tar._ref_tracktarget._ref_status)).vertex,tar._screen_square);
					//3d座標計算
//					this._transmat.transMat(tar._screen_square,tar._offset,tar._transform_matrix);
					this._transmat.transMatContinue(tar._screen_square,tar._offset,tar._transform_matrix,tar._transform_matrix);
					continue;
				case NyARRealityTarget.RT_UNKNOWN:
					continue;
				default:
				}
			}else{
				//更新をパスして補足レートの再計算(混ぜて8で割る)
				tar.grab_rate=tar.grab_rate-(3*tar._ref_tracktarget._delay_tick);
				if(tar.grab_rate<0){tar.grab_rate=0;}
			}
		}
	}
	private NyARLinear __tmp_l=new NyARLinear();


	/**
	 * 頂点データをNyARSquareにセットする関数です。
	 * 初期位置セットには使わないこと。
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
	 * Unknown/Knownを維持できる条件を書きます。
	 * @param i_target
	 * @return
	 */
	private final boolean isTargetAlive(NyARRealityTarget i_target)
	{
		return i_target._ref_tracktarget._st_type==NyARTargetStatus.ST_RECT;
	}
	
	/**
	 * トラックターゲットリストから、tagがNULLの{@link NyARTargetStatus#ST_RECT}アイテムを探して返します。
	 * @return
	 */
	private final static NyARTarget findEmptyTagItem(NyARTargetList i_list)
	{
		NyARTarget[] items=i_list.getArray();
		for(int i=i_list.getLength()-1;i>=0;i--){
			if(items[i]._st_type!=NyARTargetStatus.ST_RECT){
				continue;
			}
			if(items[i].tag!=null){
				continue;
			}
			return items[i];
		}
		return null;
	}
	//RealityTargetの編集関数

	/**
	 * Realityターゲットリストへ新しい{@link NyARRealityTarget}を追加する。
	 * @param i_track_target
	 * UnknownTargetに関連付ける{@link NyARTarget}.このターゲットは、{@link NyARTargetStatus#ST_RECT}であること？
	 */
	private final NyARRealityTarget addUnknownTarget(NyARTarget i_track_target) throws NyARException
	{
		assert(i_track_target._st_type==NyARTargetStatus.ST_RECT);
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
	/**
	 * Realityターゲットリストから指定したインデクス番号のターゲットを削除します。
	 * @param i_index
	 */
	private final void deleteTarget(int i_index)
	{
		//削除できるのはdeadターゲットだけ
		assert(this.target.getItem(i_index)._target_type==NyARRealityTarget.RT_DEAD);
		//poolから開放してリストから削除
		this.target.getItem(i_index).releaseObject();
		this.target.removeIgnoreOrder(i_index);
		this._number_of_dead--;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//Public:
	//RealityTargetの操作関数
	//
	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 指定したターゲットを、UnknownターゲットからKnownターゲットへ遷移させます。
	 * @param i_item
	 * 移動するターゲット
	 * @param i_dir
	 * ターゲットの予備知識。ARToolkitのdirectionでどの方位であるかを示す値
	 * @param i_marker_size
	 * ターゲットの予備知識。マーカーの高さ/幅がいくらであるかを示す値[mm単位]
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException 
	 */
	public final boolean changeTargetToKnown(NyARRealityTarget i_item,int i_dir,double i_marker_size) throws NyARException
	{
		return changeTargetToKnown(i_item,i_dir,i_marker_size,i_marker_size);
	}
	

	/**
	 * 指定したターゲットを、UnknownターゲットからKnownターゲットへ遷移させます。
	 * @param i_item
	 * 移動するターゲット
	 * @param i_dir
	 * ターゲットの予備知識。ARToolkitのdirectionでどの方位であるかを示す値
	 * @param i_marker_width
	 * ターゲットの予備知識。マーカーの高さがいくらであるかを示す値[mm単位]
	 * @param i_marker_height
	 * ターゲットの予備知識。マーカーの幅がいくらであるかを示す値[mm単位]
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException 
	 */
	public final boolean changeTargetToKnown(NyARRealityTarget i_item,int i_dir,double i_marker_width,double i_marker_height) throws NyARException
	{
		//遷移元制限
		if(i_item._target_type!=NyARRealityTarget.RT_UNKNOWN){
			return false;
		}
		//ステータス制限
		if(i_item._ref_tracktarget._st_type!=NyARTargetStatus.ST_RECT){
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
		i_item._offset.setSquare(i_marker_width,i_marker_height);
		
		//directionに応じて、元矩形のrectを回転しておく。
		((NyARRectTargetStatus)(i_item._ref_tracktarget._ref_status)).shiftByArtkDirection((4-i_dir)%4);		
		//矩形セット
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(i_item._ref_tracktarget._ref_status)).vertex;
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
	 * 指定したKnown,またはUnknownターゲットを、50サイクルの間Deadターゲットにします。
	 * Deadターゲットは次回のサイクルでRealityターゲットリストから削除され、一定のサイクル期間の間システムから無視されます。
	 * @param i_item
	 * @throws NyARException 
	 */	
	public final void changeTargetToDead(NyARRealityTarget i_item) throws NyARException
	{
		changeTargetToDead(i_item,50);
	}
	
	/**
	 * 指定したKnown,またはUnknownターゲットを、Deadターゲットにします。
	 * Deadターゲットは次回のサイクルでRealityターゲットリストから削除され、一定のサイクル期間の間システムから無視されます。
	 * @param i_item
	 * @param i_dead_cycle
	 * 無視するサイクルを指定します。1サイクルは1フレームです。デフォルトは50です。
	 * @throws NyARException 
	 */
	public final void changeTargetToDead(NyARRealityTarget i_item,int i_dead_cycle) throws NyARException
	{
		assert(i_item._target_type==NyARRealityTarget.RT_UNKNOWN || i_item._target_type==NyARRealityTarget.RT_KNOWN);
		//IG検出して遷移した場合
		if(i_item._ref_tracktarget._st_type!=NyARTargetStatus.ST_IGNORE){
			//所有するトラックターゲットがIGNOREに設定
			this._tracker.changeStatusToIgnore(i_item._ref_tracktarget,i_dead_cycle);
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
	public final boolean changeTargetToKnownBySerial(long i_serial,int i_dir,double i_marker_width) throws NyARException
	{
		NyARRealityTarget item=this.target.getItemBySerial(i_serial);
		if(item==null){
			return false;
		}
		return changeTargetToKnown(item,i_dir,i_marker_width);
	}
	/**
	 * 指定したシリアル番号のKnown/UnknownターゲットをDeadターゲットへ遷移します。
	 * @param i_serial
	 * @throws NyARException 
	 */
	public final NyARRealityTarget changeTargetToDeadBySerial(long i_serial) throws NyARException
	{
		NyARRealityTarget item=this.target.getItemBySerial(i_serial);
		if(item==null){
			return null;
		}
		changeTargetToDead(item);
		return item;
	}
	
	/**
	 * 現在のUnKnownターゲットの数を返します。
	 * @return
	 */
	public final int getNumberOfUnknown()
	{
		return this._number_of_unknown;
	}
	/**
	 * 現在のKnownターゲットの数を返します。
	 * @return
	 */
	public final int getNumberOfKnown()
	{
		return this._number_of_known;
	}
	/**
	 * 現在のDeadターゲットの数を返します。
	 * @return
	 */
	public final int getNumberOfDead()
	{
		return this._number_of_dead;
	}
	/**
	 * Realityターゲットリストへの参照値を返します。
	 * このリストは編集関数を持ちますが、直接編集してはいけません。
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
	/**
	 * フラスタムオブジェクトを返します。
	 * @return
	 */
	public NyARFrustum refFrustum()
	{
		return this._frustum;
	}
	/**
	 * ARToolKitスタイルの射影変換行列を返します。
	 * @return
	 */
	public NyARPerspectiveProjectionMatrix refPerspectiveProjectionMatrix()
	{
		return this._ref_prjmat;
	}
	/**
	 * 画面座標系の4頂点でかこまれる領域から、RGB画像をo_rasterに取得します。
	 * @param i_vertex
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * @param o_raster
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbPatt2d(NyARRealitySource i_src,NyARIntPoint2d[] i_vertex,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		return i_src.refPerspectiveRasterReader().read4Point(i_src.refRgbSource(),i_vertex,0,0,i_resolution, o_raster);
	}
	/**
	 * 画面座標系の4頂点でかこまれる領域から、RGB画像をo_rasterに取得します。
	 * @param i_vertex
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * @param o_raster
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbPatt2d(NyARRealitySource i_src,NyARDoublePoint2d[] i_vertex,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		return i_src.refPerspectiveRasterReader().read4Point(i_src.refRgbSource(),i_vertex,0,0,i_resolution, o_raster);
	}	
	/**
	 * カメラ座標系の4頂点でかこまれる領域から、RGB画像をo_rasterに取得します。
	 * @param i_vertex
	 * @param i_matrix
	 * i_vertexに適応する変換行列。
	 * ターゲットの姿勢行列を指定すると、ターゲット座標系になります。不要ならばnullを設定してください。
	 * @param i_resolution
	 * @param o_raster
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbPatt3d(NyARRealitySource i_src,NyARDoublePoint3d[] i_vertex,NyARDoubleMatrix44 i_matrix,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		NyARDoublePoint2d[] vx=NyARDoublePoint2d.createArray(4);
		if(i_matrix!=null){
			//姿勢変換してから射影変換
			NyARDoublePoint3d v3d=new NyARDoublePoint3d();
			for(int i=3;i>=0;i--){
				i_matrix.transform3d(i_vertex[i],v3d);
				this._ref_prjmat.project(v3d,vx[i]);
			}
		}else{
			//射影変換のみ
			for(int i=3;i>=0;i--){
				this._ref_prjmat.project(i_vertex[i],vx[i]);
			}
		}
		//パターンの取得
		return i_src.refPerspectiveRasterReader().read4Point(i_src.refRgbSource(),vx,0,0,i_resolution, o_raster);
	}
}
