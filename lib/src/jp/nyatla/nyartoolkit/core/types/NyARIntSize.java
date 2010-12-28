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

public class NyARIntSize
{
	public int h;
	public int w;
	public NyARIntSize()
	{
		this.w=0;
		this.h=0;
		return;		
	}
	public NyARIntSize(NyARIntSize i_ref_object)
	{
		this.w=i_ref_object.w;
		this.h=i_ref_object.h;
		return;		
	}
	public NyARIntSize(int i_width,int i_height)
	{
		this.w=i_width;
		this.h=i_height;
		return;
	}
	public final void setValue(int i_w,int i_h)
	{
		this.w=i_w;
		this.h=i_h;
		return;
	}
	/**
	 * サイズが同一であるかを確認する。
	 * 
	 * @param i_width
	 * @param i_height
	 * @return
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
	 * サイズが同一であるかを確認する。
	 * 
	 * @param i_width
	 * @param i_height
	 * @return
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
	 * サイズがサイズ値の範囲であるか判定します。
	 * @param i_x
	 * @param i_y
	 * @return
	 */
	public final boolean isInnerSize(int i_x,int i_y)
	{
		return (i_x<=this.w && i_y<=this.h);
	}
	/**
	 * サイズ値が、このサイズの範囲内であるか判定します。
	 * @param i_size
	 * @return
	 */
	public final boolean isInnerSize(NyARIntSize i_size)
	{
		return (i_size.w<=this.w && i_size.h<=this.h);
	}
	/**
	 * 点が、サイズの内部にあるか判定します。
	 * @param i_point
	 * @return
	 */
	public final boolean isInnerSize(NyARDoublePoint2d i_point)
	{
		return (i_point.x<this.w && i_point.y<this.h && 0<=i_point.x && 0<=i_point.y);
	}
	/**
	 * 点が、サイズの内部にあるか判定します。
	 * @param i_x
	 * @param i_y
	 * @return
	 */
	public final boolean isInnerPoint(int i_x,int i_y)
	{
		return (i_x<this.w && i_y<this.h && 0<=i_x && 0<=i_y);
	}
	/**
	 * 点が、サイズの内部にあるか判定します。
	 * @param i_x
	 * @param i_y
	 * @return
	 */
	public final boolean isInnerPoint(NyARDoublePoint2d i_pos)
	{
		return (i_pos.x<this.w && i_pos.y<this.h && 0<=i_pos.x && 0<=i_pos.y);
	}
	/**
	 * 点が、サイズの内部にあるか判定します。
	 * @param i_x
	 * @param i_y
	 * @return
	 */	
	public final boolean isInnerPoint(NyARIntPoint2d i_pos)
	{
		return (i_pos.x<this.w && i_pos.y<this.h && 0<=i_pos.x && 0<=i_pos.y);
	}
	/**
	 * 頂点セットの広がりを計算して格納する。
	 * この関数は、頂点セットを全て包括するサイズを計算します。
	 * @param i_vertex
	 * @param i_num_of_vertex
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
