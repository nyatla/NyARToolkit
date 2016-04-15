package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.imageset.NyARSurfaceImageSet;

/**
 * �?ンプレートに使用するパッチ画像を格納します�??
 *
 */
public class NyARTemplatePatchImage
{
	public final static int AR2_TEMP_SCALE = 3;
	public final static int AR2_TEMPLATE_NULL_PIXEL = 2000000000;
	/**
	 * �?ンプレートサイズ
	 */
	public int xsize;
	/**
	 * �?ンプレートサイズ
	 */
	public int ysize;
	/**
	 * x方向�?��?ンプレート�?�域
	 * xsize=xts*2+1
	 */
	public int xts;
	/**
	 * y方向�?��?ンプレート�?�域
	 * ysize=yts*2+1
	 */
	public int yts;
	/**
	 * �?ンプレートイメージ。この値はARToolkitNFTと異なり�?�生�?ータであるので注意すること?�?
	 */
	public int[] img;
	/**
	 * length of vector *img
	 */
	public int vlen;
	/**
	 * 有効なピクセル値の合計�?�
	 */
	public int sum_of_img;
	/**
	 * 有効なピクセルの数
	 */
	public int num_of_pixels;
	/**
	 * 有効なピクセルの平�?値
	 */
	public int ave;
	


	/**
	 * 1bitを中�?に�?(i_tx*2+1)*(i_ty*2+1)の�?ンプレートを生�?�する�??
	 * @param i_tx
	 * @param i_ty
	 */
	public NyARTemplatePatchImage(int i_tx, int i_ty)
	{
		this.xts = i_tx;
		this.yts = i_ty;

		int xsize, ysize;
		this.xsize = xsize = i_tx*2 + 1;
		this.ysize = ysize = i_ty*2 + 1;
		this.img = new int[xsize * ysize];
	}

	private NyARDoublePoint2d __in=new NyARDoublePoint2d();
	
	/**
	 * 元ar2GenTemplate関数�?
	 * 与えられた座標を中�?に、テンプレート画像を生�?�する�??
	 * 座標�?�観察座標点�?
	 * @param i_x
	 * @param i_y
	 * @param i_scale
	 * @param o_template
	 * @return
	 * @throws NyARRuntimeException
	 */
	public void makeFromReferenceImage(double i_x,double i_y,NyARDoubleMatrix44 i_ref_ctrans,INyARCameraDistortionFactor i_ref_dist_factor,jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.imageset.ReferenceImage i_source) throws NyARRuntimeException
	{
		int[] img = this.img;
		int img1_ptr=0;
		int k = 0;
		int r1=0;
		int r2=0;
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

				    //座標計算と値取得�?��?けよ�?�?
				    if( ix < 0 || ix >= i_source.width || iy < 0 || iy >= i_source.height ){
				    	img[img1_ptr] = AR2_TEMPLATE_NULL_PIXEL;
				    }else{
					    int ret = img[img1_ptr] =0xff & i_source.img[iy*i_source.width+ix];
						r1+=ret*ret;
						r2+=ret;
						k++;
				    }
				    //byte値はint�?
				}				    
				img1_ptr++;
			}
		}
		int ave= r2/k;
		this.vlen = (int)Math.sqrt((double)(r1-2*r2*ave+ave*ave*k));
		this.sum_of_img=r2;
		this.ave=ave;
		this.num_of_pixels=k;
		return;
	}	
}