package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils;

/**
 * NyARManagedObjectPoolの要素クラスです。
 *
 */
public class NyARManagedObject
{
	/**
	 * このインタフェイスは、NyARManagedObjectがPoolを操作するために使います。
	 */	
	public interface INyARManagedObjectPoolOperater
	{
		public void deleteObject(NyARManagedObject i_object);	
	}
	/**
	 * オブジェクトの参照カウンタ
	 */
	private int _count;
	/**
	 * オブジェクトの解放関数へのポインタ
	 */
	private INyARManagedObjectPoolOperater _pool_operater;
	/**
	 * NyARManagedObjectPoolのcreateElement関数が呼び出すコンストラクタです。
	 * @param i_ref_pool_operator
	 * Pool操作の為のインタフェイス
	 */
	protected NyARManagedObject(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		this._count=0;
		this._pool_operater=i_ref_pool_operator;
	}
	/**
	 * このオブジェクトに対する、新しい参照オブジェクトを返します。
	 * @return
	 */
	public NyARManagedObject refObject()
	{
		this._count++;
		return this;
	}
	/**
	 * 参照オブジェクトを開放します。
	 * @return
	 */
	public int releaseObject()
	{
		this._count--;
		if(this._count==0){
			this._pool_operater.deleteObject(this);
		}
		return this._count;
	}	
}