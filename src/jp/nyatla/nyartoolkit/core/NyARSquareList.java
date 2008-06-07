package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;

public class NyARSquareList extends NyARMarkerList
{
    private final NyARSquare[] square_array;
    private int square_array_num;
    public NyARSquareList(int i_number_of_holder)
    {
	super(new NyARSquare[i_number_of_holder]);
	//マーカーホルダに実体を割り当てる。
	for(int i=0;i<this.marker_holder.length;i++){
	    this.marker_holder[i]=new NyARSquare();
	}
	this.square_array=new NyARSquare[i_number_of_holder];
	this.square_array_num=0;
    }
    /**
     * マーカーアレイをフィルタして、square_arrayを更新する。
     * [[この関数はマーカー検出処理と密接に関係する関数です。
     * NyARDetectSquareクラス以外から呼び出さないで下さい。]]
     */
    public final void updateSquareArray(NyARParam i_param) throws NyARException
    {
	NyARSquare square;
	int j=0;
	for (int i = 0; i <this.marker_array_num; i++){
//	    double[][]  line	=new double[4][3];
//	    double[][]  vertex	=new double[4][2];
	    //NyARMarker marker=detect.getMarker(i);
	    square=(NyARSquare)this.marker_array[i];
	    //・・・線の検出？？
            if (!square.getLine(i_param))
            {
            	continue;
            }
            this.square_array[j]=square;
//ここで計算するのは良くないと思うんだ	
//		marker_infoL[j].id  = id.get();
//		marker_infoL[j].dir = dir.get();
//		marker_infoL[j].cf  = cf.get();	
            j++;
	}
	this.square_array_num=j;
    }
    /**
     * スクエア配列に格納されている要素数を返します。
     * @return
     */
    public final int getSquareNum()
    {
	return 	this.square_array_num;
    }
    /**
     * スクエア配列の要素を返します。
     * スクエア配列はマーカーアレイをさらにフィルタした結果です。
     * マーカーアレイの部分集合になっている点に注意してください。
     * @param i_index
     * @return
     * @throws NyARException
     */
    public final NyARSquare getSquare(int i_index) throws NyARException
    {
	if(i_index>=this.square_array_num){
	    throw new NyARException();
	}
	return this.square_array[i_index];
    }
}
