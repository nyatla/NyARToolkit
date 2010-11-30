package jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * YCbCr変換したY成分を、RGBカラーベクトルの差が少いほど強度が落ちるようにしてグレースケールに変換します。
 * sには減衰度数を設定します。
 * 強度計算は以下のように行います。
 * f(x):=exp(-x^2/s^2)  窓関数
 * p :=Y*f(cr)*f(cb)
 */
public class NyARRasterFilter_Rgb2Gs_CbCrCut implements INyARRasterFilter_Rgb2Gs
{
	private IdoFilterImpl _dofilterimpl;
	/**
	 * 1024倍した値
	 */
	private int _window[]=new int[256];
	public NyARRasterFilter_Rgb2Gs_CbCrCut(int i_raster_type,double i_sigma) throws NyARException
	{
		switch (i_raster_type) {
		case NyARBufferType.BYTE1D_B8G8R8_24:
			this._dofilterimpl=new IdoFilterImpl_BYTE1D_B8G8R8_24();
			break;
		case NyARBufferType.BYTE1D_R8G8B8_24:
		default:
			throw new NyARException();
		}
		this._dofilterimpl._window_ref=this._window;
		//windowの作成
		for(int i=0;i<256;i++){
			double p=((double)i-127.0)/127.0;
			this._window[i]=(int)(1024*Math.exp(-p*p/(i_sigma*i_sigma)));
		}
	}
	public void doFilter(INyARRgbRaster i_input, NyARGrayscaleRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._dofilterimpl.doFilter(i_input,i_output,i_input.getSize());
	}
	
	abstract class IdoFilterImpl
	{
		int[] _window_ref;
		public abstract void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException;
		
	}
	class IdoFilterImpl_BYTE1D_B8G8R8_24 extends IdoFilterImpl
	{
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
		{
			assert(	i_input.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24));
			assert(	i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			
			int r,g,b;
			int[] win=this._window_ref;

			int bp = 0;
			for (int y = 0; y < i_size.h; y++){
				for (int x = 0; x < i_size.w; x++){
					b=(in_buf[bp] & 0xff);
					g=(in_buf[bp + 1] & 0xff);
					r=(in_buf[bp + 2] & 0xff);
					bp += 3;
					int yv=(306*r+601*g+117 * b)>>10;//0<yv<255
					int cr=(((-173 * r-339 * g + 512 *b))>>10)+127;//-127.5<=0<=127.5
					int cb=((( 512 * r-429 * g -  83 *b))>>10)+127;//-127.5<=0<=127.5
					out_buf[y*i_size.w+x]=(yv*(int)win[cr]*win[cb])>>20;
				}
			}
			return;
		}
	}	
}