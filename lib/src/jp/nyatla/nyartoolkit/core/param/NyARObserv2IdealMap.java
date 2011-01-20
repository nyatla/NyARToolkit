/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.param;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、観察座標系を理想座標系へ変換するテーブルです。
 * 座標系を変換するテーブルと、変換機能を提供します。
 */
public class NyARObserv2IdealMap
{
	/** テーブル１行当たりのデータ数*/
	protected int _stride;
	/** X座標の変換テーブル*/
	protected double[] _mapx;
	/** Y座標の変換テーブル*/
	protected double[] _mapy;
	/**
	 * コンストラクタです。
	 * 入力した{@link NyARCameraDistortionFactor}とそのサイズから、テーブルを作成します。
	 * ２つのパラメータは整合性が取れていなければなりません。
	 * (通常は、{@link NyARParam}の{@link NyARParam#getDistortionFactor()},{@link NyARParam#getScreenSize()}から得られた
	 * パラメータを入力します。)
	 * @param i_distfactor
	 * 樽型歪みパラメータのオブジェクト。
	 * @param i_screen_size
	 * スクリーンサイズ
	 */
	public NyARObserv2IdealMap(NyARCameraDistortionFactor i_distfactor,NyARIntSize i_screen_size)
	{
		NyARDoublePoint2d opoint=new NyARDoublePoint2d();
		this._mapx=new double[i_screen_size.w*i_screen_size.h];
		this._mapy=new double[i_screen_size.w*i_screen_size.h];
		this._stride=i_screen_size.w;
		int ptr=i_screen_size.h*i_screen_size.w-1;
		//歪みマップを構築
		for(int i=i_screen_size.h-1;i>=0;i--)
		{
			for(int i2=i_screen_size.w-1;i2>=0;i2--)
			{
				i_distfactor.observ2Ideal(i2,i, opoint);
				this._mapx[ptr]=opoint.x;
				this._mapy[ptr]=opoint.y;
				ptr--;
			}
		}
		return;
	}
	/**
	 * この関数は、観察座標を理想座標へ変換します。
	 * 入力できる値範囲は、コンストラクタに設定したスクリーンサイズの範囲内です。
	 * @param ix
	 * 観察座標の値
	 * @param iy
	 * 観察座標の値
	 * @param o_point
	 * 理想座標を受け取るオブジェクト。
	 */
	public void observ2Ideal(int ix, int iy, NyARIntPoint2d o_point)
	{
		int idx=ix+iy*this._stride;
		o_point.x=(int)this._mapx[idx];
		o_point.y=(int)this._mapy[idx];
		return;
	}
	/**
	 * この関数は、観察座標を理想座標へ変換します。
	 * 入力できる値範囲は、コンストラクタに設定したスクリーンサイズの範囲内です。
	 * @param ix
	 * 観察座標の値
	 * @param iy
	 * 観察座標の値
	 * @param o_point
	 * 理想座標を受け取るオブジェクト。
	 */	
	public void observ2Ideal(int ix, int iy, NyARDoublePoint2d o_point)
	{
		int idx=ix+iy*this._stride;
		o_point.x=this._mapx[idx];
		o_point.y=this._mapy[idx];
		return;
	}
	/**
	 * この関数は、一括して観察座標を理想座標へ変換します。
	 * 入力できる値範囲は、コンストラクタに設定したスクリーンサイズの範囲内です。
	 * @param i_coord
	 * 観察座標の配列
	 * @param i_start
	 * 変換対象にする配列の開始インデクス。
	 * @param i_num
	 * 変換対象にする要素の数。
	 * i_startから、i_start+i_num-1までの要素を変換します。
	 * @param o_x_coord
	 * 変換したX座標を受け取る配列。
	 * @param o_y_coord
	 * 変換したY座標を受け取る配列。
	 * @param i_out_start_index
	 * 出力先配列の開始インデクス。指定した位置から先に結果を返します。
	 */
	public void observ2IdealBatch(NyARIntPoint2d[] i_coord,int i_start, int i_num, double[] o_x_coord,double[] o_y_coord,int i_out_start_index)
	{
		int idx;
		int ptr=i_out_start_index;
		final double[] mapx=this._mapx;
		final double[] mapy=this._mapy;
		final int stride=this._stride;
		for (int j = 0; j < i_num; j++){
			idx=i_coord[i_start + j].x+i_coord[i_start + j].y*stride;
			o_x_coord[ptr]=mapx[idx];
			o_y_coord[ptr]=mapy[idx];
			ptr++;
		}
		return;
	}	
}
