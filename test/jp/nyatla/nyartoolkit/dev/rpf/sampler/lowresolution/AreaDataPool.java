package jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObjectPool;



public class AreaDataPool extends NyARManagedObjectPool<AreaDataPool.AreaDataItem>
{
	class AreaDataItem extends NyARManagedObject
	{
		public NyARGrayscaleRaster ref_raster;
		public int resolution;
		public NyARIntPoint2d entry_pos;		
		public NyARIntRect    area  =new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		/**
		 * エリア矩形の対角距離の2乗値
		 */
		public int area_sq_diagonal;
		public AreaDataItem(INyARManagedObjectPoolOperater i_pool)
		{
			super(i_pool);
		}
	}
	protected AreaDataPool.AreaDataItem createElement() throws NyARException
	{
		return new AreaDataItem(this._inner_pool);
	}
	public AreaDataPool(int i_length) throws NyARException
	{
		super.initInstance(i_length, AreaDataPool.AreaDataItem.class);
	}
}
