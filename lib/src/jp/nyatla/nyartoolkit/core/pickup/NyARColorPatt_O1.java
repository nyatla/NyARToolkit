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
 * 演算順序以外の最適化をしたもの
 * 
 */
public class NyARColorPatt_O1 implements INyARColorPatt
{
	private static final int AR_PATT_SAMPLE_NUM = 64;
	private static final int BUFFER_FORMAT=NyARBufferType.INT1D_X8R8G8B8_32;
	private int[] _patdata;
	private NyARRgbPixelReader_INT1D_X8R8G8B8_32 _pixelreader;

	private NyARIntSize _size;

	public NyARColorPatt_O1(int i_width, int i_height)
	{
		//入力制限
		assert i_width<=64 && i_height<=64;
		
		this._size=new NyARIntSize(i_width,i_height);
		this._patdata = new int[i_height*i_width];
		this._pixelreader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32(this._patdata,this._size);
		return;
	}

	public final int getWidth()
	{
		return this._size.w;
	}
	public final int getHeight()
	{
		return this._size.h;
	}
	public final NyARIntSize getSize()
	{
		return 	this._size;
	}
	public final INyARRgbPixelReader getRgbPixelReader()
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
	private final NyARMat __get_cpara_a = new NyARMat(8, 8);
	private final NyARMat __get_cpara_b = new NyARMat(8, 1);
	private final static double[][] __get__cpara_world = {{ 100.0, 100.0 }, { 100.0 + 10.0, 100.0 }, { 100.0 + 10.0, 100.0 + 10.0 },{ 100.0, 100.0 + 10.0 } };
	
	final protected boolean get_cpara(final NyARIntPoint2d[] i_vertex, NyARMat o_para)throws NyARException
	{
		double[][] world = __get__cpara_world;
		NyARMat a = __get_cpara_a;// 次処理で値を設定するので、初期化不要// new NyARMat( 8, 8 );
		double[][] a_array = a.getArray();
		NyARMat b = __get_cpara_b;// 次処理で値を設定するので、初期化不要// new NyARMat( 8, 1 );
		double[][] b_array = b.getArray();
		double[] a_pt0, a_pt1;
		double[] world_pti;

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
	private final int[] __pickFromRaster_rgb_tmp = new int[3];
	private final NyARMat __pickFromRaster_cpara_c = new NyARMat(8, 1);
	
	/**
	 * @see INyARColorPatt#pickFromRaster
	 */
	public boolean pickFromRaster(INyARRgbRaster image,NyARIntPoint2d[] i_vertexs)throws NyARException
	{
		// パターンの切り出しに失敗することもある。
		NyARMat cpara = this.__pickFromRaster_cpara_c;
		if (!get_cpara(i_vertexs, cpara)) {
			return false;
		}
		final double[][] para=cpara.getArray();
		final double para00=para[0*3+0][0];
		final double para01=para[0*3+1][0];
		final double para02=para[0*3+2][0];
		final double para10=para[1*3+0][0];
		final double para11=para[1*3+1][0];
		final double para12=para[1*3+2][0];
		final double para20=para[2*3+0][0];
		final double para21=para[2*3+1][0];
		final double para22=1.0;
		
		int lx1 = (int) ((i_vertexs[0].x - i_vertexs[1].x) * (i_vertexs[0].x - i_vertexs[1].x) + (i_vertexs[0].y - i_vertexs[1].y)* (i_vertexs[0].y - i_vertexs[1].y));
		int lx2 = (int) ((i_vertexs[2].x - i_vertexs[3].x) * (i_vertexs[2].x - i_vertexs[3].x) + (i_vertexs[2].y - i_vertexs[3].y)* (i_vertexs[2].y - i_vertexs[3].y));
		int ly1 = (int) ((i_vertexs[1].x - i_vertexs[2].x) * (i_vertexs[1].x - i_vertexs[2].x) + (i_vertexs[1].y - i_vertexs[2].y)* (i_vertexs[1].y - i_vertexs[2].y));
		int ly2 = (int) ((i_vertexs[3].x - i_vertexs[0].x) * (i_vertexs[3].x - i_vertexs[0].x) + (i_vertexs[3].y - i_vertexs[0].y)* (i_vertexs[3].y - i_vertexs[0].y));
		if (lx2 > lx1) {
			lx1 = lx2;
		}
		if (ly2 > ly1) {
			ly1 = ly2;
		}
		
		int sample_pixel_x = this._size.w;
		int sample_pixel_y = this._size.h;
		while (sample_pixel_x * sample_pixel_x < lx1 / 4) {
			sample_pixel_x *= 2;
		}
		while (sample_pixel_y * sample_pixel_y < ly1 / 4) {
			sample_pixel_y *= 2;
		}

		if (sample_pixel_x > AR_PATT_SAMPLE_NUM) {
			sample_pixel_x = AR_PATT_SAMPLE_NUM;
		}
		if (sample_pixel_y > AR_PATT_SAMPLE_NUM) {
			sample_pixel_y = AR_PATT_SAMPLE_NUM;
		}

		final int xdiv = sample_pixel_x / this._size.w;// xdiv = xdiv2/Config.AR_PATT_SIZE_X;
		final int ydiv = sample_pixel_y / this._size.h;// ydiv = ydiv2/Config.AR_PATT_SIZE_Y;


		int img_x = image.getWidth();
		int img_y = image.getHeight();

		final double xdiv2_reciprocal = 1.0 / sample_pixel_x;
		final double ydiv2_reciprocal = 1.0 / sample_pixel_y;
		int r,g,b;
		int[] rgb_tmp = __pickFromRaster_rgb_tmp;

		//ピクセルリーダーを取得
		INyARRgbPixelReader reader=image.getRgbPixelReader();
		final int xdiv_x_ydiv = xdiv * ydiv;

		for(int iy=0;iy<this._size.h;iy++){
			for(int ix=0;ix<this._size.w;ix++){
				r=g=b=0;
				//1ピクセルを作成
				for(int j=0;j<ydiv;j++){
					final double yw = 102.5 + 5.0 * (iy*ydiv+j + 0.5) * ydiv2_reciprocal;						
					for(int i=0;i<xdiv;i++){
						final double xw = 102.5 + 5.0 * (ix*xdiv+i + 0.5) * xdiv2_reciprocal;
						final double d = para20 * xw + para21 * yw+ para22;
						if (d == 0) {
							throw new NyARException();
						}
						final int xc = (int) ((para00 * xw + para01 * yw + para02) / d);
						final int yc = (int) ((para10 * xw + para11 * yw + para12) / d);
	
						if (xc >= 0 && xc < img_x && yc >= 0 && yc < img_y) {
							reader.getPixel(xc, yc, rgb_tmp);
							r += rgb_tmp[0];// R
							g += rgb_tmp[1];// G
							b += rgb_tmp[2];// B
							// System.out.println(xc+":"+yc+":"+rgb_tmp[0]+":"+rgb_tmp[1]+":"+rgb_tmp[2]);
						}
					}
				}
				this._patdata[iy*this._size.w+ix]=(((r / xdiv_x_ydiv)&0xff)<<16)|(((g / xdiv_x_ydiv)&0xff)<<8)|(((b / xdiv_x_ydiv)&0xff));
			}
		}
		return true;
	}
	
}