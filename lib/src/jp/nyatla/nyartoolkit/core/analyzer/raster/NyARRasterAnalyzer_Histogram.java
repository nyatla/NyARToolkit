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
package jp.nyatla.nyartoolkit.core.analyzer.raster;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、グレースケール画像のヒストグラムを計算します。
 * <p>memo: RGB画像のグレースケール化は分離が必要？</p>
 */
public class NyARRasterAnalyzer_Histogram
{
	protected ICreateHistogramImpl _histImpl;
	/**
	 * ヒストグラム解析の縦方向スキップ数。
	 * <p> memo:継承クラスはこのライン数づつ、スキップしながらヒストグラム計算を行うこと。</p>
	 */
	protected int _vertical_skip;
	
	/**
	 * コンストラクタです。
	 * ラスタの画素形式を指定して、ヒストグラム分析器を作ります。
	 * @param i_raster_format
	 * 入力するラスタの画素形式を指定します。
	 * {@link NyARBufferType#INT1D_GRAY_8}以外の画素形式の場合は、解析時にグレースケール化を行います。
	 * @param i_vertical_interval
	 * 画素スキャン時の、Y軸方向のスキップ数を指定します。数値が大きいほど高速に解析できますが、精度は劣化します。0以上を指定してください。
	 * @throws NyARException
	 */
	public NyARRasterAnalyzer_Histogram(int i_raster_format,int i_vertical_interval) throws NyARException
	{
		if(!initInstance(i_raster_format,i_vertical_interval)){
			throw new NyARException();
		}
	}
	/**
	 * インスタンスを初期化します。
	 * 継承クラスを実装するときには、この関数をオーバライドしてください。
	 * @see #NyARRasterAnalyzer_Histogram(int i_raster_format,int i_vertical_interval)
	 * @param i_raster_format
	 * see Also.
	 * @param i_vertical_interval
	 * see Also.
	 * @return
	 * インスタンスの初期化に成功するとTrueを返します。
	 */
	protected boolean initInstance(int i_raster_format,int i_vertical_interval)
	{
		switch (i_raster_format) {
		case  NyARBufferType.BYTE1D_B8G8R8_24:
		case  NyARBufferType.BYTE1D_R8G8B8_24:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_BYTE1D_RGB_24();
			break;
		case  NyARBufferType.INT1D_GRAY_8:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_INT1D_GRAY_8();
			break;
		case  NyARBufferType.BYTE1D_B8G8R8X8_32:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_BYTE1D_B8G8R8X8_32();
			break;
		case  NyARBufferType.BYTE1D_X8R8G8B8_32:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_BYTE1D_X8R8G8B8_32();
			break;
		case  NyARBufferType.WORD1D_R5G6B5_16LE:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_WORD1D_R5G6B5_16LE();
			break;
		case  NyARBufferType.INT1D_X8R8G8B8_32:
			this._histImpl = new NyARRasterThresholdAnalyzer_Histogram_INT1D_X8R8G8B8_32();
			break;
		default:
			return false;
		}
		//初期化
		this._vertical_skip=i_vertical_interval;
		return true;
	}	
	/**
	 * 画素スキャン時の、Y軸方向のスキップ数を指定します。
	 * @param i_step
	 * 新しい行スキップ数
	 */
	public void setVerticalInterval(int i_step)
	{
		assert(this._vertical_skip>0);
		this._vertical_skip=i_step;
		return;
	}

	/**
	 * ラスタから、ヒストグラムを計算します。
	 * @param i_input
	 * 解析するラスタオブジェクト
	 * @param o_histogram
	 * 解析結果を格納するヒストグラムオブジェクト。ヒストグラムの範囲は、256であること。
	 * @throws NyARException
	 */
	public void analyzeRaster(INyARRaster i_input,NyARHistogram o_histogram) throws NyARException
	{
		
		final NyARIntSize size=i_input.getSize();
		//最大画像サイズの制限
		assert(size.w*size.h<0x40000000);
		assert(o_histogram.length==256);//現在は固定

		int[] h=o_histogram.data;
		//ヒストグラム初期化
		for (int i = o_histogram.length-1; i >=0; i--){
			h[i] = 0;
		}
		o_histogram.total_of_data=size.w*size.h/this._vertical_skip;
		this._histImpl.createHistogram(i_input,0,0,size.w,size.h,o_histogram.data,this._vertical_skip);
		return;
	}
	/**
	 * ラスタの一部分（矩形）から、ヒストグラムを計算します。
	 * @param i_input
	 * 解析するラスタオブジェクト
	 * @param i_area
	 * 解析する矩形範囲。ラスタの内側である事が必要。
	 * @param o_histogram
	 * 解析結果を格納するヒストグラムオブジェクト。ヒストグラムの範囲は、256であること。
	 * @throws NyARException
	 */	
	public void analyzeRaster(INyARRaster i_input,NyARIntRect i_area,NyARHistogram o_histogram) throws NyARException
	{
		
		final NyARIntSize size=i_input.getSize();
		//最大画像サイズの制限
		assert(size.w*size.h<0x40000000);
		assert(o_histogram.length==256);//現在は固定

		int[] h=o_histogram.data;
		//ヒストグラム初期化
		for (int i = o_histogram.length-1; i >=0; i--){
			h[i] = 0;
		}
		o_histogram.total_of_data=i_area.w*i_area.h/this._vertical_skip;
		this._histImpl.createHistogram(i_input,i_area.x,i_area.y,i_area.w,i_area.h,o_histogram.data,this._vertical_skip);
		return;
	}
	
	/**
	 * このインタフェイスは、ヒストグラム解析関数を定義します。
	 * このインタフェイスを継承して、画素フォーマット毎のヒストグラム解析関数を実装します。
	 */
	protected interface ICreateHistogramImpl
	{
		/**
		 * この関数には、ラスタ内の矩形範囲からヒストグラムを作成する機能を実装します。
		 * @param i_raster
		 * 解析するラスタオブジェクト
		 * @param i_l
		 * 矩形範囲 -L
		 * @param i_t
		 * 矩形範囲 -T
		 * @param i_w
		 * 矩形範囲 -W
		 * @param i_h
		 * 矩形範囲 -H
		 * @param o_histogram
		 * ヒストグラムを格納するオブジェクト
		 * @param i_skip
		 * 行スキップ数
		 */
		public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip);
	}
	class NyARRasterThresholdAnalyzer_Histogram_INT1D_GRAY_8 implements ICreateHistogramImpl
	{
		public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
		{
			assert (i_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			final int[] input=(int[])i_raster.getBuffer();
			NyARIntSize s=i_raster.getSize();
			int skip=(i_skip*s.w-i_w);
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt=(i_t*s.w+i_l);
			for (int y = i_h-1; y >=0 ; y-=i_skip){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					o_histogram[input[pt++]]++;
				}
				for (;x>=0;x-=8){
					o_histogram[input[pt++]]++;
					o_histogram[input[pt++]]++;
					o_histogram[input[pt++]]++;
					o_histogram[input[pt++]]++;
					o_histogram[input[pt++]]++;
					o_histogram[input[pt++]]++;
					o_histogram[input[pt++]]++;
					o_histogram[input[pt++]]++;
				}
				//スキップ
				pt+=skip;
			}
			return;			

		}	
	}
	class NyARRasterThresholdAnalyzer_Histogram_INT1D_X8R8G8B8_32 implements ICreateHistogramImpl
	{
		public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
		{
			assert (i_raster.isEqualBufferType( NyARBufferType.INT1D_X8R8G8B8_32));
			final int[] input=(int[])i_raster.getBuffer();
			NyARIntSize s=i_raster.getSize();
			int skip=(i_skip*s.w-i_w);
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt=(i_t*s.w+i_l);
			for (int y = i_h-1; y >=0 ; y-=i_skip){
				int x,v;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
				}
				for (;x>=0;x-=8){
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
					v=input[pt++];o_histogram[((v& 0xff)+(v& 0xff)+(v& 0xff))/3]++;
				}
				//スキップ
				pt+=skip;
			}
			return;			
		}	
	}

	
	class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_RGB_24 implements ICreateHistogramImpl
	{
		public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
		{
			assert (
					i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24)||
					i_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24));
			final byte[] input=(byte[])i_raster.getBuffer();
			NyARIntSize s=i_raster.getSize();
			int skip=(i_skip*s.w-i_w)*3;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt=(i_t*s.w+i_l)*3;
			for (int y = i_h-1; y >=0 ; y-=i_skip){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=3;
				}
				for (;x>=0;x-=8){
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=3*8;
				}
				//スキップ
				pt+=skip;
			}
			return;	
		}
	}

	class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_B8G8R8X8_32 implements ICreateHistogramImpl
	{
		public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
		{
	        assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32));
			final byte[] input=(byte[])i_raster.getBuffer();
			NyARIntSize s=i_raster.getSize();
			int skip=(i_skip*s.w-i_w)*4;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt=(i_t*s.w+i_l)*4;
			for (int y = i_h-1; y >=0 ; y-=i_skip){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
				}
				for (;x>=0;x-=8){
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+0]& 0xff)+(input[pt+1]& 0xff)+(input[pt+2]& 0xff))/3]++;
					pt+=4;
				}
				//スキップ
				pt+=skip;
			}
			return;	
	    }
	}

	class NyARRasterThresholdAnalyzer_Histogram_BYTE1D_X8R8G8B8_32 implements ICreateHistogramImpl
	{
		public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
		{
	        assert(i_raster.isEqualBufferType(NyARBufferType.BYTE1D_X8R8G8B8_32));
			final byte[] input=(byte[])i_raster.getBuffer();
			NyARIntSize s=i_raster.getSize();
			int skip=(i_skip*s.w-i_w)*4;
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt=(i_t*s.w+i_l)*4;
			for (int y = i_h-1; y >=0 ; y-=i_skip){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+ 3]& 0xff))/3]++;
					pt+=4;
				}
				for (;x>=0;x-=8){
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
					o_histogram[((input[pt+1]& 0xff)+(input[pt+2]& 0xff)+(input[pt+3]& 0xff))/3]++;
					pt+=4;
				}
				//スキップ
				pt+=skip;
			}
			return;	
	    }
	}

	class NyARRasterThresholdAnalyzer_Histogram_WORD1D_R5G6B5_16LE implements ICreateHistogramImpl
	{
		public void createHistogram(INyARRaster i_raster,int i_l,int i_t,int i_w,int i_h, int[] o_histogram,int i_skip)
		{
	        assert(i_raster.isEqualBufferType(NyARBufferType.WORD1D_R5G6B5_16LE));
			final short[] input=(short[])i_raster.getBuffer();
			NyARIntSize s=i_raster.getSize();
			int skip=(i_skip*s.w-i_w);
			final int pix_count=i_w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			int pt=(i_t*s.w+i_l);
			for (int y = i_h-1; y >=0 ; y-=i_skip){
				int x,v;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
				}
				for (;x>=0;x-=8){
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
					v =(int)input[pt++]; o_histogram[(((v & 0xf800) >> 8) + ((v & 0x07e0) >> 3) + ((v & 0x001f) << 3))/3]++;
				}
				//スキップ
				pt+=skip;
			}
			return;	
	    }
	}
	/**
	 * デバック用関数
	 * @param args
	 * main関数引数
	 */
	public static void main(String[] args)
	{
		try{
			NyARRgbRaster raster=new NyARRgbRaster(100,100,NyARBufferType.WORD1D_R5G6B5_16LE);
			short[] buf=(short[])raster.getBuffer();
			for(int i=0;i<100;i++){
				for(int i2=0;i2<100;i2++){
					buf[(i*100+i2)+0]=(short)(3); //buf[(i*100+i2)*3+1]=buf[(i*100+i2)*3+2]=(byte)i2;
				}
			}
			NyARIntRect rect=new NyARIntRect();
			rect.x=2;rect.y=2;rect.h=10;rect.w=10;
			NyARRasterAnalyzer_Histogram ha=new NyARRasterAnalyzer_Histogram(raster.getBufferType(),1);
			NyARHistogram h=new NyARHistogram(256);
			ha.analyzeRaster(raster,rect, h);
//			ha.analyzeRaster(raster, h);
			return;
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}