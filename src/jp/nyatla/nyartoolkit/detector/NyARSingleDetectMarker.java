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
package jp.nyatla.nyartoolkit.detector;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.raster.*;
/**
 * 1個のマーカーに対する変換行列を計算するクラスです。
 *
 */
public class NyARSingleDetectMarker{
    private static final int AR_SQUARE_MAX=10;
    private NyARParam param;
    private NyARDetectSquare square;
    private NyARCode code;
    protected NyARTransMat transmat;
    private double marker_width;
    //検出結果の保存用
    private int detected_direction;
    private double detected_confidence;
    private NyARSquare detected_square;
    public NyARSingleDetectMarker(NyARParam i_param,NyARCode i_code,double i_marker_width)
    {
	param=i_param;
	//解析オブジェクトを作る
	square=new NyARDetectSquare(AR_SQUARE_MAX,i_param);
	transmat=new NyARTransMat(param);
	//比較コードを保存
	code=i_code;
	marker_width=i_marker_width;

	
    }
    /**
     * i_imageにマーカー検出処理を実行して、結果を保持します。
     * @param dataPtr
     * @param thresh
     * @return
     * マーカーが検出できたかを真偽値で返します。
     * @throws NyARException
     */
    public boolean detectMarkerLite(NyARRaster i_image,int i_thresh) throws NyARException
    {
	detected_square=null;
	//スクエアコードを探す
	square.detectSquare(i_image, i_thresh);
	int number_of_square=square.getSquareCount();
	//コードは見つかった？
	if(number_of_square<1){
	    return false;
	}

	//コードの一致度を調べる準備
	NyARSquare[] squares=square.getSquareArray();
	//パターンホルダを作る
	NyARColorPatt patt=new NyARColorPatt(code.getWidth(),code.getHeight());
	//評価基準になるパターンをイメージから切り出す
	patt.pickFromRaster(i_image,squares[0].getMarker());
	
	//パターンの評価オブジェクトを作る。
	NyARMatchPatt_Color_WITHOUT_PCA eva=new NyARMatchPatt_Color_WITHOUT_PCA();
	//パターンを評価器にセット
	eva.setPatt(patt);
	//コードと比較する
	eva.evaluate(code);
	int square_index=0;
	int direction=eva.getDirection();
	double confidence=eva.getConfidence();
	for(int i=1;i<number_of_square;i++){
	    //次のパターンを取得
	    patt.pickFromRaster(i_image,squares[i].getMarker());
	    //評価器にセットする。
	    eva.setPatt(patt);
            //コードと比較する
            eva.evaluate(code);
	    double c2=eva.getConfidence();
	    if(confidence>c2){
		continue;
	    }
	    //もっと一致するマーカーがあったぽい
	    square_index=i;
	    direction=eva.getDirection();
	    confidence=c2;
	}
	//マーカー情報を保存
	detected_square=squares[square_index];
	detected_direction=direction;
	detected_confidence=confidence;
	return true;
    }
    /**
     * 変換行列を返します。直前に実行したdetectMarkerLiteが成功していないと使えません。
     * @param i_marker_width
     * マーカーの大きさを指定します。
     * @return
     * double[3][4]の変換行列を返します。
     * @throws NyARException
     */
    public NyARMat getTransmationMatrix() throws NyARException
    {
	//一番一致したマーカーの位置とかその辺を計算
	transmat.transMat(detected_square,detected_direction,marker_width);
	return transmat.getTransformationMatrix();
    }
    public double getConfidence()
    {
	return detected_confidence;
    }
    public int getDirection()
    {
	return detected_direction;
    }
    

	
	
	
	
	
//	public static class arUtil_c{
//		public static final int		arFittingMode	=Config.DEFAULT_FITTING_MODE;
//		private static final int		arImageProcMode	=Config.DEFAULT_IMAGE_PROC_MODE;
//		public static final int		arTemplateMatchingMode  =Config.DEFAULT_TEMPLATE_MATCHING_MODE;
//		public static final int		arMatchingPCAMode       =Config.DEFAULT_MATCHING_PCA_MODE;	
		/*int arInitCparam( ARParam *param )*/


	
}

	

	
	
	
	
	
	

	


	

