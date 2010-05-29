package jp.nyatla.nyartoolkit.core.analyzer.raster.threshold;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.*;
import jp.nyatla.nyartoolkit.core.analyzer.raster.*;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;
/**
 * 明点と暗点をPタイル法で検出して、その中央値を閾値とする。
 * 
 * 
 */
public class NyARRasterThresholdAnalyzer_SlidePTile implements INyARRasterThresholdAnalyzer
{
	protected NyARRasterAnalyzer_Histogram _raster_analyzer;
	private NyARHistogramAnalyzer_SlidePTile _sptile;
	private NyARHistogram _histogram;
	public void setVerticalInterval(int i_step)
	{
		this._raster_analyzer.setVerticalInterval(i_step);
		return;
	}
	public NyARRasterThresholdAnalyzer_SlidePTile(int i_persentage,int i_raster_format,int i_vertical_interval) throws NyARException
	{
		assert (0 <= i_persentage && i_persentage <= 50);
		//初期化
		if(!initInstance(i_raster_format,i_vertical_interval)){
			throw new NyARException();
		}
		this._sptile=new NyARHistogramAnalyzer_SlidePTile(i_persentage);
		this._histogram=new NyARHistogram(256);
	}
	protected boolean initInstance(int i_raster_format,int i_vertical_interval) throws NyARException
	{
		this._raster_analyzer=new NyARRasterAnalyzer_Histogram(i_raster_format,i_vertical_interval);
		return true;
	}
	public int analyzeRaster(INyARRaster i_input) throws NyARException
	{
		this._raster_analyzer.analyzeRaster(i_input, this._histogram);
		return this._sptile.getThreshold(this._histogram);
	}
}
