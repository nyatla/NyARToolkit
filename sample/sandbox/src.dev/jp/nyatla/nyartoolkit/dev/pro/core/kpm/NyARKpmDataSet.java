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
package jp.nyatla.nyartoolkit.dev.pro.core.kpm;

import java.io.InputStream;
import java.nio.ByteOrder;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.j2se.ByteBufferedInputStream;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfFeatureSet;

/**
 * based on kpm reddataset.cpp
 */
public class NyARKpmDataSet
{
	/**
	 * ペ�?�ジ毎�?�{@link NyARSurfFeatureSet}
	 */
	public NyARSurfFeatureSet[] _featureset;
	public int surfThresh;

	/**
	 * 
	 * @param i_surfset
	 * ラ�?プするデータオブジェクト�?�配�?��??
	 * �?有権はインスタンスに移ります�??
	 * @throws NyARRuntimeException
	 */
	protected NyARKpmDataSet(NyARSurfFeatureSet[] i_surfset)throws NyARRuntimeException
	{
		this._featureset = i_surfset;
	}

	public static NyARKpmDataSet loadFromFset2(InputStream i_stream)throws NyARRuntimeException
	{
		Fset2Reader fset2file = new Fset2Reader(i_stream);
		Fset2Reader.FileImage fimg=fset2file.getFileImage();
		//3D flags reqired!
		if(fimg.coord3d_flag==0){
			throw new NyARRuntimeException();
		}
		NyARSurfFeatureSet[] fset = new NyARSurfFeatureSet[fimg.page_no_set.length];
		// ペ�?�ジ�?離のために、特徴数を�?��?�ジ番号ごとに�?�?
		int[] fnum = new int[fset.length];
		for (int i = fnum.length - 1; i >= 0; i--) {
			fnum[i] = 0;
		}
		for (int i = fimg.feature_set.length - 1; i >= 0; i--) {
			int pn = fimg.feature_set[i].pageNo;
			for (int i2 = fimg.page_no_set.length - 1; i2 >= 0; i2--) {
				if (fimg.page_no_set[i2] != pn) {
					continue;
				}
				fnum[i2]++;
				break;
			}
		}
		// RefDatasetの作�??
		for (int i = fset.length - 1; i >= 0; i--) {
			fset[i] = new NyARSurfFeatureSet(fnum[i],Fset2Reader.FSET2_SURF_DIMENTION);
		}
		// それぞれのRefDatasetを�?�期�?
		for (int i = fimg.feature_set.length - 1; i >= 0; i--) {
			int pn = fimg.feature_set[i].pageNo;
			for (int i2 = fimg.page_no_set.length - 1; i2 >= 0; i2--) {
				if (fimg.page_no_set[i2] != pn) {
					continue;
				}
				NyARSurfFeatureSet.Item item = fset[i2].getItem(fnum[i2] - 1);
				item.coord2D.setValue(fimg.feature_set[i].coord2D);
				item.coord3D.x=fimg.feature_set[i].coord3D.x;
				item.coord3D.y=fimg.feature_set[i].coord3D.y;
				item.coord3D.z=0;
				item.coord3DI.x=(int)(item.coord3D.x+0.5);
				item.coord3DI.y=(int)(item.coord3D.y+0.5);
				item.refImageIndex = -1;
				System.arraycopy(fimg.feature_set[i].v, 0, item.v, 0,item.v.length);
				fnum[i2]--;
				break;
			}
		}
		// fnumが�?�て0ならOK
		NyARKpmDataSet ret=new NyARKpmDataSet(fset);
		ret.surfThresh=fimg.surf_thresh;
		return ret;
	}
}



/**
 * Fset2形式�?�ファイルイメージを�?�納するクラスです�??
 * Fset2の�?容をpublic変数へ格納します�??
 * ----
 * File structure of FSEAT2
 * ----
 * byte Header[SIZE_OF_FSET2_HEADER] as
 * {
 * 		int number_of_fset_item;
 * 		int surf_thresh;
 * 		int coord3d_flag;
 * };
 * byte FsetItem[SIZE_OF_FSET2_REFSET_BLOCK] as
 * {
 * 		...;
 * }[number_of_fset_item];
 * int page_no;
 * int page_no_set[page_no];
 * ----
 */
class Fset2Reader extends ByteBufferedInputStream
{
	public static final int FSET2_SURF_DIMENTION = 64;

	/**
	 * FSET2_HEADER int num; int surf_thresh; int coord3d_flag;
	 */
	private final static int SIZE_OF_FSET2_HEADER = 4 * 3;
	/**
	 * float coord2d_x,coord2d_y,coord3d_x,coord3d_y; +float[64] +int [2]
	 */
	private final static int SIZE_OF_FSET2_REFSET_BLOCK = (4 * 4)
			+ (FSET2_SURF_DIMENTION * 4) + (4 * 2);

	public static class Fset2Item {
		public double[] v;
		public NyARDoublePoint2d coord2D = new NyARDoublePoint2d();
		public NyARDoublePoint2d coord3D = new NyARDoublePoint2d();
		public int refImageIndex;
		public int pageNo;

		private Fset2Item(int i_v_dim) {
			this.v = new double[i_v_dim];
		}

		public static Fset2Item[] createArray(int number_of_fset_item,
				int fset2SurfDimention) {
			Fset2Item[] ret = new Fset2Item[number_of_fset_item];
			for (int i = ret.length - 1; i >= 0; i--) {
				ret[i] = new Fset2Item(FSET2_SURF_DIMENTION);
			}
			return ret;
		}
	};
	public class FileImage
	{
		public Fset2Item[] feature_set;
		public int[] page_no_set;
		public int surf_thresh;
		public int coord3d_flag;
	};


	public Fset2Reader(InputStream i_stream) throws NyARRuntimeException
	{
		super(i_stream,512);
		this.order(ENDIAN_LITTLE);
	}
	/**
	 * fset2ファイルの�?容を読み出します�??
	 * @return
	 */
	public FileImage getFileImage() throws NyARRuntimeException
	{
		FileImage ret=new FileImage();
		this.readToBuffer(SIZE_OF_FSET2_HEADER);
		int number_of_fset_item = this.getInt();
		ret.surf_thresh = this.getInt();
		ret.coord3d_flag = this.getInt();
		ret.feature_set = Fset2Item.createArray(number_of_fset_item,FSET2_SURF_DIMENTION);
		for (int i = 0; i < number_of_fset_item; i++) {
			this.readToBuffer(SIZE_OF_FSET2_REFSET_BLOCK);
			Fset2Item item = ret.feature_set[i];
			item.coord2D.x = this.getFloat();
			item.coord2D.y = this.getFloat();
			item.coord3D.x = this.getFloat();
			item.coord3D.y = this.getFloat();
			for (int i2 = 0; i2 < FSET2_SURF_DIMENTION; i2++) {
				item.v[i2] = this.getFloat();
			}
			item.pageNo = this.getInt();
			item.refImageIndex = this.getInt();
		}
		this.readToBuffer(4);
		// 再取�?
		int num_of_pset = this.getInt();
		ret.page_no_set = new int[num_of_pset];
		int ptr = 0;
		for (; num_of_pset >= 16; num_of_pset -= 16) {
			this.readToBuffer(16 * 4);
			for (int i = 0; i < 16; i++) {
				ret.page_no_set[ptr++] = this.getInt();
			}
		}
		this.readToBuffer(num_of_pset * 4);
		for (int i = 0; i < num_of_pset; i++) {
			ret.page_no_set[ptr++] = this.getInt();
		}
		return ret;
	}
}


