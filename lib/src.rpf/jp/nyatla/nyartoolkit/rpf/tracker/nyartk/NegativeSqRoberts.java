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

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.INyARRasterFilter;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * この関数は、{@link NyARTracker}用のエッジ検出フィルターです。
 * {@link NyARTrackerSource_Reference}から、ヒント画像を作るために使います。通常、ユーザが使うことはありません。
 * <p>フィルタの構造 - 
 * このフィルタは、Roberts勾配の値の2乗値を16倍して、範囲を0から255に制限して反転した値を出力します。
 * 右端と左端の1ピクセルは、常に0が入ります。<br/>
 * Roberts勾配のカーネルとは、以下の形式です。
 * <pre>
 * X=|-1, 0|  Y=|0,-1|
 *   | 0, 1|    |1, 0|
 * V=(sqrt(X^2+Y+2)>>4);V=V>255?255:V;
 * </pre>
 */
public class NegativeSqRoberts implements INyARRasterFilter
{
	private IdoFilterImpl _do_filter_impl; 
	/**
	 * コンストラクタです。
	 * 入力ラスタの画素形式を指定して、インスタンスを作成します。
	 * @param i_raster_type
	 * 入力するラスタの画素形式です。{@link NyARBufferType#INT1D_GRAY_8}のみ対応します。
	 * @throws NyARException
	 */
	public NegativeSqRoberts(int i_raster_type) throws NyARException
	{
		this.initInstance(i_raster_type);
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * コンストラクタから呼び出します。
	 * @param i_raster_type
	 * 入力するラスタの画素形式です。
	 * @throws NyARException
	 */
	protected void initInstance(int i_raster_type)throws NyARException
	{
		switch (i_raster_type) {
		case NyARBufferType.INT1D_GRAY_8:
			this._do_filter_impl=new IdoFilterImpl_GRAY_8();
			break;
		default:
			throw new NyARException();
		}		
	}
	/**
	 * この関数は、入力画像にフィルタ処理をして、出力画像に書き込みます。
	 */
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		this._do_filter_impl.doFilter(i_input,i_output,i_input.getSize());
	}
	/**
	 * このインタフェイスは、フィルタ処理関数を定義します。
	 * {@link NegativeSqRoberts#doFilter}から使います。
	 *
	 */
	protected interface IdoFilterImpl
	{
		/**
		 * この関数は、i_sizeのサイズを持つ入力画像をフィルタ処理して、同じくi_sizeのサイズを持つ出力画像へ書込みます。
		 * 実装クラスでは、{@link NegativeSqRoberts}のアルゴリズムでフィルタ処理を行う処理を書きます。
		 * @param i_input
		 * 入力画像
		 * @param i_output
		 * 出力画像
		 * @param i_size
		 * 入力、出力画像のサイズ
		 * @throws NyARException
		 */
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException;
	}
	private final static int SH=4;
	private class IdoFilterImpl_GRAY_8 implements IdoFilterImpl
	{
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
		{
			assert (i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			assert (i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			int[] in_ptr =(int[])i_input.getBuffer();
			int[] out_ptr=(int[])i_output.getBuffer();
			int width=i_size.w;
			int idx=0;
			int idx2=width;
			int fx,fy;
			int mod_p=(width-2)-(width-2)%4;
			for(int y=i_size.h-2;y>=0;y--){
				int p00=in_ptr[idx++];
				int p10=in_ptr[idx2++];
				int p01,p11;
				int x=width-2;
				for(;x>=mod_p;x--){
					p01=in_ptr[idx++];p11=in_ptr[idx2++];
					fx=p11-p00;fy=p10-p01;
//					out_ptr[idx-2]=255-(((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1);
					fx=(fx*fx+fy*fy)>>SH;out_ptr[idx-2]=(fx>255?0:255-fx);
					p00=p01;
					p10=p11;
				}
				for(;x>=0;x-=4){
					p01=in_ptr[idx++];p11=in_ptr[idx2++];
					fx=p11-p00;
					fy=p10-p01;
//					out_ptr[idx-2]=255-(((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1);
					fx=(fx*fx+fy*fy)>>SH;out_ptr[idx-2]=(fx>255?0:255-fx);
					p00=p01;p10=p11;
					p01=in_ptr[idx++];p11=in_ptr[idx2++];
					fx=p11-p00;
					fy=p10-p01;
//					out_ptr[idx-2]=255-(((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1);
					fx=(fx*fx+fy*fy)>>SH;out_ptr[idx-2]=(fx>255?0:255-fx);
					p00=p01;p10=p11;
					p01=in_ptr[idx++];p11=in_ptr[idx2++];
					
					fx=p11-p00;
					fy=p10-p01;
//					out_ptr[idx-2]=255-(((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1);
					fx=(fx*fx+fy*fy)>>SH;out_ptr[idx-2]=(fx>255?0:255-fx);
					p00=p01;p10=p11;

					p01=in_ptr[idx++];p11=in_ptr[idx2++];
					fx=p11-p00;
					fy=p10-p01;
//					out_ptr[idx-2]=255-(((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1);
					fx=(fx*fx+fy*fy)>>SH;out_ptr[idx-2]=(fx>255?0:255-fx);
					p00=p01;p10=p11;

				}
				out_ptr[idx-1]=255;
			}
			for(int x=width-1;x>=0;x--){
				out_ptr[idx++]=255;
			}
			return;
		}
	}
}

