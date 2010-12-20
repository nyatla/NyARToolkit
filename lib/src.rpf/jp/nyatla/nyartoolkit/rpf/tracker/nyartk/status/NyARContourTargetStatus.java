package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.INyARVectorReader;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARTrackerSource;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinates;

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
	 * @param i_vecreader
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */
	public boolean setValue(INyARVectorReader i_vecreader,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		return i_vecreader.traceConture(i_sample.lebeling_th, i_sample.entry_pos, this.vecpos);
	}	
}
