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


import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;


/**
 * このクラスは、グレースケールの差分画像を格納します。
 * 差分画像は、p[i]=((255-画素[i])-画像全体の平均値)のピクセルで構成されている、平均値との差分値です。
 * {@link NyARMatchPatt_BlackWhite}の入力値と使います。
 * <p>使い方 - 
 * {@link #setRaster}関数で、差分画像を作成し、プロパティ取得関数でその情報を得ます。
 * </p>
 */
public class NyARMatchPattDeviationBlackWhiteData
{
	private int[] _data;
	private double _pow;
	//
	private int _number_of_pixels;
	/**
	 * この関数は、画素データを格納した配列を返します。
	 * {@link NyARMatchPatt_BlackWhite#evaluate}関数から使います。
	 */
	public int[] refData()
	{
		return this._data;
	}
	/**
	 * この関数は、差分画像の強度値を返します。
	 * 強度値は、差分画像の画素を二乗した値の合計です。
	 * @return
	 * 0&lt;nの強度値。
	 */
	public double getPow()
	{
		return this._pow;
	}
	/**
	 * コンストラクタです。
	 * 差分画像のサイズを指定して、インスタンスを生成します。
	 * @param i_width
	 * 差分画像のサイズ
	 * @param i_height
	 * 差分画像のサイズ
	 */
	public NyARMatchPattDeviationBlackWhiteData(int i_width,int i_height)
	{
		this._number_of_pixels=i_height*i_width;
		this._data=new int[this._number_of_pixels];
		return;
	}
	/**
	 * この関数は、ラスタから差分画像を生成して、格納します。
	 * 制限事項として、{@link NyARBufferType#INT1D_X8R8G8B8_32}形式のラスタのみ、入力できます。
	 * @param i_raster
	 * 差分画像の元画像。サイズは、このインスタンスと同じである必要があります。
	 */
	public void setRaster(INyARRaster i_raster)
	{
		//i_buffer[XRGB]→差分[BW]変換			
		int i;
		int ave;//<PV/>
		int rgb;//<PV/>
		final int[] linput=this._data;//<PV/>
		final int[] buf=(int[])i_raster.getBuffer();

		// input配列のサイズとwhも更新// input=new int[height][width][3];
		final int number_of_pixels=this._number_of_pixels;

		//<平均値計算(FORの1/8展開)/>
		ave = 0;
		for(i=number_of_pixels-1;i>=0;i--){
			rgb = buf[i];
			ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);
		}
		ave=(number_of_pixels*255*3-ave)/(3*number_of_pixels);
		//
		int sum = 0,w_sum;
		
		//<差分値計算/>
		for (i = number_of_pixels-1; i >= 0;i--) {
			rgb = buf[i];
			w_sum =((255*3-(rgb & 0xff)-((rgb >> 8) & 0xff)-((rgb >> 16) & 0xff))/3)-ave;
			linput[i] = w_sum;
			sum += w_sum * w_sum;
		}
		final double p=Math.sqrt((double) sum);
		this._pow=p!=0.0?p:0.0000001;
		return;
	}
}
