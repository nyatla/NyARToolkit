package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
/**
 * このクラスは、newステータスのターゲットステータスを格納します。
 * newステータスは、ラべリング結果の参照値をメンバに持ちます。
 */
public final class NyARNewTargetStatus extends NyARTargetStatus
{
	/** [readonly]ラべリング結果の参照ポインタです。*/
	public LowResolutionLabelingSamplerOut.Item current_sampleout;
	/**
	 * コンストラクタです。
	 * この関数は、所有されるプールオブジェクトが使います。ユーザは使いません。
	 * @param i_ref_pool_operator
	 * プールオブジェクトのコントロールインタフェイス
	 */	
	public NyARNewTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator) throws NyARException
	{
		super(i_ref_pool_operator);
		this.current_sampleout=null;
	}
	/**
	 * この関数は、オブジェクトの参照カウンタを1減算します。 
	 * 参照しているオブジェクトの参照カウンタ操作も、同時に行います。
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
	 * この関数は、ラべリング結果から、メンバ変数に値をセットします。
	 * @param i_src
	 * セットするラべリング結果を指定します。
	 * 関数は、このオブジェクトの参照カウンタをインクリメントします。
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

