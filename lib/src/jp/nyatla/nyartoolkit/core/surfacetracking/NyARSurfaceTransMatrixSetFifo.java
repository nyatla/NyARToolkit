/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.surfacetracking;



/**
 * 内部クラス。N個の{@link NyARSurfaceTransMatrixSet}オブジェクトFIFO
 * @author nyatla
 *
 */
public class NyARSurfaceTransMatrixSetFifo
{
	/**
	 * [readonly]
	 * ログの数
	 */
	public int num_of_item;
	/**
	 * [readonly]
	 * ログの配列
	 */
	public NyARSurfaceTransMatrixSet[] items;
	public NyARSurfaceTransMatrixSetFifo(int i_number_of_log)
	{
		this.num_of_item=0;
		this.items=new NyARSurfaceTransMatrixSet[i_number_of_log];
		for(int i=0;i<i_number_of_log;i++){
			this.items[i]=new NyARSurfaceTransMatrixSet();
		}
	}
	/**
	 * アイテム数を0にリセットする。
	 */
	public void init()
	{
		this.num_of_item=0;
	}
	/**
	 * 新しいMatrixをFifoへ追加する。
	 * @param i_trans
	 * @return
	 * 先頭のFifo領域
	 */
	public NyARSurfaceTransMatrixSet preAdd()
	{
		int len=this.items.length;
		//巡回(last->0)
		NyARSurfaceTransMatrixSet tmp=this.items[len-1];
		for(int i=len-1;i>0;i--){
			this.items[i]=this.items[i-1];
		}
		//要素0に値を計算
		this.items[0]=tmp;
		if(this.num_of_item<len){
			this.num_of_item++;
		}
		return tmp;
	}
}