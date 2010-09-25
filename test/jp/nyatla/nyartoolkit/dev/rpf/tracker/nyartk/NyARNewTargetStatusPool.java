package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.*;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut.Item;


public class NyARNewTargetStatusPool extends NyARManagedObjectPool<NyARNewTargetStatus>
{
	/**
	 * @Override
	 * 引数なしの関数は使用を禁止します。
	 */
	public NyARNewTargetStatus newObject()
	{
		return null;
	}
	/**
	 * 初期化済みのNyARNewTargetStatusを作成します。
	 * @param i_src
	 * 参照するSampleObject
	 * @return
	 */
	public NyARNewTargetStatus newObject(LowResolutionLabelingSamplerOut.Item i_src)
	{
		NyARNewTargetStatus s=super.newObject();
		if(s==null){
			return null;
		}
		s.sampleout=(Item) i_src.refObject();
		return s;
	}
}
