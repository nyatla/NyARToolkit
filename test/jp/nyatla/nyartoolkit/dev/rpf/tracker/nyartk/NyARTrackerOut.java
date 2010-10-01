package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
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
class NyARRectTargetStatus extends NyARTargetStatus
{
	/**
	 * 輪郭ターゲット
	 */
	public NyARContourTargetStatus _contoure;
	
	/**
	 * 
	 */
	public NyARSquare square;

	/**
	 * ベクトル要素を格納する配列です。
	 */
	public CoordData[] vecpos=CoordData.createArray(100);
	/**
	 * ベクトル配列の有効長です。
	 */
	public int vecpos_length;
	//
	//制御部
	
	/**
	 * @Override
	 */
	public NyARRectTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		super(i_ref_pool_operator);
		this.square=new NyARSquare();
		this._contoure=null;
	}
	/**
	 * i_contour_statusをソースにして、メンバ変数を更新します。
	 * @param i_contour_status
	 * @return
	 */
	private boolean updateParam(NyARContourTargetStatus i_contour_status)
	{
		int[] indexbuf=new int[4];
		//4線分抽出
		if(i_contour_status.vecpos_length<4){
			return false;
		}
		i_contour_status.getKeyCoordIndexes(i_contour_status.vecpos_length, indexbuf);
		//[省略]正当性チェック？(もしやるなら輪郭抽出系にも手を加えないと。)
		NyARSquare sq=this.square;
		//4頂点を計算する。(本当はベクトルの方向を調整してから計算するべき)
//		for(int i=3;i>=0;i--){
//			NyARContourTargetStatus.CoordData cv=i_contour_status.vecpos[indexbuf[i]];
//			sq.line[i].setVector(cv.dx,cv.dy,cv.x,cv.y);
//		}
		//4点抽出
		for(int i=3;i>=0;i--){
//			if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
//				//四角が作れない。
//				return false;
//			}
			sq.sqvertex[i].x=i_contour_status.vecpos[indexbuf[i]].x;
			sq.sqvertex[i].y=i_contour_status.vecpos[indexbuf[i]].y;
		}
		return true;
	}
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_contour_status
	 * @return
	 * @throws NyARException
	 */

	public boolean setValue(NyARContourTargetStatus i_contour_status) throws NyARException
	{
		updateParam(i_contour_status);
		//参照する値の差し替え
		if(this._contoure!=null){
			this._contoure.releaseObject();
		}
		this._contoure=(NyARContourTargetStatus)i_contour_status.refObject();
		return true;
	}
	public boolean setValue(NyARContourTargetStatusPool i_pool, NyARGrayscaleRaster i_raster,LowResolutionLabelingSamplerOut.Item i_source) throws NyARException
	{
		NyARContourTargetStatus s=i_pool.newObject();
		if(s==null){
			return false;
		}
		if(!s.setValue(i_raster, i_source)){
			s.releaseObject();
			return false;
		}
		if(!updateParam(s)){
			s.releaseObject();
			return false;
		}
		if(this._contoure!=null){
			this._contoure.releaseObject();
		}
		this._contoure=s;
		return true;
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
	public final static int NUMBER_OF_IGNORE=10;
	public final static int NUMBER_OF_RECT=3;

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