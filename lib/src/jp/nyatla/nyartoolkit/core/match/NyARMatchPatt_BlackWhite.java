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

/**
 * このクラスは、グレースケールで２パターンの一致度を計算します。
 * 評価アルゴリズムは、ARToolKitの、AR_TEMPLATE_MATCHING_BWと同様です。
 * 比較対象のデータには、{@link NyARMatchPattDeviationBlackWhiteData}クラスの物を使います。
 */
public class NyARMatchPatt_BlackWhite implements INyARMatchPatt
{
	/** 基準となるARマーカパターンを格納するオブジェクトへの参照値です。*/
	protected NyARCode _ref_code_patt;
	/**　基準パターンの*/
	protected int _pixels;
	/**
	 * コンストラクタ。
	 * 基準パターンの解像度を指定して、インスタンスを生成します。
	 * このコンストラクタで生成したインスタンスの基準パターンは、NULLになっています。
	 * 後で基準パターンを{@link #setARCode}関数で設定してください。
	 * @param i_width
	 * 基準パターンのサイズ
	 * @param i_height
	 * 基準パターンのサイズ
	 */
	public NyARMatchPatt_BlackWhite(int i_width, int i_height)
	{
		//最適化定数の計算
		this._pixels=i_height*i_width;
		return;
	}
	/**
	 * コンストラクタ。
	 * 基準パターンを元に、評価インスタンスを生成します。
	 * @param i_code_ref
	 * セットする基準パターン
	 */
	public NyARMatchPatt_BlackWhite(NyARCode i_code_ref)
	{
		//最適化定数の計算
		this._pixels=i_code_ref.getWidth()*i_code_ref.getHeight();
		//基準パターンをセット
		this._ref_code_patt=i_code_ref;
		return;
	}	
	/**
	 * 基準パターンをセットします。セットできる基準パターンは、コンストラクタに設定したサイズと同じものである必要があります。
	 * @param i_code_ref
	 * セットする基準パターンを格納したオブジェクト
	 * @throws NyARException
	 */
	public void setARCode(NyARCode i_code_ref)
	{
		this._ref_code_patt=i_code_ref;
		return;
	}
	/**
	 * この関数は、現在の基準パターンと検査パターンを比較して、類似度を計算します。
	 * @param i_patt
	 * 検査パターンを格納したオブジェクトです。このサイズは、基準パターンと一致している必要があります。
	 * @param o_result
	 * 結果を受け取るオブジェクトです。
	 * @return
	 * 検査に成功するとtrueを返します。
	 * @throws NyARException
	 */
	public boolean evaluate(NyARMatchPattDeviationBlackWhiteData i_patt,NyARMatchPattResult o_result) throws NyARException
	{
		assert this._ref_code_patt!=null;

		final int[] linput = i_patt.refData();
		int sum;
		double max = 0.0;
		int res = NyARMatchPattResult.DIRECTION_UNKNOWN;
		

		for (int j = 0; j < 4; j++) {
			//合計値初期化
			sum=0;
			final NyARMatchPattDeviationBlackWhiteData code_patt=this._ref_code_patt.getBlackWhiteData(j);
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
