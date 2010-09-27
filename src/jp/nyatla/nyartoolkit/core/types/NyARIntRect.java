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
	public void setAreaRect(NyARDoublePoint2d i_vertex[],int i_num_of_vertex)
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
		this.x=xmin;
		this.w=xmax-xmin+1;
		this.y=ymin;
	}
	public void setAreaRect(NyARIntPoint2d i_vertex[],int i_num_of_vertex)
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
		this.x=xmin;
		this.w=xmax-xmin+1;
		this.y=ymin;
	}
	/**
	 * LTRB矩形表現を、RECTにセットします。
	 * @param i_l
	 * @param i_t
	 * @param i_r
	 * @param i_b
	 */
	public void setLtrb(int i_l,int i_t,int i_r,int i_b)
	{
		assert(i_l<=i_r && i_t<=i_b);
		this.x=i_l;
		this.y=i_t;
		this.w=i_r-i_l+1;
		this.h=i_b-i_t+1;
		
	}
	/**
	 * 矩形を指定した領域内にクリップします。
	 * @param top
	 * @param bottom
	 * @param left
	 * @param right
	 */
	public void clip(int i_left,int i_top,int i_right,int i_bottom)
	{
		int x=this.x;
		int y=this.y;
		int r=x+this.w-1;
		int b=y+this.h-1;
		if(x<i_left){
			x=i_left;
		}else if(x>i_right){
			x=i_right;	
		}
		if(y<i_top){
			y=i_top;
		}else if(y>i_bottom){
			y=i_bottom;			
		}
		int l;
		l=(r>i_right)?i_right-x:r-x;
		if(l<0){
			this.w=0;
		}else{
			this.w=l+1;
		}
		l=(b>i_bottom)?i_bottom-y:b-y;
		if(l<0){
			this.h=0;
		}else{
			this.h=l+1;
		}
		this.x=x;
		this.y=y;
		return;
	}

	/**
	 * 点がRECTの範囲内であるか判定します。
	 * @param i_x
	 * @param i_y
	 * @return
	 */
	public boolean isInnerPoint(int i_x,int i_y)
	{
		int x=i_x-this.x;
		int y=i_y-this.y;
		return (0<=x && x<this.w && 0<=y && y<this.h);
	}
	/**
	 * RECTがこのRECTの範囲内であるか判定します。
	 * @param i_rect
	 * @param i_y
	 * @return
	 */
	public boolean isInnerRect(NyARIntRect i_rect)
	{
		assert(i_rect.w>=0 && i_rect.h>=0);
		int lx=i_rect.x-this.x;
		int ly=i_rect.y-this.y;
		int lw=lx+i_rect.w;
		int lh=ly+i_rect.h;
		return (0<=lx && lx<this.w && 0<=ly && ly<this.h && lw<=this.w && lh<=this.h);
	}
	public boolean isInnerRect(int i_x,int i_y,int i_w,int i_h)
	{
		assert(i_w>=0 && i_h>=0);
		int lx=i_x-this.x;
		int ly=i_y-this.y;
		int lw=lx+i_w;
		int lh=ly+i_h;
		return (0<=lx && lx<this.w && 0<=ly && ly<this.h && lw<=this.w && lh<=this.h);
	}
	/**
	 * i_sourceの値をthisにセットします。
	 * @param i_source
	 */
	public final void setValue(NyARIntRect i_source)
	{
		this.x=i_source.x;
		this.y=i_source.y;
		this.h=i_source.h;
		this.w=i_source.w;
	}

}
