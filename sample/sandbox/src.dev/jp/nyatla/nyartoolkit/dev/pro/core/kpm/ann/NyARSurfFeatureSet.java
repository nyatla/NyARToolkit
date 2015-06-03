/* 
 * PROJECT: NyARToolkit Professional
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2012 Ryo Iizuka
 * wm@nyatla.jp
 * http://nyatla.jp
 * 
 * This work is based on the ARToolKit4.
 * Copyright 2010-2011 ARToolworks, Inc. All rights reserved.
 *
 */
package jp.nyatla.nyartoolkit.dev.pro.core.kpm.ann;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;


public class NyARSurfFeatureSet
{
	public class Item
	{
		public double[] v;
		public NyARDoublePoint2d coord2D = new NyARDoublePoint2d();
		public NyARDoublePoint3d coord3D = new NyARDoublePoint3d();
		public NyARIntPoint2d coord3DI = new NyARIntPoint2d();
		// public int pageNo;
		public int refImageIndex;

		public Item(int i_dim)
		{
			this.v = new double[i_dim];
		}
	}
	public int getDescDim()
	{
		return this._items[0].v.length;
	}

	public Item[] _items;
	public Item[] getItems()
	{
		return this._items;
	}
	public Item getItem(int i_idx)
	{
		return this._items[i_idx];
	}

	public int getLength()
	{
		return this._items.length;
	}

	/**
	 * コンストラクタです�?? 配�?��?��?大長さを�?定して、インスタンスを生成します�??
	 * 
	 * @param i_length
	 * 配�?��?��?大長�?
	 * @throws NyARException
	 */
	public NyARSurfFeatureSet(int i_length, int i_dim) throws NyARException
	{
		this._items = new Item[i_length];
		for (int i = 0; i < i_length; i++) {
			this._items[i] = new Item(i_dim);
		}
	}



		
}
