package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
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






public class NyARTrackerOut
{
	public NyARNewTargetList newtarget;
	public NyARIgnoreTargetList igtarget;
	public NyARCoordTargetList coordtarget;
	public NyARRectTargetList recttarget;
	
	public NyARNewTargetStatusPool newst_pool;
	public NyARContourTargetStatusPool contourst_pool;
	public NyARRectTargetStatusPool rect_pool;

	public NyARTargetPool target_pool;
	
	public final static int NUMBER_OF_NEW=10;
	public final static int NUMBER_OF_CONTURE=1;
	public final static int NUMBER_OF_IGNORE=100;
	public final static int NUMBER_OF_RECT=10;

	public final static int NUMBER_OF_CONTURE_POOL=NUMBER_OF_RECT+NUMBER_OF_CONTURE*2;

	public NyARTrackerOut() throws NyARException
	{
		//ステータスプール
		this.newst_pool=new NyARNewTargetStatusPool(NUMBER_OF_NEW*2);
		this.contourst_pool=new NyARContourTargetStatusPool(NUMBER_OF_CONTURE_POOL,160+120);
		this.rect_pool=new NyARRectTargetStatusPool(NUMBER_OF_RECT*2);
		//ターゲットプール
		this.target_pool=new NyARTargetPool(NUMBER_OF_NEW+NUMBER_OF_CONTURE+NUMBER_OF_IGNORE,this.newst_pool);
		//ターゲット
		this.newtarget=new NyARNewTargetList(NUMBER_OF_NEW);
		this.igtarget=new NyARIgnoreTargetList(NUMBER_OF_IGNORE);
		this.coordtarget=new NyARCoordTargetList(NUMBER_OF_CONTURE);
		this.recttarget=new NyARRectTargetList(NUMBER_OF_RECT);
	}	
}