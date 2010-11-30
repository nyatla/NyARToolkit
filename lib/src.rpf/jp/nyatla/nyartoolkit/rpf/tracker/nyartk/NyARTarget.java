package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.NyARTargetStatus;

/**
 * トラッキングターゲットのクラスです。
 * {@link #tag}以外の要素については、ユーザからの直接アクセスを推奨しません。
 *
 */
public class NyARTarget extends NyARManagedObject
{
	/**
	 * シリアルID生成時に使うロックオブジェクト。
	 */
	private static Object _serial_lock=new Object();
	/**
	 * システム動作中に一意なシリアル番号
	 */
	private static long _serial_counter=0;
	/**
	 * 新しいシリアルIDを返します。この値は、NyARTargetを新規に作成したときに、Poolクラスがserialプロパティに設定します。
	 * @return
	 */
	public static long createSerialId()
	{
		synchronized(NyARTarget._serial_lock){
			return NyARTarget._serial_counter++;
		}
	}
	////////////////////////
	//targetの基本情報
	/**
	 * ステータスのタイプを表します。この値はref_statusの型と同期しています。
	 */
	public int _st_type;
	/**
	 * Targetを識別するID値
	 */
	public long _serial;
	/**
	 * 認識サイクルの遅延値。更新ミスの回数と同じ。
	 */
	public int _delay_tick;

	/**
	 * 現在のステータスの最大寿命。
	 */
	public int _status_life;

	////////////////////////
	//targetの情報
	public NyARTargetStatus _ref_status;
	
	/**
	 * ユーザオブジェクトを配置するポインタータグです。リリース時にNULL初期化されます。
	 */
	public Object tag;
//	//Samplerからの基本情報
	
	/**
	 * サンプリングエリアを格納する変数です。
	 */
	public NyARIntRect _sample_area=new NyARIntRect();
	//アクセス用関数
	
	/**
	 * Constructor
	 */
	public NyARTarget(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
		this.tag=null;
	}
	/**
	 * この関数は、ref_statusの内容を安全に削除します。
	 */
	public int releaseObject()
	{
		int ret=super.releaseObject();
		if(ret==0 && this._ref_status!=null)
		{
			this._ref_status.releaseObject();
		}
		return ret;
	}
	
	/**
	 * 頂点情報を元に、sampleAreaにRECTを設定します。
	 * @param i_vertex
	 */
	public void setSampleArea(NyARDoublePoint2d[] i_vertex)
	{
		this._sample_area.setAreaRect(i_vertex,4);
	}	

	/**
	 * LowResolutionLabelingSamplerOut.Itemの値をを元に、sample_areaにRECTを設定します。
	 * @param i_item
	 * 設定する値です。
	 */
	public void setSampleArea(LowResolutionLabelingSamplerOut.Item i_item)
	{
		this._sample_area.setValue(i_item.base_area);
	}
}
