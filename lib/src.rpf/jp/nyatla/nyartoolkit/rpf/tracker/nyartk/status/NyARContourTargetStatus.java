package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.INyARVectorReader;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinates;


/**
 * このクラスは、輪郭ステータスのターゲットステータスを格納します。
 * 輪郭ステータスは、ベクトル化した輪郭点配列をメンバに持ちます。
 */
public final class NyARContourTargetStatus extends NyARTargetStatus
{
	/**
	 * [readonly]輪郭点のベクトル要素を格納する配列です。
	 */
	public VecLinearCoordinates vecpos=new VecLinearCoordinates(100);
	//
	//制御部

	/**
	 * コンストラクタです。
	 * この関数は、所有されるプールオブジェクトが使います。ユーザは使いません。
	 * @param i_ref_pool_operator
	 * プールオブジェクトのコントロールインタフェイス
	 */
	public NyARContourTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		super(i_ref_pool_operator);
	}
	/**
	 * この関数は、サンプル値を元に、ベクトル輪郭を生成して、インスタンスを更新します。
	 * @param i_vecreader
	 * 画素ベクトルの読出しオブジェクト。
	 * @param i_sample
	 * 輪郭点の基点情報に使う、サンプルオブジェクト。
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException
	 */
	public boolean setValue(INyARVectorReader i_vecreader,LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		return i_vecreader.traceConture(i_sample.lebeling_th, i_sample.entry_pos, this.vecpos);
	}	
}
