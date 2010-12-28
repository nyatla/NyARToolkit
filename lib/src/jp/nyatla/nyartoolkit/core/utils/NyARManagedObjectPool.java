package jp.nyatla.nyartoolkit.core.utils;

import java.lang.reflect.Array;

import jp.nyatla.nyartoolkit.NyARException;





/**
 * 参照カウンタ付きのobjectPoolです。NyARManagedObjectから派生したオブジェクトを管理します。
 * このクラスは、参照カウンタ付きのオブジェクト型Tのオブジェクトプールを実現します。
 * 
 * このクラスは、NyARManagedObjectと密接に関連して動作することに注意してください。
 * 要素の作成関数はこのクラスで公開しますが、要素の解放関数は要素側に公開します。
 * @param <T>
 */
public class NyARManagedObjectPool<T extends NyARManagedObject>
{
	/**
	 * Javaの都合でバッファを所有させていますが、別にこの形で実装しなくてもかまいません。
	 */
	public class Operator implements NyARManagedObject.INyARManagedObjectPoolOperater
	{
		public NyARManagedObject[] _buffer;
		public NyARManagedObject[] _pool;
		public int _pool_stock;
		public void deleteObject(NyARManagedObject i_object)
		{
			assert(i_object!=null);
			assert(this._pool_stock<this._pool.length);
			this._pool[this._pool_stock]=i_object;
			this._pool_stock++;
		}
	}
	/**
	 * 公開するオペレータオブジェクトです。
	 * このプールに所属する要素以外からは参照しないでください。
	 */
	public Operator _op_interface=new Operator();

	/**
	 * プールから型Tのオブジェクトを割り当てて返します。
	 * @return
	 * 新しいオブジェクト
	 */
	@SuppressWarnings("unchecked")
	public T newObject() throws NyARException
	{
		Operator pool=this._op_interface;
		if(pool._pool_stock<1){
			return null;
		}
		pool._pool_stock--;
		//参照オブジェクトを作成して返す。
		return (T)(pool._pool[pool._pool_stock].initObject());
	}
	/**
	 * 実体化の拒否の為に、コンストラクタを隠蔽します。
	 * 継承クラスを作成して、初期化処理を実装してください。
	 */
	protected NyARManagedObjectPool()
	{
	}
	/**
	 * オブジェクトを初期化します。この関数は、このクラスを継承したクラスを公開するときに、コンストラクタから呼び出します。
	 * @param i_length
	 * @param i_element_type
	 * @throws NyARException
	 */
	@SuppressWarnings("unchecked")
	protected void initInstance(int i_length,Class<T> i_element_type) throws NyARException
	{
		Operator pool=this._op_interface;
		//領域確保
		pool._buffer = (T[])Array.newInstance(i_element_type, i_length);
		pool._pool = (T[])Array.newInstance(i_element_type, i_length);
		//使用中個数をリセット
		pool._pool_stock=i_length;
		//オブジェクトを作成
		for(int i=pool._pool.length-1;i>=0;i--)
		{
			pool._buffer[i]=pool._pool[i]=createElement();
		}
		return;		
	}

	@SuppressWarnings("unchecked")
	protected void initInstance(int i_length,Class<T> i_element_type,Object i_param) throws NyARException
	{
		Operator pool=this._op_interface;
		//領域確保
		pool._buffer = (T[])Array.newInstance(i_element_type, i_length);
		pool._pool = (T[])Array.newInstance(i_element_type, i_length);
		//使用中個数をリセット
		pool._pool_stock=i_length;
		//オブジェクトを作成
		for(int i=pool._pool.length-1;i>=0;i--)
		{
			pool._buffer[i]=pool._pool[i]=createElement(i_param);
		}
		return;		
	}
	/**
	 * オブジェクトを作成します。継承クラス内で、型Tのオブジェクトを作成して下さい。
	 * @return
	 * @throws NyARException
	 */
	protected T createElement() throws NyARException
	{
		throw new NyARException();
	}
	protected T createElement(Object i_param) throws NyARException
	{
		throw new NyARException();
	}
}
