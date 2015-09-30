package jp.nyatla.nyartoolkit.dev.pro.core.rasterfiltr;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.pixel.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARDefocusFilterFactory
{
	/**
	 * こ�?�関数は、Defocusフィルターを生成します�??
	 * �?適化されて�?る形式�?�以下�?�通りです�??
	 * <ul>
	 * <li></li>
	 * </ul>
	 * @param i_raster
	 * @return
	 * @throws NyARRuntimeException
	 */
	public static INyARDefocusFilter createDriver(INyARGrayscaleRaster i_raster) throws NyARRuntimeException
	{
		switch(i_raster.getBufferType()){
		default:
			return new DefocusFilter_Any(i_raster);
		}
	}
}

class DefocusFilter_Any implements INyARDefocusFilter
{
	private INyARGrayscaleRaster _ref_raster;
	private INyARGrayscaleRaster _tmp_raster;
	public DefocusFilter_Any(INyARGrayscaleRaster i_raster) throws NyARRuntimeException
	{
		this._ref_raster=i_raster;
	}
	public void doFilter(INyARGrayscaleRaster i_output,int i_loop) throws NyARRuntimeException
	{
		assert(i_loop>0);
		assert(this._ref_raster!=i_output);
		switch(i_loop){
		case 1:
			this.doFilter(i_output);
			break;
		default:
			//BufferTypeが異なるか、テンポラリバッファが無�?場合�?��?ンポラリバッファを構�?
			if(this._tmp_raster==null || i_output.isEqualBufferType(this._tmp_raster.getBufferType())){
				NyARIntSize s=this._ref_raster.getSize();
				this._tmp_raster=new NyARGrayscaleRaster(s.w,s.h,NyARBufferType.INT1D_GRAY_8,true);
			}
			//loop
			if(i_loop%2==0){
				this.doFilter(this._tmp_raster);
			}else{
				this.doFilter(i_output);
			}
			//インタフェイスください�?
			INyARDefocusFilter dft=new DefocusFilter_Any(this._tmp_raster);
			INyARDefocusFilter dfo=new DefocusFilter_Any(i_output);
			//dstに出力�?��?
			for(int i=i_loop-2;i>=0;i--){
				if(i%2==1){
					//out raster
					dfo.doFilter(this._tmp_raster);
				}else{
					//tmp laster
					dft.doFilter(i_output);
				}
			}
			break;
		}
		return;
	}
	
	
	public void doFilter(INyARGrayscaleRaster i_output) throws NyARRuntimeException
	{
		assert(i_output!=this._ref_raster);
	    NyARIntSize s=this._ref_raster.getSize();
        //edge部�?は何もしな�?よ�??
        INyARGsPixelDriver src=this._ref_raster.getGsPixelDriver();
        INyARGsPixelDriver dst=i_output.getGsPixelDriver();
        for(int i=s.w-1;i>=0;i--){
        	dst.setPixel(i, 0,src.getPixel(i,0));
        	dst.setPixel(i,s.h-1,src.getPixel(i,s.h-1));
        }
        for(int i=s.h-1;i>=1;i--){
        	dst.setPixel(0, i,src.getPixel(0,i));
        	dst.setPixel(s.w-1,i,src.getPixel(s.w-1,i));
        }
        //エ�?ジを除�?た部�?
        for(int y=s.h-2;y>0;y--){
	        for(int x=s.w-2;x>0;x--){
                dst.setPixel(x,y,
                		(src.getPixel(x-1,y-1)+src.getPixel(x  ,y-1)+src.getPixel(x+1,y-1)+
                		src.getPixel(x-1,y  )+src.getPixel(x  ,y  )+src.getPixel(x+1,y  )+
                		src.getPixel(x-1,y+1)+src.getPixel(x  ,y+1)+src.getPixel(x+1,y+1))/9);
	        }
        } 
	}
}