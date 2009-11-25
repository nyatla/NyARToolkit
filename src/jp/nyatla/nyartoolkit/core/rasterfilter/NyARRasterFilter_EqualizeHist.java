package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.raster.NyARRasterAnalyzer_Histgram;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.types.NyARHistgram;
/**
 * ヒストグラムを平滑化します。
 *
 */
public class NyARRasterFilter_EqualizeHist extends NyARRasterFilter_CustomToneTable
{
	private NyARRasterAnalyzer_Histgram _hist_analyzer;
	private NyARHistgram _histgram=new NyARHistgram(256);
	public NyARRasterFilter_EqualizeHist(int i_raster_type,int i_sample_interval) throws NyARException
	{
		super(i_raster_type);
		this._hist_analyzer=new NyARRasterAnalyzer_Histgram(i_raster_type,i_sample_interval);
	}
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input!=i_output);
		//ヒストグラムを得る
		NyARHistgram hist=this._histgram;
		this._hist_analyzer.analyzeRaster(i_input,hist);
		//変換テーブルを作成
		int hist_total=this._histgram.total_of_data;
		int min=hist.getMinData();
		int hist_size=this._histgram.length;
		int sum=0;
		for(int i=0;i<hist_size;i++){
			sum+=hist.data[i];
			this.table[i]=(int)((sum-min)*(hist_size-1)/((hist_total-min)));
		}
		//変換
		super.doFilter(i_input, i_output);
	}
}