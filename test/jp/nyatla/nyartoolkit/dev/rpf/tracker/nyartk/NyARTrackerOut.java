package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;


class NyARRectTargetList extends NyARTargetList<NyARTarget>
{
	public NyARRectTargetList(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
	/**
	 * super classの機能に、予測位置からの探索を追加します。
	 */
	public int getMatchTargetIndex(LowResolutionLabelingSamplerOut.Item i_item)
	{
		//1段目:通常の検索
		int ret=super.getMatchTargetIndex(i_item);
		if(ret>=0){
			return ret;
		}
		//2段目:予測位置から検索
		NyARRectTargetStatus iitem;
		int min_d=Integer.MAX_VALUE;

		//対角範囲の距離が、対角距離の1/2以下で、最も小さいこと。
		for(int i=this._length-1;i>=0;i--)
		{
			iitem=(NyARRectTargetStatus)this._items[i].ref_status;
			int d;
			d=i_item.base_area.getSqDiagonalPointDiff(iitem.estimate_rect);	
			if(d<min_d){
				min_d=d;
				ret=i;
			}
		}
		//許容距離誤差の2乗を計算(対角線の20%以内)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/5)^2
		if(min_d<(2*(i_item.base_area_sq_diagonal)/25)){
			return ret;
		}
		return -1;
	}	
}




public class NyARTrackerOut
{
	public NyARNewTargetList newtarget;
	public NyARIgnoreTargetList igtarget;
	public NyARCoordTargetList coordtarget;
	public NyARRectTargetList recttarget;
//	public NyARMarkerTargetList markertarget;
	
	public NyARNewTargetStatusPool newst_pool;
	public NyARContourTargetStatusPool contourst_pool;
	public NyARTargetPool target_pool;
	public NyARRectTargetStatusPool rect_pool;
	
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