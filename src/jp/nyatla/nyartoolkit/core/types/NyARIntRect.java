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

/**
 * 基点x,yと、幅、高さで矩形を定義します。
 *
 */
public class NyARIntRect
{
	public int x;

	public int y;

	public int w;

	public int h;
	/**
	 * 頂点を包括するRECTを計算する。
	 * @param i_vertex
	 * @param i_num_of_vertex
	 * @param o_rect
	 */
	public void wrapVertex(NyARDoublePoint2d i_vertex[],int i_num_of_vertex)
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
		this.h=ymax-ymin;
		this.x=xmin;
		this.w=xmax-xmin;
		this.y=ymin;
	}
	/**
	 * 矩形を指定した領域内にクリップする。
	 * @param top
	 * @param bottom
	 * @param left
	 * @param right
	 */
	public void clip(int i_left,int i_top,int i_right,int i_bottom)
	{
		int r=this.x+this.w;
		int b=this.y+this.h;
		if(this.x<i_left){
			this.x=i_left;
		}
		if(this.y<i_top){
			this.y=i_top;
		}
		this.w=(r>i_right)?i_right-this.x:r-this.x;
		this.h=(b>i_bottom)?i_bottom-this.y:b-this.y;
		return;
	}
	
}
