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
 *　このクラスは、NyARToolkitのリアリティモデルを実装します。
 * Realityターゲット(以下、RTターゲット)要素の保持と、{@link NyARRealitySource}を元にした更新を担当します。
 * <p>NyARToolkitのリアリティモデル-
 * NyARToolkitのリアリティモデルは、ARToolKitのマーカー認識処理系をベースとしたデータモデルです。
 * このモデルでは、空間に存在する複数のマーカを個別に管理し、これらの情報に対するランダムアクセス性をアプリケーションに提供します。
 * 基本的には複数の{@link NyARRealityTarget}要素を持つテーブルです。
 * マーカ１個を１RTターゲットとして取り扱い、{@link NyARRealityTarget}クラスに要素として格納します。
 * </p>
 * <p>基本的な使い方-
 * {@link NyARReality}は、定期的に入力される更新ソース(画像入力)に従ってテーブルの状態を更新することで、全体を管理します。
 * 他に操作関数がいくつあり、ユーザが要素の状態を制御することもできます。基本的には、ユーザは次の3つの実装をすることになります。
 * <ul>
 * <li>定期的に{@link NyARReality}へ更新ソースを入力する処理({@link #progress}関数を使用。)
 * <li>更新した{@link NyARReality}のRTターゲットをチェックし、その状態をユーザへ反映する処理。({@link #refTargetList()}で得られるRTターゲットリストから要素を読みだす。)
 * <li>アプリケーションの都合に合せた、{@link NyARReality}の編集処理({@link NyARReality#changeTargetToDead}関数等により、RTターゲットのステータスを更新。)
 * </ul>
 * </p>
 * <p>シリアル番号について
 * {@link NyARReality}は、発見した全てのターゲットに、システムの起動から終了までの間一意になる「シリアル番号」を割り振ります。
 * この番号は、システム内でターゲットを区別することに役立ち、{@link NyARRealityTarget#getSerialId()}関数で得ることができます。
 * IDは64bitの数値です。
 * </p>
 * <p>RTターゲットのステータス-
 * {@link NyARReality}は、所有するRTターゲット{@link NyARRealityTarget}を３つのステータスで管理します。
 * 各RTターゲットのステータス値は、{@link NyARRealityTarget#getTargetType()}関数で得ることができます。
 * ステータスの意味は以下の通りです。
 * <ul>
 * <li>Unknown　-
 * 既知のRTターゲットです。このターゲットは、２次元パラメータ（マーカの画面座標、パターンなど）を読みだすことができます。
 * このステータスのRTターゲットは、手動更新によりKnownステータスに遷移できます。遷移は、{@link #changeTargetToKnown}で手動で行います。
 * また、{@link #progress}による自動更新と、{@link #changeTargetToDead}関数による
 * 手動更新により、Deadステータスに遷移できます。自動更新は、システムがマーカを見失った場合に自動的に起こります。
 * 手動更新は、例えばそのRTターゲットが不要である場合（パターンチェックで対象外となった）に、無視するために使います。
 * ステータス値には、{@link NyARRealityTarget#RT_UNKNOWN}を取ります。
 * <li>Known -
 * 既知のRTターゲットです。このRTターゲットは、２次元パラメータと、物理サイズ、物理姿勢等の、３次元パラメータを読みだすことができます。
 * このステータスのターゲットは、{@link #progress}による自動更新と、{@link #changeTargetToDead}関数による
 * 手動更新により、Deadステータスに遷移できます。自動更新は、システムがマーカを見失った場合に自動的に起こります。
 * 手動更新は、例えばそのRTターゲットが不要である場合（対象外のマーカである）に、認識を無視するために使います。
 * ステータス値には、{@link NyARRealityTarget#RT_KNOWN}を取ります。
 * <li>Dead -
 * 間もなく消失するRTターゲットです。次回の{@link #progress}の実行で、テーブルから消失します。
 * ステータス値には、{@link NyARRealityTarget#RT_DEAD}を取ります。
 * </ul>
 * 全てのRTターゲットは、{@link #progress}により発生するUnknownステータスから始まり、必要なものだけがKnownステータスまで昇格し、
 * 要らないものと消失した者はDeadステータスで終了します。UnknownステータスとKnownステータスの違いは、外部情報（マーカパターンに関する物理知識）
 * の有無です。Knownステータスのターゲットはより多くの外部情報により、Unknownステータスのターゲットよりも多くの情報を提供します。
 * </p>
 */
public class NyARReality
{
	/** 定数値。ARToolKitの視錐台のNEAR値です。(単位はmm)*/
	public final static double FRASTRAM_ARTK_NEAR=10;
	/** 定数値。ARToolKitの視錐台のFAR値です。(単位はmm)*/
	public final static double FRASTRAM_ARTK_FAR=10000;
	/** 視錐台のキャッシュ用オブジェクト*/
	protected NyARFrustum _frustum;
	/** 射影変換行列の参照値*/
	protected NyARPerspectiveProjectionMatrix _ref_prjmat;

	
	//Realityでーた
	/**
	 * UnknownステータスのRTターゲットの最大数です。
	 */
	private final int MAX_LIMIT_UNKNOWN;
	/**
	 * KnownターゲットのRTターゲットの最大数です。
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
	 * 初期状態のインスタンスを生成します。
	 * {@link #progress}に入力する入力画像の樽型歪みがすくないか、外部で補正した画像を入力するときは、別のコンストラクタ{@link #NyARReality(NyARIntSize, double, double, NyARPerspectiveProjectionMatrix, NyARCameraDistortionFactor, int, int)}
	 * のi_dist_factorにnullを指定すると、より高速な動作が期待できます。
	 * @param i_param
	 * カメラパラメータを指定します。
	 * @param i_near
	 * 視錐体のnear-pointをmm単位で指定します。
	 * 標準値は{@link #FRASTRAM_ARTK_NEAR}です。
	 * @param i_far
	 * 視錐体のfar-pointをmm単位で指定します。
	 * 標準値は{@link #FRASTRAM_ARTK_FAR}です。
	 * @param i_max_known_target
	 * KnownステータスのRTターゲットの最大数を指定します。
	 * @param i_max_unknown_target
	 * UnKnownステータスのRTターゲットの最大数を指定します。
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
	 * 初期状態のインスタンスを生成します。
	 * @param i_screen
	 * スクリーン(入力画像)のサイズを指定します。
	 * @param i_near
	 * {@link #NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_far
	 * {@link #NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_prjmat
	 * ARToolKit形式の射影変換パラメータを指定します。
	 * @param i_dist_factor
	 * 樽型歪み矯正オブジェクトを指定します。歪み矯正が不要な時は、nullを指定します。
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
	 * <ul>
	 * <li>呼び出されるごとに、トラックターゲットからUnknownステータスのRTターゲットを生成する。
	 * <li>一定時間捕捉不能なKnown,Unknownステータスは、deadステータスへ移動する。
	 * <li>knownステータスのRTターゲットは最新の状態を維持する。
	 * <li>deadステータスのRTターゲットは（次の呼び出しで）捕捉対象から削除する。
	 * </ul>
	 * <p>メモ-
	 * [未実装]捕捉不能なRTターゲットの予測と移動
	 * </p>
	 * @param i_in
	 * 入力する画像をセットしたオブジェクト。
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
	 * {@link #progress}のサブ関数です。
	 * RTターゲットリストの全ての項目を更新します。この関数内では、リスト要素の増減はありません。
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
	 * {@link #progress}のサブ関数です。
	 * RTターゲットリストの全ての項目のアップグレード処理を行います。この関数内でリスト要素の加算/減算/種別変更処理を行います。
	 * @throws NyARException
	 */
	private final void updateLists() throws NyARException
	{
		NyARRealityTarget[] rt_array=this.target.getArray();
		
		for(int i=this.target.getLength()-1;i>=0;i--){
			NyARRealityTarget tar=rt_array[i];
			if(tar._ref_tracktarget._delay_tick==0){
				//30fps前後で1秒間の認識率とする。
				tar._grab_rate+=3;
				if(tar._grab_rate>100){tar._grab_rate=100;}
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
				tar._grab_rate=tar._grab_rate-(3*tar._ref_tracktarget._delay_tick);
				if(tar._grab_rate<0){tar._grab_rate=0;}
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
	 * RTターゲットリストへ新しい{@link NyARRealityTarget}を追加する。
	 * @param i_track_target
	 * UnknownTargetに関連付ける{@link NyARTarget}.このRTターゲットは、{@link NyARTargetStatus#ST_RECT}であること？
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
	 * RTターゲットリストから指定したインデクス番号のRTターゲットを削除します。
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
	 * この関数は、指定したRTターゲットをUnknownステータスからKnownステータスへ遷移させます。
	 * @param i_item
	 * 遷移させるRTターゲット。thisインスタンスから得られたものである必要があります。
	 * @param i_dir
	 * このRTターゲットが、ARToolKitのdirectionでどの方位であるかを示す値
	 * @param i_marker_size
	 * マーカーの高さ/幅がいくらであるかを示す値[mm単位]
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException 
	 */
	public final boolean changeTargetToKnown(NyARRealityTarget i_item,int i_dir,double i_marker_size) throws NyARException
	{
		return changeTargetToKnown(i_item,i_dir,i_marker_size,i_marker_size);
	}
	

	/**
	 * この関数は、指定したRTターゲットをUnknownステータスからKnownステータスへ遷移させます。
	 * @param i_item
	 * 遷移させるRTターゲット。thisインスタンスから得られたものである必要があります。
	 * @param i_dir
	 * このRTターゲットが、ARToolKitのdirectionでどの方位であるかを示す値
	 * @param i_marker_width
	 * マーカーの幅がいくらであるかを示す値[mm単位]
	 * @param i_marker_height
	 * マーカーの高さがいくらであるかを示す値[mm単位]
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
	 * この関数は、指定しRTたターゲットをKnown,またはUnknownステータスからDeadステータスへ遷移させます。
	 * {@link #changeTargetToDead(NyARRealityTarget, int)}の第二引数に、50を設定したときと同じ動作です。
	 * @param i_item
	 * 遷移させるRTターゲット。thisインスタンスから得られたものである必要があります。
	 * @throws NyARException 
	 */	
	public final void changeTargetToDead(NyARRealityTarget i_item) throws NyARException
	{
		changeTargetToDead(i_item,50);
	}
	
	/**
	 * この関数は、指定したRTターゲットをKnown,またはUnknownステータスからDeadステータスへ遷移させます。
	 * Deadターゲットは次回のサイクルでRealityターゲットリストから削除され、i_dead_cycleに指定したサイクル期間、システムから無視されます。
	 * @param i_item
	 * 遷移させるターゲット。thisインスタンスから得られたものである必要があります。
	 * @param i_dead_cycle
	 * 無視するサイクルを指定します。1サイクルは1フレームです。画像入力のフレームレートにより、実時間は異なります。
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
	 * この関数は、シリアル番号に一致するRTターゲットをUnknownステータスからKnownステータスへ遷移させます。
	 * @param i_serial
	 * RTターゲットのシリアル番号を示す値。この値は、{@link NyARRealityTarget#getSerialId()}で得られる値です。
	 * @param i_dir
	 * このRTターゲットが、ARToolKitのdirectionでどの方位であるかを示す値
	 * @param i_marker_width
	 * マーカーの高さ/幅がいくらであるかを示す値[mm単位]
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
	 * この関数は、シリアル番号に一致するRTターゲットをKnown/UnknownステータスからDeadターゲットへ遷移させます。
	 * @param i_serial
	 * RTターゲットのシリアル番号を示す値。この値は、{@link NyARRealityTarget#getSerialId()}で得られる値です。
	 * @return
	 * 成功すると、遷移したRTターゲットを返します。
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
	 * この関数は、UnKnownステータスのRTターゲット数を返します。
	 * @return
	 * Unknownステータスのターゲット数。
	 */
	public final int getNumberOfUnknown()
	{
		return this._number_of_unknown;
	}
	/**
	 * この関数は、KnownステータスのRTターゲット数を返します。
	 * @return
	 * Knownステータスのターゲット数。
	 */
	public final int getNumberOfKnown()
	{
		return this._number_of_known;
	}
	/**
	 * この関数は、DeadステータスのRTターゲット数を返します。
	 * @return
	 * Deadステータスのターゲット数。
	 */
	public final int getNumberOfDead()
	{
		return this._number_of_dead;
	}
	/**
	 * この関数は、RTターゲットリストへの参照値を返します。
	 * 得られたオブジェクトは、読出し専用です。直接操作しないでください。
	 * @return
	 * [READ　ONLY]RTターゲットリストへの参照値。
	 */
	public NyARRealityTargetList refTargetList()
	{
		return this.target;
	}

	/**
	 * この関数は、KnownステータスのRTターゲットを検索して、配列に返します。
	 * @param o_result
	 * 結果を格納する配列です。格納されるターゲットの最大数は、コンストラクタの設定値と同じです。
	 * 配列サイズが不足した場合は、発見した順に配列の個数だけ返します。
	 * @return
	 * 配列に格納したターゲットの数を返します。
	 */
	public int selectKnownTargets(NyARRealityTarget[] o_result)
	{
		return this.target.selectTargetsByType(NyARRealityTarget.RT_KNOWN, o_result);
	}
	/**
	 * この関数は、UnknownステータスのRTターゲットを検索して、配列に返します。
	 * @param o_result
	 * 結果を格納する配列です。格納されるターゲットの最大数は、コンストラクタの設定値と同じです。
	 * 配列サイズが不足した場合は、発見した順に配列の個数だけ返します。
	 * @return
	 * 配列に格納したターゲットの数を返します。
	 */
	public int selectUnKnownTargets(NyARRealityTarget[] o_result)
	{
		return this.target.selectTargetsByType(NyARRealityTarget.RT_UNKNOWN, o_result);
	}
	/**
	 * この関数は、UnknownステータスのRTターゲットを1個検索して返します。
	 * <p>注意-
	 * この関数を使うと、複数のUnknownステータスのRTターゲットがあるときに、その順番の影響で取りこぼしが発生することがあります。
	 * </p>
	 * @return
	 * 一番初めに発見したターゲットを返します。見つからないときはNULLです。
	 */
	public NyARRealityTarget selectSingleUnknownTarget()
	{
		return this.target.selectSingleTargetByType(NyARRealityTarget.RT_UNKNOWN);
	}
	/**
	 * この関数は、フラスタムオブジェクトの参照値を返します。
	 * フラスタムオブジェクトは、コンストラクタに与えたカメラパラメータから計算されます。
	 * @return
	 * [READ ONLY]フラスタムオブジェクト。
	 */
	public NyARFrustum refFrustum()
	{
		return this._frustum;
	}
	/**
	 * この関数は、ARToolKitスタイルの射影変換行列の参照値を返します。
	 * 射影変換行列は、コンストラクタに与えたカメラパラメータから計算されます。
	 * @return
	 * [READ ONLY]射影変換行列オブジェクト。
	 */
	public NyARPerspectiveProjectionMatrix refPerspectiveProjectionMatrix()
	{
		return this._ref_prjmat;
	}
	/**
	 * この関数は、画面座標系の4頂点でかこまれる領域を遠近法で自由変形して、o_rasterにRGB画像を取得します。
	 * @param i_vertex
	 * 四角形を定義する4頂点を格納した配列。
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * 2なら4ピクセル、4なら16ピクセルの入力から、出力1ピクセルを生成します。
	 * @param o_raster
	 * 出力先のラスタオブジェクト。
	 * @return
	 * 画像取得に成功するとtrue
	 * @throws NyARException
	 */
	public final boolean getRgbPatt2d(NyARRealitySource i_src,NyARIntPoint2d[] i_vertex,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		return i_src.refPerspectiveRasterReader().read4Point(i_src.refRgbSource(),i_vertex,0,0,i_resolution, o_raster);
	}
	/**
	 * この関数は、画面座標系の4頂点でかこまれる領域を遠近法で自由変形して、o_rasterにRGB画像を取得します。
	 * @param i_vertex
	 * 四角形を定義する4頂点を格納した配列。 
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * 2なら4ピクセル、4なら16ピクセルの入力から、出力1ピクセルを生成します。
	 * @param o_raster
	 * 出力先のラスタオブジェクト。
	 * @return
	 * 画像取得に成功するとtrue
	 * @throws NyARException
	 */
	public final boolean getRgbPatt2d(NyARRealitySource i_src,NyARDoublePoint2d[] i_vertex,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		return i_src.refPerspectiveRasterReader().read4Point(i_src.refRgbSource(),i_vertex,0,0,i_resolution, o_raster);
	}
	/**
	 * この関数は、RTターゲットの３次元座標系で定義される4頂点でかこまれる領域から、o_rasterにRGB画像を取得します。
	 * @param i_vertex
	 * RTターゲットの３次元座標系にある、四角形を定義する4頂点を格納した配列。 
	 * @param i_matrix
	 * i_vertexに適応する変換行列。
	 * ターゲットの姿勢行列を指定すると、ターゲット座標系になります。不要ならばnullを設定してください
	 * (nullの場合、カメラ座標系での定義になります。)
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * 2なら4ピクセル、4なら16ピクセルの入力から、出力1ピクセルを生成します。
	 * @param o_raster
	 * 出力先のラスタオブジェクト。
	 * @return
	 * 画像取得に成功するとtrue
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
