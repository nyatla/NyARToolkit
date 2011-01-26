package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.*;

/**
 * このクラスは、トラッキングデータ1個のデータを格納します。
 * 単体での使用は想定していません。
 * トラッキングデータは２層構造です。１層目は２次元画像上のラベルデータを元にした情報群をメンバ変数に格納します。
 * ２層目は、{@link NyARTargetStatus}型のメンバ変数{@link #_ref_status}に格納します。
 * {@link #_ref_status}の内容は、認識の進行具合（ステータス）に合せて、{@link NyARTracker}が再割り当てします。
 * 現在割り当てられているステータスオブジェクトの型を知るには、{@link #_st_type}をチェックします。
 * <p>
 * {@link #tag}以外の要素については、ユーザからの直接アクセスを推奨しません。
 * </p>
 */
public class NyARTarget extends NyARManagedObject
{
	/**　シリアルID生成時に使うロックオブジェクト。*/
	private static Object _serial_lock=new Object();
	/** シリアルIDカウンタ*/
	private static long _serial_counter=0;
	/**
	 * この関数は、クラスのドメインで一意なシリアルID（トラックターゲットID）を返します。
	 * この関数は、{@link NyARTargetPool}が呼び出します。通常ユーザは使いません。
	 * @return
	 * トラックターゲットID
	 */
	public static long createSerialId()
	{
		synchronized(NyARTarget._serial_lock){
			return NyARTarget._serial_counter++;
		}
	}
	////////////////////////
	//targetの基本情報
	/** {@link #_ref_status}に格納しているオブジェクトのタイプを表します。ユーザに対しては、[read only]です。*/
	public int _st_type;
	/**　トラックターゲットIDです。ユーザに対しては、[read only]です。*/
	public long _serial;
	/**　認識の遅延サイクル数です。認識ミスが発生するとインクリメントされ、成功すると0にリセットされます。ユーザに対しては、[read only]です。*/
	public int _delay_tick;
	/** 現在のステータスの最大寿命値です。この値は、{@link NyARTracker}がトラックターゲットの寿命管理に使います。ユーザに対しては、[read only]です。*/
	public int _status_life;

	////////////////////////
	//targetの情報
	/**
	 * ステータスオブジェクトです。ステータスオブジェクトは、トラックターゲットのステータス{@link #_ref_status}により、その内容が変わります。
	 * <ul>
	 * <li>{@link NyARTargetStatus#ST_NEW} - オブジェクトは、{@link NyARNewTargetStatus}にキャストできます。
	 * <li>{@link NyARTargetStatus#ST_IGNORE} - オブジェクトは、{@link NyARTargetStatus}にキャストできます。
	 * <li>{@link NyARTargetStatus#ST_CONTURE} - オブジェクトは、{@link NyARContourTargetStatus}にキャストできます。
	 * <li>{@link NyARTargetStatus#ST_RECT} - オブジェクトは、{@link NyARRectTargetStatus}にキャストできます。
	 * </ul>
	 */
	public NyARTargetStatus _ref_status;
	
	/** ユーザオブジェクトを配置するポインタータグです。*/
	public Object tag;
//	//Samplerからの基本情報
	
	/** トラックターゲットのクリップ領域を格納する変数です。クリップ領域は、画像上の矩形で表現します。*/
	public NyARIntRect _sample_area=new NyARIntRect();

	
	/**
	 * コンストラクタです。
	 * 親poolの操作インタフェイスを指定してインスタンスを生成します。
	 * @param iRefPoolOperator
	 * 親poolの操作インタフェイス
	 */
	public NyARTarget(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
		this.tag=null;
	}

	/**
	 * この関数は、オブジェクトの参照カウンタを1減算します。
	 * 参照カウンタが0になった時のみ、所有する参照オブジェクトの参照カウンタも操作します。
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
	 * この関数は、４頂点から包括矩形を計算して{@link #_sample_area}に設定します。
	 * @param i_vertex
	 * 4頂点を格納した配列。
	 */
	public void setSampleArea(NyARDoublePoint2d[] i_vertex)
	{
		this._sample_area.setAreaRect(i_vertex,4);
	}	

	/**
	 * この関数は、LowResolutionLabelingSamplerOut.Itemから{@link #_sample_area}に設定します。
	 * @param i_item
	 * 設定する値です。
	 */
	public void setSampleArea(LowResolutionLabelingSamplerOut.Item i_item)
	{
		this._sample_area.setValue(i_item.base_area);
	}
}
