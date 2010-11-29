/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

class NyARCodeFileReader
{

	/**
	 * ARコードファイルからデータを読み込んでo_raster[4]に格納します。
	 * @param i_stream
	 * @param o_raster
	 * @throws NyARException
	 */
	public static void loadFromARToolKitFormFile(InputStream i_stream,NyARRaster[] o_raster) throws NyARException
	{
		assert o_raster.length==4;
		//4個の要素をラスタにセットする。
		try {
			StreamTokenizer st = new StreamTokenizer(new InputStreamReader(i_stream));
			//GBRAで一度読みだす。
			for (int h = 0; h < 4; h++) {
				assert o_raster[h].isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32);
				final NyARRaster ra=o_raster[h];
				readBlock(st,ra.getWidth(),ra.getHeight(),(int[])ra.getBuffer());
			}
		} catch (Exception e) {
			throw new NyARException(e);
		}
		return;
	}
	/**
	 * ARコードファイルからデータを読み込んでo_codeに格納します。
	 * @param i_stream
	 * @param o_code
	 * @throws NyARException
	 */
	public static void loadFromARToolKitFormFile(InputStream i_stream,NyARCode o_code) throws NyARException
	{
		int width=o_code.getWidth();
		int height=o_code.getHeight();
		NyARRaster tmp_raster=new NyARRaster(width,height, NyARBufferType.INT1D_X8R8G8B8_32);
		//4個の要素をラスタにセットする。
		try {
			StreamTokenizer st = new StreamTokenizer(new InputStreamReader(i_stream));
			int[] buf=(int[])tmp_raster.getBuffer();
			//GBRAで一度読みだす。
			for (int h = 0; h < 4; h++){
				readBlock(st,width,height,buf);
				//ARCodeにセット(カラー)
				o_code.getColorData(h).setRaster(tmp_raster);
				o_code.getBlackWhiteData(h).setRaster(tmp_raster);
			}
		} catch (Exception e) {
			throw new NyARException(e);
		}
		tmp_raster=null;//ポイ
		return;
	}
	/**
	 * 1ブロック分のXRGBデータをi_stからo_bufへ読みだします。
	 * @param i_st
	 * @param o_buf
	 */
	private static void readBlock(StreamTokenizer i_st,int i_width,int i_height,int[] o_buf) throws NyARException
	{
		try {
			final int pixels=i_width*i_height;
			for (int i3 = 0; i3 < 3; i3++) {
				for (int i2 = 0; i2 < pixels; i2++){
					// 数値のみ読み出す
					switch (i_st.nextToken()){
					case StreamTokenizer.TT_NUMBER:
						break;
					default:
						throw new NyARException();
					}
					o_buf[i2]=(o_buf[i2]<<8)|((0x000000ff&(int)i_st.nval));
				}
			}
			//GBR→RGB
			for(int i3=0;i3<pixels;i3++){
				o_buf[i3]=((o_buf[i3]<<16)&0xff0000)|(o_buf[i3]&0x00ff00)|((o_buf[i3]>>16)&0x0000ff);
			}
		} catch (Exception e) {
			throw new NyARException(e);
		}		
		return;
	}
}

/**
 * ARToolKitのマーカーコードを1個保持します。
 * 
 */
public class NyARCode
{
	private NyARMatchPattDeviationColorData[] _color_pat=new NyARMatchPattDeviationColorData[4];
	private NyARMatchPattDeviationBlackWhiteData[] _bw_pat=new NyARMatchPattDeviationBlackWhiteData[4];
	private int _width;
	private int _height;
	
	public NyARMatchPattDeviationColorData getColorData(int i_index)
	{
		return this._color_pat[i_index];
	}
	public NyARMatchPattDeviationBlackWhiteData getBlackWhiteData(int i_index)
	{
		return this._bw_pat[i_index];
	}	
	public int getWidth()
	{
		return _width;
	}

	public int getHeight()
	{
		return _height;
	}
	public NyARCode(int i_width, int i_height) throws NyARException
	{
		this._width = i_width;
		this._height = i_height;
		//空のラスタを4個作成
		for(int i=0;i<4;i++){
			this._color_pat[i]=new NyARMatchPattDeviationColorData(i_width,i_height);
			this._bw_pat[i]=new NyARMatchPattDeviationBlackWhiteData(i_width,i_height);
		}
		return;
	}
	public void loadARPattFromFile(String filename) throws NyARException
	{
		try {
			loadARPatt(new FileInputStream(filename));
		} catch (Exception e) {
			throw new NyARException(e);
		}
		return;
	}
	public void setRaster(NyARRaster[] i_raster) throws NyARException
	{
		assert i_raster.length!=4;
		//ラスタにパターンをロードする。
		for(int i=0;i<4;i++){
			this._color_pat[i].setRaster(i_raster[i]);				
		}
		return;
	}

	public void loadARPatt(InputStream i_stream) throws NyARException
	{
		//ラスタにパターンをロードする。
		NyARCodeFileReader.loadFromARToolKitFormFile(i_stream,this);
		return;
	}
}
