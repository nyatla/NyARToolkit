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
 * 輪郭線を取得するクラスです。
 *
 */
public class NyARContourPickup
{
	//巡回参照できるように、テーブルを二重化
	//                                           0  1  2  3  4  5  6  7   0  1  2  3  4  5  6
	protected final static int[] _getContour_xdir = { 0, 1, 1, 1, 0,-1,-1,-1 , 0, 1, 1, 1, 0,-1,-1};
	protected final static int[] _getContour_ydir = {-1,-1, 0, 1, 1, 1, 0,-1 ,-1,-1, 0, 1, 1, 1, 0};
	public int getContour(NyARBinRaster i_raster,int i_entry_x,int i_entry_y,NyARIntPoint2d[] o_coord) throws NyARException
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_BIN_8));
		NyARIntSize s=i_raster.getSize();
		return impl_getContour(i_raster,0,0,s.w-1,s.h-1,0,i_entry_x,i_entry_y,o_coord);
	}
	/**
	 * 
	 * @param i_raster
	 * @param i_th
	 * 画像を２値化するための閾値。暗点<=i_th<明点となります。
	 * @param i_entry_x
	 * 輪郭の追跡開始点を指定します。
	 * @param i_entry_y
	 * @param o_coord
	 * @return
	 * @throws NyARException
	 */
	public int getContour(NyARGrayscaleRaster i_raster,int i_th,int i_entry_x,int i_entry_y,NyARIntPoint2d[] o_coord) throws NyARException
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		NyARIntSize s=i_raster.getSize();
		return impl_getContour(i_raster,0,0,s.w-1,s.h-1,i_th,i_entry_x,i_entry_y,o_coord);
	}
	public int getContour(NyARGrayscaleRaster i_raster,NyARIntRect i_area,int i_th,int i_entry_x,int i_entry_y,NyARIntPoint2d[] o_coord) throws NyARException
	{
		assert(i_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		return impl_getContour(i_raster,i_area.x,i_area.y,i_area.x+i_area.w-1,i_area.h+i_area.y-1,i_th,i_entry_x,i_entry_y,o_coord);
	}
	
	/**
	 * ラスタのエントリポイントから辿れる輪郭線を配列に返します。
	 * 探索範囲は、i_l<=n<=i_r,i_t<=n<=i_b
	 * @param i_raster
	 * @param i_l
	 * @param i_t
	 * @param i_r
	 * @param i_b
	 * @param i_th
	 * 暗点<=th<明点
	 * @param i_entry_x
	 * @param i_entry_y
	 * @param o_coord
	 * @return
	 * 輪郭線の長さを返します。
	 * @throws NyARException
	 */
	private int impl_getContour(INyARRaster i_raster,int i_l,int i_t,int i_r,int i_b,int i_th,int i_entry_x,int i_entry_y,NyARIntPoint2d[] o_coord) throws NyARException
	{
		final int[] xdir = _getContour_xdir;// static int xdir[8] = { 0, 1, 1, 1, 0,-1,-1,-1};
		final int[] ydir = _getContour_ydir;// static int ydir[8] = {-1,-1, 0, 1, 1, 1, 0,-1};

		final int[] i_buf=(int[])i_raster.getBuffer();
		final int width=i_raster.getWidth();
		//クリップ領域の上端に接しているポイントを得る。


		int max_coord=o_coord.length;
		int coord_num = 1;
		o_coord[0].x = i_entry_x;
		o_coord[0].y = i_entry_y;
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
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] <= i_th) {
						break;
					}
/*
					try{
						BufferedImage b=new BufferedImage(width,height,ColorSpace.TYPE_RGB);
						NyARRasterImageIO.copy(i_raster, b);
					ImageIO.write(b,"png",new File("bug.png"));
					}catch(Exception e){
						
					}*/
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
						if (i_buf[(y)*width+(x)] <= i_th) {
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
			
			dir=dir% 8;//dirの正規化

			// xcoordとycoordをc,rにも保存
			c = c + xdir[dir];
			r = r + ydir[dir];
			o_coord[coord_num].x = c;
			o_coord[coord_num].y = r;
			// 終了条件判定
			if (c == i_entry_x && r == i_entry_y){
				coord_num++;
				break;
			}
			coord_num++;
			if (coord_num == max_coord) {
				//輪郭が末端に達した
				return coord_num;
			}
		}
		return coord_num;
	}
}
