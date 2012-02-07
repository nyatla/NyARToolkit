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
package jp.nyatla.nyartoolkit.core.rasterfilter.gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARHistogramFromRaster;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;

/**
 * このクラスは、ヒストグラムの平滑化フィルタです。
 */
public interface INyARGsEqualizeHistFilter
{
	public void doFilter(int i_hist_interval,INyARGrayscaleRaster i_output) throws NyARException;
}




class NyARGsEqualizeHistFilter_Any implements INyARGsEqualizeHistFilter
{
	private INyARGsCustomToneTableFilter _tone_table;
	private INyARHistogramFromRaster _histdrv;
	private NyARHistogram _histogram=new NyARHistogram(256);
	private int[] _hist=new int[256];

	public NyARGsEqualizeHistFilter_Any(INyARGrayscaleRaster i_raster) throws NyARException
	{
		this._tone_table=NyARGsFilterFactory.createCustomToneTableFilter(i_raster);
		this._histdrv=(INyARHistogramFromRaster) i_raster.createInterface(INyARHistogramFromRaster.class);
	}
	public void doFilter(int i_hist_interval,INyARGrayscaleRaster i_output) throws NyARException
	{
		//ヒストグラムを得る
		NyARHistogram hist=this._histogram;
		this._histdrv.createHistogram(i_hist_interval, hist);
		//変換テーブルを作成
		int hist_total=this._histogram.total_of_data;
		int min=hist.getMinData();
		int hist_size=this._histogram.length;
		int sum=0;
		for(int i=0;i<hist_size;i++){
			sum+=hist.data[i];
			this._hist[i]=(int)((sum-min)*(hist_size-1)/((hist_total-min)));
		}
		//変換
		this._tone_table.doFilter(this._hist,i_output);
		return;
	}
}