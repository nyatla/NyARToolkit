/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.marker.artk.match;


import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.artk.algo.NyARMatchPatt_BlackWhite;
import jp.nyatla.nyartoolkit.core.marker.artk.algo.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.*;


/**
 * このクラスは、RGBカラーの差分画像を格納します。
 * 差分画像は、p[i]=((255-画素[i])-画像全体の平均値)のピクセルで構成されている、平均値との差分値です。
 * {@link NyARMatchPatt_BlackWhite}の入力値と使います。
 * <p>使い方 - 
 * {@link #setRaster}関数で、差分画像を作成し、プロパティ取得関数でその情報を得ます。
 * </p>
 */
public class NyARMatchPattDeviationColorData
{
	/**
	 * Rasterからデータを生成するインタフェイス。
	 */
	public interface IRasterDriver
	{
		/**
		 * この関数は、参照するラスタの差分画像データを取得する。
		 * @param o_out
		 * 差分画像データ
		 * @return
		 * pow値
		 */
		public double makeColorData(int[] o_out);
	}
	public static class RasterDriverFactory
	{
		public static IRasterDriver createDriver(INyARRgbRaster i_raster)
		{
			switch(i_raster.getBufferType())
			{
			case NyARBufferType.INT1D_X8R8G8B8_32:
				return new NyARMatchPattDeviationDataDriver_INT1D_X8R8G8B8_32(i_raster);
			default:
				break;
			}
			return new NyARMatchPattDeviationDataDriver_RGBAny(i_raster);
		}
	}
	private int[] _data;
	private double _pow;
	private NyARIntSize _size;
	/**
	 * この関数は、画素データを格納した配列を返します。
	 * {@link NyARMatchPatt_Color_WITHOUT_PCA#evaluate}関数等から使います。
	 * [R0,G0,B0],[R1,G1,B1]の順番で、直列にデータを格納します。
	 */	
	public int[] getData()
	{
		return this._data;
	}
	/**
	 * この関数は、i_bufに画素データをコピーして返します。
	 * @param i_buf
	 * 複製先の領域。パターンの幅*高さ*3の領域を用意すること。
	 * @return
	 */
	public int[] getData(int[] i_buf)
	{
		System.arraycopy(this._data,0,i_buf,0,this._data.length);
		return i_buf;
	}
	/**
	 * この関数は、差分画像の強度値を返します。
	 * 強度値は、差分画像の画素を二乗した値の合計です。
	 * @return
	 * 0&lt;nの強度値。
	 */	
	public double getPow()
	{
		return this._pow;
	}
	/**
	 * コンストラクタです。
	 * 差分画像のサイズを指定して、インスタンスを生成します。
	 * @param i_width
	 * 差分画像のサイズ
	 * @param i_height
	 * 差分画像のサイズ
	 */	                  
	public NyARMatchPattDeviationColorData(int i_width,int i_height)
	{
		this._size=new NyARIntSize(i_width,i_height);
		this._data=new int[this._size.w*this._size.h*3];
		return;
	}

	private INyARRaster _last_input_raster=null;
	private IRasterDriver _last_drv;
	/**
	 * この関数は、ラスタから差分画像を生成して、インスタンスに格納します。
	 * @param i_raster
	 * 差分画像の元画像。サイズは、このインスタンスと同じである必要があります。
	 * {@link NyARBufferType#INT1D_X8R8G8B8_32}形式のバッファを持つラスタの場合、他の形式よりも
	 * 何倍か高速に動作します。
	 */
	public void setRaster(INyARRgbRaster i_raster)
	{
		//ドライバの生成
		if(this._last_input_raster!=i_raster){
			this._last_drv=(IRasterDriver) i_raster.createInterface(IRasterDriver.class);
			this._last_input_raster=i_raster;
		}
		this._pow=this._last_drv.makeColorData(this._data);
		return;
	}
	/**
	 * この関数は、元画像を回転してから、差分画像を生成して、格納します。
	 * 制限として、この関数はあまり高速ではありません。連続使用するときは、最適化を検討してください。
	 * @param i_raster
	 * 差分画像の元画像。サイズは、このインスタンスと同じである必要があります。
	 * @param i_direction
	 * 右上の位置です。0=1象限、1=2象限、、2=3象限、、3=4象限の位置に対応します。
	 * @throws NyARRuntimeException
	 */
	public final void setRaster(INyARRgbRaster i_raster,int i_direction)
	{
		int width=this._size.w;
		int height=this._size.h;
		int i_number_of_pix=width*height;
		int[] rgb=new int[3];
		int[] dout=this._data;
		int ave;//<PV/>
		//<平均値計算>
		ave = 0;
		for(int y=height-1;y>=0;y--){
			for(int x=width-1;x>=0;x--){
				i_raster.getPixel(x,y,rgb);
				ave += rgb[0]+rgb[1]+rgb[2];
			}
		}
		//<平均値計算>
		ave=i_number_of_pix*255*3-ave;
		ave =255-(ave/ (i_number_of_pix * 3));//(255-R)-ave を分解するための事前計算

		int sum = 0,w_sum;
		int input_ptr=i_number_of_pix*3-1;
		switch(i_direction)
		{
		case 0:
			for(int y=height-1;y>=0;y--){
				for(int x=width-1;x>=0;x--){
					i_raster.getPixel(x,y,rgb);
					w_sum = (ave - rgb[2]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
					w_sum = (ave - rgb[1]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
					w_sum = (ave - rgb[0]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
				}
			}
			break;
		case 1:
			for(int x=0;x<width;x++){
				for(int y=height-1;y>=0;y--){
					i_raster.getPixel(x,y,rgb);
					w_sum = (ave - rgb[2]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
					w_sum = (ave - rgb[1]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
					w_sum = (ave - rgb[0]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
				}
			}
			break;
		case 2:
			for(int y=0;y<height;y++){
				for(int x=0;x<width;x++){
					i_raster.getPixel(x,y,rgb);
					w_sum = (ave - rgb[2]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
					w_sum = (ave - rgb[1]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
					w_sum = (ave - rgb[0]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
				}
			}
			break;
		case 3:
			for(int x=width-1;x>=0;x--){
				for(int y=0;y<height;y++){
					i_raster.getPixel(x,y,rgb);
					w_sum = (ave - rgb[2]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
					w_sum = (ave - rgb[1]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
					w_sum = (ave - rgb[0]) ;dout[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
				}
			}
			break;
			
		}
		//<差分値計算>
		//<差分値計算(FORの1/8展開)/>
		final double p=Math.sqrt((double) sum);
		this._pow=(p!=0.0?p:0.0000001);
	}
}


//
//	画像ドライバ
//

class NyARMatchPattDeviationDataDriver_INT1D_X8R8G8B8_32 implements NyARMatchPattDeviationColorData.IRasterDriver
{
	private INyARRgbRaster _ref_raster;
	public NyARMatchPattDeviationDataDriver_INT1D_X8R8G8B8_32(INyARRgbRaster i_raster)
	{
		this._ref_raster=i_raster;
	}
	public double makeColorData(int[] o_out)
	{
		//i_buffer[XRGB]→差分[R,G,B]変換			
		int i;
		int rgb;//<PV/>
		//<平均値計算(FORの1/8展開)>
		int ave;//<PV/>
		int[] buf=(int[])(this._ref_raster.getBuffer());
		NyARIntSize size=this._ref_raster.getSize();
		int number_of_pix=size.w*size.h;
		int optimize_mod=number_of_pix-(number_of_pix%8);
		ave=0;
		for(i=number_of_pix-1;i>=optimize_mod;i--){
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);
		}
		for (;i>=0;) {
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
			rgb = buf[i];ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);i--;
		}
		//<平均値計算(FORの1/8展開)/>
		ave=number_of_pix*255*3-ave;
		ave =255-(ave/ (number_of_pix * 3));//(255-R)-ave を分解するための事前計算

		int sum = 0,w_sum;
		int input_ptr=number_of_pix*3-1;
		//<差分値計算(FORの1/8展開)>
		for (i = number_of_pix-1; i >=optimize_mod;i--) {
			rgb = buf[i];
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
		}
		for (; i >=0;) {
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			rgb = buf[i];i--;
			w_sum = (ave - (rgb & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
			w_sum = (ave - ((rgb >> 8) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
			w_sum = (ave - ((rgb >> 16) & 0xff)) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
		}
		//<差分値計算(FORの1/8展開)/>
		final double p=Math.sqrt((double) sum);
		return p!=0.0?p:0.0000001;
	}
}
class NyARMatchPattDeviationDataDriver_RGBAny implements NyARMatchPattDeviationColorData.IRasterDriver
{
	private INyARRgbRaster _ref_raster;
	public NyARMatchPattDeviationDataDriver_RGBAny(INyARRgbRaster i_raster)
	{
		this._ref_raster=i_raster;
	}
	private int[] __rgb=new int[3];
	public double makeColorData(int[] o_out)
	{
		NyARIntSize size=this._ref_raster.getSize();
		INyARRgbRaster pixdev=this._ref_raster;
		int[] rgb=this.__rgb;
		int width=size.w;
		//<平均値計算>
		int ave=0;//<PV/>
		for(int y=size.h-1;y>=0;y--){
			for(int x=width-1;x>=0;x--){
				pixdev.getPixel(x,y,rgb);
				ave += rgb[0]+rgb[1]+rgb[2];
			}
		}
		//<平均値計算>
		int number_of_pix=size.w*size.h;
		ave=number_of_pix*255*3-ave;
		ave =255-(ave/ (number_of_pix * 3));//(255-R)-ave を分解するための事前計算

		int sum = 0,w_sum;
		int input_ptr=number_of_pix*3-1;
		//<差分値計算>
		for(int y=size.h-1;y>=0;y--){
			for(int x=width-1;x>=0;x--){
				pixdev.getPixel(x,y,rgb);
				w_sum = (ave - rgb[2]) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//B
				w_sum = (ave - rgb[1]) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//G
				w_sum = (ave - rgb[0]) ;o_out[input_ptr--] = w_sum;sum += w_sum * w_sum;//R
			}
		}
		//<差分値計算(FORの1/8展開)/>
		final double p=Math.sqrt((double) sum);
		return p!=0.0?p:0.0000001;
		
	}
}
