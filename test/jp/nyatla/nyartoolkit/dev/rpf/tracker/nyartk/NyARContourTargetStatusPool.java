package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.*;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObject.INyARManagedObjectPoolOperater;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;


/*
 * 輪郭情報を保管します。
 * このクラスの要素は、他の要素から参照する可能性があります。
 */
public class NyARContourTargetStatusPool extends NyARManagedObjectPool<NyARContourTargetStatus>
{	
	public class WorkObject
	{
		public int VEC_MAX_COORD;
		public NyARIntPoint2d[] coord_buf;
		public final NyARContourPickup cpickup=new NyARContourPickup();
		public WorkObject(int i_cood_max)
		{
			this.VEC_MAX_COORD=i_cood_max;
			this.coord_buf=NyARIntPoint2d.createArray(i_cood_max);
		}
	}
	private WorkObject _work_object;
	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @param i_cood_max
	 * 輪郭ベクトルの最大数
	 * @throws NyARException
	 */
	public NyARContourTargetStatusPool(int i_size,int i_cood_max) throws NyARException
	{
		this._work_object=new WorkObject(i_cood_max);
		super.initInstance(i_size,NyARContourTargetStatus.class,this._work_object);
	}
	/**
	 * @Override
	 */
	protected NyARContourTargetStatus createElement(Object i_param)
	{
		return new NyARContourTargetStatus(this._inner_pool,(WorkObject)i_param);
	}
}