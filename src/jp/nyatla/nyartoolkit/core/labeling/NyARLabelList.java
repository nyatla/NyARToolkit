package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * NyLabelの予約型動的配列
 *
 */
public class NyARLabelList
{    
    private final static int ARRAY_APPEND_STEP=128;
    private final static int ARRAY_MAX=1024*16;
    private final NyARLabel[] _labels;
    private int _allocated_size;
    private int _length;
    /**
     * 最大ARRAY_MAX個の動的割り当てバッファを準備する。
     * @param i_holder_size
     */
    public NyARLabelList()
    {
	//ポインタだけははじめに確保しておく
	this._labels=new NyARLabel[ARRAY_MAX];
	//現在アロケート中の個数は0
	this._allocated_size=0;
	this._length=0;
    }
    /**
     * i_indexで指定した番号までのバッファを準備する。
     * @param i_index
     */
    public final void reserv(int i_index) throws NyARException
    {
	//アロケート済みなら即リターン
	if(this._allocated_size>i_index){
	    return;
	}
	//要求されたインデクスは範囲外
	if(i_index>=this._labels.length){
	    throw new NyARException();
	}
	//追加アロケート範囲を計算
	int range=i_index+ARRAY_APPEND_STEP;
	if(range>=this._labels.length){
	    range=this._labels.length;
	}
	//アロケート
	for(int i=this._allocated_size;i<range;i++)
	{
	    this._labels[i]=new NyARLabel();
	}
	this._allocated_size=range;
    }
    public final NyARLabel[] getArray()
    {
	return this._labels;
    }
    /**
     * 動的配列の見かけ上の要素数を設定する。
     */
    public final void setLength(int i_length)
    {
	this._length=i_length;
	
    }
    public final int getCount()
    {
	return this._length;
    }
}
