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
package jp.nyatla.nyartoolkit.core.pickup;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.raster.*;

/**
 * 24ビットカラーのマーカーを保持するために使うクラスです。 このクラスは、ARToolkitのパターンと、ラスタから取得したパターンを保持します。
 * 演算順序を含む最適化をしたもの
 * 
 */
public class NyARColorPatt_O3 implements INyARColorPatt
{
	private static final int AR_PATT_SAMPLE_NUM = 64;
	private static final int BUFFER_FORMAT=NyARBufferType.INT1D_X8R8G8B8_32;

	private int[] _patdata;
	private NyARIntSize _size;
	private NyARRgbPixelReader_INT1D_X8R8G8B8_32 _pixelreader;
	
	public NyARColorPatt_O3(int i_width, int i_height)
	{
		this._size=new NyARIntSize(i_width,i_height);
		this._patdata = new int[i_height*i_width];
		this._pixelreader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32(this._patdata,this._size);
	}
	public int getWidth()
	{
		return this._size.w;
	}
	public int getHeight()
	{
		return this._size.h;
	}
	public NyARIntSize getSize()
	{
		return 	this._size;
	}
	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._pixelreader;
	}
	public Object getBuffer()
	{
		return this._patdata;
	}
	public boolean hasBuffer()
	{
		return this._patdata!=null;
	}
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		NyARException.notImplement();
	}
	final public int getBufferType()
	{
		return BUFFER_FORMAT;
	}
	final public boolean isEqualBufferType(int i_type_value)
	{
		return BUFFER_FORMAT==i_type_value;
	}
	private final NyARMat wk_get_cpara_a = new NyARMat(8, 8);
	private final NyARMat wk_get_cpara_b = new NyARMat(8, 1);
	private final NyARMat wk_pickFromRaster_cpara = new NyARMat(8, 1);

	/**
	 * @param world
	 * @param vertex
	 * @param o_para
	 * @throws NyARException
	 */
	private boolean get_cpara(final NyARIntPoint2d[] i_vertex, NyARMat o_para)throws NyARException
	{
		int[][] world = wk_pickFromRaster_world;
		NyARMat a = wk_get_cpara_a;// 次処理で値を設定するので、初期化不要// new NyARMat( 8, 8 );
		double[][] a_array = a.getArray();
		NyARMat b = wk_get_cpara_b;// 次処理で値を設定するので、初期化不要// new NyARMat( 8, 1 );
		double[][] b_array = b.getArray();
		double[] a_pt0, a_pt1;
		int[] world_pti;

		for (int i = 0; i < 4; i++) {
			a_pt0 = a_array[i * 2];
			a_pt1 = a_array[i * 2 + 1];
			world_pti = world[i];

			a_pt0[0] = (double) world_pti[0];// a->m[i*16+0] = world[i][0];
			a_pt0[1] = (double) world_pti[1];// a->m[i*16+1] = world[i][1];
			a_pt0[2] = 1.0;// a->m[i*16+2] = 1.0;
			a_pt0[3] = 0.0;// a->m[i*16+3] = 0.0;
			a_pt0[4] = 0.0;// a->m[i*16+4] = 0.0;
			a_pt0[5] = 0.0;// a->m[i*16+5] = 0.0;
			a_pt0[6] = (double) (-world_pti[0] * i_vertex[i].x);// a->m[i*16+6]= -world[i][0]*vertex[i][0];
			a_pt0[7] = (double) (-world_pti[1] * i_vertex[i].x);// a->m[i*16+7]=-world[i][1]*vertex[i][0];
			a_pt1[0] = 0.0;// a->m[i*16+8] = 0.0;
			a_pt1[1] = 0.0;// a->m[i*16+9] = 0.0;
			a_pt1[2] = 0.0;// a->m[i*16+10] = 0.0;
			a_pt1[3] = (double) world_pti[0];// a->m[i*16+11] = world[i][0];
			a_pt1[4] = (double) world_pti[1];// a->m[i*16+12] = world[i][1];
			a_pt1[5] = 1.0;// a->m[i*16+13] = 1.0;
			a_pt1[6] = (double) (-world_pti[0] * i_vertex[i].y);// a->m[i*16+14]=-world[i][0]*vertex[i][1];
			a_pt1[7] = (double) (-world_pti[1] * i_vertex[i].y);// a->m[i*16+15]=-world[i][1]*vertex[i][1];
			b_array[i * 2 + 0][0] = (double) i_vertex[i].x;// b->m[i*2+0] =vertex[i][0];
			b_array[i * 2 + 1][0] = (double) i_vertex[i].y;// b->m[i*2+1] =vertex[i][1];
		}
		if (!a.matrixSelfInv()) {
			return false;
		}

		o_para.matrixMul(a, b);
		return true;
	}

	// private final double[] wk_pickFromRaster_para=new double[9];//[3][3];
	private final static int[][] wk_pickFromRaster_world = {// double world[4][2];
	{ 100, 100 }, { 100 + 10, 100 }, { 100 + 10, 100 + 10 }, { 100, 100 + 10 } };



	/**
	 * @see INyARColorPatt#pickFromRaster
	 */
	public boolean pickFromRaster(INyARRgbRaster image,NyARIntPoint2d[] i_vertexs)throws NyARException
	{
		NyARMat cpara = this.wk_pickFromRaster_cpara;
		// xdiv2,ydiv2の計算
		int xdiv2, ydiv2;
		int l1, l2;
		int w1, w2;
		// x計算
		w1 = i_vertexs[0].x - i_vertexs[1].x;
		w2 = i_vertexs[0].y - i_vertexs[1].y;
		l1 = (w1 * w1 + w2 * w2);
		w1 = i_vertexs[2].x - i_vertexs[3].x;
		w2 = i_vertexs[2].y - i_vertexs[3].y;
		l2 = (w1 * w1 + w2 * w2);
		if (l2 > l1) {
			l1 = l2;
		}
		l1 = l1 / 4;
		xdiv2 = this._size.w;
		while (xdiv2 * xdiv2 < l1) {
			xdiv2 *= 2;
		}
		if (xdiv2 > AR_PATT_SAMPLE_NUM) {
			xdiv2 = AR_PATT_SAMPLE_NUM;
		}

		// y計算
		w1 = i_vertexs[1].x - i_vertexs[2].x;
		w2 = i_vertexs[1].y - i_vertexs[2].y;
		l1 = (w1 * w1 + w2 * w2);
		w1 = i_vertexs[3].x - i_vertexs[0].x;
		w2 = i_vertexs[3].y - i_vertexs[0].y;
		l2 = (w1 * w1 + w2 * w2);
		if (l2 > l1) {
			l1 = l2;
		}
		ydiv2 = this._size.h;
		l1 = l1 / 4;
		while (ydiv2 * ydiv2 < l1) {
			ydiv2 *= 2;
		}
		if (ydiv2 > AR_PATT_SAMPLE_NUM) {
			ydiv2 = AR_PATT_SAMPLE_NUM;
		}

		// cparaの計算
		if (!get_cpara(i_vertexs, cpara)) {
			return false;
		}
		updateExtpat(image, cpara, xdiv2, ydiv2);

		return true;
	}
	private int[] __updateExtpat_rgbset;
	private int[] __updateExtpat_xc;
	private int[] __updateExtpat_yc;
	private double[] __updateExtpat_xw;
	private double[] __updateExtpat_yw;
	private int _last_pix_resolution_x=0;
	private int _last_pix_resolution_y=0;
	private void reservWorkBuffers(int i_xdiv,int i_ydiv)
	{
		if(this._last_pix_resolution_x<i_xdiv || this._last_pix_resolution_y<i_ydiv){
			this.__updateExtpat_xc=new int[i_xdiv*i_ydiv];
			this.__updateExtpat_yc=new int[i_xdiv*i_ydiv];
			this.__updateExtpat_xw=new double[i_xdiv];
			this.__updateExtpat_yw=new double[i_ydiv];
			this.__updateExtpat_rgbset=new int[i_xdiv*i_ydiv*3];
			this._last_pix_resolution_x=i_xdiv;
			this._last_pix_resolution_y=i_ydiv;
		}
		return;
	}
	private static double LT_POS=102.5;
	private static double SQ_SIZE=5.0;
	
	//分割数16未満になると少し遅くなるかも。
	private void updateExtpat(INyARRgbRaster image, NyARMat i_cpara, int i_xdiv2,int i_ydiv2) throws NyARException
	{

		int i,j;
		int r,g,b;
		//ピクセルリーダーを取得
		final int pat_size_w=this._size.w;
		final int xdiv = i_xdiv2 / pat_size_w;// xdiv = xdiv2/Config.AR_PATT_SIZE_X;
		final int ydiv = i_ydiv2 / this._size.h;// ydiv = ydiv2/Config.AR_PATT_SIZE_Y;
		final int xdiv_x_ydiv = xdiv * ydiv;
		double reciprocal;
		final double[][] para=i_cpara.getArray();
		final double para00=para[0*3+0][0];
		final double para01=para[0*3+1][0];
		final double para02=para[0*3+2][0];
		final double para10=para[1*3+0][0];
		final double para11=para[1*3+1][0];
		final double para12=para[1*3+2][0];
		final double para20=para[2*3+0][0];
		final double para21=para[2*3+1][0];

		INyARRgbPixelReader reader=image.getRgbPixelReader();
		final int img_width=image.getWidth();
		final int img_height=image.getHeight();

		//ワークバッファの準備
		reservWorkBuffers(xdiv,ydiv);
		final double[] xw=this.__updateExtpat_xw;
		final double[] yw=this.__updateExtpat_yw;
		final int[] xc=this.__updateExtpat_xc;
		final int[] yc=this.__updateExtpat_yc;
		int[] rgb_set = this.__updateExtpat_rgbset;

		
		for(int iy=this._size.h-1;iy>=0;iy--){
			for(int ix=pat_size_w-1;ix>=0;ix--){
				//xw,ywマップを作成
				reciprocal= 1.0 / i_xdiv2;
				for(i=xdiv-1;i>=0;i--){
					xw[i]=LT_POS + SQ_SIZE * (ix*xdiv+i + 0.5) * reciprocal;
				}
				reciprocal= 1.0 / i_ydiv2;
				for(i=ydiv-1;i>=0;i--){
					yw[i]=LT_POS + SQ_SIZE * (iy*ydiv+i + 0.5) * reciprocal;
				}
				//1ピクセルを構成するピクセル座標の集合をxc,yc配列に取得
				int number_of_pix=0;
				for(i=ydiv-1;i>=0;i--)
				{
					final double para01_x_yw_para02=para01 * yw[i] + para02;
					final double para11_x_yw_para12=para11 * yw[i] + para12;
					final double para12_x_yw_para22=para21 * yw[i]+ 1.0;
					for(j=xdiv-1;j>=0;j--){
							
						final double d = para20 * xw[j] + para12_x_yw_para22;
						if (d == 0) {
							throw new NyARException();
						}
						final int xcw= (int) ((para00 * xw[j] + para01_x_yw_para02) / d);
						final int ycw= (int) ((para10 * xw[j] + para11_x_yw_para12) / d);
						if(xcw<0 || xcw>=img_width || ycw<0 ||ycw>=img_height){
							continue;
						}
						xc[number_of_pix] =xcw;
						yc[number_of_pix] =ycw;
						number_of_pix++;
					}
				}
				//1ピクセル分の配列を取得
				reader.getPixelSet(xc,yc,number_of_pix, rgb_set);
				r=g=b=0;
				for(i=number_of_pix*3-1;i>=0;i-=3){
					r += rgb_set[i-2];// R
					g += rgb_set[i-1];// G
					b += rgb_set[i];// B
				}
				//1ピクセル確定
				this._patdata[iy*pat_size_w+ix]=(((r / xdiv_x_ydiv)&0xff)<<16)|(((g / xdiv_x_ydiv)&0xff)<<8)|(((b / xdiv_x_ydiv)&0xff));
			}
		}
		return;
	}
}