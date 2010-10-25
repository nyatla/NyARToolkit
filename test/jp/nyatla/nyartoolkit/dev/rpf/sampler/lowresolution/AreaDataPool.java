package jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObjectPool;



public class AreaDataPool extends NyARManagedObjectPool<AreaDataPool.AreaDataItem>
{
	class AreaDataItem extends NyARManagedObject
	{
		/**
		 * この塊を検出したラスタへの参照ポインタ。
		 */
		public NyARGrayscaleRaster ref_raster;
		/**
		 * ラスタの解像度値
		 */
		public int resolution;
		/**
		 * 塊の輪郭を検出するためのエントリポイント
		 */
		public NyARIntPoint2d entry_pos;
		/**
		 * 検出した塊のクリップ範囲
		 */
		public NyARIntRect    clip  =new NyARIntRect();
		/**
		 * 検出した塊のクリップ中心
		 */
		public NyARIntPoint2d clip_center=new NyARIntPoint2d();
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
		return new AreaDataItem(this._op_interface);
	}
	public AreaDataPool(int i_length) throws NyARException
	{
		super.initInstance(i_length, AreaDataPool.AreaDataItem.class);
	}
}
