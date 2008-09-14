/* 
 * PROJECT: NyARToolkit JOGL utilities.
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
package jp.nyatla.nyartoolkit.jogl.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.detector.NyARDetectMarker;

/**
 * NyARSingleDetectMarkerにOpenGL向け関数を追加したもの
 */
public class GLNyARDetectMarker extends NyARDetectMarker
{
	private NyARTransMatResult trans_mat_result = new NyARTransMatResult();

	private double view_scale_factor = 0.025;

	public GLNyARDetectMarker(NyARParam i_param, NyARCode[] i_code, double[] i_marker_width, int i_number_of_code) throws NyARException
	{
		super(i_param, i_code, i_marker_width, i_number_of_code);
	}

	public void setScaleFactor(double i_new_value)
	{
		view_scale_factor = i_new_value;
	}

	/**
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @param o_result
	 * 結果値を格納する配列を指定してください。double[16]以上が必要です。
	 * @throws NyARException
	 */
	public void getCameraViewRH(int i_index, double[] o_result) throws NyARException
	{
		// 座標を計算
		this.getTransmationMatrix(i_index, this.trans_mat_result);
		// 行列変換
		final NyARTransMatResult mat = this.trans_mat_result;
		o_result[0 + 0 * 4] = mat.m00; 
		o_result[0 + 1 * 4] = mat.m01;
		o_result[0 + 2 * 4] = mat.m02;
		o_result[0 + 3 * 4] = mat.m03;
		o_result[1 + 0 * 4] = -mat.m10;
		o_result[1 + 1 * 4] = -mat.m11;
		o_result[1 + 2 * 4] = -mat.m12;
		o_result[1 + 3 * 4] = -mat.m13;
		o_result[2 + 0 * 4] = -mat.m20;
		o_result[2 + 1 * 4] = -mat.m21;
		o_result[2 + 2 * 4] = -mat.m22;
		o_result[2 + 3 * 4] = -mat.m23;
		o_result[3 + 0 * 4] = 0.0;
		o_result[3 + 1 * 4] = 0.0;
		o_result[3 + 2 * 4] = 0.0;
		o_result[3 + 3 * 4] = 1.0;
		if (view_scale_factor != 0.0) {
			o_result[12] *= view_scale_factor;
			o_result[13] *= view_scale_factor;
			o_result[14] *= view_scale_factor;
		}
		return;
	}
}
