/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.surfacetracking;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;
import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;


/**
 * テンプレートに使用するパッチ画像を格納します。
 *
 */
public class NyARTemplatePatchImage
{
	public final static int AR2_TEMP_SCALE = 2;
	public final static int AR2_TEMPLATE_NULL_PIXEL = 2000000000;
	/**
	 * テンプレートサイズ
	 */
	final public int xsize;
	/**
	 * テンプレートサイズ
	 */
	final public int ysize;
	/**
	 * x方向のテンプレート領域
	 * xsize=xts*2+1
	 */
	final public int xts;
	/**
	 * y方向のテンプレート領域
	 * ysize=yts*2+1
	 */
	final public int yts;
	/**
	 * テンプレートイメージ。この値はARToolkitNFTと異なり、生データであるので注意すること！
	 */
	final public int[] img;
	/**
	 * length of vector *img
	 */
	public int vlen;
	/**
	 * 有効なピクセル値の合計値
	 */
	public int sum_of_img;
	/**
	 * 有効なピクセルの数
	 */
	public int valid_pixels;
	
	/**
	 * 1bitを中心に、(i_tx*2+1)*(i_ty*2+1)のテンプレートを生成する。
	 * @param i_tx
	 * @param i_ty
	 */
	public NyARTemplatePatchImage(int i_tx, int i_ty)
	{
		this.xts = i_tx;
		this.yts = i_ty;


		this.xsize = i_tx*2 + 1;
		this.ysize = i_ty*2 + 1;
		this.img = new int[this.xsize * this.ysize];
	}

	final private NyARDoublePoint2d __in=new NyARDoublePoint2d();
	
	/**
	 * 元ar2GenTemplate関数。
	 * 与えられた座標を中心に、テンプレート画像を生成する。
	 * 座標は観察座標点。
	 * @param i_x
	 * @param i_y
	 * @param i_scale
	 * @param o_template
	 * @return
	 * @throws NyARException
	 */
	public void makeFromReferenceImage(double i_x,double i_y,NyARDoubleMatrix44 i_ref_ctrans,INyARCameraDistortionFactor i_ref_dist_factor,NyARNftIsetFile.ReferenceImage i_source)
	{
		int[] img = this.img;
		int img1_ptr=0;
		int k = 0;
		int sum2=0;
		int sum=0;
	    NyARDoublePoint2d ideal=this.__in;
		for(int  j = -(this.yts); j <= this.yts; j++ ) {
			for(int  i = -(this.xts); i <= this.xts; i++ )
			{
			    i_ref_dist_factor.observ2Ideal(i_x+i*AR2_TEMP_SCALE,i_y+j*AR2_TEMP_SCALE,ideal);
			    double ideal_x=ideal.x;
			    double ideal_y=ideal.y;
			    //ar2ScreenCoord2MarkerCoord(in.x,in.y,i_ref_ctrans,in);の展開
			    double c11 = i_ref_ctrans.m20 * ideal_x - i_ref_ctrans.m00;
			    double c12 = i_ref_ctrans.m21 * ideal_x - i_ref_ctrans.m01;
			    double c21 = i_ref_ctrans.m20 * ideal_y - i_ref_ctrans.m10;
			    double c22 = i_ref_ctrans.m21 * ideal_y - i_ref_ctrans.m11;
			    double b1  = i_ref_ctrans.m03 - i_ref_ctrans.m23 * ideal_x;
			    double b2  = i_ref_ctrans.m13 - i_ref_ctrans.m23 * ideal_y;
			    double m = c11 * c22 - c12 * c21;
				//public int ar2GetImageValue(double sx, double sy) throws NyARExceptionの展開
				{
					int ix= (int)((((c22 * b1 - c12 * b2) / m) * i_source.dpi / 25.4f)+0.5);
					int iy= (int)((i_source.height - (((c11 * b2 - c21 * b1) / m) * i_source.dpi)/ 25.4f)+0.5);

				    //座標計算と値取得は分けよう。
				    if( ix < 0 || ix >= i_source.width || iy < 0 || iy >= i_source.height ){
				    	img[img1_ptr] = AR2_TEMPLATE_NULL_PIXEL;
				    }else{
					    int ret = img[img1_ptr] =i_source.img[iy*i_source.width+ix];
						sum2+=ret*ret;
						sum+=ret;
						k++;
				    }
				    //byte値はint化
				}				    
				img1_ptr++;
			}
		}
		int vlen= sum2-sum*sum/k;
		this.vlen = (int)Math.sqrt((double)(vlen));
		this.sum_of_img=sum;
		this.valid_pixels=k;
		return;
	}	
}