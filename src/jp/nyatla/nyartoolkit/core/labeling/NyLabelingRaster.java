package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.*;
/**
 * 
 * @author nyatla
 *	モノクロ256段階ラスタデータ保持クラス
 */
public class NyLabelingRaster
{
    private INyARRaster _related_raster;
    private int[][] _pixcels;
    private int[]   _average;
    private int _width;
    private int _height;
    
    /**
     * 現状の設定でラスタの保持データを初期化する。
     */
    private void initRaster()
    {
	//ワーク領域を確保
	if(this._pixcels==null)
	{
	    
	}
	//値を初期化(_averageが)
	
	
    }
    public NyLabelingRaster(int i_width,int i_height)
    {
	this._height=i_height;
	this._width=i_width;
	//指定サイズで初期化をする。
	this._pixcels=new int[i_height][];

    }
    /**
     * 連携するラスタオブジェクトを指定する。
     * @param i_raster
     */
    public void SetRelatedRaster(INyARRaster i_raster) throws NyARException
    {
	//新しいラスタをセット
	this._related_raster=i_raster;
	//ラスタサイズが一致しているかを確認する。(一致していなければ例外を発生する)
	if(i_raster.getSize().isEqualSize(this._width,this._height))
	{
	    throw new NyARException();
	}
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
