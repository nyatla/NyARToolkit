package jp.nyatla.nyartoolkit.sandbox.utils;

/**
 * 1区間にある1個のエッジ位置を推定するクラスです。
 *
 */
public class NyARSingleEdgeFinder
{
	public static class TEdgeInfo
	{
		double point;	//検出したエッジの位置
		int sub;		//エッジの差分
	}	
	private int[] _work;
	private int _width;
	private int _height;
	public NyARSingleEdgeFinder(int i_width,int i_height)
	{
		this._work=new int[(i_width>i_height?i_width:i_height)+1];
		this._work[this._work.length-1]=0;
		this._width=i_width;
		this._height=i_height;
		return;
	}
	/**
	 * この関数は、一区間に１個のエッジが含まれていると仮定して、その位置を推定します。
	 * [n]個の配列を与えた場合、[n+2]～[n-1]の間にあるエッジを検出します。
	 * 検出方向は左→右の順です
	 * @param i_pixcel
	 * @param i_start
	 * @param i_length
	 * @param o_out
	 * @return
	 */
	public boolean scanSingleEdgeLeftToRight(int[] i_pixcel,int i_y,TEdgeInfo o_out)
	{
		final int[] temp=this._work;
		//1回微分(0-8)
		final int length=this._width-1;
		int p=i_y*this._width;
		for(int i2=0;i2<length;i2++){
			temp[i2]=i_pixcel[p+1]-i_pixcel[p];
			p++;
		}
		return scanSingleEdge(temp,length,o_out);
	}
	/**
	 * 線分内のエッジを検出します。
	 * この関数は、1区間に1個のエッジが含まれていると仮定して、その位置を推定します。
	 * [n]個の配列を与えた場合、[n+2]～[n-1]の間にあるエッジを検出します。
	 * 検出方向は右→左の順です
	 * @param i_pixcel
	 * @param i_start
	 * @param i_length
	 * @param o_out
	 * @return
	 */
	public boolean scanSingleEdgeRightToLeft(int[] i_pixcel,int i_y,TEdgeInfo o_out)
	{
		final int[] temp=this._work;
		//1回微分(0-8)
		final int length=this._width-1;
		int p=(i_y+1)*this._width-1;
		for(int i2=0;i2<length;i2++){
			temp[i2]=i_pixcel[p-1]-i_pixcel[p];
			p--;
		}
		return scanSingleEdge(temp,length,o_out);
	}	
	public boolean scanSingleEdgeTopToBottom(int[] i_pixcel,int i_x,TEdgeInfo o_out)
	{
		final int[] temp=this._work;
		//1回微分(0-8)
		final int step=this._width;
		final int length=this._height-1;
		int p=i_x;
		for(int i2=0;i2<length;i2++){
			temp[i2]=i_pixcel[p+step]-i_pixcel[p];
			p+=step;
		}
		return scanSingleEdge(temp,length,o_out);
	}
	public boolean scanSingleEdgeBottomToTop(int[] i_pixcel,int i_x,TEdgeInfo o_out)
	{
		final int[] temp=this._work;
		//1回微分(0-8)
		final int step=this._width;
		final int length=this._height-1;
		int p=i_x+step*length;
		for(int i2=0;i2<length;i2++){
			temp[i2]=i_pixcel[p-step]-i_pixcel[p];
			p-=step;
		}
		return scanSingleEdge(temp,length,o_out);
	}	
	private boolean scanSingleEdge(int[] i_pixels,int i_length,TEdgeInfo o_out)
	{
		//微分(2回目)して、極値２か所を得る
		int max_index,min_index;
		int length=i_length-1;
		max_index=min_index=0;
		int max_value,min_value;
		max_value=min_value=0;
		for(int i2=0;i2<length;i2++){
			int t=i_pixels[i2+1]-i_pixels[i2];
			if(t>max_value){
				max_index=i2;
				max_value=t;
			}
			if(t<min_value){
				min_index=i2;
				min_value=t;
			}
		}
		//同符号である場合、範囲内にエッジはない
		if(max_value*min_value>=0){
			return false;
		}		
		o_out.point=(max_index+min_index)/2.0;
		o_out.sub=max_value-min_value;
		return true;
	}
	public int scanEdgeLeftToRight(int[] i_pixel,int i_y,int i_noise_th,double[] o_edge_index)
	{
		final int[] temp=this._work;
		//1回微分(0-8)
		final int length=this._width-1;
		int p=i_y*this._width;
		for(int i2=0;i2<length;i2++){
			temp[i2]=i_pixel[p+1]-i_pixel[p];
			p++;
		}
		//0終端させるために1要素を後続に追加
		return scanEdge(temp,length+1,i_noise_th,o_edge_index);
	}
	private int scanEdge(int[] i_pixels,int i_length,int i_noise_th,double[] o_out)
	{
		int points=0;
		final int length=i_length;
		//エッジ1区間を抜き出す
		for(int i2=0;i2<length;i2++){
			int t=i_pixels[i2];
			if(t>i_noise_th){
				int st=i2;
				i2++;
				for(;i2<length;i2++){
					t=i_pixels[i2];
					if(t<=0){
						//(st - i2で1区間)
						//エッジ位置は区間の中央にする。
						o_out[points]=(st+i2)/2.0;
						points++;
						if(t<0){
							//マイナスであれば、0を補完する
							i2--;
							i_pixels[i2]=0;
						}
						break;
					}

				}
			}else if(t<-i_noise_th){
				int st=i2;
				i2++;
				for(;i2<length;i2++){
					t=i_pixels[i2];
					if(t>=0){
						//(st - i2で1区間)
						//エッジ位置は区間の中央にする。
						o_out[points]=(st+i2)/2.0;
						points++;
						if(t>0){
							//プラスであれば、0を補完する
							i2--;
							i_pixels[i2]=0;
						}
						break;
					}
				}
			}
		}
		return points;
	}	
	/**
	 * 指定した配列をノイズパターンとして、ノイズ値を計算します。
	 * このノイズ値は、scanEdgeのノイズ値として使用できます。
	 * @param i_pixels
	 * @param i_length
	 * @return
	 */
	public int getNoiseValue(int[] i_pixels,int i_length)
	{
		//1回微分して、その最大値と最小値を計算
		int length=i_length-1;
		int max_value,min_value;
		max_value=min_value=0;
		for(int i2=0;i2<length;i2++){
			int t=i_pixels[i2+1]-i_pixels[i2];
			if(t>max_value){
				max_value=t;
			}
			if(t<min_value){
				min_value=t;
			}
		}
		return (-min_value>max_value)?-min_value:max_value;
	}
}
