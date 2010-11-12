package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.*;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.*;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARDistMap;



/**
 * 四角形のトラッキングクラスです。
 * @author nyatla
 *
 */
public class NyARTracker
{
	private DistMap _map;
	protected int[] _index;	
	

	private NyARNewTargetStatusPool newst_pool;
	private NyARContourTargetStatusPool contourst_pool;
	private NyARRectTargetStatusPool rect_pool;

	private NyARTargetPool target_pool;
	/**
	 * ターゲットリストです。このプロパティは内部向けです。
	 * refTrackTarget関数を介してアクセスしてください。
	 */
	public NyARTargetList _targets;


	//環境定数
	private final int MAX_NUMBER_OF_NEW;
	private final int MAX_NUMBER_OF_CONTURE;
	private final int MAX_NUMBER_OF_RECT;
	private final int MAX_NUMBER_OF_TARGET;
	
	private SampleStack _newsource;
	private SampleStack _igsource;
	private SampleStack _coordsource;
	private SampleStack _rectsource;	
	public NyARTargetList[] _temp_targets;

	private int _number_of_new;
	private int _number_of_ignore;
	private int _number_of_contoure;
	private int _number_of_rect;	
	
	/**
	 * newターゲットの数を返します。
	 * @return
	 */
	public final int getNumberOfNew()
	{
		return this._number_of_new;		
	}
	/**
	 * ignoreターゲットの数を返します。
	 * @return
	 */
	public final int getNumberOfIgnore()
	{
		return this._number_of_ignore;		
	}
	/**
	 * contourターゲットの数を返します。
	 * @return
	 */
	public final int getNumberOfContur()
	{
		return this._number_of_contoure;		
	}
	/**
	 * rectターゲットの数を返します。
	 * @return
	 */
	public final int getNumberOfRect()
	{
		return this._number_of_rect;		
	}
	/**
	 * ターゲットリストの参照値を返します。
	 * @return
	 */
	public final NyARTargetList refTrackTarget()
	{
		return this._targets;
	}
	/**
	 * コンストラクタです。パラメータには、処理するNyARTrackerSnapshotに設定した値よりも大きな値を設定して下さい。
	 * @param i_max_new
	 * NyARTrackerSnapshotコンストラクタのi_max_newに設定した値を指定してください。
	 * @param i_max_cont
	 * NyARTrackerSnapshotコンストラクタのi_max_contに設定した値を指定してください。
	 * @param i_max_rect
	 * NyARTrackerSnapshotコンストラクタのi_max_rectに設定した値を指定してください。

	 * @throws NyARException
	 */
	public NyARTracker(int i_max_new,int i_max_cont,int i_max_rect) throws NyARException
	{
		//環境定数の設定
		this.MAX_NUMBER_OF_NEW=i_max_new;
		this.MAX_NUMBER_OF_CONTURE=i_max_cont;
		this.MAX_NUMBER_OF_RECT=i_max_rect;		
		this.MAX_NUMBER_OF_TARGET=(i_max_new+i_max_cont+i_max_rect)*5;		


		//ターゲットマップ用の配列と、リスト。この関数はNyARTargetStatusのIDと絡んでるので、気をつけて！
		this._temp_targets=new NyARTargetList[NyARTargetStatus.MAX_OF_ST_KIND+1];
		this._temp_targets[NyARTargetStatus.ST_NEW]    =new NyARTargetList(i_max_new);
		this._temp_targets[NyARTargetStatus.ST_IGNORE] =new NyARTargetList(this.MAX_NUMBER_OF_TARGET);
		this._temp_targets[NyARTargetStatus.ST_CONTURE]=new NyARTargetList(i_max_cont);
		this._temp_targets[NyARTargetStatus.ST_RECT]   =new NyARRectTargetList(i_max_rect);

		//ソースリスト
		this._newsource=new SampleStack(i_max_new);
		this._igsource=new SampleStack(this.MAX_NUMBER_OF_TARGET);
		this._coordsource=new SampleStack(i_max_cont);
		this._rectsource=new SampleStack(i_max_rect);

		//ステータスプール
		this.newst_pool=new NyARNewTargetStatusPool(i_max_new*2);
		this.contourst_pool=new NyARContourTargetStatusPool(i_max_rect+i_max_cont*2,160+120);
		this.rect_pool=new NyARRectTargetStatusPool(i_max_rect*2);
		//ターゲットプール
		this.target_pool=new NyARTargetPool(this.MAX_NUMBER_OF_TARGET);
		//ターゲット
		this._targets=new NyARTargetList(this.MAX_NUMBER_OF_TARGET);
		
		
		
		//ここ注意！マップの最大値は、ソースアイテムの個数よりおおきいこと！
		this._map=new DistMap(this.MAX_NUMBER_OF_TARGET,this.MAX_NUMBER_OF_TARGET);
		this._index=new int[this.MAX_NUMBER_OF_TARGET];
		
		//定数初期化
		this._number_of_new=this._number_of_ignore=this._number_of_contoure=this._number_of_rect=0;
		
	}

	/**
	 * Trackerの状態を更新します。
	 * @param i_source
	 * @throws NyARException
	 */
	public void progress(LrlsSource i_s) throws NyARException
	{
		NyARTargetList[] targets=this._temp_targets;
		NyARTargetList newtr=targets[NyARTargetStatus.ST_NEW];
		NyARTargetList igtr=targets[NyARTargetStatus.ST_IGNORE];
		NyARTargetList cotr=targets[NyARTargetStatus.ST_CONTURE];
		NyARTargetList retw=targets[NyARTargetStatus.ST_RECT];

		//ターゲットリストの振り分け
		NyARTarget[] target_array=this._targets.getArray();
		newtr.clear();
		igtr.clear();
		cotr.clear();
		retw.clear();
		for(int i=this._targets.getLength()-1;i>=0;i--){
			targets[target_array[i].st_type].pushAssert(target_array[i]);
		}		
		int[] index=this._index;
		//サンプルをターゲット毎に振り分け
		sampleMapper(
			i_s.samplerout,
			newtr,igtr,cotr,retw,
			this._newsource,this._igsource,this._coordsource,this._rectsource);
		
		//ターゲットの更新
		this._map.makePairIndexes(this._igsource,igtr,index);
		updateIgnoreStatus(igtr,this._igsource.getArray(),index);
		
		this._map.makePairIndexes(this._newsource,newtr,index);
		updateNewStatus(newtr,this.newst_pool, this._newsource.getArray(),index);

		this._map.makePairIndexes(this._rectsource,retw,index);
		updateRectStatus(retw, i_s, this.rect_pool, this._rectsource.getArray(),index);
		
		this._map.makePairIndexes(this._coordsource,cotr,index);
		updateContureStatus(cotr, i_s,this.contourst_pool,this._coordsource.getArray(),index);

		//ターゲットのアップグレード
		for(int i=this._targets.getLength()-1;i>=0;i--){
			switch(target_array[i].st_type){
			case NyARTargetStatus.ST_IGNORE:
				upgradeIgnoreTarget(i);
				continue;
			case NyARTargetStatus.ST_NEW:
				upgradeNewTarget(target_array[i],i_s);
				continue;
			case NyARTargetStatus.ST_RECT:
				upgradeRectTarget(target_array[i]);
				continue;
			case NyARTargetStatus.ST_CONTURE:
				upgradeContourTarget(target_array[i]);
				continue;
			}
		}
		return;
	}
	
	/**
	 * アップグレードパラメータ。NewからIgnoreへ遷移させるまでの待ち期間です。
	 */
	private static int UPGPARAM_NEW_TO_IGNORE_EXPIRE=50;
	/**
	 * アップグレードパラメータ。Ignoreを消失させるまでの待ち期間です。
	 */
	private static int IGNPARAM_EXPIRE_COUNT=5;
	
	private final static int UPGPARAM_CONTOUR_TO_RECT_EXPIRE=10;
	
	
	/**
	 * i_new_targetのアップグレードを試行します。
	 * アップグレードの種類は以下のにとおりです。1.一定期間経過後の破棄ルート(Ignoreへ遷移)2.正常認識ルート(Contourへ遷移)
	 * @param i_new_target
	 * @param i_base_raster
	 * @return
	 * @throws NyARException
	 */
	private final void upgradeNewTarget(NyARTarget i_new_target,LrlsSource i_sampler_in) throws NyARException
	{
		assert(i_new_target.st_type==NyARTargetStatus.ST_NEW);

		//寿命を超えたらignoreへ遷移
		if(i_new_target.status_age>UPGPARAM_NEW_TO_IGNORE_EXPIRE)
		{
			this.changeStatusToIgnore(i_new_target);
			return;
		}
		NyARNewTargetStatus st=(NyARNewTargetStatus)i_new_target.ref_status;
		//このターゲットをアップグレードできるか確認
		if(st.current_sampleout==null){
			//直近のsampleoutが無い。->なにもできない。
			return;
		}
		//coordステータスを生成
		NyARContourTargetStatus c=this.contourst_pool.newObject();
		if(c==null){
			//ターゲットがいっぱい。(失敗して何もしない)
			System.out.println("upgradeNewTarget:status pool full");
			return;
		}
		//ステータスの値をセット
		if(!c.setValue(i_sampler_in,st.current_sampleout))
		{
			//値のセットに失敗したので、Ignoreへ遷移(この対象は輪郭認識できない)
			this.changeStatusToIgnore(i_new_target);
			//System.out.println("drop:new->ignore[contoure failed.]"+t.serial+":"+t.last_update);
			c.releaseObject();
			return;//失敗しようが成功しようが終了
		}
		if(this.changeStatusToCntoure(i_new_target,c)==null){
			c.releaseObject();
			return;
		}
		return;
	}
	
	/**
	 * 指定したi_ig_targetをリストから削除します。
	 * リストは詰められますが、そのルールはdeleatTarget依存です。
	 * @param i_ig_index
	 * @throws NyARException
	 */
	private final void upgradeIgnoreTarget(int i_ig_index) throws NyARException
	{
		assert(this._targets.getItem(i_ig_index).st_type==NyARTargetStatus.ST_IGNORE);
		if(this._targets.getItem(i_ig_index).status_age>IGNPARAM_EXPIRE_COUNT)
		{
			//オブジェクトのリリース
//System.out.println("lost:ignore:"+t.serial+":"+t.last_update);
			this.deleatTarget(i_ig_index);
		}
	}
	
	/**
	 * NyARTrackerOutのCOntourTargetについて、アップグレード処理をします。
	 * アップグレードの種類は以下のにとおりです。1.一定期間経過後の破棄ルート(Ignoreへ遷移)2.正常認識ルート(Rectへ遷移)
	 * @param i_base_raster
	 * @param i_trackdata
	 * @throws NyARException
	 */
	private final void upgradeContourTarget(NyARTarget i_contoure_target) throws NyARException
	{
		assert(i_contoure_target.st_type==NyARTargetStatus.ST_CONTURE);
		if(i_contoure_target.status_age>UPGPARAM_CONTOUR_TO_RECT_EXPIRE)
		{
			//一定の期間が経過したら、ignoreへ遷移
			this.changeStatusToIgnore(i_contoure_target);
			return;
		}
		NyARContourTargetStatus st=(NyARContourTargetStatus)i_contoure_target.ref_status;
		//coordステータスを生成
		NyARRectTargetStatus c=this.rect_pool.newObject();
		if(c==null){
			//ターゲットがいっぱい。
			return;
		}
		//ステータスの値をセット
		if(!c.setValueWithInitialCheck(st,i_contoure_target.sample_area)){
			//値のセットに失敗した。
			c.releaseObject();
			return;
		}
		if(this.changeStatusToRect(i_contoure_target,c)==null){
			//ターゲットいっぱい？
			c.releaseObject();
			return;
		}
		return;
	}	
	private final void upgradeRectTarget(NyARTarget i_rect_target) throws NyARException
	{
		assert(i_rect_target.st_type==NyARTargetStatus.ST_RECT);
		if(i_rect_target.delay_tick>20)
		{
			this.changeStatusToIgnore(i_rect_target);
			//一定の期間updateができなければ、ignoreへ遷移
		}
	}	
	
	//
	//update
	//
	private final static void updateIgnoreStatus(NyARTargetList i_igliet,LowResolutionLabelingSamplerOut.Item[] source,int[] index)
	{
		NyARTarget d_ptr;
		//マップする。
		NyARTarget[] i_ignore_target=i_igliet.getArray();
		//ターゲットの更新
		for(int i=i_igliet.getLength()-1;i>=0;i--){
			d_ptr=i_ignore_target[i];
			int sample_index=index[i];
			//年齢を加算
			d_ptr.status_age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				d_ptr.delay_tick++;
				continue;
			}
			d_ptr.setSampleArea(source[sample_index]);
			d_ptr.delay_tick=0;
		}
	}	
		
	/**
	 * NewTargetのステータスを更新します。
	 * @param i_sample
	 * @throws NyARException 
	 */
	public final static void updateNewStatus(NyARTargetList i_list,NyARNewTargetStatusPool i_pool,LowResolutionLabelingSamplerOut.Item[] source,int[] index) throws NyARException
	{
		NyARTarget d_ptr;
		NyARTarget[] i_nes=i_list.getArray();		
		//ターゲットの更新
		for(int i=i_list.getLength()-1;i>=0;i--){
			d_ptr=i_nes[i];
			int sample_index=index[i];
			//年齢を加算
			d_ptr.status_age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				((NyARNewTargetStatus)d_ptr.ref_status).setValue(null);
				d_ptr.delay_tick++;
				continue;
			}
			LowResolutionLabelingSamplerOut.Item s=source[sample_index];
			//先にステータスを作成しておく
			NyARNewTargetStatus st=i_pool.newObject();
			if(st==null){
				//ステータスの生成に失敗
				d_ptr.delay_tick++;
//System.out.println("updateNewStatus:status pool full");
				continue;
			}
			//新しいステータス値のセット
			st.setValue(s);
			
			//ターゲットの更新
			d_ptr.setSampleArea(s);
			d_ptr.delay_tick=0;

			//ref_statusのセットと切り替え(失敗時の上書き防止のためにダブルバッファ化)
			d_ptr.ref_status.releaseObject();
			d_ptr.ref_status=st;
		}
	}
	/**
	 * ContoureTargetのステータスを更新します。
	 * @param i_base_raster
	 * @param i_trackdata
	 * @throws NyARException
	 */
	public static void updateContureStatus(NyARTargetList i_list,LrlsSource i_sample_in,NyARContourTargetStatusPool i_stpool,LowResolutionLabelingSamplerOut.Item[] source,int[] index) throws NyARException
	{
		NyARTarget[] crd=i_list.getArray();		
		NyARTarget d_ptr;
		//ターゲットの更新
		for(int i=i_list.getLength()-1;i>=0;i--){
			d_ptr=crd[i];
			int sample_index=index[i];
			//年齢を加算
			d_ptr.status_age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				d_ptr.delay_tick++;
				continue;
			}
			LowResolutionLabelingSamplerOut.Item s=source[sample_index];
			//失敗の可能性を考慮して、Statusを先に生成しておく
			NyARContourTargetStatus st=i_stpool.newObject();
			if(st==null){
				//失敗（作れなかった？）
				d_ptr.delay_tick++;
				continue;
			}
			if(!st.setValue(i_sample_in,s)){
				//新しいステータスのセットに失敗？
				st.releaseObject();
				d_ptr.delay_tick++;
				continue;
			}
			d_ptr.setSampleArea(s);
			d_ptr.delay_tick=0;
			//ref_statusの切り替え
			d_ptr.ref_status.releaseObject();
			d_ptr.ref_status=st;
		}
	}
	public static void updateRectStatus(NyARTargetList i_list,LrlsSource i_sample_in,NyARRectTargetStatusPool i_stpool,LowResolutionLabelingSamplerOut.Item[] source,int[] index) throws NyARException
	{	
		NyARTarget[] rct=i_list.getArray();
		NyARTarget d_ptr;
		//ターゲットの更新
		for(int i=i_list.getLength()-1;i>=0;i--){
			d_ptr=rct[i];
			//年齢を加算
			d_ptr.status_age++;
			//新しいステータスの作成
			NyARRectTargetStatus st=i_stpool.newObject();
			if(st==null){
				//失敗（作れなかった？）
				d_ptr.delay_tick++;
				continue;
			}
			int sample_index=index[i];
			LowResolutionLabelingSamplerOut.Item s=sample_index<0?null:source[sample_index];		
			if(!st.setValueByAutoSelect(i_sample_in, s, (NyARRectTargetStatus)d_ptr.ref_status)){
				st.releaseObject();
				d_ptr.delay_tick++;
				continue;
			}else{
				if(s!=null){
					d_ptr.setSampleArea(s);
				}
			}
			d_ptr.ref_status.releaseObject();
			d_ptr.ref_status=st;
			d_ptr.delay_tick=0;
		}		
	}

	/**
	 * ターゲットリストを参考に、sampleを振り分て、サンプルスタックに格納します。
	 * ターゲットは、rect>coord>new>ignoreの順に優先して振り分けられます。
	 * @param i_snapshot
	 * @param i_source
	 * @param i_new
	 * @param i_ig
	 * @param i_cood
	 * @param i_rect
	 * @param i_newsrc
	 * @param i_igsrc
	 * @param i_coodsrc
	 * @param i_rectsrc
	 * @throws NyARException
	 */
	private final void sampleMapper(
		LowResolutionLabelingSamplerOut i_source,
		NyARTargetList i_new,NyARTargetList i_ig,NyARTargetList i_cood,NyARTargetList i_rect,
		SampleStack i_newsrc,SampleStack i_igsrc,SampleStack i_coodsrc,SampleStack i_rectsrc) throws NyARException
	{
		//スタックを初期化
		i_newsrc.clear();
		i_coodsrc.clear();
		i_igsrc.clear();
		i_rectsrc.clear();
		//
		LowResolutionLabelingSamplerOut.Item[] sample_items=i_source.getArray();
		for(int i=i_source.getLength()-1;i>=0;i--)
		{
			//サンプラからの値を其々のターゲットのソースへ分配
			LowResolutionLabelingSamplerOut.Item sample_item=sample_items[i];
			int id;
			id=i_rect.getMatchTargetIndex(sample_item);
			if(id>=0){
				i_rectsrc.push(sample_item);
				continue;
			}
			//coord
			id=i_cood.getMatchTargetIndex(sample_item);
			if(id>=0){
				i_coodsrc.push(sample_item);
				continue;
			}
			//newtarget
			id=i_new.getMatchTargetIndex(sample_item);
			if(id>=0){
				i_newsrc.push(sample_item);
				continue;
			}
			//ignore target
			id=i_ig.getMatchTargetIndex(sample_item);
			if(id>=0){
				i_igsrc.push(sample_item);
				continue;
			}
			//マップできなかったものは、NewTragetへ登録(種類別のListには反映しない)
			NyARTarget t=this.addNewTarget(sample_item);
			if(t==null){
				continue;
			}
			i_newsrc.push(sample_item);
		}
		return;
	}
	//ターゲット操作系関数
	
	//IgnoreTargetの数は、NUMBER_OF_TARGETと同じです。





	
	/**
	 * 新しいNewTargetを追加します。
	 * @param i_clock
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */
	private final NyARTarget addNewTarget(LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		//個数制限
		if(this._number_of_new>=this.MAX_NUMBER_OF_NEW){
			return null;
		}
		//アイテム生成
		NyARTarget t=this.target_pool.newNewTarget();
		if(t==null){
			return null;
		}
		t.status_age=0;
		t.st_type=NyARTargetStatus.ST_NEW;
		t.delay_tick=0;
		t.setSampleArea(i_sample);
		t.ref_status=this.newst_pool.newObject();
		if(t.ref_status==null){
			t.releaseObject();
			return null;
		}
		((NyARNewTargetStatus)t.ref_status).setValue(i_sample);
		//ターゲットリストへ追加
		this._targets.pushAssert(t);
		this._number_of_new++;
		return t;
	}
	/**
	 * 指定したインデクスのターゲットをリストから削除します。
	 * ターゲットだけを外部から参照している場合など、ターゲットのindexが不明な場合は、
	 * ターゲットをignoreステータスに設定して、trackerのprogressを経由してdeleateを実行します。
	 * @param i_index
	 * @return
	 * @throws NyARException
	 */
	private final void deleatTarget(int i_index) throws NyARException
	{
		assert(this._targets.getItem(i_index).st_type==NyARTargetStatus.ST_IGNORE);
		NyARTarget tr=this._targets.getItem(i_index);
		this._targets.removeIgnoreOrder(i_index);
		tr.releaseObject();
		this._number_of_ignore--;
		return;
	}
	
	/**
	 * このターゲットのステータスを、IgnoreStatusへ変更します。
	 * @throws NyARException 
	 */
	public final void changeStatusToIgnore(NyARTarget i_target) throws NyARException
	{
		//遷移元のステータスを制限すること！
		assert( (i_target.st_type==NyARTargetStatus.ST_NEW) || 
				(i_target.st_type==NyARTargetStatus.ST_CONTURE) || 
				(i_target.st_type==NyARTargetStatus.ST_RECT));

		//カウンタ更新
		switch(i_target.st_type)
		{
		case NyARTargetStatus.ST_NEW:
			this._number_of_new--;
			break;
		case NyARTargetStatus.ST_RECT:
			this._number_of_rect--;
			break;
		case NyARTargetStatus.ST_CONTURE:
			this._number_of_contoure--;
			break;
		default:
			return;
		}
		i_target.st_type=NyARTargetStatus.ST_IGNORE;
		i_target.ref_status.releaseObject();
		i_target.status_age=0;
		i_target.ref_status=null;
		this._number_of_ignore++;
		return;
	}
	/**
	 * このターゲットのステータスを、CntoureStatusへ遷移させます。
	 * @param i_c
	 */
	private final NyARTarget changeStatusToCntoure(NyARTarget i_target,NyARContourTargetStatus i_c)
	{
		//遷移元のステータスを制限
		assert(i_target.st_type==NyARTargetStatus.ST_NEW);
		//個数制限
		if(this._number_of_contoure>=this.MAX_NUMBER_OF_CONTURE){
			return null;
		}
		i_target.st_type=NyARTargetStatus.ST_CONTURE;
		i_target.ref_status.releaseObject();
		i_target.status_age=0;
		i_target.ref_status=i_c;
		//カウンタ更新
		this._number_of_new--;
		this._number_of_contoure++;
		return i_target;
	}
	/**
	 * このターゲットをRectターゲットに遷移させます。
	 * @param i_target
	 * @param i_c
	 * @return
	 */
	private final NyARTarget changeStatusToRect(NyARTarget i_target,NyARRectTargetStatus i_c)
	{
		assert(i_target.st_type==NyARTargetStatus.ST_CONTURE);
		if(this._number_of_rect>=this.MAX_NUMBER_OF_RECT){
			return null;
		}
		i_target.st_type=NyARTargetStatus.ST_RECT;
		i_target.ref_status.releaseObject();
		i_target.status_age=0;
		i_target.ref_status=i_c;
		//カウンタ更新
		this._number_of_contoure--;
		this._number_of_rect++;
		return i_target;
	}

	
}

/**
 * サンプルを格納するスタックです。このクラスは、一時的なリストを作るために使います。
 */
final class SampleStack extends NyARPointerStack<LowResolutionLabelingSamplerOut.Item>
{
	protected SampleStack(int i_size) throws NyARException
	{
		super();
		this.initInstance(i_size,LowResolutionLabelingSamplerOut.Item.class);
	}
}


/**
 * NyARTargetとSampleStack.Item間の、点間距離マップを作製するクラスです。
 * スーパークラスから、setPointDists関数をオーバライドします。
 *
 */
final class DistMap extends NyARDistMap
{
	public DistMap(int i_max_col,int i_max_row)
	{
		super(i_max_col,i_max_row);
	}
	public void makePairIndexes(SampleStack igsource, NyARTargetList igtr,int[] index)
	{
		this.setPointDists(igsource.getArray(),igsource.getLength(),igtr.getArray(),igtr.getLength());
		this.getMinimumPair(index);
		return;
	}
	/**
	 * ２ペアの点間距離を計算します。
	 * getMinimumPairで求まるインデクスは、NyARTargetに最も一致するLowResolutionLabelingSamplerOut.Itemのインデックスになります。
	 * @param i_sample
	 * @param i_smp_len
	 * @param i_target
	 * @param i_target_len
	 */
	public void setPointDists(LowResolutionLabelingSamplerOut.Item[] i_sample,int i_smp_len,NyARTarget[] i_target,int i_target_len)
	{
		NyARDistMap.DistItem[] map=this._map;
		//distortionMapを作成。ついでに最小値のインデクスも取得
		int min_index=0;
		int min_dist =Integer.MAX_VALUE;
		int idx=0;
		for(int r=0;r<i_smp_len;r++){
			for(int c=0;c<i_target_len;c++){
				map[idx].col=c;
				map[idx].row=r;
				//中央座標の距離？
				int d=i_target[c].sample_area.sqDiagonalPointDiff(i_sample[r].base_area);
				map[idx].dist=d;
				if(min_dist>d){
					min_index=idx;
					min_dist=d;
				}
				idx++;
			}
		}
		this._min_dist=min_dist;
		this._min_dist_index=min_index;
		this._size_col=i_smp_len;
		this._size_row=i_target_len;
		return;
	}			
}
