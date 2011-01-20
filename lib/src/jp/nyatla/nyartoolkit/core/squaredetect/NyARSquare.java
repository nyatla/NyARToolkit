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
package jp.nyatla.nyartoolkit.core.squaredetect;

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、矩形情報を格納します。
 * ARToolKitのARMarkerInfoに相当しますが、このクラスは理想座標のみを取り扱います。
 */
public class NyARSquare
{
	/** 矩形の辺の直線式です。*/
    public NyARLinear[] line = NyARLinear.createArray(4);
	/** 矩形の頂点です。 line[n]と、line[(n+3)%4]の交点でもあります。*/
    public NyARDoublePoint2d[] sqvertex = NyARDoublePoint2d.createArray(4);
    
    /**
     * この関数は、矩形の中心点を計算します。
     * @param o_out
     * 結果を格納するバッファ。
     */
    public void getCenter2d(NyARDoublePoint2d o_out)
    {
    	o_out.x=(this.sqvertex[0].x+this.sqvertex[1].x+this.sqvertex[2].x+this.sqvertex[3].x)/4;
    	o_out.y=(this.sqvertex[0].y+this.sqvertex[1].y+this.sqvertex[2].y+this.sqvertex[3].y)/4;
    	return;
    }
    /**
     * この関数は、頂点同士の距離から、頂点のシフト量（回転量）を返します。
     * よく似た２つの矩形の頂点同士の、頂点の対応を取るために使用します。
     * @param i_square
     * 比較対象の矩形
     * @return
     * シフト量を数値で返します。
     * シフト量はthis-i_squareです。1の場合、this.sqvertex[0]とi_square.sqvertex[1]が対応点になる(shift量1)であることを示します。
     */
    public int checkVertexShiftValue(NyARSquare i_square)
    {
    	NyARDoublePoint2d[] a=this.sqvertex;
    	NyARDoublePoint2d[] b=i_square.sqvertex;
    	
    	//3-0番目
    	int min_dist=Integer.MAX_VALUE;
    	int min_index=0;
    	int xd,yd;
    	for(int i=3;i>=0;i--){
    		int d=0;
    		for(int i2=3;i2>=0;i2--){
    			xd= (int)(a[i2].x-b[(i2+i)%4].x);
    			yd= (int)(a[i2].y-b[(i2+i)%4].y);
    			d+=xd*xd+yd*yd;
    		}
    		if(min_dist>d){
    			min_dist=d;
    			min_index=i;
    		}
    	}
    	return min_index;
    }
    
    /** 4とnの最大公約数テーブル*/
    private final static int[] _gcd_table4={-1,1,2,1};
    /**
     * この関数は、頂点を左回転して、矩形を回転させます。
     * @param i_shift
     * シフト量。4未満、0以上である事。
     */
    public void rotateVertexL(int i_shift)
    {
    	assert(i_shift<4);
    	NyARDoublePoint2d vertext;
    	NyARLinear linet;
    	if(i_shift==0){
    		return;
    	}
    	int t1,t2;
    	int d, i, j, mk;
	    int ll=4-i_shift;
	    d = _gcd_table4[ll];//NyMath.gcn(4,ll);
	    mk = (4-ll) % 4;
	    for (i = 0; i < d; i++) {
	    	linet=this.line[i];
	    	vertext=this.sqvertex[i];
	        for (j = 1; j < 4/d; j++) {
	            t1=(i + (j-1)*mk) % 4;
	            t2=(i + j*mk) % 4;
	    		this.line[t1]=this.line[t2];
	    		this.sqvertex[t1]=this.sqvertex[t2];
	        }
	        t1=(i + ll) % 4;
    		this.line[t1]=linet;
    		this.sqvertex[t1]=vertext;
	    }
    }   
}