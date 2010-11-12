package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARNewTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObject.INyARManagedObjectPoolOperater;

/**
 * トラッキングターゲットのクラスです。
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
	private static long _serial=0;
	/**
	 * 新しいシリアルIDを返します。この値は、NyARTargetを新規に作成したときに、Poolクラスがserialプロパティに設定します。
	 * @return
	 */
	public static long getSerial()
	{
		synchronized(NyARTarget._serial_lock){
			return NyARTarget._serial++;
		}
	}
	////////////////////////
	//targetの基本情報
	/**
	 * ステータスのタイプを表します。この値はref_statusの型と同期しています。
	 */
	public int st_type;
	/**
	 * Targetを識別するID値
	 */
	public long serial;
	/**
	 * 認識サイクルの遅延値。更新ミスの回数と同じ。
	 */
	public int delay_tick;

	/**
	 * 現在のステータスになってからのターゲットの寿命値
	 */
	public int status_age;
	/**
	 * 検知率
	 */
	/**
	 * 
	 */
	////////////////////////
	//targetの情報
	public NyARTargetStatus ref_status;
	
	/**
	 * ユーザオブジェクトを配置するポインタータグです。リリース時にNULL初期化されます。
	 */
	public Object tag;
//	//Samplerからの基本情報
	
	/**
	 * サンプリングエリアを格納する変数です。
	 */
	public NyARIntRect sample_area=new NyARIntRect();
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
		if(ret==0 && this.ref_status!=null)
		{
			this.ref_status.releaseObject();
			this.ref_status=null;
			this.tag=null;
		}
		return ret;
	}
	
	/**
	 * 頂点情報を元に、sampleAreaにRECTを設定します。
	 * @param i_vertex
	 */
	public void setSampleArea(NyARDoublePoint2d[] i_vertex)
	{
		this.sample_area.setAreaRect(i_vertex,4);
	}	

	/**
	 * LowResolutionLabelingSamplerOut.Itemの値をを元に、sample_areaにRECTを設定します。
	 * @param i_item
	 * 設定する値です。
	 */
	public void setSampleArea(LowResolutionLabelingSamplerOut.Item i_item)
	{
		this.sample_area.setValue(i_item.base_area);
	}
}
