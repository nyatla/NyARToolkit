package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * RGB画像をHSV画像に変換します。
 *
 */
public class NyARRasterFilter_Rgb2Hsv implements INyARRasterFilter
{
	private IdoFilterImpl _dofilterimpl;
	public NyARRasterFilter_Rgb2Hsv(int i_raster_type) throws NyARException
	{
		switch (i_raster_type) {
		case NyARBufferType.BYTE1D_B8G8R8_24:
			this._dofilterimpl=new IdoFilterImpl_BYTE1D_B8G8R8_24();
			break;
		case NyARBufferType.BYTE1D_R8G8B8_24:
		default:
			throw new NyARException();
		}
	}
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		this._dofilterimpl.doFilter(i_input,i_output,i_input.getSize());
	}
	
	abstract class IdoFilterImpl
	{
		public abstract void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException;
		
	}
	class IdoFilterImpl_BYTE1D_B8G8R8_24 extends IdoFilterImpl
	{
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
		{
			assert(		i_input.isEqualBufferType(NyARBufferType.INT1D_X7H9S8V8_32));
			
			int[] out_buf = (int[]) i_output.getBuffer();
			byte[] in_buf = (byte[]) i_input.getBuffer();
			int s;
			for(int i=i_size.h*i_size.w-1;i>=0;i--)
			{
				int r=(in_buf[i*3+2] & 0xff);
				int g=(in_buf[i*3+1] & 0xff);
				int b=(in_buf[i*3+0] & 0xff);
				int cmax,cmin;
				//最大値と最小値を計算
				if(r>g){
					cmax=r;
					cmin=g;
				}else{
					cmax=g;
					cmin=r;
				}
				if(b>cmax){
					cmax=b;
				}
				if(b<cmin){
					cmin=b;
				}
				int h;
				if(cmax==0) {
					s=0;
					h=0;
				}else {
					s=(cmax-cmin)*255/cmax;
					int cdes=cmax-cmin;
					//H成分を計算
					if(cdes!=0){
						if(cmax==r){
							h=(g-b)*60/cdes;
						}else if(cmax==g){
							h=(b-r)*60/cdes+2*60;
						}else{
							h=(r-g)*60/cdes+4*60;
						}
					}else{
						h=0;
					}
				}
				if(h<0)
				{
					h+=360;
				}
				//hsv変換(h9s8v8)
				out_buf[i]=(0x1ff0000&(h<<16))|(0x00ff00&(s<<8))|(cmax&0xff);
			}
			return;
		}
	}	
}