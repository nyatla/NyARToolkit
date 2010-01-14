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
package jp.nyatla.nyartoolkit.core.rasteranalyzer;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * QRコードの4頂点候補を探そうとするクラス。
 * 未完成
 *
 */
public class NyARRasterDetector_QrCodeEdge
{
	private NyARIntRectStack _result;

	public NyARRasterDetector_QrCodeEdge(int i_result_max) throws NyARException
	{
		this._result = new NyARIntRectStack(i_result_max);
		return;
	}

	public NyARIntRectStack geResult()
	{
		return this._result;
	}

	private boolean check_w1(int i_w1)
	{
		return i_w1>=1;		
	}
	private boolean check_b1(int i_b1)
	{
		return i_b1 >= 2;		
	}
	private boolean check_w2(int i_b1,int i_w2)
	{
		int v=i_w2*100/i_b1;
		return (30<=v && v<=170);
	}
	private boolean check_b2(int i_b1,int i_b2)
	{
		int v=i_b2*100/i_b1;
		//条件:(b1)/2の2～4倍
		return (200<=v && v<=400);
	}
	private boolean check_w3(int i_w2,int i_w3)
	{
		int v=i_w3*100/i_w2;
		return (50<=v && v<=150);
	}
	private boolean check_b3(int i_b3,int i_b1)
	{
		int v=i_b3*100/i_b1;
		return (50<=v && v<=150);
	}	
	public void analyzeRaster(INyARRaster i_input) throws NyARException
	{
		assert (i_input.isEqualBufferType(NyARBufferType.INT1D_BIN_8));

		// 結果をクリア
		this._result.clear();

		NyARIntSize size = i_input.getSize();
		int x = 0;
		int w1, b1, w2, b2, w3, b3;
		w1 = b1 = w2 = b2 = w3 = b3 = 0;

		NyARIntRect item;
		int[] raster_buf=(int[])i_input.getBuffer();
		int line_ptr;
		int s_pos, b2_spos,b3_spos;
		b2_spos=0;
		for (int y = size.h - 1-8; y >= 8; y--) {
			line_ptr = y*size.w;
			x = size.w - 1;
			s_pos=0;
			int token_id=0;
			while(x>=0){
				switch(token_id){
				case 0:
					// w1の特定
					w1 = 0;
					for (; x >= 0; x--) {
						if (raster_buf[line_ptr+x] == 0) {
							// 検出条件確認:w1は2以上欲しいな。
							if (!check_w1(w1)) {
								// 条件不十分
								continue;
							}else{
								// 検出→次段処理へ
								token_id=1;
							}
							break;
						}
						w1++;
					}
					break;
				case 1:
					// b1の特定
					b1 = 0;
					s_pos = x;
					for (; x >= 0; x--) {
						if (raster_buf[line_ptr+x] > 0) {
							// 検出条件確認:b1は1以上欲しいな。
							if (!check_b1(b1)){
								//条件不十分→白検出からやり直し
								token_id=0;
							}else{
								// 検出→次段処理へ
								token_id=2;
							}
							break;
						}
						b1++;
					}
					break;
				case 2:
					// w2の特定
					w2 = 0;
					for (; x >= 0; x--) {
						if (raster_buf[line_ptr+x] == 0) {
							// 検出条件確認:w2*10/b1は80-120以上欲しいな。
							if (!check_w2(b1,w2)) {
								//条件不十分→w2→w1として、b1を解析
								w1=w2;
								token_id=1;
							}else{
								// 検出→次段処理へ
//								w1=w2;
//								token_id=11;
								token_id=3;
							}
							break;
						}
						w2++;
					}
					break;
				case 3:
					// b2の特定
					b2 = 0;
					b2_spos=x;
					for (; x >= 0; x--) {
						if (raster_buf[line_ptr+x] > 0){
							//条件:(w1+b1)/2の2～4倍

							if (!check_b2(b1,b2)) {
								// b2->b1と仮定して解析しなおす。
								if(check_w1(w2) && check_b1(b2)){
									w1 = w2;
									b1 = b2;
									s_pos=b2_spos;
									token_id=2;
								}else{
									
									token_id=0;
								}
							}else{
								// 検出→次段処理へ
//								token_id=10;
								token_id=4;
							}
							break;
						}
						b2++;
					}
					break;
				case 4:
					// w3の特定
					w3 = 0;
					for (; x >= 0; x--) {
						if (raster_buf[line_ptr+x] == 0){
							if (!check_w3(w2,w3)) {
								//w2→w1,b2->b1として解析しなおす。
								if(check_w1(w2) && check_b1(b2)){
									w1 = w2;
									b1 = b2;
									s_pos=b2_spos;
									token_id=2;
								}else{
									token_id=0;
								}
							}else{
								// 検出→次段処理へ
//								w1=w3;
//								token_id=10;
								token_id=5;
							}
							break;
						}
						w3++;
					}
					break;
				case 5:
					// b3の特定
					b3 = 0;
					b3_spos=x;
					for (; x >= 0; x--) {
						if (raster_buf[line_ptr+x] > 0) {
							// 検出条件確認
							if (!check_b3(b3,b1)) {
								if(check_w1(w2) && check_b1(b2)){
								//条件不十分→b3->b1,w3->w1として再解析
									w1=w3;
									b1=b3;
									s_pos=b3_spos;
									token_id=2;
								}else{
									token_id=0;
								}
							}else{
								// 検出→次段処理へ
								token_id=10;
							}
							break;
						}
						b3++;
					}
					break;
				case 10:
					/* コード特定→保管 */
					item = this._result.prePush();
					item.x = x;
					item.y = y;
					item.w =s_pos-x;
					item.h=0;
					/* 最大個数？ */
					/* 次のコードを探す */
					token_id=0;
					break;
				default:
					throw new NyARException();
				}
			}
		}
		return;
	}
}
