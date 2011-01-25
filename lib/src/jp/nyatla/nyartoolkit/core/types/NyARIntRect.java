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
 * このクラスは、基点x,yと、幅、高さで矩形を定義します。
 */
public class NyARIntRect
{
	/** 矩形の左上の点(X)*/
	public int x;
	/** 矩形の左上の点(Y)*/
	public int y;
	/** 矩形の幅(X)*/
	public int w;
	/** 矩形の高さ(Y)*/
	public int h;
	/**
	 * この関数は、頂点集合を包括する矩形を計算して、インスタンスにセットします。
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
		this.x=xmin;
		this.w=xmax-xmin+1;
		this.y=ymin;
	}
	/**
	 * この関数は、頂点集合を包括する矩形を計算して、インスタンスにセットします。
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
		this.x=xmin;
		this.w=xmax-xmin+1;
		this.y=ymin;
	}

	/**
	 * この関数は、矩形を領域内にクリップします。
	 * @param i_left
	 * クリップする左辺
	 * @param i_top
	 * クリップする上辺
	 * @param i_right
	 * クリップする右辺
	 * @param i_bottom
	 * クリップする下辺
	 */
	public final void clip(int i_left,int i_top,int i_right,int i_bottom)
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
	 * この関数は、点が矩形の範囲内にあるか判定します。
	 * @param i_x
	 * 調査する座標(X)
	 * @param i_y
	 * 調査する座標(Y)
	 * @return
	 *　点が矩形の中にあれば、trueを返します。
	 */
	public final boolean isInnerPoint(int i_x,int i_y)
	{
		int x=i_x-this.x;
		int y=i_y-this.y;
		
		return (0<=x && x<this.w && 0<=y && y<this.h);
	}
	/**
	 * この関数は、点が矩形の範囲内にあるか判定します。
	 * @param i_pos
	 * 調査する座標
	 * @return
	 *　点が矩形の中にあれば、trueを返します。
	 */
	public final boolean isInnerPoint(NyARDoublePoint2d i_pos)
	{
		int x=(int)i_pos.x-this.x;
		int y=(int)i_pos.y-this.y;
		return (0<=x && x<this.w && 0<=y && y<this.h);
	}
	/**
	 * この関数は、点が矩形の範囲内にあるか判定します。
	 * @param i_pos
	 * 調査する座標
	 * @return
	 *　点が矩形の中にあれば、trueを返します。
	 */
	public final boolean isInnerPoint(NyARIntPoint2d i_pos)
	{
		int x=i_pos.x-this.x;
		int y=i_pos.y-this.y;
		return (0<=x && x<this.w && 0<=y && y<this.h);
	}
	/**
	 * この関数は、引数の矩形が、この矩形内にあるか判定します。
	 * @param i_rect
	 * 内側にあるか調べる矩形
	 * @return
	 *　矩形が内側にあれば、trueを返します。
	 */
	public final boolean isInnerRect(NyARIntRect i_rect)
	{
		assert(i_rect.w>=0 && i_rect.h>=0);
		int lx=i_rect.x-this.x;
		int ly=i_rect.y-this.y;
		int lw=lx+i_rect.w;
		int lh=ly+i_rect.h;
		return (0<=lx && lx<this.w && 0<=ly && ly<this.h && lw<=this.w && lh<=this.h);
	}
	/**
	 * この関数は、引数で定義される矩形が、この矩形内にあるか判定します。
	 * @param i_x
	 * 内側にあるか調べる矩形の左上座標(X)
	 * @param i_y
	 * 内側にあるか調べる矩形の左上座標(Y)
	 * @param i_w
	 * 内側にあるか調べる矩形の幅
	 * @param i_h
	 * 内側にあるか調べる矩形の高さ
	 * @return
	 *　矩形が内側にあれば、trueを返します。
	 */
	public final boolean isInnerRect(int i_x,int i_y,int i_w,int i_h)
	{
		assert(i_w>=0 && i_h>=0);
		int lx=i_x-this.x;
		int ly=i_y-this.y;
		int lw=lx+i_w;
		int lh=ly+i_h;
		return (0<=lx && lx<this.w && 0<=ly && ly<this.h && lw<=this.w && lh<=this.h);
	}
	/**
	 * この関数は、２つの矩形の対角点同士の距離の二乗値を計算します。
	 * @param i_rect2
	 * 比較する矩形
	 * @return
	 * 左上、右下の点同士の距離の二乗値
	 */
	public final int sqDiagonalPointDiff(NyARIntRect i_rect2)
	{
		int w1,w2;
		int ret;
		w1=this.x-i_rect2.x;
		w2=this.y-i_rect2.y;
		ret=w1*w1+w2*w2;
		w1+=this.w-i_rect2.w;
		w2+=this.h-i_rect2.h;
		ret+=w1*w1+w2*w2;
		return ret;
	}
	/**
	 * この関数は、矩形の対角距離の二乗距離を返します。
	 * @return
	 * 矩形の対角距離の二乗値。
	 */
	public final int getDiagonalSqDist()
	{
		int lh=this.h;
		int lw=this.w;
		return lh*lh+lw*lw;
	}

	/**
	 * この関数は、オブジェクトの値をインスタンスにセットします。
	 * @param i_source
	 * セットする値を格納したオブジェクト。
	 */
	public final void setValue(NyARIntRect i_source)
	{
		this.x=i_source.x;
		this.y=i_source.y;
		this.h=i_source.h;
		this.w=i_source.w;
	}

}
