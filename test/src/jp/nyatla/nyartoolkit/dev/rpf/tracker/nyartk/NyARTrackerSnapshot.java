package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.*;


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






public class NyARTrackerSnapshot
{
	public NyARTargetList<NyARTarget> target_list;

	public NyARNewTargetStatusPool newst_pool;
	public NyARContourTargetStatusPool contourst_pool;
	public NyARRectTargetStatusPool rect_pool;

	private NyARTargetPool target_pool;
	
	public final static int NUMBER_OF_NEW=10;
	public final static int NUMBER_OF_CONTURE=1;
	public final static int NUMBER_OF_IGNORE=100;
	public final static int NUMBER_OF_RECT=10;
	/**
	 * ターゲットリストの最大数。4リストのうち最大の数を指定すること。
	 */
	public final static int MAX_LIST_TARGET=100;

	public final static int NUMBER_OF_CONTURE_POOL=NUMBER_OF_RECT+NUMBER_OF_CONTURE*2;

	public NyARTrackerSnapshot() throws NyARException
	{
		//ステータスプール
		this.newst_pool=new NyARNewTargetStatusPool(NUMBER_OF_NEW*2);
		this.contourst_pool=new NyARContourTargetStatusPool(NUMBER_OF_CONTURE_POOL,160+120);
		this.rect_pool=new NyARRectTargetStatusPool(NUMBER_OF_RECT*2);
		//ターゲットプール
		this.target_pool=new NyARTargetPool(NUMBER_OF_NEW+NUMBER_OF_CONTURE+NUMBER_OF_IGNORE+NUMBER_OF_RECT);
		//ターゲット
		this.target_list=new NyARTargetList<NyARTarget>(NUMBER_OF_NEW+NUMBER_OF_CONTURE+NUMBER_OF_IGNORE+NUMBER_OF_RECT);
		
		this.number_of_new=0;
		this.number_of_ignore=0;
		this.number_of_contoure=0;
		this.number_of_rect=0;

	}
	private int number_of_new;
	private int number_of_ignore;
	private int number_of_contoure;
	private int number_of_rect;
	
	public NyARTarget addNewTarget(long i_clock,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		//個数制限
		if(this.number_of_new>=NUMBER_OF_NEW){
			return null;
		}
		//アイテム生成
		NyARTarget t=this.target_pool.newNewTarget();
		if(t==null){
			return null;
		}
		t.status_age=0;
		t.st_type=NyARTargetStatus.ST_NEW;
		t.last_update_tick=i_clock;
		t.setSampleArea(i_sample);
		t.ref_status=this.newst_pool.newObject();
		if(t.ref_status==null){
			t.releaseObject();
			return null;
		}
		((NyARNewTargetStatus)t.ref_status).setValue(i_sample);
		//ターゲットリストへ追加
		this.target_list.pushAssert(t);
		this.number_of_new++;
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
	public void deleatTarget(int i_index) throws NyARException
	{
		assert(this.target_list.getItem(i_index).st_type==NyARTargetStatus.ST_IGNORE);
		NyARTarget tr=this.target_list.getItem(i_index);
		this.target_list.removeIgnoreOrder(i_index);
		tr.releaseObject();
		this.number_of_ignore--;
		return;
	}
	
	/**
	 * このターゲットのステータスを、IgnoreStatusへ変更します。
	 * @throws NyARException 
	 */
	public final NyARTarget moveIgnoreStatus(NyARTarget i_target) throws NyARException
	{
		//遷移元のステータスを制限すること！
		assert( (i_target.st_type==NyARTargetStatus.ST_NEW) || 
				(i_target.st_type==NyARTargetStatus.ST_CONTURE) || 
				(i_target.st_type==NyARTargetStatus.ST_RECT));
		//個数制限
		if(this.number_of_new>=NUMBER_OF_IGNORE){
			return null;
		}

		//カウンタ更新
		switch(i_target.st_type)
		{
		case NyARTargetStatus.ST_NEW:
			this.number_of_new--;
			break;
		case NyARTargetStatus.ST_RECT:
			this.number_of_rect--;
			break;
		case NyARTargetStatus.ST_CONTURE:
			this.number_of_contoure--;
			break;
		default:
			throw new NyARException();
		}
		i_target.st_type=NyARTargetStatus.ST_IGNORE;
		i_target.ref_status.releaseObject();
		i_target.status_age=0;
		i_target.ref_status=null;
		this.number_of_ignore++;
		return i_target;
	}
	/**
	 * このターゲットのステータスを、CntoureStatusへ遷移させます。
	 * @param i_c
	 */
	public final NyARTarget moveCntoureStatus(NyARTarget i_target,NyARContourTargetStatus i_c)
	{
		//遷移元のステータスを制限
		assert(i_target.st_type==NyARTargetStatus.ST_NEW);
		//個数制限
		if(this.number_of_contoure>=NUMBER_OF_CONTURE){
			return null;
		}
		i_target.st_type=NyARTargetStatus.ST_CONTURE;
		i_target.ref_status.releaseObject();
		i_target.status_age=0;
		i_target.ref_status=i_c;
		//カウンタ更新
		this.number_of_new--;
		this.number_of_contoure++;
		return i_target;
	}
	public final NyARTarget moveRectStatus(NyARTarget i_target,NyARRectTargetStatus i_c)
	{
		assert(i_target.st_type==NyARTargetStatus.ST_CONTURE);
		if(this.number_of_rect>=NUMBER_OF_RECT){
			return null;
		}
		i_target.st_type=NyARTargetStatus.ST_RECT;
		i_target.ref_status.releaseObject();
		i_target.status_age=0;
		i_target.ref_status=i_c;
		//カウンタ更新
		this.number_of_contoure--;
		this.number_of_rect++;
		return i_target;
	}	
}