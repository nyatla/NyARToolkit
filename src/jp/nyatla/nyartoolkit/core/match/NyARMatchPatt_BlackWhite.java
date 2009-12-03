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
package jp.nyatla.nyartoolkit.core.match;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;

/**
 * AR_TEMPLATE_MATCHING_BWと同等のルールで マーカを評価します。
 * 
 */
public class NyARMatchPatt_BlackWhite implements INyARMatchPatt
{
	protected NyARCode _code_patt;	
	protected int _pixels;
	
	public NyARMatchPatt_BlackWhite(int i_width, int i_height)
	{
		//最適化定数の計算
		this._pixels=i_height*i_width;
		return;
	}
	public NyARMatchPatt_BlackWhite(NyARCode i_code_ref)
	{
		//最適化定数の計算
		this._pixels=i_code_ref.getWidth()*i_code_ref.getHeight();
		this._code_patt=i_code_ref;
		return;
	}	
	/**
	 * 比較対象のARCodeをセットします。
	 * @throws NyARException
	 */
	public void setARCode(NyARCode i_code_ref)
	{
		this._code_patt=i_code_ref;
		return;
	}
	/**
	 * 現在セットされているコードとパターンを比較して、結果値o_resultを更新します。
	 * 比較部分はFor文を16倍展開してあります。
	 */
	public boolean evaluate(NyARMatchPattDeviationBlackWhiteData i_patt,NyARMatchPattResult o_result) throws NyARException
	{
		assert this._code_patt!=null;

		final int[] linput = i_patt.refData();
		int sum;
		double max = 0.0;
		int res = NyARMatchPattResult.DIRECTION_UNKNOWN;
		

		for (int j = 0; j < 4; j++) {
			//合計値初期化
			sum=0;
			final NyARMatchPattDeviationBlackWhiteData code_patt=this._code_patt.getBlackWhiteData(j);
			final int[] pat_j = code_patt.refData();
			//<全画素について、比較(FORの1/16展開)/>
			int i;
			for(i=this._pixels-1;i>=0;i--){
				sum += linput[i] * pat_j[i];
			}
			//0.7776737688877927がでればOK
			final double sum2 = sum / code_patt.getPow() / i_patt.getPow();// sum2 = sum / patpow[k][j]/ datapow;
			if (sum2 > max) {
				max = sum2;
				res = j;
			}
		}
		o_result.direction = res;
		o_result.confidence= max;
		return true;
	}
}
