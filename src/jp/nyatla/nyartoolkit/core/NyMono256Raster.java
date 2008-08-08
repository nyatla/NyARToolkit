package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.core.raster.*;
/**
 * 
 * @author nyatla
 *	モノクロ256段階ラスタデータ保持クラス
 */
public class NyMono256Raster
{
    private NyARRaster _related_raster;
    private int[][] _pixcels;
    private int[]   _average;
    private int _width;
    private int _height;
    public void NyMono256Raster(int i_width,int i_height)
    {
	this._pixcels=new int[i_height][];
	this._height=i_height;
	this._width=i_width;

    }
    /**
     * 連携するラスタオブジェクトを指定する。
     * @param i_raster
     */
    public void SetRelatedRaster(NyARRaster i_raster)
    {
	//新しいラスタをセット
	this._related_raster=i_raster;
	//キャッシュしている情報を無効化
	this._pixcels=new int[this._height][];
	this._average=new int[this._height];
    }
    
    public int[] GetLine(int i_line_no)
    {
	int[] line=this._pixcels[i_line_no];
	if(line==null){
	    //ラインを作る。
	    line=new int[this._width];
	    this._related_raster.getPixelTotalRowLine(i_line_no,line);
	    this._pixcels[i_line_no]=line;
	    //平均値の計算
	    int ave=0;
	    for(int i=this._width-1;i>=0;i++){
		ave+=line[i];
	    }
	    this._average[i_line_no]=ave/this._width;
	}
	return line;
    }
    public int GetLineAverage(int i_line_no)
    {
	if(this._pixcels[i_line_no]==null){
	    this.GetLine(i_line_no);
	}
	return this._average[i_line_no];
    }
    
}
