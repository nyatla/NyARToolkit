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
 * typedef struct {
 * 	int     area;
 * 	double  pos[2];
 * 	int     coord_num;
 * 	int     x_coord[AR_CHAIN_MAX];
 * 	int     y_coord[AR_CHAIN_MAX];
 * 	int     vertex[5];
 * } ARMarkerInfo2;
 * 
 */
class NyARMarker
{
    private static final int AR_CHAIN_MAX=10000;
    int		area;
    double[]	pos=new double[2];
    int		coord_num;
    int[]	x_coord=new int[AR_CHAIN_MAX];
    int[]	y_coord=new int[AR_CHAIN_MAX];
    int[]	vertex=new int[5];
}


