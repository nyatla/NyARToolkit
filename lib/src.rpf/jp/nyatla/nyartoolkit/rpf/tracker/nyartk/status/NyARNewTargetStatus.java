package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;

public final class NyARNewTargetStatus extends NyARTargetStatus
{

	public LowResolutionLabelingSamplerOut.Item current_sampleout;
	public NyARNewTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator) throws NyARException
	{
		super(i_ref_pool_operator);
		this.current_sampleout=null;
	}
	/**
	 * @Override
	 */
	public int releaseObject()
	{
		int ret=super.releaseObject();
		if(ret==0 && this.current_sampleout!=null)
		{
			this.current_sampleout.releaseObject();
			this.current_sampleout=null;
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
		if(this.current_sampleout!=null){
			this.current_sampleout.releaseObject();
		}
		if(i_src!=null){
			this.current_sampleout=(LowResolutionLabelingSamplerOut.Item)i_src.refObject();
		}else{
			this.current_sampleout=null;
		}
	}
	
}

