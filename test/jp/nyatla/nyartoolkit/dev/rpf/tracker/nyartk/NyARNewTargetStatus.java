package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;

public class NyARNewTargetStatus extends NyARTargetStatus
{
	public LowResolutionLabelingSamplerOut.Item sampleout;
	protected NyARNewTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator) throws NyARException
	{
		super(i_ref_pool_operator);
	}
	/**
	 * @Override
	 */
	public int releaseObject()
	{
		if(sampleout!=null){
			this.sampleout.releaseObject();
		}
		return super.releaseObject();
	}		
}

