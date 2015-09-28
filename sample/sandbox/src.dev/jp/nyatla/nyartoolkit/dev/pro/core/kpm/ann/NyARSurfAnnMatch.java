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

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.pro.core.surf.NyARSurfDescriptor;

public abstract class NyARSurfAnnMatch
{
	public static double ANN_THRESH = 0.1;
	public static class ResultItem
	{
		public NyARSurfDescriptor.Item key;
		public NyARSurfFeatureSet.Item feature;
		public double dist;
	}
	
	public static class ResultPtr extends NyARPointerStack<ResultItem>
	{
		/**
		 * コンストラクタです�?? 配�?��?��?大長さを�?定して、インスタンスを生成します�??
		 * 
		 * @param i_length
		 *            配�?��?��?大長�?
		 * @throws NyARRuntimeException
		 */
		public ResultPtr(int i_length) throws NyARRuntimeException
		{
			super.initInstance(i_length, ResultItem.class);
		}
	}

	public static class Result extends ResultPtr
	{
		public Result(int i_length) throws NyARRuntimeException
		{
			super(i_length);
			for(int i=i_length-1;i>=0;i--){
				this._items[i]=new ResultItem();
			}
		}
		public final ResultItem prePush()
		{
			// �?要に応じてアロケー�?
			if (this._length >= this._items.length){
				return null;
			}
			// 使用領域�?+1して、予�?した領域を返す�?
			ResultItem ret = this._items[this._length];
			this._length++;
			return ret;
		}
		
	}
	protected NyARSurfFeatureSet _template;

	public NyARSurfAnnMatch(NyARSurfFeatureSet i_template)
	{
		this._template=i_template;
	}

	public abstract void match(NyARSurfDescriptor i_query, NyARSurfAnnMatch.Result i_result);
};


