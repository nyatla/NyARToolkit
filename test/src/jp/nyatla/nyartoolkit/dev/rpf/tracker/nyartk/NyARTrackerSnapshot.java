package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.*;






public class NyARTrackerSnapshot
{
	public NyARNewTargetStatusPool newst_pool;
	public NyARContourTargetStatusPool contourst_pool;
	public NyARRectTargetStatusPool rect_pool;

	private NyARTargetPool target_pool;
	public NyARTargetList target_list;


	//環境定数
	private final int MAX_NUMBER_OF_NEW;
	private final int MAX_NUMBER_OF_CONTURE;
	private final int MAX_NUMBER_OF_RECT;
	private final int MAX_NUMBER_OF_TARGET;
	//IgnoreTargetの数は、NUMBER_OF_TARGETと同じです。


	/**
	 * 係数から、ターゲット数を計算する。
	 */
	public static int calculateNumberofTarget(int i_max_new,int i_max_cont,int i_max_rect)
	{
		return (i_max_new+i_max_cont+i_max_rect)*5;
	}

	/**
	 * コンストラクタです。
	 * @param i_max_new
	 * 同時に存在を許可するnewターゲットの数です。
	 * default=10
	 * @param i_max_cont
	 * 同時に存在を許可するcontourターゲットの数。
	 * 1ターンあたりのcontourターゲットへの遷移と、rectへの遷移数になる。
	 * default=1
	 * @param i_max_rect
	 * 同時に存在を許可するrectターゲットの数です。トラック数+1,2くらいを指定する。
	 * default=10
	 * @throws NyARException
	 */
	public NyARTrackerSnapshot(int i_max_new,int i_max_cont,int i_max_rect) throws NyARException
	{
		//環境定数の設定
		this.MAX_NUMBER_OF_NEW=i_max_new;
		this.MAX_NUMBER_OF_CONTURE=i_max_cont;
		this.MAX_NUMBER_OF_RECT=i_max_rect;
		this.MAX_NUMBER_OF_TARGET=calculateNumberofTarget(i_max_new,i_max_cont,i_max_rect);
		

		//ステータスプール
		this.newst_pool=new NyARNewTargetStatusPool(this.MAX_NUMBER_OF_NEW*2);
		this.contourst_pool=new NyARContourTargetStatusPool(MAX_NUMBER_OF_RECT+MAX_NUMBER_OF_CONTURE*2,160+120);
		this.rect_pool=new NyARRectTargetStatusPool(this.MAX_NUMBER_OF_RECT*2);
		//ターゲットプール
		this.target_pool=new NyARTargetPool(this.MAX_NUMBER_OF_TARGET);
		//ターゲット
		this.target_list=new NyARTargetList(this.MAX_NUMBER_OF_TARGET);
		
		this.number_of_new=0;
		this.number_of_ignore=0;
		this.number_of_contoure=0;
		this.number_of_rect=0;

	}
	private int number_of_new;
	private int number_of_ignore;
	private int number_of_contoure;
	private int number_of_rect;
	
	/**
	 * 新しいNewTargetを追加します。
	 * この関数はNyARTrackerが使用します。
	 * @param i_clock
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */
	public NyARTarget addNewTarget(long i_clock,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		//個数制限
		if(this.number_of_new>=this.MAX_NUMBER_OF_NEW){
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
		return;
	}
	/**
	 * このターゲットのステータスを、CntoureStatusへ遷移させます。
	 * @param i_c
	 */
	public final NyARTarget changeStatusToCntoure(NyARTarget i_target,NyARContourTargetStatus i_c)
	{
		//遷移元のステータスを制限
		assert(i_target.st_type==NyARTargetStatus.ST_NEW);
		//個数制限
		if(this.number_of_contoure>=this.MAX_NUMBER_OF_CONTURE){
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
	public final NyARTarget changeStatusToRect(NyARTarget i_target,NyARRectTargetStatus i_c)
	{
		assert(i_target.st_type==NyARTargetStatus.ST_CONTURE);
		if(this.number_of_rect>=this.MAX_NUMBER_OF_RECT){
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