package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourDataPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObjectPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObject.INyARManagedObjectPoolOperater;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARContourTargetStatus.CoordData;

class NyARRectTargetList extends NyARTargetList<NyARTarget>
{
	public NyARRectTargetList(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}	
}


/*
 * 輪郭情報を保管します。
 * このクラスの要素は、他の要素から参照する可能性があります。
 */
class NyARRectTargetStatusPool extends NyARManagedObjectPool<NyARRectTargetStatus>
{	

	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @param i_cood_max
	 * 輪郭ベクトルの最大数
	 * @throws NyARException
	 */
	public NyARRectTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARRectTargetStatus.class);
	}
	protected NyARRectTargetStatus createElement()
	{
		return new NyARRectTargetStatus(this._inner_pool);
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
	
	public final static int NUMBER_OF_NEW=3;
	public final static int NUMBER_OF_CONTURE=3;
	public final static int NUMBER_OF_IGNORE=100;
	public final static int NUMBER_OF_RECT=8;

	public final static int NUMBER_OF_CONTURE_POOL=NUMBER_OF_RECT+NUMBER_OF_CONTURE*2;

	public NyARTrackerOut() throws NyARException
	{
		//ステータスプール
		this.newst_pool=new NyARNewTargetStatusPool(NUMBER_OF_CONTURE*2);
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