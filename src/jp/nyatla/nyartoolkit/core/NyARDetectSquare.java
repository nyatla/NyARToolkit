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

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;



public class NyARDetectSquare
{
    private final NyARLabeling labeling;
    private final NyARDetectMarker detect;
    private NyARParam param;

    /**
     * マーカー抽出インスタンスを作ります。
     * @param i_param
     */
    public NyARDetectSquare(NyARParam i_param)
    {
	param=i_param;
	//解析オブジェクトを作る
	int width=i_param.getX();
	int height=i_param.getY();

	labeling=new NyARLabeling_O2(width,height);
	detect=new NyARDetectMarker(width,height);
    }
    /**
     * ラスタイメージから矩形を検出して、結果o_square_holderへ格納します。
     * @param i_marker
     * @param i_number_of_marker
     * @param i_square_holder
     * @throws NyARException
     */
    public void detectSquare(NyARRaster i_image,int i_thresh,NyARSquareList o_square_holder) throws NyARException
    {
//	number_of_square=0;
	
	labeling.labeling(i_image, i_thresh);
	if(labeling.getLabelNum()<1){
	    return;
	}
	//ここでマーカー配列を作成する。
	detect.detectMarker(labeling,1.0,o_square_holder);
	
	//マーカー情報をフィルタして、スクエア配列を更新する。
	o_square_holder.updateSquareArray(param);

//	NyARSquare square;
//	int j=0;
//	for (int i = 0; i <number_of_marker; i++){
//	double[][]  line	=new double[4][3];
//	double[][]  vertex	=new double[4][2];
//	//NyARMarker marker=detect.getMarker(i);
//	square=square_holder.getSquare(i);
//	//・・・線の検出？？
//	if (!square.getLine(param))
//	{
//	    continue;
//	}
//	ここで計算するのは良くないと思うんだ	
//	marker_infoL[j].id  = id.get();
//	marker_infoL[j].dir = dir.get();
//	marker_infoL[j].cf  = cf.get();	
//	j++;
//	//配列数こえたらドゴォォォンしないようにループを抜ける
//	if(j>=marker_info.length){
//	    break;
//	}
//    }
//    number_of_square=j;
    }
}
