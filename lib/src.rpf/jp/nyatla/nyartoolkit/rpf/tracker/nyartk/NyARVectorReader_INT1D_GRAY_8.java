/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.*;


/**
 * グレイスケールラスタに対する、特殊な画素アクセス手段を提供します。
 *
 */
public class NyARVectorReader_INT1D_GRAY_8 extends NyARVectorReader_Base
{
	/**
	 * 
	 * @param i_ref_raster
	 * 基本画像
	 * @param i_ref_raster_distortion
	 * 歪み解除オブジェクト(nullの場合歪み解除を省略)
	 * @param i_ref_rob_raster
	 * エッジ探索用のROB画像
	 * @param 
	 */
	public NyARVectorReader_INT1D_GRAY_8(NyARGrayscaleRaster i_ref_raster,NyARCameraDistortionFactor i_ref_raster_distortion,NyARGrayscaleRaster i_ref_rob_raster)
	{
		super();
		assert (i_ref_raster.getBufferType() == NyARBufferType.INT1D_GRAY_8);
		assert (i_ref_rob_raster.getBufferType() == NyARBufferType.INT1D_GRAY_8);
		this.initInstance(i_ref_raster, i_ref_raster_distortion, i_ref_rob_raster,new NyARContourPickup());
	}

	/**
	 * 画素の4近傍の画素ベクトルを取得します。 取得可能な範囲は、Rasterの1ドット内側です。 0 ,-1, 0 0, 0, 0 0 , x,
	 * 0　+ -1, y,+1 0 ,+1, 0 0, 0, 0
	 * 
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
/*	未使用につきコメントアウト
	public void getPixelVector4(int x, int y, NyARIntPoint2d o_v)
	{
		assert ((x > 0) && (y > 0) && (x) < this._ref_base_raster.getWidth() && (y) < this._ref_base_raster.getHeight());
		int[] buf = (int[])(this._ref_base_raster.getBuffer());
		int w = this._ref_base_raster.getWidth();
		int idx = w * y + x;
		o_v.x = (buf[idx + 1] - buf[idx - 1]) >> 1;
		o_v.y = (buf[idx + w] - buf[idx - w]) >> 1;
		//歪み補正どうすんの
	}
*/
	/**
	 * 画素の8近傍画素ベクトルを取得します。 取得可能な範囲は、Rasterの1ドット内側です。
	 *  -1,-2,-1　　　　-1,　0,+1
	 *   0, y,　0 +　　-2, x,+2
	 *  +1,+2,+1 　　　　-1, 0,+1
	 * 
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
/*  未使用につきコメントアウト
	public void getPixelVector8(int x, int y, NyARIntPoint2d o_v) {
		assert ((x > 0) && (y > 0) && (x) < this._ref_base_raster.getWidth() && (y) < this._ref_base_raster.getHeight());
		int[] buf = (int[])this._ref_base_raster.getBuffer();
		int sw = this._ref_base_raster.getWidth();
		int idx_0 = sw * y + x;
		int idx_p1 = idx_0 + sw;
		int idx_m1 = idx_0 - sw;
		int b = buf[idx_m1 - 1];
		int d = buf[idx_m1 + 1];
		int h = buf[idx_p1 - 1];
		int f = buf[idx_p1 + 1];
		o_v.x = ((buf[idx_0 + 1] - buf[idx_0 - 1]) >> 1)
				+ ((d - b + f - h) >> 2);
		o_v.y = ((buf[idx_p1] - buf[idx_m1]) >> 1) + ((f - d + h - b) >> 2);
		//歪み補正どうするの？
	}
*/
	/**
	 * RECT範囲内の画素ベクトルの合計値と、ベクトルのエッジ中心を取得します。 320*240の場合、
	 * RECTの範囲は(x>=0 && x<319 x+w>=0 && x+w<319),(y>=0 && y<239 x+w>=0 && x+w<319)となります。
	 * @param ix
	 * ピクセル取得を行う位置を設定します。
	 * @param iy
	 * ピクセル取得を行う位置を設定します。
	 * @param iw
	 * ピクセル取得を行う範囲を設定します。
	 * @param ih
	 * ピクセル取得を行う範囲を設定します。
	 * @param o_posvec
	 * エッジ中心とベクトルを返します。
	 * @return
	 * ベクトルの強度を返します。強度値は、差分値の二乗の合計です。
	 */
	public final int getAreaVector33(int ix, int iy, int iw, int ih,NyARVecLinear2d o_posvec)
	{
		assert (ih >= 3 && iw >= 3);
		assert ((ix >= 0) && (iy >= 0) && (ix + iw) <= this._ref_base_raster.getWidth() && (iy + ih) <= this._ref_base_raster.getHeight());
		int[] buf =(int[])this._ref_base_raster.getBuffer();
		int stride =this._ref_base_raster.getWidth();
		// x=(Σ|Vx|*Xn)/n,y=(Σ|Vy|*Yn)/n
		// x=(ΣVx)^2/(ΣVx+ΣVy)^2,y=(ΣVy)^2/(ΣVx+ΣVy)^2
		int sum_x, sum_y, sum_wx, sum_wy, sum_vx, sum_vy;
		sum_x = sum_y = sum_wx = sum_wy = sum_vx = sum_vy = 0;
		int lw=iw - 3;
		int vx, vy;
		for (int i = ih - 3; i >= 0; i--) {
			int idx_0 = stride * (i + 1 + iy) + (iw - 3 + 1 + ix);
			for (int i2 = lw; i2 >= 0; i2--){
				// 1ビット分のベクトルを計算
				int idx_p1 = idx_0 + stride;
				int idx_m1 = idx_0 - stride;
				int b = buf[idx_m1 - 1];
				int d = buf[idx_m1 + 1];
				int h = buf[idx_p1 - 1];
				int f = buf[idx_p1 + 1];
				vx = ((buf[idx_0 + 1] - buf[idx_0 - 1]) >> 1)+ ((d - b + f - h) >> 2);
				vy = ((buf[idx_p1] - buf[idx_m1]) >> 1)+ ((f - d + h - b) >> 2);
				idx_0--;

				// 加重はvectorの絶対値
				int wx = vx * vx;
				int wy = vy * vy;
				sum_wx += wx; //加重値
				sum_wy += wy; //加重値
				sum_vx += wx * vx; //加重*ベクトルの積
				sum_vy += wy * vy; //加重*ベクトルの積
				sum_x += wx * (i2 + 1);//位置
				sum_y += wy * (i + 1); //
			}
		}
		//x,dx,y,dyの計算
		double xx,yy;
		if (sum_wx == 0) {
			xx = ix + (iw >> 1);
			o_posvec.dx = 0;
		} else {
			xx = ix+(double) sum_x / sum_wx;
			o_posvec.dx = (double) sum_vx / sum_wx;
		}
		if (sum_wy == 0) {
			yy = iy + (ih >> 1);
			o_posvec.dy = 0;
		} else {
			yy = iy+(double) sum_y / sum_wy;
			o_posvec.dy = (double) sum_vy / sum_wy;
		}
		//必要なら歪みを解除
		if(this._factor!=null){
			this._factor.observ2Ideal(xx, yy, o_posvec);
		}else{
			o_posvec.x=xx;
			o_posvec.y=yy;
		}
		//加重平均の分母を返却
		return sum_wx+sum_wy;
	}
	public final int getAreaVector22(int ix, int iy, int iw, int ih,NyARVecLinear2d o_posvec)
	{
		assert (ih >= 3 && iw >= 3);
		assert ((ix >= 0) && (iy >= 0) && (ix + iw) <= this._ref_base_raster.getWidth() && (iy + ih) <= this._ref_base_raster.getHeight());
		int[] buf =(int[])this._ref_base_raster.getBuffer();
		int stride =this._ref_base_raster.getWidth();
		int sum_x, sum_y, sum_wx, sum_wy, sum_vx, sum_vy;
		sum_x = sum_y = sum_wx = sum_wy = sum_vx = sum_vy = 0;
		int vx, vy;
		int ll=iw-1;
		for (int i = 0; i<ih-1; i++) {
			int idx_0 = stride * (i+iy) + ix+1;
			int a=buf[idx_0-1];
			int b=buf[idx_0];
			int c=buf[idx_0+stride-1];
			int d=buf[idx_0+stride];
			for (int i2 = 0; i2<ll; i2++){
				// 1ビット分のベクトルを計算
				vx=(b-a+d-c)>>2;
				vy=(c-a+d-b)>>2;
				idx_0++;
				a=b;
				c=d;
				b=buf[idx_0];
				d=buf[idx_0+stride];

				// 加重はvectorの絶対値
				int wx = vx * vx;
				sum_wx += wx; //加重値
				sum_vx += wx * vx; //加重*ベクトルの積
				sum_x += wx * i2;//位置

				int wy = vy * vy;
				sum_wy += wy; //加重値
				sum_vy += wy * vy; //加重*ベクトルの積
				sum_y += wy * i; //
			}
		}
		//x,dx,y,dyの計算
		double xx,yy;
		if (sum_wx == 0) {
			xx = ix + (iw >> 1);
			o_posvec.dx = 0;
		} else {
			xx = ix+(double) sum_x / sum_wx;
			o_posvec.dx = (double) sum_vx / sum_wx;
		}
		if (sum_wy == 0) {
			yy = iy + (ih >> 1);
			o_posvec.dy = 0;
		} else {
			yy = iy+(double) sum_y / sum_wy;
			o_posvec.dy = (double) sum_vy / sum_wy;
		}
		//必要なら歪みを解除
		if(this._factor!=null){
			this._factor.observ2Ideal(xx, yy, o_posvec);
		}else{
			o_posvec.x=xx;
			o_posvec.y=yy;
		}
		//加重平均の分母を返却
		return sum_wx+sum_wy;
	}
}