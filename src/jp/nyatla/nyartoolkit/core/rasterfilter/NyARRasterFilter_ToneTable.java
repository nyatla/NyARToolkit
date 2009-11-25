/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * 色調テーブルを使用したフィルターです。
 * 基本的な関数テーブルで色調テーブルを作成できます。
 */
public class NyARRasterFilter_ToneTable extends NyARRasterFilter_CustomToneTable
{
	public NyARRasterFilter_ToneTable(int i_raster_type) throws NyARException
	{
		super(i_raster_type);
	}
	/**
	 * 点x,yを通過する、傾きi_aの直線をテーブルに書き込みます。
	 * @param i_x
	 * @param i_y
	 * @param i_a
	 */
	public void setLine(int i_x,int i_y,double i_a)
	{
		if(i_a==0){
			int i;
			for(i=0;i<=i_x;i++){
				this.table[i]=0;
			}
			for(i=0;i<256;i++){
				this.table[i]=255;
			}
		}else{
			int b=i_y-(int)(i_a*i_x);
			for(int i=0;i<256;i++){
				int v=(int)(i_a*i)+b;
				this.table[i]=v<0?0:v>255?255:v;
			}
		}
	}
	/**
	 * 点0,0を通過する、傾きaの直線をテーブルに書き込みます。
	 * i_aの値をvとしたとき、以下のようになります。
	 * v<=0		黒色
	 * 0<v<1	暗くする。
	 * v=0		変化しない
	 * 1<v<255	明るくする。
	 * 255<=v	白色
	 * @param i_ax
	 * @param i_ay
	 */
	public void setLine(double i_a)
	{
		setLine(0,0,i_a);
	}
	/**
	 * 点 i_x,i_yを中心とする、ゲインi_gainのシグモイド関数をテーブルに書き込みます。
	 * @param i_x
	 * @param i_y
	 * @param i_gain
	 */
	public void setSigmoid(int i_x,int i_y,double i_gain)
	{
		for(int i=0;i<256;i++){
			int v=255*(int)(1/(1+Math.exp(i_gain*(i-i_x)))-0.5)+i_y;
			this.table[i]=v<0?0:v>255?255:v;
		}
	}
	/**
	 * ガンマ補正値をテーブルに書き込みます。
	 * @param i_gamma
	 */
	public void setGamma(double i_gamma)
	{
		for(int i=0;i<256;i++){
			this.table[i]=(int)(Math.pow((double)i/255.0,i_gamma)*255.0);
		}
	}
}
