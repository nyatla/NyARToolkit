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
 * ARMarkerInfoに相当するクラス。 矩形情報を保持します。
 * 
 * directionは方角を表します。
 * 決定しないときはDIRECTION_UNKNOWNを設定してください。
 * 
 */
public class NyARSquare
{
    public NyARLinear[] line = NyARLinear.createArray(4);
    public NyARDoublePoint2d[] sqvertex = NyARDoublePoint2d.createArray(4);
    /**
     * 中心点を計算します。
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
     * 頂点同士の距離から、頂点の位相差を返します。この関数は、よく似た２つの矩形の頂点同士の対応を取るために使用します。
     * @param i_square
     * @return
     * 位相差を数値で返します。
     * 位相差はthis-argです。1の場合、this.sqvertex[0]とthis.sqvertex[1]が対応点になることを示します。
     */
    public int getVertexPhaseShift(NyARSquare i_square)
    {
    	NyARDoublePoint2d[] a=this.sqvertex;
    	NyARDoublePoint2d[] b=i_square.sqvertex;
    	
    	int shift;
    	//割り算したくないでござる。→展開

    	//3番目

    	//2-0番目
    	int max_dist=Integer.MAX_VALUE;
    	for(int i=3;i>=0;i--){
    		int idx=
    		for(int i2=3;i2>=0;i2--){
    			int xd= (int)(a[i].x-b[i].x);
    			int yd= (int)(a[i].y-b[i].y);
    			int d=xd*xd+yd*yd;
    		}
    	}
    }
    /**
     * 頂点をシフトして、矩形を回転させます。
     * @param i_shift
     */
    public void rotateVertex(int i_shift)
    {
    	
    }

}