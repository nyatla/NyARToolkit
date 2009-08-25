/* 
 * PROJECT: NyARToolkit
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
package jp.nyatla.nyartoolkit.dev;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.*;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabel;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.nyidmarker.MarkerPattEncoder;
import jp.nyatla.nyartoolkit.nyidmarker.PerspectivePixelReader;



/**
 * 1区間にある1個のエッジ位置を推定するクラスです。
 *
 */
class NyARSingleEdgeFinder2
{
	public static class TEdgeInfo
	{
		double point;	//検出したエッジの位置
		int sub;		//エッジの差分
	}	
	private int[] _work;
	private int _width;
	private int _height;
	public NyARSingleEdgeFinder2(int i_width,int i_height)
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

class MarkerEncoder2
{
	private final static int[] _bit_table_3={
		25,	26,	27,	28,	29,	30,	31,
		48,	9,	10,	11,	12,	13,	32,
		47,	24,	1,	2,	3,	14,	33,
		46,	23,	8,	0,	4,	15,	34,
		45,	22,	7,	6,	5,	16,	35,
		44,	21,	20,	19,	18,	17,	36,
		43,	42,	41,	40,	39,	38,	37
		};	
	private final static int[] _bit_table_2={
		9,	10,	11,	12,	13,
		24,	1,	2,	3,	14,
		23,	8,	0,	4,	15,
		22,	7,	6,	5,	16,
		21,	20,	19,	18,	17};
	private final static int[][] _bit_tables={
		_bit_table_2,_bit_table_3
	};
	/**
	 * RECT(0):[0]=(0)
	 * RECT(1):[1]=(1-8)
	 * RECT(2):[2]=(9-16),[3]=(17-24)
	 * RECT(3):[4]=(25-32),[5]=(33-40),[6]=(41-48)
	 */
	int[] _bit_table;
	int[] _bits=new int[16];
	int[] _work=new int[16];
	int _model;
	public void setBitByBitIndex(int i_index_no,int i_value)
	{
		assert i_value==0 || i_value==1;
		final int bit_no=this._bit_table[i_index_no];
		if(bit_no==0){
			this._bits[0]=i_value;
		}else{
			int bidx=(bit_no-1)/8+1;
			int sidx=(bit_no-1)%8;
			this._bits[bidx]=(this._bits[bidx]&(~(0x01<<sidx)))|(i_value<<sidx);
		}
		return;
	}
	
	public void setBit(int i_bit_no,int i_value)
	{
		assert i_value==0 || i_value==1;
		if(i_bit_no==0){
			this._bits[0]=i_value;
		}else{
			int bidx=(i_bit_no-1)/8+1;
			int sidx=(i_bit_no-1)%8;
			this._bits[bidx]=(this._bits[bidx]&(~(0x01<<sidx)))|(i_value<<sidx);
		}
		return;
	}
	public int getBit(int i_bit_no)
	{
		if(i_bit_no==0){
			return this._bits[0];
		}else{
			int bidx=(i_bit_no-1)/8+1;
			int sidx=(i_bit_no-1)%8;
			return (this._bits[bidx]>>(sidx))&(0x01);
		}
	}
	public boolean initEncoder(int i_model)
	{
		if(i_model>=4 || i_model<2){
			//Lv4以降に対応する時は、この制限を変える。
			return false;
		}
		this._bit_table=_bit_tables[i_model-2];
		this._model=i_model;
		return true;
	}
	private int getDirection()
	{
		int l,t,r,b;
		switch(this._model){
		case 2:
			//トラッキングセルを得る
			t=this._bits[2] & 0x07;
			r=(this._bits[2] & 0x70)>>4;
			b=this._bits[3] & 0x07;
			l=(this._bits[3] & 0x70)>>4;
			break;
		case 3:
		default:
			return -3;
		}
		//101であるかを確認
		if(t==0x05){
			if(r==0x05){
				return (b!=0x05 && l!=0x05)?2:-2;
			}else if(l==0x05){
				return (b!=0x05 && r!=0x05)?3:-2;
			}
		}else if(b==0x05){
			if(r==0x05){
				return (t!=0x05 && l!=0x05)?1:-2;
			}else if(l==0x05){
				return (t!=0x05 && r!=0x05)?0:-2;
			}
		}
		return -1;
	}

	public boolean encode()
	{
		int d=getDirection();
		if(d<0){
			return false;
		}
		//回転操作
		
		rotateDirection(d);
		//デコード
		
		return true;
	}
	private void rotateDirection(int i_direction)
	{
		int sl=i_direction*2;
		int sr=8-sl;

		int w1;
		//RECT1
		w1=this._bits[1];
		this._bits[1]=(w1<<sl)|(w1>>sr);
		
		//RECT2
		sl=i_direction*4;
		sr=16-sl;
		w1=this._bits[2]|(this._bits[3]<<8);
		w1=(w1<<sl)|(w1>>sr);
		this._bits[2]=w1 & 0xff;
		this._bits[3]=(w1>>8) & 0xff;

		if(this._model<2){
			return;
		}

		//RECT3
		sl=i_direction*6;
		sr=24-sl;			
		w1=this._bits[4]|(this._bits[5]<<8)|(this._bits[6]<<16);
		w1=(w1<<sl)|(w1>>sr);
		this._bits[4]=w1 & 0xff;
		this._bits[5]=(w1>>8) & 0xff;
		this._bits[6]=(w1>>16) & 0xff;
		
		if(this._model<3){
			return;
		}
		//RECT4(Lv4以降はここの制限を変える)
//		shiftLeft(this._bits,7,3,i_direction*);

		return;
	}
	public void shiftLeft(int[] i_pack,int i_start,int i_length,int i_ls)
	{
		int[] work=this._work;
		//端数シフト
		final int mod_shift=i_ls%8;
		for(int i=i_length-1;i>=1;i--){
			work[i]=(i_pack[i+i_start]<<mod_shift)|(0xff&(i_pack[i+i_start-1]>>(8-mod_shift)));
		}
		work[0]=(i_pack[i_start]<<mod_shift)|(0xff&(i_pack[i_start+i_length-1]>>(8-mod_shift)));
		//バイトシフト
		final int byte_shift=(i_ls/8)%i_length;
		for(int i=i_length-1;i>=0;i--){
			i_pack[(byte_shift+i)%i_length+i_start]=0xff & work[i];
		}
		return;
	}

	
}


/**
 * NyARColorPatt_NyIdMarkerがラスタからPerspective変換して読みだすためのクラス
 *
 */
class PerspectivePixelReader2 extends NyARPerspectiveParamGenerator_O1
{
	private double[] _cparam=new double[8];

	public PerspectivePixelReader2(int i_local_x,int i_local_y,int i_width, int i_height)
	{
		super(i_local_x,i_local_y,i_width,i_height);
		this._temp1=new int[i_width];
		this._temp2=new int[i_width];
		this._pixcel_temp=new int[i_width*3];

		return;
	}
	private int[] _temp1;
	private int[] _temp2;
	private int[] _pixcel_temp;
	private INyARRgbRaster _raster;
	public void setSourceRaster(INyARRgbRaster i_raster)
	{
		this._raster=i_raster;
		return;
	}
	public boolean setSourceSquare(NyARIntPoint2d[] i_vertex)throws NyARException
	{
		return this.getParam(i_vertex, this._cparam);
	}
	public void rectPixels(int i_lt_x,int i_lt_y,int i_width,int i_height,int[] o_pixcel)throws NyARException
	{
		final double[] cpara=this._cparam;
		final INyARRgbPixelReader reader=this._raster.getRgbPixelReader();
		final int[] ref_x=this._temp1;
		final int[] ref_y=this._temp2;
		final int[] pixcel_temp=this._pixcel_temp;
		int out_index=0;

		for(int i=0;i<i_height;i++){
			//1列分のピクセルのインデックス値を計算する。
			int cy0=1+i+i_lt_y;
			double cpy0_12=cpara[1]*cy0+cpara[2];
			double cpy0_45=cpara[4]*cy0+cpara[5];
			double cpy0_7=cpara[7]*cy0+1.0;			
			int pt=0;
			for(int i2=0;i2<i_width;i2++)
			{
				int cx0=1+i2+i_lt_x;

				double cp6_0=cpara[6]*cx0;
				double cpx0_0=cpara[0]*cx0;
				double cpx3_0=cpara[3]*cx0;
				
				final double d=cp6_0+cpy0_7;
				ref_x[pt]=(int)((cpx0_0+cpy0_12)/d);
				ref_y[pt]=(int)((cpx3_0+cpy0_45)/d);
				pt++;
			}
			//1行分のピクセルを取得(場合によっては専用アクセサを書いた方がいい)
			reader.getPixelSet(ref_x,ref_y,i_width,pixcel_temp);
			//グレースケールにしながら、line→mapへの転写
			for(int i2=0;i2<i_width;i2++){
				int index=i2*3;
				o_pixcel[out_index]=(pixcel_temp[index+0]+pixcel_temp[index+1]+pixcel_temp[index+2])/3;
				out_index++;
			}			
		}
		return;
	}


		
	
	//タイミングパターン用のパラメタ(FRQ_POINTS*FRQ_STEPが100を超えないようにすること)
	private static int FRQ_STEP=2;
	private static int FRQ_POINTS=45;
	public static int FRQ_EDGE=5;

	
	/**
	 * i_y1行目とi_y2行目を平均して、タイミングパターンの周波数を得ます。
	 * LHLを1周期として、たとえばLHLHLの場合は2を返します。LHLHやHLHL等の始端と終端のレベルが異なるパターンを
	 * 検出した場合、関数は失敗します。
	 * 
	 * @param i_y1
	 * @param i_y2
	 * @param i_th_h
	 * @param i_th_l
	 * @param o_edge_index
	 * 検出したエッジ位置(H->L,L->H)のインデクスを受け取る配列です。
	 * [FRQ_POINTS]以上の配列を指定してください。
	 * @return
	 * @throws NyARException
	 */
	public int getRowFrequency(int i_y1,int i_th_h,int i_th_l,int[] o_edge_index)throws NyARException
	{
		final double[] cpara=this._cparam;
		final INyARRgbPixelReader reader=this._raster.getRgbPixelReader();
		final int[] ref_x=this._temp1;
		final int[] ref_y=this._temp2;
		final int[] pixcel_temp=this._pixcel_temp;

		//2行分のピクセルインデックスを計算
		int cy0=1+i_y1;
		double cpy0_12=cpara[1]*cy0+cpara[2];
		double cpy0_45=cpara[4]*cy0+cpara[5];
		double cpy0_7=cpara[7]*cy0+1.0;			


		int pt=0;
		for(int i2=0;i2<FRQ_POINTS;i2++)
		{
			double d;
			final int cx0=1+i2*FRQ_STEP+FRQ_EDGE;			
			d=(cpara[6]*cx0)+cpy0_7;
			ref_x[pt]=(int)((cpara[0]*cx0+cpy0_12)/d);
			ref_y[pt]=(int)((cpara[3]*cx0+cpy0_45)/d);
			pt++;
		}
		
		//ピクセルを取得(入力画像を多様化するならここから先を調整すること)
		reader.getPixelSet(ref_x,ref_y,FRQ_POINTS,pixcel_temp);
		return getFreqInfo(pixcel_temp,i_th_h,i_th_l,o_edge_index);
	}
	public int getColFrequency(int i_x1,int i_th_h,int i_th_l,int[] o_edge_index)throws NyARException
	{
		final double[] cpara=this._cparam;
		final INyARRgbPixelReader reader=this._raster.getRgbPixelReader();
		final int[] ref_x=this._temp1;
		final int[] ref_y=this._temp2;
		final int[] pixcel_temp=this._pixcel_temp;

		final int cx0=1+i_x1;
		final double cp6_0=cpara[6]*cx0;
		final double cpx0_0=cpara[0]*cx0;
		final double cpx3_0=cpara[3]*cx0;

		int pt=0;
		for(int i2=0;i2<FRQ_POINTS;i2++)
		{
			double d;
			int cy=1+i2*FRQ_STEP+FRQ_EDGE;
			double cpy_12=cpara[1]*cy+cpara[2];
			double cpy_45=cpara[4]*cy+cpara[5];
			double cpy_7=cpara[7]*cy+1.0;	
			
			d=cp6_0+cpy_7;
			ref_x[pt]=(int)((cpx0_0+cpy_12)/d);
			ref_y[pt]=(int)((cpx3_0+cpy_45)/d);
			pt++;
			
		}		
	
		//ピクセルを取得(入力画像を多様化するならここから先を調整すること)
		reader.getPixelSet(ref_x,ref_y,FRQ_POINTS,pixcel_temp);
		return getFreqInfo(pixcel_temp,i_th_h,i_th_l,o_edge_index);
	}

	/**
	 * デバックすんだらstaticにしておｋ
	 * @param i_pixcels
	 * @param i_th_h
	 * @param i_th_l
	 * @param o_edge_index
	 * @return
	 */
	private int getFreqInfo(int[] i_pixcels,int i_th_h,int i_th_l,int[] o_edge_index)
	{
		//トークンを解析して、周波数を計算
		int i=0;
		int frq_l2h=0;
		int frq_h2l=0;
		while(i<FRQ_POINTS){
			//L->Hトークンを検出する
			while(i<FRQ_POINTS){
				final int index=i*3;
				final int pix=(i_pixcels[index+0]+i_pixcels[index+1]+i_pixcels[index+2])/3;
				if(pix>i_th_h){
					//トークン発見
					o_edge_index[frq_l2h+frq_h2l]=i;
					frq_l2h++;
					break;
				}
				i++;
			}
			i++;
			//L->Hトークンを検出する
			while(i<FRQ_POINTS){
				final int index=i*3;
				final int pix=(i_pixcels[index+0]+i_pixcels[index+1]+i_pixcels[index+2])/3;
				if(pix<=i_th_l){
					//トークン発見
					o_edge_index[frq_l2h+frq_h2l]=i;
					frq_h2l++;
					break;
				}
				i++;
			}
			i++;
		}
		return frq_l2h==frq_h2l?frq_l2h:-1;			
	}



	public boolean readDataBits(double[] i_index_x,double[] i_index_y,int i_size,int i_th,MarkerPattEncoder o_bitbuffer)throws NyARException
	{
		
		assert i_size*4<this._width;
		
		final double[] cpara=this._cparam;

		final int[] ref_x=this._temp1;
		final int[] ref_y=this._temp2;
		final INyARRgbPixelReader reader=this._raster.getRgbPixelReader();
		final int[] pixcel_temp=this._pixcel_temp;
		
		int p=0;
		for(int i=0;i<i_size;i++){
			//1列分のピクセルのインデックス値を計算する。
			double cy0=1+i_index_y[i*2+0];
			double cy1=1+i_index_y[i*2+1];			
			double cpy0_12=cpara[1]*cy0+cpara[2];
			double cpy0_45=cpara[4]*cy0+cpara[5];
			double cpy0_7=cpara[7]*cy0+1.0;
			double cpy1_12=cpara[1]*cy1+cpara[2];
			double cpy1_45=cpara[4]*cy1+cpara[5];
			double cpy1_7=cpara[7]*cy1+1.0;
			
			int pt=0;
			for(int i2=0;i2<i_size;i2++)
			{			

				double d;
				double cx0=1+i_index_x[i2*2+0];
				double cx1=1+i_index_x[i2*2+1];

				double cp6_0=cpara[6]*cx0;
				double cp6_1=cpara[6]*cx1;
				double cpx0_0=cpara[0]*cx0;
				double cpx3_0=cpara[3]*cx0;
				
				d=cp6_0+cpy0_7;
				ref_x[pt]=(int)((cpx0_0+cpy0_12)/d);
				ref_y[pt]=(int)((cpx3_0+cpy0_45)/d);
				pt++;

				d=cp6_0+cpy1_7;
				ref_x[pt]=(int)((cpx0_0+cpy1_12)/d);
				ref_y[pt]=(int)((cpx3_0+cpy1_45)/d);
				pt++;

				d=cp6_1+cpy0_7;
				ref_x[pt]=(int)((cpara[0]*cx1+cpy0_12)/d);
				ref_y[pt]=(int)((cpara[3]*cx1+cpy0_45)/d);
				pt++;

				d=cp6_1+cpy1_7;
				ref_x[pt]=(int)((cpara[0]*cx1+cpy1_12)/d);
				ref_y[pt]=(int)((cpara[3]*cx1+cpy1_45)/d);
				pt++;
			}
			//1行分のピクセルを取得(場合によっては専用アクセサを書いた方がいい)
			reader.getPixelSet(ref_x,ref_y,i_size*4,pixcel_temp);
			//グレースケールにしながら、line→mapへの転写
			for(int i2=0;i2<i_size;i2++){
				int index=i2*3*4;
				int pixel=(	pixcel_temp[index+0]+pixcel_temp[index+1]+pixcel_temp[index+2]+
							pixcel_temp[index+3]+pixcel_temp[index+4]+pixcel_temp[index+5]+
							pixcel_temp[index+6]+pixcel_temp[index+7]+pixcel_temp[index+8]+
							pixcel_temp[index+9]+pixcel_temp[index+10]+pixcel_temp[index+11])/(4*3);
				o_bitbuffer.setBitByBitIndex(p,pixel>i_th?1:0);
				p++;
			}
		}
		return true;
	}
	
	public boolean setSquare(NyARIntPoint2d[] i_vertex) throws NyARException
	{
		if (!getParam(i_vertex,this._cparam)) {
			return false;
		}
		return true;
	}
}

/**
 * 遠近法を使ったパースペクティブ補正付きのINyARColorPatt
 *
 */
public class CopyOfNyARColorPatt_NyIdMarker implements INyARColorPatt
{
	private int[] _patdata;
	private NyARBufferReader _buf_reader;
	private NyARRgbPixelReader_INT1D_GRAY_8 _pixelreader;
	private NyARIntSize _size;
	PerspectivePixelReader2 _perspective_gen;
	private static final int LOCAL_LT=1;
	/**
	 * 例えば、64
	 * @param i_width
	 * 取得画像の解像度幅
	 * @param i_height
	 * 取得画像の解像度高さ
	 * @param i_edge_percentage
	 * エッジ幅の割合(ARToolKit標準と同じなら、25)
	 */
	public CopyOfNyARColorPatt_NyIdMarker(int i_model)
	{
		//入力制限
		assert i_model==2;
		int resolution=i_model*2+1;
		this._size=new NyARIntSize(resolution,resolution);
		this._patdata = new int[resolution*resolution];
		this._buf_reader=new NyARBufferReader(this._patdata,NyARBufferReader.BUFFERFORMAT_INT1D_GLAY_8);
		this._pixelreader=new NyARRgbPixelReader_INT1D_GRAY_8(this._patdata,this._size);
		this._perspective_gen=new PerspectivePixelReader2(LOCAL_LT,LOCAL_LT,100,100);
		return;
	}	
	
	public final int getWidth()
	{
		return this._size.w;
	}
	public final int getHeight()
	{
		return this._size.h;
	}
	public final NyARIntSize getSize()
	{
		return 	this._size;
	}
	public final INyARBufferReader getBufferReader()
	{
		return this._buf_reader;
	}
	public final INyARRgbPixelReader getRgbPixelReader()
	{
		return this._pixelreader;
	}
	

	public int[] vertex_x=new int[49*4];
	public int[] vertex_y=new int[49*4];
	public int[] vertex2_x=new int[400];
	public int[] vertex2_y=new int[400];
	public int[] line1=new int[45];
	public int[] line2=new int[45];
	public int[] sv=new int[4];
	private int[] temp_pixcel=new int[144];
//	private NyARSingleEdgeFinder _edge_finder=new NyARSingleEdgeFinder(12,12);


	/**
	 * マーカパラメータを格納する構造体
	 */
	private class TMarkerParam{
		int threshold;
		int model;
		private double[] index_x=new double[25*2];
		private double[] index_y=new double[25*2];
	}
	/**
	 * 周期が等間隔か調べる。
	 * 次段半周期が、前段の80%より大きく、120%未満であるものを、等間隔周期であるとみなす。
	 * @param i_freq
	 * @param i_width
	 */
	private boolean checkFreqWidth(int[] i_freq,int i_width)
	{
		int c=i_freq[1]-i_freq[0];
		for(int i=1;i<i_width*2-1;i++){
			final int n=i_freq[i+1]-i_freq[i];
			final int v=n*100/c;
			if(v>150 || v<50){
				return false;
			}
			c=n;
		}
		return true;
	}
	private static int MIN_FREQ=3;
	private static int MAX_FREQ=10;
	
	private int getRowFreq(int i_y,int i_th_h,int i_th_l,int[] o_freq_index)throws NyARException
	{
		//3,4,5,6,7,8,9,10
		int freq_index1[]=new int[45];
		int freq_count_table[]=new int[10];
		//0,2,4,6,8,10,12,14,16,18,20の要素を持つ配列
		int freq_table[][]=new int[10][20];
		//初期化
		
		//10-20ピクセル目からタイミングパターンを検出
		for(int i=i_y;i<i_y+10;i++){
			final int freq_t=this._perspective_gen.getRowFrequency(i,i_th_h,i_th_l,freq_index1);
			//周期は3-10であること
			if(freq_t<MIN_FREQ || freq_t>MAX_FREQ){
				continue;
			}
			//周期は等間隔であること
			if(!checkFreqWidth(freq_index1,freq_t)){
				continue;
			}
			//検出カウンタを追加
			freq_count_table[freq_t]++;
			for(int i2=0;i2<freq_t*2;i2++){
				freq_table[freq_t][i2]+=freq_index1[i2];
			}
		}
		//一番成分の大きいものを得る
		int index=-1;
		int max=0;
		for(int i=0;i<MAX_FREQ;i++){
			if(max<freq_count_table[i]){
				index=i;
				max=freq_count_table[i];
			}
		}
		/*周波数インデクスを計算*/
		for(int i=0;i<index*2;i++)
		{
			o_freq_index[i]=freq_table[index][i]/max;
		}
		return index;
	}
	private int getColFreq(int i_y,int i_th_h,int i_th_l,int[] o_freq_index)throws NyARException
	{
		//3,4,5,6,7,8,9,10
		int freq_index1[]=new int[45];
		int freq_count_table[]=new int[10];
		int freq_table[][]=new int[10][20];
		//初期化
		
		//10-20ピクセル目からタイミングパターンを検出
		for(int i=i_y;i<i_y+10;i++){
			final int freq_t=this._perspective_gen.getColFrequency(i,i_th_h,i_th_l,freq_index1);
			//周期は3-10であること
			if(freq_t<MIN_FREQ || freq_t>MAX_FREQ){
				continue;
			}
			//周期は等間隔であること
			if(!checkFreqWidth(freq_index1,freq_t)){
				continue;
			}
			//検出カウンタを追加
			freq_count_table[freq_t]++;
			for(int i2=0;i2<freq_t*2;i2++){
				freq_table[freq_t][i2]+=freq_index1[i2];
			}
		}
		//一番成分の大きいものを得る
		int index=-1;
		int max=0;
		for(int i=0;i<MAX_FREQ;i++){
			if(max<freq_count_table[i]){
				index=i;
				max=freq_count_table[i];
			}
		}
		for(int i=0;i<10;i++){
			System.out.print(freq_count_table[i]+",");
		}
		System.out.println();
		if(index==-1){
			return -1;
		}
		/*周波数インデクスを計算*/
		for(int i=0;i<index*2;i++)
		{
			o_freq_index[i]=freq_table[index][i]/max;
		}
		return index;
	}
	private boolean detectMarkerParam(INyARRgbPixelReader i_reader,TMarkerParam o_param)throws NyARException
	{
		TThreshold th=new TThreshold();
		
		detectThresholdValue(i_reader,10,10,th);
		//周波数測定(最も正しい周波数を検出する)

		


		//左上,右下から、12x12(抽出範囲は10x10)の調査用矩形を抽出


		
		//周波数を測定
		int freq_index1[]=new int[45];
		int freq_index2[]=new int[45];
		int frq_t=getRowFreq(10,th.th_h,th.th_l,freq_index1);
		int frq_b=getRowFreq(80,th.th_h,th.th_l,freq_index2);
		//周波数はまとも？
		if((frq_t<0 && frq_b<0) || frq_t==frq_b){
			System.out.print("FRQ_R");
			return false;
		}
		//タイミングパターンからインデクスを作成
		int freq;
		int[] index;
		if(frq_t>frq_b){
			freq=frq_t;
			index=freq_index1;
		}else{
			freq=frq_b;
			index=freq_index2;
		}
		for(int i=0;i<freq*2-1;i++){
			o_param.index_x[i*2]=((index[i+1]-index[i])*2/5+index[i])*2+PerspectivePixelReader.FRQ_EDGE;
			o_param.index_x[i*2+1]=((index[i+1]-index[i])*3/5+index[i])*2+PerspectivePixelReader.FRQ_EDGE;
		}
		int frq_l=getColFreq(10,th.th_h,th.th_l,freq_index1);
		int frq_r=getColFreq(80,th.th_h,th.th_l,freq_index2);		
		//周波数はまとも？
		if((frq_l<0 && frq_r<0) || frq_l==frq_r){
			System.out.print("FRQ_C");
			return false;
		}
		//タイミングパターンからインデクスを作成
		if(frq_l>frq_r){
			freq=frq_l;
			index=freq_index1;
		}else{
			freq=frq_r;
			index=freq_index2;
		}
		for(int i=0;i<freq*2-1;i++){
			o_param.index_y[i*2]=((index[i+1]-index[i])*2/5+index[i])*2+PerspectivePixelReader.FRQ_EDGE;
			o_param.index_y[i*2+1]=((index[i+1]-index[i])*3/5+index[i])*2+PerspectivePixelReader.FRQ_EDGE;
		}

		
		
		//Lv4以上は無理
		if(freq>4){
			System.out.print("LVL_4");
			return false;
		}
		o_param.model=freq-1;
		o_param.threshold=th.th;
		return true;
		
	}
	private class TThreshold{
		public int th_h;
		public int th_l;
		public int th;
	}
	/**
	 * 指定した場所のピクセル値を調査して、閾値を計算して返します。
	 * @param i_reader
	 * @param i_x
	 * @param i_y
	 * @return
	 * @throws NyARException
	 */
	private void detectThresholdValue(INyARRgbPixelReader i_reader,int i_x,int i_y,TThreshold o_threshold)throws NyARException
	{
		final int[] temp_pixel=this.temp_pixcel;
		//特定エリアから画素を得る
		this._perspective_gen.rectPixels(i_x,i_y,10,10,temp_pixel);
		//ソート
		int len=100;
		int h = len *13/10;
		for(;;){
		    int swaps = 0;
		    for (int i = 0; i + h < len; i++) {
		        if (temp_pixel[i + h] > temp_pixel[i]) {
		            final int temp = temp_pixel[i + h];
		            temp_pixel[i + h] = temp_pixel[i];
		            temp_pixel[i] = temp;
		            swaps++;
		        }
		    }
		    if (h == 1) {
		        if (swaps == 0){
		        	break;
		        }
		    }else{
		        h=h*10/13;
		    }
		}
		//値の大きい方と小さい方の各4点を取得
		//4点の中間を閾値中心とする
		final int th_l=temp_pixel[99]+temp_pixel[98]+temp_pixel[97]+temp_pixel[96];
		final int th_h=temp_pixel[0]+temp_pixel[1]+temp_pixel[2]+temp_pixel[3];
		int th_sub=(th_h-th_l)/(4*5);//ヒステリシス(20%)
		int th=(th_h+th_l)/8;
		o_threshold.th=th;
		o_threshold.th_h=th+th_sub;//ヒステリシス付き閾値
		o_threshold.th_l=th-th_sub;//ヒステリシス付き閾値
		System.out.println(o_threshold.th+","+o_threshold.th_h+","+o_threshold.th_l);
		return;
	}	
	

	/**
	 * 
	 * @param image
	 * @param i_marker
	 * @return 切り出しに失敗した
	 * @throws Exception
	 */
	public boolean pickFromRaster(INyARRgbRaster image, NyARSquare i_square)throws NyARException
	{
		this._perspective_gen.setSourceRaster(image);
		i_square.imvertex[0].x=149;
		i_square.imvertex[0].y=179;
		i_square.imvertex[0].x=27;
		i_square.imvertex[0].y=96;
		i_square.imvertex[0].x=152;
		i_square.imvertex[0].y=29;
		i_square.imvertex[0].x=280;
		i_square.imvertex[0].y=72;
		
		//遠近法のパラメータを計算
		if(!this._perspective_gen.setSourceSquare(i_square.imvertex)){
			return false;
		};
		
		final INyARRgbPixelReader reader=image.getRgbPixelReader();


		TMarkerParam param=new TMarkerParam();
		MarkerPattEncoder encoder=new MarkerPattEncoder();
		//マーカパラメータを取得
		if(!detectMarkerParam(reader,param))
		{
			System.out.println("E");
			return false;
		}		
		final int resolution=(param.model*2)+1;
		//Threshold検出

		if(!encoder.initEncoder(param.model)){
			return false;
		}
		//int i_stx,int i_sty,int size_x,int size_y,int i_resolution,int i_th,
		//this._perspective_gen.readDataBits(st_x,st_y,size_x,size_y,resolution, param.threshold, encoder);
		this._perspective_gen.readDataBits(param.index_x,param.index_y,resolution, param.threshold, encoder);
		encoder.encode();

		for(int i=0;i<49*4;i++){
			this.vertex_x[i]=0;
			this.vertex_y[i]=0;
		}
		for(int i=0;i<resolution*2;i++){
			for(int i2=0;i2<resolution*2;i2++){
				this.vertex_x[i*resolution*2+i2]=(int)param.index_x[i2];
				this.vertex_y[i*resolution*2+i2]=(int)param.index_y[i];
				
			}
		}

		System.out.println(param.model);
/*
		//マップを作りなおす
		for(int i=0;i<size*size;i++){
			this._patdata[i]=encoder.getBit(_role_table[i])==0?0:255;
		}
*/			
		
		
		return true;
	}
	public static void main(String[] args)
	{
		try {
			double[] a=new double[10];
			int[] d={
				//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9
					0,0,0,0,0,0,0,1,5,1,2,0,0,0,1,2,5,2,1,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			NyARSingleEdgeFinder px=new NyARSingleEdgeFinder(20,2);
			System.out.print(px.scanEdgeLeftToRight(d,0,0,a));
			System.out.print("");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}