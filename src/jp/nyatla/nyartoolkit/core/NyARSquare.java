/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core;
/**
 * ARMarkerInfoに相当するクラス。
 * スクエア情報を保持します。
 *
 */
public class NyARSquare{
    private NyARMarker marker;
    public int area;
    public double[] pos;
    public double[][] line;  //double[4][3]
    public double[][] vertex;//double[4][2];
    public NyARSquare(NyARMarker i_marker,double[][] i_attached_line,double[][] i_attached_vertex)
    {
        //ARSquareは、ARMarkerを完全にラップするようにした。
        marker=i_marker;
        area=i_marker.area;
        pos =i_marker.pos;
        line  =i_attached_line;
        vertex=i_attached_vertex;
    }
    public NyARMarker getMarker()
    {
	return marker;
    }
}
