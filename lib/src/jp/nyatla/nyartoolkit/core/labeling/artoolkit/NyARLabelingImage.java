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
package jp.nyatla.nyartoolkit.core.labeling.artoolkit;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 *
 */
public class NyARLabelingImage extends NyARRaster_BasicClass
{
	private final static int MAX_LABELS = 1024*32;

	protected int[] _ref_buf;
	protected NyARLabelingLabelStack _label_list;
	protected int[] _index_table;
	protected boolean _is_index_table_enable;
	public NyARLabelingImage(int i_width, int i_height) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.INT1D);
		this._ref_buf =new int[i_height*i_width];
		this._label_list = new NyARLabelingLabelStack(MAX_LABELS);
		this._index_table=new int[MAX_LABELS];
		this._is_index_table_enable=false;
		//生成時に枠を書きます。
		drawFrameEdge();
		return;
	}
	public Object getBuffer()
	{
		return this._ref_buf;
	}
	public boolean hasBuffer()
	{
		return this._ref_buf!=null;
	}
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		NyARException.notImplement();
	}	
	/**
	 * エッジを書きます。
	 */
	public void drawFrameEdge()
	{
		int w=this._size.w;
		int h=this._size.h;
		// NyLabelingImageのイメージ初期化(枠書き)
		int[] img = (int[]) this._ref_buf;
		int bottom_ptr = (h - 1) * w;
		for (int i = 0; i < w; i++) {
			img[i] = 0;
			img[bottom_ptr + i] = 0;
		}
		for (int i = 0; i < h; i++) {
			img[i * w] = 0;
			img[(i + 1) * w - 1] = 0;
		}
		return;
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
	/**
	 * i_labelのラベルの、クリップ領域が上辺に接しているx座標を返します。
	 * @param i_index
	 * @return
	 */
	public int getTopClipTangentX(NyARLabelingLabel i_label) throws NyARException
	{
		int pix;
		int i_label_id=i_label.id;
		int[] index_table=this._index_table;
		int[] limage=this._ref_buf;
		int limage_ptr=i_label.clip_t*this._size.w;
		final int clip1 = i_label.clip_r;
		// p1=ShortPointer.wrap(limage,j*xsize+clip.get());//p1 =&(limage[j*xsize+clip[0]]);
		for (int i = i_label.clip_l; i <= clip1; i++) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			pix = limage[limage_ptr+i];
			if (pix > 0 && index_table[pix-1] == i_label_id){
				return i;
			}
		}
		//あれ？見つからないよ？
		throw new NyARException();
	}	
}
