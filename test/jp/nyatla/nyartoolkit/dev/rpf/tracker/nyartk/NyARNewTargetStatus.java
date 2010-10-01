package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;

public class NyARNewTargetStatus extends NyARTargetStatus
{
	public LowResolutionLabelingSamplerOut.Item sampleout;
	protected NyARNewTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator) throws NyARException
	{
		super(i_ref_pool_operator);
		this.sampleout=null;
	}
	/**
	 * @Override
	 */
	public int releaseObject()
	{
		int ret=super.releaseObject();
		if(ret==0 && this.sampleout!=null)
		{
			this.sampleout.releaseObject();
			this.sampleout=null;
		}
		return ret;
	}
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_src
	 * セットするLowResolutionLabelingSamplerOut.Itemを指定します。関数は、このアイテムの参照カウンタをインクリメントします。
	 * @throws NyARException
	 */
	public void setValue(LowResolutionLabelingSamplerOut.Item i_src) throws NyARException
	{
		if(this.sampleout!=null){
			this.sampleout.releaseObject();
		}
		this.sampleout=(LowResolutionLabelingSamplerOut.Item)i_src.refObject();
	}
	
}

