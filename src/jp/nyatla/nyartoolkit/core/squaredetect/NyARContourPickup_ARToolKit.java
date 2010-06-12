package jp.nyatla.nyartoolkit.core.squaredetect;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

public class NyARContourPickup_ARToolKit extends NyARContourPickup
{
	public int getContour(NyARLabelingImage i_raster,int i_entry_x,int i_entry_y,int i_array_size,NyARIntPoint2d[] o_coord) throws NyARException
	{	
		final int[] xdir = _getContour_xdir;// static int xdir[8] = { 0, 1, 1, 1, 0,-1,-1,-1};
		final int[] ydir = _getContour_ydir;// static int ydir[8] = {-1,-1, 0, 1, 1, 1, 0,-1};

		final int[] i_buf=(int[])i_raster.getBuffer();
		final int width=i_raster.getWidth();
		final int height=i_raster.getHeight();
		//クリップ領域の上端に接しているポイントを得る。
		int sx=i_entry_x;
		int sy=i_entry_y;

		int coord_num = 1;
		o_coord[0].x = sx;
		o_coord[0].y = sy;
		int dir = 5;

		int c = o_coord[0].x;
		int r = o_coord[0].y;
		for (;;) {
			dir = (dir + 5) % 8;//dirの正規化
			//ここは頑張ればもっと最適化できると思うよ。
			//4隅以外の境界接地の場合に、境界チェックを省略するとかね。
			if(c>=1 && c<width-1 && r>=1 && r<height-1){
				for(;;){//gotoのエミュレート用のfor文
					//境界に接していないとき
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
						break;
					}
					dir++;
					if (i_buf[(r + ydir[dir])*width+(c + xdir[dir])] > 0) {
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
					if(x>=0 && x<width && y>=0 && y<height){
						if (i_buf[(y)*width+(x)] > 0) {
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
			if (c == sx && r == sy){
				coord_num++;
				break;
			}
			coord_num++;
			if (coord_num == i_array_size) {
				//輪郭が末端に達した
				return coord_num;
			}
		}
		return coord_num;
	}
}
