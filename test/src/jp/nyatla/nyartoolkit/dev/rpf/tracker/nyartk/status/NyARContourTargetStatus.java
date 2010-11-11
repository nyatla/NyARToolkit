package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LrlsGsRaster;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.dev.rpf.utils.VecLinearCoordinates;

/**
 * 輪郭ソース1個を格納するクラスです。
 *
 */
public final class NyARContourTargetStatus extends NyARTargetStatus
{
	/**
	 * ベクトル要素を格納する配列です。
	 */
	public VecLinearCoordinates vecpos=new VecLinearCoordinates(100);

	
	
	//
	//制御部

	/**
	 * @param i_ref_pool_operator
	 * @param i_shared
	 * 共有ワークオブジェクトを指定します。
	 * 
	 */
	public NyARContourTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		super(i_ref_pool_operator);
	}
	/**
	 * データソースから値をセットします。
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */

	public boolean setValue(LrlsGsRaster i_base_raster,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		LrlsGsRaster r=(LrlsGsRaster)i_sample.ref_raster;
		return i_base_raster.getVectorReader().traceConture(r, i_sample.lebeling_th, i_sample.entry_pos, vecpos);
	}	
}
