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
package jp.nyatla.nyartoolkit.core.types;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * このクラスは、整数型の距離値を格納します。
 *
 */
public class NyARIntSize
{
	/** Y軸方向のサイズ値*/
	public int h;
	/** X軸方向のサイズ値*/
	public int w;
	/**
	 * コンストラクタです。
	 * 初期値を格納したインスタンスを生成します。
	 */
	public NyARIntSize()
	{
		this.w=0;
		this.h=0;
		return;		
	}
	/**
	 * コンストラクタです。
	 * @param i_ref_object
	 * 引数値で初期化したインスタンスを生成します。
	 */
	public NyARIntSize(NyARIntSize i_ref_object)
	{
		this.w=i_ref_object.w;
		this.h=i_ref_object.h;
		return;		
	}
	/**
	 * コンストラクタです。
	 * @param i_width
	 * {@link #w}に設定する値
	 * @param i_height
	 * {@link #h}に設定する値
	 */
	public NyARIntSize(int i_width,int i_height)
	{
		this.w=i_width;
		this.h=i_height;
		return;
	}
	/**
	 * この関数は、引数値をインスタンスにセットします。
	 * @param i_w
	 * {@link #w}に設定する値
	 * @param i_h
	 * {@link #h}に設定する値
	 */
	public final void setValue(int i_w,int i_h)
	{
		this.w=i_w;
		this.h=i_h;
		return;
	}
	/**
	 * この関数は、サイズが引数値と同一であるかを確認します。
	 * @param i_width
	 * 比較するサイズ値(W)
	 * @param i_height
	 * 比較するサイズ値(H)
	 * @return
	 * サイズが引数値と同じなら、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isEqualSize(int i_width, int i_height)
	{
		if (i_width == this.w && i_height == this.h) {
			return true;
		}
		return false;
	}

	/**
	 * この関数は、サイズが引数値と同一であるかを確認します。
	 * @param i_size
	 * 比較するサイズ値
	 * @return
	 * サイズが引数値と同じなら、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isEqualSize(NyARIntSize i_size)
	{
		if (i_size.w == this.w && i_size.h == this.h) {
			return true;
		}
		return false;
	}
	/**
	 * この関数は、引数値がインスタンスのサイズよりも小さいかを返します。
	 * @param i_x
	 * 比較するサイズ値(W)
	 * @param i_y
	 * 比較するサイズ値(H)
	 * @return
	 * 引数値がインスタンスのサイズよりも小さければ、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isInnerSize(int i_x,int i_y)
	{
		return (i_x<=this.w && i_y<=this.h);
	}
	/**
	 * この関数は、引数値がインスタンスのサイズよりも小さいかを返します。
	 * @param i_size
	 * 比較するサイズ値
	 * @return
	 * 引数値がインスタンスのサイズよりも小さければ、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isInnerSize(NyARIntSize i_size)
	{
		return (i_size.w<=this.w && i_size.h<=this.h);
	}
	/**
	 * この関数は、座標がサイズの範囲内(0,0基点)よりも小さいかを返します。
	 * @param i_point
	 * 調査する座標点
	 * @return
	 * 引数値が範囲内ならば、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isInnerSize(NyARDoublePoint2d i_point)
	{
		return (i_point.x<this.w && i_point.y<this.h && 0<=i_point.x && 0<=i_point.y);
	}
	/**
	 * この関数は、座標がサイズの範囲内(0,0-w,hの矩形)にあるかを返します。
	 * @param i_x
	 * 調査する座標点
	 * @param i_y
	 * 調査する座標点
	 * @return
	 * 引数値が範囲内ならば、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isInnerPoint(int i_x,int i_y)
	{
		return (i_x<this.w && i_y<this.h && 0<=i_x && 0<=i_y);
	}
	/**
	 * この関数は、座標がサイズの範囲内(0,0-w,hの矩形)にあるかを返します。
	 * @param i_pos
	 * 調査する座標点
	 * @return
	 * 引数値が範囲内ならば、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isInnerPoint(NyARDoublePoint2d i_pos)
	{
		return (i_pos.x<this.w && i_pos.y<this.h && 0<=i_pos.x && 0<=i_pos.y);
	}
	/**
	 * この関数は、座標がサイズの範囲内(0,0-w,hの矩形)にあるかを返します。
	 * @param i_pos
	 * 調査する座標点
	 * @return
	 * 引数値が範囲内ならば、trueを返します。
	 * @throws NyARException
	 */
	public final boolean isInnerPoint(NyARIntPoint2d i_pos)
	{
		return (i_pos.x<this.w && i_pos.y<this.h && 0<=i_pos.x && 0<=i_pos.y);
	}
	/**
	 * この関数は、頂点集合を包括する矩形のサイズ値（幅、高さ）を計算して、インスタンスにセットします。
	 * @param i_vertex
	 * 頂点集合を格納した配列
	 * @param i_num_of_vertex
	 * 計算対象とする要素の数
	 */
	public final void setAreaRect(NyARDoublePoint2d[] i_vertex,int i_num_of_vertex)
	{
		//エリアを求める。
		int xmax,xmin,ymax,ymin;
		xmin=xmax=(int)i_vertex[i_num_of_vertex-1].x;
		ymin=ymax=(int)i_vertex[i_num_of_vertex-1].y;
		for(int i=i_num_of_vertex-2;i>=0;i--){
			if(i_vertex[i].x<xmin){
				xmin=(int)i_vertex[i].x;
			}else if(i_vertex[i].x>xmax){
				xmax=(int)i_vertex[i].x;
			}
			if(i_vertex[i].y<ymin){
				ymin=(int)i_vertex[i].y;
			}else if(i_vertex[i].y>ymax){
				ymax=(int)i_vertex[i].y;
			}
		}
		this.h=ymax-ymin+1;
		this.w=xmax-xmin+1;
	}
	/**
	 * この関数は、頂点集合を包括する矩形のサイズ値（幅、高さ）を計算して、インスタンスにセットします。
	 * @param i_vertex
	 * 頂点集合を格納した配列
	 * @param i_num_of_vertex
	 * 計算対象とする要素の数
	 */	
	public final void setAreaRect(NyARIntPoint2d[] i_vertex,int i_num_of_vertex)
	{
		//エリアを求める。
		int xmax,xmin,ymax,ymin;
		xmin=xmax=(int)i_vertex[i_num_of_vertex-1].x;
		ymin=ymax=(int)i_vertex[i_num_of_vertex-1].y;
		for(int i=i_num_of_vertex-2;i>=0;i--){
			if(i_vertex[i].x<xmin){
				xmin=(int)i_vertex[i].x;
			}else if(i_vertex[i].x>xmax){
				xmax=(int)i_vertex[i].x;
			}
			if(i_vertex[i].y<ymin){
				ymin=(int)i_vertex[i].y;
			}else if(i_vertex[i].y>ymax){
				ymax=(int)i_vertex[i].y;
			}
		}
		this.h=ymax-ymin+1;
		this.w=xmax-xmin+1;
	}

}
