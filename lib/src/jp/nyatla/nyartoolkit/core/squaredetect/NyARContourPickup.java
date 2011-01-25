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
package jp.nyatla.nyartoolkit.core.squaredetect;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、輪郭線の抽出クラスです。
 * 画像中の１点を開始点として、８方位探索で輪郭線を抽出します。出力は輪郭点の配列です。
 * <p>入力できる画素形式 - {@link #getContour}に入力できる画素形式に制限があります。<br/>
 * {@link NyARBinRaster}
 * <ul>
 * <li>{@link NyARBufferType#INT1D_BIN_8}
 * </ul>
 * {@link NyARGrayscaleRaster}
 * <ul>
 * <li>{@link NyARBufferType#INT1D_GRAY_8}
 * </ul>
 * </p>
 */
public class NyARContourPickup
{
	//巡回参照できるように、テーブルを二重化
	//                                           0  1  2  3  4  5  6  7   0  1  2  3  4  5  6
	/** 8方位探索の座標マップ*/
	protected final static int[] _getContour_xdir = { 0, 1, 1, 1, 0,-1,-1,-1 , 0, 1, 1, 1, 0,-1,-1};
	/** 8方位探索の座標マップ*/
	protected final static int[] _getContour_ydir = {-1,-1, 0, 1, 1, 1, 0,-1 ,-1,-1, 0, 1, 1, 1, 0};

	/**
	 * この関数は、ラスタの指定点を基点に、輪郭線を抽出します。
	 * 開始点は、輪郭の一部である必要があります。
	 * 通常は、ラべリングの結果の上辺クリップとX軸エントリポイントを開始点として入力します。
	 * @param i_raster
	 * 輪郭線を抽出するラスタを指定します。
	 * @param i_entry_x
	 * 輪郭抽出の開始点です。
	 * @param i_entry_y
	 * 輪郭抽出の開始点です。
	 * @param o_coord
	 * 輪郭点を格納するオブジェクトを指定します。
	 * @return
	 * 輪郭線がo_coordの長さを超えた場合、falseを返します。
	 * @throws NyARException
	 */
	public boolean getContour(NyARBinRaster i_raster,int i_entry_x,int i_entry_y,NyARIntCoordinates o_coord) throws NyARException
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
		NyARIntSize s=i_raster.getSize();
		return impl_getContour(i_raster,0,0,s.w-1,s.h-1,0,i_entry_x,i_entry_y,o_coord);
	}
	/**
	 * この関数は、ラスタの指定点を基点に、画像の特定の範囲内から輪郭線を抽出します。
	 * 開始点は、輪郭の一部である必要があります。
	 * 通常は、ラべリングの結果の上辺クリップとX軸エントリポイントを開始点として入力します。
	 * @param i_raster
	 * 輪郭線を抽出するラスタを指定します。
	 * @param i_area
	 * 輪郭線の抽出範囲を指定する矩形。i_rasterのサイズ内である必要があります。
	 * @param i_entry_x
	 * 輪郭抽出の開始点です。
	 * @param i_entry_y
	 * 輪郭抽出の開始点です。
	 * @param o_coord
	 * 輪郭点を格納するオブジェクトを指定します。
	 * @return
	 * 輪郭線がo_coordの長さを超えた場合、falseを返します。
	 * @throws NyARException
	 */	
	public boolean getContour(NyARBinRaster i_raster,NyARIntRect i_area,int i_entry_x,int i_entry_y,NyARIntCoordinates o_coord) throws NyARException
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
		return impl_getContour(i_raster,i_area.x,i_area.y,i_area.x+i_area.w-1,i_area.h+i_area.y-1,0,i_entry_x,i_entry_y,o_coord);
	}
	/**
	 * この関数は、ラスタの指定点を基点に、輪郭線を抽出します。
	 * 開始点は、輪郭の一部である必要があります。
	 * 通常は、ラべリングの結果の上辺クリップとX軸エントリポイントを開始点として入力します。
	 * @param i_raster
	 * 輪郭線を抽出するラスタを指定します。
	 * @param i_th
	 * 輪郭とみなす暗点の敷居値を指定します。
	 * @param i_entry_x
	 * 輪郭抽出の開始点です。
	 * @param i_entry_y
	 * 輪郭抽出の開始点です。
	 * @param o_coord
	 * 輪郭点を格納する配列を指定します。i_array_sizeよりも大きなサイズの配列が必要です。
	 * @return
	 * 輪郭の抽出に成功するとtrueを返します。輪郭抽出に十分なバッファが無いと、falseになります。
	 * @throws NyARException
	 */
	public boolean getContour(NyARGrayscaleRaster i_raster,int i_th,int i_entry_x,int i_entry_y,NyARIntCoordinates o_coord) throws NyARException
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		NyARIntSize s=i_raster.getSize();
		return impl_getContour(i_raster,0,0,s.w-1,s.h-1,i_th,i_entry_x,i_entry_y,o_coord);
	}
	/**
	 * この関数は、ラスタの指定点を基点に、画像の特定の範囲内から輪郭線を抽出します。
	 * 開始点は、輪郭の一部である必要があります。
	 * 通常は、ラべリングの結果の上辺クリップとX軸エントリポイントを開始点として入力します。
	 * @param i_raster
	 * 輪郭線を抽出するラスタを指定します。
	 * @param i_area
	 * 輪郭線の抽出範囲を指定する矩形。i_rasterのサイズ内である必要があります。
	 * @param i_th
	 * 輪郭とみなす暗点の敷居値を指定します。
	 * @param i_entry_x
	 * 輪郭抽出の開始点です。
	 * @param i_entry_y
	 * 輪郭抽出の開始点です。
	 * @param o_coord
	 * 輪郭点を格納するオブジェクトを指定します。
	 * @return
	 * 輪郭線がo_coordの長さを超えた場合、falseを返します。
	 * @throws NyARException
	 */
	public boolean getContour(NyARGrayscaleRaster i_raster,NyARIntRect i_area,int i_th,int i_entry_x,int i_entry_y,NyARIntCoordinates o_coord) throws NyARException
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		return impl_getContour(i_raster,i_area.x,i_area.y,i_area.x+i_area.w-1,i_area.h+i_area.y-1,i_th,i_entry_x,i_entry_y,o_coord);
	}
	
	/**
	 * 輪郭線抽出関数の実体です。
	 * @param i_raster
	 * @param i_l
	 * @param i_t
	 * @param i_r
	 * @param i_b
	 * @param i_th
	 * @param i_entry_x
	 * @param i_entry_y
	 * @param o_coord
	 * @return
	 * @throws NyARException
	 */
	private boolean impl_getContour(INyARRaster i_raster,int i_l,int i_t,int i_r,int i_b,int i_th,int i_entry_x,int i_entry_y,NyARIntCoordinates o_coord) throws NyARException
	{
		assert(i_t<=i_entry_x);
		NyARIntPoint2d[] coord=o_coord.items;
		final int[] xdir = _getContour_xdir;// static int xdir[8] = { 0, 1, 1, 1, 0,-1,-1,-1};
		final int[] ydir = _getContour_ydir;// static int ydir[8] = {-1,-1, 0, 1, 1, 1, 0,-1};

		final int[] buf=(int[])i_raster.getBuffer();
		final int width=i_raster.getWidth();
		//クリップ領域の上端に接しているポイントを得る。


		int max_coord=o_coord.items.length;
		int coord_num = 1;
		coord[0].x = i_entry_x;
		coord[0].y = i_entry_y;
		int dir = 5;

		int c = i_entry_x;
		int r = i_entry_y;
		for (;;) {
			dir = (dir + 5) % 8;//dirの正規化
			//ここは頑張ればもっと最適化できると思うよ。
			//4隅以外の境界接地の場合に、境界チェックを省略するとかね。
			if(c>i_l && c<i_r && r>i_t && r<i_b){
				for(;;){//gotoのエミュレート用のfor文
					//境界に接していないとき(暗点判定)
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}

					//8方向全て調べたけどラベルが無いよ？
					throw new NyARException();			
				}
			}else{
				//境界に接しているとき
				int i;
				for (i = 0; i < 8; i++){				
					final int x=c + xdir[dir];
					final int y=r + ydir[dir];
					//境界チェック
					if(x>=i_l && x<=i_r && y>=i_t && y<=i_b){
						if (buf[(y)*width+(x)] <= i_th) {
							break;
						}
					}
					dir++;//倍長テーブルを参照するので問題なし
				}
				if (i == 8) {
					//8方向全て調べたけどラベルが無いよ？
					throw new NyARException();// return(-1);
				}				
			}

			// xcoordとycoordをc,rにも保存
			c = c + xdir[dir];
			r = r + ydir[dir];
			coord[coord_num].x = c;
			coord[coord_num].y = r;
			//終了条件判定
			if (c == i_entry_x && r == i_entry_y){
				//開始点と同じピクセルに到達したら、終点の可能性がある。
				coord_num++;
				//末端のチェック
				if (coord_num == max_coord) {
					//輪郭bufが末端に達した
					return false;
				}				
				//末端候補の次のピクセルを調べる
				dir = (dir + 5) % 8;//dirの正規化
				int i;
				for (i = 0; i < 8; i++){				
					final int x=c + xdir[dir];
					final int y=r + ydir[dir];
					//境界チェック
					if(x>=i_l && x<=i_r && y>=i_t && y<=i_b){
						if (buf[(y)*width+(x)] <= i_th) {
							break;
						}
					}
					dir++;//倍長テーブルを参照するので問題なし
				}
				if (i == 8) {
					//8方向全て調べたけどラベルが無いよ？
					throw new NyARException();
				}
				//得たピクセルが、[1]と同じならば、末端である。
				c = c + xdir[dir];
				r = r + ydir[dir];
				if(coord[1].x ==c && coord[1].y ==r){
					//終点に達している。
					o_coord.length=coord_num;
					break;
				}else{
					//終点ではない。
					coord[coord_num].x = c;
					coord[coord_num].y = r;
				}
			}
			coord_num++;
			//末端のチェック
			if (coord_num == max_coord) {
				//輪郭が末端に達した
				return false;
			}
		}
		return true;
	}
}
