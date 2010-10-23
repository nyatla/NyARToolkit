package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import java.awt.ItemSelectable;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.*;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.*;


/**
 * サンプルを格納するスタックです。
 */
class SampleStack extends NyARPointerStack<LowResolutionLabelingSamplerOut.Item>
{
	protected SampleStack(int i_size) throws NyARException
	{
		super();
		this.initInstance(i_size,LowResolutionLabelingSamplerOut.Item.class);
	}
}

class NyARCoordTargetList extends NyARTargetList<NyARTarget>
{
	/**
	 * コンストラクタです。NyARContourStatusのNyARTargetのリストクラスを生成します。
	 * @param iMaxTarget
	 * 取り扱うターゲットの最大数
	 * @throws NyARException
	 */
	public NyARCoordTargetList(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
}
class NyARIgnoreTargetList extends NyARTargetList<NyARTarget>
{
	public NyARIgnoreTargetList(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
}


class NyARNewTargetList extends NyARTargetList<NyARTarget>
{
	/**
	 * コンストラクタです。NewTargetステータスのNyARTargetオブジェクトをリストします。
	 * @param iMaxTarget
	 * 取り扱うターゲットの最大数。
	 * @throws NyARException
	 */
	public NyARNewTargetList(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
}







public class NyARTracker
{
	private DistMap _map;
	protected int[] _index;	
	
	
	private long _clock=0;

	
	private SampleStack _newsource;
	private SampleStack _igsource;
	private SampleStack _coordsource;
	private SampleStack _rectsource;

	/**
	 * コンストラクタです。
	 * @throws NyARException
	 */
	public NyARTracker() throws NyARException
	{
		//ソースターゲット
		this._newsource=new SampleStack(NyARTrackerOut.NUMBER_OF_NEW);
		this._igsource=new SampleStack(NyARTrackerOut.NUMBER_OF_IGNORE);
		this._coordsource=new SampleStack(NyARTrackerOut.NUMBER_OF_CONTURE);
		this._rectsource=new SampleStack(NyARTrackerOut.NUMBER_OF_RECT);
		//ここ注意！マップの最大値は、ソースアイテムの個数よりおおきいこと！
		this._map=new DistMap(20,200);
		this._index=new int[200];
		
	}
	/**
	 * i_trackdataの状態を更新します。
	 * @param i_source
	 * @throws NyARException
	 */
	public void progress(LowResolutionLabelingSamplerOut i_source,NyARTrackerOut i_trackdata) throws NyARException
	{
		//クロック進行
		this._clock++;

		//サンプルのマッピング
		sampleMapper(i_source,i_trackdata);
		
		//ターゲットステータスの更新
		updateRectStatus(i_source.ref_base_raster,i_trackdata);
		updateContureStatus(i_source.ref_base_raster,i_trackdata);
		updateNewStatus(i_trackdata);
		updateIgnoreStatus(i_trackdata);
		//ターゲットのアップグレード
		upgradeRectTarget(i_trackdata);
		upgradeContourTarget(i_trackdata);
		upgradeNewTarget(i_source.ref_base_raster,i_trackdata);
		upgradeIgnoreTarget(i_trackdata);
	}
	
	/**
	 * アップグレードパラメータ。NewからIgnoreへ遷移させるまでの待ち期間です。
	 */
	private static int UPGPARAM_NEW_TO_IGNORE_EXPIRE=50;
	/**
	 * アップグレードパラメータ。Ignoreを消失させるまでの待ち期間です。
	 */
	private static int IGNPARAM_EXPIRE_COUNT=5;
	
	
	
	/**
	 * NyARTrackerOutのNewtargetについて、アップグレード処理をします。
	 * アップグレードの種類は以下のにとおりです。1.一定期間経過後の破棄ルート(Ignoreへ遷移)2.正常認識ルート(Contourへ遷移)
	 * @param i_base_raster
	 * @param i_trackdata
	 * @throws NyARException
	 */
	private void upgradeNewTarget(LrlsGsRaster i_base_raster,NyARTrackerOut i_trackdata) throws NyARException
	{
		long clock=this._clock;
		int len_of_new;
		NyARTarget[] array_of_new=i_trackdata.newtarget.getArray();
		
		len_of_new=i_trackdata.newtarget.getLength();
		for(int i=len_of_new-1;i>=0;i--){
			NyARTarget t=array_of_new[i];
			if(t.age>UPGPARAM_NEW_TO_IGNORE_EXPIRE)
			{
				//一定の期間が経過したら、ignoreへ遷移
				if(i_trackdata.igtarget.push(t)!=null){
					//成功。ステータスの切り替えて、このターゲットのリストから外す。
					t.setIgnoreStatus(clock);
					i_trackdata.newtarget.removeIgnoreOrder(i);	
System.out.println("drop:new->ignore"+t.serial+":"+t.last_update);
				}else{
					//追加失敗→何もせず
					break;
				}
			}
		}
		//リストの編集をしたので再度長さを取得
		len_of_new=i_trackdata.newtarget.getLength();
		//newtargetのいくつかをcoordtargetへ遷移。
		for(int i=len_of_new-1;i>=0;i--)
		{
			NyARTarget t=array_of_new[i];
			NyARNewTargetStatus st=(NyARNewTargetStatus)t.ref_status;
			//このターゲットをアップグレードできるか確認
			if(st.current_sampleout==null){
				//直近のsampleoutが無い。->なにもできない。
				continue;
			}
			//coordステータスを生成
			NyARContourTargetStatus c=i_trackdata.contourst_pool.newObject();
			if(c==null){
				//ターゲットがいっぱい。
				System.out.println("upgradeNewTarget:status pool full");
				break;
			}
			//ステータスの値をセット
			if(!c.setValue(st.current_sampleout)){
				//値のセットに失敗したので、Ignoreへ遷移(この対象は輪郭認識できない)
				if(i_trackdata.igtarget.push(t)!=null){
					t.setIgnoreStatus(clock);
					i_trackdata.newtarget.removeIgnoreOrder(i);	
System.out.println("drop:new->ignore[contoure failed.]"+t.serial+":"+t.last_update);
				}
				c.releaseObject();
				continue;
			}
			if(i_trackdata.coordtarget.push(t)==null){
				//追加失敗。生成したステータスを破棄
				c.releaseObject();
				break;
			}else{
				//成功。ステータスを切り替えて、このターゲットリストから外す。
				t.setCntoureStatus(c);
				i_trackdata.newtarget.removeIgnoreOrder(i);
System.out.println("upgr:new->coord"+t.serial+":"+t.last_update);
				
			}
		}
	}
	
	
	private void upgradeIgnoreTarget(NyARTrackerOut i_trackdata)
	{
		NyARTarget[] array_of_ign=i_trackdata.igtarget.getArray();
		int len_of_ign=i_trackdata.igtarget.getLength();
		for(int i=len_of_ign-1;i>=0;i--){
			NyARTarget t=array_of_ign[i];
			if(t.age>IGNPARAM_EXPIRE_COUNT)
			{
				//オブジェクトのリリース
System.out.println("lost:ignore:"+t.serial+":"+t.last_update);
				t.releaseObject();
				i_trackdata.igtarget.removeIgnoreOrder(i);				
			}
		}
	}
	int UPGPARAM_CONTOUR_TO_RECT_EXPIRE=10;
	
	/**
	 * NyARTrackerOutのCOntourTargetについて、アップグレード処理をします。
	 * アップグレードの種類は以下のにとおりです。1.一定期間経過後の破棄ルート(Ignoreへ遷移)2.正常認識ルート(Rectへ遷移)
	 * @param i_base_raster
	 * @param i_trackdata
	 * @throws NyARException
	 */
	private void upgradeContourTarget(NyARTrackerOut i_trackdata) throws NyARException
	{
		long clock=this._clock;
		int len_of_cont;
		NyARTarget[] array_of_new=i_trackdata.coordtarget.getArray();
		len_of_cont=i_trackdata.coordtarget.getLength();
		for(int i=len_of_cont-1;i>=0;i--){
			NyARTarget t=array_of_new[i];
			if(t.age>UPGPARAM_CONTOUR_TO_RECT_EXPIRE)
			{
				//一定の期間が経過したら、ignoreへ遷移
				if(i_trackdata.igtarget.push(t)!=null){
					//成功。ステータスの切り替えて、このターゲットのリストから外す。
					t.setIgnoreStatus(clock);
					i_trackdata.coordtarget.removeIgnoreOrder(i);	
				}else{
					//追加失敗→何もせず
					break;
				}
			}
		}
		//リストの編集をしたので再度長さを取得
		len_of_cont=i_trackdata.coordtarget.getLength();
		for(int i=len_of_cont-1;i>=0;i--)
		{
			NyARTarget t=array_of_new[i];
			NyARContourTargetStatus st=(NyARContourTargetStatus)t.ref_status;
			//coordステータスを生成
			NyARRectTargetStatus c=i_trackdata.rect_pool.newObject();
			if(c==null){
				//ターゲットがいっぱい。
				break;
			}
			//ステータスの値をセット
			if(!c.setValueWithInitialCheck(st,t.sample_area)){
				//値のセットに失敗した。
				c.releaseObject();
				continue;
			}
			if(i_trackdata.recttarget.push(t)==null){
				//追加失敗。生成したステータスを破棄
				c.releaseObject();
				break;
			}
			//成功。ステータスを切り替えて、このターゲットリストから外す。
			//メモ：stは既にc.setValueで参照+1されているので、setRectStatusが-1してもOK
			t.setRectStatus(c);
			i_trackdata.coordtarget.removeIgnoreOrder(i);
		}
	}	
	private void upgradeRectTarget(NyARTrackerOut i_trackdata) throws NyARException
	{
		long clock=this._clock;
		int len_of_rect;
		NyARTarget[] array_of_new=i_trackdata.recttarget.getArray();
		len_of_rect=i_trackdata.recttarget.getLength();
		for(int i=len_of_rect-1;i>=0;i--){
			NyARTarget t=array_of_new[i];
			if(clock-t.last_update>20)
			{
				//一定の期間が経過したら、ignoreへ遷移
				if(i_trackdata.igtarget.push(t)!=null){
					//成功。ステータスの切り替えて、このターゲットのリストから外す。
					t.setIgnoreStatus(clock);
					i_trackdata.recttarget.removeIgnoreOrder(i);	
				}else{
					//追加失敗→何もせず
System.out.println("upgradeRectTarget:ignore pool full");
					break;
				}
			}
		}
	}	
	
	//
	//update
	//
	
	/**
	 * このターゲットを、SampleStackを使って更新します。
	 * @param i_sample
	 */
	public void updateIgnoreStatus(NyARTrackerOut i_trackdata)
	{
		long clock=this._clock;
		LowResolutionLabelingSamplerOut.Item[] source=this._igsource.getArray();
		NyARTarget[] igs=i_trackdata.igtarget.getArray();
		int len_of_igs=i_trackdata.igtarget.getLength();
		NyARTarget d_ptr;
		//マップする。
		this._map.setPointDists(source,this._igsource.getLength(),igs,len_of_igs);
		this._map.getMinimumPair(this._index);
		//ターゲットの更新
		for(int i=len_of_igs-1;i>=0;i--){
			d_ptr=igs[i];
			int sample_index=this._index[i];
			//年齢を加算
			d_ptr.age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				continue;
			}
			d_ptr.setSampleArea(source[sample_index]);
			d_ptr.last_update=clock;
		}
	}		
	/**
	 * NewTargetのステータスを更新します。
	 * @param i_sample
	 * @throws NyARException 
	 */
	public void updateNewStatus(NyARTrackerOut i_trackdata) throws NyARException
	{
		long clock=this._clock;
		LowResolutionLabelingSamplerOut.Item[] source=this._newsource.getArray();
		NyARTarget[] nes=i_trackdata.newtarget.getArray();
		int len_of_nes=i_trackdata.newtarget.getLength();		
		NyARTarget d_ptr;
		//マップする。
		this._map.setPointDists(source,this._newsource.getLength(),nes,len_of_nes);
		this._map.getMinimumPair(this._index);
		//ターゲットの更新
		for(int i=len_of_nes-1;i>=0;i--){
			d_ptr=nes[i];
			int sample_index=this._index[i];
			//年齢を加算
			d_ptr.age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				((NyARNewTargetStatus)d_ptr.ref_status).setValue(null);
				continue;
			}
			LowResolutionLabelingSamplerOut.Item s=source[sample_index];
			//先にステータスを作成しておく
			NyARNewTargetStatus st=i_trackdata.newst_pool.newObject();
			if(st==null){
				//ステータスの生成に失敗
System.out.println("updateNewStatus:status pool full");
				continue;
			}
			//新しいステータス値のセット
			st.setValue(s);
			
			//ターゲットの更新
			d_ptr.setSampleArea(s);
			d_ptr.last_update=clock;

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
	public void updateContureStatus(LrlsGsRaster i_base_raster,NyARTrackerOut i_trackdata) throws NyARException
	{
		long clock=this._clock;
		LowResolutionLabelingSamplerOut.Item[] source=this._coordsource.getArray();
		NyARTarget[] crd=i_trackdata.coordtarget.getArray();
		int len_of_crd=i_trackdata.coordtarget.getLength();		
		
		NyARTarget d_ptr;
		//マップする。
		this._map.setPointDists(source,this._coordsource.getLength(),crd,len_of_crd);
		this._map.getMinimumPair(this._index);
		//ターゲットの更新
		for(int i=len_of_crd-1;i>=0;i--){
			d_ptr=crd[i];
			int sample_index=this._index[i];
			//年齢を加算
			d_ptr.age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				continue;
			}
			LowResolutionLabelingSamplerOut.Item s=source[sample_index];
			//失敗の可能性を考慮して、Statusを先に生成しておく
			NyARContourTargetStatus st=i_trackdata.contourst_pool.newObject();
			if(st==null){
				//失敗（作れなかった？）
				continue;
			}
			if(!st.setValue(s)){
				//新しいステータスのセットに失敗？
				st.releaseObject();
				continue;
			}
//[DEBUG:]st.vecpos.margeResembleCoordIgnoreOrder();
			d_ptr.setSampleArea(s);
			d_ptr.last_update=clock;
			//ref_statusの切り替え
			d_ptr.ref_status.releaseObject();
			d_ptr.ref_status=st;
		}
	}
	public void updateRectStatus(LrlsGsRaster i_base_raster,NyARTrackerOut i_trackdata) throws NyARException
	{
		long clock=this._clock;
		LowResolutionLabelingSamplerOut.Item[] source=this._rectsource.getArray();
		NyARTarget[] rct=i_trackdata.recttarget.getArray();
		int len_of_rct=i_trackdata.recttarget.getLength();		
		
		NyARTarget d_ptr;
		//マップする。
		this._map.setPointDists(source,this._rectsource.getLength(),rct,len_of_rct);
		this._map.getMinimumPair(this._index);
		//ターゲットの更新
		for(int i=len_of_rct-1;i>=0;i--){
			d_ptr=rct[i];
			//年齢を加算
			d_ptr.age++;
			//新しいステータスの作成
			NyARRectTargetStatus st=i_trackdata.rect_pool.newObject();
			if(st==null){
				//失敗（作れなかった？）
				continue;
			}
			//{単独検出を試行}
			if(st.setValue(i_base_raster,(NyARRectTargetStatus)d_ptr.ref_status)){
				//単独検出に成功
				d_ptr.sample_area.setAreaRect(st.square.sqvertex,4);
			}else{
		st.releaseObject();
				continue;}
/*				int sample_index=this._index[i];
				if(sample_index<0){
					//このターゲットに合致するアイテムは無い。
					st.releaseObject();
					continue;
				}
				LowResolutionLabelingSamplerOut.Item s=source[sample_index];
				//失敗の可能性を考慮して、Statusを先に生成しておく
				//ステータスを更新
				if(!st.setValueWithDeilyCheck(i_base_raster,s,(NyARRectTargetStatus)d_ptr.ref_status)){
					//新しいステータスのセットに失敗？
					st.releaseObject();
					continue;
				}
				//ref_statusの切り替え
//				d_ptr.setSampleArea(st.square.sqvertex);//ラベルの方がいいかもー
				d_ptr.setSampleArea(s);
			}
//			*/
			d_ptr.ref_status.releaseObject();
			d_ptr.ref_status=st;
			d_ptr.last_update=clock;
		}		
	}

	private void sampleMapper(LowResolutionLabelingSamplerOut i_source,NyARTrackerOut i_trackdata) throws NyARException
	{
		//スタックを初期化
		this._newsource.clear();
		this._coordsource.clear();
		this._igsource.clear();
		this._rectsource.clear();
		LowResolutionLabelingSamplerOut.Item[] sample_items=i_source.getArray();
		for(int i=i_source.getLength()-1;i>=0;i--)
		{
			//サンプラからの値を其々のターゲットのソースへ分配
			LowResolutionLabelingSamplerOut.Item sample_item=sample_items[i];
			int id;
			id=i_trackdata.recttarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._rectsource.push(sample_item);
				continue;
			}
			//coord
			id=i_trackdata.coordtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._coordsource.push(sample_item);
				continue;
			}
			//newtarget
			id=i_trackdata.newtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._newsource.push(sample_item);
				continue;
			}
			//ignore target
			id=i_trackdata.igtarget.getMatchTargetIndex(sample_item);
			if(id>=0){
				this._igsource.push(sample_item);
				continue;
			}
			//マップできなかったものは、NewTragetへ登録
			NyARTarget t=i_trackdata.target_pool.newNewTarget(this._clock,sample_item);
			if(t==null){
				continue;
			}
			if(i_trackdata.newtarget.push(t)==null){
				//todo:次回に重複チェックでが発生するけど、どうしようか。回避するには、一旦エントリリストにしないといけないね？
				//newtargetへの遷移が失敗したら、ターゲットを解放。
				t.releaseObject();
			}
		}
		return;
	}	
}



/**
 * NyARTargetとSampleStack.Item間の、点間距離マップを作製するクラスです。
 * スーパークラスから、setPointDists関数をオーバライドします。
 *
 */
class DistMap extends NyARDistMap
{
	public DistMap(int i_max_col,int i_max_row)
	{
		super(i_max_col,i_max_row);
	}
	/**
	 * ２つのターゲット間の距離値を計算します。
	 * 距離は、範囲矩形の対角頂点動詞の和です。
	 *  d=(l1-l2)^2+(t1-t2)^2+ ((l1+w1)-(l2+w2))^2+((t1+h1)-(t2+h2))^2;
	 *   =(- w2 + w1 - l2 + l1)^2  + (- t2 + t1 - h2 + h1)^2  + (t1 - t2)^2  + (l1 - l2)^2
	 * @param i_target
	 * @param i_sample
	 * @return
	 */
	private int dist(NyARTarget i_target,LowResolutionLabelingSamplerOut.Item i_sample)
	{
		int a=i_target.sample_area.y-i_sample.base_area.y;//t1 - t2
		int b=i_target.sample_area.x-i_sample.base_area.x;//l1 - l2
		
		//(- w2 + w1 +b)^2  + (a - h2 + h1)^2  + (a)^2  + (b)^2
		int v1=(-i_sample.base_area.w+i_target.sample_area.w+b);
		int v2=(a-i_sample.base_area.h+i_target.sample_area.h);
		return v1*v1+v2*v2+a*a+b*b;
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
				int d=dist(i_target[c], i_sample[r]);
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
