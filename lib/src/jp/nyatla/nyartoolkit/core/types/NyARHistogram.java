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
 * Copyright (C)2008-2010 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.types;


/**
 * このクラスは、ヒストグラムを格納するデータ型
 */
public class NyARHistogram
{
	/** ヒストグラムを格納する配列です。
	 * <p>注意 - 配列の長さ({@link #data})と{@link #length}の意味は異なります。
	 * ヒストグラムの解像度に、この配列の長さを使わないでください。</p>
	 */
	public final int[] data;
	/** ヒストグラムの解像度です。
	 * {@link #data}配列の0から{@link #length}-1までの要素が、アクセス可能な要素です。
	 */
	public int length;
	/**
	 * ヒストグラムの合計値です。
	 * ヒストグラム分析器は、ここにヒストグラム要素の合計値を書込みます。
	 */
	public int total_of_data;
	
	
	/**
	 * コンストラクタです。
	 * ヒストグラムの解像度を指定してインスタンスを作ります。
	 * @param i_length
	 * ヒストグラムの解像度値。通常は256を指定してください。
	 */
	public NyARHistogram(int i_length)
	{
		this.data=new int[i_length];
		this.length=i_length;
		this.total_of_data=0;
	}
	/**
	 * この関数は、ヒストグラム要素の、i_stからi_edまでの区間の、合計値を返します。
	 * @param i_st
	 * 集計開始点のインデクス
	 * @param i_ed
	 * 集計終了点のインデクス
	 * @return
	 * ヒストグラムの合計値
	 */
	public final int getTotal(int i_st,int i_ed)
	{
		assert(i_st<i_ed && i_ed<this.length);
		int result=0;
		int[] s=this.data;
		for(int i=i_st;i<=i_ed;i++){
			result+=s[i];
		}
		return result;
	}
	/**
	 * この関数は、指定したインデクス以下のヒストグラム要素を0にします。
	 *　実行結果は、{@link #total_of_data}に反映されます。
	 * @param i_pos
	 * 操作するヒストグラム要素のインデクス値。
	 */
	public void lowCut(int i_pos)
	{
		int s=0;
		for(int i=0;i<i_pos;i++){
			s+=this.data[i];
			this.data[i]=0;
		}
		this.total_of_data-=s;
	}
	/**
	 * この関数は、指定したインデクス以上のヒストグラム要素を0にします。
	 *　実行結果は、{@link #total_of_data}に反映されます。
	 * @param i_pos
	 * 操作するヒストグラム要素のインデクス値。
	 */
	public void highCut(int i_pos)
	{
		int s=0;
		for(int i=this.length-1;i>=i_pos;i--){
			s+=this.data[i];
			this.data[i]=0;
		}
		this.total_of_data-=s;
	}
	/**
	 * この関数は、ヒストグラム要素の中で最小の要素のインデクス番号を返します。
	 * @return
	 * 最小要素のインデクス番号
	 */
	public int getMinSample()
	{
		int[] data=this.data;
		int ret=this.length-1;
		int min=data[ret];
		for(int i=this.length-2;i>=0;i--)
		{
			if(data[i]<min){
				min=data[i];
				ret=i;
			}
		}
		return ret;
	}
	/**
	 * この関数は、ヒストグラム要素の中で最小の要素値を返します。
	 * @return
	 * 最小要素の値
	 */
	public int getMinData()
	{
		return this.data[this.getMinSample()];
	}
	/**
	 * この関数は、ヒストグラム要素全体の平均値を計算します。
	 * @return
	 * ヒストグラム要素の平均値
	 */
	public int getAverage()
	{
		long sum=0;
		for(int i=this.length-1;i>=0;i--)
		{
			sum+=this.data[i]*i;
		}
		return (int)(sum/this.total_of_data);
	}
	
}
