package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LrlAreaDataPool;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARNewTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARManagedObject.INyARManagedObjectPoolOperater;


public class NyARTarget extends NyARManagedObject
{

	private static Object _serial_lock=new Object();
	private static long _serial=0;
	/**
	 * システムの稼働範囲内で一意なIDを持つこと。
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
	 * Targetを識別するID値
	 */
	public long serial;
	/**
	 * このターゲットが最後にアップデートされたtick
	 */
	public long last_update_tick;
	/**
	 * 現在のステータスになってからのターゲットの寿命値
	 */
	public int status_age;
	////////////////////////
	//targetの情報
	public NyARTargetStatus ref_status;
	
	/**
	 * ユーザオブジェクトを配置するポインタータグです。リリース時にNULL初期化されます。
	 */
	public Object tag;
//	//Samplerからの基本情報
	public NyARIntRect sample_area=new NyARIntRect();
	//アクセス用関数
	
	public NyARTarget(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
		this.tag=null;
	}
	/**
	 * @Override
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
	public void setSampleArea(NyARDoublePoint2d[] i_vertex)
	{
		this.sample_area.setAreaRect(i_vertex,4);
	}	
	/**
	 * LowResolutionLabelingSamplerOut.Itemの値をセットします。セットされる値は、sample_areaとsample_area_centerだけです。
	 * @param i_item
	 * 設定する値です。
	 */
	public void setSampleArea(LowResolutionLabelingSamplerOut.Item i_item)
	{
		this.sample_area.setValue(i_item.base_area);
	}
	/**
	 * このターゲットのステータスを、IgnoreStatusへ変更します。
	 */
	public void setIgnoreStatus()
	{
		//遷移元のステータスを制限すること！
		assert(
				((this.ref_status instanceof NyARRectTargetStatus) == true) || 
				((this.ref_status instanceof NyARContourTargetStatus) == true) || 
				((this.ref_status instanceof NyARNewTargetStatus)== true)
		);
		this.ref_status.releaseObject();
		this.status_age=0;
		this.ref_status=null;
	}
	/**
	 * このターゲットのステータスを、CntoureStatusへ遷移させます。
	 * @param i_c
	 */
	public void setCntoureStatus(NyARContourTargetStatus i_c)
	{
		//遷移元のステータスを制限
		assert((this.ref_status instanceof NyARNewTargetStatus) == true);
		this.ref_status.releaseObject();
		this.status_age=0;
		this.ref_status=i_c;
	}
	public void setRectStatus(NyARRectTargetStatus i_c)
	{
		assert((this.ref_status instanceof NyARContourTargetStatus) == true);
		this.ref_status.releaseObject();
		this.status_age=0;
		this.ref_status=i_c;		
	}
	
}
