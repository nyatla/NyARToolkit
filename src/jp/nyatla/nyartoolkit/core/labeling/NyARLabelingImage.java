/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARBufferReader;

/**
 *
 */
public class NyARLabelingImage extends NyARRaster_BasicClass implements INyARLabelingImage
{
	private final static int MAX_LABELS = 1024*32;	
	protected int[][] _ref_buf;
	private INyARBufferReader _buffer_reader;
	protected NyARLabelingLabelStack _label_list;
	protected int[] _index_table;
	protected boolean _is_index_table_enable;

	public NyARLabelingImage(int i_width, int i_height)
	{
		this._ref_buf =new int[i_height][i_width];
		this._size.w = i_width;
		this._size.h = i_height;
		this._label_list = new NyARLabelingLabelStack(MAX_LABELS);
		this._index_table=new int[MAX_LABELS];
		this._is_index_table_enable=false;
		this._buffer_reader=new NyARBufferReader(this._ref_buf,INyARBufferReader.BUFFERFORMAT_INT2D);
		
		return;
	}
	public INyARBufferReader getBufferReader()
	{
		return this._buffer_reader;
	}


	/**
	 * ラベリング結果がインデックステーブルを持つ場合、その配列を返します。
	 * 持たない場合、nullを返します。
	 * 
	 * 値がnullの時はラベル番号そのものがラスタに格納されていますが、
	 * null以外の時はラスタに格納されているのはインデクス番号です。
	 * 
	 * インデクス番号とラベル番号の関係は、以下の式で表されます。
	 * ラベル番号:=value[インデクス番号]
	 * 
	 */
	public int[] getIndexArray()
	{
		return this._is_index_table_enable?this._index_table:null;
	}
	
	public NyARLabelingLabelStack getLabelStack()
	{
		return this._label_list;
	}
	public void reset(boolean i_label_index_enable)
	{
		assert(i_label_index_enable==true);//非ラベルモードは未実装
		this._label_list.clear();
		this._is_index_table_enable=i_label_index_enable;
		return;
	}
	
	protected final int[] _getContour_xdir = { 0, 1, 1, 1, 0,-1,-1,-1};
	protected final int[] _getContour_ydir = {-1,-1, 0, 1, 1, 1, 0,-1};
	/**
	 * i_labelのラベルの、クリップ領域が上辺に接しているx座標を返します。
	 * @param i_index
	 * @return
	 */
	protected int getTopClipTangentX(NyARLabelingLabel i_label) throws NyARException
	{
		int w;
		int i_label_id=i_label.id;
		int[] index_table=this._index_table;
		int[] limage_j=this._ref_buf[i_label.clip_t];
		final int clip1 = i_label.clip_r;
		// p1=ShortPointer.wrap(limage,j*xsize+clip.get());//p1 =&(limage[j*xsize+clip[0]]);
		for (int i = i_label.clip_l; i <= clip1; i++) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			w = limage_j[i];
			if (w > 0 && index_table[w-1] == i_label_id){
				return i;
			}
		}
		//あれ？見つからないよ？
		throw new NyARException();
	}
	/**
	 * i_index番目のラベルの輪郭線を配列に返します。
	 * @param i_index
	 * @param i_array_size
	 * @param o_coord_x
	 * @param o_coord_y
	 * @return
	 * 輪郭線の長さを返します。
	 * @throws NyARException
	 */
	public int getContour(int i_index,int i_array_size,int o_coord_x[],int o_coord_y[]) throws NyARException
	{
		final int[] xdir = this._getContour_xdir;// static int xdir[8] = { 0,1, 1, 1, 0,-1,-1,-1};
		final int[] ydir = this._getContour_ydir;// static int ydir[8] = {-1,-1,0, 1, 1, 1, 0,-1};
		final NyARLabelingLabel label=this._label_list.getItem(i_index);		
		int i;
		//クリップ領域の上端に接しているポイントを得る。
		int sx=getTopClipTangentX(label);
		int sy=label.clip_t;

		int coord_num = 1;// marker_info2->coord_num = 1;
		o_coord_x[0] = sx;// marker_info2->x_coord[0] = sx;
		o_coord_y[0] = sy;// marker_info2->y_coord[0] = sy;
		int dir = 5;

		int[][] limage=this._ref_buf;
		int c = o_coord_x[0];
		int r = o_coord_y[0];
		for (;;) {
			dir = (dir + 5) % 8;
			for (i = 0; i < 8; i++) {
				if (limage[r + ydir[dir]][c + xdir[dir]] > 0) {// if(
					// p1[ydir[dir]*xsize+xdir[dir]] > 0 ){
					break;
				}
				dir = (dir + 1) % 8;
			}
			if (i == 8) {
				//8方向全て調べたけどラベルが無いよ？
				throw new NyARException();// return(-1);
			}
			// xcoordとycoordをc,rにも保存
			c = c + xdir[dir];// marker_info2->x_coord[marker_info2->coord_num]=marker_info2->x_coord[marker_info2->coord_num-1]
			// + xdir[dir];
			r = r + ydir[dir];// marker_info2->y_coord[marker_info2->coord_num]=marker_info2->y_coord[marker_info2->coord_num-1]+ ydir[dir];
			o_coord_x[coord_num] = c;// marker_info2->x_coord[marker_info2->coord_num]=marker_info2->x_coord[marker_info2->coord_num-1]+ xdir[dir];
			o_coord_y[coord_num] = r;// marker_info2->y_coord[marker_info2->coord_num]=marker_info2->y_coord[marker_info2->coord_num-1]+ ydir[dir];
			// 終了条件判定
			if (c == sx && r == sy){
				coord_num++;
				break;
			}
			coord_num++;
			if (coord_num == i_array_size) {// if( marker_info2.coord_num ==Config.AR_CHAIN_MAX-1 ){
				//輪郭が末端に達した
				return coord_num;
			}
		}
		return coord_num;		
		
	}
}
