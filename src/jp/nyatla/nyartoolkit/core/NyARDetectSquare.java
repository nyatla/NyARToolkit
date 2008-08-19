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

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.labeling.*;


public class NyARDetectSquare
{
    private final NyARLabeling_O2 _labeling;
    private final NyARDetectMarker _detecter;
    private final NyLabelingImage _limage;
    private final NyARParam _param;
    private final NyARMarkerList _marker_list;
    
    /**
     * マーカー抽出インスタンスを作ります。
     * @param i_param
     * @param i_max_marker
     * 認識するマーカーの最大個数を指定します。
     * @throws NyARException
     */
    public NyARDetectSquare(NyARParam i_param,int i_max_marker) throws NyARException
    {
	this._param=i_param;
	//解析オブジェクトを作る
	int width=i_param.getX();
	int height=i_param.getY();
	

	this._detecter=new NyARDetectMarker(width,height);
	this._labeling=new NyARLabeling_O2();
	this._limage=new NyLabelingImage(width,height);
	this._marker_list=new NyARMarkerList(i_max_marker);
	
	this._labeling.attachDestination(this._limage);
    }
    /**
     * ラスタイメージから矩形を検出して、結果o_square_holderへ格納します。
     * @param i_marker
     * @param i_number_of_marker
     * @param i_square_holder
     * @throws NyARException
     */
    public void detectSquare(INyARRaster i_image,int i_thresh,NyARSquareList o_square_list) throws NyARException
    {
	this._labeling.setThresh(i_thresh);
	this._labeling.labeling(i_image);
	//ラベル数が0ならマーカー検出をしない。	
	if(this._limage.getLabelList().getCount()<1){
	    return;
	}
	//ここでマーカー配列を作成する。
	this._detecter.detectMarker(this._limage,1.0,this._marker_list);
	
	//マーカー情報をフィルタして、スクエア配列を更新する。
	o_square_list.pickupSquare(this._param, this._marker_list);
    }
}
