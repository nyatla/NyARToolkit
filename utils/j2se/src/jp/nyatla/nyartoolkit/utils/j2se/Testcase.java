package jp.nyatla.nyartoolkit.utils.j2se;

import java.awt.image.BufferedImage;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.raster.NyARRasterAnalyzer_Histogram;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve192;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.nyar.NyARMarkerSystem;
import jp.nyatla.nyartoolkit.nyar.NyARSensor;

/**
 * {@link NyARBufferType#OBJECT_Java_BufferedImage}に対応した{@link NyARRasterFilter_Rgb2Gs_RgbAve192}
 *
 */
class NyARRasterFilter_Rgb2Gs_RgbAve192_J2SE extends NyARRasterFilter_Rgb2Gs_RgbAve192
{
	public NyARRasterFilter_Rgb2Gs_RgbAve192_J2SE(int iInRasterType,int iOutRasterType) throws NyARException
	{
		super(iInRasterType, iOutRasterType);
	}
	protected INyARRasterFilter_Rgb2Gs_RgbAve192_Filter createFilter(int i_in_raster_type,int i_out_raster_type)
	{
		if(i_in_raster_type==NyARBufferType.OBJECT_Java_BufferedImage){
			if(i_out_raster_type==NyARBufferType.INT1D_GRAY_8){
				return new Filter_Jbi2Ig8();
			}
		}
		return super.createFilter(i_in_raster_type, i_out_raster_type);
	}
	private class Filter_Jbi2Ig8 implements INyARRasterFilter_Rgb2Gs_RgbAve192_Filter
	{
		public boolean isSupport(INyARRaster i_input,INyARRaster i_output)
		{
			return i_input.isEqualBufferType(NyARBufferType.OBJECT_Java_BufferedImage) && i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8);
		}
		public void doCutFilter(INyARRaster i_input, int l,int t,int i_st,NyARGrayscaleRaster o_output) throws NyARException
		{
			assert(i_input.isEqualBufferType(NyARBufferType.OBJECT_Java_BufferedImage));
			assert(i_input.getSize().isInnerSize(l+o_output.getWidth()*i_st,t+o_output.getHeight()*i_st));
			final int[] input=(int[])i_input.getBuffer();
			final int[] output=(int[])o_output.getBuffer();
			int v;
			int pt_src,pt_dst;
			NyARIntSize dest_size=o_output.getSize();			
			NyARIntSize src_size=i_input.getSize();
			int skip_src_y=(src_size.w-dest_size.w*i_st)+src_size.w*(i_st-1);
			int skip_src_x=i_st;
			final int pix_count=dest_size.w;
			final int pix_mod_part=pix_count-(pix_count%8);			
			//左上から1行づつ走査していく
			pt_dst=0;
			pt_src=(t*src_size.w+l);
			for (int y = dest_size.h-1; y >=0; y-=1){
				int x;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
				}
				for (;x>=0;x-=8){
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
					v=input[pt_src++];output[pt_dst++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					pt_src+=skip_src_x;
				}
				//スキップ
				pt_src+=skip_src_y;
			}
			return;		
		}
		public void doFilter(INyARRaster i_input, int[] o_output,int l,int t,int w,int h)
		{
			assert(i_input.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
			NyARIntSize size=i_input.getSize();
			int[] in_buf = (int[]) i_input.getBuffer();
			int bp = (l+t*size.w);
			int v;
			final int b=t+h;
			final int row_padding_dst=(size.w-w);
			final int row_padding_src=row_padding_dst;
			final int pix_count=w;
			final int pix_mod_part=pix_count-(pix_count%8);
			int src_ptr=t*size.w+l;
			for (int y = t; y < b; y++) {
				int x=0;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
				}
				for (;x>=0;x-=8){
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
					v=in_buf[src_ptr++];o_output[bp++]=(((v>>16)& 0xff)+((v>>8)& 0xff)+(v &0xff))>>2;
				}
				bp+=row_padding_dst;
				src_ptr+=row_padding_src;
			}
			return;			
		}
	}
	
}
class NyARSensorJ2Se extends NyARSensor
{
	NyARBufferedImageRaster _bir;
	public NyARSensorJ2Se(NyARParam i_param) throws NyARException
	{
		super(i_param);
		NyARIntSize s=i_param.getScreenSize();
		//ラップ用のラスタ
		this._bir=new NyARBufferedImageRaster(s.w,s.h,false);
	}
	/**
	 * 処理系初期化の上書き
	 */
	protected void initResource(NyARIntSize s) throws NyARException
	{
		this._rgb2gs=new NyARRasterFilter_Rgb2Gs_RgbAve192(NyARBufferType.INT1D_GRAY_8,NyARBufferType.BYTE1D_B8G8R8_24);
		this._gs_raster=new NyARGrayscaleRaster(s.w,s.h,NyARBufferType.INT1D_GRAY_8,true);
		this._gs_hist=new NyARHistogram(256);
		this._hist_make=new NyARRasterAnalyzer_Histogram(NyARBufferType.INT1D_GRAY_8,4);
	}
	
	

	/**
	 * インスタンスにBufferedImageをセットします。
	 * @param i_ref_img
	 * @throws NyARException
	 */
	void update(BufferedImage i_ref_img) throws NyARException
	{
		this._bir.wrapImage(i_ref_img);
		super.update(this._bir);
	}
}
class NyARJ2seMarkerSystem extends NyARMarkerSystem
{
	
}

public class Testcase {
	void main()
	{
		try{
			NyARSensor s=new NyARSensorJ2Se(null);
	//		NyARRender r=new Render(config);
			NyARMarkerSystem rel=new NyARMarkerSystem(s,null);
			rel.addNyIdMarker(0,80);
			for(;;){
				//状況を更新する。
				rel.update();
				//状況からレンダラに書き込む。
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
