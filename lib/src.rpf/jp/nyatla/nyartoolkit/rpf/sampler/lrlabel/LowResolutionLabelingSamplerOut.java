package jp.nyatla.nyartoolkit.rpf.sampler.lrlabel;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObject;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;

/**
 * このクラスは、{@link LowResolutionLabelingSampler}の結果を受け取るコンテナです。
 * ラベルリストを所有します。
 */
public class LowResolutionLabelingSamplerOut
{
	/**
	 * ラべリング結果を格納するデータクラスです。
	 * <p> メモ - クラス内定義ができない処理系では、LowResolutionLabelingSamplerOutItemで定義してください。</p>
	 *
	 */
	public class Item extends NyARManagedObject
	{
		/** 輪郭のエントリポイントです。*/
		public NyARIntPoint2d entry_pos=new NyARIntPoint2d();
		/** ラべルのクリップ範囲を、元画像の解像度に換算した値です。*/
		public NyARIntRect    base_area  =new NyARIntRect();
		/**　ラベルのクリップ範囲の中心を、元画像の解像度に換算した値です。*/
		public NyARIntPoint2d base_area_center=new NyARIntPoint2d();
		/**　ラベル範囲矩形の、対角距離の2乗値です。*/
		public int base_area_sq_diagonal;
		/** ラベルを検出した時の閾値です。*/
		public int lebeling_th;
		/**
		 * コンストラクタです。
		 * @param i_pool
		 * 親プールのコントロールインタフェイス。
		 */
		public Item(INyARManagedObjectPoolOperater i_pool)
		{
			super(i_pool);
		}
	}	
	/**
	 * AreaのPoolクラス
	 */
	private class AreaPool extends NyARManagedObjectPool<Item>
	{
		public AreaPool(int i_length) throws NyARException
		{
			super.initInstance(i_length,Item.class);
			return;
		}
		protected Item createElement()
		{
			return new Item(this._op_interface);
		}
	}
	/**
	 * AreaのStackクラス
	 */
	private class AreaStack extends NyARPointerStack<Item>
	{
		public AreaStack(int i_length) throws NyARException
		{
			super.initInstance(i_length, Item.class);
		}
	}

	private AreaPool _pool;
	private AreaStack _stack;
	/**
	 * コンストラクタです。
	 * 格納出来るラベルの最大値を指定して、インスタンスを生成します。
	 * @param i_length
	 * 格納するラベルの最大値
	 * @throws NyARException
	 */
	public LowResolutionLabelingSamplerOut(int i_length) throws NyARException
	{
		this._pool=new AreaPool(i_length);
		this._stack=new AreaStack(i_length);
		return;
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * {@link LowResolutionLabelingSampler}が使う関数です。ユーザが使用することはありません。
	 */
	public void initializeParams()
	{
		//基準ラスタの設定
		
		Item[] items=this._stack.getArray();
		//スタック内容の初期化
		for(int i=this._stack.getLength()-1;i>=0;i--){
			items[i].releaseObject();
			items[i]=null;
		}
		//スタックをクリア
		this._stack.clear();
	}
	/**
	 * この関数は、新しいラベル要素を1個割り当てます。
	 * 通常、ユーザが使用することはありません。
	 * @return
	 * 割り当てられたラベルオブジェクト。失敗するとNULL
	 * @throws NyARException
	 */
	public Item prePush() throws NyARException
	{
		Item result=this._pool.newObject();
		if(result==null){
			return null;
		}
		if(this._stack.push(result)==null){
			result.releaseObject();
			return null;
		}
		return result;
		
	}
	/**
	 * この関数は、ラベル配列の参照値を返します。
	 * 有効な個数は、{@link #getLength}で得られます。
	 * @return
	 * ラベル配列の参照値。
	 */
	public Item[] getArray()
	{
		return this._stack.getArray();
	}
	/**
	 * この関数は、有効なラベル配列の要素数を返します。
	 * @return
	 * 有効な要素数。
	 */
	public int getLength()
	{
		return this._stack.getLength();
	}
}