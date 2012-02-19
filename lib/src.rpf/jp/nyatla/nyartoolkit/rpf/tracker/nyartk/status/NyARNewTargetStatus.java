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
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
/**
 * このクラスは、newステータスのターゲットステータスを格納します。
 * newステータスは、ラべリング結果の参照値をメンバに持ちます。
 */
public final class NyARNewTargetStatus extends NyARTargetStatus
{
	/** [readonly]ラべリング結果の参照ポインタです。*/
	public LowResolutionLabelingSamplerOut.Item current_sampleout;
	/**
	 * コンストラクタです。
	 * この関数は、所有されるプールオブジェクトが使います。ユーザは使いません。
	 * @param i_ref_pool_operator
	 * プールオブジェクトのコントロールインタフェイス
	 */	
	public NyARNewTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator) throws NyARException
	{
		super(i_ref_pool_operator);
		this.current_sampleout=null;
	}
	/**
	 * この関数は、オブジェクトの参照カウンタを1減算します。 
	 * 参照しているオブジェクトの参照カウンタ操作も、同時に行います。
	 */
	public int releaseObject()
	{
		int ret=super.releaseObject();
		if(ret==0 && this.current_sampleout!=null)
		{
			this.current_sampleout.releaseObject();
			this.current_sampleout=null;
		}
		return ret;
	}
	/**
	 * この関数は、ラべリング結果から、メンバ変数に値をセットします。
	 * @param i_src
	 * セットするラべリング結果を指定します。
	 * 関数は、このオブジェクトの参照カウンタをインクリメントします。
	 * @throws NyARException
	 */
	public void setValue(LowResolutionLabelingSamplerOut.Item i_src) throws NyARException
	{
		if(this.current_sampleout!=null){
			this.current_sampleout.releaseObject();
		}
		if(i_src!=null){
			this.current_sampleout=(LowResolutionLabelingSamplerOut.Item)i_src.referenceObject();
		}else{
			this.current_sampleout=null;
		}
	}
	
}

