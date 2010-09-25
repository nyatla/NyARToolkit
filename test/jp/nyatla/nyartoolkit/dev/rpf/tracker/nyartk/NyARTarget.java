package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObject.INyARManagedObjectPoolOperater;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.AreaDataPool;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;


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
	public long last_update;
	/**
	 * 寿命値
	 */
	public int age;
	////////////////////////
	//targetの情報
	public NyARTargetStatus ref_status;
	
	/**
	 * ユーザオブジェクトを配置するポインタータグ
	 */
	public Object tag;
//	//Samplerからの基本情報
	public NyARIntRect sample_area=new NyARIntRect();
	public NyARIntPoint2d sample_area_center=new NyARIntPoint2d();
	//アクセス用関数
	
	public NyARTarget(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
	}
	/**
	 * @Override
	 */
	public int releaseObject()
	{
		if(this.ref_status!=null)
		{
			this.ref_status.releaseObject();
		}
		return super.releaseObject();
	}	
	
	/**
	 * LowResolutionLabelingSamplerOut.Itemの値をセットします。セットされる値は、sample_areaとsample_area_centerだけです。
	 * @param i_item
	 * 設定する値です。
	 */
	public void setValue(LowResolutionLabelingSamplerOut.Item i_item)
	{
		this.sample_area.setValue(i_item.base_area);
		this.sample_area_center.setValue(i_item.base_area_center);
	}
	/**
	 * このターゲットのステータスを、IgnoreStatusへ変更します。
	 */
	public void setIgnoreStatus()
	{
		//遷移元のステータスを制限すること！
		assert(
				((this.ref_status instanceof NyARContourTargetStatus) == true) || 
				((this.ref_status instanceof NyARNewTargetStatus)== true)
		);
		this.ref_status.releaseObject();
		this.ref_status=null;
	}
	
	public void setCntoureStatus(NyARContourTargetStatus i_c)
	{
		//遷移元のステータスを制限
		assert((this.ref_status instanceof NyARNewTargetStatus) == true);
		this.ref_status.releaseObject();
		this.ref_status=i_c;
	}
	
}
